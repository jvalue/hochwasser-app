package de.bitdroid.flooding.monitor;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import de.bitdroid.flooding.ods.OdsSource;


public final class SourceMonitor {

	public final static String COLUMN_ID = "_id";
	public final static String COLUMN_MONITOR_TIMESTAMP = "monitorTimestamp";

	private final static String PREFS_NAME = "de.bitdroid.flooding.monitor.SourceMonitor";


	private static SourceMonitor instance;
	public static SourceMonitor getInstance(Context context) {
		if (context == null) throw new NullPointerException("param cannot be null");
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
		if (source == null) throw new NullPointerException("param cannot be null");
		if (isBeingMonitored(source)) throw new IllegalArgumentException("Already being monitored");

		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putString(source.getClass().getName(), "").commit();

		monitorDatabase.addSource(monitorDatabase.getWritableDatabase(), getTableName(source), source);
		context.startService(getServiceIntent(source, true));
	}


	public void stopMonitoring(OdsSource source) {
		if (source == null) throw new NullPointerException("param cannot be null");
		if (!isBeingMonitored(source)) throw new IllegalArgumentException("Not monitored");

		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.remove(source.getClass().getName()).commit();

		context.startService(getServiceIntent(source, false));
	}


	public boolean isBeingMonitored(OdsSource source) {
		if (source == null) throw new NullPointerException("param cannot be null");
		return getSharedPreferences().contains(source.getClass().getName());
	}


	public Set<OdsSource> getMonitoredSources() {
		Set<OdsSource> ret = new HashSet<OdsSource>();

		SharedPreferences prefs = getSharedPreferences();
		for (String className : prefs.getAll().keySet()) {
			ret.add(OdsSource.fromClassName(className));
		}

		return ret;
	}


	public Cursor query(
			OdsSource source,
			String[] projection,
			String selection,
			String[] selectionArgs,
			String sortOrder) {
		
		String tableName = getTableName(source);
		SQLiteDatabase database = monitorDatabase.getWritableDatabase();

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


	private String getTableName(OdsSource source) {
		return source.toSqlTableName() + "_MONITOR";
	}


	private Intent getServiceIntent(OdsSource source, boolean startMonitoring) {
		return new Intent(context, MonitorService.class);
	}


	private SharedPreferences getSharedPreferences() {
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}
}
