package de.bitdroid.flooding.news;


import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Default RxJava transformer for IO operations (e.g. database). Does
 * not retry on errors.
 */
public class DefaultTransformer<T> implements Observable.Transformer<T, T> {

	@Override
	public Observable<T> call(Observable<T> observable) {
		return observable
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread());
	}

}
