package de.bitdroid.flooding.ods;

public interface GcmRegistrationListener {

	public void onSuccess();
	public void onFailure(GcmException ge);

}
