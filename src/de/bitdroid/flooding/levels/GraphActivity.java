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

import com.jjoe64.graphview.GraphViewDataInterface;
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

		final String waterName = getIntent().getExtras().getString(EXTRA_WATER_NAME);

		final LineGraphView graph = new LineGraphView(this, waterName);
		graph.setDrawBackground(true);
		graph.setScrollable(true);
		graph.setScalable(true);

		LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
		layout.addView(graph);

		AbstractLoaderCallbacks loader = new AbstractLoaderCallbacks(LOADER_ID) {

			@Override
			protected void onLoadFinishedHelper(Loader<Cursor> loader, Cursor cursor) {
				if (cursor == null) return;

				MeasurementManager manager = new MeasurementManager();

				cursor.moveToFirst();
				int nameIdx = cursor.getColumnIndex(COLUMN_STATION_NAME);
				int unitIdx = cursor.getColumnIndex(COLUMN_LEVEL_UNIT);
				int kmIdx = cursor.getColumnIndex(COLUMN_STATION_KM);
				int valueIdx = cursor.getColumnIndex(COLUMN_LEVEL_VALUE);
				int zeroValueIdx = cursor.getColumnIndex(COLUMN_LEVEL_ZERO_VALUE);
				int zeroUnitIdx = cursor.getColumnIndex(COLUMN_LEVEL_ZERO_UNIT);

				do {

					manager.addMeasurement(
							cursor.getString(nameIdx),
							cursor.getDouble(kmIdx),
							cursor.getDouble(valueIdx),
							cursor.getString(unitIdx),
							cursor.getDouble(zeroValueIdx),
							cursor.getString(zeroUnitIdx));

				} while(cursor.moveToNext());

				graph.removeAllSeries();
				graph.addSeries(new GraphViewSeries(manager.getAllMeasurements()));
				graph.setViewPort(manager.getMinRiverKm(), manager.getMaxRiverKm());
				graph.setManualYAxisBounds(manager.getMinLevel(), manager.getMaxLevel());

				int skippedMeasurements = manager.getSkippedMeasurements();
				if (skippedMeasurements > 0) 
					Log.warning("Skipped " + skippedMeasurements + " measurements as they were incomplete");
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




	private static class MeasurementManager {

		private double minLevel = Double.MAX_VALUE, maxLevel = Double.MIN_VALUE;
		private double minRiverKm = Double.MAX_VALUE, maxRiverKm = Double.MIN_VALUE;

		private int skippedValues = 0;
		private List<Measurement> measurements = new ArrayList<Measurement>();

		public void addMeasurement(
				String stationName,
				double riverKm,
				double value,
				String unit,
				double zeroValue,
				String zeroUnit) {


			value = UnitConverter.toCm(value, unit);
			if (zeroUnit != null) {
				value += UnitConverter.toCm(zeroValue, zeroUnit);
			} else if (UnitConverter.isRelativeCmUnit(unit)) {
				skippedValues++;
				return;
			}

			measurements.add(new Measurement(stationName, riverKm, value, unit));

			if (riverKm < minRiverKm) minRiverKm = riverKm;
			if (riverKm > maxRiverKm) maxRiverKm = riverKm;

			if (value < minLevel) minLevel = value;
			if (value > maxLevel) maxLevel = value;
		}


		public double getMaxRiverKm() {
			Log.debug("maxRiverKm = " + maxRiverKm);
			return maxRiverKm;
		}

		public double getMinRiverKm() {
			return minRiverKm;
		}

		public double getMinLevel() {
			return minLevel;
		}

		public double getMaxLevel() {
			return maxLevel;
		}

		public Measurement[] getAllMeasurements() {
			return measurements.toArray(new Measurement[measurements.size()]);
		}

		public int getSkippedMeasurements() {
			return skippedValues;
		}

	}

	private static class Measurement implements GraphViewDataInterface {

		private final String stationName;
		private final double value;
		private final String unit;
		private final double riverKm;

		private Measurement(
				String stationName,
				double riverKm,
				double value,
				String unit) {

			this.stationName = stationName;
			this.riverKm = riverKm;
			this.value = value;
			this.unit = unit;
		}


		@Override
		public double getX() {
			return riverKm;
		}

		@Override
		public double getY() {
			return value;
		}

		public String getStationName() {
			return stationName;
		}

		public String getUnit() {
			return unit;
		}
	}
}
