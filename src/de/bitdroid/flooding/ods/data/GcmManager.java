package de.bitdroid.flooding.ods.data;

import java.util.HashSet;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.bitdroid.flooding.ods.gcm.GcmRegistrationManager;
import de.bitdroid.flooding.ods.gcm.GcmStatus;
import de.bitdroid.flooding.utils.Assert;


public final class GcmManager {

	private static GcmManager instance;

	public static GcmManager getInstance(Context context) {
		Assert.assertNotNull(context);
		if (instance == null) instance = new GcmManager(context);
		return instance;
	}


	private final Context context;
	private final GcmRegistrationManager registrationManager;

	private GcmManager(Context context) {
		Assert.assertNotNull(context);
		this.context = context;
		this.registrationManager = new GcmRegistrationManager(context, GcmManager.class.getName());
	}


	public void registerSource(OdsSource source) {
		sourceRegistrationHelper(source, true);
	}


	public void unregisterSource(OdsSource source) {
		sourceRegistrationHelper(source, false);
	}


	private void sourceRegistrationHelper(OdsSource source, boolean register) {
		String sourceId = source.toString();
		String clientId = registrationManager.getClientId(sourceId);

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


	public GcmStatus getRegistrationStatus(OdsSource source) {
		return registrationManager.getStatus(source.toString());
	}


	public Set<OdsSource> getRegisteredSources() {
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
			GcmManager.getInstance(context).registrationManager.update(sourceString, clientId, status);
		}

	}

}
