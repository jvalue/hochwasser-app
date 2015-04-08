package de.bitdroid.ods.cep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.test.AndroidTestCase;

public class BaseEventReceiverTest extends AndroidTestCase {

	private static final String
			EVENT_ID = "eventId";
	private static final Rule
			RULE = new Rule.Builder("theBestPath").parameter("key1", "value1").build();

	private int callCounter = 0;

	public void testParameters() {
		BroadcastReceiver receiver = new BaseEventReceiver() {

			@Override
			public void onReceive(Context context, Rule rule, String eventId) {
				assertNotNull(context);
				assertEquals(RULE, rule);
				assertEquals(EVENT_ID, eventId);
				callCounter++;
			}

		};

		Intent intent = new Intent();
		intent.putExtra(BaseEventReceiver.EXTRA_RULE, RULE);
		intent.putExtra(BaseEventReceiver.EXTRA_EVENT_ID, EVENT_ID);

		receiver.onReceive(getContext(), intent);
		assertEquals(1, callCounter);
	}

}
