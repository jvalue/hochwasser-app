package de.bitdroid.ods.cep;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.bitdroid.ods.gcm.GcmStatus;


/**
 * Loader for fetching all rules and their registration status.
 * <br>
 * In general the registration status will either be pending or registered,
 * but never unregistered, as all possible rules are, if unknown to the CEP
 * manager, considered unregistered.
 */
public final class RuleLoader extends AsyncTaskLoader<Map<Rule, GcmStatus>> implements RuleUpdateListener {

	private final CepManager cepManager;

	private Map<Rule, GcmStatus> rules;
	private boolean monitoringRules = false;

	public RuleLoader(Context context, CepManager cepManager) {
		super(context);
		this.cepManager = cepManager;
	}


	@Override
	public Map<Rule, GcmStatus> loadInBackground() {
		Set<Rule> rules = cepManager.getAllRules();
		Map<Rule, GcmStatus> result = new HashMap<Rule, GcmStatus>();
		for (Rule rule : rules) {
			result.put(rule, cepManager.getRegistrationStatus(rule));
		}
		return result;
	}


	@Override
	public void deliverResult(Map<Rule, GcmStatus> rules) {
		if (isReset()) return;

		this.rules = rules;

		if (isStarted()) {
			super.deliverResult(rules);
		}
	}


	@Override
	protected void onStartLoading() {
		if (rules != null) deliverResult(rules);

		if (!monitoringRules) {
			monitoringRules = true;
			cepManager.registerRuleUpdateListener(this);
		}

		if (takeContentChanged() || rules == null) forceLoad();
	}


	@Override
	protected void onStopLoading() {
		cancelLoad();
	}


	@Override
	public void onCanceled(Map<Rule, GcmStatus> rules) {
		super.onCanceled(rules);
	}


	@Override
	protected void onReset() {
		onStopLoading();
		rules = null;

		if (monitoringRules) {
			monitoringRules = false;
			cepManager.unregisterRuleUpdateListener(this);
		}
	}


	@Override
	public void onStatusChanged(Rule rule, GcmStatus status) {
		onContentChanged();
	}

}
