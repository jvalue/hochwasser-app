package de.bitdroid.flooding.ods.gcm;


public final class GcmException extends Exception {

	public static final long serialVersionUID = 42L;

	GcmException(Throwable cause) {
		super(cause);
	}

	GcmException(String message) {
		super(message);
	}
}
