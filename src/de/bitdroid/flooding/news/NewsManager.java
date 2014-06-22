package de.bitdroid.flooding.news;

import java.util.LinkedList;
import java.util.List;


public final class NewsManager {

	private static NewsManager INSTANCE;


	public static NewsManager getInstance() {
		if (INSTANCE == null) INSTANCE = new NewsManager();
		return INSTANCE;
	}


	private final List<NewsItem> items = new LinkedList<NewsItem>();

	private NewsManager() {
			addItem("Welcome", "... to the flooding app!");
	}


	public List<NewsItem> getItems() {
		return new LinkedList<NewsItem>(items);
	}


	public void addItem(String title, String content) {
		if (title == null || content == null) 
			throw new NullPointerException("params cannot be null");
		items.add(new NewsItem(title, content, System.currentTimeMillis()));
	}

}
