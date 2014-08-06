package de.bitdroid.flooding.monitor;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.bitdroid.flooding.R;
import de.bitdroid.ods.data.OdsSource;
import de.bitdroid.utils.Log;
import de.bitdroid.ods.data.SQLiteType;

import static de.bitdroid.ods.data.OdsSource.COLUMN_TIMESTAMP;


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

		List<Long> timestamps = SourceMonitor.getInstance(getApplicationContext()).getAvailableTimestamps(source);
		Collections.sort(timestamps);

		Cursor cursor = null;
		SQLiteDatabase database = null;
		
		int copyCount = 0;
		try {
			// check for timestamp
			cursor = getContentResolver().query(
					source.toUri(),
					sourceColumns.toArray(new String[sourceColumns.size()]),
					null, null, null);

			if (cursor.getCount() == 0) return;
			cursor.moveToFirst();

			int timestampIdx = cursor.getColumnIndex(OdsSource.COLUMN_TIMESTAMP);
			long dataTimestamp = cursor.getLong(timestampIdx);
			long monitorTimestamp = timestamps.isEmpty() ? -1 : timestamps.get(timestamps.size() - 1);

			if (dataTimestamp <= monitorTimestamp) return;

			// insert new values
			database = new MonitorDatabase(getApplicationContext()).getWritableDatabase();
			String[] cursorColumns = cursor.getColumnNames();
			timestamps.add(dataTimestamp);

			do {
				ContentValues values = new ContentValues();
				for (int i = 0; i < cursorColumns.length; i++) {
					String column = cursorColumns[i];
					SQLiteType type = schema.get(column);

					insertIntoValues(values, column, cursor, i, type);
				}

				database.insert(source.toSqlTableName(), null, values);
				copyCount++;
			} while (cursor.moveToNext());

			// remove old values (according to settings)

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			long monitorPeriod = Long.valueOf(prefs.getString(getString(R.string.prefs_ods_monitor_days_key), "-1"));
			double monitorInterval = Float.valueOf(prefs.getString(getString(R.string.prefs_ods_monitor_interval_key), "-1"));
			if (monitorPeriod == -1 || monitorInterval == -1) {
				Log.error("failed to read settings for removing old monitor data");
				return;
			}

			long monitorCount = (long) (monitorPeriod * (24.0 / monitorInterval));
			int deletionCount;
			for (deletionCount = 0; monitorCount + deletionCount < timestamps.size(); deletionCount++) {
				long timestamp = timestamps.get(deletionCount);
				database.delete(
						source.toSqlTableName(),
						COLUMN_TIMESTAMP + "=?",
						new String[] { String.valueOf(timestamp) });
			}
			Log.debug("deleted " + deletionCount + " old monitor entries");


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
				Long lon = cursor.getLong(colIdx);
				values.put(key, lon);
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
