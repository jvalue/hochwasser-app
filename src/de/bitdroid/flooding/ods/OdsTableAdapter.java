package de.bitdroid.flooding.ods;

import java.util.Map;

import org.json.JSONObject;

import android.content.ContentValues;

import de.bitdroid.flooding.utils.SQLiteType;


/**
 * Adapter that determines what to save of data fetched from the ODS.
 */
public interface OdsTableAdapter {

	/**
	 * Returns the source URL NOT including domain name and port.
	 */
	public String getSourceUrl();


	/**
	 * Describes what parts of data should be saved.
	 */
	public Map<String, SQLiteType> getSchema(); 


	/**
	 * Fetch parts that should be stored.
	 */
	public ContentValues saveData(JSONObject jsonObject);

}
