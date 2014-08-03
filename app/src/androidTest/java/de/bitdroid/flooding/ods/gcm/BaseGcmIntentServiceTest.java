package de.bitdroid.flooding.ods.gcm;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.test.AndroidTestCase;

import de.bitdroid.flooding.testUtils.PrefsRenamingDelegatingContext;
import de.bitdroid.flooding.testUtils.SharedPreferencesHelper;

public class BaseGcmIntentServiceTest extends AndroidTestCase {

	private static final String PREFIX = BaseGcmIntentServiceTest.class.getSimpleName();

	private int
			handleRegistrationCounter = 0,
			prepareResultIntentCounter = 0,
			getActionNameCounter = 0;


	public void testRegister() throws Exception {
		final String SERVICE_CLIENTID = "serviceClientId";
		final boolean REGISTER = true;
		final String ACTION = "BaseGcmIntentServiceTest.ACTION";

		setContext(new PrefsRenamingDelegatingContext(getContext(), PREFIX));
		SharedPreferencesHelper.clearAll((PrefsRenamingDelegatingContext) getContext());

		BaseGcmIntentService service = new BaseGcmIntentService("test", getContext()) {
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

		service.onHandleIntent(intent);

		assertEquals(1, handleRegistrationCounter);
		assertEquals(1, prepareResultIntentCounter);
		assertEquals(1, getActionNameCounter);
	}

}
