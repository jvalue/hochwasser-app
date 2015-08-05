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
import de.bitdroid.flooding.ui.AbstractRestrictedActivity;
import de.bitdroid.flooding.ui.AbstractMapSelectionActivity;
import de.bitdroid.flooding.ui.StationSelection;
import de.bitdroid.flooding.utils.StringUtils;
import roboguice.inject.ContentView;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;

@ContentView(R.layout.activity_water_graph)
public class WaterGraphActivity extends AbstractRestrictedActivity {

	private static final String
			STATE_MEASUREMENTS = "STATE_MEASUREMENTS",
			STATE_SHOWING_REGULAR_SERIES = "STATE_SHOWING_REGULAR_SERIES";


	@Inject private OdsManager odsManager;

	private WaterGraph graph;
	private boolean showingRegularSeries = true;
	private BodyOfWater bodyOfWater;
	private ArrayList<StationMeasurements> measurementsList;


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
		graph.setDomainAxis(
				getString(R.string.graph_domainlabel),
				new DecimalFormat("@@#"),
				XYStepMode.SUBDIVIDE,
				10);

		// load data
		if (savedInstanceState != null) {
			showingRegularSeries = savedInstanceState.getBoolean(STATE_SHOWING_REGULAR_SERIES);
			ArrayList<StationMeasurements> measurementsList = savedInstanceState.getParcelableArrayList(STATE_MEASUREMENTS);
			setupData(measurementsList);
			graph.restoreState(savedInstanceState);

		} else {
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
							setupData(stationMeasurements);
						}
					}, new Action1<Throwable>() {
						@Override
						public void call(Throwable throwable) {
							hideSpinner();
							Timber.e(throwable, "failed to download measurements");
						}
					});
		}
	}


	@Override
	protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		state.putBoolean(STATE_SHOWING_REGULAR_SERIES, showingRegularSeries);
		state.putParcelableArrayList(STATE_MEASUREMENTS, measurementsList);
		graph.saveState(state);
	}


	private void setupData(List<StationMeasurements> measurementsList) {
		if (!assertValidMeasurements(measurementsList)) return;

		// setup graph UI
		if (showingRegularSeries) {
			graph.setSeries(getRegularSeries());
			showRegularRangeLabel();
		} else {
			graph.setSeries(getNormalizedSeries());
			showRelativeRangeLabel();
		}

		// set data
		sortMeasurement(measurementsList);
		this.measurementsList = new ArrayList<>(measurementsList);
		graph.setData(measurementsList);
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
				Intent mapIntent = new StationSelection(bodyOfWater).toIntent(this, MapInfoActivity.class);
				startActivity(mapIntent);
				return true;

			case R.id.menu_help:
				Intent intent = new Intent(this, WaterGraphHelpActivity.class);
				startActivity(intent);
				return true;
		}
		return super.onOptionsItemSelected(menuItem);
	}


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


	private boolean assertValidMeasurements(List<StationMeasurements> measurementsList) {
		// check that there are enough stations present to show in graph
		boolean valid = true;
		if (measurementsList.size() <= 1) {
			valid = false;
		} else if (measurementsList.size() == 2) {
			float km1 = measurementsList.get(0).getStation().getRiverKm();
			float km2 = measurementsList.get(1).getStation().getRiverKm();
			if (km1 == km2) valid = false;
		}

		if (valid) return true;

		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.invalid_graph_title))
				.setMessage(getString(R.string.invalid_graph_msg))
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						finish();
					}
				})
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				})
				.show();
		return false;
	}


	private void sortMeasurement(List<StationMeasurements> measurementsList) {
		Collections.sort(measurementsList, new Comparator<StationMeasurements>() {
			@Override
			public int compare(StationMeasurements lhs, StationMeasurements rhs) {
				return lhs.getStation().getRiverKm().compareTo(rhs.getStation().getRiverKm());
			}
		});
	}


	public static class MapInfoActivity extends AbstractMapSelectionActivity {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			String bodyOfWaterName = new StationSelection(getIntent()).getWater().getName();
			setTitle(StringUtils.toProperCase(bodyOfWaterName));
		}

		@Override
		public void onStationSelected(Station station) {
			// nothing to do
		}

	}

}
