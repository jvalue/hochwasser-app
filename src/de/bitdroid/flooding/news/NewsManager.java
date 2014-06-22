package de.bitdroid.flooding.news;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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


	private final Map<String, NewsItem> unreadItems = new HashMap<String, NewsItem>();
	private final Map<String, NewsItem> readItems = new HashMap<String, NewsItem>();
	private final Context context;


	private NewsManager(Context context) {
		this.context = context;
		addItem("Welcome", "... to the flooding app!", true);
		addItem("By the way", "... you have got news!", true);
	}


	public List<NewsItem> getAllItems() {
		List<NewsItem> ret = new LinkedList<NewsItem>();
		ret.addAll(readItems.values());
		ret.addAll(unreadItems.values());
		return ret;
	}


	public List<NewsItem> getUnreadItems() {
		return new LinkedList<NewsItem>(unreadItems.values());
	}


	public List<NewsItem> getReadItems() {
		return new LinkedList<NewsItem>(readItems.values());
	}


	public NewsItem getById(String id) {
		Assert.assertNotNull(id);
		if (unreadItems.containsKey(id)) return unreadItems.get(id);
		else return readItems.get(id);
	}


	public void markAllItemsRead() {
		readItems.putAll(unreadItems);
		unreadItems.clear();

		NotificationManager manager 
			= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(NOTIFICATION_ID);
	}


	public NewsItem addItem(String title, String content, boolean showNotification) {
		Assert.assertNotNull(title, content);

		NewsItem item = new NewsItem(
				UUID.randomUUID().toString(), 
				title, 
				content, 
				System.currentTimeMillis());

		unreadItems.put(item.getId(), item);

		if (!showNotification) return item;

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
			.setContentTitle(title)
			.setContentText(content)
			.setAutoCancel(true)
			.setNumber(unreadItems.size())
			.setContentIntent(pendingIntent);


		NotificationManager manager 
			= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(NOTIFICATION_ID, builder.build());

		return item;
	}


	public void removeItem(NewsItem item) {
		Assert.assertNotNull(item);
		Assert.assertTrue(
				unreadItems.containsKey(item.getId()) || readItems.containsKey(item.getId()), 
				"item not present");

		unreadItems.remove(item.getId());
		readItems.remove(item.getId());
	}

}
