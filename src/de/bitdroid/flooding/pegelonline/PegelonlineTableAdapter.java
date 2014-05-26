package de.bitdroid.flooding.pegelonline;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.content.ContentValues;

import de.bitdroid.flooding.ods.OdsTableAdapter;
import de.bitdroid.flooding.utils.SQLiteType;


public final class PegelonlineTableAdapter implements OdsTableAdapter { 

	private static final String
		KEY_WATER_NAME = "waterName",
		KEY_STATION_NAME = "stationName",
		KEY_STATION_LAT = "stationLat",
		KEY_STATION_LONG = "stationLong",
		KEY_STATION_KM = "stationKm",
		KEY_LEVEL_TIMESTAMP = "levelTimestamp",
		KEY_LEVEL_VALUE = "levelValue",
		KEY_LEVEL_UNIT = "leveUnit";


	private static final Map<String, SQLiteType> SCHEMA = new HashMap<String, SQLiteType>();

	static {
		SCHEMA.put(KEY_WATER_NAME, SQLiteType.TEXT);
		SCHEMA.put(KEY_STATION_NAME, SQLiteType.TEXT);
		SCHEMA.put(KEY_STATION_LAT, SQLiteType.REAL);
		SCHEMA.put(KEY_STATION_LONG, SQLiteType.REAL);
		SCHEMA.put(KEY_STATION_KM, SQLiteType.REAL);
		SCHEMA.put(KEY_LEVEL_TIMESTAMP, SQLiteType.TEXT);
		SCHEMA.put(KEY_LEVEL_VALUE, SQLiteType.TEXT);
		SCHEMA.put(KEY_LEVEL_UNIT, SQLiteType.TEXT);
	}


	@Override
	public Map<String, SQLiteType> getSchema() {
		return SCHEMA;
	}


	@Override
	public ContentValues saveData(JSONObject json) {
		ContentValues values = new ContentValues();

		values.put(KEY_WATER_NAME, json.optJSONObject("water").optString("longname"));
		values.put(KEY_STATION_NAME, json.optString("longname"));
		values.put(KEY_STATION_LAT, json.optDouble("latitude"));
		values.put(KEY_STATION_LONG, json.optDouble("longitude"));
		values.put(KEY_STATION_KM, json.optDouble("km"));

		JSONObject timeseries = json.optJSONArray("timeseries").optJSONObject(0);
		values.put(KEY_LEVEL_UNIT, timeseries.optString("unit"));

		JSONObject measurement = timeseries.optJSONObject("currentMeasurement");

		values.put(KEY_LEVEL_TIMESTAMP, measurement.optString("timestamp"));
		values.put(KEY_LEVEL_VALUE, measurement.optDouble("value"));

		return values;
	}

}
