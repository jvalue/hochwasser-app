package de.bitdroid.flooding.levels;

import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_TYPE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_NAME;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_WATER_NAME;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.app.Service;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.pegelonline.PegelOnlineSource;
import de.bitdroid.flooding.utils.Log;

public class ChooseRiverFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private static final int LOADER_ID = 44;

	private ArrayAdapter<Entry> listAdapter = null;
	private EditText searchBox = null;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}


    @Override
	public View onCreateView(
			LayoutInflater inflater,
			ViewGroup container,
			Bundle savedInstanceState) {


		View view = inflater.inflate(R.layout.select_water, container, false);

		// search box
		searchBox = (EditText) view.findViewById(R.id.search);
		searchBox.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence text, int arg1, int arg2, int arg3) {
				listAdapter.getFilter().filter(text);
			}

			@Override
			public void beforeTextChanged(CharSequence text, int arg1, int arg2, int arg3) { }

			@Override
			public void afterTextChanged(Editable e) { }
		});

		// show stations on long click
		ListView listView = (ListView) view.findViewById(android.R.id.list);
		listView.setLongClickable(true);
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
				Entry e = listAdapter.getItem(position);
				for (String name : e.getStationNames())
					Log.debug(name);
				return true;
			}
		});

		return view;
    }


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// list adapter
		listAdapter = new ArrayAdapter<Entry>(
				getActivity().getApplicationContext(), 
				android.R.layout.simple_list_item_1);
		setListAdapter(listAdapter);
	}


	@Override
	public void onResume() {
		super.onResume();
		getLoaderManager().initLoader(LOADER_ID, null, this);
	}
	

	@Override
	public void onListItemClick(ListView list, View item, int position, long id) {
		// hide keyboard
		InputMethodManager inputManager 
			= (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);

		// start graph activity
		String waterName = listAdapter.getItem(position).getWaterName();
		Bundle extras = new Bundle();
		extras.putString(GraphActivity.EXTRA_WATER_NAME, waterName);
		Intent intent = new Intent(
				getActivity().getApplicationContext(),
				GraphActivity.class);
		intent.putExtras(extras);
		startActivity(intent);
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.select_water_menu, menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch(menuItem.getItemId()) {
			case R.id.search:
				InputMethodManager inputManager = (InputMethodManager) getActivity()
					.getSystemService(Service.INPUT_METHOD_SERVICE);
				if (searchBox.getVisibility() == View.GONE) {
					searchBox.setVisibility(View.VISIBLE);
					inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
					inputManager.showSoftInput(searchBox, InputMethodManager.SHOW_IMPLICIT);
					searchBox.requestFocus();
				} else {
					searchBox.setVisibility(View.GONE);
					inputManager.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
				}
				return true;
		}
		return super.onOptionsItemSelected(menuItem);
	}


	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		if (id != LOADER_ID) return null;

		return new CursorLoader(
				getActivity().getApplicationContext(),
				PegelOnlineSource.INSTANCE.toUri(),
				new String[] { COLUMN_WATER_NAME, COLUMN_STATION_NAME },
				COLUMN_LEVEL_TYPE + "=?", 
				new String[] { "W" }, 
				null);
	}


	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (loader.getId() != LOADER_ID) return;

		int waterIdx = cursor.getColumnIndex(COLUMN_WATER_NAME);
		int stationIdx = cursor.getColumnIndex(COLUMN_STATION_NAME);

		Map<String, Entry> waterNames = new HashMap<String, Entry>();

		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				String wName = cursor.getString(waterIdx);
				String sName = cursor.getString(stationIdx);

				if (!waterNames.containsKey(wName)) waterNames.put(wName, new Entry(wName));
				waterNames.get(wName).addStation(sName);
			} while (cursor.moveToNext());
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
	public void onLoaderReset(Loader<Cursor> loader) {
		listAdapter.clear();
		listAdapter.notifyDataSetChanged();
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
