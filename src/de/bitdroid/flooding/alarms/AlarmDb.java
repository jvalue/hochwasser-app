package de.bitdroid.flooding.alarms;

import static de.bitdroid.flooding.alarms.AlarmDbSchema.COLUMN_ID;
import static de.bitdroid.flooding.alarms.AlarmDbSchema.COLUMN_JSON;
import static de.bitdroid.flooding.alarms.AlarmDbSchema.TABLE_NAME;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import de.bitdroid.flooding.utils.Log;


final class AlarmDb extends SQLiteOpenHelper {

	private final static String DATABASE_NAME = "alarms-database.db";
	private final static int DATABASE_VERSION = 1;


	public AlarmDb(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}


	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL("create table if not exists " + TABLE_NAME + " ( "
				+ COLUMN_ID + " integer primary key autoincrement, "
				+ COLUMN_JSON + " text not null)");
	}


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
