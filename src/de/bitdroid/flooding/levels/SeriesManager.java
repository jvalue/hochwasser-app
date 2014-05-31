package de.bitdroid.flooding.levels;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.database.Cursor;
import android.util.Pair;

import com.androidplot.xy.XYSeriesFormatter;


final class SeriesManager {
	
	private final Map<String, Pair<AbstractSeries, XYSeriesFormatter<?>>> 
			visibleSeries = new HashMap<String, Pair<AbstractSeries, XYSeriesFormatter<?>>>(),
			hiddenSeries = new HashMap<String, Pair<AbstractSeries, XYSeriesFormatter<?>>>();


	public String addSeries(AbstractSeries series, XYSeriesFormatter<?> formatter) {
		visibleSeries.put(
				series.getTitle(),
				new Pair<AbstractSeries, XYSeriesFormatter<?>>(series, formatter));
		return series.getTitle();
	}


	public void makeSeriesVisible(String seriesKey) {
		Pair<AbstractSeries, XYSeriesFormatter<?>> series = hiddenSeries.remove(seriesKey);
		if (series == null)  return;
		visibleSeries.put(seriesKey,  series);
	}


	public void makeSeriesHidden(String seriesKey) {
		Pair<AbstractSeries, XYSeriesFormatter<?>> series = visibleSeries.remove(seriesKey);
		if (series == null)  return;
		hiddenSeries.put(seriesKey,  series);
	}


	public Collection<Pair<AbstractSeries, XYSeriesFormatter<?>>> getVisibleSeries() {
		return visibleSeries.values();
	}


	public boolean isVisible(String seriesKey) {
		return visibleSeries.containsKey(seriesKey);
	}
	

	public void reset() {
		for (Pair<AbstractSeries, ?> p : visibleSeries.values())
			p.first.reset();
		for (Pair<AbstractSeries, ?> p : hiddenSeries.values())
			p.first.reset();
	}


	public void addData(Cursor cursor) {
		for (Pair<AbstractSeries, ?> p : visibleSeries.values())
			p.first.addData(cursor);
		for (Pair<AbstractSeries, ?> p : hiddenSeries.values())
			p.first.addData(cursor);
	}
}
