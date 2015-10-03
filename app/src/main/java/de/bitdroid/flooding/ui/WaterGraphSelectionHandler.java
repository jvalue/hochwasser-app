package de.bitdroid.flooding.ui;


import android.content.Intent;

import java.util.Iterator;
import java.util.List;

import de.bitdroid.flooding.ods.BodyOfWater;
import de.bitdroid.flooding.ui.graph.WaterGraphActivity;
import rx.Observable;
import rx.functions.Func1;


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
		protected Observable<List<BodyOfWater>> doLoadItems() {
			return super.doLoadItems()
					.map(new Func1<List<BodyOfWater>, List<BodyOfWater>>() {
						@Override
						public List<BodyOfWater> call(List<BodyOfWater> bodyOfWaters) {
							Iterator<BodyOfWater> iterator = bodyOfWaters.iterator();
							while (iterator.hasNext()) {
								if (iterator.next().getStationCount() <= 1) iterator.remove();
							}
							return bodyOfWaters;
						}
					});
		}

		@Override
		protected Class<? extends AbstractMapSelectionActivity> getMapSelectionClass() {
			return null;
		}

	}

}
