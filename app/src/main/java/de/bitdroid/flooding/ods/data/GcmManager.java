package de.bitdroid.flooding.ods.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.HashSet;
import java.util.Set;

import de.bitdroid.flooding.ods.gcm.GcmRegistrationManager;
import de.bitdroid.flooding.ods.gcm.GcmStatus;
import de.bitdroid.flooding.utils.Assert;


final class GcmManager {

	private static final String PREFS_NAME = GcmManager.class.getName();


	private final Context context;
	private final GcmRegistrationManager registrationManager;

	public GcmManager(Context context) {
		Assert.assertNotNull(context);
		this.context = context;
		this.registrationManager = new GcmRegistrationManager(context, PREFS_NAME);
	}


	void registerSource(OdsSource source) {
		sourceRegistrationHelper(source, true);
	}


	void unregisterSource(OdsSource source) {
		sourceRegistrationHelper(source, false);
	}


	private void sourceRegistrationHelper(OdsSource source, boolean register) {
		String sourceId = source.toString();
		String clientId = registrationManager.getClientIdForObjectId(sourceId);

		// mark task pending
		GcmStatus status = null;
		if (register) status = GcmStatus.PENDING_REGISTRATION;
		else status = GcmStatus.PENDING_UNREGISTRATION;
		registrationManager.update(sourceId, clientId, status);

		// execute in background
		Intent registrationIntent = new Intent(context, GcmIntentService.class);
		registrationIntent.putExtra(GcmIntentService.EXTRA_SOURCE, sourceId);
		registrationIntent.putExtra(GcmIntentService.EXTRA_SERVICE_CLIENTID, clientId);
		registrationIntent.putExtra(GcmIntentService.EXTRA_REGISTER, register);
		context.startService(registrationIntent);
	}


	GcmStatus getRegistrationStatus(OdsSource source) {
		return registrationManager.getStatusForObjectId(source.toString());
	}


	Set<OdsSource> getRegisteredSources() {
		Set<String> sourceStrings = registrationManager.getAllObjects(GcmStatus.REGISTERED);
		Set<OdsSource> sources = new HashSet<OdsSource>();
		for (String sourceString : sourceStrings) {
			sources.add(OdsSource.fromString(sourceString));
		}
		return sources;
	}


	public static class StatusUpdater extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String sourceString = intent.getStringExtra(GcmIntentService.EXTRA_SOURCE);
			String errorMsg = intent.getStringExtra(GcmIntentService.EXTRA_ERROR_MSG);
			String clientId = intent.getStringExtra(GcmIntentService.EXTRA_SERVICE_CLIENTID);
			boolean register = intent.getBooleanExtra(GcmIntentService.EXTRA_REGISTER, false);

			// clear pending flag
			if (errorMsg != null) register = !register;

			GcmStatus status = null;
			if (register) status = GcmStatus.REGISTERED;
			else status = GcmStatus.UNREGISTERED;
			new GcmRegistrationManager(context, PREFS_NAME).update(sourceString, clientId, status);
		}

	}

}
