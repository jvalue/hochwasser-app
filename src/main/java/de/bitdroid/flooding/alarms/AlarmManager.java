package de.bitdroid.flooding.alarms;

import static de.bitdroid.flooding.alarms.AlarmDbSchema.COLUMN_ID;
import static de.bitdroid.flooding.alarms.AlarmDbSchema.COLUMN_JSON;
import static de.bitdroid.flooding.alarms.AlarmDbSchema.TABLE_NAME;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.bitdroid.flooding.ods.cep.CepManager;
import de.bitdroid.flooding.ods.gcm.GcmStatus;
import de.bitdroid.flooding.utils.Assert;
import de.bitdroid.flooding.utils.Log;


final class AlarmManager {

	private static final ObjectMapper mapper = new ObjectMapper();

	private static AlarmManager instance;

	public static AlarmManager getInstance(Context context) {
		Assert.assertNotNull(context);
		if (instance == null) instance = new AlarmManager(context);
		return instance;
	}


	private final CepManager cepManager;
	private final AlarmDb alarmDb;
	private final List<AlarmUpdateListener> listeners = new LinkedList<AlarmUpdateListener>();
	private final EplStmtCreator stmtCreator = new EplStmtCreator();

	private AlarmManager(Context context) {
		this.cepManager = CepManager.getInstance(context);
		this.alarmDb = new AlarmDb(context);
	}


	public synchronized Set<Alarm> getAll() {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(TABLE_NAME);
		String[] columns = { COLUMN_JSON };
		Cursor cursor = builder.query(
				alarmDb.getReadableDatabase(),
				columns,
				null, null, null, null, null);

		Set<Alarm> alarms = new HashSet<Alarm>();

		if (cursor.getCount() <= 0) return alarms;
		cursor.moveToFirst();
		do {
			String json = cursor.getString(0);

			try {
				Alarm alarm = mapper.treeToValue(mapper.readTree(json), Alarm.class);
				alarms.add(alarm);
			} catch (Exception e) {
				Log.error("failed to read alarm from db", e);
			}

		} while (cursor.moveToNext());

		return alarms;
	}


	public synchronized void register(Alarm alarm) {
		Assert.assertNotNull(alarm);

		String eplStmt = alarm.accept(stmtCreator, null);
		GcmStatus status = cepManager.getRegistrationStatus(eplStmt);

		// register on server
		if (status.equals(GcmStatus.UNREGISTERED)) {
			cepManager.registerEplStmt(alarm.accept(stmtCreator, null));
		}

		// store in db
		if (isRegistered(alarm)) return;
		SQLiteDatabase database = null;
		try {
			database = alarmDb.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(COLUMN_JSON, mapper.valueToTree(alarm).toString());
			database.insert(TABLE_NAME, null, values);
			alertListenersOnNewAlarm(alarm);
		} finally {
			if (database != null) database.close();
		}
	}


	public synchronized void unregister(Alarm alarm) {
		Assert.assertNotNull(alarm);


		// unregister from server
		String stmt = alarm.accept(stmtCreator, null);
		if (cepManager.getRegistrationStatus(stmt).equals(GcmStatus.REGISTERED)) {
			cepManager.unregisterEplStmt(stmt);
		}

		// remove from db
		SQLiteDatabase database = null;
		try {
			database = alarmDb.getWritableDatabase();
			database.delete(
					TABLE_NAME, 
					COLUMN_JSON + "=?", 
					new String[]{ mapper.valueToTree(alarm).toString() });

			alertListenersOnDeletedAlarm(alarm);

		} finally {
			if (database != null) database.close();
		}
	}


	public synchronized boolean isRegistered(Alarm alarm) {
		Assert.assertNotNull(alarm);

		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(TABLE_NAME);
		String[] columns = { COLUMN_ID };
		Cursor cursor = builder.query(
				alarmDb.getReadableDatabase(),
				columns,
				COLUMN_JSON + "=?",
				new String[]{ mapper.valueToTree(alarm).toString() },
				null, null, null);

		if (cursor.getCount() <= 0) return false;
		return true;
	}


	public synchronized GcmStatus getRegistrationStatus(Alarm alarm) {
		Assert.assertNotNull(alarm);

		return cepManager.getRegistrationStatus(alarm.accept(stmtCreator, null));
	}



	public synchronized void registerListener(AlarmUpdateListener listener) {
		Assert.assertNotNull(listener);
		Assert.assertTrue(!listeners.contains(listener), "already registered");
		listeners.add(listener);
	}


	public synchronized void unregisterListener(AlarmUpdateListener listener) {
		Assert.assertNotNull(listener);
		Assert.assertTrue(listeners.contains(listener), "not registered");
		listeners.remove(listener);
	}


	private void alertListenersOnNewAlarm(Alarm alarm) {
		for (AlarmUpdateListener listener : listeners) {
			listener.onNewAlarm(alarm);
		}
	}


	private void alertListenersOnDeletedAlarm(Alarm alarm) {
		for (AlarmUpdateListener listener : listeners) {
			listener.onDeletedAlarm(alarm);
		}
	}

}
