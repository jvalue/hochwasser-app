package de.bitdroid.flooding.auth;


import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import java.io.IOException;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.utils.PreferenceManager;
import timber.log.Timber;

public final class LoginManager {

	private static final String KEY_ACCOUNT_NAME = "accountName";

	private final Context context;
	private final PreferenceManager preferenceManager;
	private final String scope;

	// cache token for future calls to clearToken
	private String cachedToken = null;

	@Inject
	LoginManager(
			Context context,
			PreferenceManager preferenceManager) {

		this.context = context;
		this.preferenceManager = preferenceManager;
		this.scope = "audience:server:client_id:" + context.getString(R.string.google_oauth_web_client_id);

	}


	public void setAccountName(String accountName) {
		Timber.d("setting account name to " + accountName);
		preferenceManager.set(KEY_ACCOUNT_NAME, accountName);
	}


	public void clearAccountName() {
		preferenceManager.clear(KEY_ACCOUNT_NAME);
	}


	public String getAccountName() {
		return preferenceManager.get(KEY_ACCOUNT_NAME);
	}


	public String getToken() throws IOException, UserRecoverableAuthException, GoogleAuthException {
		cachedToken = GoogleAuthUtil.getToken(context, preferenceManager.get(KEY_ACCOUNT_NAME), scope, new Bundle());
		return cachedToken;
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
