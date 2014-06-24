package de.bitdroid.flooding.news;

import java.util.HashSet;
import java.util.Set;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import de.bitdroid.flooding.MainActivity;
import de.bitdroid.flooding.R;
import de.bitdroid.flooding.utils.Assert;


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


	private NewsManager(Context context) {
		this.context = context;

		addItem(new NewsItem.Builder(
				"Alarms",
				"If you want to be alarmed when water levels reach a certain level, head over to the alarms section!",
				System.currentTimeMillis())
			.setNavigationPos(1)
			.build(),
			true);

		addItem(new NewsItem.Builder(
				"Data",
				"Want more details about the current water sitation? Check our the data section!",
				System.currentTimeMillis())
			.setNavigationPos(2)
			.build(),
			true);
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
	}


	public NewsItem addItem(String title, String content, boolean showNotification) {
		Assert.assertNotNull(title, content);
		NewsItem item = new NewsItem.Builder(
				title, 
				content, 
				System.currentTimeMillis())
			.build();
		addItem(item, showNotification);
		return item;
	}


	public void addItem(NewsItem item, boolean showNotification) {
		unreadItems.add(item);

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
	}

}
