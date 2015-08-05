package de.bitdroid.flooding.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.app.AnalyticsUtils;
import de.bitdroid.flooding.network.NetworkUtils;
import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectView;
import rx.subscriptions.CompositeSubscription;

/**
 * Base activity class.
 */
public class AbstractActivity extends RoboActionBarActivity {

	@Inject protected UiUtils uiUtils;
	@Inject protected NetworkUtils networkUtils;
	@Inject protected AnalyticsUtils analyticsUtils;
	@InjectView(R.id.spinner) private View spinnerContainerView;
	@InjectView(R.id.spinner_image) private ImageView spinnerImageView;
	protected CompositeSubscription compositeSubscription = new CompositeSubscription();


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// action bar back button
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setHomeButtonEnabled(true);
			actionBar.setDisplayShowHomeEnabled(true);
		}
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


	public void showSpinner() {
		uiUtils.showSpinner(spinnerContainerView, spinnerImageView);
	}


	public void hideSpinner() {
		uiUtils.hideSpinner(spinnerContainerView, spinnerImageView);
	}


	public boolean isSpinnerVisible() {
		return uiUtils.isSpinnerVisible(spinnerContainerView);
	}
}
