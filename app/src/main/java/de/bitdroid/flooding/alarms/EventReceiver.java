package de.bitdroid.flooding.alarms;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Set;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.news.NewsManager;
import de.bitdroid.flooding.ods.cep.BaseEventReceiver;
import de.bitdroid.flooding.ods.cep.CepManagerFactory;
import de.bitdroid.flooding.utils.Log;
import de.bitdroid.flooding.utils.StringUtils;


public final class EventReceiver extends BaseEventReceiver {

	private static final String
		EXTRA_EPL_STMT = "EXTRA_EPL_STMT",
		EXTRA_ALARM = "EXTRA_ALARM";

	private static final ObjectMapper mapper = new ObjectMapper();

	@Override
	protected void onReceive(Context context, String eplStmt, String eventId) {
		Log.debug("Received event with id " + eventId);

		Intent intent = new Intent(context, EventIntentService.class);
		intent.putExtra(EXTRA_EPL_STMT, eplStmt);
		context.startService(intent);
	}


	public static final class EventIntentService extends IntentService {

		private static final EplStmtCreator stmtCreator = new EplStmtCreator();

		public EventIntentService() {
			super(EventIntentService.class.getSimpleName());
		}

		@Override
		protected void onHandleIntent(Intent intent) {
			String eplStmt = intent.getStringExtra(EXTRA_EPL_STMT);

			// find matching alarm and show news
			Set<Alarm> alarms = AlarmManager.getInstance(getApplicationContext()).getAll();
			for (Alarm alarm : alarms) {
				if (alarm.accept(stmtCreator, null).equals(eplStmt)) {
					String json = mapper.valueToTree(alarm).toString();
					Intent newsIntent = new Intent(this, EventToNewsReceiver.class);
					newsIntent.putExtra(EXTRA_ALARM, json);
					sendBroadcast(newsIntent);
					return;
				}
			}

			// if no alarm was found for epl stmt, then there was an error
			// when last trying to unregister it --> retry now
			Intent newsIntent = new Intent(this, EventToNewsReceiver.class);
			newsIntent.putExtra(EXTRA_EPL_STMT, eplStmt);
			sendBroadcast(newsIntent);
		}
	}


	public static final class EventToNewsReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String alarmJson = intent.getStringExtra(EXTRA_ALARM);
			String eplStmt = intent.getStringExtra(EXTRA_EPL_STMT);

			// check or error while unregistering
			if (alarmJson == null && eplStmt != null) {
				CepManagerFactory.createCepManager(context).unregisterEplStmt(eplStmt);

			// show news
			} else {
				LevelAlarm alarm = null;
				try {
					alarm = mapper.treeToValue(
							mapper.readTree(alarmJson), 
							LevelAlarm.class);
				} catch (Exception e) {
					Log.error("failed to recreate alarm", e);
				}

				String location = 
					StringUtils.toProperCase(alarm.getRiver()) 
					+ " - " 
					+ StringUtils.toProperCase(alarm.getStation());

				String title = context.getString(
						R.string.alarms_triggered_title,
						location);

				String relation = alarm.getAlarmWhenAbove() 
					? context.getString(R.string.alarms_new_trigger_above)
					: context.getString(R.string.alarms_new_trigger_below);

				String msg = context.getString(
						R.string.alarms_triggered_msg,
						location,
						relation,
						alarm.getLevel());

				NewsManager.getInstance(context).addItem(title, msg, 1, true);
			}
		}
	}

}
