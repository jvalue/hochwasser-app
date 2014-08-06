package de.bitdroid.flooding.testUtils;

import android.content.Context;

import java.io.File;

import de.bitdroid.utils.Assert;

public class SharedPreferencesHelper {

	private SharedPreferencesHelper() { }

	public static void clearAll(PrefsRenamingDelegatingContext context) {
		Assert.assertNotNull(context);

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
