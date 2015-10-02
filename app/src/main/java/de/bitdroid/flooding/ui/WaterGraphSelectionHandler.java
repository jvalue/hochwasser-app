package de.bitdroid.flooding.ui;


import android.content.Intent;

import de.bitdroid.flooding.ods.BodyOfWater;
import de.bitdroid.flooding.ui.graph.WaterGraphActivity;


/**
 * Container class for dispatching to river graph activities.
 *
 * This is the "glue" holding the various selection activities together.
 */
public class WaterGraphSelectionHandler {

	public static class WaterSelectionActivity extends AbstractWaterSelectionActivity {

		public WaterSelectionActivity() {
			super(false);
		}

		@Override
		protected void onWaterSelected(BodyOfWater water) {
			Intent graphIntent = new StationSelection(water).toIntent(this, WaterGraphActivity.class);
			startActivity(graphIntent);
		}

		@Override
		protected Class<? extends AbstractMapSelectionActivity> getMapSelectionClass() {
			return null;
		}

	}

}
