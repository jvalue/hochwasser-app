package de.bitdroid.flooding.network;


import rx.exceptions.OnErrorThrowable;
import rx.functions.Action1;

/**
 * Base RxJava error handler which can forward the error
 * to further handlers.
 */
public abstract class AbstractErrorAction implements Action1<Throwable> {


	private Action1<Throwable> nextAction = null;

	public void setNextAction(Action1<Throwable> nextAction) {
		this.nextAction = nextAction;
	}


	@Override
	public final void call(Throwable throwable) {
		if (throwable instanceof OnErrorThrowable) {
			call(throwable.getCause());
			return;
		}

		doCall(throwable);
		if (nextAction != null) nextAction.call(throwable);
	}


	protected abstract void doCall(Throwable throwable);

}
