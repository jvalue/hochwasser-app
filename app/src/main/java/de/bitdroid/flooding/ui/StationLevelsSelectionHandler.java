package de.bitdroid.flooding.ui;


import android.content.Context;

import de.bitdroid.flooding.ods.BodyOfWater;
import de.bitdroid.flooding.ods.Station;


/**
 * Container class for handling station selection when trying to view station data.
 *
 * This is the "glue" holding the various selection activities together.
 */
public class StationLevelsSelectionHandler {

	public static class WaterSelectionActivity extends AbstractWaterSelectionActivity {

		@Override
		protected void onWaterSelected(BodyOfWater water) {
			startActivity(new StationSelection(water).toIntent(this, StationSelectionActivity.class));
		}

		@Override
		protected Class<? extends AbstractMapSelectionActivity> getMapSelectionClass() {
			return MapSelectionActivity.class;
		}

	}


	public static class StationSelectionActivity extends AbstractStationSelectionActivity {

		@Override
		protected void onStationSelected(Station station) {
			StationLevelsSelectionHandler.onStationSelected(this, station);
		}

		@Override
		protected Class<? extends AbstractMapSelectionActivity> getMapSelectionClass() {
			return MapSelectionActivity.class;
		}

	}


	public static class MapSelectionActivity extends AbstractMapSelectionActivity {

		@Override
		public void onStationSelected(Station station) {
			StationLevelsSelectionHandler.onStationSelected(this, station);
		}

	}


	private static void onStationSelected(Context context, Station station) {
		context.startActivity(new StationSelection(station.getBodyOfWater(), station).toIntent(context, StationInfoActivity.class));
	}

}
