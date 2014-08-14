package de.bitdroid.ods.cep;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.Set;

import de.bitdroid.utils.Assert;
import de.bitdroid.utils.Log;

import static de.bitdroid.ods.cep.RuleDbSchema.COLUMN_ID;
import static de.bitdroid.ods.cep.RuleDbSchema.COLUMN_JSON;
import static de.bitdroid.ods.cep.RuleDbSchema.TABLE_NAME;


final class RuleDb extends SQLiteOpenHelper {

	private final static ObjectMapper mapper = new ObjectMapper();

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


	public void insert(Rule rule) {
		Assert.assertNotNull(rule);
		SQLiteDatabase database = null;
		try {
			database = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(COLUMN_JSON, ((Object) mapper.valueToTree(rule)).toString());
			database.insert(TABLE_NAME, null, values);
		} finally {
			if (database != null) database.close();
		}
	}


	public void delete(Rule rule) {
		Assert.assertNotNull(rule);
		SQLiteDatabase database = null;
		try {
			database = getWritableDatabase();
			database.delete(
					TABLE_NAME,
					COLUMN_JSON + "=?",
					new String[]{((Object) mapper.valueToTree(rule)).toString()});
		} finally {
			if (database != null) database.close();
		}
	}


	public Set<Rule> getAll() {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(TABLE_NAME);
		String[] columns = { COLUMN_JSON };
		Cursor cursor = builder.query(
				getReadableDatabase(),
				columns,
				null, null, null, null, null);

		Set<Rule> rules = new HashSet<Rule>();

		if (cursor.getCount() <= 0) return rules;
		cursor.moveToFirst();
		do {
			String json = cursor.getString(0);
			try {
				Rule rule = mapper.treeToValue(mapper.readTree(json), Rule.class);;
				rules.add(rule);
			} catch (Exception e) {
				Log.error("failed to read rule from db", e);
			}
		} while (cursor.moveToNext());

		return rules;
	}

}
