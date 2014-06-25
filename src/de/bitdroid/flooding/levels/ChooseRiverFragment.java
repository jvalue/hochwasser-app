package de.bitdroid.flooding.levels;

import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_TYPE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_NAME;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_WATER_NAME;

import java.util.Comparator;
import java.util.HashMap;
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.pegelonline.PegelOnlineSource;
import de.bitdroid.flooding.utils.StringUtils;

public class ChooseRiverFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private static final int LOADER_ID = 44;

	private ArrayAdapter<River> listAdapter = null;
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

		return view;
    }


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// list adapter
		listAdapter = new ArrayAdapter<River>(
				getActivity().getApplicationContext(), 
				android.R.layout.simple_list_item_2,
				android.R.id.text1) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				TextView text1 = (TextView) view.findViewById(android.R.id.text1);
				TextView text2 = (TextView) view.findViewById(android.R.id.text2);

				River river = getItem(position);
				text1.setText(StringUtils.toProperCase(river.getWaterName()));
				text2.setText(getString(R.string.waters_station_count, river.getStationCount()));
				return view;
			}
			
		};
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
		getActivity().overridePendingTransition(
				R.anim.slide_enter_from_right, 
				R.anim.slide_exit_to_left);
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

		Map<String, River> waterNames = new HashMap<String, River>();

		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				String wName = cursor.getString(waterIdx);
				if (!waterNames.containsKey(wName)) waterNames.put(wName, new River(wName));
				waterNames.get(wName).addStation();
			} while (cursor.moveToNext());
		}
		
		listAdapter.clear();
		listAdapter.addAll(waterNames.values());
		listAdapter.sort(new Comparator<River>() {
			@Override
			public int compare(River e1, River e2) {
				return e1.getWaterName().compareTo(e2.getWaterName());
			}
		});
		listAdapter.getFilter().filter("");
	}


	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		listAdapter.clear();
	}


	private static final class River {
		private final String waterName;
		private int stationCount = 0;

		public River(String waterName) {
			this.waterName = waterName;
		}

		public void addStation() {
			stationCount++;
		}

		public String getWaterName() {
			return waterName;
		}

		public int getStationCount() {
			return stationCount;
		}

		@Override
		public String toString() {
			return waterName;
		}
	}

}
