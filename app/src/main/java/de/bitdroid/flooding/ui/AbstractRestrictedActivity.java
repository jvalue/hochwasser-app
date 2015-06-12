package de.bitdroid.flooding.ui;

import de.bitdroid.flooding.auth.RestrictedResource;

/**
 * Base activity which contains restricted content that users must be
 * logged in to interact with.
 */
public class AbstractRestrictedActivity extends AbstractActivity implements RestrictedResource {

	@Override
	public void logout() {
		uiUtils.logout(this);
	}

}
