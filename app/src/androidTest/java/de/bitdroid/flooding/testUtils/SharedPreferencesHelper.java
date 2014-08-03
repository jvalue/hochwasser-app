package de.bitdroid.flooding.testUtils;

import android.content.Context;

import java.io.File;

public class SharedPreferencesHelper {

	private SharedPreferencesHelper() { }

	public static void clearAll(Context context) {
		File dir = new File(context.getFilesDir().getParent() + "/shared_prefs/");
		String[] prefsNames = dir.list();
		for (String prefs : prefsNames) {
			context.getSharedPreferences(
					prefs.replace(".xml", ""),
					Context.MODE_PRIVATE)
					.edit().clear().commit();
		}
	}

}
