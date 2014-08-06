package de.bitdroid.ods.gcm;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.test.ServiceTestCase;

import de.bitdroid.testUtils.PrefsRenamingDelegatingContext;
import de.bitdroid.testUtils.SharedPreferencesHelper;

public class BaseGcmIntentServiceTest extends ServiceTestCase<BaseGcmIntentServiceTest.DummyBaseGcmIntentService> {

	private static final String PREFIX = BaseGcmIntentServiceTest.class.getSimpleName();

	private static final String SERVICE_CLIENTID = "serviceClientId";
	private static final boolean REGISTER = true;
	private static final String ACTION = "BaseGcmIntentServiceTest.ACTION";

	private static int
			handleRegistrationCounter = 0,
			prepareResultIntentCounter = 0,
			getActionNameCounter = 0;


	public BaseGcmIntentServiceTest() {
		super(DummyBaseGcmIntentService.class);
	}

	public void testRegister() throws Exception {

		setContext(new PrefsRenamingDelegatingContext(getContext(), PREFIX));
		SharedPreferencesHelper.clearAll((PrefsRenamingDelegatingContext) getContext());


		GcmIdManager idManager = new GcmIdManager(getContext());
		assertNull(idManager.getClientId());

		getContext().registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				assertNull(intent.getStringExtra(BaseGcmIntentService.EXTRA_ERROR_MSG));
				assertTrue(intent.getExtras().containsKey(BaseGcmIntentService.EXTRA_REGISTER));
				assertTrue(intent.getExtras().containsKey(BaseGcmIntentService.EXTRA_SERVICE_CLIENTID));
			}
		}, new IntentFilter(ACTION));

		Intent intent = new Intent("test");
		intent.putExtra(BaseGcmIntentService.EXTRA_REGISTER, true);

		startService(intent);
		Thread.sleep(300);

		assertEquals(1, handleRegistrationCounter);
		assertEquals(1, prepareResultIntentCounter);
		assertEquals(1, getActionNameCounter);
	}


	public static final class DummyBaseGcmIntentService extends BaseGcmIntentService {

		@Override
		protected String handleRegistration(
				Intent intent,
				String gcmClientId,
				String serviceClientId,
				boolean register) throws Exception {

			assertNotNull(gcmClientId);
			assertNull(serviceClientId);
			assertEquals(REGISTER, register);

			handleRegistrationCounter++;

			return SERVICE_CLIENTID;
		}

		@Override
		protected void prepareResultIntent(Intent originalIntent, Intent resultIntent) {
			assertFalse(originalIntent.getExtras().containsKey(EXTRA_ERROR_MSG));
			assertTrue(originalIntent.getExtras().containsKey(EXTRA_REGISTER));
			assertFalse(originalIntent.getExtras().containsKey(EXTRA_SERVICE_CLIENTID));

			assertNull(resultIntent.getExtras().getString(EXTRA_ERROR_MSG));
			assertTrue(resultIntent.getExtras().containsKey(EXTRA_REGISTER));
			assertTrue(resultIntent.getExtras().containsKey(EXTRA_SERVICE_CLIENTID));

			prepareResultIntentCounter++;
		}

		@Override
		protected String getActionName() {
			getActionNameCounter++;
			return ACTION;
		}
	};

}
