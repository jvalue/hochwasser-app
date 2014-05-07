package de.bitdroid.flooding.ods;

import android.database.sqlite.SQLiteOpenHelper;


interface Table {
	public SQLiteOpenHelper getSQLiteOpenHelper();
	public String getTableName();
	public String getIdColumn();
	public String[] getAllColumns();
}
