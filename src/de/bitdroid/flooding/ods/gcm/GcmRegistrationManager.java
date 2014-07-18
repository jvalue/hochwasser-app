package de.bitdroid.flooding.ods.gcm;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

import de.bitdroid.flooding.utils.Assert;


public final class GcmRegistrationManager {

	private static final String PREFS_NAME = "GcmRegistrationManager";


	private final Context context;

	public GcmRegistrationManager(Context context) {
		Assert.assertNotNull(context);
		this.context = context;
	}


	public void update(String objectId, GcmStatus status) {
		Assert.assertNotNull(objectId, status);

		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putString(objectId, status.name());
		editor.commit();
	}


	public GcmStatus get(String objectId) {
		Assert.assertNotNull(objectId);

		String status = getSharedPreferences().getString(objectId, null);
		if (status == null) return GcmStatus.UNREGISTERED;
		else return GcmStatus.valueOf(status);
	}


	public Map<String, GcmStatus> getAll() {
		Map<String, ?> entries = getSharedPreferences().getAll();

		Map<String, GcmStatus> result = new HashMap<String, GcmStatus>();
		for (Map.Entry<String, ?> entry : entries.entrySet()) {
			result.put(entry.getKey(), GcmStatus.valueOf(entry.getValue().toString()));
		}

		return result;
	}


	public Map<String, GcmStatus> getAll(GcmStatus status) {
		Assert.assertNotNull(status);

		Map<String, GcmStatus> allEntries = getAll();
		Map<String, GcmStatus> filteredEntries = new HashMap<String, GcmStatus>();
		for (Map.Entry<String, GcmStatus> entry : allEntries.entrySet()) {
			if (entry.getValue().equals(status)) 
				filteredEntries.put(entry.getKey(), entry.getValue());
		}
		
		return filteredEntries;
	}


	private SharedPreferences getSharedPreferences() {
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}

}
