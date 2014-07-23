package de.bitdroid.flooding.alarms;

import android.content.Context;

import de.bitdroid.flooding.news.NewsItem;
import de.bitdroid.flooding.news.NewsManager;
import de.bitdroid.flooding.ods.cep.BaseEventReceiver;
import de.bitdroid.flooding.utils.Log;


public final class EventReceiver extends BaseEventReceiver {

	@Override
	protected void onReceive(Context context, String eventId) {
		NewsItem news = new NewsItem.Builder(
				"Alarm triggered!",
				"Alarm triggered event " + eventId,
				System.currentTimeMillis())
			.setNavigationPos(1)
			.build();

		NewsManager.getInstance(context).addItem(news, true);
		Log.info("Received event with id " + eventId);
	}

}
