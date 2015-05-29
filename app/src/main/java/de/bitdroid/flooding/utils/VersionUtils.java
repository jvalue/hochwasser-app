package de.bitdroid.flooding.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import javax.inject.Inject;

import timber.log.Timber;


public class VersionUtils {

	private final Context context;

	@Inject
	VersionUtils(Context context) {
		this.context = context;
	}

	public String getVersion() {
		try {
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException nnfe) {
			Timber.e(nnfe, "failed to get version");
			return null;
		}
	}
}
