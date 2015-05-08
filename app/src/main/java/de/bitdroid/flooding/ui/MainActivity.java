package de.bitdroid.flooding.ui;


import android.os.Bundle;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import de.bitdroid.flooding.network.NetworkUtils;
import de.bitdroid.flooding.ods.OdsManager;
import de.bitdroid.flooding.ods.Station;
import roboguice.activity.RoboListActivity;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * Container activity for the main navigation drawer.
 */
public class MainActivity extends RoboListActivity {

	@Inject private OdsManager odsManager;
	@Inject private NetworkUtils networkUtils;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		odsManager.getStations()
				.compose(networkUtils.<List<Station>>getDefaultTransformer())
				.subscribe(new Action1<List<Station>>() {
					@Override
					public void call(List<Station> stations) {
						Set<String> waters = new HashSet<>();
						for (Station station : stations) waters.add(station.getWaterName());
						setListAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, new ArrayList<>(waters)));
					}
				}, new Action1<Throwable>() {
					@Override
					public void call(Throwable throwable) {
						Timber.e(throwable, "failed to get stations");
					}
				});
	}

}
