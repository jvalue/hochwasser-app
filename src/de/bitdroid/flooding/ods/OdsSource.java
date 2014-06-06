package de.bitdroid.flooding.ods;

import java.util.Map;

import org.json.JSONObject;

import android.accounts.Account;
import android.content.ContentValues;
import android.net.Uri;

import de.bitdroid.flooding.utils.Log;
import de.bitdroid.flooding.utils.SQLiteType;


/**
 * Adapter that determines what to save of data fetched from the ODS.
 */
public abstract class OdsSource {

	public static final String
			ACCOUNT_NAME = "OpenDataService",
			ACCOUNT_TYPE = "de.bitdroid.flooding";

	public static final Account ACCOUNT = new Account(ACCOUNT_NAME, ACCOUNT_TYPE);

	public static final String AUTHORITY = "de.bitdroid.flooding.provider";

	public static final String
		COLUMN_ID = "_id",
		COLUMN_SERVER_ID = "serverId",
		COLUMN_SYNC_STATUS = "syncStatus";


	private static final String
		BASE_PATH = "ods",
		SYNC_PATH = "sync";


	private final Uri baseUri;

	protected OdsSource() {
		Uri.Builder builder = new Uri.Builder()
				.scheme("content")
				.authority(AUTHORITY)
				.appendPath(BASE_PATH);

		String[] classPaths = getClass().getName().split("\\.");
		for (String path : classPaths) {
			builder.appendPath(path);
		}

		this.baseUri = builder.build();
	}



	/**
	 * Returns the source URL NOT including domain name and port.
	 */
	public abstract String getSourceUrlPath();


	/**
	 * Describes what parts of data should be saved.
	 */
	public abstract Map<String, SQLiteType> getSchema(); 


	/**
	 * Fetch parts that should be stored.
	 */
	public abstract ContentValues saveData(JSONObject jsonObject);


	public final Uri toUri() {
		return baseUri;
	}

	public final Uri toSyncUri() {
		return baseUri 
			.buildUpon()
			.appendPath(SYNC_PATH)
			.build();
	}


	public final String toSqlTableName() {
		return getClass().getName().replaceAll("\\.", "_");
	}


	public static boolean isSyncUri(Uri uri) {
		return uri.getLastPathSegment().equals(SYNC_PATH);
	}


	public static OdsSource fromUri(Uri uri) {
		String path = uri.getPath();
		
		// remove sync path
		if (isSyncUri(uri)) {
			path = path.substring(0, path.length() - SYNC_PATH.length() - 1);
		}

		// remove base path
		path = path.substring(BASE_PATH.length() + 2);

		return fromClassName(path.replaceAll("/", "."));
	}


	@SuppressWarnings("unchecked")
	public static OdsSource fromClassName(String className) {
		try {
			Class<? extends OdsSource> sourceClass 
					= (Class<? extends OdsSource>) Class.forName(className);
			return sourceClass.newInstance();
		} catch (Exception e) {
			Log.error(android.util.Log.getStackTraceString(e));
			return null;
		}
	}

}
