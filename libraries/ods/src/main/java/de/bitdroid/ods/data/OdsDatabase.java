package de.bitdroid.ods.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import de.bitdroid.utils.Assert;
import timber.log.Timber;

import static de.bitdroid.ods.data.OdsSource.COLUMN_ID;


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
			+ COLUMN_ID + " integer primary key autoincrement");

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

		Timber.w("Upgrading ods db, this will erase all data!");
		database.execSQL("DROP TABLE *");
		onCreate(database);
	}
}
