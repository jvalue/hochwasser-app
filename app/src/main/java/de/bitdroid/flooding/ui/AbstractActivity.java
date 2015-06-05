package de.bitdroid.flooding.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.auth.LoginManager;
import de.bitdroid.flooding.auth.RestrictedResource;
import de.bitdroid.flooding.network.NetworkUtils;
import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectView;

/**
 * Base activity class.
 */
public class AbstractActivity extends RoboActionBarActivity implements RestrictedResource {

	@Inject private LoginManager loginManager;
	@Inject private UiUtils uiUtils;
	@Inject protected NetworkUtils networkUtils;
	@InjectView(R.id.spinner) private View spinnerContainerView;
	@InjectView(R.id.spinner_image) private ImageView spinnerImageView;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// action bar back button
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
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


	@Override
	public void logout() {
		loginManager.clearToken();
		loginManager.clearAccountName();
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
		finish();
	}


	public void showSpinner() {
		uiUtils.showSpinner(spinnerContainerView, spinnerImageView);
	}


	public void hideSpinner() {
		uiUtils.hideSpinner(spinnerContainerView, spinnerImageView);
	}

}
