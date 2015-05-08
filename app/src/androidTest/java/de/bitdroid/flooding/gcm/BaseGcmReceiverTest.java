package de.bitdroid.flooding.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.test.AndroidTestCase;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BaseGcmReceiverTest extends AndroidTestCase {

	private int counter;

	@Override
	public void setUp() {
		counter = 0;
	}

	public void testDeligationSuccess() {
		BroadcastReceiver receiver = new BaseGcmReceiver() {
			@Override
			protected Set<String> getRequiredExtras() {
				return new HashSet<String>();
			}

			@Override
			protected void handle(Context context, Intent intent) {
				counter++;
			}
		};

		receiver.onReceive(getContext(), getGcmIntent());
		assertEquals(1, counter);
	}


	public void testDeligationFailure() {
		BroadcastReceiver receiver = new BaseGcmReceiver() {
			@Override
			protected Set<String> getRequiredExtras() {
				return new HashSet<String>(Arrays.asList("test"));
			}

			@Override
			protected void handle(Context context, Intent intent) {
				fail();
			}
		};

		receiver.onReceive(getContext(), getGcmIntent());
		assertEquals(0, counter);
	}


	private Intent getGcmIntent() {
		Intent intent = new Intent("com.google.android.c2dm.intent.RECEIVE");
		intent.putExtras(new Bundle());
		return intent;
	}

}
