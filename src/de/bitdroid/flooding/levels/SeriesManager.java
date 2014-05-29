package de.bitdroid.flooding.levels;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.database.Cursor;
import android.util.Pair;

import com.androidplot.xy.XYSeriesFormatter;


final class SeriesManager {
	
	private final List<Pair<AbstractSeries, XYSeriesFormatter<?>>> allSeries 
			= new ArrayList<Pair<AbstractSeries, XYSeriesFormatter<?>>>();


	public void addSeries(AbstractSeries series, XYSeriesFormatter<?> formatter) {
		allSeries.add(new Pair<AbstractSeries, XYSeriesFormatter<?>>(series, formatter));
	}

	public void removeSeries(AbstractSeries series) {
		Iterator<Pair<AbstractSeries, XYSeriesFormatter<?>>> iter = allSeries.iterator();
		while (iter.hasNext())
			if (iter.next().first.equals(series)) iter.remove();
	}

	public List<Pair<AbstractSeries, XYSeriesFormatter<?>>> getSeries() {
		return allSeries;
	}

	public void reset() {
		for (Pair<AbstractSeries, ?> p : allSeries)
			p.first.reset();
	}

	public void addData(Cursor cursor) {
		for (Pair<AbstractSeries, ?> p : allSeries)
			p.first.addData(cursor);
	}
}
