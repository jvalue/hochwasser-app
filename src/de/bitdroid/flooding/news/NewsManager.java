package de.bitdroid.flooding.news;

import java.util.HashSet;
import java.util.Set;

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

import de.bitdroid.flooding.MainActivity;
import de.bitdroid.flooding.R;
import de.bitdroid.flooding.utils.Assert;
import de.bitdroid.flooding.utils.Log;


public final class NewsManager {

	private static final int NOTIFICATION_ID = 4211;
	private static NewsManager instance;

	public static NewsManager getInstance(Context context) {
		Assert.assertNotNull(context);
		if (instance == null) instance = new NewsManager(context);
		return instance;
	}


	private final Set<NewsItem> unreadItems = new HashSet<NewsItem>();
	private final Set<NewsItem> readItems = new HashSet<NewsItem>();
	private final Context context;
	private final SQLiteOpenHelper dbHelper;


	private NewsManager(Context context) {
		this.context = context;
		this.dbHelper = new NewsDatabase(context);

		readAllItemsFromDb();
	}


	public Set<NewsItem> getAllItems() {
		Set<NewsItem> ret = new HashSet<NewsItem>();
		ret.addAll(readItems);
		ret.addAll(unreadItems);
		return ret;
	}


	public Set<NewsItem> getUnreadItems() {
		return new HashSet<NewsItem>(unreadItems);
	}


	public Set<NewsItem> getReadItems() {
		return new HashSet<NewsItem>(readItems);
	}


	public void markAllItemsRead() {
		readItems.addAll(unreadItems);
		unreadItems.clear();

		NotificationManager manager 
			= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(NOTIFICATION_ID);

		SQLiteDatabase database = null;
		try {
			database = dbHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(NewsDatabase.COLUMN_READ, 1);
			database.update(NewsDatabase.TABLE_NAME, values, null, null);
		} finally {
			if (database != null) database.close();
		}
	}


	public void addItem(NewsItem item, boolean showNotification) {
		Assert.assertFalse(unreadItems.contains(item) && readItems.contains(item), "teim alredy added");
		unreadItems.add(item);
		insertIntoDb(item);

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
			.setNumber(unreadItems.size())
			.setContentIntent(pendingIntent);


		NotificationManager manager 
			= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(NOTIFICATION_ID, builder.build());
	}


	public void removeItem(NewsItem item) {
		Assert.assertNotNull(item);
		Assert.assertTrue(
				unreadItems.contains(item) || readItems.contains(item),
				"item not present");

		unreadItems.remove(item);
		readItems.remove(item);
		deleteItemFromDb(item);
	}


	private void insertIntoDb(NewsItem item) {
		ContentValues values = new ContentValues();
		values.put(NewsDatabase.COLUMN_TITLE, item.getTitle());
		values.put(NewsDatabase.COLUMN_CONTENT, item.getContent());
		values.put(NewsDatabase.COLUMN_TIMESTAMP, item.getTimestamp());
		values.put(NewsDatabase.COLUMN_NAVIGATION_ENABLED, item.isNavigationEnabled());
		values.put(NewsDatabase.COLUMN_NAVIGATION_POS, item.getNavigationPos());
		values.put(NewsDatabase.COLUMN_READ, false);

		SQLiteDatabase database = null;
		try {
			database = dbHelper.getWritableDatabase();
			database.insert(NewsDatabase.TABLE_NAME, null, values);
		} finally {
			if (database != null) database.close();
		}
	}


	private void deleteItemFromDb(NewsItem item) {
		SQLiteDatabase database = null;
		try {
			database = dbHelper.getWritableDatabase();
			String d = 
					NewsDatabase.COLUMN_TITLE + "=? AND "
					+ NewsDatabase.COLUMN_CONTENT + "=? AND "
					+ NewsDatabase.COLUMN_TIMESTAMP + "=? AND "
					+ NewsDatabase.COLUMN_NAVIGATION_ENABLED + "=? AND "
					+ NewsDatabase.COLUMN_NAVIGATION_POS + "=?";
			Log.debug(d);
			String[] s = new String[] {
						item.getTitle(),
						item.getContent(),
						String.valueOf(item.getTimestamp()),
						String.valueOf(item.isNavigationEnabled() ? 1 : 0),
						String.valueOf(item.getNavigationPos())
					};
			for (String st : s) Log.debug(st);

			int count = database.delete(NewsDatabase.TABLE_NAME,
					NewsDatabase.COLUMN_TITLE + "=? AND "
					+ NewsDatabase.COLUMN_CONTENT + "=? AND "
					+ NewsDatabase.COLUMN_TIMESTAMP + "=? AND "
					+ NewsDatabase.COLUMN_NAVIGATION_ENABLED + "=? AND "
					+ NewsDatabase.COLUMN_NAVIGATION_POS + "=?",
					new String[] {
						item.getTitle(),
						item.getContent(),
						String.valueOf(item.getTimestamp()),
						String.valueOf(item.isNavigationEnabled() ? 1 : 0),
						String.valueOf(item.getNavigationPos()),
					});

			Log.debug("DELETED " + count + " ITEMS");

		} finally {
			if (database != null) database.close();
		}
	}


	private void readAllItemsFromDb() {
		Cursor cursor = null;
		try {
			cursor = dbHelper.getReadableDatabase()
				.rawQuery("SELECT * FROM " + NewsDatabase.TABLE_NAME, null);

			if (cursor.getCount() == 0) return;
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

				if (cursor.getInt(readPos) > 0) readItems.add(builder.build());
				else unreadItems.add(builder.build());

			} while (cursor.moveToNext());
		} finally {
			if (cursor != null) cursor.close();
		}
	}

}
