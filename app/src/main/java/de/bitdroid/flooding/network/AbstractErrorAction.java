package de.bitdroid.flooding.network;


import de.bitdroid.flooding.auth.AuthException;
import de.bitdroid.flooding.ui.AbstractRestrictedActivity;
import rx.functions.Action1;

/**
 * Base RxJava error handler.
 */
public abstract class AbstractErrorAction implements Action1<Throwable> {

	private final AbstractRestrictedActivity restrictedActivity;

	public AbstractErrorAction(AbstractRestrictedActivity restrictedActivity) {
		this.restrictedActivity = restrictedActivity;
	}


	@Override
	public final void call(Throwable throwable) {
		if (throwable instanceof AuthException) {
			restrictedActivity.logout();
			return;
		}
		doCall(throwable);
	}


	protected abstract void doCall(Throwable throwable);

}
