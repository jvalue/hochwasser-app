package de.bitdroid.flooding.alarms;


import android.app.Activity;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.ceps.RuleManager;

/* Should only be used from the main thread! */
public final class AlarmDeregistrationQueue {

	private final Activity activity;
	private final RuleManager ruleManager;

	private final Timer timer = new Timer();
	private final Map<LevelAlarm, TimerTask> tasks  = new HashMap<LevelAlarm, TimerTask>();

	public AlarmDeregistrationQueue(Activity activity, RuleManager ruleManager) {
		this.activity = activity;
		this.ruleManager = ruleManager;
	}


	public void unregister(final LevelAlarm alarm) {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// synchronized on main thread
						if (!tasks.containsKey(alarm)) return;
						tasks.remove(alarm);
						ruleManager.unregisterRule(alarm.getRule());
					}
				});
			}
		};
		tasks.put(alarm, task);
		timer.schedule(task, (long) (activity.getResources().getInteger(R.integer.list_card_undobar_hide_delay) * 1.2));
	}


	public void cancelDeregistration(LevelAlarm alarm) {
		TimerTask task = tasks.remove(alarm);
		task.cancel();
	}


	public void executeAllAndShutdown() {
		timer.cancel();
		for (LevelAlarm alarm: tasks.keySet()) {
			ruleManager.unregisterRule(alarm.getRule());
		}
		tasks.clear();
	}


}
