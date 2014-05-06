package de.bitdroid.flooding.rest;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;


public final class RestContentProvider extends ContentProvider {

	private static final String AUTHORITY = "de.bitdroid.flooding.provider";
	private static final String BASE_PATH = "ods";
	public static final Uri CONTENT_URI 
		= new Uri.Builder().scheme("content").authority(AUTHORITY).path(BASE_PATH).build();

	private static final int
		URI_MATCHER_ALL = 10,
		URI_MATCHER_ID = 20;
	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	{
		URI_MATCHER.addURI(AUTHORITY, BASE_PATH, URI_MATCHER_ALL);
		URI_MATCHER.addURI(AUTHORITY, BASE_PATH + "/#", URI_MATCHER_ID);
	}

	private ODSTable odsTable;


	@Override
	public boolean onCreate() {
		odsTable = new ODSTable(getContext());
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
		queryBuilder.setTables(odsTable.getTableName());
		
		switch (URI_MATCHER.match(uri)) {
			case URI_MATCHER_ALL:
				break;
			case URI_MATCHER_ID:
				queryBuilder.appendWhere(odsTable.getIdColumn() + "=" + uri.getLastPathSegment());
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		return queryBuilder.query(
				odsTable.getWritableDatabase(),
				projection,
				selection,
				selectionArgs,
				null, null,
				sortOrder);
	}

	@Override
	public Uri insert(Uri uri, ContentValues data) {
		switch(URI_MATCHER.match(uri)) {
			case URI_MATCHER_ALL:
				long id = odsTable.getWritableDatabase().insert(
						odsTable.getTableName(),
						null,
						data);
				
				getContext().getContentResolver().notifyChange(uri, null);
				return CONTENT_URI.buildUpon().path(String.valueOf(id)).build();

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
}
