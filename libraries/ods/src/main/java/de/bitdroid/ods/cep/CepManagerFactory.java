package de.bitdroid.ods.cep;


import android.content.Context;

import de.bitdroid.utils.Assert;

public class CepManagerFactory {

	private CepManagerFactory() { }


	private static CepManager manager;

	public static synchronized CepManager createCepManager(Context context) {
		Assert.assertNotNull(context);
		if (manager == null) manager = new CepManagerImpl(context, new RuleDb(context));
		return manager;
	}


	// use only for testing!
	public static void setCepManager(CepManager manager) {
		Assert.assertNotNull(manager);
		CepManagerFactory.manager = manager;
	}

}
