package de.bitdroid.flooding.levels;

import java.util.ArrayList;

import android.database.Cursor;

import de.bitdroid.flooding.pegelonline.UnitConverter;
import de.bitdroid.flooding.utils.Log;


class NormalizedSeries extends AbstractSeries {

	private final ArrayList<Number>
			xValues = new ArrayList<Number>(),
			yValues = new ArrayList<Number>();

	private final String 
			xValueColumn, 
			yValueColumn, yUnitColumn,
			relativeUpperValueColumn,relativeUpperUnitColumn,
			relativeMidValueColumn, relativeMidUnitColumn,
			relativeLowerValueColumn,relativeLowerUnitColumn;


	public NormalizedSeries(
			String title,
			String xValueColumn,
			String yValueColumn,
			String yUnitColumn,
			String relativeUpperValueColumn,
			String relativeUpperUnitColumn,
			String relativeMidValueColumn,
			String relativeMidUnitColumn,
			String relativeLowerValueColumn,
			String relativeLowerUnitColumn) {

		super(title);

		this.xValueColumn = xValueColumn;
		this.yValueColumn = yValueColumn; 
		this.yUnitColumn = yUnitColumn;
		this.relativeUpperValueColumn = relativeUpperValueColumn; 
		this.relativeUpperUnitColumn = relativeUpperUnitColumn;
		this.relativeMidValueColumn = relativeMidValueColumn;
		this.relativeMidUnitColumn = relativeMidUnitColumn;
		this.relativeLowerValueColumn = relativeLowerValueColumn;
		this.relativeLowerUnitColumn = relativeLowerUnitColumn;
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

	@Override
	protected void reset() {
		xValues.clear();
		yValues.clear();
	}

	@Override
	protected void addData(Cursor cursor) {
		Log.debug("Setting data for normalized one");
		cursor.moveToFirst();

		int xIdx = cursor.getColumnIndex(xValueColumn);
		do {
			Double xValue = cursor.getDouble(xIdx);
			Double yValue = getValue(cursor, yValueColumn, yUnitColumn);
			Double lower = getValue(cursor, relativeLowerValueColumn, relativeLowerUnitColumn);
			Double mid = getValue(cursor, relativeMidValueColumn, relativeMidUnitColumn);
			Double upper = getValue(cursor, relativeUpperValueColumn, relativeUpperUnitColumn);

			if (lower == null || upper == null || mid == null) continue;


			double normalized = 0;
			if (yValue > mid) normalized = 0.5 * (1 + ((yValue.doubleValue() - mid.doubleValue()) / (upper.doubleValue() - mid.doubleValue())));
			else normalized = 0.5 * ((yValue.doubleValue() - lower.doubleValue())  / (mid.doubleValue() - lower.doubleValue()));

			xValues.add(xValue);
			yValues.add(normalized);
			Log.debug(xValue + ", " + normalized);

		} while (cursor.moveToNext());
	}


	protected Double getValue(Cursor cursor, String valueColumn, String unitColumn) {
		int valueIdx = cursor.getColumnIndex(valueColumn);
		int unitIdx = cursor.getColumnIndex(unitColumn);
		Double value = cursor.getDouble(valueIdx);
		String unit = cursor.getString(unitIdx);
		if (value != null && unit != null) return UnitConverter.toCm(value, unit);
		return null;
	}
}
