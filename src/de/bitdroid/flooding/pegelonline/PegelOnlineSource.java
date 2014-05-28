package de.bitdroid.flooding.pegelonline;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.content.ContentValues;

import de.bitdroid.flooding.ods.OdsSource;
import de.bitdroid.flooding.utils.SQLiteType;


public final class PegelOnlineSource extends OdsSource { 

	public static final String
		COLUMN_WATER_NAME = "waterName",
		COLUMN_STATION_NAME = "stationName",
		COLUMN_STATION_LAT = "stationLat",
		COLUMN_STATION_LONG = "stationLong",
		COLUMN_STATION_KM = "stationKm",
		COLUMN_LEVEL_TIMESTAMP = "levelTimestamp",
		COLUMN_LEVEL_VALUE = "levelValue",
		COLUMN_LEVEL_UNIT = "leveUnit",
		COLUMN_LEVEL_TYPE = "leveType",
		COLUMN_LEVEL_ZERO_VALUE = "levelZeroValue",
		COLUMN_LEVEL_ZERO_UNIT = "levelZeroUnit";


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
		SCHEMA.put(COLUMN_LEVEL_VALUE, SQLiteType.REAL);
		SCHEMA.put(COLUMN_LEVEL_UNIT, SQLiteType.TEXT);
		SCHEMA.put(COLUMN_LEVEL_ZERO_VALUE, SQLiteType.REAL);
		SCHEMA.put(COLUMN_LEVEL_ZERO_UNIT, SQLiteType.TEXT);
		SCHEMA.put(COLUMN_LEVEL_TYPE, SQLiteType.TEXT);
	}


	@Override
	public String getSourceUrlPath() {
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
		values.put(COLUMN_LEVEL_TYPE, timeseries.optString("shortname"));

		JSONObject measurement = timeseries.optJSONObject("currentMeasurement");
		values.put(COLUMN_LEVEL_TIMESTAMP, measurement.optString("timestamp"));
		values.put(COLUMN_LEVEL_VALUE, measurement.optDouble("value"));

		JSONObject gaugeZero = timeseries.optJSONObject("gaugeZero");
		if (gaugeZero != null) {
			values.put(COLUMN_LEVEL_ZERO_VALUE, gaugeZero.optDouble("value"));
			values.put(COLUMN_LEVEL_ZERO_UNIT, gaugeZero.optString("unit"));
		}

		return values;
	}

}
