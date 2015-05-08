package de.bitdroid.flooding.utils;

import android.content.Context;

import java.io.File;

public class SharedPreferencesHelper {

	private SharedPreferencesHelper() { }

	public static void clearAll(PrefsRenamingDelegatingContext context) {
		if (context == null) throw new NullPointerException("context cannot be null");
		File dir = new File(context.getFilesDir().getParent() + "/shared_prefs/");
		String[] prefsNames = dir.list();
		if (prefsNames == null || prefsNames.length == 0) return;

		for (String prefs : prefsNames) {
			if (!prefs.startsWith(context.getPrefix())) continue;
			context.getTargetContext().getSharedPreferences(
					prefs.replace(".xml", ""),
					Context.MODE_PRIVATE)
					.edit().clear().commit();

		}
	}

}
