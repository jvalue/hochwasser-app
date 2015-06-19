package de.bitdroid.flooding.utils;


import android.content.Context;
import android.content.SharedPreferences;

/**
 * Easier android shared preferences handling.
 */
public class PreferenceUtils {

	private final Context context;
	private final String prefsName;

	PreferenceUtils(Context context, String prefsName) {
		this.context = context;
		this.prefsName = prefsName;
	}


	public void set(String key, String value) {
		SharedPreferences.Editor editor = getPrefs().edit();
		editor.putString(key, value);
		editor.commit();
	}


	public void set(String key, long value) {
		SharedPreferences.Editor editor = getPrefs().edit();
		editor.putLong(key, value);
		editor.commit();
	}


	public String get(String key) {
		return getPrefs().getString(key, null);
	}


	public void remove(String key) {
		SharedPreferences.Editor editor = getPrefs().edit();
		editor.remove(key);
		editor.commit();
	}


	public boolean contains(String key) {
		return getPrefs().contains(key);
	}


	private SharedPreferences getPrefs() {
		return context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
	}

}
