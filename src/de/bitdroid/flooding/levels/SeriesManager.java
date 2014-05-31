package de.bitdroid.flooding.levels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Pair;

import com.androidplot.xy.XYSeriesFormatter;


final class SeriesManager {
	
	private final List<Pair<AbstractSeries, XYSeriesFormatter<?>>>
			allSeries = new ArrayList<Pair<AbstractSeries, XYSeriesFormatter<?>>>();
			
	private final Map<String, Pair<AbstractSeries, XYSeriesFormatter<?>>> 
			visibleSeries = new HashMap<String, Pair<AbstractSeries, XYSeriesFormatter<?>>>(),
			hiddenSeries = new HashMap<String, Pair<AbstractSeries, XYSeriesFormatter<?>>>();


	public String addSeries(AbstractSeries series, XYSeriesFormatter<?> formatter) {
		Pair<AbstractSeries, XYSeriesFormatter<?>> pair 
				= new Pair<AbstractSeries, XYSeriesFormatter<?>>(series, formatter);

		allSeries.add(pair);
		visibleSeries.put(series.getTitle(), pair);

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
		List<Pair<AbstractSeries, XYSeriesFormatter<?>>> ret 
			= new ArrayList<Pair<AbstractSeries, XYSeriesFormatter<?>>>();

		for (Pair<AbstractSeries, XYSeriesFormatter<?>> p : allSeries)
			if (visibleSeries.containsValue(p)) ret.add(p);

		return ret;
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


	public List<String> getSeriesKeys() {
		List<String> ret = new ArrayList<String>();
		for (Pair<AbstractSeries, ?> p : allSeries)
			ret.add(p.first.getTitle());
		return ret;
	}



	private static final String EXTRA_VISIBLE_SERIES = "visibleSeries";
	public void saveVisibleSeries(Bundle state) {
		int count = 0;
		for (String key : visibleSeries.keySet()) { 
			state.putString(EXTRA_VISIBLE_SERIES + count, key);
			count++;
		}
	}


	public void restoreVisibleSeries(Bundle state) {	
		hiddenSeries.putAll(visibleSeries);
		visibleSeries.clear();
		String key;
		int count = 0;
		while ((key = state.getString(EXTRA_VISIBLE_SERIES + count, null)) != null) {
			makeSeriesVisible(key);
			count++;
		}
	}
}
