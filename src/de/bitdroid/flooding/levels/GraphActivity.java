package de.bitdroid.flooding.levels;

import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_MHW_UNIT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_MHW_VALUE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_MNW_UNIT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_MNW_VALUE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_MW_UNIT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_MW_VALUE;
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

import android.app.Activity;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Pair;

import com.androidplot.Plot;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeriesFormatter;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.pegelonline.PegelOnlineSource;
import de.bitdroid.flooding.utils.AbstractLoaderCallbacks;

public class GraphActivity extends Activity {
	
	public static final String EXTRA_WATER_NAME = "waterName";
	private static final int LOADER_ID = 44;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.graph);

		final String waterName = getIntent().getExtras().getString(EXTRA_WATER_NAME);
		final XYPlot graph = (XYPlot) findViewById(R.id.graph);
		final SeriesManager manager = new SeriesManager();

		// regular water level (relative values)
		manager.addSeries(
				new SimpleSeries(
					"Water levels",
					COLUMN_STATION_KM, 
					COLUMN_LEVEL_VALUE, 
					COLUMN_LEVEL_UNIT),
				getWaterLevelSeriesFormatter());


		// add characteristic values
		manager.addSeries(
				new SimpleSeries(
					"MW",
					COLUMN_STATION_KM,
					COLUMN_CHARVALUES_MW_VALUE,
					COLUMN_CHARVALUES_MW_UNIT),
				getInfoSeriesFormatter());
		manager.addSeries(
				new SimpleSeries(
					"MHW",
					COLUMN_STATION_KM,
					COLUMN_CHARVALUES_MHW_VALUE,
					COLUMN_CHARVALUES_MHW_UNIT),
				getInfoSeriesFormatter());
		manager.addSeries(
				new SimpleSeries(
					"MNW",
					COLUMN_STATION_KM,
					COLUMN_CHARVALUES_MNW_VALUE,
					COLUMN_CHARVALUES_MNW_UNIT),
				getInfoSeriesFormatter());



		for (Pair<AbstractSeries, XYSeriesFormatter<?>> p : manager.getSeries()) {
			graph.addSeries(p.first, p.second);
		}

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

				manager.reset();
				manager.addData(cursor);
				graph.redraw();
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
							COLUMN_LEVEL_ZERO_UNIT,
							COLUMN_CHARVALUES_MW_VALUE,
							COLUMN_CHARVALUES_MW_UNIT,
							COLUMN_CHARVALUES_MHW_VALUE,
							COLUMN_CHARVALUES_MHW_UNIT,
							COLUMN_CHARVALUES_MNW_VALUE,
							COLUMN_CHARVALUES_MNW_UNIT
						}, COLUMN_WATER_NAME + "=? AND " + COLUMN_LEVEL_TYPE + "=?", 
						new String[] { waterName, "W" }, 
						null);
			}
		};

		getLoaderManager().initLoader(LOADER_ID, null, loader);
    }


	private LineAndPointFormatter getWaterLevelSeriesFormatter() {
		return getDefaultFormatter(R.xml.series_water_levels);
	}

	private LineAndPointFormatter getInfoSeriesFormatter() {
		return getDefaultFormatter(R.xml.series_characteristic_values);
	}

	private LineAndPointFormatter getDefaultFormatter(int configuration) {
		LineAndPointFormatter formatter = new LineAndPointFormatter();
		formatter.setPointLabelFormatter(null);
		formatter.configure(getApplicationContext(), configuration);
		return formatter;
	}
}
