package de.bitdroid.flooding.monitor;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import de.bitdroid.flooding.ods.OdsSource;


public final class SourceMonitor {

	public final static String COLUMN_ID = "_id";
	public final static String COLUMN_MONITOR_TIMESTAMP = "monitorTimestamp";


	private static SourceMonitor instance;
	public static SourceMonitor getInstance(Context context) {
		if (context == null) throw new NullPointerException("param cannot be null");
		if (instance == null) instance = new SourceMonitor(context);
		return instance;
	}


	private final Map<String, OdsSource> monitoredSource 
		= new HashMap<String, OdsSource>(); // save in prefs...
	private final MonitorDatabase monitorDatabase;
	private final Context context;

	private SourceMonitor(Context context) {
		this.monitorDatabase = new MonitorDatabase(context);
		this.context = context;
	}


	public void startMonitoring(OdsSource source)  {
		if (source == null) throw new NullPointerException("param cannot be null");
		if (isBeingMonitored(source)) throw new IllegalArgumentException("Already being monitored");

		monitoredSource.put(source.getClass().getName(), source);
		monitorDatabase.addSource(monitorDatabase.getWritableDatabase(), getTableName(source), source);
		context.startService(getServiceIntent(source, true));
	}


	public void stopMonitoring(OdsSource source) {
		if (source == null) throw new NullPointerException("param cannot be null");
		if (!isBeingMonitored(source)) throw new IllegalArgumentException("Not monitored");

		monitoredSource.remove(source.getClass().getName());
		context.startService(getServiceIntent(source, false));
	}


	public boolean isBeingMonitored(OdsSource source) {
		if (source == null) throw new NullPointerException("param cannot be null");
		return monitoredSource.containsKey(source.getClass().getName());
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
		Intent intent = new Intent(context, MonitorService.class);
		intent.putExtra(MonitorService.EXTRA_SOURCE_NAME, source.getClass().getName());
		intent.putExtra(MonitorService.EXTRA_REGISTER_SOURCE, startMonitoring);
		return intent;
	}
}
