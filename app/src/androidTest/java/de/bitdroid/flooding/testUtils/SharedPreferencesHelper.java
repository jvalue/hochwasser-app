package de.bitdroid.flooding.testUtils;

import android.content.Context;

import java.io.File;

import de.bitdroid.flooding.utils.Assert;
import de.bitdroid.flooding.utils.Log;

public class SharedPreferencesHelper {

	private SharedPreferencesHelper() { }

	public static void clearAll(Context context, String prefix) {
		Assert.assertNotNull(context, prefix);

		File dir = new File(context.getFilesDir().getParent() + "/shared_prefs/");
		String[] prefsNames = dir.list();
		Log.debug("prefix is " + prefix);
		for (String prefs : prefsNames) {
			Log.debug("prefs = " + prefs);
			if (!prefs.startsWith(prefix)) continue;
			context.getSharedPreferences(
					prefs.replace(".xml", ""),
					Context.MODE_PRIVATE)
					.edit().clear().commit();

			Log.debug("deleted");
		}
	}

}
