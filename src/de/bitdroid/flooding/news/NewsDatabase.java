package de.bitdroid.flooding.news;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import de.bitdroid.flooding.utils.Log;


final class NewsDatabase extends SQLiteOpenHelper {

	private final static String DATABASE_NAME = "news-database.db";
	private final static int DATABASE_VERSION = 1;

	private final static String
		TABLE_NAME = "news",
		COLUMN_ID = "_id",
		COLUMN_TITLE = "title",
		COLUMN_CONTENT = "content",
		COLUMN_TIMESTAMP = "timestamp";

	public NewsDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}


	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL("create table " + TABLE_NAME + " ( "
				+ COLUMN_ID + " integer primary key autoincrement, "
				+ COLUMN_TITLE + " text not null, "
				+ COLUMN_CONTENT + " text not null, "
				+ COLUMN_TIMESTAMP + " integer)");
	}


	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.warning("Upgrading table. This will erase all data.");
		database.execSQL("drop table *");
		onCreate(database);
	}

}
