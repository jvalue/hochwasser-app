package de.bitdroid.flooding.rest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import de.bitdroid.flooding.utils.Log;



public final class ODSTable extends SQLiteOpenHelper implements Table {

	private static final String DATABASE_NAME = "database.db";
	private static final int DATABASE_VERSION = 1;

	public static final String 
				TABLE_NAME = "poItems",
				COLUMN_ID = "_id",
				COLUMN_SERVER_ID = "serverId",
				COLUMN_HTTP_STATUS = "httpStatus",
				COLUMN_SYNC_STATUS = "syncStatus",
				COLUMN_JSON_DATA = "jsonData";

	public static final String[] COLUMN_NAMES = 
	{ 
		COLUMN_ID, COLUMN_SERVER_ID, COLUMN_HTTP_STATUS, COLUMN_SYNC_STATUS, COLUMN_JSON_DATA
	};

	private final String DATABASE_CREATE =
		"create table " + TABLE_NAME + " ( "
		+ COLUMN_ID + " integer primary key autoincrement, "
		+ COLUMN_SERVER_ID + " text not null, "
		+ COLUMN_HTTP_STATUS + " text not null, "
		+ COLUMN_SYNC_STATUS + " text not null, "
		+ COLUMN_JSON_DATA + " text not null);";


	public ODSTable(Context context) {
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


	@Override
	public SQLiteOpenHelper getSQLiteOpenHelper() {
		return this;
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
	}

	@Override
	public String getIdColumn() {
		return COLUMN_ID;
	}

	@Override
	public String[] getAllColumns() {
		return COLUMN_NAMES;
	}
}
