package de.bitdroid.flooding.levels;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.format.DateFormat;
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;

import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.dataselection.Extras;
import de.bitdroid.flooding.map.InfoMapActivity;
import de.bitdroid.flooding.monitor.SourceMonitor;
import de.bitdroid.flooding.utils.BaseActivity;
import de.bitdroid.flooding.utils.SwipeRefreshLayoutUtils;
import de.bitdroid.utils.StringUtils;

import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_HTHW_UNIT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_HTHW_VALUE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_MHW_UNIT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_MHW_VALUE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_MNW_UNIT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_MNW_VALUE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_MTHW_UNIT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_MTHW_VALUE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_MTNW_UNIT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_MTNW_VALUE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_MW_UNIT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_MW_VALUE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_NTNW_UNIT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_CHARVALUES_NTNW_VALUE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_TIMESTAMP;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_UNIT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_VALUE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_KM;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_NAME;

public class RiverGraphActivity extends BaseActivity
		implements LoaderManager.LoaderCallbacks<Cursor>,
		SwipeRefreshLayout.OnRefreshListener,
		StationIntentService.SyncStatusReceiver.SyncListener,
		Extras {

	private static final int
			TIMESTAMP_LOADER_ID = 48,
			DATA_LOADER_ID = 49;


	private WaterGraph graph;
	private boolean showingRegularSeries = true;
	private boolean showingSeekbar = false;
	private Cursor levelData;
	private String waterName;

	private SeekBar seekbar;
	private List<Long> timestamps;
	private Long selectedTimestamp;

	private CombinedSourceLoader dataLoader;

	private Handler handler = new Handler();

	private SwipeRefreshLayout swipeLayout;
	private StationIntentService.SyncStatusReceiver syncStatusReceiver;
	private boolean newContentDownloaded = false; // indicates whether download has run
	private String[] stationNames;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		FullScreenUtils.startFullScreen(this);

		// set layout
		setContentView(R.layout.data_river);
		waterName = getIntent().getExtras().getString(EXTRA_WATER_NAME);
		getActionBar().setTitle(StringUtils.toProperCase(waterName));

		// setup graph
		XYPlot graphView = (XYPlot) findViewById(R.id.graph);
		this.graph = new WaterGraph(graphView, getApplicationContext());
		graph.setSeries(getRegularSeries());
		graph.setDomainAxis(
				getString(R.string.graph_domainlabel), 
				new DecimalFormat("@@#"), 
				XYStepMode.SUBDIVIDE, 
				10);
		showRegularRangeLabel();

		this.seekbar = (SeekBar) findViewById(R.id.seekbar);
		getSupportLoaderManager().initLoader(TIMESTAMP_LOADER_ID, null, this);

		// setup pull to refresh
		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
		swipeLayout.setOnRefreshListener(this);
		swipeLayout.setEnabled(false);
		syncStatusReceiver = new StationIntentService.SyncStatusReceiver(new Handler());
		syncStatusReceiver.setSyncListener(this);
		// syncStationData(false);
		SwipeRefreshLayoutUtils.setDefaultColors(swipeLayout);

    }


	@Override
	public void onResume() {
		super.onResume();
	}


	private final Runnable startFullscreenRunnable = new Runnable() {
		@Override
		public void run() {
			FullScreenUtils.startFullScreen(RiverGraphActivity.this);
		}
	};

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			handler.removeCallbacks(startFullscreenRunnable);
			handler.postDelayed(startFullscreenRunnable, 3000);
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.graph_menu, menu);
		return true;
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		MenuItem item = menu.findItem(R.id.menu_normalize);
		if (showingRegularSeries) {
			item.setTitle(getString(R.string.menu_graph_normalize));
			item.setIcon(getResources().getDrawable(R.drawable.ic_action_normalize_light));
		} else {
			item.setTitle(getString(R.string.menu_graph_regular));
			item.setIcon(getResources().getDrawable(R.drawable.ic_action_cm));
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

				new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Holo_Dialog))
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
					.setNegativeButton(getString(R.string.btn_cancel), null)
					.setPositiveButton(getString(R.string.btn_ok) , new DialogInterface.OnClickListener() {
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
				if (levelData != null) graph.setData(levelData);
				invalidateOptionsMenu();
				return true;

			case R.id.menu_seekbar:
				showingSeekbar = !showingSeekbar;
				if (showingSeekbar) seekbar.setVisibility(View.VISIBLE);
				else seekbar.setVisibility(View.GONE);
				return true;

			case R.id.menu_map:
				Intent mapIntent = new Intent(getApplicationContext(), InfoMapActivity.class);
				mapIntent.putExtra(InfoMapActivity.EXTRA_WATER_NAME, waterName);
				startActivity(mapIntent);
				overridePendingTransition(
						R.anim.slide_enter_from_right,
						R.anim.slide_exit_to_left);
				return true;

			case R.id.menu_help:
				Intent intent = new Intent(this, RiverGraphHelpActivity.class);
				startActivity(intent);
				overridePendingTransition(
						R.anim.slide_enter_from_right,
						R.anim.slide_exit_to_left);
				return true;
		}
		return super.onOptionsItemSelected(menuItem);
	}

	
	private static final String 
		EXTRA_SHOWING_REGULAR_SERIES = "EXTRA_SHOWING_REGULAR_SERIES",
		EXTRA_TIMESTAMP = "EXTRA_TIMESTAMP",
		EXTRA_SHOWING_SEEKBAR = "EXTRA_SHOWING_SEEKBAR",
		EXTRA_DATA_DOWNLOADED = "EXTRA_DATA_DOWNLOADED";

	@Override
	protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		state.putBoolean(EXTRA_SHOWING_REGULAR_SERIES, showingRegularSeries);
		state.putLong(EXTRA_TIMESTAMP, selectedTimestamp);
		state.putBoolean(EXTRA_SHOWING_SEEKBAR, showingSeekbar);
		state.putBoolean(EXTRA_DATA_DOWNLOADED, newContentDownloaded);
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
		Loader<Cursor> loader = getSupportLoaderManager().getLoader(DATA_LOADER_ID);
		this.dataLoader = (CombinedSourceLoader) loader;

		// restore timestamp
		setTimestamp(state.getLong(EXTRA_TIMESTAMP));

		// restore seekbar
		showingSeekbar = state.getBoolean(EXTRA_SHOWING_SEEKBAR);
		if (showingSeekbar) seekbar.setVisibility(View.VISIBLE);

		this.newContentDownloaded =  state.getBoolean(EXTRA_DATA_DOWNLOADED);
	}


	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		switch(id) {
			case TIMESTAMP_LOADER_ID:
				return new TimestampLoader(getApplicationContext(), SourceMonitor.getInstance(getApplicationContext()), waterName);

			case DATA_LOADER_ID:
				dataLoader = new CombinedSourceLoader(getApplicationContext(), waterName, CombinedSourceLoader.MOST_CURRENT_TIMESTAMP);
				return dataLoader;

			default:
				throw new IllegalStateException("found illegal loader id " + id);
		}
	}


	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		switch(loader.getId()) {
			case TIMESTAMP_LOADER_ID:
				timestamps = new LinkedList<Long>();
				cursor.moveToFirst();
				if (cursor.getCount() == 0) return;
				while (!cursor.isAfterLast()) {
					timestamps.add(cursor.getLong(cursor.getColumnIndex(COLUMN_LEVEL_TIMESTAMP)));
					cursor.moveToNext();
				}
				if (selectedTimestamp == null && timestamps.size() > 0) setTimestamp(timestamps.get(timestamps.size() - 1));
				if (dataLoader == null) getSupportLoaderManager().initLoader(DATA_LOADER_ID, null, this);
				updateSeekbar();
				break;

			case DATA_LOADER_ID:
				levelData = cursor;
				graph.setData(cursor);
				if (!checkForValidData()) break;

				// download new river data
				if (!newContentDownloaded) {
					swipeLayout.setRefreshing(true);
					newContentDownloaded = true;
					cursor.moveToFirst();
					List<String> stationNames = new LinkedList<String>();
					while (!cursor.isAfterLast()) {
						stationNames.add(cursor.getString(cursor.getColumnIndex(COLUMN_STATION_NAME)));
						cursor.moveToNext();
					}
					this.stationNames = stationNames.toArray(new String[stationNames.size()]);
					syncRiverData(false);
					// only enable once stations have been loaded
					swipeLayout.setEnabled(true);
				}
				break;

			default:
				throw new IllegalStateException("found illegal loader id " + loader.getId());
		}
	}


	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		switch(loader.getId()) {
			case TIMESTAMP_LOADER_ID:
				timestamps = null;
				break;

			case DATA_LOADER_ID:
				levelData = null;
				break;

			default:
				throw new IllegalStateException("found illegal loader id " + loader.getId());
		}
	}


	@Override
	public void onRefresh() {
		syncRiverData(true);
	}


	@Override
	public void onSyncFinished() {
		swipeLayout.setRefreshing(false);
		dataLoader.onContentChanged();
		setTimestamp(timestamps.get(timestamps.size() - 1));
	}


	private List<Pair<AbstractSeries, Integer>> getRegularSeries() {
		// regular water level (relative values)
		List<Pair<AbstractSeries, Integer>> seriesList 
				= new ArrayList<Pair<AbstractSeries, Integer>>();
		seriesList.add(new Pair<AbstractSeries, Integer>(
					new SimpleSeries(
						getString(R.string.series_water_levels),
						COLUMN_STATION_KM, 
						COLUMN_LEVEL_VALUE, 
						COLUMN_LEVEL_UNIT),
					R.xml.series_water_levels));

		// add characteristic values
		seriesList.add(new Pair<AbstractSeries, Integer>(
					new SimpleSeries(
						getString(R.string.series_mw),
						COLUMN_STATION_KM,
						COLUMN_CHARVALUES_MW_VALUE,
						COLUMN_CHARVALUES_MW_UNIT),
					R.xml.series_average_levels));
		seriesList.add(new Pair<AbstractSeries, Integer>(
					new SimpleSeries(
						getString(R.string.series_mhw),
						COLUMN_STATION_KM,
						COLUMN_CHARVALUES_MHW_VALUE,
						COLUMN_CHARVALUES_MHW_UNIT),
					R.xml.series_average_levels));
		seriesList.add(new Pair<AbstractSeries, Integer>(
					new SimpleSeries(
						getString(R.string.series_mnw),
						COLUMN_STATION_KM,
						COLUMN_CHARVALUES_MNW_VALUE,
						COLUMN_CHARVALUES_MNW_UNIT),
					R.xml.series_average_levels));

		// add tild series
		seriesList.add(new Pair<AbstractSeries, Integer>(
					new SimpleSeries(
						getString(R.string.series_mthw),
						COLUMN_STATION_KM,
						COLUMN_CHARVALUES_MTHW_VALUE,
						COLUMN_CHARVALUES_MTHW_UNIT),
					R.xml.series_average_tide_values));
		seriesList.add(new Pair<AbstractSeries, Integer>(
					new SimpleSeries(
						getString(R.string.series_mtnw),
						COLUMN_STATION_KM,
						COLUMN_CHARVALUES_MTNW_VALUE,
						COLUMN_CHARVALUES_MTNW_UNIT),
					R.xml.series_average_tide_values));
		seriesList.add(new Pair<AbstractSeries, Integer>(
					new SimpleSeries(
						getString(R.string.series_hthw),
						COLUMN_STATION_KM,
						COLUMN_CHARVALUES_HTHW_VALUE,
						COLUMN_CHARVALUES_HTHW_UNIT),
					R.xml.series_extreme_tide_values));
		seriesList.add(new Pair<AbstractSeries, Integer>(
					new SimpleSeries(
						getString(R.string.series_ntnw),
						COLUMN_STATION_KM,
						COLUMN_CHARVALUES_NTNW_VALUE,
						COLUMN_CHARVALUES_NTNW_UNIT),
					R.xml.series_extreme_tide_values));
		
		return seriesList;
	}


	private List<Pair<AbstractSeries, Integer>> getNormalizedSeries() {
		List<Pair<AbstractSeries, Integer>> series 
				= new ArrayList<Pair<AbstractSeries, Integer>>();

		AbstractSeries normalizedSeries = new NormalizedSeries(
				getString(R.string.series_water_levels_normalized),
				COLUMN_STATION_KM,
				COLUMN_LEVEL_VALUE,
				COLUMN_LEVEL_UNIT,
				COLUMN_CHARVALUES_MHW_VALUE,
				COLUMN_CHARVALUES_MHW_UNIT,
				COLUMN_CHARVALUES_MW_VALUE,
				COLUMN_CHARVALUES_MW_UNIT,
				COLUMN_CHARVALUES_MNW_VALUE,
				COLUMN_CHARVALUES_MNW_UNIT);

		series.add(new Pair<AbstractSeries, Integer>(
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


	private void setTimestamp(long newTimestamp) {
		selectedTimestamp = newTimestamp;
		Date date = new Date(selectedTimestamp);
		graph.setTitle(DateFormat.getDateFormat(this).format(date)
				+ " " + DateFormat.getTimeFormat(this).format(date));
		if (dataLoader != null) {
			if (timestamps.indexOf(selectedTimestamp) == timestamps.size() - 1)
				dataLoader.setTimestamp(CombinedSourceLoader.MOST_CURRENT_TIMESTAMP);
			else dataLoader.setTimestamp(selectedTimestamp);
		}
	}


	private void updateSeekbar() {
		seekbar.setMax(timestamps.size() - 1);
		seekbar.setProgress(timestamps.indexOf(selectedTimestamp));
		seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
				if (fromUser) setTimestamp(timestamps.get(progress));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekbar) { }

			@Override
			public void onStopTrackingTouch(SeekBar seekbar) { }
		});
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


	private void syncRiverData(boolean forceSync) {
		Intent intent = new Intent(this, StationIntentService.class);
		intent.putExtra(StationIntentService.EXTRA_STATION_NAME, stationNames);
		intent.putExtra(StationIntentService.EXTRA_SYNC_STATUS_RECEIVER, syncStatusReceiver);
		intent.putExtra(StationIntentService.EXTRA_FORCE_SYNC, forceSync);
		startService(intent);
	}
}
