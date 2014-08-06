package de.bitdroid.ods.cep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.test.AndroidTestCase;

public class BaseEventReceiverTest extends AndroidTestCase {

	private static final String
			EPL_STMT = "select * from *",
			EVENT_ID = "eventId";

	private int callCounter = 0;

	public void testParameters() {
		BroadcastReceiver receiver = new BaseEventReceiver() {

			@Override
			public void onReceive(Context context, String eplStmt, String eventId) {
				assertNotNull(context);
				assertEquals(EPL_STMT, eplStmt);
				assertEquals(EVENT_ID, eventId);
				callCounter++;
			}

		};

		Intent intent = new Intent();
		intent.putExtra(BaseEventReceiver.EXTRA_EPLSTMT, EPL_STMT);
		intent.putExtra(BaseEventReceiver.EXTRA_EVENTID, EVENT_ID);

		receiver.onReceive(getContext(), intent);
		assertEquals(1, callCounter);
	}

}
