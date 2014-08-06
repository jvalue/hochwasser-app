package de.bitdroid.flooding.monitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.bitdroid.flooding.ods.data.OdsSource;
import de.bitdroid.utils.Assert;
import de.bitdroid.utils.Log;

import static de.bitdroid.flooding.ods.data.OdsSource.COLUMN_TIMESTAMP;


public final class SourceMonitor {

	public final static String COLUMN_ID = "_id";

	private final static String PREFS_NAME = "de.bitdroid.flooding.monitor.SourceMonitor";


	private static SourceMonitor instance;
	public static SourceMonitor getInstance(Context context) {
		Assert.assertNotNull(context);
		if (instance == null) instance = new SourceMonitor(context);
		return instance;
	}


	private final MonitorDatabase monitorDatabase;
	private final Context context;

	private SourceMonitor(Context context) {
		this.monitorDatabase = new MonitorDatabase(context);
		this.context = context;
	}


	public void startMonitoring(OdsSource source)  {
		Assert.assertNotNull(source);
		Assert.assertFalse(isBeingMonitored(source), "Already being monitored");

		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putString(source.toString(), "").commit();

		monitorDatabase.addSource(
				monitorDatabase.getWritableDatabase(), 
				source.toSqlTableName(), 
				source);

		Log.debug("Starting SourceMonitor for " + source.getSourceId());
	}


	public void stopMonitoring(OdsSource source) {
		Assert.assertNotNull(source);
		Assert.assertTrue(isBeingMonitored(source), "Not monitored");

		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.remove(source.toString()).commit();

		Log.debug("Stopping SourceMonitor for " + source.getSourceId());
	}


	public boolean isBeingMonitored(OdsSource source) {
		Assert.assertNotNull(source);
		return getSharedPreferences().contains(source.toString());
	}


	public Set<OdsSource> getMonitoredSources() {
		Set<OdsSource> ret = new HashSet<OdsSource>();

		SharedPreferences prefs = getSharedPreferences();
		for (String className : prefs.getAll().keySet()) {
			ret.add(OdsSource.fromString(className));
		}

		return ret;
	}


	public Cursor query(
			OdsSource source,
			String[] projection,
			String selection,
			String[] selectionArgs,
			String sortOrder) {
		
		String tableName = source.toSqlTableName();
		SQLiteDatabase database = monitorDatabase.getReadableDatabase();

		monitorDatabase.addSource(database, tableName, source);

		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(tableName);
		return builder.query(
				database,
				projection,
				selection,
				selectionArgs,
				null, null,
				sortOrder);
	}


	public List<Long> getAvailableTimestamps(OdsSource source) {
		List<Long> timestamps = new LinkedList<Long>();

		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(source.toSqlTableName());

		Cursor cursor = monitorDatabase.getReadableDatabase().rawQuery(
				"SELECT " + COLUMN_TIMESTAMP + " FROM "
				+ source.toSqlTableName() + " GROUP BY " + COLUMN_TIMESTAMP,
				null);

		if (cursor.getCount() == 0) return timestamps;
		cursor.moveToFirst();
		do {
			timestamps.add(cursor.getLong(0));
		} while (cursor.moveToNext());

		return timestamps;
	}


	private SharedPreferences getSharedPreferences() {
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}
}
