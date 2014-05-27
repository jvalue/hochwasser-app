package de.bitdroid.flooding.ods;

import java.util.ArrayList;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;

import de.bitdroid.flooding.utils.Log;


public final class OdsContentProvider extends ContentProvider {


	private OdsTable odsDatabase;

	@Override
	public boolean onCreate() {
		odsDatabase = new OdsTable(getContext());
		return true;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Cursor query(
			Uri uri,
			String[] projection,
			String  selection,
			String[] selectionArgs,
			String sortOrder) {

		OdsTableAdapter source = OdsTableAdapter.fromUri(uri);


		// check if source is being monitored
		if (!OdsSourceManager.getInstance().isSourceRegistered(getContext(), source))
			return null;

		// check if values have been inserted
		String tableName = source.toSqlTableName();
		Cursor tableCheckCursor = odsDatabase.getReadableDatabase() .rawQuery(
					"SELECT name FROM sqlite_master WHERE type=? AND name=?",
					new String[] { "table", tableName });
		if (tableCheckCursor.getCount() < 1) return null;


		// check for manual sync reqeust
		if (OdsTableAdapter.isSyncUri(uri)) {
			Bundle settingsBundle = new Bundle();
			settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
			settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
			ContentResolver.requestSync(
					OdsTableAdapter.ACCOUNT,
					OdsTableAdapter.AUTHORITY,
					settingsBundle);
		}

		// query db
		Log.debug("Querying");
		Log.debug("tableName = " + tableName);

		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(tableName);

		Cursor cursor = queryBuilder.query(
				odsDatabase.getReadableDatabase(),
				projection,
				selection,
				selectionArgs,
				null, null,
				sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public Uri insert(Uri uri, ContentValues data) {

		OdsTableAdapter source = OdsTableAdapter.fromUri(uri);
		String tableName = source.toSqlTableName();

		Log.debug("Fetching " + tableName);

		SQLiteDatabase database = odsDatabase.getWritableDatabase();
		odsDatabase.addSource(database, tableName, source);

		long id = database.insert(
				tableName,
				null,
				data);

		getContext().getContentResolver().notifyChange(uri, null);
		return uri.buildUpon().appendPath(String.valueOf(id)).build();
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException("under construction");
	}

	@Override
	public int update(
			Uri uri,
			ContentValues values,
			String  selection,
			String[] selectionArgs) {
		throw new UnsupportedOperationException("under construction");
	}


	@Override
	public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
			throws OperationApplicationException {

		SQLiteDatabase db = odsDatabase.getWritableDatabase();
		db.beginTransaction();

		try {
			ContentProviderResult[] result = super.applyBatch(operations);
			db.setTransactionSuccessful();
			return result;
		} finally {
			db.endTransaction();
		}
	}
}
