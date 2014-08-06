package de.bitdroid.ods.cep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import de.bitdroid.testUtils.BaseAndroidTestCase;
import de.bitdroid.testUtils.PrefsRenamingDelegatingContext;
import de.bitdroid.testUtils.SharedPreferencesHelper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GcmReceiverTest extends BaseAndroidTestCase {

	private static final String PREFIX = GcmReceiverTest.class.getSimpleName();
	private static final String
			CLIENTID = "someClientId",
			EPL_STMT = "select * from *",
			EVENTID = "someEventId";

	private BroadcastReceiver eventReceiver;
	private int receiverCount;


	@Override
	public void beforeClass() {
		setContext(new PrefsRenamingDelegatingContext(getContext(), PREFIX));
	}


	@Override
	public void beforeTest() {
		SharedPreferencesHelper.clearAll((PrefsRenamingDelegatingContext) getContext());

		this.receiverCount = 0;
		this.eventReceiver = new BaseEventReceiver() {
			@Override
			protected void onReceive(Context context, String eplStmt, String eventId) {
				assertEquals(EPL_STMT, eplStmt);
				assertEquals(EVENTID, eventId);
				receiverCount++;
			}
		};

		getContext().registerReceiver(
				eventReceiver,
				new IntentFilter("de.bitdroid.ods.cep.ACTION_EVENT_RECEIVED"));
	}


	@Override
	public void tearDown() throws Exception {
		getContext().unregisterReceiver(eventReceiver);
	}


	public void testValidEvent() throws Exception {
		CepManager manager = mock(CepManager.class);
		when(manager.getEplStmtForClientId(CLIENTID)).thenReturn(EPL_STMT);
		CepManagerFactory.setCepManager(manager);

		new GcmReceiver().handle(getContext(), getIntent());

		Thread.sleep(200);

		assertEquals(1, receiverCount);
	}


	public void testInvalidRequest() throws Exception {
		CepManager manager = mock(CepManager.class);
		CepManagerFactory.setCepManager(manager);

		new GcmReceiver().handle(getContext(), getIntent());

		verify(manager).unregisterClientId(CLIENTID);
		assertEquals(0, receiverCount);
	}


	private Intent getIntent() {
		Intent intent = new Intent();
		intent.putExtra("client", CLIENTID);
		intent.putExtra("event", EVENTID);
		return intent;
	}

}
