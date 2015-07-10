package de.bitdroid.flooding.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;

import javax.inject.Inject;

import de.bitdroid.flooding.ods.BodyOfWater;
import de.bitdroid.flooding.ods.Station;
import de.bitdroid.flooding.ui.AbstractActivity;
import de.bitdroid.flooding.ui.AbstractMapSelectionActivity;
import de.bitdroid.flooding.ui.AbstractStationSelectionActivity;
import de.bitdroid.flooding.ui.AbstractWaterSelectionActivity;
import de.bitdroid.flooding.ui.StationSelection;
import de.bitdroid.flooding.ui.graph.WaterGraphActivity;

/**
 * This actiivty helps during widget configuration. It mainly acts as a dispatcher
 * to start the station selection process.
 */
public class ConfigureStationInfoWidgetActivity extends AbstractActivity {

	private static final int REQUEST_SELECT_STATION = 42;
	@Inject private WidgetDataManager widgetDataManager;

	private int appWidgetId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		appWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

		// start selection activity
		startActivityForResult(
				new Intent(ConfigureStationInfoWidgetActivity.this, WaterSelectionActivity.class),
				REQUEST_SELECT_STATION);
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_SELECT_STATION:
				if (resultCode != RESULT_OK) break;
				Station selectedStation = new StationSelection(data).getStation();

				// store selected station
				widgetDataManager.storeGaugeId(appWidgetId, selectedStation.getGaugeId());

				// manually start first widget layout refresh
				Intent intent = new Intent(
						AppWidgetManager.ACTION_APPWIDGET_UPDATE,
						null,
						ConfigureStationInfoWidgetActivity.this,
						StationInfoWidgetProvider.class);
				intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { appWidgetId });
				sendBroadcast(intent);

				// return widget creation result
				Intent resultValue = new Intent();
				resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
				setResult(RESULT_OK, resultValue);
				finish();
				break;
		}
	}



	public static class WaterSelectionActivity extends AbstractWaterSelectionActivity {

		@Override
		protected void onDataSelected(BodyOfWater water) {
			startActivityForResult(
					new StationSelection(water).toIntent(this, StationSelectionActivity.class),
					REQUEST_SELECT_STATION);
		}

		@Override
		protected Class<? extends AbstractMapSelectionActivity> getMapSelectionClass() {
			return MapSelectionActivity.class;
		}

		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			switch (requestCode) {
				case REQUEST_SELECT_STATION:
					if (resultCode != RESULT_OK) break;
					setResult(RESULT_OK, data);
					finish();
					break;
			}
		}

	}


	public static class StationSelectionActivity extends AbstractStationSelectionActivity {

		public StationSelectionActivity() {
			super(false);
		}

		@Override
		protected void onAllStationsSelected() {
			Intent graphIntent = new StationSelection(getIntent()).toIntent(this, WaterGraphActivity.class);
			startActivityForResult(graphIntent, REQUEST_SELECT_STATION);
		}

		@Override
		protected void onStationSelected(Station station) {
			ConfigureStationInfoWidgetActivity.onStationSelected(this, station);
		}

		@Override
		protected Class<? extends AbstractMapSelectionActivity> getMapSelectionClass() {
			return MapSelectionActivity.class;
		}

		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			switch (requestCode) {
				case REQUEST_SELECT_STATION:
					if (resultCode != RESULT_OK) break;
					setResult(RESULT_OK, data);
					finish();
					break;
			}
		}

	}


	public static class MapSelectionActivity extends AbstractMapSelectionActivity {

		@Override
		public void onStationClicked(Station station) {
			onStationSelected(this, station);
		}

	}


	private static void onStationSelected(Activity activity, Station station) {
		Intent resultIntent = new StationSelection(station.getBodyOfWater(), station).toIntent();
		activity.setResult(RESULT_OK, resultIntent);
		activity.finish();
	}

}
