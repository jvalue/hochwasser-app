package de.bitdroid.flooding.ods;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import de.bitdroid.flooding.utils.Log;



public final class OdsTable extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "database.db";
	private static final int DATABASE_VERSION = 1;

	private final String DATABASE_CREATE =
		"create table " + OdsContract.BASE_PATH + " ( "
		+ OdsContract.COLUMN_ID + " integer primary key autoincrement, "
		+ OdsContract.COLUMN_SERVER_ID + " text not null, "
		+ OdsContract.COLUMN_SYNC_STATUS + " text not null, "
		+ OdsContract.COLUMN_JSON_DATA + " text not null);";


	public OdsTable(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
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
