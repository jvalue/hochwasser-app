package de.bitdroid.flooding.utils;


import android.content.Context;

import javax.inject.Inject;

/**
 * Easier android shared preferences handling.
 */
public class PreferenceUtilsFactory {

	private final Context context;

	@Inject
	PreferenceUtilsFactory(Context context) {
		this.context = context;
	}

	public PreferenceUtils createUtils(String prefsName) {
		return new PreferenceUtils(context, prefsName);
	}

}
