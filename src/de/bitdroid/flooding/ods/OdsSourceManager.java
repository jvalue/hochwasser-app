package de.bitdroid.flooding.ods;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;

import de.bitdroid.flooding.utils.Log;


public final class OdsSourceManager {

	private static final OdsSourceManager instance = new OdsSourceManager();
	public static OdsSourceManager getInstance() {
		return instance;
	}


	private final Set<String> sourceClassNames = new HashSet<String>();
	private String odsServerName = "http://faui2o2f.cs.fau.de:8080/open-data-service";

	public <T extends OdsTableAdapter> void registerSource(Class<T> source) {
		sourceClassNames.add(source.getName());
	}

	public <T extends OdsTableAdapter> void unregisterSource(Class<T> source) {
		throw new UnsupportedOperationException();
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


	@SuppressWarnings("unchecked")
	Set<OdsTableAdapter> getSources() {
		Set<OdsTableAdapter> sources = new HashSet<OdsTableAdapter>();
		for (String className: sourceClassNames) {
			try {
				Class<? extends OdsTableAdapter> sourceClass 
						= (Class<? extends OdsTableAdapter>) Class.forName(className);
				sources.add(sourceClass.newInstance());
			} catch (Exception e) {
				Log.error(android.util.Log.getStackTraceString(e));
			}
		}
		return sources;
	}

}
