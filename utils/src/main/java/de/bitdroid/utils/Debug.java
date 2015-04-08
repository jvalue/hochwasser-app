package de.bitdroid.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;

public final class Debug {

	private Debug() { }

	public static boolean isDebugBuild(Context context) {
		Assert.assertNotNull(context);
		return 0 != (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE );
	}

}
