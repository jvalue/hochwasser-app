package de.bitdroid.flooding.ui;


import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.SignInButton;

import org.jvalue.ceps.api.UserApi;
import org.jvalue.commons.auth.OAuthUserDescription;
import org.jvalue.commons.auth.Role;
import org.jvalue.commons.auth.User;

import java.io.IOException;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.auth.LoginManager;
import de.bitdroid.flooding.network.NetworkUtils;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import timber.log.Timber;

@ContentView(R.layout.activity_login)
public class LoginActivity extends RoboActivity {

	private static final int
			REQUEST_CODE_AUTH = 42,
			REQUEST_CODE_ACCOUNT = 43;

	@InjectView(R.id.login) SignInButton loginButton;
	@Inject LoginManager loginManager;
	@Inject UserApi userApi;
	@Inject NetworkUtils networkUtils;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setup login
		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String[] accountTypes = new String[]{"com.google"};
				Intent intent = AccountPicker.newChooseAccountIntent(
						null, null, accountTypes, false, null, null, null, null);
				startActivityForResult(intent, REQUEST_CODE_ACCOUNT);
			}
		});

		// check if signed in
		if (loginManager.getAccountName() != null) {
			registerAndGetUser();
		}
	}


	private void showMainActivity() {
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) return;
		switch (requestCode) {
			case REQUEST_CODE_AUTH:
				registerAndGetUser();
				break;

			case REQUEST_CODE_ACCOUNT:
				loginManager.setAccountName(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
				registerAndGetUser();
				break;
		}
	}


	private void registerAndGetUser() {
		Observable
				.defer(new Func0<Observable<String>>() {
					@Override
					public Observable<String> call() {
						try {
							return Observable.just(loginManager.getToken());
						} catch (IOException | GoogleAuthException e) {
							return Observable.error(e);
						}
					}
				})
				.flatMap(new Func1<String, Observable<User>>() {
					@Override
					public Observable<User> call(String accessToken) {
						return userApi.addUser(new OAuthUserDescription(Role.PUBLIC, accessToken));
					}
				})
				.compose(networkUtils.<User>getDefaultTransformer())
				.subscribe(new Action1<User>() {
					@Override
					public void call(User user) {
						Timber.d("login success (" + user.getId() + ")");
						showMainActivity();
					}
				}, new Action1<Throwable>() {
					@Override
					public void call(Throwable throwable) {
						Timber.e(throwable, "login error");
						if (throwable instanceof UserRecoverableAuthException) {
							Timber.d("starting recover activity");
							startActivityForResult(((UserRecoverableAuthException) throwable).getIntent(), REQUEST_CODE_AUTH);

						} else if (throwable instanceof IOException) {
							Toast.makeText(LoginActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();

						} else {
							Toast.makeText(LoginActivity.this, "unknown error", Toast.LENGTH_SHORT).show();
						}
					}
				});
	}

}
