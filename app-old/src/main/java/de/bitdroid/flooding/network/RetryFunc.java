package de.bitdroid.flooding.network;

import com.google.android.gms.auth.GoogleAuthException;

import java.util.concurrent.TimeUnit;

import de.bitdroid.flooding.auth.LoginManager;
import retrofit.RetrofitError;
import rx.Observable;
import rx.functions.Func1;
import timber.log.Timber;


public final class RetryFunc implements Func1<Observable<? extends Throwable>, Observable<?>> {

	private static final int MAX_RETRY = 1;
	private static final long DELAY = 200; // ms

	private final LoginManager loginManager;

	private int retryCount = 0;
	private int currentDelay = 0;

	RetryFunc(LoginManager loginManager) {
		this.loginManager = loginManager;
	}


	@Override
	public Observable<?> call(Observable<? extends Throwable> observable) {
		return observable.flatMap(new Func1<Throwable, Observable<?>>() {
			@Override
			public Observable<?> call(Throwable throwable) {
				// check for max retries
				++retryCount;
				if (retryCount > MAX_RETRY) return Observable.error(throwable);

				if (throwable instanceof RetrofitError) {
					// if unauthorized try resetting access token
					RetrofitError error = (RetrofitError) throwable;
					if (error.getKind() == RetrofitError.Kind.HTTP && error.getResponse().getStatus() == 401) {
						loginManager.clearToken();
						return Observable.just(null);
					}

					// if problem getting the token then forward exception
					Throwable cause = error.getCause();
					if (cause != null && cause instanceof GoogleAuthException) {
						Timber.d("found GoogleAuthException, should log out");
						return Observable.error(throwable);
					}
				}

				// linear backoff
				currentDelay += DELAY;
				return Observable.timer(currentDelay, TimeUnit.MILLISECONDS);
			}
		});
	}
}
