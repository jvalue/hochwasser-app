package de.bitdroid.flooding.ui;

import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.ods.OdsManager;
import de.bitdroid.flooding.ods.Station;
import de.bitdroid.flooding.utils.StringUtils;
import rx.Observable;
import rx.functions.Func1;

abstract class AbstractStationSelectionActivity extends AbstractListSelectionActivity<Station> {

	@Inject private OdsManager odsManager;

	public AbstractStationSelectionActivity() {
		super(R.string.menu_select_station_search_hint, R.layout.item_data);
	}


	@Override
	protected Observable<List<Station>> loadItems() {
		return odsManager
				.getStationsByBodyOfWater(stationSelection.getWater())
				.flatMap(new Func1<List<Station>, Observable<List<Station>>>() {
					@Override
					public Observable<List<Station>> call(List<Station> stations) {
						Collections.sort(stations, new Comparator<Station>() {
							@Override
							public int compare(Station lhs, Station rhs) {
								return lhs.getStationName().compareTo(rhs.getStationName());
							}
						});
						return Observable.just(stations);
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
