package de.bitdroid.flooding.auth;

/**
 * A resource which is protected by resources and supports logging out.
 */
public interface RestrictedResource {

	void logout();

}
