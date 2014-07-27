package de.bitdroid.flooding.levels;

import android.database.Cursor;


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
	public void reset() { }

	@Override
	public void addData(Cursor cursor) { }
}
