package de.bitdroid.flooding.ui.graph;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.ods.BodyOfWater;
import de.bitdroid.flooding.ods.OdsManager;
import de.bitdroid.flooding.ods.Station;
import de.bitdroid.flooding.ods.StationMeasurements;
import de.bitdroid.flooding.ui.AbstractActivity;
import de.bitdroid.flooding.ui.StationSelection;
import de.bitdroid.flooding.utils.StringUtils;
import roboguice.inject.ContentView;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;

@ContentView(R.layout.activity_water_graph)
public class WaterGraphActivity extends AbstractActivity {

	@Inject private OdsManager odsManager;

	private WaterGraph graph;
	private boolean showingRegularSeries = true;
	private BodyOfWater bodyOfWater;

	private List<StationMeasurements> measurementsList;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		// get selected water
		StationSelection selection = new StationSelection(getIntent());
		bodyOfWater = selection.getWater();

		// set title
		setTitle(StringUtils.toProperCase(bodyOfWater.getName()));

		// setup graph
		XYPlot graphView = (XYPlot) findViewById(R.id.graph);
		graph = new WaterGraph(graphView, this);
		graph.setSeries(getRegularSeries());
		graph.setDomainAxis(
				getString(R.string.graph_domainlabel),
				new DecimalFormat("@@#"),
				XYStepMode.SUBDIVIDE,
				10);
		showRegularRangeLabel();

