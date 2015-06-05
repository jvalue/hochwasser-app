package de.bitdroid.flooding.auth;


import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import java.io.IOException;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.utils.PreferenceUtils;
import timber.log.Timber;


/**
 * Handles login / logout to the app.
 */
public class LoginManager {

	private static final String KEY_ACCOUNT_NAME = "accountName";

	private final Context context;
	private final PreferenceUtils preferenceUtils;
	private final String scope;

	// cache token for future calls to clearToken
	private String cachedToken = null;

	@Inject
	LoginManager(
			Context context,
			PreferenceUtils preferenceUtils) {

		this.context = context;
		this.preferenceUtils = preferenceUtils;
		this.scope = "audience:server:client_id:" + context.getString(R.string.google_oauth_web_client_id);

	}


	public void setAccountName(String accountName) {
		Timber.d("setting account name to " + accountName);
		preferenceUtils.set(KEY_ACCOUNT_NAME, accountName);
	}


	public void clearAccountName() {
		preferenceUtils.clear(KEY_ACCOUNT_NAME);
	}


	public String getAccountName() {
		return preferenceUtils.get(KEY_ACCOUNT_NAME);
	}


	public String getToken(String accountName) throws IOException, UserRecoverableAuthException, GoogleAuthException {
		cachedToken = GoogleAuthUtil.getToken(context, accountName, scope, new Bundle());
		return cachedToken;
	}

	public String getToken() throws IOException, UserRecoverableAuthException, GoogleAuthException {
		return getToken(preferenceUtils.get(KEY_ACCOUNT_NAME));
	}


	public void clearToken() {
		if (cachedToken == null) return;
		try {
			GoogleAuthUtil.clearToken(context, cachedToken);
			cachedToken = null;
		} catch (Exception e) {
			// is recovery even possible when trying to clear a local cache??
			Timber.e(e, "failed to clear token");
		}
	}

}
