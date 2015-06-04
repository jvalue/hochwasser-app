package de.bitdroid.flooding.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.auth.LoginManager;
import de.bitdroid.flooding.auth.RestrictedResource;
import roboguice.fragment.provided.RoboFragment;
import roboguice.inject.InjectView;

/**
 * Base fragment class.
 */
abstract class AbstractFragment extends RoboFragment implements RestrictedResource {

	@Inject private LoginManager loginManager;
	@Inject private UiUtils uiUtils;
	@InjectView(R.id.spinner) private View spinnerContainerView;
	@InjectView(R.id.spinner_image) private ImageView spinnerImageView;

	private final int layoutResource;

	AbstractFragment(int layoutResource) {
		this.layoutResource = layoutResource;
	}


	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(layoutResource, container, false);
	}


	@Override
	public void logout() {
		loginManager.clearToken();
		loginManager.clearAccountName();
		Intent intent = new Intent(getActivity(), LoginActivity.class);
		startActivity(intent);
		getActivity().finish();
	}


	public void showSpinner() {
		uiUtils.showSpinner(spinnerContainerView, spinnerImageView);
	}


	public void hideSpinner() {
		uiUtils.hideSpinner(spinnerContainerView, spinnerImageView);
	}

}