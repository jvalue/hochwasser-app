package de.bitdroid.flooding.alarms;

import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import de.bitdroid.flooding.utils.StringUtils;
import de.timroes.android.listview.EnhancedListView;


public final class AlarmsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Map<Long, Alarm>> {

	private static final int LOADER_ID  = 47;

	private EnhancedListView listView;
	private ArrayAdapter<Map.Entry<Long, Alarm>> listAdapter;
	private AlarmManager alarmManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		alarmManager = AlarmManager.getInstance(getActivity().getApplicationContext());
	}


	@Override
	public View onCreateView(
			LayoutInflater inflater, 
			ViewGroup container, 
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.alarms, container, false);
		listView = (EnhancedListView) view.findViewById(R.id.list);
		listAdapter = new ArrayAdapter<Map.Entry<Long, Alarm>>(
					getActivity().getApplicationContext(),
					R.layout.alarms_item,
					android.R.id.text1) { 
			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				Map.Entry<Long, Alarm> entry = getItem(position);
				View view = super.getView(position, convertView, parent);
				LevelAlarm alarm = (LevelAlarm) entry.getValue();

				TextView text1 = (TextView) view.findViewById(android.R.id.text1);
				text1.setText(
					StringUtils.toProperCase(alarm.getRiver()) + " - " 
					+ StringUtils.toProperCase(alarm.getStation()));

				TextView text2 = (TextView) view.findViewById(android.R.id.text2);
				String description;
				if (alarm.getAlarmWhenAbove()) {
					description = getString(R.string.alarms_description_above, alarm.getLevel());
				} else {
					description = getString(R.string.alarms_description_below, alarm.getLevel());
				}
				text2.setText(description);

				return view;
			}

		};
		listView.setAdapter(listAdapter);
		listView.setEmptyView(view.findViewById(R.id.empty));

		// enable swipe and undo
		listView.setDismissCallback(new EnhancedListView.OnDismissCallback() {
            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView listView, int pos) {
                final Map.Entry<Long, Alarm> item = listAdapter.getItem(pos);
				alarmManager.remove(item.getKey());
				listAdapter.remove(item); // hack to stop list from flashing

                return new EnhancedListView.Undoable() {
                    @Override
                    public void undo() {
						alarmManager.add(item.getValue());
                    }   
                };  
            }   
        }); 
        listView.enableSwipeToDismiss();
		listView.setUndoStyle(EnhancedListView.UndoStyle.COLLAPSED_POPUP);


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
	public Loader<Map<Long, Alarm>> onCreateLoader(int id, Bundle bundle) {
		if (id != LOADER_ID) return null;
		return new AlarmLoader(getActivity().getApplicationContext());
	}


	@Override
	public void onLoadFinished(Loader<Map<Long, Alarm>> loader, Map<Long, Alarm> alarms) {
		if (loader.getId() != LOADER_ID) return;
		listAdapter.clear();
		listAdapter.addAll(alarms.entrySet());
	}


	@Override
	public void onLoaderReset(Loader<Map<Long, Alarm>> loader) {
		listAdapter.clear();
	}

}
