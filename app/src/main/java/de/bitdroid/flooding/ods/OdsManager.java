package de.bitdroid.flooding.ods;


import com.fasterxml.jackson.databind.JsonNode;

import org.jvalue.ods.api.DataApi;
import org.jvalue.ods.api.data.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

/**
 * Responsible for communicating with the ODS.
 */
public class OdsManager {

	private static final String PEGELONLINE_SOURCE_ID = "pegelonline";
	private static final int QUERY_COUNT = 100;
	private static final String PROPERTY_FILTER_STATION
			= "result.shortname,result.longname,result.km,result.latitude,result.longitude,result.water.longname,cursor";

	private final DataApi dataApi;

	private List<Station> stationCache;


	@Inject
	OdsManager(DataApi dataApi) {
		this.dataApi = dataApi;
	}


	public Observable<List<Station>> getStations() {
		if (stationCache != null) {
			return Observable.just(stationCache);
		} else {
			return Observable.defer(new Func0<Observable<List<Station>>>() {
				@Override
				public Observable<List<Station>> call() {
					List<Station> stations = new ArrayList<>();
					String startId = null;
					Data data;
					do {
						data = dataApi.getObjectsSynchronously(PEGELONLINE_SOURCE_ID, startId, QUERY_COUNT, PROPERTY_FILTER_STATION);
						for (JsonNode node : data.getResult()) {
							stations.add(parseStation(node));
						}
						startId = data.getCursor().getNext();

					} while (data.getCursor().getHasNext());
					return Observable.just(stations);
				}
			});
		}
	}


	public Observable<List<BodyOfWater>> getBodyOfWaters() {
		return getStations()
				.flatMap(new Func1<List<Station>, Observable<List<BodyOfWater>>>() {
					@Override
					public Observable<List<BodyOfWater>> call(List<Station> stations) {
						Set<BodyOfWater> bodies = new HashSet<>();
						for (Station station : stations) bodies.add(station.getBodyOfWater());
						List<BodyOfWater> result = new ArrayList<>(bodies);
						return Observable.just(result);
					}
				});
	}


	private Station parseStation(JsonNode node) {
		return new Station.Builder()
				.setUuid(node.path("uuid").asText())
				.setStationName(node.path("longname").asText())
				.setBodyOfWater(new BodyOfWater(node.path("water").path("longname").asText()))
				.setLatitude((float) node.path("latitude").asDouble())
				.setLongitude((float) node.path("longitude").asDouble())
				.setRiverKm(node.path("km").intValue())
				.build();
	}


	/*
	private void foobar() {

		JsonNode timeseries = node.path("timeseries");
		JsonNode currentMeasurement = timeseries.path("currentMeasurement");
		builder
				.setLevel(new Measurement(
						(float) currentMeasurement.path("value").asDouble(),
						timeseries.path("unit").asText()));

		for (JsonNode charValue : timeseries.path("characteristicValues")) {
			Measurement measurement = new Measurement((float) charValue.path("value").asDouble(), charValue.path("unit").asText());
			String charType = charValue.path("longname").asText();

			if ("MNW".equalsIgnoreCase(charType)) {
				builder.setMnw(measurement);
			} else if ("MW".equalsIgnoreCase(charType)) {
				builder.setMw(measurement);
			} else if ("MHW".equalsIgnoreCase(charType)) {
				builder.setMhw(measurement);
			} else if ("HThw".equalsIgnoreCase(charType)) {
				builder.setHthw(measurement);
			} else if ("NTnw".equalsIgnoreCase(charType)) {
				builder.setNtnw(measurement);
			} else if ("MTnw".equalsIgnoreCase(charType)) {
				builder.setMtnw(measurement);
			} else if ("MThw".equalsIgnoreCase(charType)) {
				builder.setMthw(measurement);
			}
		}

		return builder.build();
	}

*/

}