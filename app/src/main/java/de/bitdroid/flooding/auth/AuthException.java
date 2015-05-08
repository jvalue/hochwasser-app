package de.bitdroid.flooding.auth;

/**
 * Indicates that there was a problem authenticating the current user
 */
public class AuthException extends Exception {

	public AuthException(String message) {
		super(message);
	}


	public AuthException(String message, Throwable cause) {
		super(message, cause);
	}

}
