package de.bitdroid.flooding.ui;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.gcm.GcmManager;
import de.bitdroid.flooding.network.NetworkUtils;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * Container activity for the main navigation drawer.
 */
public class MainDrawerActivity extends AbstractRoboDrawerActivity {

	@Inject private GcmManager gcmManager;
	@Inject private NetworkUtils networkUtils;

	@Override
	public void init(Bundle bundle) {
		addSection(newSection(getString(R.string.nav_home), new NewsFragment()));
		Intent intent = new Intent(this, DataSelectionHandler.WaterSelectionActivity.class);
		addSection(newSection(getString(R.string.nav_alarms), new AlarmsFragment()));
		addSection(newSection(getString(R.string.nav_analysis), intent));

		// register for gcm updates
		if (!gcmManager.isRegistered()) {
			gcmManager
					.register()
					.compose(networkUtils.<Void>getDefaultTransformer())
					.subscribe(new Action1<Void>() {
						@Override
						public void call(Void aVoid) {
							Timber.d("GCM registration successful");
						}
					}, new Action1<Throwable>() {
						@Override
						public void call(Throwable throwable) {
							Toast.makeText(MainDrawerActivity.this, "failed to register for GCM", Toast.LENGTH_LONG).show();
							Timber.e(throwable, "failed to register GCM");
						}
					});
		}
	}

}
