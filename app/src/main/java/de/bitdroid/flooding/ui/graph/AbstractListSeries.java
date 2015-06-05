package de.bitdroid.flooding.ui.graph;

import android.util.Pair;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**
 * Base class for managing list series on a graph.
 */
abstract class AbstractListSeries extends AbstractSeries {

	private final ArrayList<Pair<Number, Number>> values = new ArrayList<Pair<Number, Number>>();

	protected AbstractListSeries(String title) {
		super(title);
	}

	@Override
	public final Number getX(int idx) {
		return values.get(idx).first;
	}

	@Override
	public final Number getY(int idx) {
		return values.get(idx).second;
	}

	@Override
	public final int size() {
		return values.size();
	}

	@Override
	public final void reset() {
		values.clear();
	}

	protected final void addValues(Number xValue, Number yValue) {
		values.add(new Pair<>(xValue, yValue));
	}

	protected final void sortXValues() {
		Collections.sort(values, new Comparator<Pair<Number, Number>>() {
			@Override
			public int compare(Pair<Number, Number> lhs, Pair<Number, Number> rhs) {
				// bad hack that only works when Number is not subclassed
				// right solution would eliminate Number altogether and replace with Double?
				return new BigDecimal(lhs.first.toString()).compareTo(new BigDecimal(rhs.first.toString()));
			}
		});
	}
}
