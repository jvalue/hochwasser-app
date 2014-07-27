package de.bitdroid.flooding.alarms;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.ods.gcm.GcmStatus;
import de.bitdroid.flooding.utils.Log;
import de.bitdroid.flooding.utils.StringUtils;
import de.timroes.android.listview.EnhancedListView;


public final class AlarmFragment extends Fragment implements LoaderManager.LoaderCallbacks<Set<Alarm>> {

	private static final int LOADER_ID  = 47;

	private EnhancedListView listView;
	private ArrayAdapter<Alarm> listAdapter;
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
		listAdapter = new ArrayAdapter<Alarm>(
					getActivity().getApplicationContext(),
					R.layout.alarms_item,
					android.R.id.text1) { 
			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				LevelAlarm alarm = (LevelAlarm) getItem(position);
				View view = super.getView(position, convertView, parent);

				TextView text1 = (TextView) view.findViewById(android.R.id.text1);
				text1.setText(formatAlarmTitle(alarm));

				TextView text2 = (TextView) view.findViewById(android.R.id.text2);
				String description;
				if (alarm.getAlarmWhenAbove()) {
					description = getString(R.string.alarms_description_above, alarm.getLevel());
				} else {
					description = getString(R.string.alarms_description_below, alarm.getLevel());
				}
				text2.setText(description);

				LinearLayout regView = (LinearLayout) view.findViewById(R.id.registration);
				TextView regStatusView = (TextView) view.findViewById(R.id.registration_status);

				GcmStatus regStatus = alarmManager.getRegistrationStatus(alarm);
				if (regStatus.equals(GcmStatus.REGISTERED)) return view;

				regView.setVisibility(View.VISIBLE);
				switch (regStatus) {
					case PENDING_REGISTRATION:
						view.findViewById(R.id.registration_pending).setVisibility(View.VISIBLE);
						regStatusView.setText(getString(R.string.alarms_registration_pending));
						break;
					case UNREGISTERED:
						view.findViewById(R.id.registration_error).setVisibility(View.VISIBLE);
						regStatusView.setText(getString(R.string.alarms_registration_error));
						break;
					case PENDING_UNREGISTRATION:
						Log.warning("Found alarm with PENDING_UNREGISTRATION");
				}

				return view;
			}

		};
		listView.setAdapter(listAdapter);
		listView.setEmptyView(view.findViewById(R.id.empty));

		// retry registration on tap
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				LevelAlarm alarm = (LevelAlarm) parent.getAdapter().getItem(position);
				if (alarmManager.getRegistrationStatus(alarm).equals(GcmStatus.UNREGISTERED)) {
					alarmManager.register(alarm);
				}
			}
		});

		// enable swipe and undo
		listView.setDismissCallback(new EnhancedListView.OnDismissCallback() {
            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView listView, int pos) {
                final Alarm alarm = listAdapter.getItem(pos);
				alarmManager.unregister(alarm);
				listAdapter.remove(alarm); // hack to stop list from flashing

                return new EnhancedListView.Undoable() {
                    @Override
                    public void undo() {
						alarmManager.register(alarm);
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
	public Loader<Set<Alarm>> onCreateLoader(int id, Bundle bundle) {
		if (id != LOADER_ID) return null;
		return new AlarmLoader(getActivity().getApplicationContext());
	}


	@Override
	public void onLoadFinished(Loader<Set<Alarm>> loader, Set<Alarm> alarms) {
		if (loader.getId() != LOADER_ID) return;

		List<Alarm> sortedAlarms = new LinkedList<Alarm>();
		sortedAlarms.addAll(alarms);
		Collections.sort(sortedAlarms, new Comparator<Alarm>() {
			@Override
			public int compare(Alarm alarm1, Alarm alarm2) {
				return formatAlarmTitle((LevelAlarm) alarm1).compareTo(formatAlarmTitle((LevelAlarm) alarm2));
			}
		});

		listAdapter.clear();
		listAdapter.addAll(sortedAlarms);
	}


	@Override
	public void onLoaderReset(Loader<Set<Alarm>> loader) {
		listAdapter.clear();
	}


	private String formatAlarmTitle(LevelAlarm alarm) {
		return StringUtils.toProperCase(alarm.getRiver()) + " - " + StringUtils.toProperCase(alarm.getStation());
	}

}
