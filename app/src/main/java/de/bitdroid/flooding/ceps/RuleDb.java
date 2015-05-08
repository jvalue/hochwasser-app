package de.bitdroid.flooding.ceps;

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

import de.bitdroid.flooding.gcm.GcmStatus;
import de.bitdroid.utils.Assert;
import timber.log.Timber;


final class RuleDb extends SQLiteOpenHelper {

	private final static String DATABASE_NAME = "rules-database.db";
	private final static int DATABASE_VERSION = 1;


	public RuleDb(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}


	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL("create table if not exists " + RuleDbSchema.TABLE_NAME + " ( "
				+ RuleDbSchema.COLUMN_ID + " integer primary key autoincrement, "
				+ RuleDbSchema.COLUMN_UUID + " text not null, "
				+ RuleDbSchema.COLUMN_CEPS_RULE_PATH + " text not null, "
				+ RuleDbSchema.COLUMN_PARAMS + " text not null, "
				+ RuleDbSchema.COLUMN_REGISTRATION_STATUS + " text, "
				+ RuleDbSchema.COLUMN_CEPS_CLIENT_ID + " text)");
	}


	@Override
	public void onUpgrade(
			SQLiteDatabase database,
			int oldVersion,
			int newVersion) {

		Timber.w("Upgrading rule table, this will erase all data!");
		database.execSQL("DROP TABLE *");
		onCreate(database);
	}


	public void insert(Rule rule) {
		Assert.assertNotNull(rule);
		SQLiteDatabase database = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(RuleDbSchema.COLUMN_UUID, rule.getUuid());
		values.put(RuleDbSchema.COLUMN_CEPS_RULE_PATH, rule.getCepsRulePath());
		values.put(RuleDbSchema.COLUMN_PARAMS, new JSONObject(rule.getParams()).toString());
		database.insert(RuleDbSchema.TABLE_NAME, null, values);
	}


	public void delete(Rule rule) {
		Assert.assertNotNull(rule);
		SQLiteDatabase database = getWritableDatabase();
		database.delete(
				RuleDbSchema.TABLE_NAME,
				RuleDbSchema.COLUMN_UUID + "=?",
				new String[]{ rule.getUuid() });
	}


	public Set<Rule> getAll() {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(RuleDbSchema.TABLE_NAME);
		String[] columns = { RuleDbSchema.COLUMN_UUID, RuleDbSchema.COLUMN_CEPS_RULE_PATH, RuleDbSchema.COLUMN_PARAMS };
		Cursor cursor = builder.query(
				getReadableDatabase(),
				columns,
				null, null, null, null, null);

		Set<Rule> rules = new HashSet<Rule>();

		if (cursor.getCount() <= 0) return rules;
		cursor.moveToFirst();
		do {
			rules.add(cursorToRule(cursor));
		} while (cursor.moveToNext());

		return rules;
	}


	public Rule getRuleForClientId(String clientId) {
		Assert.assertNotNull(clientId);
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(RuleDbSchema.TABLE_NAME);
		String[] columns = { RuleDbSchema.COLUMN_UUID, RuleDbSchema.COLUMN_CEPS_RULE_PATH, RuleDbSchema.COLUMN_PARAMS };
		Cursor cursor = builder.query(
				getReadableDatabase(),
				columns,
				RuleDbSchema.COLUMN_CEPS_CLIENT_ID + "=?",
				new String[] { clientId },
				null, null, null);

		if (cursor.getCount() <= 0) return null;
		cursor.moveToFirst();
		return cursorToRule(cursor);
	}


	private Rule cursorToRule(Cursor cursor) {
		int uuidIdx = cursor.getColumnIndex(RuleDbSchema.COLUMN_UUID);
		int pathIdx = cursor.getColumnIndex(RuleDbSchema.COLUMN_CEPS_RULE_PATH);
		int paramsIdx = cursor.getColumnIndex(RuleDbSchema.COLUMN_PARAMS);

		Rule.Builder ruleBuilder = new Rule.Builder(cursor.getString(pathIdx))
				.uuid(cursor.getString(uuidIdx));

		try {
			JSONObject jsonParams = new JSONObject(cursor.getString(paramsIdx));
			JSONArray jsonParamKeys = jsonParams.names();
			for (int i = 0; i < jsonParamKeys.length(); i++) {
				String key = jsonParamKeys.getString(i);
				ruleBuilder.parameter(key, jsonParams.opt(key).toString());
			}
		} catch (Exception e) {
			Timber.e(e, "failed to read rule");
		}

		return ruleBuilder.build();
	}


	public String getClientIdForRule(Rule rule) {
		Assert.assertNotNull(rule);
		return getColumnForRule(rule, RuleDbSchema.COLUMN_CEPS_CLIENT_ID);
	}


	public GcmStatus getStatusForRule(Rule rule) {
		Assert.assertNotNull(rule);
		String status = getColumnForRule(rule, RuleDbSchema.COLUMN_REGISTRATION_STATUS);
		if (status == null) return null;
		else return GcmStatus.valueOf(status);
	}


	private String getColumnForRule(Rule rule, String columnName) {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(RuleDbSchema.TABLE_NAME);
		String[] columns = { columnName };
		Cursor cursor = builder.query(
				getReadableDatabase(),
				columns,
				RuleDbSchema.COLUMN_UUID + "=?",
				new String[] { rule.getUuid() },
				null, null, null);

		if (cursor.getCount() <= 0) return null;
		cursor.moveToFirst();
		do {
			return cursor.getString(0);
		} while (cursor.moveToNext());
	}


	public void updateCepsData(Rule rule, String clientId, GcmStatus registrationStatus) {
		ContentValues values = new ContentValues();
		values.put(RuleDbSchema.COLUMN_CEPS_CLIENT_ID, clientId);
		values.put(RuleDbSchema.COLUMN_REGISTRATION_STATUS, registrationStatus.name());

		SQLiteDatabase database = getWritableDatabase();
		database.update(RuleDbSchema.TABLE_NAME, values, RuleDbSchema.COLUMN_UUID + "=?", new String[] { rule.getUuid() });
	}

}
