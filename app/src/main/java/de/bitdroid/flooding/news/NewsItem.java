package de.bitdroid.flooding.news;

import com.google.common.base.Objects;
import com.orm.SugarRecord;

import de.bitdroid.flooding.utils.Assert;


/**
 * One piece of news which can be displayed in the news section.
 * News are sortable by timestamp, putting the newest item first.
 */
public final class NewsItem extends SugarRecord<NewsItem> implements Comparable<NewsItem> {

	private String title, content;
	private long timestamp;
	private boolean navigationEnabled;
	private int navigationPos;
	private boolean isNew;

	public NewsItem() { }

	public NewsItem(
			String title, 
			String content, 
			long timestamp,
			boolean navigationEnabled,
			int navigationPos,
			boolean isNew) {

		this.title = title;
		this.content = content;
		this.timestamp = timestamp;
		this.navigationEnabled = navigationEnabled;
		this.navigationPos = navigationPos;
		this.isNew = isNew;
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


	public boolean getIsNew() {
		return isNew;
	}


	public void setIsNew(boolean isNew) {
		this.isNew = isNew;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		NewsItem newsItem = (NewsItem) o;
		return Objects.equal(timestamp, newsItem.timestamp) &&
				Objects.equal(navigationEnabled, newsItem.navigationEnabled) &&
				Objects.equal(navigationPos, newsItem.navigationPos) &&
				Objects.equal(title, newsItem.title) &&
				Objects.equal(content, newsItem.content) &&
				Objects.equal(isNew, newsItem.isNew);
	}


	@Override
	public int hashCode() {
		return Objects.hashCode(title, content, timestamp, navigationEnabled, navigationPos, isNew);
	}


	@Override
	public int compareTo(NewsItem another) {
		return -Long.valueOf(timestamp).compareTo(another.getTimestamp());
	}


	public static class Builder {

		private final String title, content;
		private final long timestamp;
		private boolean navigationEnabled = false;
		private int navigationPos = -1;
		private boolean isNew = false;

		public Builder(String title, String content, long timestamp) {
			Assert.assertNotNull(title, content);
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


		public Builder isNew(boolean isNew) {
			this.isNew = isNew;
			return this;
		}


		public NewsItem build() {
			return new NewsItem(title, content, timestamp, navigationEnabled, navigationPos, isNew);
		}
	}

}
