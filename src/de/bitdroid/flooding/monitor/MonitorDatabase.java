package de.bitdroid.flooding.monitor;

import static de.bitdroid.flooding.monitor.SourceMonitor.COLUMN_ID;
import static de.bitdroid.flooding.monitor.SourceMonitor.COLUMN_MONITOR_TIMESTAMP;
import static de.bitdroid.flooding.ods.data.OdsSource.COLUMN_SERVER_ID;

import java.util.Map;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import de.bitdroid.flooding.ods.data.OdsSource;
import de.bitdroid.flooding.utils.Assert;
import de.bitdroid.flooding.utils.Log;
import de.bitdroid.flooding.utils.SQLiteType;


final class MonitorDatabase extends SQLiteOpenHelper {

	private final static String DATABASE_NAME = "monitor-database.db";
	private final static int DATABASE_VERSION = 1;

	public MonitorDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}


	void addSource(SQLiteDatabase database, String tableName, OdsSource source) {
		Assert.assertNotNull(database, tableName, source);

		StringBuilder builder = new StringBuilder(
				"create table if not exists " + tableName + " ( "
				+ COLUMN_ID + " integer primary key autoincrement, "
				+ COLUMN_SERVER_ID + " text not null, "
				+ COLUMN_MONITOR_TIMESTAMP + " integer");

		for (Map.Entry<String, SQLiteType> e : source.getSchema().entrySet()) {
			builder.append(", " + e.getKey() + " " + e.getValue().toString());
		}
		builder.append(")");

		database.execSQL(builder.toString());

	}

	@Override
	public void onCreate(SQLiteDatabase database) { }

	@Override
	public void onUpgrade(
			SQLiteDatabase database,
			int oldVersion,
			int newVersion) {

		Log.warning("Upgrading table. This will erase all data.");
		database.execSQL("DROP TABLE *");
		onCreate(database);
	}
}
