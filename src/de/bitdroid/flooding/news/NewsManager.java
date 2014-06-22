package de.bitdroid.flooding.news;

import java.util.LinkedList;
import java.util.List;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import de.bitdroid.flooding.MainActivity;
import de.bitdroid.flooding.R;


public final class NewsManager {

	private static final int NOTIFICATION_ID = 4211;
	private static NewsManager instance;

	public static NewsManager getInstance(Context context) {
		if (context == null) throw new NullPointerException("param cannot be null");
		if (instance == null) instance = new NewsManager(context);
		return instance;
	}


	private final List<NewsItem> unreadItems = new LinkedList<NewsItem>();
	private final List<NewsItem> readItems = new LinkedList<NewsItem>();
	private final Context context;


	private NewsManager(Context context) {
		this.context = context;
		addItem("Welcome", "... to the flooding app!", true);
		addItem("By the way", "... you have got news!", true);
	}


	public List<NewsItem> getAllItems() {
		List<NewsItem> ret = new LinkedList<NewsItem>();
		ret.addAll(readItems);
		ret.addAll(unreadItems);
		return ret;
	}


	public List<NewsItem> getUnreadItems() {
		return new LinkedList<NewsItem>(unreadItems);
	}


	public List<NewsItem> getReadItems() {
		return new LinkedList<NewsItem>(readItems);
	}


	public void markAllItemsRead() {
		readItems.addAll(unreadItems);
		unreadItems.clear();

		NotificationManager manager 
			= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(NOTIFICATION_ID);
	}


	public void addItem(String title, String content, boolean showNotification) {
		if (title == null || content == null) 
			throw new NullPointerException("params cannot be null");

		unreadItems.add(new NewsItem(title, content, System.currentTimeMillis()));

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
			.setContentTitle(title)
			.setContentText(content)
			.setAutoCancel(true)
			.setNumber(unreadItems.size())
			.setContentIntent(pendingIntent);


		NotificationManager manager 
			= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(NOTIFICATION_ID, builder.build());
	}


	public void addItem(String title, String content) {
		if (title == null || content == null) 
			throw new NullPointerException("params cannot be null");
	}

}
