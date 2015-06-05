package de.bitdroid.flooding.ui.graph;

import java.util.List;

import de.bitdroid.flooding.ods.StationMeasurements;
import de.bitdroid.flooding.utils.PegelOnlineUtils;


class NormalizedSeries extends AbstractListSeries {

	public NormalizedSeries(String title) {
		super(title);
	}

	@Override
	public void addData(List<StationMeasurements> measurementsList) {
		for (StationMeasurements measurements : measurementsList) {
			if (measurements.getMhw() == null || measurements.getMw() == null || measurements.getMnw() == null) {
				continue;
			}

			float xValue = measurements.getStation().getRiverKm();
			float yValue = PegelOnlineUtils.toCm(measurements.getLevel().getValue(), measurements.getLevel().getUnit());
			float lower = PegelOnlineUtils.toCm(measurements.getMnw().getValue(), measurements.getMnw().getUnit());
			float mid = PegelOnlineUtils.toCm(measurements.getMw().getValue(), measurements.getMw().getUnit());
			float upper = PegelOnlineUtils.toCm(measurements.getMhw().getValue(), measurements.getMhw().getUnit());

			float normalizedYValue;
			if (yValue > mid) normalizedYValue = 0.5f * (1 + ((yValue - mid) / (upper - mid)));
			else normalizedYValue = 0.5f * ((yValue - lower)  / (mid - lower));

			addValues(xValue, normalizedYValue * 100);
		}
	}

}
