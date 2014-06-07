package de.bitdroid.flooding.ods;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.content.ContentValues;

import de.bitdroid.flooding.utils.SQLiteType;


public final class DummySource extends OdsSource {

	public static String 
			COLUMN_TEXT_DUMMY = "text_dummy",
			COLUMN_REAL_DUMMY = "real_dummy",
			COLUMN_INTEGER_DUMMY = "integer_dummy";

	private static Map<String, SQLiteType> schema = new HashMap<String, SQLiteType>();
	static {
		schema.put(COLUMN_TEXT_DUMMY, SQLiteType.TEXT);
		schema.put(COLUMN_REAL_DUMMY, SQLiteType.REAL);
		schema.put(COLUMN_INTEGER_DUMMY, SQLiteType.INTEGER);
	}


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
	public Map<String, SQLiteType> getSchema() {
		return schema;
	}


	@Override
	public ContentValues saveData(JSONObject json) {
		return new ContentValues();
	}
}
