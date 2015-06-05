package de.bitdroid.flooding.ui.graph;

import android.database.Cursor;

import java.util.List;

import de.bitdroid.flooding.ods.Measurement;
import de.bitdroid.flooding.ods.StationMeasurements;


class NormalizedSeries extends AbstractListSeries {

	protected NormalizedSeries(String title) {
		super(title);
	}

	@Override
	public void addData(List<StationMeasurements> measurementsList) {
		/*
		int xIdx = 0;
		for (StationMeasurements measurements : measurementsList) {
			if (measurements.get)


			++xIdx;
		}

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

			addValues(xValue, normalized * 100);

		} while (cursor.moveToNext());
		*/
	}


	protected Double getValue(Cursor cursor, String valueColumn, String unitColumn) {
		int valueIdx = cursor.getColumnIndex(valueColumn);
		int unitIdx = cursor.getColumnIndex(unitColumn);
		Double value = cursor.getDouble(valueIdx);
		String unit = cursor.getString(unitIdx);
		// if (value != null && unit != null) return PegelOnlineUtils.toCm(value, unit);
		return null;
	}


	public static class Data {

		private final Measurement yMeasurement, lowerMeasurement, midMeasurement, upperMeasurement;

		public Data(
				Measurement yMeasurement,
				Measurement lowerMeasurement,
				Measurement midMeasurement,
				Measurement upperMeasurement) {

			this.yMeasurement = yMeasurement;
			this.lowerMeasurement = lowerMeasurement;
			this.midMeasurement = midMeasurement;
			this.upperMeasurement = upperMeasurement;
		}

	}
}
