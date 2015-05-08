package de.bitdroid.flooding.app;

import de.bitdroid.utils.Assert;


final class NavItem {
	
	private final String title;
	private final int iconId;

	public NavItem(String title, int iconId) {
		Assert.assertNotNull(title);
		this.title = title;
		this.iconId = iconId;
	}


	public String getTitle() {
		return title;
	}


	public int getIconId() {
		return iconId;
	}


	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof NavItem)) return false;
		NavItem item = (NavItem) other;
		return title.equals(item.title) && iconId == item.iconId;
	}


	@Override
	public int hashCode() {
		final int MULT = 17;
		int hash = 13;
		hash = hash + title.hashCode() * MULT;
		hash = hash + iconId * MULT;
		return hash;
	}

}
