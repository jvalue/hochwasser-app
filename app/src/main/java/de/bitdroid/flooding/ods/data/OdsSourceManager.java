package de.bitdroid.flooding.ods.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.net.URL;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.bitdroid.flooding.ods.gcm.GcmStatus;
import de.bitdroid.flooding.utils.Assert;
import de.bitdroid.flooding.utils.Log;


public final class OdsSourceManager {
	
	private static final String PREFS_NAME = OdsSourceManager.class.getName();
	private static final String KEY_SERVER_NAME = "serverName";

	private static OdsSourceManager instance;
	public static OdsSourceManager getInstance(Context context) {
		Assert.assertNotNull(context);
		synchronized(OdsSourceManager.class) {
			if (instance == null) {
				instance = new OdsSourceManager(
						context,
						new GcmManager(context),
						new SyncUtils(context),
						new SyncStatusListener(context));
			}
			return instance;
		}
	}


	private final Context context;
	private final GcmManager gcmManager;
	private final SyncUtils syncUtils;
	private final SyncStatusListener syncStatusListener;

	private OdsSourceManager(
			Context context,
			GcmManager gcmManager,
			SyncUtils syncUtils,
			SyncStatusListener syncStatusListener) {

		Assert.assertNotNull(context, gcmManager, syncUtils, syncStatusListener);
		this.context = context;
		this.gcmManager = gcmManager;
		this.syncUtils = syncUtils;
		this.syncStatusListener = syncStatusListener;
	}


	/**
	 * Check whether a source is currently registered for synchronization.
	 */
	public boolean isRegisteredForPolling(OdsSource source) {
		Assert.assertNotNull(source);
		SharedPreferences prefs = getSharedPreferences();
		return prefs.contains(source.toString());
	}


	/**
	 * Starts to synchronize all sources on a periodic schedule.
	 */
	public void startPolling(
			long pollFrequency,
			OdsSource ... sources) {

		Assert.assertNotNull((Object) sources);
		Assert.assertTrue(pollFrequency > 0, "pollFrequency must be > 0");
		Assert.assertFalse(syncUtils.isPeriodicSyncScheduled(), "sync already scheduled");

		Log.debug("started polling with frequency " + pollFrequency);

		addSyncAccount();
		for (OdsSource source : sources) registerSource(source);
		syncUtils.startPeriodicSync(pollFrequency);
	}


	/**
	 * Stops the periodic synchronization schedule.
	 */
	public void stopPolling() {
		Assert.assertTrue(syncUtils.isPeriodicSyncScheduled(), "sync not scheduled");

		Log.debug("stopped polling");

		syncUtils.stopPeriodicSync();

		SharedPreferences prefs = getSharedPreferences();
		Set<String> keySet = prefs.getAll().keySet();
		SharedPreferences.Editor editor = prefs.edit();
		for (String key : keySet)  if (!key.equals(KEY_SERVER_NAME)) editor.remove(key);
		editor.commit();
	}


	/**
	 * Returns whether a periodic sync is scheduled for execution.
	 */
	public boolean isPollingActive() {
		return syncUtils.isPeriodicSyncScheduled();
	}


	/**
	 * Returns all sources currently registered to receive polling updates.
	 */
	public Set<OdsSource> getPollingSources() {
		SharedPreferences prefs = getSharedPreferences();
		Map<String, ?> values = prefs.getAll();

		Set<OdsSource> sources = new HashSet<OdsSource>();
		for (String key : values.keySet()) {
			if (key.equals(KEY_SERVER_NAME)) continue;
			sources.add(OdsSource.fromString(key));
		}
		return sources;
	}


	/**
	 * Registering a source for automatic updates will sync this source
	 * every time data on the ODS for this source changes.
	 * <br>
	 * You should check first how often this source changes on the ODS. If the
	 * update intervals are too frequent, consider using a periodic sync instead.
	 * <br>
	 * This request will cause network operations. Make sure not to call it from
	 * the main thread.
	 */
	public void startPushNotifications(OdsSource source) {
		Assert.assertNotNull(source);
		GcmStatus status = gcmManager.getRegistrationStatus(source);
		Assert.assertEquals(status, GcmStatus.UNREGISTERED, "Already registered");

		gcmManager.registerSource(source);
	}


	/**
	 * Stops a source from receiving automatic updates each time the source
	 * is updated on the ODS server.
	 * <br>
	 * This request will cause network operations. Make sure not to call it from
	 * the main thread.
	 */
	public void stopPushNotifications(OdsSource source) {
		Assert.assertNotNull(source);
		GcmStatus status = gcmManager.getRegistrationStatus(source);
		Assert.assertEquals(status, GcmStatus.REGISTERED, "Not registered");

		gcmManager.unregisterSource(source);
	}


	/**
	 * Check whether a source is registered for push notifications whenever the
	 * source on the ODS changes.
	 */
	public GcmStatus getPushNotificationsRegistrationStatus(OdsSource source) {
		Assert.assertNotNull(source);
		return gcmManager.getRegistrationStatus(source);
	}


	/**
	 * Get all sources that are registered for push notifications when data on the
	 * ODS changes.
	 */
	public Set<OdsSource> getPushNotificationSources() {
		return gcmManager.getRegisteredSources();
	}



	/**
	 * Starts a manual sync for one source.
	 * <br>
	 * Note: do NOT use this as your primary way of fetching data from the server,
	 * as it requires more battery life.
	 */
	public void startManualSync(OdsSource source) {
		Assert.assertNotNull(source);

		Log.debug("starting manual sync");

		addSyncAccount();
		syncUtils.startManualSync(source);
	}


	/**
	 * Set the name for the ODS server.
	 * <br>
	 * This name will be combined with the source name defined in each
	 * {@link de.bitdroid.flooding.ods.data.OdsSource} to form the complete
	 * URL for accessing the ODS server.
	 */
	public void setOdsServerName(String odsServerName) {
		Assert.assertNotNull(odsServerName);
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
		return getSharedPreferences().getString(KEY_SERVER_NAME, null);
	}


	/**
	 * Returns a timestamp representing the last successful sync of this source,
	 * or null if none was recorded.
	 */
	public Calendar getLastSuccessfulSync(OdsSource source) {
		Assert.assertNotNull(source);
		return syncStatusListener.getLastSuccessfulSync(source);
	}


	/**
	 * Returns a timestamp representing the last sync of this source,
	 * or null if it has never been synced.
	 */
	public Calendar getLastFailedSync(OdsSource source) {
		Assert.assertNotNull(source);
		return syncStatusListener.getLastFailedSync(source);
	}


	/**
	 * Convenience method for getting the last sync for the supplied source.
	 * @return null if no sync ever occured.
	 */
	public Calendar getLastSync(OdsSource source) {
		Assert.assertNotNull(source);
		return syncStatusListener.getLastSync(source);
	}


	/**
	 * @return true if the underlying sync adapter is currently running.
	 */
	public boolean isSyncRunning() {
		return syncStatusListener.isSyncRunning();
	}


	private void registerSource(OdsSource source) {
		String key = source.toString();
		String value = source.getSourceUrlPath();

		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putString(key, value);
		editor.commit();
	}


	private void addSyncAccount() {
		if (!syncUtils.isAccountAdded()) syncUtils.addAccount();
	}


	private SharedPreferences getSharedPreferences() {
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}
}
