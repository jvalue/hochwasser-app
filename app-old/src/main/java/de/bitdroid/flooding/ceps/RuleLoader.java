package de.bitdroid.flooding.ceps;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.bitdroid.flooding.gcm.GcmStatus;


/**
 * Loader for fetching all rules and their registration status.
 * <br>
 * In general the registration status will either be pending or registered,
 * but never unregistered, as all possible rules are, if unknown to the CEP
 * manager, considered unregistered.
 */
public final class RuleLoader extends AsyncTaskLoader<Map<Rule, GcmStatus>> implements RuleUpdateListener {

	private final RuleManager ruleManager;

	private Map<Rule, GcmStatus> rules;
	private boolean monitoringRules = false;

	public RuleLoader(Context context, RuleManager ruleManager) {
		super(context);
		this.ruleManager = ruleManager;
	}


	@Override
	public Map<Rule, GcmStatus> loadInBackground() {
		Set<Rule> rules = ruleManager.getAllRules();
		Map<Rule, GcmStatus> result = new HashMap<Rule, GcmStatus>();
		for (Rule rule : rules) {
			result.put(rule, ruleManager.getRegistrationStatus(rule));
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
			ruleManager.registerRuleUpdateListener(this);
		}

		if (takeContentChanged() || rules == null) forceLoad();
	}


	@Override
	protected void onStopLoading() {
		cancelLoad();
	}


	@Override
	protected void onReset() {
		onStopLoading();
		rules = null;

		if (monitoringRules) {
			monitoringRules = false;
			ruleManager.unregisterRuleUpdateListener(this);
		}
	}


	@Override
	public void onStatusChanged(Rule rule, GcmStatus status) {
		onContentChanged();
	}

}
