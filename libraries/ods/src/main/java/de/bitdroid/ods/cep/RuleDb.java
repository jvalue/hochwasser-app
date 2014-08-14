package de.bitdroid.ods.cep;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

import de.bitdroid.utils.Assert;
import de.bitdroid.utils.Log;

import static de.bitdroid.ods.cep.RuleDbSchema.COLUMN_CEPS_RULE_PATH;
import static de.bitdroid.ods.cep.RuleDbSchema.COLUMN_ID;
import static de.bitdroid.ods.cep.RuleDbSchema.COLUMN_PARAMS;
import static de.bitdroid.ods.cep.RuleDbSchema.COLUMN_UUID;
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
				+ COLUMN_UUID + " text not null, "
				+ COLUMN_CEPS_RULE_PATH + " text not null, "
				+ COLUMN_PARAMS + " text not null)");
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
			values.put(COLUMN_UUID, rule.getUuid());
			values.put(COLUMN_CEPS_RULE_PATH, rule.getCepsRulePath());
			values.put(COLUMN_PARAMS, new JSONObject(rule.getParams()).toString());
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
					COLUMN_UUID + "=?",
					new String[]{ rule.getUuid() });
		} finally {
			if (database != null) database.close();
		}
	}


	public Set<Rule> getAll() {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(TABLE_NAME);
		String[] columns = { COLUMN_UUID, COLUMN_CEPS_RULE_PATH, COLUMN_PARAMS };
		Cursor cursor = builder.query(
				getReadableDatabase(),
				columns,
				null, null, null, null, null);

		Set<Rule> rules = new HashSet<Rule>();

		if (cursor.getCount() <= 0) return rules;
		cursor.moveToFirst();
		do {
			Rule.Builder ruleBuilder = new Rule.Builder(cursor.getString(1))
					.uuid(cursor.getString(0));

			try {
				JSONObject jsonParams = new JSONObject(cursor.getString(2));
				JSONArray jsonParamKeys = jsonParams.names();
				for (int i = 0; i < jsonParamKeys.length(); i++) {
					String key = jsonParamKeys.getString(i);
					ruleBuilder.parameter(key, jsonParams.opt(key).toString());
				}
			} catch (Exception e) {
				Log.error("failed to read rule", e);
			}

			rules.add(ruleBuilder.build());
		} while (cursor.moveToNext());

		return rules;
	}

}
