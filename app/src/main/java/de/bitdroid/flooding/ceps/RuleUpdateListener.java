package de.bitdroid.flooding.ceps;


import de.bitdroid.flooding.gcm.GcmStatus;

interface RuleUpdateListener {

	public void onStatusChanged(Rule rule, GcmStatus status);

}
