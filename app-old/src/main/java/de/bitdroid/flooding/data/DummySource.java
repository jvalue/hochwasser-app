package de.bitdroid.flooding.data;

import android.content.ContentValues;

import org.json.JSONObject;

import java.util.Map;


public final class DummySource extends OdsSource {

	public static String 
			COLUMN_TEXT_DUMMY = "text_dummy",
			COLUMN_REAL_DUMMY = "real_dummy",
			COLUMN_INTEGER_DUMMY = "integer_dummy";



	private final String sourceUrl;

	public DummySource() {
		sourceUrl = "some/path/to/somewhere";
	}

	public DummySource(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}


	@Override
	public String getSourceUrlPath() {
		return sourceUrl;
	}


	@Override
	protected void getSchema(Map<String, SQLiteType> schema) {
		schema.put(COLUMN_TEXT_DUMMY, SQLiteType.TEXT);
		schema.put(COLUMN_REAL_DUMMY, SQLiteType.REAL);
		schema.put(COLUMN_INTEGER_DUMMY, SQLiteType.INTEGER);
	}


	@Override
	public String getSourceId() {
		return "dummy";
	}


	@Override
	protected void saveData(JSONObject json, ContentValues values) { }
}
