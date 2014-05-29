package de.bitdroid.flooding.levels;

import java.util.HashMap;
import java.util.Map;

import android.database.Cursor;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYSeries;


final class SeriesManager {
	
	private final Map<AbstractSeries, LineAndPointFormatter> allSeries
			= new HashMap<AbstractSeries, LineAndPointFormatter>();


	public void addSeries(AbstractSeries series, LineAndPointFormatter format) {
		allSeries.put(series, format);
	}

	public void removeSeries(AbstractSeries series) {
		allSeries.remove(series);
	}

	public Map<XYSeries, LineAndPointFormatter> getSeries() {
		return new HashMap<XYSeries, LineAndPointFormatter>(allSeries);
	}

	public void reset() {
		for (AbstractSeries series : allSeries.keySet())
			series.reset();
	}

	public void addData(Cursor cursor) {
		for (AbstractSeries series : allSeries.keySet())
			series.addData(cursor);
	}
}
