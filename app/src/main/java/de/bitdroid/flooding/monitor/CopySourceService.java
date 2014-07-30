package de.bitdroid.flooding.monitor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.bitdroid.flooding.ods.data.OdsSource;
import de.bitdroid.flooding.utils.Log;
import de.bitdroid.flooding.utils.SQLiteType;


public final class CopySourceService extends IntentService {

	static final String EXTRA_SOURCE_NAME = "sourceName";


	public CopySourceService() {
		super(CopySourceService.class.getSimpleName());
	}


	@Override
	protected void onHandleIntent(Intent intent) {
		String sourceName = intent.getExtras().getString(EXTRA_SOURCE_NAME);
		OdsSource source = OdsSource.fromString(sourceName);

		Map<String, SQLiteType> schema = source.getSchema();
		Set<String> sourceColumns = new HashSet<String>(schema.keySet());
		sourceColumns.add(OdsSource.COLUMN_SERVER_ID);

		Cursor cursor = null;
		SQLiteDatabase database = null;
		
		int copyCount = 0;
		try {
			cursor = getContentResolver().query(
					source.toUri(),
					sourceColumns.toArray(new String[sourceColumns.size()]),
					null, null, null);

			if (cursor.getCount() == 0) return;

			database = new MonitorDatabase(getApplicationContext()).getWritableDatabase();


			long timeStamp = System.currentTimeMillis();
			String[] cursorColumns = cursor.getColumnNames();

			cursor.moveToFirst();
			do {
				ContentValues values = new ContentValues();
				values.put(SourceMonitor.COLUMN_MONITOR_TIMESTAMP, timeStamp);
				for (int i = 0; i < cursorColumns.length; i++) {
					String column = cursorColumns[i];
					SQLiteType type = null;
					if (column.equals(OdsSource.COLUMN_SERVER_ID)) type = SQLiteType.TEXT;
					else type = schema.get(column);

					insertIntoValues(values, column, cursor, i, type);
				}

				database.insert(source.toSqlTableName(), null, values);
				copyCount++;
			} while (cursor.moveToNext());

		} finally {
			if (cursor != null) cursor.close();
			if (database != null) database.close();
			Log.debug("Inserted " + copyCount + " entries into monitor db");
		}
	}


	private void insertIntoValues(
			ContentValues values, 
			String key,
			Cursor cursor,
			int colIdx,
			SQLiteType type) {


		switch(type) {
			case TEXT:
				String string = cursor.getString(colIdx);
				values.put(key, string);
				break;
			case INTEGER:
				Integer integer = cursor.getInt(colIdx);
				values.put(key, integer);
				break;
			case REAL:
				Double doubl = cursor.getDouble(colIdx);
				values.put(key, doubl);
				break;
			case BLOB:
				byte[] bytes = cursor.getBlob(colIdx);
				values.put(key, bytes);
				break;
			case NULL:
				values.putNull(key);
				break;
		}
	}
}
