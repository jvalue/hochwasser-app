package de.bitdroid.flooding.testUtils;


import android.content.Context;
import android.content.SharedPreferences;
import android.test.RenamingDelegatingContext;

public class PrefsRenamingDelegatingContext extends RenamingDelegatingContext {

	private final String prefix;

	public PrefsRenamingDelegatingContext(Context targetContext, String prefix) {
		super(targetContext, prefix);
		this.prefix = prefix;
	}


	@Override
	public SharedPreferences getSharedPreferences(String prefsName, int mode) {
		return super.getSharedPreferences(prefix + prefsName, mode);
	}

}
