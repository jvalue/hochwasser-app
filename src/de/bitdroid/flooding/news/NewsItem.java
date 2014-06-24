package de.bitdroid.flooding.news;

import de.bitdroid.flooding.utils.Assert;


public final class NewsItem implements Comparable<NewsItem> {

	private final String title, content;
	private final String id;
	private final long timestamp;

	NewsItem(String id, String title, String content, long timestamp) {
		Assert.assertNotNull(id, title, content);
		this.id = id;
		this.title = title;
		this.content = content;
		this.timestamp = timestamp;
	}


	public String getId() {
		return id;
	}


	public String getTitle() {
		return title;
	}

	
	public String getContent() {
		return content;
	}


	public long getTimestamp() {
		return timestamp;
	}


	@Override
	public String toString() {
		return title + ": " + content + " (" + timestamp + ")";
	}

	
	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof NewsItem)) return false;
		NewsItem item = (NewsItem) other;
		return id.equals(item.id)
			&& title.equals(item.title) 
			&& content.equals(item.content)
			&& timestamp == item.timestamp;
	}

	
	@Override
	public int hashCode() {
		final int MULT = 17;
		int hash = 13;
		hash = hash + MULT * id.hashCode();
		hash = hash + MULT * title.hashCode();
		hash = hash + MULT * content.hashCode();
		hash = hash + MULT * Long.valueOf(timestamp).hashCode();
		return hash;
	}


	@Override
	public int compareTo(NewsItem other) {
		return new Long(timestamp).compareTo(other.timestamp);
	}

}
