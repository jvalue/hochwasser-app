package de.bitdroid.flooding.monitor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Map;

import de.bitdroid.flooding.data.OdsSource;
import de.bitdroid.flooding.data.SQLiteType;
import de.bitdroid.flooding.utils.Assert;
import timber.log.Timber;

import static de.bitdroid.flooding.monitor.SourceMonitor.COLUMN_ID;


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
				+ COLUMN_ID + " integer primary key autoincrement");

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

		Timber.w("Upgrading monitor table, this will erase all data!");
		database.execSQL("DROP TABLE *");
		onCreate(database);
	}
}
