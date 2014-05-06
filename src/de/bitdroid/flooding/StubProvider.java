package de.bitdroid.flooding;

import java.util.LinkedList;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;


public final class StubProvider extends ContentProvider {

	public static final String KEY = "value";

	private final List<String> values = new LinkedList<String>();


	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public String getType(Uri uri) {
		return new String();
	}

	@Override
	public Cursor query(
			Uri uri,
			String[] projection,
			String  selection,
			String[] selectionArgs,
			String sortOrder) {

		Log.i("Flooding", "Cursor has " + values.size() + " elements");

		return new AbstractCursor() {
			@Override
			public String [] getColumnNames() { return null; }
			@Override
			public int getCount() { return values.size(); }
			@Override
			public String getString(int idx) { return values.get(idx); }
			@Override
			public float getFloat(int idx) { return 0; }
			@Override
			public int getInt(int idx) { return 0; }
			@Override
			public long getLong(int idx) { return 0; }
			@Override
			public short getShort(int idx) { return (short) 0; }
			@Override
			public double getDouble(int idx) { return 0; }
			@Override
			public boolean isNull(int idx) { return false; }
		};
	}

	@Override
	public Uri insert(Uri uri, ContentValues data) {
		values.add(data.getAsString(KEY));
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public int update(
			Uri uri,
			ContentValues values,
			String  selection,
			String[] selectionArgs) {
		return 0;
	}
}
