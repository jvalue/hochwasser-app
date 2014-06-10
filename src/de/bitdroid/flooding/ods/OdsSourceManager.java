package de.bitdroid.flooding.ods;

import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;


public final class OdsSourceManager {
	
	private static final String PREFS_NAME = "de.bitdroid.flooding.ods.OdsSourceManager";
	private static final String KEY_SERVER_NAME = "serverName";
	private static final String DEFAULT_SERVER_NAME = "http://faui2o2f.cs.fau.de:8080/open-data-service";

	private static OdsSourceManager instance;
	public static OdsSourceManager getInstance(Context context) {
		if(context == null) throw new NullPointerException("context cannot be null");
		synchronized(OdsSourceManager.class) {
			if (instance == null)
				instance = new OdsSourceManager(context);
			return instance;
		}
	}


	private final Context context;

	private OdsSourceManager(Context context) {
		this.context = context;
	}


	/**
	 * Check whether a source is currently registered for synchronization.
	 */
	public boolean isSourceRegisteredForPeriodicSync(OdsSource source) {
		if (source == null) throw new NullPointerException("param cannot be null");
		SharedPreferences prefs = getSharedPreferences();
		return prefs.contains(source.getClass().getName());
	}


	/**
	 * Starts to synchronize all sources on a periodic schedule.
	 */
	public void startPeriodicSync(
			long pollFrequency,
			OdsSource ... sources) {

		if (sources == null) 
			throw new NullPointerException("param cannot be null");
		if (pollFrequency <= 0) 
			throw new IllegalArgumentException("pollFrequency must be > 0");
		if (SyncUtils.isPeriodicSyncScheduled(context)) 
			throw new IllegalStateException("sync already scheduled");

		addSyncAccount();
		for (OdsSource source : sources) registerSource(source);
		SyncUtils.startPeriodicSync(context, pollFrequency);
	}


	/**
	 * Stops the periodic synchronization schedule.
	 */
	public void stopPeriodicSync() {
		if (!SyncUtils.isPeriodicSyncScheduled(context)) 
			throw new IllegalStateException("sync not scheduled");

		SyncUtils.stopPeriodicSync(context);
		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.clear();
		editor.commit();
	}


	/**
	 * Returns whether a periodic sync is scheduled for execution.
	 */
	public boolean isPeriodicSyncScheduled() {
		return SyncUtils.isPeriodicSyncScheduled(context);
	}


	/**
	 * Starts a manual sync for one source.
	 * <br>
	 * Note: do NOT use this as your primary way of fetching data from the server,
	 * as it requires more battery life.
	 */
	public void startManualSync(OdsSource source) {
		if (source == null) throw new NullPointerException("param cannot be null");
		addSyncAccount();
		SyncUtils.startManualSync(context, source);
	}


	/**
	 * Set the name for the ODS server.
	 * <br>
	 * This name will be combined with the source name defined in each
	 * {@link de.bitdroid.flooding.ods.OdsSource} to form the complete
	 * URL for accessing the ODS server.
	 */
	public void setOdsServerName(String odsServerName) {
		if (odsServerName == null) throw new NullPointerException("param cannot be null");
		try {
			URL checkUrl = new URL(odsServerName);
			checkUrl.toURI();
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putString(KEY_SERVER_NAME, odsServerName);
		editor.commit();
	}


	/**
	 * Returns the ODS server name currently being used for all interaction with
	 * the ODS server.
	 */
	public String getOdsServerName() {
		return getSharedPreferences().getString(KEY_SERVER_NAME, DEFAULT_SERVER_NAME);
	}


	Set<OdsSource> getPeriodicSyncSources() {
		SharedPreferences prefs = getSharedPreferences();
		Map<String, ?> values = prefs.getAll();

		Set<OdsSource> sources = new HashSet<OdsSource>();
		for (String key : values.keySet()) {
			if (key.equals(KEY_SERVER_NAME)) continue;
			sources.add(OdsSource.fromClassName(key));
		}
		return sources;
	}



	private void registerSource(OdsSource source) {
		String key = source.getClass().getName();
		String value = source.getSourceUrlPath();

		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putString(key, value);
		editor.commit();
	}


	private void addSyncAccount() {
		if (!SyncUtils.isAccountAdded(context)) SyncUtils.addAccount(context);
	}


	private SharedPreferences getSharedPreferences() {
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}
}
