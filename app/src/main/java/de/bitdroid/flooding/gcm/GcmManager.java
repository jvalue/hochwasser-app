package de.bitdroid.flooding.gcm;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import retrofit.client.Response;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

public class GcmManager {

	private static final String PREFS_NAME = GcmManager.class.getName();
	private static final String
			KEY_GCM_ID = "GCM_ID",
			KEY_APP_VERSION = "APP_VERSION";

	private final Context context;
	private final GoogleCloudMessaging googleCloudMessaging;

	@Inject
	GcmManager(Context context) {
		this.context = context;
		this.googleCloudMessaging = GoogleCloudMessaging.getInstance(context);
	}


	public boolean isRegistered() {
		return getRegId() != null;
	}


	public Observable<Void> register() {
		return Observable
				.defer(new Func0<Observable<String>>() {
					@Override
					public Observable<String> call() {
						String regId = getRegId();
						if (regId != null) {
							return Observable.just(regId);
						} else {
							try {
								regId = googleCloudMessaging.register(context.getString(R.string.google_project_id));
								return Observable.just(regId);
							} catch (IOException ioe) {
								return Observable.error(ioe);
							}
						}
					}
				})
				.retry(3)
				.flatMap(new Func1<String, Observable<Void>>() {
					@Override
					public Observable<Void> call(String gcmId) {
						storeRegId(gcmId);
						return Observable.just(null);
					}
				});
	}


	public Observable<Void> unregister() {
		return Observable.defer(new Func0<Observable<Response>>() {
			@Override
			public Observable<Response> call() {
				try {
					clearRegId();
					googleCloudMessaging.unregister();
					return Observable.just(null);
				} catch (IOException ioe) {
					return Observable.error(ioe);
				}
			}
		}).flatMap(new Func1<Response, Observable<Void>>() {
			@Override
			public Observable<Void> call(Response response) {
				return Observable.just(null);
			}
		});
	}


	/**
	 * @return the last stored reg id or null if none was stored or the app version has changed since.
	 */
	public String getRegId() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		int storedAppVersion = prefs.getInt(KEY_APP_VERSION, -1);
		if (storedAppVersion == -1 || storedAppVersion != getAppVersion()) return null;
		return prefs.getString(KEY_GCM_ID, null);
	}


	private void storeRegId(String regId) {
		SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
		editor.putString(KEY_GCM_ID, regId);
		editor.putInt(KEY_APP_VERSION, getAppVersion());
		editor.commit();
	}


	private void clearRegId() {
		SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
	}


	private int getAppVersion() {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			// should (tm) never happen
			throw new RuntimeException(e);
		}
	}

}
