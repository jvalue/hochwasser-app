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

	public void unregisterSource(Context context, OdsSource source) {
		throw new UnsupportedOperationException();
	}

	public boolean isSourceRegistered(Context context, OdsSource source) {
		if (source == null) throw new NullPointerException("param cannot be null");
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.contains(source.getClass().getName());
	}


	public void startMonitoring(Context context) {
		SyncUtils.setupSyncAdapter(context);
	}

	public void stopMonitoring(Context context) {
		throw new UnsupportedOperationException();
	}


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
