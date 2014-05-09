package de.bitdroid.flooding.ods.json;

import org.json.JSONException;
import org.json.JSONObject;


public final class PegelonlineParser {

	private PegelonlineParser() { }

	public static String getStationName(JSONObject object) {
		if (object == null) throw new NullPointerException("param cannot be null");

		try {
			return object.getString("shortname");
		} catch(JSONException je) {
			throw new IllegalArgumentException(je);
		}
	}


	public static String getServerId(JSONObject object) {
		if (object == null) throw new NullPointerException("param cannot be null");

		try {
			return object.getString("_id");
		} catch(JSONException je) {
			throw new IllegalArgumentException(je);
		}
	}


	public static double getLatitude(JSONObject object) {
		if (object == null) throw new NullPointerException("param cannot be null");

		try {
			return object.getDouble("latitude");
		} catch(JSONException je) {
			throw new IllegalArgumentException(je);
		}
	}


	public static double getLongitude(JSONObject object) {
		if (object == null) throw new NullPointerException("param cannot be null");

		try {
			return object.getDouble("longitude");
		} catch(JSONException je) {
			throw new IllegalArgumentException(je);
		}
	}


	public static boolean isValidStation(JSONObject object) {
		try {
			return getStationName(object) != null;
		} catch (Exception e) {
			return false;
		}
	}


}
