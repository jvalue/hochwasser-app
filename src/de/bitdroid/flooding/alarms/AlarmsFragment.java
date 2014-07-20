package de.bitdroid.flooding.alarms;

import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import de.bitdroid.flooding.R;


public final class AlarmsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Map<Long, Alarm>> {

	private static final int LOADER_ID  = 47;

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

		View view = inflater.inflate(R.layout.alarms_list, container, false);


		setListAdapter(new ArrayAdapter<Map.Entry<Long, Alarm>>(
					getActivity().getApplicationContext(),
					R.layout.alarms_item,
					android.R.id.text1) { 

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				Map.Entry<Long, Alarm> entry = getItem(position);
				View view = super.getView(position, convertView, parent);
				LevelAlarm alarm = (LevelAlarm) entry.getValue();

				TextView text1 = (TextView) view.findViewById(android.R.id.text1);
				text1.setText(alarm.getStation());

				TextView text2 = (TextView) view.findViewById(android.R.id.text2);
				text2.setText("Alarm when at " + alarm.getLevel());

				return view;
			}

		});
		return view;
	}


	@Override
	public void onResume() {
		super.onResume();
		getLoaderManager().initLoader(LOADER_ID, null, this);
	}



	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.alarms_menu, menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.add:
				Intent intent = new Intent(
						getActivity().getApplicationContext(), 
						NewAlarmActivity.class);
				startActivity(intent);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	@SuppressWarnings("unchecked")
	public ArrayAdapter<Map.Entry<Long, Alarm>> getListAdapter() {
		return (ArrayAdapter<Map.Entry<Long, Alarm>>) super.getListAdapter();
	}


	@Override
	public Loader<Map<Long, Alarm>> onCreateLoader(int id, Bundle bundle) {
		if (id != LOADER_ID) return null;
		return new AlarmLoader(getActivity().getApplicationContext());
	}


	@Override
	public void onLoadFinished(Loader<Map<Long, Alarm>> loader, Map<Long, Alarm> alarms) {
		if (loader.getId() != LOADER_ID) return;
		getListAdapter().clear();
		getListAdapter().addAll(alarms.entrySet());
	}


	@Override
	public void onLoaderReset(Loader<Map<Long, Alarm>> loader) {
		getListAdapter().clear();
	}

}
