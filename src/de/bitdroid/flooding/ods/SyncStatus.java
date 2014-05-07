package de.bitdroid.flooding.ods;


public enum SyncStatus {
	SYNCED("s"),
	ERROR("e"),
	SYNC_REQUESTED("r"),
	SYNC_RUNNING("a");

	private final String status;
	private SyncStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return status;
	}
	
	public static SyncStatus fromString(String string) {
		if (string == null) return null;
		for (SyncStatus status : SyncStatus.values()) {
			if (status.toString().equals(string)) return status;
		}
		throw new IllegalArgumentException("uknown sync status " + string);
	}
}