		// load data
		showSpinner();
		odsManager.getStationsByBodyOfWater(bodyOfWater)
				.flatMap(new Func1<List<Station>, Observable<List<StationMeasurements>>>() {
					@Override
					public Observable<List<StationMeasurements>> call(List<Station> stations) {
						return Observable.from(stations)
								.flatMap(new Func1<Station, Observable<StationMeasurements>>() {
									@Override
									public Observable<StationMeasurements> call(Station station) {
										return odsManager.getMeasurements(station);
									}
								})
								.toList();
					}
				})
				.compose(networkUtils.<List<StationMeasurements>>getDefaultTransformer())
				.subscribe(new Action1<List<StationMeasurements>>() {
					@Override
					public void call(List<StationMeasurements> stationMeasurements) {
						hideSpinner();
						sortMeasurement(stationMeasurements);
						WaterGraphActivity.this.measurementsList = stationMeasurements;
						graph.setData(stationMeasurements);
					}
				}, new Action1<Throwable>() {
					@Override
					public void call(Throwable throwable) {
						hideSpinner();
						Timber.e(throwable, "failed to download measurements");
					}
				});
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_graph, menu);
		return true;
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		MenuItem item = menu.findItem(R.id.menu_normalize);
		if (showingRegularSeries) {
			item.setTitle(getString(R.string.menu_graph_normalize));
			item.setIcon(getResources().getDrawable(R.drawable.ic_normalize));
		} else {
			item.setTitle(getString(R.string.menu_graph_regular));
			item.setIcon(getResources().getDrawable(R.drawable.ic_denormalize));
		}
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch(menuItem.getItemId()) {
			case R.id.menu_series:
				List<String> seriesKeys = graph.getSeriesKeys();
				final String[] items = seriesKeys.toArray(new String[seriesKeys.size()]);
				final boolean[] selectedItems = new boolean[seriesKeys.size()];
				int i = 0;
				for (String item : items) {
					if (graph.isVisible(item)) selectedItems[i] = true;
					i++;
				}

				new AlertDialog.Builder(this)
					.setTitle(getString(R.string.series_select_dialog_title))
					.setMultiChoiceItems(
							items,
							selectedItems,
							new DialogInterface.OnMultiChoiceClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int idx, boolean checked) {
									selectedItems[idx] = checked;
								}
							}
					)
					.setNegativeButton(getString(android.R.string.cancel), null)
					.setPositiveButton(getString(android.R.string.ok) , new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							Set<String> visibleSeries = new HashSet<String>();
							for (int i = 0; i < selectedItems.length; i++) 
								if (selectedItems[i]) visibleSeries.add(items[i]);
							graph.setVisibleSeries(visibleSeries);
							
						}
					}).create().show();
				return true;

			case R.id.menu_normalize:
				if (showingRegularSeries) {
					showRelativeRangeLabel();
					graph.setSeries(getNormalizedSeries());
				} else {
					showRegularRangeLabel();
					graph.setSeries(getRegularSeries());
				}
				this.showingRegularSeries = !showingRegularSeries;
				if (measurementsList != null) graph.setData(measurementsList);
				invalidateOptionsMenu();
				return true;


			case R.id.menu_map:
				/*
				Intent mapIntent = new Intent(getApplicationContext(), InfoMapActivity.class);
				mapIntent.putExtra(InfoMapActivity.EXTRA_WATER_NAME, waterName);
				startActivity(mapIntent);
				overridePendingTransition(
						R.anim.slide_enter_from_right,
						R.anim.slide_exit_to_left);
						*/
				return true;

			case R.id.menu_help:
				Intent intent = new Intent(this, RiverGraphHelpActivity.class);
				startActivity(intent);
				return true;
		}
		return super.onOptionsItemSelected(menuItem);
	}


	/*
	private static final String 
		EXTRA_SHOWING_REGULAR_SERIES = "EXTRA_SHOWING_REGULAR_SERIES",
		EXTRA_TIMESTAMP = "EXTRA_TIMESTAMP",
		EXTRA_SHOWING_SEEKBAR = "EXTRA_SHOWING_SEEKBAR",
		EXTRA_DATA_DOWNLOADED = "EXTRA_DATA_DOWNLOADED";

	@Override
	protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		state.putBoolean(EXTRA_SHOWING_REGULAR_SERIES, showingRegularSeries);
		graph.saveState(state);
	}


	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);

		// restore series
		this.showingRegularSeries = state.getBoolean(EXTRA_SHOWING_REGULAR_SERIES);
		if (!showingRegularSeries) {
			showRelativeRangeLabel();
			graph.setSeries(getNormalizedSeries());
			if (levelData != null) graph.setData(levelData);
		}
		graph.restoreState(state);

		// set data loader
		Loader<Cursor> loader = getLoaderManager().getLoader(DATA_LOADER_ID);
		this.dataLoader = (CombinedSourceLoader) loader;

		// restore timestamp
		setTimestamp(state.getLong(EXTRA_TIMESTAMP));

		// restore seekbar
		showingSeekbar = state.getBoolean(EXTRA_SHOWING_SEEKBAR);
		if (showingSeekbar) seekbar.setVisibility(View.VISIBLE);

		this.newContentDownloaded =  state.getBoolean(EXTRA_DATA_DOWNLOADED);
	}
	*/



	private List<Pair<AbstractSeries, Integer>> getRegularSeries() {
		// series <--> series formatting xml file
		List<Pair<AbstractSeries, Integer>> seriesList = new ArrayList<>();

		// regular water level (relative values)
		seriesList.add(new Pair<AbstractSeries, Integer>(
				SimpleSeries.createLevelSeries(getString(R.string.series_water_levels)),
				R.xml.series_water_levels));

		// add characteristic values
		seriesList.add(new Pair<AbstractSeries, Integer>(
				SimpleSeries.createMWSeries(getString(R.string.series_mw)),
				R.xml.series_average_levels));
		seriesList.add(new Pair<AbstractSeries, Integer>(
				SimpleSeries.createMHWSeries(getString(R.string.series_mhw)),
				R.xml.series_average_levels));
		seriesList.add(new Pair<AbstractSeries, Integer>(
				SimpleSeries.createMNWSeries(getString(R.string.series_mnw)),
				R.xml.series_average_levels));

		// add tide series
		seriesList.add(new Pair<AbstractSeries, Integer>(
				SimpleSeries.createMTHWSeries(getString(R.string.series_mthw)),
				R.xml.series_average_tide_values));
		seriesList.add(new Pair<AbstractSeries, Integer>(
				SimpleSeries.createMTNWSeries(getString(R.string.series_mtnw)),
				R.xml.series_average_tide_values));
		seriesList.add(new Pair<AbstractSeries, Integer>(
				SimpleSeries.createHTHWSeries(getString(R.string.series_hthw)),
				R.xml.series_extreme_tide_values));
		seriesList.add(new Pair<AbstractSeries, Integer>(
				SimpleSeries.createNTNWSeries(getString(R.string.series_ntnw)),
				R.xml.series_extreme_tide_values));
		
		return seriesList;
	}


	private List<Pair<AbstractSeries, Integer>> getNormalizedSeries() {
		List<Pair<AbstractSeries, Integer>> series = new ArrayList<>();

		AbstractSeries normalizedSeries = new NormalizedSeries(getString(R.string.series_water_levels_normalized));

		series.add(new Pair<>(
					normalizedSeries,
					R.xml.series_water_levels));

		series.add(new Pair<AbstractSeries, Integer>(
					new ConstantSeries(getString(R.string.series_mhw), normalizedSeries, 100),
					R.xml.series_average_levels));

		series.add(new Pair<AbstractSeries, Integer>(
					new ConstantSeries(getString(R.string.series_mw), normalizedSeries, 50),
					R.xml.series_average_levels));

		series.add(new Pair<AbstractSeries, Integer>(
					new ConstantSeries(getString(R.string.series_mnw), normalizedSeries, 0),
					R.xml.series_average_levels));

		return series;
	}


	private void showRegularRangeLabel() {
		graph.setRangeAxis(
				getString(R.string.graph_rangelabel_cm),
				new DecimalFormat("@@##"),
				XYStepMode.SUBDIVIDE, 11);
	}


	private void showRelativeRangeLabel() {
		graph.setRangeAxis(
				getString(R.string.graph_rangelabel_pc),
				new DecimalFormat("@@##"),
				XYStepMode.INCREMENT_BY_VAL, 12.5);
	}


	/*
	TODO
	private boolean checkForValidData() {
		// check that there are enough stations present to show in graph
		boolean valid = true;

		levelData.moveToFirst();
		if (levelData.getCount() <= 1) valid = false;
		else if (levelData.getCount() == 2) {
			double km1 = levelData.getDouble(levelData.getColumnIndex(COLUMN_STATION_KM));
			levelData.moveToNext();
			double km2 = levelData.getDouble(levelData.getColumnIndex(COLUMN_STATION_KM));
			if (km1 == km2) valid = false;

			levelData.moveToFirst();
		}

		if (valid) return true;

		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.invalid_graph_title))
				.setMessage(getString(R.string.invalid_graph_msg))
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						RiverGraphActivity.this.onBackPressed();
					}
				})
				.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						RiverGraphActivity.this.onBackPressed();
					}
				})
				.create()
				.show();

		return false;
	}
	 */


	private void sortMeasurement(List<StationMeasurements> measurementsList) {
		Collections.sort(measurementsList, new Comparator<StationMeasurements>() {
			@Override
			public int compare(StationMeasurements lhs, StationMeasurements rhs) {
				return lhs.getStation().getRiverKm().compareTo(rhs.getStation().getRiverKm());
			}
		});
	}

}
