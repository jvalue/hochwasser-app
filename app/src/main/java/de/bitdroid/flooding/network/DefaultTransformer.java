package de.bitdroid.flooding.network;


import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public final class DefaultTransformer<T> implements Observable.Transformer<T, T> {

	private final RetryFunc retryFunc;

	DefaultTransformer(RetryFunc retryFunc) {
		this.retryFunc = retryFunc;
	}


	@Override
	public Observable<T> call(Observable<T> observable) {
		return observable
				.retryWhen(retryFunc)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread());
	}

}
