package de.bitdroid.ods.cep;

import java.util.Set;

import de.bitdroid.ods.gcm.GcmStatus;

public interface CepManager {

	public static final String ACTION_REGISTRATION_STATUS_CHANGED = "de.bitdroid.ods.cep.ACTION_REGISTRATION_STATUS_CHANGED";

	public static final String
			EXTRA_RULE = "EXTRA_RULE",
			EXTRA_STATUS = "EXTRA_STATUS",
			EXTRA_ERROR_MSG = "EXTRA_ERROR_MSG";


	/**
	 * Set the name for the CEP server.
	 */
	public void setCepServerName(String cepServerName);

	/**
	 * Returns the CEP server name currently being used for all interaction with
	 * the CEP server.
	 */
	public String getCepServerName();

	/**
	 * Registered a rule with the CEPS service.
	 */
	public void registerRule(Rule rule);

	/**
	 * Unregisteres a rule from the CEPS service.
	 */
	public void unregisterRule(Rule rule);

	/**
	 * @return the registration status for this rule.
	 */
	public GcmStatus getRegistrationStatus(Rule rule);

	/**
	 * @return a rule for a clientId if present.
	 */
	public Rule getRuleForClientId(String clientId);

	/**
	 * Unregisters a client from the CEPS
	 */
	public void unregisterClientId(String clientId);

	/**
	 * Returns all rules, regardless of their registration status.
	 */
	public Set<Rule> getAllRules();

	/**
	 * Registeres a listener to receive updates whenever a registration status changes.
	 */
	public void registerRuleUpdateListener(RuleUpdateListener listener);

	/**
	 * Stops a listener from receiving callbacks.
	 */
	public void unregisterRuleUpdateListener(RuleUpdateListener listener);
}
