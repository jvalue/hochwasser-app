package de.bitdroid.flooding.ods;

import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;


public final class OdsSourceManager {
	
	private static final String PREFS_NAME = "de.bitdroid.flooding.ods.OdsSourceManager";

	private static final OdsSourceManager instance = new OdsSourceManager();
	public static OdsSourceManager getInstance() {
		return instance;
	}


	private String odsServerName = "http://faui2o2f.cs.fau.de:8080/open-data-service";


	/**
	 * Register a source which should be keep in sync with the ODS server.
	 * <br>
	 * <b>Note:</b> this does not actually trigger synchroniziation but merely marks
	 * a source to be synchronized. To start periodic synchronizing, use 
	 * {@link #startMonitoring(android.content.Context)}.
	 */
	public void registerSource(Context context, OdsSource source) {
		if (context == null || source == null) 
			throw new NullPointerException("params cannot be null");

		String key = source.getClass().getName();
		String value = source.getSourceUrl();

		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(key, value);
		editor.apply();
	}


	/**
	 * Stop monitoring a source and remove all related data from the device.
	 * <br>
	 * To additionally stop synchronizing for all sources and shut down the
	 * SyncAdapter, use {@link #stopMonitoring(android.content.Context)}.
	 */
	public void unregisterSource(Context context, OdsSource source) {
		throw new UnsupportedOperationException();
	}


	/**
	 * Check whether a source is currently registered for synchronization.
	 */
	public boolean isSourceRegistered(Context context, OdsSource source) {
		if (source == null) throw new NullPointerException("param cannot be null");
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.contains(source.getClass().getName());
	}


	/**
	 * Start synchronization for all sources.
	 */
	public void startMonitoring(Context context) {
		SyncUtils.setupSyncAdapter(context);
	}


	/**
	 * Stop synchronization for all sources.
	 */
	public void stopMonitoring(Context context) {
		throw new UnsupportedOperationException();
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
		this.odsServerName = odsServerName;
	}


	public String getOdsServerName() {
		return odsServerName;
	}


	Set<OdsSource> getSources(Context context) {
		if (context == null) throw new NullPointerException("param cannot be null");

		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		Map<String, ?> values = prefs.getAll();

		Set<OdsSource> sources = new HashSet<OdsSource>();
		for (String key : values.keySet()) {
			sources.add(OdsSource.fromClassName(key));
		}
		return sources;
	}
}
