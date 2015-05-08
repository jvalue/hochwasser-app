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

	public abstract void addData(Cursor data);
	public abstract void reset();

}
