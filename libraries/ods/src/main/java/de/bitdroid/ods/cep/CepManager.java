package de.bitdroid.ods.cep;

import java.util.Set;

import de.bitdroid.ods.gcm.GcmStatus;

public interface CepManager {

	public static final String ACTION_REGISTRATION_STATUS_CHANGED = "de.bitdroid.ods.cep.ACTION_REGISTRATION_STATUS_CHANGED";

	public static final String
			EXTRA_EPL_STMT = "EXTRA_EPL_STMT",
			EXTRA_REGISTER = "EXTRA_REGISTER",
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
	 * Registered a stmt with the CEPS service.
	 */
	public void registerEplStmt(String eplStmt);

	/**
	 * Unregisteres a stmt from the CEPS service.
	 */
	public void unregisterEplStmt(String eplStmt);

	/**
	 * @return the registration status for this epl stmt.
	 */
	public GcmStatus getRegistrationStatus(String eplStmt);

	/**
	 * @return all registered epl stmts.
	 */
	public Set<String> getRegisteredStmts();

	/**
	 * @return a CEPS client id (if registered) for a given epl stmt
	 */
	public String getEplStmtForClientId(String clientId);

	/**
	 * Unregisters a client from the CEPS
	 */
	public void unregisterClientId(String clientId);
}
