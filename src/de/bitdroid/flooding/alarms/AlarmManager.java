package de.bitdroid.flooding.alarms;

import static de.bitdroid.flooding.alarms.AlarmDbSchema.COLUMN_ID;
import static de.bitdroid.flooding.alarms.AlarmDbSchema.COLUMN_JSON;
import static de.bitdroid.flooding.alarms.AlarmDbSchema.TABLE_NAME;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.bitdroid.flooding.utils.Assert;
import de.bitdroid.flooding.utils.Log;


final class AlarmManager {

	private static final ObjectMapper mapper = new ObjectMapper();

	private static AlarmManager instance;

	public static AlarmManager getInstance(Context context) {
		Assert.assertNotNull(context);
		if (instance == null) instance = new AlarmManager(context);
		return instance;
	}


	private final AlarmDb alarmDb;

	private AlarmManager(Context context) {
		this.alarmDb = new AlarmDb(context);
	}


	public Map<Long, Alarm> getAll() {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(TABLE_NAME);
		String[] columns = { COLUMN_ID, COLUMN_JSON };
		Cursor cursor = builder.query(
				alarmDb.getReadableDatabase(),
				columns,
				null, null, null, null, null);

		Map<Long, Alarm> alarms = new HashMap<Long, Alarm>();

		if (cursor.getCount() <= 0) return alarms;
		cursor.moveToFirst();
		do {
			long id = cursor.getLong(0);
			String json = cursor.getString(1);

			try {
				Alarm alarm = mapper.treeToValue(mapper.readTree(json), Alarm.class);
				alarms.put(id, alarm);
			} catch (Exception e) {
				Log.error("failed to read alarm from db", e);
			}

		} while (cursor.moveToNext());

		return alarms;
	}


	public long add(Alarm alarm) {
		Assert.assertNotNull(alarm);

		SQLiteDatabase database = null;
		try {
			database = alarmDb.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(COLUMN_JSON, mapper.valueToTree(alarm).toString());
			return database.insert(TABLE_NAME, null, values);
		} finally {
			if (database != null) database.close();
		}
	}


	public void remove(long id) {
		SQLiteDatabase database = null;
		try {
			database = alarmDb.getWritableDatabase();
			database.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{ String.valueOf(id) });
		} finally {
			if (database != null) database.close();
		}
	}

}
