package de.bitdroid.flooding.utils;


import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;

public final class PreferenceManager {

	private final Context context;

	@Inject
	PreferenceManager(Context context) {
		this.context = context;
	}


	public void set(String key, String value) {
		SharedPreferences.Editor editor = android.preference.PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putString(key, value);
		editor.commit();
	}


	public void set(String key, long value) {
		SharedPreferences.Editor editor = android.preference.PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putLong(key, value);
		editor.commit();
	}


	public String get(String key) {
		return android.preference.PreferenceManager.getDefaultSharedPreferences(context).getString(key, null);
	}


	public void clear(String key) {
		SharedPreferences.Editor editor = android.preference.PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.remove(key);
		editor.commit();
	}

}
