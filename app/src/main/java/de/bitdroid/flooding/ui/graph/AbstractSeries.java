package de.bitdroid.flooding.ui.graph;

import com.androidplot.xy.XYSeries;

import java.util.List;

import de.bitdroid.flooding.ods.StationMeasurements;


/**
 * Base class for managing series on a graph.
 */
abstract class AbstractSeries implements XYSeries {

	private final String title;

	protected AbstractSeries(String title) {
		this.title = title;
	}

	@Override
	public final String getTitle() {
		return title;
	}

	public abstract void addData(List<StationMeasurements> measurementsList);
	public abstract void reset();

}
