package de.bitdroid.flooding.pegelonline;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.content.ContentValues;

import de.bitdroid.flooding.ods.OdsSource;
import de.bitdroid.flooding.utils.SQLiteType;


public final class PegelonlineTableAdapter extends OdsSource { 

	public static final String
		COLUMN_WATER_NAME = "waterName",
		COLUMN_STATION_NAME = "stationName",
		COLUMN_STATION_LAT = "stationLat",
		COLUMN_STATION_LONG = "stationLong",
		COLUMN_STATION_KM = "stationKm",
		COLUMN_LEVEL_TIMESTAMP = "levelTimestamp",
		COLUMN_LEVEL_VALUE = "levelValue",
		COLUMN_LEVEL_UNIT = "leveUnit";

	private static final String
		SOURCE_URL = "ods/de/pegelonline/stations";


	private static final Map<String, SQLiteType> SCHEMA = new HashMap<String, SQLiteType>();

	static {
		SCHEMA.put(COLUMN_WATER_NAME, SQLiteType.TEXT);
		SCHEMA.put(COLUMN_STATION_NAME, SQLiteType.TEXT);
		SCHEMA.put(COLUMN_STATION_LAT, SQLiteType.REAL);
		SCHEMA.put(COLUMN_STATION_LONG, SQLiteType.REAL);
		SCHEMA.put(COLUMN_STATION_KM, SQLiteType.REAL);
		SCHEMA.put(COLUMN_LEVEL_TIMESTAMP, SQLiteType.TEXT);
		SCHEMA.put(COLUMN_LEVEL_VALUE, SQLiteType.TEXT);
		SCHEMA.put(COLUMN_LEVEL_UNIT, SQLiteType.TEXT);
	}


	@Override
	public String getSourceUrl() {
		return SOURCE_URL;
	}


	@Override
	public Map<String, SQLiteType> getSchema() {
		return SCHEMA;
	}


	@Override
	public ContentValues saveData(JSONObject json) {
		ContentValues values = new ContentValues();

		values.put(COLUMN_WATER_NAME, json.optJSONObject("water").optString("longname"));
		values.put(COLUMN_STATION_NAME, json.optString("longname"));
		values.put(COLUMN_STATION_LAT, json.optDouble("latitude"));
		values.put(COLUMN_STATION_LONG, json.optDouble("longitude"));
		values.put(COLUMN_STATION_KM, json.optDouble("km"));

		JSONObject timeseries = json.optJSONArray("timeseries").optJSONObject(0);
		values.put(COLUMN_LEVEL_UNIT, timeseries.optString("unit"));

		JSONObject measurement = timeseries.optJSONObject("currentMeasurement");

		values.put(COLUMN_LEVEL_TIMESTAMP, measurement.optString("timestamp"));
		values.put(COLUMN_LEVEL_VALUE, measurement.optDouble("value"));

		return values;
	}

}
