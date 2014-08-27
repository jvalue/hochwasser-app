package de.bitdroid.flooding.alarms;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;

import de.bitdroid.ods.cep.RuleManager;
import de.bitdroid.ods.cep.RuleManagerFactory;
import de.bitdroid.ods.cep.Rule;
import de.bitdroid.ods.gcm.GcmStatus;

public final class AlarmRegistrationQueue extends Service {

	static final String
			EXTRA_RULE = "EXTRA_RULE",
			EXTRA_REGISTER = "EXTRA_REGISTER";

	private RuleManager ruleManager;
	private BroadcastReceiver statusReceiver;


	@Override
	public void onCreate() {
		super.onCreate();

		ruleManager = RuleManagerFactory.createRuleManager(this);
		statusReceiver = new StatusReceiver();
		registerReceiver(statusReceiver, new IntentFilter(RuleManager.ACTION_REGISTRATION_STATUS_CHANGED));
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			Rule rule = intent.getParcelableExtra(EXTRA_RULE);
			boolean register = intent.getBooleanExtra(EXTRA_REGISTER, false);
			GcmStatus status = ruleManager.getRegistrationStatus(rule);

			if (!status.equals(GcmStatus.PENDING_UNREGISTRATION) && !status.equals(GcmStatus.PENDING_REGISTRATION)) {
				// if currently not pending simply execute the command
				processRule(ruleManager, rule, register);
			} else {
				// otherwise store it for late (the next broadcast)
				SharedPreferences.Editor editor = getSharedPreferences(this).edit();
				editor.putBoolean(rule.getUuid(), register);
				editor.commit();
			}
		}

		return Service.START_STICKY;
	}


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(statusReceiver);
	}



	private static void processRule(RuleManager ruleManager, Rule rule, boolean start) {
		if (start) ruleManager.registerRule(rule);
		else ruleManager.unregisterRule(rule);
	}


	private static final String PREFS_NAME = AlarmRegistrationQueue.class.getName();

	private static SharedPreferences getSharedPreferences(Context context) {
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}


	public final class StatusReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Rule rule = intent.getParcelableExtra(RuleManager.EXTRA_RULE);
			GcmStatus status = GcmStatus.valueOf(intent.getStringExtra(RuleManager.EXTRA_STATUS));

			// if pending then don't start / stop any new rules
			if (status.equals(GcmStatus.PENDING_REGISTRATION) || status.equals(GcmStatus.PENDING_UNREGISTRATION))
				return;

			// otherwise check if others should be started / stopped
			SharedPreferences prefs = getSharedPreferences(context);
			if (prefs.contains(rule.getUuid())) {
				boolean register = prefs.getBoolean(rule.getUuid(), false);
				if (( register && status.equals(GcmStatus.REGISTERED))
						|| (!register && status.equals(GcmStatus.UNREGISTERED))) {
					// nothing to do here
					return;
				}
				processRule(RuleManagerFactory.createRuleManager(context), rule, register);
			}
		}

	}

}
