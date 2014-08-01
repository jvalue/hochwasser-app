package de.bitdroid.flooding.alarms;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.bitdroid.flooding.R;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;


public final class AlarmFragment extends Fragment implements LoaderManager.LoaderCallbacks<Set<Alarm>> {

	private static final int LOADER_ID  = 47;

	private CardListView listView;
	private CardArrayAdapter listAdapter;
	private AlarmManager alarmManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		alarmManager = AlarmManager.getInstance(getActivity().getApplicationContext());
	}


	@Override
	public View onCreateView(LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {
		return inflater.inflate(R.layout.alarms, container, false);
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		listView = (CardListView) getActivity().findViewById(R.id.list);
		listView.setEmptyView(getActivity().findViewById(R.id.empty));
		listAdapter = new CardArrayAdapter(getActivity(), new ArrayList<Card>()) {
			@Override
			public void setEnableUndo(boolean enableUndo) {
				Map<String, Card> oldInternalObjects  = mInternalObjects;
				super.setEnableUndo(enableUndo);
				if (oldInternalObjects != null) mInternalObjects.putAll(oldInternalObjects);
			}
		};

		listAdapter.setEnableUndo(true);
		listView.setAdapter(listAdapter);
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
				if (!arePlayServicesAvailable()) return true;

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

		List<AlarmCard> cards = new LinkedList<AlarmCard>();
		for (Alarm alarm : alarms) {
			AlarmCard card = new AlarmCard(getActivity(), alarmManager, (LevelAlarm) alarm);
			card.setId(String.valueOf(alarm.hashCode()));
			cards.add(card);
		}

		Collections.sort(cards, new Comparator<AlarmCard>() {
			@Override
			public int compare(AlarmCard alarm1, AlarmCard alarm2) {
				return alarm1.getTitle().compareTo(alarm2.getTitle());
			}
		});

		listAdapter.clear();
		listAdapter.addAll(cards);
		listAdapter.setEnableUndo(true);
	}


	@Override
	public void onLoaderReset(Loader<Set<Alarm>> loader) {
		listAdapter.clear();
	}


	private boolean arePlayServicesAvailable() {
		Context context = getActivity().getApplicationContext();
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
		if (status == ConnectionResult.SUCCESS) return true;

		if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, getActivity(), 42);
			dialog.show();
		} else {
			new AlertDialog.Builder(context)
					.setTitle(getString(R.string.error_play_services_missing_title))
					.setMessage(getString(R.string.error_play_services_missing_msg))
					.setPositiveButton(getString(R.string.ok), null)
					.create()
					.show();
		}

		return false;
	}

}
