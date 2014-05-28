package de.bitdroid.flooding.levels;

import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_WATER_NAME;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import android.app.ListActivity;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import de.bitdroid.flooding.pegelonline.PegelOnlineSource;
import de.bitdroid.flooding.utils.AbstractLoaderCallbacks;

public class LevelsActivity extends ListActivity {
	
	private static final int LOADER_ID = 44;

	private ArrayAdapter<Entry> listAdapter = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		listAdapter = new ArrayAdapter<Entry>(
				getApplicationContext(), 
				android.R.layout.simple_list_item_1);

		setListAdapter(listAdapter);

		AbstractLoaderCallbacks loader = new AbstractLoaderCallbacks(LOADER_ID) {

			@Override
			protected void onLoadFinishedHelper(Loader<Cursor> loader, Cursor cursor) {
				if (cursor == null) return;
				cursor.moveToFirst();
				int waterIdx = cursor.getColumnIndex(COLUMN_WATER_NAME);

				Map<String, Entry> waterNames = new HashMap<String, Entry>();
				while (cursor.moveToNext()) {
					String name = cursor.getString(waterIdx);
					if (!waterNames.containsKey(name)) waterNames.put(name, new Entry(name));
					else waterNames.get(name).incStationsCount();
				}
				
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
						new String[] { COLUMN_WATER_NAME },
						null, null, null);
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
		private int stationsCount;

		Entry(String waterName) {
			this.waterName = waterName;
			this.stationsCount = 1;
		}

		void incStationsCount() {
			stationsCount++;
		}

		String getWaterName() {
			return waterName;
		}

		@Override
		public String toString() {
			return waterName + " (" + stationsCount + ")";
		}
	}
}
