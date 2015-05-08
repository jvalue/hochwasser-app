package de.bitdroid.flooding.ui;

import android.content.Intent;

import javax.inject.Inject;

import de.bitdroid.flooding.auth.LoginManager;
import roboguice.activity.RoboActivity;

/**
 * An activity which requires the user to be authenticated
 */
public class AbstractRestrictedActivity extends RoboActivity {

	@Inject
	LoginManager loginManager;

	public void logout() {
		loginManager.clearToken();
		loginManager.clearAccountName();
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
		finish();
	}

}
