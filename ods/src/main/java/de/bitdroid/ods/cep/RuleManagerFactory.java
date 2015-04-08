package de.bitdroid.ods.cep;


import android.content.Context;

import de.bitdroid.utils.Assert;

public class RuleManagerFactory {

	private RuleManagerFactory() { }


	private static RuleManager manager;

	public static synchronized RuleManager createRuleManager(Context context) {
		Assert.assertNotNull(context);
		if (manager == null) manager = new RuleManagerImpl(context, new RuleDb(context));
		return manager;
	}


	// use only for testing!
	public static void setRuleManager(RuleManager manager) {
		Assert.assertNotNull(manager);
		RuleManagerFactory.manager = manager;
	}

}
