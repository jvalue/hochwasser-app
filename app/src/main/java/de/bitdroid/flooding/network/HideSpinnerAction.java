package de.bitdroid.flooding.network;

import de.bitdroid.flooding.ui.AbstractActivity;
import de.bitdroid.flooding.ui.AbstractFragment;

/**
 * Hides the spinner of an activity / fragment.
 */
public class HideSpinnerAction extends AbstractErrorAction {

	private final AbstractActivity activity;
	private final AbstractFragment fragment;

	public HideSpinnerAction(AbstractActivity activity) {
		this.activity = activity;
		this.fragment = null;
	}

	public HideSpinnerAction(AbstractFragment fragment) {
		this.activity = null;
		this.fragment = fragment;
	}


	@Override
	protected void doCall(Throwable throwable) {
		if (activity != null) activity.hideSpinner();
		if (fragment != null) fragment.hideSpinner();
	}

}
