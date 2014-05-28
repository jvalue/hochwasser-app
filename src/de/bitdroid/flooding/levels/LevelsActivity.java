package de.bitdroid.flooding.levels;

import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_NAME;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_WATER_NAME;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_TYPE;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import de.bitdroid.flooding.pegelonline.PegelOnlineSource;
import de.bitdroid.flooding.utils.AbstractLoaderCallbacks;
import de.bitdroid.flooding.utils.Log;

public class LevelsActivity extends ListActivity {
	
	private static final int LOADER_ID = 44;

	private ArrayAdapter<Entry> listAdapter = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		// list adapter
		listAdapter = new ArrayAdapter<Entry>(
				getApplicationContext(), 
				android.R.layout.simple_list_item_1);
		setListAdapter(listAdapter);


		// show stations on long click
		getListView().setLongClickable(true);
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
				Entry e = listAdapter.getItem(position);
				for (String name : e.getStationNames())
					Log.debug(name);
				return true;
			}
		});

		// data loader
		AbstractLoaderCallbacks loader = new AbstractLoaderCallbacks(LOADER_ID) {

			@Override
			protected void onLoadFinishedHelper(Loader<Cursor> loader, Cursor cursor) {
				if (cursor == null) return;
				cursor.moveToFirst();
				int waterIdx = cursor.getColumnIndex(COLUMN_WATER_NAME);
				int stationIdx = cursor.getColumnIndex(COLUMN_STATION_NAME);

				Map<String, Entry> waterNames = new HashMap<String, Entry>();
				do {
					String wName = cursor.getString(waterIdx);
					String sName = cursor.getString(stationIdx);

					if (!waterNames.containsKey(wName)) waterNames.put(wName, new Entry(wName));
					waterNames.get(wName).addStation(sName);
				} while (cursor.moveToNext());
				
				listAdapter.clear();
				listAdapter.addAll(waterNames.values());
				listAdapter.sort(new Comparator<Entry>() {
					@Override
					public int compare(Entry e1, Entry e2) {
						return e1.getWaterName().compareTo(e2.getWaterName());
					}
				});
				listAdapter.notifyDataSetChanged();
			}

			@Override
			protected void onLoaderResetHelper(Loader<Cursor> loader) { }

			@Override
			protected Loader<Cursor> getCursorLoader() {
				return new CursorLoader(
						getApplicationContext(),
						new PegelOnlineSource().toUri(),
						new String[] { COLUMN_WATER_NAME, COLUMN_STATION_NAME },
						COLUMN_LEVEL_TYPE + "=?", 
						new String[] { "W" }, 
						null);
			}
		};

		getLoaderManager().initLoader(LOADER_ID, null, loader);
    }


	@Override
	protected void onListItemClick(ListView list, View item, int position, long id) {
		String waterName = listAdapter.getItem(position).getWaterName();
		Bundle extras = new Bundle();
		extras.putString(GraphActivity.EXTRA_WATER_NAME, waterName);
		Intent intent = new Intent(
				getApplicationContext(),
				GraphActivity.class);
		intent.putExtras(extras);
		startActivity(intent);
	}


	private static final class Entry {
		private final String waterName;
		private final List<String> stationNames = new LinkedList<String>();

		Entry(String waterName) {
			this.waterName = waterName;
		}

		void addStation(String stationName) {
			this.stationNames.add(stationName);
		}

		String getWaterName() {
			return waterName;
		}

		List<String> getStationNames() {
			return stationNames;
		}

		@Override
		public String toString() {
			return waterName + " (" + stationNames.size() + ")";
		}
	}
}
