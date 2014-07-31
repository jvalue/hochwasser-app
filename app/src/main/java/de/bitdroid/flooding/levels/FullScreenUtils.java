package de.bitdroid.flooding.levels;

import android.app.Activity;
import android.os.Build;
import android.view.View;

final class FullScreenUtils {

	private FullScreenUtils() { }


	public static void startFullScreen(Activity activity) {
		int currentApiVersion = Build.VERSION.SDK_INT;
		if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {
			activity.getWindow().getDecorView().setSystemUiVisibility(
							View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_FULLSCREEN
							| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
			);
		}
	}

}
