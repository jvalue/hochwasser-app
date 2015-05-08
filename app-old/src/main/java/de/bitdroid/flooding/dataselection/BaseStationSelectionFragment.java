package de.bitdroid.flooding.dataselection;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.pegelonline.PegelOnlineSource;
import de.bitdroid.flooding.utils.Assert;
import de.bitdroid.flooding.utils.StringUtils;

import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_TYPE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_NAME;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_WATER_NAME;

public abstract class BaseStationSelectionFragment extends BaseSelectionFragment<String> implements Extras {

	private static final int LOADER_ID = 45;

	private static final String EXTRA_SHOW_ALL_STATIONS_ENTRY = "EXTRA_SHOW_ALL_STATIONS_ENTRY";

	protected static void addArguments(
			BaseStationSelectionFragment fragment,
			String waterName,
			boolean showAllStationsEntry) {

		Assert.assertNotNull(waterName);

		Bundle args = fragment.getArguments();
		if (args == null) args = new Bundle();
		args.putString(EXTRA_WATER_NAME, waterName);
		args.putBoolean(EXTRA_SHOW_ALL_STATIONS_ENTRY, showAllStationsEntry);
		fragment.setArguments(args);
	}


	protected abstract void onStationClicked(String waterName, String stationName);
	protected abstract void onWaterClicked(String waterName);
	protected abstract void onMapClicked(String waterName);


	@Override
	protected final void onItemClicked(String stationName) {
		if (stationName.equals(getString(R.string.data_station_all))) onWaterClicked(getWaterName());
		else onStationClicked(getWaterName(), stationName);
	}


	@Override
	protected final void onMapClicked() {
		onMapClicked(getWaterName());
	}


	@Override
	protected ArrayAdapter<String> getAdapter() {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				getActivity().getApplicationContext(), 
				android.R.layout.simple_list_item_1,
				android.R.id.text1) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				String station = getItem(position);
				View view = super.getView(position, convertView, parent);

				TextView text1 = (TextView) view.findViewById(android.R.id.text1);
				text1.setText(StringUtils.toProperCase(station));

				// if all stations entry --> highlight
				if (isShowingAllEntries() && station.equals(getString(R.string.data_station_all))) {
					text1.setTextColor(getResources().getColor(R.color.blue_dark));
				} else {
					text1.setTextColor(getResources().getColor(R.color.black));
				}

				return view;
			}
		};

		addExtraEntries(adapter);
		return adapter;
	}


	@Override
	protected int getLoaderId() {
		return LOADER_ID;
	}


	@Override
	protected Loader<Cursor> getLoader() {
		return new CursorLoader(
				getActivity().getApplicationContext(),
				PegelOnlineSource.INSTANCE.toUri(),
				new String[] { COLUMN_STATION_NAME },
				COLUMN_WATER_NAME + "=? AND " + COLUMN_LEVEL_TYPE + "=?", 
				new String[] { getWaterName(), "W" },
				null);
	}


	@Override
	protected void onLoadFinished(Cursor cursor, ArrayAdapter<String> listAdapter) {
		int waterIdx = cursor.getColumnIndex(COLUMN_STATION_NAME);

		List<String> stations = new LinkedList<String>();

		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				stations.add(cursor.getString(waterIdx));
			} while (cursor.moveToNext());
		}
		
		listAdapter.clear();
		listAdapter.addAll(stations);
		listAdapter.sort(new Comparator<String>() {
			@Override
			public int compare(String e1, String e2) {
				return e1.compareTo(e2);
			}
		});
		addExtraEntries(listAdapter);
		listAdapter.getFilter().filter("");
	}


	@Override
	protected final int getSearchHintStringId() {
		return R.string.menu_select_station_search_hint;
	}


	private String getWaterName() {
		return getArguments().getString(EXTRA_WATER_NAME);
	}


	private void addExtraEntries(ArrayAdapter<String> adapter) {
		if (isShowingAllEntries()) adapter.insert(getActivity().getString(R.string.data_station_all), 0);

	}


	private boolean isShowingAllEntries() {
		return getArguments().getBoolean(EXTRA_SHOW_ALL_STATIONS_ENTRY, false);
	}

}
