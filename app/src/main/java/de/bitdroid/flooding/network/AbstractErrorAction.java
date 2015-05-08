package de.bitdroid.flooding.network;


import de.bitdroid.flooding.auth.AuthException;
import de.bitdroid.flooding.auth.RestrictedResource;
import rx.functions.Action1;

/**
 * Base RxJava error handler for triggering logout on auth exception.
 */
public abstract class AbstractErrorAction implements Action1<Throwable> {

	private final RestrictedResource restrictedResource;

	public AbstractErrorAction(RestrictedResource restrictedResource) {
		this.restrictedResource = restrictedResource;
	}


	@Override
	public final void call(Throwable throwable) {
		if (throwable instanceof AuthException) {
			restrictedResource.logout();
			return;
		}
		doCall(throwable);
	}


	protected abstract void doCall(Throwable throwable);

}
