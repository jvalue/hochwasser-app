package de.bitdroid.flooding.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.ods.BodyOfWater;
import de.bitdroid.flooding.ods.OdsManager;
import de.bitdroid.flooding.ods.Station;
import de.bitdroid.flooding.utils.StringUtils;
import rx.Observable;
import rx.functions.Func1;
import timber.log.Timber;

public class StationSelectionActivity extends AbstractSelectionActivity<Station> {

	static final String
			EXTRA_BODY_OF_WATER = "EXTRA_BODY_OF_WATER",
			EXTRA_STATION = "EXTRA_STATION";

	@Inject private OdsManager odsManager;
	private BodyOfWater selectedWater;

	public StationSelectionActivity() {
		super(R.string.menu_select_station_search_hint, R.layout.item_data);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		selectedWater = getIntent().getParcelableExtra(EXTRA_BODY_OF_WATER);
	}


	@Override
	protected void onMapClicked() {
		Timber.d("on map clicked");
	}


	@Override
	protected Observable<List<Station>> loadItems() {
		return odsManager.getStations()
				.flatMap(new Func1<List<Station>, Observable<List<Station>>>() {
					@Override
					public Observable<List<Station>> call(List<Station> stations) {
						List<Station> filteredStations = new ArrayList<>();
						for (Station station : stations) {
							if (station.getBodyOfWater().getName().equals(selectedWater.getName())) filteredStations.add(station);
						}
						return Observable.just(filteredStations);
					}
				});
	}


	@Override
	protected void setDataView(final Station station, View view) {
		// station name
		TextView text1 = (TextView) view.findViewById(android.R.id.text1);
		text1.setText(StringUtils.toProperCase(station.getStationName()));
		text1.setTextColor(getResources().getColor(android.R.color.black));

		// hide second text
		view.findViewById(android.R.id.text2).setVisibility(View.GONE);

		// return on click
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra(EXTRA_STATION, station);
				setResult(RESULT_OK, intent);
				finish();
			}
		});
	}


	@Override
	protected List<Station> filterItems(CharSequence constraint, List<Station> items) {
		List<Station> result = new ArrayList<>(items);
		if (constraint == null || constraint.length() == 0) return result;
		String stringConstraint= constraint.toString().toLowerCase();
		Iterator<Station> iter = result.iterator();
		while (iter.hasNext()) {
			if (!iter.next().getStationName().toLowerCase().contains(stringConstraint)) {
				iter.remove();
			}
		}
		return result;
	}

}
