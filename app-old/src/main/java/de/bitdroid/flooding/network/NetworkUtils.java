package de.bitdroid.flooding.network;


import javax.inject.Inject;

import de.bitdroid.flooding.auth.LoginManager;
import rx.Observable;

public final class NetworkUtils {

	private final DefaultTransformer defaultTransformer;

	@Inject
	NetworkUtils(LoginManager loginManager) {
		this.defaultTransformer = new DefaultTransformer(new RetryFunc(loginManager));
	}


	@SuppressWarnings("unchecked")
	public <T> Observable.Transformer<T, T> getDefaultTransformer() {
		return (Observable.Transformer<T, T>) defaultTransformer;
	}
}
