package de.bitdroid.flooding.ods;

import static de.bitdroid.flooding.ods.OdsContract.ACCOUNT;
import static de.bitdroid.flooding.ods.OdsContract.AUTHORITY;
import static de.bitdroid.flooding.ods.OdsContract.BASE_CONTENT_URI;
import static de.bitdroid.flooding.ods.OdsContract.BASE_PATH;
import static de.bitdroid.flooding.ods.OdsContract.SYNC_PATH;

import java.util.ArrayList;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;


public final class OdsContentProvider extends ContentProvider {

	private static final int
		URI_MATCHER_ALL = 10,
		URI_MATCHER_ALL_SYNC = 20,
		URI_MATCHER_SERVER_ID = 30;
	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		URI_MATCHER.addURI(
				AUTHORITY, 
				BASE_PATH, 
				URI_MATCHER_ALL);
		URI_MATCHER.addURI(
				AUTHORITY, 
				BASE_PATH + "/" + SYNC_PATH, 
				URI_MATCHER_ALL_SYNC);
		URI_MATCHER.addURI(
				AUTHORITY, 
				BASE_PATH + "/*", 
				URI_MATCHER_SERVER_ID);
	}

	private OdsTable odsTable;


	@Override
	public boolean onCreate() {
		odsTable = new OdsTable(getContext());
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


		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		// todo check columns
		queryBuilder.setTables(BASE_PATH);
		
		switch (URI_MATCHER.match(uri)) {
			case URI_MATCHER_ALL_SYNC:
				Bundle settingsBundle = new Bundle();
				settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
				settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
				ContentResolver.requestSync(
						ACCOUNT,
						AUTHORITY,
						settingsBundle);

			case URI_MATCHER_ALL:
				break;
			case URI_MATCHER_SERVER_ID:
				queryBuilder.appendWhere(
						OdsContract.COLUMN_ID + "=\"" + uri.getLastPathSegment() + "\"");
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		Cursor cursor = queryBuilder.query(
				odsTable.getReadableDatabase(),
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
		switch(URI_MATCHER.match(uri)) {
			case URI_MATCHER_ALL:
				long id = odsTable.getWritableDatabase().insert(
						BASE_PATH,
						null,
						data);

				getContext().getContentResolver().notifyChange(uri, null);
				return BASE_CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();

			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public int update(
			Uri uri,
			ContentValues values,
			String  selection,
			String[] selectionArgs) {
		return 0;
	}


	@Override
	public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
			throws OperationApplicationException {

		SQLiteDatabase db = odsTable.getWritableDatabase();
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
