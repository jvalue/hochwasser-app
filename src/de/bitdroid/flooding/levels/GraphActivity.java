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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;

import com.androidplot.Plot;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

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

		final XYPlot graph = (XYPlot) findViewById(R.id.graph);
		final MeasurementSeries series = new MeasurementSeries("Cant see me");
		LineAndPointFormatter seriesFormat = new LineAndPointFormatter();
		seriesFormat.setPointLabelFormatter(null);
		seriesFormat.configure(
				getApplicationContext(), 
				R.xml.line_point_formatter);
		graph.addSeries(series, seriesFormat);
		graph.setBorderStyle(Plot.BorderStyle.SQUARE, null, null);
		graph.getLayoutManager().remove(graph.getLegendWidget());
		graph.setTitle(waterName);
		graph.setTicksPerRangeLabel(2);
		graph.setTicksPerDomainLabel(3);
		graph.getGraphWidget().setRangeValueFormat(new DecimalFormat("@@##"));
		graph.getGraphWidget().setDomainValueFormat(new DecimalFormat("@@#"));
		graph.setBorderStyle(Plot.BorderStyle.NONE, null, null);

		graph.setBackgroundColor(Color.BLACK);
		graph.getGraphWidget().getBackgroundPaint().setColor(Color.BLACK);
		graph.getGraphWidget().getGridBackgroundPaint().setColor(Color.BLACK);

		AbstractLoaderCallbacks loader = new AbstractLoaderCallbacks(LOADER_ID) {

			@Override
			protected void onLoadFinishedHelper(Loader<Cursor> loader, Cursor cursor) {
				if (cursor == null) return;

				cursor.moveToFirst();
				int nameIdx = cursor.getColumnIndex(COLUMN_STATION_NAME);
				int unitIdx = cursor.getColumnIndex(COLUMN_LEVEL_UNIT);
				int kmIdx = cursor.getColumnIndex(COLUMN_STATION_KM);
				int valueIdx = cursor.getColumnIndex(COLUMN_LEVEL_VALUE);
				int zeroValueIdx = cursor.getColumnIndex(COLUMN_LEVEL_ZERO_VALUE);
				int zeroUnitIdx = cursor.getColumnIndex(COLUMN_LEVEL_ZERO_UNIT);

				series.reset();
				do {
					series.addMeasurement(
							cursor.getString(nameIdx),
							cursor.getDouble(kmIdx),
							cursor.getDouble(valueIdx),
							cursor.getString(unitIdx),
							cursor.getDouble(zeroValueIdx),
							cursor.getString(zeroUnitIdx));

				} while(cursor.moveToNext());

				graph.redraw();

				int skippedMeasurements = series.getSkippedMeasurements();
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




	private static class MeasurementSeries implements XYSeries {

		private final String title;

		private double minLevel = Double.MAX_VALUE, maxLevel = Double.MIN_VALUE;
		private double minRiverKm = Double.MAX_VALUE, maxRiverKm = Double.MIN_VALUE;

		private int skippedValues = 0;
		private List<Measurement> measurements = new ArrayList<Measurement>();

		public MeasurementSeries(String title) {
			this.title = title;
		}

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

		public void reset() {
			measurements.clear();
			skippedValues = 0;
			minLevel = Double.MAX_VALUE;
			maxLevel = Double.MIN_VALUE;
			minRiverKm = Double.MAX_VALUE;
			maxRiverKm = Double.MIN_VALUE;
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

		public int getSkippedMeasurements() {
			return skippedValues;
		}

		@Override
		public Number getX(int idx) {
			return measurements.get(idx).getRiverKm();
		}
		
		@Override
		public Number getY(int idx) {
			return measurements.get(idx).getValue();
		}

		@Override
		public int size() {
			return measurements.size();
		}

		@Override
		public String getTitle() {
			return title;
		}

	}

	private static class Measurement {

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

		public double getRiverKm() {
			return riverKm;
		}

		public double getValue() {
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
