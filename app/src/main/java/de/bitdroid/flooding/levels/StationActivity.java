package de.bitdroid.flooding.levels;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.alarms.NewAlarmActivity;
import de.bitdroid.flooding.dataselection.Extras;
import de.bitdroid.flooding.utils.BaseActivity;
import de.bitdroid.flooding.utils.SwipeRefreshLayoutUtils;
import de.bitdroid.flooding.utils.StringUtils;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardView;

public class StationActivity extends BaseActivity
	implements LoaderManager.LoaderCallbacks<Cursor>,
		SwipeRefreshLayout.OnRefreshListener,
		StationIntentService.SyncStatusReceiver.SyncListener,
		Extras {
	
	private static final int LOADERID = 46;

	private String stationName;
	private String waterName;
	private CardView levelView, infoView, charValuesView, mapView;
	private StationCardFactory factory;
	private SwipeRefreshLayout swipeLayout;
	private StationIntentService.SyncStatusReceiver syncStatusReceiver;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.data_station);
		stationName = getIntent().getExtras().getString(EXTRA_STATION_NAME);
		waterName = getIntent().getExtras().getString(EXTRA_WATER_NAME);

		levelView = (CardView) findViewById(R.id.level);
		infoView = (CardView) findViewById(R.id.info);
		charValuesView = (CardView) findViewById(R.id.charValues);
		mapView = (CardView) findViewById(R.id.map);

		getActionBar().setTitle(StringUtils.toProperCase(stationName));
		getActionBar().setSubtitle(StringUtils.toProperCase(waterName));

		factory = new StationCardFactory(getApplicationContext());
		getLoaderManager().initLoader(LOADERID, null, this);

		// setup pull to refresh
		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
		swipeLayout.setOnRefreshListener(this);
		swipeLayout.setRefreshing(true);
		SwipeRefreshLayoutUtils.setDefaultColors(swipeLayout);
		syncStatusReceiver = new StationIntentService.SyncStatusReceiver(new Handler());
		syncStatusReceiver.setSyncListener(this);

		// refresh station data manually
		syncStationData(false);
    }


	@Override
	public void onPause() {
		super.onPause();
		syncStatusReceiver.setSyncListener(null);
	}


	@Override
	public void onResume() {
		super.onResume();
		syncStatusReceiver.setSyncListener(this);
		if (syncStatusReceiver.isSyncFinished()) {
			syncStatusReceiver.resetSyncFinished();
			swipeLayout.setRefreshing(false);
		}
	}


	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		return factory.createCursorLoader(stationName);
	}


	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (loader.getId() != LOADERID) return;
		if (cursor.getCount() <= 0) return;
		cursor.moveToFirst();

		Card levelCard = factory.createStationLevelCard(cursor);
		if (levelView.getCard() == null) levelView.setCard(levelCard);
		else levelView.refreshCard(levelCard);
		levelView.setVisibility(View.VISIBLE);

		Card infoCard = factory.createStationInfoCard(cursor);
		if (infoView.getCard() == null) infoView.setCard(infoCard);
		else infoView.refreshCard(infoCard);
		infoView.setVisibility(View.VISIBLE);

		StationCharValuesCard charValuesCard = factory.createStationCharValuesCard(cursor);
		if (charValuesCard.isEmpty()) charValuesView.setVisibility(View.GONE);
		else {
			if (charValuesView.getCard() == null) charValuesView.setCard(charValuesCard);
			else charValuesView.setCard(charValuesCard);
			charValuesView.setVisibility(View.VISIBLE);
		}

		StationMapCard mapCard = factory.createStationMapCard(cursor, this);
		if (mapCard.isEmpty()) mapView.setVisibility(View.GONE);
		else {
			if (mapView.getCard() == null) mapView.setCard(mapCard);
			else mapView.refreshCard(mapCard);
			mapView.setVisibility(View.VISIBLE);
		}
	}


	@Override
	public void onLoaderReset(Loader<Cursor> loader) { }


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.station_menu, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.show_river_graph:
				Intent graphIntent = new Intent(getApplicationContext(), RiverGraphActivity.class);
				graphIntent.putExtra(StationActivity.EXTRA_WATER_NAME, waterName);
				startActivity(graphIntent);
				overridePendingTransition(R.anim.slide_enter_from_right, R.anim.slide_exit_to_left);
				return true;

			case R.id.create_alarm:
				Intent alarmIntent = new Intent(getApplicationContext(), NewAlarmActivity.class);
				alarmIntent.putExtra(Extras.EXTRA_WATER_NAME, waterName);
				alarmIntent.putExtra(Extras.EXTRA_STATION_NAME, stationName);
				startActivity(alarmIntent);
				return true;

		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onRefresh() {
		syncStationData(true);
	}


	@Override
	public void onSyncFinished() {
		swipeLayout.setRefreshing(false);
	}


	private void syncStationData(boolean forceSync) {
		Intent intent = new Intent(this, StationIntentService.class);
		intent.putExtra(StationIntentService.EXTRA_STATION_NAME, new String[] { stationName });
		intent.putExtra(StationIntentService.EXTRA_SYNC_STATUS_RECEIVER, syncStatusReceiver);
		intent.putExtra(StationIntentService.EXTRA_FORCE_SYNC, forceSync);
		startService(intent);
	}

}
