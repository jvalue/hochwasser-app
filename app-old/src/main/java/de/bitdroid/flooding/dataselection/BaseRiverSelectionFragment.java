package de.bitdroid.flooding.dataselection;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.dataselection.BaseRiverSelectionFragment.River;
import de.bitdroid.flooding.pegelonline.PegelOnlineSource;
import de.bitdroid.flooding.utils.StringUtils;

import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_TYPE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_NAME;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_WATER_NAME;


public abstract class BaseRiverSelectionFragment extends BaseSelectionFragment<River> {

	private static final int LOADER_ID = 44;


	@Override
	protected ArrayAdapter<River> getAdapter() {
		return new ArrayAdapter<River>(
				getActivity().getApplicationContext(), 
				R.layout.data_item,
				android.R.id.text1) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				River river = getItem(position);
				View view = super.getView(position, convertView, parent);

				TextView text1 = (TextView) view.findViewById(android.R.id.text1);
				text1.setText(StringUtils.toProperCase(river.getRiverName()));
				text1.setTextColor(getResources().getColor(R.color.black));

				TextView text2 = (TextView) view.findViewById(android.R.id.text2);
				text2.setText(getString(R.string.data_station_count, river.getStationsCount()));
				text2.setTextColor(getResources().getColor(R.color.gray));

				return view;
			}
			
		};
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
				new String[] { COLUMN_WATER_NAME, COLUMN_STATION_NAME },
				COLUMN_LEVEL_TYPE + "=?", 
				new String[] { "W" }, 
				null);
	}


	@Override
	protected void onLoadFinished(Cursor cursor, ArrayAdapter<River> listAdapter) {
		int waterIdx = cursor.getColumnIndex(COLUMN_WATER_NAME);

		Map<String, River> items = new HashMap<String, River>();

		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				String wName = cursor.getString(waterIdx);
				if (!items.containsKey(wName)) items.put(wName, new River(wName));
				items.get(wName).addStation();
			} while (cursor.moveToNext());
		}
		
		listAdapter.clear();
		listAdapter.addAll(items.values());
		listAdapter.sort(new Comparator<River>() {
			@Override
			public int compare(River e1, River e2) {
				return e1.getRiverName().compareTo(e2.getRiverName());
			}
		});
		listAdapter.getFilter().filter("");
	}


	@Override
	protected final int getSearchHintStringId() {
		return R.string.menu_select_water_search_hint;
	}


	// TOOD remove
	public static final class River {
		private final String riverName;
		private int stationsCount;

		public River(String riverName) {
			this.riverName = riverName;
		}

		public void addStation() {
			stationsCount++;
		}

		public int getStationsCount() {
			return stationsCount;
		}

		public String getRiverName() {
			return riverName;
		}

		@Override
		public String toString() {
			return riverName;
		}
	}

}
