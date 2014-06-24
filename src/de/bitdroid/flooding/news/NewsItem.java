package de.bitdroid.flooding.news;

import de.bitdroid.flooding.utils.Assert;


public final class NewsItem implements Comparable<NewsItem> {

	private final String title, content;
	private final String id;
	private final long timestamp;
	private final boolean navigationEnabled;
	private final int navigationPos;

	private NewsItem(
			String id, 
			String title, 
			String content, 
			long timestamp,
			boolean navigationEnabled,
			int navigationPos) {

		this.id = id;
		this.title = title;
		this.content = content;
		this.timestamp = timestamp;
		this.navigationEnabled = navigationEnabled;
		this.navigationPos = navigationPos;
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


	public boolean isNavigationEnabled() {
		return navigationEnabled;
	}


	public int getNavigationPos() {
		return navigationPos;
	}


	
	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof NewsItem)) return false;
		NewsItem item = (NewsItem) other;
		return id.equals(item.id)
			&& title.equals(item.title) 
			&& content.equals(item.content)
			&& timestamp == item.timestamp
			&& navigationEnabled == item.navigationEnabled
			&& navigationPos == item.navigationPos;
	}

	
	@Override
	public int hashCode() {
		final int MULT = 17;
		int hash = 13;
		hash = hash + MULT * id.hashCode();
		hash = hash + MULT * title.hashCode();
		hash = hash + MULT * content.hashCode();
		hash = hash + MULT * Long.valueOf(timestamp).hashCode();
		hash = hash + MULT * (navigationEnabled ? 1 : 0);
		hash = hash + MULT * Integer.valueOf(navigationPos).hashCode();
		return hash;
	}


	@Override
	public int compareTo(NewsItem other) {
		return new Long(timestamp).compareTo(other.timestamp);
	}


	public static class Builder {

		private final String id, title, content;
		private final long timestamp;
		private boolean navigationEnabled = false;
		private int navigationPos = -1;

		public Builder(String id, String title, String content, long timestamp) {
			Assert.assertNotNull(id, title, content);
			this.id = id;
			this.title = title;
			this.content = content;
			this.timestamp = timestamp;
		}


		public Builder setNavigationPos(int navigationPos) {
			navigationEnabled = true;
			this.navigationPos = navigationPos;
			return this;
		}


		public Builder disableNavigation() {
			navigationEnabled = false;
			return this;
		}


		public NewsItem build() {
			return new NewsItem(id, title, content, timestamp, navigationEnabled, navigationPos);
		}
	}

}
