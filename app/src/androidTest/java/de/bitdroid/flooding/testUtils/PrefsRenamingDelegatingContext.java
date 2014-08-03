package de.bitdroid.flooding.testUtils;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.test.RenamingDelegatingContext;
import android.test.mock.MockContext;

public class PrefsRenamingDelegatingContext extends RenamingDelegatingContext {

	private final Context targetContext;
	private final String prefix;

	public PrefsRenamingDelegatingContext(Context targetContext, String prefix) {
		// this(new MockDelegatingContext(targetContext), prefix);
		super(targetContext, prefix);
		this.targetContext = targetContext;
		this.prefix = prefix;
	}


	public PrefsRenamingDelegatingContext(MockDelegatingContext targetContext, String prefix) {
		super(new MockDelegatingContext(targetContext), prefix);
		this.targetContext = targetContext;
		this.prefix = prefix;
	}


	@Override
	public SharedPreferences getSharedPreferences(String prefsName, int mode) {
		if (prefsName.contains(prefix)) super.getSharedPreferences(prefsName, mode);
		return super.getSharedPreferences(prefix + prefsName, mode);
	}


	@Override
	public Context getApplicationContext() {
		return this;
	}



	String getPrefix() {
		return prefix;
	}


	Context getTargetContext() {
		return targetContext;
	}


	private static final class MockDelegatingContext extends MockContext {

		private final Context context;

		public MockDelegatingContext(Context context) {
			this.context = context;
		}


		@Override
		public SharedPreferences getSharedPreferences(String prefsName, int mode) {
			return context.getSharedPreferences(prefsName, mode);
		}

		@Override
		public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
			return context.registerReceiver(receiver, filter);
		}

		@Override
		public void unregisterReceiver(BroadcastReceiver receiver) {
			context.unregisterReceiver(receiver);
		}

		@Override
		public String getPackageName() {
			return context.getPackageName();
		}

	}
}
