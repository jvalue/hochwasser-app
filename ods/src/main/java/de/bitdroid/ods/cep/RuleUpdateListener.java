package de.bitdroid.ods.cep;


import de.bitdroid.ods.gcm.GcmStatus;

interface RuleUpdateListener {

	public void onStatusChanged(Rule rule, GcmStatus status);

}
