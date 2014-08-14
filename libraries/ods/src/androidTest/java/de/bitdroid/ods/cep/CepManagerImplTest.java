package de.bitdroid.ods.cep;

import android.content.Context;
import android.content.Intent;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import de.bitdroid.ods.gcm.GcmStatus;
import de.bitdroid.testUtils.BaseAndroidTestCase;
import de.bitdroid.testUtils.PrefsRenamingDelegatingContext;
import de.bitdroid.testUtils.SharedPreferencesHelper;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CepManagerImplTest extends BaseAndroidTestCase {

	private static final String PREFIX = CepManagerImplTest.class.getSimpleName();


	@Override
	public void beforeClass() {
		// some love potion for our two friends: dexmaker and mockito
		System.setProperty("dexmaker.dexcache", getContext().getCacheDir().getPath());
	}


	public void testServerName() {
		Context context = new PrefsRenamingDelegatingContext(getContext(), PREFIX);
		SharedPreferencesHelper.clearAll((PrefsRenamingDelegatingContext) context);
		CepManager manager = new CepManagerImpl(context, new RuleDb(context));

		String serverName1 = "http://somedomain.com";
		String serverName2 = "http://someotherdomain.com";

		manager.setCepServerName(serverName1);
		assertEquals(serverName1, manager.getCepServerName());

		manager.setCepServerName(serverName2);
		assertEquals(serverName2, manager.getCepServerName());
	}


	public void testRuleCrud() throws Exception {
		Context mockContext = Mockito.mock(Context.class);
		Context renamingContext = new PrefsRenamingDelegatingContext(getContext(), PREFIX);
		RuleDb ruleDb = new RuleDb(renamingContext);
		CepManager manager = new CepManagerImpl(mockContext, ruleDb);

		Rule rule1 = newRule("path1", 2);
		Rule rule2 = newRule("path2", 0);

		assertEquals(GcmStatus.UNREGISTERED, manager.getRegistrationStatus(rule1));
		assertEquals(GcmStatus.UNREGISTERED, manager.getRegistrationStatus(rule2));
		assertEquals(0, manager.getAllRules().size());

		manager.registerRule(rule1);

		// test status
		assertEquals(GcmStatus.PENDING_REGISTRATION, manager.getRegistrationStatus(rule1));
		assertEquals(GcmStatus.UNREGISTERED, manager.getRegistrationStatus(rule2));
		assertEquals(1, manager.getAllRules().size());
		assertTrue(manager.getAllRules().contains(rule1));

		// test started service
		ArgumentCaptor<Intent> captor  = ArgumentCaptor.forClass(Intent.class);
		verify(mockContext).startService(captor.capture());
		Intent serviceIntent = captor.getValue();
		assertEquals(rule1, serviceIntent.getParcelableExtra(GcmIntentService.EXTRA_RULE));
		assertEquals(true, serviceIntent.getBooleanExtra(GcmIntentService.EXTRA_REGISTER, false));

		// test registration response
		CepManagerFactory.setCepManager(manager);
		Intent resultIntent = new Intent();
		resultIntent.putExtra(GcmIntentService.EXTRA_RULE, rule1);
		resultIntent.putExtra(GcmIntentService.EXTRA_SERVICE_CLIENTID, "someClientId");
		resultIntent.putExtra(GcmIntentService.EXTRA_REGISTER, true);
		new CepManagerImpl.StatusUpdater().onReceive(mockContext, resultIntent);

		// test broadcast sent
		captor  = ArgumentCaptor.forClass(Intent.class);
		verify(mockContext).sendBroadcast(captor.capture());
		Intent broadcastIntent = captor.getValue();
		assertEquals(rule1, broadcastIntent.getParcelableExtra(CepManager.EXTRA_RULE));
		assertTrue(broadcastIntent.getBooleanExtra(CepManager.EXTRA_REGISTER, false));
		assertEquals(GcmStatus.REGISTERED, manager.getRegistrationStatus(rule1));
		assertEquals(1, manager.getAllRules().size());

		// test unregister
		manager.unregisterRule(rule1);
		captor  = ArgumentCaptor.forClass(Intent.class);
		verify(mockContext, times(2)).startService(captor.capture());
		serviceIntent = captor.getValue();
		assertEquals(rule1, serviceIntent.getParcelableExtra(GcmIntentService.EXTRA_RULE));
		assertEquals(false, serviceIntent.getBooleanExtra(GcmIntentService.EXTRA_REGISTER, true));
		assertEquals(GcmStatus.PENDING_UNREGISTRATION, manager.getRegistrationStatus(rule1));

		// test registration response
		CepManagerFactory.setCepManager(manager);
		resultIntent = new Intent();
		resultIntent.putExtra(GcmIntentService.EXTRA_RULE, rule1);
		resultIntent.putExtra(GcmIntentService.EXTRA_SERVICE_CLIENTID, "someClientId");
		resultIntent.putExtra(GcmIntentService.EXTRA_REGISTER, false);
		new CepManagerImpl.StatusUpdater().onReceive(mockContext, resultIntent);
		assertEquals(GcmStatus.UNREGISTERED, manager.getRegistrationStatus(rule1));
		assertEquals(0, manager.getAllRules().size());

	}


	private Rule newRule(String path, int paramCount) {
		Rule.Builder builder = new Rule.Builder(path);
		for (int i = 0; i < paramCount; i++) {
			builder.parameter("key" + i, "value" + i);
		}
		return builder.build();
	}

}
