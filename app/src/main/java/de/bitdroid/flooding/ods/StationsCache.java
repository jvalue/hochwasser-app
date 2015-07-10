package de.bitdroid.flooding.ods;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.inject.Inject;

import org.roboguice.shaded.goole.common.base.Optional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Caches {@link Station} in memory and in a DB.
 */
public class StationsCache {

	private static final String PREFS_NAME = "de.bitdroid.flooding.ods.StationsCache";
	private static final String KEY_LAST_UPDATE = "KEY_LAST_UPDATE";

	private static final long DB_CACHE_MAX_AGE_IN_SECONDS = 60 * 60 * 24 * 10; // 10 days

	private final Context context;
	private final Map<String, Station> stations = new HashMap<>(); // station uuid --> station

	@Inject
	StationsCache(Context context) {
		this.context = context;
	}


	public void setStations(Map<String, Station> stations) {
		// store in memory
		this.stations.clear();
		this.stations.putAll(stations);

		// store in db
		List<BodyOfWater> waters = new ArrayList<>();
		for (Station station : stations.values()) waters.add(station.getBodyOfWater());
		BodyOfWater.saveInTx(waters);
		Station.saveInTx(stations.values());

		// mark data as "fresh"
		setLastUpdateTimestamp(System.currentTimeMillis() / 1000);
	}


	public Optional<Map<String, Station>> getStations() {
		long currentTimestamp = System.currentTimeMillis() / 1000;

		if (currentTimestamp - getLastUpdateTimestamp() > DB_CACHE_MAX_AGE_IN_SECONDS) {
			Timber.d("station cache miss");

			// clear memory and DB if cache is outdated
			stations.clear();
			Station.deleteAll(Station.class);
			return Optional.absent();

		} else {
			Timber.d("station cache hit");

			// load from DB if necessary
			if (stations.isEmpty()) {
				for (Station station : Station.listAll(Station.class)) {
					stations.put(station.getGaugeId(), station);
				}
			}
			return Optional.of(stations);
		}
	}


	private long getLastUpdateTimestamp() {
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getLong(KEY_LAST_UPDATE, 0);
	}


	private void setLastUpdateTimestamp(long timetstamp) {
		SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
		editor.putLong(KEY_LAST_UPDATE, timetstamp);
		editor.commit();
	}

}
