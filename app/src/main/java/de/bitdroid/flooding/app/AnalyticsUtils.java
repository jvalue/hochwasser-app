package de.bitdroid.flooding.app;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import javax.inject.Inject;

public class AnalyticsUtils {

	public static final String
			CATEGORY_UX = "UX";

	public static final String
			ACTION_CLICK = "click",
			ACTION_SWIPE = "swipe";


	private final Tracker tracker;

	@Inject
	AnalyticsUtils(Tracker tracker) {
		this.tracker = tracker;
	}


	public void onScreen(String screenName) {
		tracker.setScreenName(screenName);
	}


	public void onClick(String label) {
		tracker.send(new HitBuilders.EventBuilder()
				.setCategory(CATEGORY_UX)
				.setAction(ACTION_CLICK)
				.setLabel(label)
				.build());
	}


	public void onSwipe(String label) {
		tracker.send(new HitBuilders.EventBuilder()
				.setCategory(CATEGORY_UX)
				.setAction(ACTION_SWIPE)
				.setLabel(label)
				.build());
	}


	public void onException(String description, boolean isFatal) {
		tracker.send(new HitBuilders.ExceptionBuilder()
				.setDescription(description)
				.setFatal(isFatal)
				.build());
	}

}
