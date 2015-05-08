package de.bitdroid.flooding.news;

public interface NewsUpdateListener {

	public void onNewItem(NewsItem item);
	public void onDeletedItem(NewsItem item);
	public void onAllItemsRead();

}
