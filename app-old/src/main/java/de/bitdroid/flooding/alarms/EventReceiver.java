package de.bitdroid.flooding.alarms;

import android.content.Context;
import android.os.Vibrator;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.news.NewsManager;
import de.bitdroid.flooding.ceps.BaseEventReceiver;
import de.bitdroid.flooding.ceps.RuleManager;
import de.bitdroid.flooding.ceps.RuleManagerFactory;
import de.bitdroid.flooding.ceps.Rule;
import de.bitdroid.flooding.gcm.GcmStatus;
import de.bitdroid.flooding.utils.StringUtils;
import timber.log.Timber;


public final class EventReceiver extends BaseEventReceiver {

	@Override
	protected void onReceive(Context context, Rule rule, String eventId) {
		Timber.d("Received event with id " + eventId);

		LevelAlarm alarm = new LevelAlarm(rule);

		// retry unregistration if necessary
		RuleManager manager = RuleManagerFactory.createRuleManager(context);
		if (manager.getRegistrationStatus(alarm.getRule()).equals(GcmStatus.ERROR_UNREGISTRATION)) {
			manager.unregisterRule(alarm.getRule());
			return;
		}

		// show news
		String station = StringUtils.toProperCase(alarm.getStation());
		String river = StringUtils.toProperCase(alarm.getRiver());

		String title = context.getString(R.string.alarms_triggered_title, station);

		String relation = alarm.getAlarmWhenAbove()
				? context.getString(R.string.alarms_triggered_msg_above)
				: context.getString(R.string.alarms_triggered_msg_below);

		String msg = context.getString(
				R.string.alarms_triggered_msg,
				station + " (" + river + ")",
				relation,
				alarm.getLevel());

		// show notification and pass to news manager
		NewsManager.getInstance(context).addItem(title, msg, 1, true, true);

		// vibrate
		Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(400);
	}

}
