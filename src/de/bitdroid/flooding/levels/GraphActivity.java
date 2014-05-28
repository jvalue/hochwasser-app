package de.bitdroid.flooding.levels;

import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_TIMESTAMP;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_TYPE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_UNIT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_VALUE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_ZERO_UNIT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_ZERO_VALUE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_KM;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_NAME;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_WATER_NAME;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.pegelonline.PegelOnlineSource;
import de.bitdroid.flooding.pegelonline.UnitConverter;
import de.bitdroid.flooding.utils.AbstractLoaderCallbacks;
import de.bitdroid.flooding.utils.Log;

public class GraphActivity extends Activity {
	
	public static final String EXTRA_WATER_NAME = "waterName";
	private static final int LOADER_ID = 44;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.graph);


		final LineGraphView graph = new LineGraphView(this, "Water Levels");
		graph.setDrawBackground(true);
		graph.setScrollable(true);
		LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
		layout.addView(graph);

		final String waterName = getIntent().getExtras().getString(EXTRA_WATER_NAME);

		AbstractLoaderCallbacks loader = new AbstractLoaderCallbacks(LOADER_ID) {

			@Override
			protected void onLoadFinishedHelper(Loader<Cursor> loader, Cursor cursor) {
				if (cursor == null) return;
				cursor.moveToFirst();

				int unitIdx = cursor.getColumnIndex(COLUMN_LEVEL_UNIT);
				int kmIdx = cursor.getColumnIndex(COLUMN_STATION_KM);
				int valueIdx = cursor.getColumnIndex(COLUMN_LEVEL_VALUE);
				int zeroValueIdx = cursor.getColumnIndex(COLUMN_LEVEL_ZERO_VALUE);
				int zeroUnitIdx = cursor.getColumnIndex(COLUMN_LEVEL_ZERO_UNIT);


				// some measurements are relative without a relation to make
				// values absolute --> skip those measurements!
				int skippedValuesCount = 0;

				List<GraphViewData> data = new ArrayList<GraphViewData>(); 
				do {

					String unit = cursor.getString(unitIdx);

					double levelValue = UnitConverter.toCm(
							cursor.getDouble(valueIdx),
							unit);

					String zeroUnit = cursor.getString(zeroUnitIdx);
					if (zeroUnit != null) {
						levelValue += UnitConverter.toCm(
								cursor.getDouble(zeroValueIdx),
								zeroUnit);

					} else if (UnitConverter.isRelativeCmUnit(unit)) {
						skippedValuesCount++;
						continue;
					}

					data.add(new GraphViewData(cursor.getDouble(kmIdx), levelValue));

				} while(cursor.moveToNext());

				graph.removeAllSeries();
				graph.addSeries(new GraphViewSeries(data.toArray(new GraphViewData[data.size()])));


				if (skippedValuesCount > 0) 
					Log.warning("Skipped " + skippedValuesCount + " measurements as they were incomplete");
			}

			@Override
			protected void onLoaderResetHelper(Loader<Cursor> loader) { }

			@Override
			protected Loader<Cursor> getCursorLoader() {
				return new CursorLoader(
						getApplicationContext(),
						new PegelOnlineSource().toUri(),
						new String[] { 
							COLUMN_STATION_NAME,
							COLUMN_STATION_KM,
							COLUMN_LEVEL_TIMESTAMP,
							COLUMN_LEVEL_VALUE,
							COLUMN_LEVEL_UNIT,
							COLUMN_LEVEL_ZERO_VALUE,
							COLUMN_LEVEL_ZERO_UNIT
						}, COLUMN_WATER_NAME + "=? AND " + COLUMN_LEVEL_TYPE + "=?", 
						new String[] { waterName, "W" }, 
						null);
			}
		};

		getLoaderManager().initLoader(LOADER_ID, null, loader);
    }
}
