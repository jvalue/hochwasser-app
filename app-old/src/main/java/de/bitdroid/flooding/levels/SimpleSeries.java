package de.bitdroid.flooding.levels;

import android.database.Cursor;

import de.bitdroid.flooding.pegelonline.PegelOnlineUtils;


class SimpleSeries extends AbstractListSeries {

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
	public void addData(Cursor cursor) {
		if (cursor.getCount() == 0) return;
		cursor.moveToFirst();
		int xIdx = cursor.getColumnIndex(xValueColumn);
		int yUnitIdx = cursor.getColumnIndex(yUnitColumn);
		int yIdx = cursor.getColumnIndex(yValueColumn);

		do {
			Double xValue = cursor.getDouble(xIdx);
			Double yValue = cursor.getDouble(yIdx);
			String yUnit = cursor.getString(yUnitIdx);

			if (xValue != null && yUnit != null && yValue != null) {
				addValues(xValue, PegelOnlineUtils.toCm(yValue, yUnit));
			}
		} while(cursor.moveToNext());
		sortXValues();
	}
}