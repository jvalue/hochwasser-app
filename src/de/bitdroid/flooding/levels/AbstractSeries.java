package de.bitdroid.flooding.levels;

import android.database.Cursor;

import com.androidplot.xy.XYSeries;


abstract class AbstractSeries implements XYSeries {

	private final String title;

	protected AbstractSeries(String title) {
		this.title = title;
	}

	@Override
	public final String getTitle() {
		return title;
	}

	protected abstract void addData(Cursor data);
	protected abstract void reset();

}
