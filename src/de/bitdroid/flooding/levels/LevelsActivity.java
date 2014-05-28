package de.bitdroid.flooding.levels;

import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_WATER_NAME;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

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

				Set<String> waterNames = new HashSet<String>();
				while (cursor.moveToNext()) {
					waterNames.add(cursor.getString(waterIdx));
				}
				listAdapter.clear();
				listAdapter.addAll(waterNames);
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
