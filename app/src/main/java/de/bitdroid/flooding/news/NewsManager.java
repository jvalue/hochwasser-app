package de.bitdroid.flooding.news;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.main.MainActivity;
import de.bitdroid.utils.Assert;


public final class NewsManager {

	private static final int NOTIFICATION_ID = 4211;
	private static NewsManager instance;

	public static NewsManager getInstance(Context context) {
		Assert.assertNotNull(context);
		if (instance == null) instance = new NewsManager(context);
		return instance;
	}


	private final Context context;
	private final SQLiteOpenHelper dbHelper;
	private final List<NewsUpdateListener> listeners = new LinkedList<NewsUpdateListener>();


	private NewsManager(Context context) {
		this.context = context;
		this.dbHelper = new NewsDatabase(context);
	}


	/**
	 * @return all news items mapped to their status: true if they have been read, false otherwise.
	 */
	public Map<NewsItem, Boolean> getAllItems() {
		Map<NewsItem, Boolean> items = new HashMap<NewsItem, Boolean>();

		synchronized (this) {
			Cursor cursor = null;
			try {
				cursor = dbHelper.getReadableDatabase()
						.rawQuery("SELECT * FROM " + NewsDatabase.TABLE_NAME, null);

				if (cursor.getCount() == 0) return items;
				cursor.moveToFirst();

				int titleIdx = cursor.getColumnIndex(NewsDatabase.COLUMN_TITLE);
				int contentIdx = cursor.getColumnIndex(NewsDatabase.COLUMN_CONTENT);
				int timestampIdx = cursor.getColumnIndex(NewsDatabase.COLUMN_TIMESTAMP);
				int navEnabledIdx = cursor.getColumnIndex(NewsDatabase.COLUMN_NAVIGATION_ENABLED);
				int navPosIdx = cursor.getColumnIndex(NewsDatabase.COLUMN_NAVIGATION_POS);
				int readPos = cursor.getColumnIndex(NewsDatabase.COLUMN_READ);

				do {
					NewsItem.Builder builder = new NewsItem.Builder(
							cursor.getString(titleIdx),
							cursor.getString(contentIdx),
							cursor.getLong(timestampIdx));
					if (cursor.getInt(navEnabledIdx) > 0)
						builder.setNavigationPos(cursor.getInt(navPosIdx));
					else
						builder.disableNavigation();

					if (cursor.getInt(readPos) > 0) items.put(builder.build(), true);
					else items.put(builder.build(), false);

				} while (cursor.moveToNext());
			} finally {
				if (cursor != null) cursor.close();
			}
		}

		return items;
	}


	public void markAllItemsRead() {
		NotificationManager manager
			= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(NOTIFICATION_ID);

		synchronized (this) {
			SQLiteDatabase database = null;
			try {
				database = dbHelper.getWritableDatabase();
				ContentValues values = new ContentValues();
				values.put(NewsDatabase.COLUMN_READ, 1);
				int updateCount = database.update(NewsDatabase.TABLE_NAME, values, null, null);
				if (updateCount > 0) {
					for (NewsUpdateListener listener : listeners) {
						listener.onAllItemsRead();
					}
				}
			} finally {
				if (database != null) database.close();
			}
		}
	}


	public NewsItem addItem(
			String title,
			String content,
			int navPos,
			boolean navigationEnabled,
			boolean showNotification) {

		Assert.assertNotNull(title, content);
		Assert.assertTrue(!navigationEnabled || (navPos > 0 && navPos < 4), "invalid nav pos");

		NewsItem.Builder builder = new NewsItem.Builder(title, content, System.currentTimeMillis());
		if (navigationEnabled) builder.setNavigationPos(navPos);
		NewsItem item = builder.build();

		addItem(item, showNotification);
		return item;
	}


	public void addItem(NewsItem item, boolean showNotification) {
		Assert.assertNotNull(item);

		// insert into db
		synchronized (this) {
			ContentValues values = new ContentValues();
			values.put(NewsDatabase.COLUMN_TITLE, item.getTitle());
			values.put(NewsDatabase.COLUMN_CONTENT, item.getContent());
			values.put(NewsDatabase.COLUMN_TIMESTAMP, item.getTimestamp());
			values.put(NewsDatabase.COLUMN_NAVIGATION_ENABLED, item.isNavigationEnabled());
			values.put(NewsDatabase.COLUMN_NAVIGATION_POS, item.getNavigationPos());
			values.put(NewsDatabase.COLUMN_READ, false);

			alertListeners(item, true);

			SQLiteDatabase database = null;
			try {
				database = dbHelper.getWritableDatabase();
				database.insert(NewsDatabase.TABLE_NAME, null, values);
			} finally {
				if (database != null) database.close();
			}
		}

		if (!showNotification) return;

		// show user notification
		Intent intent = new Intent(context, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(intent);

		PendingIntent pendingIntent = stackBuilder.getPendingIntent(
				0,
				PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder builder =  new NotificationCompat.Builder(context)
			.setSmallIcon(R.drawable.ic_menu_home)
			.setContentTitle(item.getTitle())
			.setContentText(item.getContent())
			.setAutoCancel(true)
			.setLights(context.getResources().getColor(R.color.notification_light), 1000, 3000)
			.setContentIntent(pendingIntent);


		NotificationManager manager
			= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(NOTIFICATION_ID, builder.build());
	}


	public void removeItem(NewsItem item) {
		Assert.assertNotNull(item);

		// remove from db
		synchronized (this) {
			SQLiteDatabase database = null;
			try {
				database = dbHelper.getWritableDatabase();
				int count = database.delete(NewsDatabase.TABLE_NAME,
						NewsDatabase.COLUMN_TITLE + "=? AND "
								+ NewsDatabase.COLUMN_CONTENT + "=? AND "
								+ NewsDatabase.COLUMN_TIMESTAMP + "=? AND "
								+ NewsDatabase.COLUMN_NAVIGATION_ENABLED + "=? AND "
								+ NewsDatabase.COLUMN_NAVIGATION_POS + "=?",
						new String[]{
								item.getTitle(),
								item.getContent(),
								String.valueOf(item.getTimestamp()),
								String.valueOf(item.isNavigationEnabled() ? 1 : 0),
								String.valueOf(item.getNavigationPos()),
						}
				);
			} finally {
				if (database != null) database.close();
			}
		}

		// alert about deletion
		alertListeners(item, false);
	}


	public void registerListener(NewsUpdateListener listener) {
		Assert.assertNotNull(listener);
		this.listeners.add(listener);
	}


	public void unregisterListener(NewsUpdateListener listener) {
		Assert.assertNotNull(listener);
		this.listeners.remove(listener);
	}


	private void alertListeners(NewsItem item, boolean added) {
		for (NewsUpdateListener listener : listeners) {
			if (added) listener.onNewItem(item);
			else listener.onDeletedItem(item);
		}
	}

}
