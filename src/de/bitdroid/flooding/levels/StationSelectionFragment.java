package de.bitdroid.flooding.levels;

import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_TYPE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_NAME;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_WATER_NAME;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.pegelonline.PegelOnlineSource;
import de.bitdroid.flooding.utils.StringUtils;

public final class StationSelectionFragment extends DataSelectionFragment<String> {

	private static final int LOADER_ID = 45;
	private static final String EXTRA_RIVERNAME = "EXTRA_RIVERNAME";


	public static StationSelectionFragment newInstance(String riverName) {
		StationSelectionFragment fragment = new StationSelectionFragment();
		Bundle args = new Bundle();
		args.putString(EXTRA_RIVERNAME, riverName);
		fragment.setArguments(args);
		return fragment;
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

				return view;
			}
		};

		adapter.insert(getActivity().getString(R.string.data_station_all), 0);
		return adapter;
	}


	@Override
	protected Intent getActivityIntent(String station) {
		Bundle extras = new Bundle();
		if (station.equals(getActivity().getString(R.string.data_station_all))) {
			extras.putString(
					RiverGraphActivity.EXTRA_WATER_NAME, 
					getArguments().getString(EXTRA_RIVERNAME));
			Intent intent = new Intent(
					getActivity().getApplicationContext(),
					RiverGraphActivity.class);
			intent.putExtras(extras);
			return intent;

		} else {
			extras.putString(StationGraphActivity.EXTRA_STATION_NAME, station);
			extras.putString(
					StationGraphActivity.EXTRA_WATER_NAME, 
					getArguments().getString(EXTRA_RIVERNAME));
			Intent intent = new Intent(
					getActivity().getApplicationContext(),
					StationGraphActivity.class);
			intent.putExtras(extras);
			return intent;
		}
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
				new String[] { getArguments().getString(EXTRA_RIVERNAME), "W" }, 
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
		listAdapter.insert(getActivity().getString(R.string.data_station_all), 0);
		listAdapter.getFilter().filter("");
	}

}
