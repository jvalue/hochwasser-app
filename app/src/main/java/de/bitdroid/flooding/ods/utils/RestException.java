package de.bitdroid.flooding.ods.utils;

import java.io.IOException;


public final class RestException extends Exception {
	public static final long serialVersionUID = 42L;

	public static final int UNSET  = -1;
	private final int code;
    private final String message;

	public RestException(int code, String message) {
		this.code = code;
        this.message = message;
	}

	public RestException(IOException nestedException) {
		super(nestedException);
		this.code = RestException.UNSET;
        this.message = null;
	}

	public int getCode() {
		return code;
	}

	@Override
	public String getMessage() {
        Throwable cause = getCause();
        if (cause != null) return cause.getMessage();
        else if (message != null) return message + " (" + code + ")";
        else return "return error code " + code;
    }

}
