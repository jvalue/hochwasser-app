package de.bitdroid.flooding.ui.graph;

import java.util.List;

import de.bitdroid.flooding.ods.StationMeasurements;


class ConstantSeries extends AbstractSeries {

	private final AbstractSeries xValues;
	private final Number yValue;

	public ConstantSeries(
			String title,
			AbstractSeries xValues,
			Number yValue) {

		super(title);
		this.xValues = xValues;
		this.yValue = yValue;
	}

	@Override
	public Number getX(int idx) {
		return xValues.getX(idx);
	}

	@Override
	public Number getY(int idx) {
		return yValue;
	}

	@Override
	public int size() {
		return xValues.size();
	}

	@Override
	public void addData(List<StationMeasurements> measurements) { }

	@Override
	public void reset() { }

}
