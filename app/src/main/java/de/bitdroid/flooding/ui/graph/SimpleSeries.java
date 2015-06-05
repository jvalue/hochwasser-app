package de.bitdroid.flooding.ui.graph;

import java.util.List;

import de.bitdroid.flooding.ods.Measurement;
import de.bitdroid.flooding.ods.StationMeasurements;
import de.bitdroid.flooding.utils.PegelOnlineUtils;


/**
 * Simple list series for showing data without performing calculations.
 */
abstract class SimpleSeries extends AbstractListSeries {

	public SimpleSeries(String title) {
		super(title);
	}

	@Override
	public void addData(List<StationMeasurements> measurementsList) {
		for (StationMeasurements measurements : measurementsList) {
			if (getYValue(measurements) == null) continue;
			addValues(
					measurements.getStation().getRiverKm(),
					PegelOnlineUtils.toCm(
							getYValue(measurements).getValue(),
							getYValue(measurements).getUnit()));
		}
	}


	protected abstract Measurement getYValue(StationMeasurements measurements);


	public static SimpleSeries createLevelSeries(String title) {
		return new SimpleSeries(title) {
			@Override
			protected Measurement getYValue(StationMeasurements measurements) {
				return measurements.getLevel();
			}
		};
	}


	public static SimpleSeries createMWSeries(String title) {
		return new SimpleSeries(title) {
			@Override
			protected Measurement getYValue(StationMeasurements measurements) {
				return measurements.getMw();
			}
		};
	}


	public static SimpleSeries createMHWSeries(String title) {
		return new SimpleSeries(title) {
			@Override
			protected Measurement getYValue(StationMeasurements measurements) {
				return measurements.getMhw();
			}
		};
	}

	public static SimpleSeries createMNWSeries(String title) {
		return new SimpleSeries(title) {
			@Override
			protected Measurement getYValue(StationMeasurements measurements) {
				return measurements.getMnw();
			}
		};
	}

	public static SimpleSeries createMTHWSeries(String title) {
		return new SimpleSeries(title) {
			@Override
			protected Measurement getYValue(StationMeasurements measurements) {
				return measurements.getMthw();
			}
		};
	}

	public static SimpleSeries createMTNWSeries(String title) {
		return new SimpleSeries(title) {
			@Override
			protected Measurement getYValue(StationMeasurements measurements) {
				return measurements.getMtnw();
			}
		};
	}

	public static SimpleSeries createHTHWSeries(String title) {
		return new SimpleSeries(title) {
			@Override
			protected Measurement getYValue(StationMeasurements measurements) {
				return measurements.getHthw();
			}
		};
	}

	public static SimpleSeries createNTNWSeries(String title) {
		return new SimpleSeries(title) {
			@Override
			protected Measurement getYValue(StationMeasurements measurements) {
				return measurements.getNtnw();
			}
		};
	}

}
