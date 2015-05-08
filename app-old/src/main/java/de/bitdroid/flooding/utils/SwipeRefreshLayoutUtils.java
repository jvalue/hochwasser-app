package de.bitdroid.flooding.utils;


import android.support.v4.widget.SwipeRefreshLayout;

import de.bitdroid.flooding.R;

public final class SwipeRefreshLayoutUtils {

	public static void setDefaultColors(SwipeRefreshLayout layout) {
		layout.setColorScheme(R.color.refresh_blue_light, R.color.refresh_blue, R.color.refresh_blue_dark, R.color.refresh_yellow);
	}
}
