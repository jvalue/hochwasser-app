package de.bitdroid.ods.cep;

import android.content.Context;
import android.content.Intent;

import org.mockito.ArgumentCaptor;

import de.bitdroid.testUtils.BaseAndroidTestCase;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GcmReceiverTest extends BaseAndroidTestCase {

	private static final String
			CLIENTID = "someClientId",
			EVENTID = "someEventId";

	private static final Rule RULE = new Rule.Builder("theBestPath").parameter("key1", "value1").build();


	@Override
	public void beforeClass() {
		// some love potion for our two friends: dexmaker and mockito
		System.setProperty("dexmaker.dexcache", getContext().getCacheDir().getPath());
	}


	public void testValidEvent() throws Exception {
		CepManager mockManager = mock(CepManager.class);
		when(mockManager.getRuleForClientId(CLIENTID)).thenReturn(RULE);
		CepManagerFactory.setCepManager(mockManager);

		Context mockContext = mock(Context.class);

		new GcmReceiver().handle(mockContext, getIntent());

		ArgumentCaptor<Intent> captor = ArgumentCaptor.forClass(Intent.class);
		verify(mockContext).sendBroadcast(captor.capture());

		Intent broadcastIntent = captor.getValue();
		assertEquals(RULE, broadcastIntent.getParcelableExtra(BaseEventReceiver.EXTRA_RULE));
		assertEquals(EVENTID, broadcastIntent.getStringExtra(BaseEventReceiver.EXTRA_EVENT_ID));
	}


	public void testInvalidRequest() throws Exception {
		CepManager mockManager = mock(CepManager.class);
		CepManagerFactory.setCepManager(mockManager);

		Context mockContext = mock(Context.class);

		new GcmReceiver().handle(mockContext, getIntent());

		verify(mockManager).unregisterClientId(CLIENTID);
		verify(mockContext, never()).sendBroadcast(any(Intent.class));
	}


	private Intent getIntent() {
		Intent intent = new Intent();
		intent.putExtra("client", CLIENTID);
		intent.putExtra("event", EVENTID);
		return intent;
	}

}
