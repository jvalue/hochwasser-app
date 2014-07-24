package de.bitdroid.flooding.ods.gcm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;

import de.bitdroid.flooding.utils.Assert;


public final class GcmRegistrationManager {

	private static final String
		PREFS_KEY_STATUS = "status-",
		PREFS_KEY_CLIENTID = "clientId-";


	private final Context context;
	private final String prefsName;

	public GcmRegistrationManager(Context context, String prefsName) {
		Assert.assertNotNull(context, prefsName);
		this.context = context;
		this.prefsName = prefsName;
	}


	public void update(String objectId, String clientId, GcmStatus status) {
		Assert.assertNotNull(objectId, status);

		SharedPreferences.Editor editor = getSharedPreferences().edit();
		if (clientId != null) editor.putString(getClientIdKey(objectId), clientId);
		editor.putString(getStatusKey(objectId), status.name());
		editor.commit();
	}


	public String getClientId(String objectId) {
		Assert.assertNotNull(objectId);

		return getSharedPreferences().getString(getClientIdKey(objectId), null);
	}


	public GcmStatus getStatus(String objectId) {
		Assert.assertNotNull(objectId);

		String status = getSharedPreferences().getString(getStatusKey(objectId), null);
		if (status == null) return GcmStatus.UNREGISTERED;
		else return GcmStatus.valueOf(status);
	}


	public Set<String> getAllObjects() {
		Set<String> keys = getSharedPreferences().getAll().keySet();
		Set<String> objects = new HashSet<String>();

		for (String key : keys) {
			if (key.startsWith(PREFS_KEY_CLIENTID)) key = key.replaceFirst(PREFS_KEY_CLIENTID, "");
			else if (key.startsWith(PREFS_KEY_STATUS)) key = key.replaceFirst(PREFS_KEY_STATUS, "");
			objects.add(key);
		}

		return objects;
	}


	public Set<String> getAllObjects(GcmStatus status) {
		Assert.assertNotNull(status);

		Set<String> objects = getAllObjects();
		Iterator<String> iter = objects.iterator();
		while (iter.hasNext()) {
			if (getStatus(iter.next()) != status) iter.remove();
		}

		return objects;
	}


	private String getClientIdKey(String objectId) {
		return PREFS_KEY_CLIENTID + objectId;
	}


	private String getStatusKey(String objectId) {
		return PREFS_KEY_STATUS + objectId;
	}


	private SharedPreferences getSharedPreferences() {
		return context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
	}

}
