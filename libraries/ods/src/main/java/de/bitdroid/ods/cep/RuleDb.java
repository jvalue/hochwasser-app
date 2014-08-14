package de.bitdroid.ods.cep;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import de.bitdroid.utils.Log;

import static de.bitdroid.ods.cep.RuleDbSchema.COLUMN_ID;
import static de.bitdroid.ods.cep.RuleDbSchema.COLUMN_JSON;
import static de.bitdroid.ods.cep.RuleDbSchema.TABLE_NAME;


final class RuleDb extends SQLiteOpenHelper {

	private final static String DATABASE_NAME = "rules-database.db";
	private final static int DATABASE_VERSION = 1;


	public RuleDb(Context context) {
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
