package de.bitdroid.flooding.ui;

import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.ods.OdsManager;
import de.bitdroid.flooding.ods.Station;
import de.bitdroid.flooding.utils.StringUtils;
import rx.Observable;
import rx.functions.Func1;

public abstract class AbstractStationSelectionActivity extends AbstractListSelectionActivity<Station> {

	private static final Station ALL_STATIONS = new Station(
			"a " + AbstractWaterSelectionActivity.class.getSimpleName() + ".ALL_STATIONS",
			"a " + AbstractWaterSelectionActivity.class.getSimpleName() + ".ALL_STATIONS",
			null,
			-1f, -1f, -1f);

	@Inject private OdsManager odsManager;
	private final boolean showAllStationsEntry;

	public AbstractStationSelectionActivity(boolean showAllStationsEntry) {
		super(R.string.select_station, R.string.menu_select_station_search_hint, R.layout.item_data);
		this.showAllStationsEntry = showAllStationsEntry;
	}


	protected abstract void onAllStationsSelected();
	protected abstract void onStationSelected(Station station);


	@Override
	public void onDataSelected(Station station) {
		if (station.equals(ALL_STATIONS)) onAllStationsSelected();
		else onStationSelected(station);
	}


	@Override
	protected Observable<List<Station>> doLoadItems() {
		return odsManager.getStationsByBodyOfWater(stationSelection.getWater())
				.flatMap(new Func1<List<Station>, Observable<List<Station>>>() {
					@Override
					public Observable<List<Station>> call(List<Station> stations) {
						if (showAllStationsEntry) stations.add(0, ALL_STATIONS);
						return Observable.just(stations);
					}
				});
	}


	@Override
	protected void setDataView(final Station station, View view) {
		if (station.equals(ALL_STATIONS)) {
			view.findViewById(R.id.container_2).setVisibility(View.VISIBLE);
			view.findViewById(R.id.container_1).setVisibility(View.GONE);

		} else {
			view.findViewById(R.id.container_1).setVisibility(View.VISIBLE);
			view.findViewById(R.id.container_2).setVisibility(View.GONE);

			// station name
			TextView text1 = (TextView) view.findViewById(android.R.id.text1);
			text1.setText(StringUtils.toProperCase(station.getStationName()));
			text1.setTextColor(getResources().getColor(android.R.color.black));

			// hide second text
			view.findViewById(android.R.id.text2).setVisibility(View.GONE);
		}
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
