package de.bitdroid.flooding.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.inject.Inject;

/**
 * Easier android shared preferences handling.
 */
public class PreferenceUtils {

	private final Context context;

	@Inject
	PreferenceUtils(Context context) {
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


	public boolean containsKey(String key) {
		return PreferenceManager.getDefaultSharedPreferences(context).contains(key);
	}

}
