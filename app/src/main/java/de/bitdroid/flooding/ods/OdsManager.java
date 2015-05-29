package de.bitdroid.flooding.ods;


import android.util.Pair;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;

import org.jvalue.ods.api.DataApi;
import org.jvalue.ods.api.data.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Responsible for communicating with the ODS.
 *
 * Note that this class is a singleton, meaning that it will be kept around in memory even if
 * no activities are visible.
 */
@Singleton
public class OdsManager {

	private static final String PEGELONLINE_SOURCE_ID = "pegelonline";
	private static final int QUERY_COUNT = 100;
	private static final String PROPERTY_FILTER_STATION
			= "result.uuid,result.shortname,result.longname,result.km,result.latitude,result.longitude,result.water.longname,cursor";

	private final DataApi dataApi;
	private final PegelOnlineUtils pegelOnlineUtils;

	private Map<String, Station> stationCache; // station uuid --> station


	@Inject
	OdsManager(DataApi dataApi, PegelOnlineUtils pegelOnlineUtils) {
		this.dataApi = dataApi;
		this.pegelOnlineUtils = pegelOnlineUtils;
	}


	public Observable<List<Station>> getStations() {
		if (stationCache != null) {
			return Observable.just(sortStations(stationCache.values()));
		} else {
			return Observable.defer(new Func0<Observable<List<Station>>>() {
				@Override
				public Observable<List<Station>> call() {
					// download stations
					Map<String, List<Station.Builder>> builderMap = new HashMap<>();
					String startId = null;
					Data data;
					do {
						// iterate over ODS cursor
						data = dataApi.getObjectsSynchronously(PEGELONLINE_SOURCE_ID, startId, QUERY_COUNT, PROPERTY_FILTER_STATION);
						for (JsonNode node : data.getResult()) {
							Pair<String, Station.Builder> pair = parseStation(node);
							List<Station.Builder> stationsList = builderMap.get(pair.first);
							if (stationsList == null) {
								stationsList = new ArrayList<>();
								builderMap.put(pair.first, stationsList);
							}
							stationsList.add(pair.second);
						}
						startId = data.getCursor().getNext();

					} while (data.getCursor().getHasNext());

					// count stations per water and build stations
					stationCache = new HashMap<>();
					for (String waterName : builderMap.keySet()) {
						BodyOfWater water = new BodyOfWater(waterName, builderMap.get(waterName).size());
						for (Station.Builder builder : builderMap.get(waterName)) {
							Station station = builder.setBodyOfWater(water).build();
							stationCache.put(station.getUuid(), station);
						}
					}
					return Observable.just(sortStations(stationCache.values()));
				}
			});
		}
	}


	public Observable<List<Station>> getStationsByBodyOfWater(final BodyOfWater water) {
		return getStations()
				.flatMap(new Func1<List<Station>, Observable<List<Station>>>() {
					@Override
					public Observable<List<Station>> call(List<Station> stations) {
						List<Station> filteredStations = new ArrayList<>();
						for (Station station : stations) {
							if (station.getBodyOfWater().getName().equals(water.getName()))
								filteredStations.add(station);
						}
						return Observable.just(filteredStations);
					}
				});
	}


	public Observable<Optional<Station>> getStationByUuid(final String stationUuid) {
		if (stationCache == null) {
			return getStations()
					.flatMap(new Func1<Collection<Station>, Observable<Optional<Station>>>() {
						@Override
						public Observable<Optional<Station>> call(Collection<Station> stations) {
							return Observable.just(Optional.of(stationCache.get(stationUuid)));
						}
					});
		} else {
			return Observable.just(Optional.of(stationCache.get(stationUuid)));
		}
	}


	public Observable<List<BodyOfWater>> getBodyOfWaters() {
		return getStations()
				.flatMap(new Func1<List<Station>, Observable<List<BodyOfWater>>>() {
					@Override
					public Observable<List<BodyOfWater>> call(List<Station> stations) {
						Set<BodyOfWater> bodies = new HashSet<>();
						for (Station station : stations) bodies.add(station.getBodyOfWater());
						List<BodyOfWater> result = sortBodyOfWaters(bodies);
						return Observable.just(result);
					}
				});
	}


	public Observable<StationMeasurements> getMeasurements(final Station station) {
		Timber.d("loading station " + station.getUuid());
		return dataApi.getObjectAttribute(PEGELONLINE_SOURCE_ID, station.getUuid(), "")
				.flatMap(new Func1<JsonNode, Observable<StationMeasurements>>() {
					@Override
					public Observable<StationMeasurements> call(JsonNode data) {
						return Observable.just(parseMeasurements(station, data));
					}
				});
	}


	/**
	 * Parse station and return station along with water name.
	 */
	private Pair<String, Station.Builder> parseStation(JsonNode node) {
		Station.Builder builder = new Station.Builder()
				.setUuid(node.path("uuid").asText())
				.setStationName(node.path("longname").asText());

		Float lat = parseFloat(node.path("latitude"));
		if (lat != null) builder.setLatitude(lat);
		Float lon = parseFloat(node.path("longitude"));
		if (lon != null) builder.setLongitude(lon);
		Float riverKm = parseFloat(node.path("km"));
		if (riverKm != null) builder.setRiverKm(riverKm);

		return new Pair<>(
				node.path("water").path("longname").asText(),
				builder);
	}


	private Float parseFloat(JsonNode value) {
		if (value.isMissingNode()) return null;
		return (float) value.asDouble();
	}


	private StationMeasurements parseMeasurements(Station station, JsonNode data) {
		StationMeasurements.Builder builder = new StationMeasurements.Builder(station);

		JsonNode timeSeries = data.path("timeseries").path(0);

		// current measurement
		JsonNode currentMeasurement = timeSeries.path("currentMeasurement");
		if (!currentMeasurement.isMissingNode()) {
			builder.setLevel(new Measurement(
					(float) currentMeasurement.path("value").asDouble(),
					timeSeries.path("unit").asText()));
			builder.setLevelTimestamp(pegelOnlineUtils.parseStringTimestamp(currentMeasurement.path("timestamp").asText()));
		}

		// gauge zero
		JsonNode gaugeZero = timeSeries.path("gaugeZero");
		if (!gaugeZero.isMissingNode()) {
			builder.setLevelZero(new Measurement(
					(float) gaugeZero.path("value").asDouble(),
					gaugeZero.path("unit").asText()));
		}

		// characteristic values
		for (JsonNode charValue : timeSeries.path("characteristicValues")) {
			Measurement measurement = new Measurement((float) charValue.path("value").asDouble(), charValue.path("unit").asText());
			String charType = charValue.path("shortname").asText();

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


	private List<Station> sortStations(Collection<Station> stations) {
		List<Station> sortedStations = new ArrayList<>(stations);
		Collections.sort(sortedStations, new Comparator<Station>() {
			@Override
			public int compare(Station lhs, Station rhs) {
				return lhs.getStationName().compareTo(rhs.getStationName());
			}
		});
		return sortedStations;
	}


	private List<BodyOfWater> sortBodyOfWaters(Collection<BodyOfWater> waters) {
		List<BodyOfWater> sortedWaters = new ArrayList<>(waters);
		Collections.sort(sortedWaters, new Comparator<BodyOfWater>() {
			@Override
			public int compare(BodyOfWater lhs, BodyOfWater rhs) {
				return lhs.getName().compareTo(rhs.getName());
			}
		});
		return sortedWaters;
	}

}