package de.bitdroid.flooding.levels;

import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_WATER_NAME;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.ListActivity;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import de.bitdroid.flooding.pegelonline.PegelOnlineSource;
import de.bitdroid.flooding.utils.AbstractLoaderCallbacks;

public class LevelsActivity extends ListActivity {
	
	private static final int LOADER_ID = 44;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		final ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(
				getApplicationContext(), 
				android.R.layout.simple_list_item_1);

		setListAdapter(listAdapter);

		AbstractLoaderCallbacks loader = new AbstractLoaderCallbacks(LOADER_ID) {

			@Override
			protected void onLoadFinishedHelper(Loader<Cursor> loader, Cursor cursor) {
				if (cursor == null) return;
				cursor.moveToFirst();
				int waterIdx = cursor.getColumnIndex(COLUMN_WATER_NAME);

				Map<String, Integer> waterNames = new HashMap<String, Integer>();
				while (cursor.moveToNext()) {
					String name = cursor.getString(waterIdx);
					if (!waterNames.containsKey(name)) waterNames.put(name, 1);
					else waterNames.put(name, waterNames.get(name) + 1);
				}
				
				List<String> adapterValues = new LinkedList<String>();
				for (Entry<String, Integer> e : waterNames.entrySet())
					adapterValues.add(e.getKey() + " (" + e.getValue() + ")");

				listAdapter.clear();
				listAdapter.addAll(adapterValues);
				listAdapter.sort(new Comparator<String>() {
					@Override
					public int compare(String s1, String s2) {
						return s1.compareTo(s2);
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
}
