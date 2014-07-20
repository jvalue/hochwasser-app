package de.bitdroid.flooding.ods.cep;

import java.net.URL;

import android.content.Context;
import android.content.SharedPreferences;

import de.bitdroid.flooding.utils.Assert;


public final class CepManager {
	
	private static final String PREFS_NAME = CepManager.class.getName();
	private static final String KEY_SERVER_NAME = "serverName";

	private static CepManager instance;
	public static CepManager getInstance(Context context) {
		Assert.assertNotNull(context);
		synchronized(CepManager.class) {
			if (instance == null)
				instance = new CepManager(context);
			return instance;
		}
	}


	private final Context context;

	private CepManager(Context context) {
		this.context = context;
	}




	/**
	 * Set the name for the CEP server.
	 */
	public void setCepServerName(String cepServerName) {
		Assert.assertNotNull(cepServerName);
		try {
			URL checkUrl = new URL(cepServerName);
			checkUrl.toURI();
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putString(KEY_SERVER_NAME, cepServerName);
		editor.commit();
	}


	/**
	 * Returns the CEP server name currently being used for all interaction with
	 * the CEP server.
	 */
	public String getCepServerName() {
		return getSharedPreferences().getString(KEY_SERVER_NAME, null);
	}


	private SharedPreferences getSharedPreferences() {
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}

}
