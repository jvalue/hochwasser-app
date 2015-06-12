package de.bitdroid.flooding.ui;

import android.content.Intent;

import javax.inject.Inject;

import de.bitdroid.flooding.auth.LoginManager;
import de.bitdroid.flooding.auth.RestrictedResource;

/**
 * Base activity which contains restricted content that users must be
 * logged in to interact with.
 */
public class AbstractRestrictedActivity extends AbstractActivity implements RestrictedResource {

	@Inject private LoginManager loginManager;

	@Override
	public void logout() {
		loginManager.clearToken();
		loginManager.clearAccountName();
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
		finish();
	}

}
