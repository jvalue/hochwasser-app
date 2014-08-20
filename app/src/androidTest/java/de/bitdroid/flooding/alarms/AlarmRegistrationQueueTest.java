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
		Intent intent = new Intent();
		intent.putExtra(AlarmRegistrationQueue.EXTRA_RULE, rule);
		intent.putExtra(AlarmRegistrationQueue.EXTRA_REGISTER, true);
		startService(intent);
		verify(cepManager, times(1)).registerRule(eq(rule));

		Intent resultIntent = new Intent(getContext(), AlarmRegistrationQueue.StatusReceiver.class);
		resultIntent.putExtra(CepManager.EXTRA_RULE, rule);
		resultIntent.putExtra(CepManager.EXTRA_STATUS, GcmStatus.REGISTERED.name());
		getContext().sendBroadcast(resultIntent);
		verify(cepManager, times(1)).registerRule(eq(rule));
		verify(cepManager, never()).unregisterRule(eq(rule));


		// unregister
		when(cepManager.getRegistrationStatus(rule)).thenReturn(GcmStatus.REGISTERED);
		intent.putExtra(AlarmRegistrationQueue.EXTRA_REGISTER, false);
		startService(intent);
		verify(cepManager, times(1)).unregisterRule(eq(rule));

		resultIntent = new Intent(getContext(), AlarmRegistrationQueue.StatusReceiver.class);
		resultIntent.putExtra(CepManager.EXTRA_RULE, rule);
		resultIntent.putExtra(CepManager.EXTRA_STATUS, GcmStatus.UNREGISTERED.name());
		getContext().sendBroadcast(resultIntent);
		verify(cepManager, times(1)).registerRule(eq(rule));
		verify(cepManager, times(1)).unregisterRule(eq(rule));
	}

}
