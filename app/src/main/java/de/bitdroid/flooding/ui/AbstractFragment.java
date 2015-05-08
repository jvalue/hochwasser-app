package de.bitdroid.flooding.ui;

import android.content.Intent;

import javax.inject.Inject;

import de.bitdroid.flooding.auth.LoginManager;
import de.bitdroid.flooding.auth.RestrictedResource;
import roboguice.fragment.provided.RoboFragment;

/**
 * Base activity class.
 */
public class AbstractFragment extends RoboFragment implements RestrictedResource {

	@Inject private LoginManager loginManager;

	@Override
	public void logout() {
		loginManager.clearToken();
		loginManager.clearAccountName();
		Intent intent = new Intent(getActivity(), LoginActivity.class);
		startActivity(intent);
		getActivity().finish();
	}

}
