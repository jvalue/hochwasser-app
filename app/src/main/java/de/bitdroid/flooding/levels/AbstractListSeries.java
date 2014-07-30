package de.bitdroid.flooding.levels;

import java.util.ArrayList;


abstract class AbstractListSeries extends AbstractSeries {

	private final ArrayList<Number> 
			xValues = new ArrayList<Number>(),
			yValues = new ArrayList<Number>();

	protected AbstractListSeries(String title) {
		super(title);
	}

	@Override
	public final Number getX(int idx) {
		return xValues.get(idx);
	}

	@Override
	public final Number getY(int idx) {
		return yValues.get(idx);
	}

	@Override
	public final int size() {
		return xValues.size();
	}

	@Override
	public final void reset() {
		xValues.clear();
		yValues.clear();
	}

	protected final void addValues(Number xValue, Number yValue) {
		xValues.add(xValue);
		yValues.add(yValue);
	}
}
