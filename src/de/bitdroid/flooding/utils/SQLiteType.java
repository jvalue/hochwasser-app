package de.bitdroid.flooding.utils;


public enum SQLiteType {

	INTEGER("INTEGER"),
	REAL("REAL"),
	TEXT("TEXT");

	// TODO include reference keys to other tables?


	private final String stringValue;
	private SQLiteType(String stringValue) {
		this.stringValue = stringValue;
	}

	@Override
	public String toString() {
		return stringValue;
	}

}
