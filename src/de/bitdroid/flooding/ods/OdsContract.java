package de.bitdroid.flooding.ods;

import android.accounts.Account;
import android.net.Uri;


public interface OdsContract {

	public static final String 
		ACCOUNT_NAME = "OpenDataService",
		ACCOUNT_TYPE = "de.bitdroid.flooding";

	public static final Account ACCOUNT = new Account(ACCOUNT_NAME, ACCOUNT_TYPE);



	public static final String AUTHORITY = "de.bitdroid.flooding.provider";

	public static final String 
		BASE_PATH = "ods",
		SYNC_PATH = "sync";

	public static final Uri BASE_CONTENT_URI
		= new Uri.Builder().scheme("content").authority(AUTHORITY).appendPath("ods").build();



	public static final String 
		COLUMN_ID = "_id",
		COLUMN_SERVER_ID = "serverId",
		COLUMN_SYNC_STATUS = "syncStatus",
		COLUMN_JSON_DATA = "jsonData";

	public static final String[] COLUMN_NAMES = new String[] 
	{ 
		COLUMN_ID, COLUMN_SERVER_ID, COLUMN_SYNC_STATUS, COLUMN_JSON_DATA
	};
}
