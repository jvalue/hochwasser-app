package de.bitdroid.flooding.ui;


import android.content.Context;
import android.widget.Toast;

import de.bitdroid.flooding.ods.BodyOfWater;
import de.bitdroid.flooding.ods.Station;


/**
 * Container class for handling station selection when trying to view station data.
 *
 * This is the "glue" holding the various selection activities together.
 */
public class DataSelectionHandler {

	public static class WaterSelectionActivity extends AbstractWaterSelectionActivity {

		@Override
		protected void onDataSelected(BodyOfWater water) {
			startActivity(new StationSelection(water).toIntent(this, StationSelectionActivity.class));
		}

		@Override
		protected Class<? extends AbstractMapSelectionActivity> getMapSelectionClass() {
			return MapSelectionActivity.class;
		}

	}


	public static class StationSelectionActivity extends AbstractStationSelectionActivity {

		public StationSelectionActivity() {
			super(true);
		}

		@Override
		protected void onAllStationsSelected() {
			Toast.makeText(this, "all selected!", Toast.LENGTH_SHORT).show();
		}

		@Override
		protected void onStationSelected(Station station) {
			DataSelectionHandler.onStationSelected(this, station);
		}

		@Override
		protected Class<? extends AbstractMapSelectionActivity> getMapSelectionClass() {
			return MapSelectionActivity.class;
		}

	}


	public static class MapSelectionActivity extends AbstractMapSelectionActivity {

		@Override
		public void onStationClicked(Station station) {
			onStationSelected(this, station);
		}

	}


	private static void onStationSelected(Context context, Station station) {
		context.startActivity(new StationSelection(station.getBodyOfWater(), station).toIntent(context, StationInfoActivity.class));
	}

}
