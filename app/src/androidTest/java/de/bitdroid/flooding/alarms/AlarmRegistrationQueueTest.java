package de.bitdroid.flooding.alarms;


import android.content.Intent;
import android.test.ServiceTestCase;

import de.bitdroid.ods.cep.RuleManager;
import de.bitdroid.ods.cep.RuleManagerFactory;
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

	private RuleManager ruleManager;


	public AlarmRegistrationQueueTest() {
		super(AlarmRegistrationQueue.class);
	}


	@Override
	public void setUp() throws Exception {
		super.setUp();

		this.ruleManager = mock(RuleManager.class);
		RuleManagerFactory.setRuleManager(this.ruleManager);
		setContext(new PrefsRenamingDelegatingContext(getContext(), PREFIX));
	}


	public void testSimpleActions() {
		// register
		Rule rule = new Rule.Builder("somePath").build();
		when(ruleManager.getRegistrationStatus(rule)).thenReturn(GcmStatus.UNREGISTERED);
		startService(getServiceIntent(rule, true));

		verify(ruleManager, times(1)).registerRule(eq(rule));

		getContext().sendBroadcast(getStatusIntent(rule, GcmStatus.PENDING_REGISTRATION));
		getContext().sendBroadcast(getStatusIntent(rule, GcmStatus.REGISTERED));

		verify(ruleManager, times(1)).registerRule(eq(rule));
		verify(ruleManager, never()).unregisterRule(eq(rule));


		// unregister
		when(ruleManager.getRegistrationStatus(rule)).thenReturn(GcmStatus.REGISTERED);
		startService(getServiceIntent(rule, false));

		verify(ruleManager, times(1)).unregisterRule(eq(rule));

		getContext().sendBroadcast(getStatusIntent(rule, GcmStatus.PENDING_UNREGISTRATION));
		getContext().sendBroadcast(getStatusIntent(rule, GcmStatus.UNREGISTERED));

		verify(ruleManager, times(1)).registerRule(eq(rule));
		verify(ruleManager, times(1)).unregisterRule(eq(rule));
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
		intent.putExtra(RuleManager.EXTRA_RULE, rule);
		intent.putExtra(RuleManager.EXTRA_STATUS, status.name());
		return intent;
	}

}
