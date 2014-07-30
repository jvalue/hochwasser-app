package de.bitdroid.flooding.news;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.Map;


final class NewsLoader extends AsyncTaskLoader<Map<NewsItem, Boolean>> implements NewsUpdateListener {

	private final NewsManager manager;
	private Map<NewsItem, Boolean> newsItems;

	private boolean monitoringAlarms = false;

	public NewsLoader(Context context) {
		super(context);
		this.manager = NewsManager.getInstance(context);
	}


	@Override
	public Map<NewsItem, Boolean> loadInBackground() {
		return manager.getAllItems();
	}


	@Override
	public void deliverResult(Map<NewsItem, Boolean> newsItems) {
		if (isReset()) return;

		this.newsItems = newsItems;

		if (isStarted()) {
			super.deliverResult(newsItems);
		}
	}


	@Override
	protected void onStartLoading() {
		if (newsItems != null) deliverResult(newsItems);

		if (!monitoringAlarms) {
			monitoringAlarms = true;
			manager.registerListener(this);
		}

		if (takeContentChanged() || newsItems == null) forceLoad();
	}


	@Override
	protected void onStopLoading() {
		cancelLoad();
	}


	@Override
	public void onCanceled(Map<NewsItem, Boolean> newsItems) {
		super.onCanceled(newsItems);
	}


	@Override
	protected void onReset() {
		onStopLoading();
		newsItems = null;

		if (monitoringAlarms) {
			monitoringAlarms = false;
			manager.unregisterListener(this);
		}
	}


	@Override
	public void onNewItem(NewsItem item) {
		onContentChanged();
	}


	@Override
	public void onDeletedItem(NewsItem item) {
		onContentChanged();
	}


	@Override
	public void onAllItemsRead() {
		onContentChanged();
	}

}
