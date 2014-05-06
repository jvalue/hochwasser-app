package de.bitdroid.flooding.rest;

import android.database.sqlite.SQLiteOpenHelper;


interface Table {
	public SQLiteOpenHelper getSQLiteOpenHelper();
	public String getTableName();
	public String getIdColumn();
	public String[] getAllColumns();
}
