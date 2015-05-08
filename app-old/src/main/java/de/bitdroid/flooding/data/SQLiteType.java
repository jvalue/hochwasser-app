package de.bitdroid.flooding.data;


public enum SQLiteType {

	INTEGER("INTEGER"),
	REAL("REAL"),
	TEXT("TEXT"),
	BLOB("BLOB"),
	NULL("NULL");


	private final String stringValue;
	private SQLiteType(String stringValue) {
		this.stringValue = stringValue;
	}

	@Override
	public String toString() {
		return stringValue;
	}

}
