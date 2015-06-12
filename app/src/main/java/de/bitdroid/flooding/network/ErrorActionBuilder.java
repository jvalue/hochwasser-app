package de.bitdroid.flooding.network;

import rx.functions.Action1;

/**
 * Builder for combining multiple {@link AbstractErrorAction}s.
 */
public class ErrorActionBuilder {

	private AbstractErrorAction firstAction, lastAction;


	public ErrorActionBuilder add(AbstractErrorAction errorAction) {
		if (firstAction == null) {
			firstAction = errorAction;
			lastAction = errorAction;
		} else {
			lastAction.setNextAction(errorAction);
			lastAction = errorAction;
		}
		return this;
	}


	public Action1<Throwable> build() {
		return firstAction;
	}

}
