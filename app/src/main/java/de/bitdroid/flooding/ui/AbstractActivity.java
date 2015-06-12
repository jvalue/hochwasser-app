package de.bitdroid.flooding.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.network.NetworkUtils;
import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectView;
import rx.subscriptions.CompositeSubscription;

/**
 * Base activity class.
 */
public class AbstractActivity extends RoboActionBarActivity {

	@Inject private UiUtils uiUtils;
	@Inject protected NetworkUtils networkUtils;
	@InjectView(R.id.spinner) private View spinnerContainerView;
	@InjectView(R.id.spinner_image) private ImageView spinnerImageView;
	protected CompositeSubscription compositeSubscription = new CompositeSubscription();


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// action bar back button
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		compositeSubscription.unsubscribe();
		compositeSubscription = new CompositeSubscription();
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}


	protected void showSpinner() {
		uiUtils.showSpinner(spinnerContainerView, spinnerImageView);
	}


	protected void hideSpinner() {
		uiUtils.hideSpinner(spinnerContainerView, spinnerImageView);
	}

}
