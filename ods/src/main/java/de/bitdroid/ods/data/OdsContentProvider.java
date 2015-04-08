package de.bitdroid.ods.data;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.ArrayList;


public final class OdsContentProvider extends ContentProvider {


	private OdsDatabase odsDatabase;

	@Override
	public boolean onCreate() {
		odsDatabase = new OdsDatabase(getContext());
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

		OdsSource source = OdsSource.fromUri(uri);

		// create table
		String tableName = source.toSqlTableName();
		SQLiteDatabase database = odsDatabase.getWritableDatabase();
		odsDatabase.addSource(database, tableName, source);


		// query db
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(tableName);

		Cursor cursor = queryBuilder.query(
				odsDatabase.getReadableDatabase(),
				projection,
				selection,
				selectionArgs,
				null, null,
				sortOrder);

		cursor.setNotificationUri(getContext().getContentResolver(), source.toUri());
		return cursor;
	}

	@Override
	public Uri insert(Uri uri, ContentValues data) {

		OdsSource source = OdsSource.fromUri(uri);
		String tableName = source.toSqlTableName();

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

		// deletes all rows
		OdsSource source = OdsSource.fromUri(uri);
		String tableName = source.toSqlTableName();

		SQLiteDatabase database = odsDatabase.getWritableDatabase();
		odsDatabase.addSource(database, tableName, source);

		getContext().getContentResolver().notifyChange(uri, null);
		return database.delete(
				tableName,
				"1",
				null);
	}

	@Override
	public int update(
			Uri uri,
			ContentValues values,
			String  selection,
			String[] selectionArgs) {

		OdsSource source = OdsSource.fromUri(uri);
		String tableName = source.toSqlTableName();

		getContext().getContentResolver().notifyChange(uri, null);
		return odsDatabase.getWritableDatabase().update(tableName, values, selection, selectionArgs);
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
