package de.bitdroid.flooding.widget;

import android.content.Context;
import android.content.SharedPreferences;

import org.roboguice.shaded.goole.common.base.Optional;

import javax.inject.Inject;

/**
 * Stores additional data for widgets.
 */
public class WidgetDataManager {

	private static final String PREFS_NAME = "WidgetDataManager";

	private final Context context;

	@Inject
	WidgetDataManager(Context context) {
		this.context = context;
	}

	public void storeGaugeId(int widgetId, String gaugeId) {
		SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
		editor.putString(String.valueOf(widgetId), gaugeId);
		editor.apply();
	}

	public Optional<String> fetchGaugeId(int widgetId) {
		String gaugeId = context
				.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
				.getString(String.valueOf(widgetId), null);
		return Optional.fromNullable(gaugeId);
	}

	public void clearGaugeId(int widgetId) {
		SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
		editor.remove(String.valueOf(widgetId));
		editor.apply();
	}

}
