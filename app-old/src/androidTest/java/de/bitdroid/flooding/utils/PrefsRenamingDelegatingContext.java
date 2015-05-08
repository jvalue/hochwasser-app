package de.bitdroid.flooding.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.test.RenamingDelegatingContext;

public class PrefsRenamingDelegatingContext extends RenamingDelegatingContext {

	private final Context targetContext;
	private final String prefix;

	public PrefsRenamingDelegatingContext(Context targetContext, String prefix) {
		super(targetContext, prefix);
		this.targetContext = targetContext;
		this.prefix = prefix;
	}


	@Override
	public SharedPreferences getSharedPreferences(String prefsName, int mode) {
		if (prefsName.contains(prefix)) super.getSharedPreferences(prefsName, mode);
		return super.getSharedPreferences(prefix + prefsName, mode);
	}


	@Override
	public Context getApplicationContext() {
		return this;
	}


	String getPrefix() {
		return prefix;
	}


	Context getTargetContext() {
		return targetContext;
	}

}
