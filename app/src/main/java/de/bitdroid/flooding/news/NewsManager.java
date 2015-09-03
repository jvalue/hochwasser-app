package de.bitdroid.flooding.news;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.List;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.ui.MainDrawerActivity;
import rx.Observable;
import rx.functions.Func0;


/**
 * Handles news items.
 */
public class NewsManager {

	private static final int NOTIFICATION_ID = 4211;

	private final Context context;
	private final NotificationManager notificationManager;


	@Inject
	NewsManager(Context context, NotificationManager notificationManager) {
		this.context = context;
		this.notificationManager = notificationManager;
	}


	public Observable<List<NewsItem>> getAllNews() {
		return Observable.defer(new Func0<Observable<List<NewsItem>>>() {
			@Override
			public Observable<List<NewsItem>> call() {
				return Observable.just(NewsItem.listAll(NewsItem.class));
			}
		});
	}


	public void markAllItemsRead() {
		// stop notifications
		notificationManager.cancel(NOTIFICATION_ID);

		// mark items as read
		List<NewsItem> newItems = NewsItem.find(NewsItem.class, "isNew = ?", String.valueOf(true));
		for (NewsItem item : newItems) {
			item.setIsNew(false);
			item.save();
		}

	}


	public NewsItem addItem(
			String title,
			String content,
			int navId,
			boolean navigationEnabled,
			boolean isWarning,
			boolean showNotification) {

		NewsItem.Builder builder = new NewsItem.Builder(title, content, System.currentTimeMillis());
		if (navigationEnabled) builder.setNavigationId(navId);
		builder.isWarning(isWarning);
		NewsItem item = builder.build();

		addItem(item, showNotification);
		return item;
	}


	public void addItem(NewsItem item, boolean showNotification) {
		item.save();

		// show user notification
		if (!showNotification) return;
		Intent intent = new Intent(context, MainDrawerActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addParentStack(MainDrawerActivity.class);
		stackBuilder.addNextIntent(intent);

		PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.ic_duck_small)
				.setContentTitle(item.getTitle())
				.setStyle(new NotificationCompat.BigTextStyle().bigText(item.getContent()))
				.setContentText(item.getContent())
				.setAutoCancel(true)
				.setLights(context.getResources().getColor(R.color.notification_light), 1000, 3000)
				.setContentIntent(pendingIntent);

		notificationManager.notify(NOTIFICATION_ID, builder.build());
	}


	public void removeItem(NewsItem item) {
		item.delete();
	}

}
