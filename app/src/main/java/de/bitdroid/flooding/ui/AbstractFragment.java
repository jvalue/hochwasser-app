package de.bitdroid.flooding.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.auth.RestrictedResource;
import roboguice.fragment.provided.RoboFragment;
import roboguice.inject.InjectView;
import rx.subscriptions.CompositeSubscription;

/**
 * Base fragment class.
 */
public abstract class AbstractFragment extends RoboFragment implements RestrictedResource {

	@Inject private UiUtils uiUtils;
	@InjectView(R.id.spinner) private View spinnerContainerView;
	@InjectView(R.id.spinner_image) private ImageView spinnerImageView;

	protected CompositeSubscription compositeSubscription = new CompositeSubscription();

	private final int layoutResource;

	AbstractFragment(int layoutResource) {
		this.layoutResource = layoutResource;
	}


	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(layoutResource, container, false);
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		compositeSubscription.unsubscribe();
		compositeSubscription = new CompositeSubscription();
	}


	@Override
	public void logout() {
		uiUtils.logout(this.getActivity());
	}


	public void showSpinner() {
		uiUtils.showSpinner(spinnerContainerView, spinnerImageView);
	}


	public void hideSpinner() {
		uiUtils.hideSpinner(spinnerContainerView, spinnerImageView);
	}

}
