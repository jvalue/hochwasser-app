package de.bitdroid.flooding.alarms;


import android.content.Intent;
import android.test.ServiceTestCase;

import de.bitdroid.ods.cep.CepManager;
import de.bitdroid.ods.cep.CepManagerFactory;
import de.bitdroid.ods.cep.Rule;
import de.bitdroid.ods.gcm.GcmStatus;
import de.bitdroid.testUtils.PrefsRenamingDelegatingContext;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class AlarmRegistrationQueueTest extends ServiceTestCase<AlarmRegistrationQueue> {

	private static final String PREFIX = AlarmRegistrationQueueTest.class.getSimpleName();

	private CepManager cepManager;


	public AlarmRegistrationQueueTest() {
		super(AlarmRegistrationQueue.class);
	}


	@Override
	public void setUp() throws Exception {
		super.setUp();

		this.cepManager = mock(CepManager.class);
		CepManagerFactory.setCepManager(this.cepManager);
		setContext(new PrefsRenamingDelegatingContext(getContext(), PREFIX));
	}


	public void testSimpleActions() {
		// register
		Rule rule = new Rule.Builder("somePath").build();
		when(cepManager.getRegistrationStatus(rule)).thenReturn(GcmStatus.UNREGISTERED);
		startService(getServiceIntent(rule, true));

		verify(cepManager, times(1)).registerRule(eq(rule));

		getContext().sendBroadcast(getStatusIntent(rule, GcmStatus.PENDING_REGISTRATION));
		getContext().sendBroadcast(getStatusIntent(rule, GcmStatus.REGISTERED));

		verify(cepManager, times(1)).registerRule(eq(rule));
		verify(cepManager, never()).unregisterRule(eq(rule));


		// unregister
		when(cepManager.getRegistrationStatus(rule)).thenReturn(GcmStatus.REGISTERED);
		startService(getServiceIntent(rule, false));

		verify(cepManager, times(1)).unregisterRule(eq(rule));

		getContext().sendBroadcast(getStatusIntent(rule, GcmStatus.PENDING_UNREGISTRATION));
		getContext().sendBroadcast(getStatusIntent(rule, GcmStatus.UNREGISTERED));

		verify(cepManager, times(1)).registerRule(eq(rule));
		verify(cepManager, times(1)).unregisterRule(eq(rule));
	}


	public void testQueue() {

	}


	private Intent getServiceIntent(Rule rule, boolean register) {
		Intent intent = new Intent();
		intent.putExtra(AlarmRegistrationQueue.EXTRA_RULE, rule);
		intent.putExtra(AlarmRegistrationQueue.EXTRA_REGISTER, register);
		return intent;
	}


	private Intent getStatusIntent(Rule rule, GcmStatus status) {
		Intent intent = new Intent(getContext(), AlarmRegistrationQueue.StatusReceiver.class);
		intent.putExtra(CepManager.EXTRA_RULE, rule);
		intent.putExtra(CepManager.EXTRA_STATUS, status.name());
		return intent;
	}

}
