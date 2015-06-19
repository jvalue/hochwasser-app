package de.bitdroid.flooding.auth;


import android.accounts.Account;
import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import org.roboguice.shaded.goole.common.base.Optional;

import java.io.IOException;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.utils.PreferenceUtils;
import timber.log.Timber;


/**
 * Handles login / logout to the app.
 */
public class LoginManager {

	private static final String
			KEY_ACCOUNT_NAME = "accountName",
			KEY_ACCOUNT_TYPE = "accountType";

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


	public void setAccount(Account account) {
		Timber.d("setting account to " + account);
		preferenceUtils.set(KEY_ACCOUNT_NAME, account.name);
		preferenceUtils.set(KEY_ACCOUNT_TYPE, account.type);
	}


	public void clearAccount() {
		preferenceUtils.clear(KEY_ACCOUNT_NAME);
		preferenceUtils.clear(KEY_ACCOUNT_TYPE);
	}


	public Optional<Account> getAccount() {
		if (!preferenceUtils.containsKey(KEY_ACCOUNT_NAME)
				|| !preferenceUtils.containsKey(KEY_ACCOUNT_TYPE)) {
			return Optional.absent();
		}
		return Optional.of(new Account(
				preferenceUtils.get(KEY_ACCOUNT_NAME),
				preferenceUtils.get(KEY_ACCOUNT_TYPE)));
	}


	public String getToken(Account account) throws IOException, UserRecoverableAuthException, GoogleAuthException {
		cachedToken = GoogleAuthUtil.getToken(context, account, scope, new Bundle());
		return cachedToken;
	}


	public Optional<String> getToken() throws IOException, UserRecoverableAuthException, GoogleAuthException {
		Optional<Account> account = getAccount();
		if (account.isPresent()) return Optional.absent();
		return Optional.of(getToken(getAccount().get()));
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
