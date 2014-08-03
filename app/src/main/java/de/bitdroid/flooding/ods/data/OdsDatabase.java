package de.bitdroid.flooding.ods.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import de.bitdroid.flooding.utils.Assert;
import de.bitdroid.flooding.utils.Log;

import static de.bitdroid.flooding.ods.data.OdsSource.COLUMN_ID;
import static de.bitdroid.flooding.ods.data.OdsSource.COLUMN_SERVER_ID;
import static de.bitdroid.flooding.ods.data.OdsSource.COLUMN_TIMESTAMP;


final class OdsDatabase extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "database.db";
	private static final int DATABASE_VERSION = 1;


	public OdsDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

	}


	void addSource(SQLiteDatabase database, String tableName, OdsSource source) {
		Assert.assertNotNull(database, tableName, source);
		
		StringBuilder createBuilder = new StringBuilder(
			"create table if not exists " + tableName + " ( "
			+ COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_SERVER_ID + " text not null, "
			+ COLUMN_TIMESTAMP + " integer");

		for (String key : source.getSchema().keySet()) {
			createBuilder.append(", " + key + " " + source.getSchema().get(key).toString());
		}
		createBuilder.append(")");

		database.execSQL(createBuilder.toString());
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
