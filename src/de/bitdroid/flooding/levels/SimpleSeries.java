package de.bitdroid.flooding.levels;

import java.util.ArrayList;

import android.database.Cursor;

import de.bitdroid.flooding.pegelonline.UnitConverter;


class SimpleSeries extends AbstractSeries {

	private final ArrayList<Number> 
			xValues = new ArrayList<Number>(),
			yValues = new ArrayList<Number>();
	private final String xValueColumn, yValueColumn, yUnitColumn;

	public SimpleSeries(
			String title, 
			String xValueColumn, 
			String yValueColumn,
			String yUnitColumn) {

		super(title);
		this.xValueColumn = xValueColumn;
		this.yValueColumn = yValueColumn;
		this.yUnitColumn = yUnitColumn;
	}

	@Override
	public Number getX(int idx) {
		return xValues.get(idx);
	}

	@Override
	public Number getY(int idx) {
		return yValues.get(idx);
	}

	@Override
	public int size() {
		return xValues.size();
	}


	public void reset() {
		xValues.clear();
		yValues.clear();
	}

	protected void addData(Cursor cursor) {
		cursor.moveToFirst();
		int xIdx = cursor.getColumnIndex(xValueColumn);
		int yUnitIdx = cursor.getColumnIndex(yUnitColumn);
		int yIdx = cursor.getColumnIndex(yValueColumn);

		do {
			Double xValue = cursor.getDouble(xIdx);
			Double yValue = cursor.getDouble(yIdx);
			String yUnit = cursor.getString(yUnitIdx);

			if (xValue != null && yUnit != null && yValue != null) {
				xValues.add(xValue);
				yValues.add(UnitConverter.toCm(yValue, yUnit));
			}
		} while(cursor.moveToNext());
	}
}
