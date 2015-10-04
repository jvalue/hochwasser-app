package de.bitdroid.flooding.gcm;

import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;

import java.util.List;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.ceps.Alarm;
import de.bitdroid.flooding.ceps.CepsManager;
import de.bitdroid.flooding.network.NetworkUtils;
import de.bitdroid.flooding.news.NewsManager;
import de.bitdroid.flooding.ods.OdsManager;
import de.bitdroid.flooding.ods.StationMeasurements;
import de.bitdroid.flooding.ui.NewsFragment;
import de.bitdroid.flooding.utils.StringUtils;
import roboguice.service.RoboIntentService;
import timber.log.Timber;

/**
 * Processes GCM messages.
 */
public class GcmService extends RoboIntentService {

	public static final String ARGUMENT_REGISTRATION_ID = "client";

	@Inject private CepsManager cepsManager;
	@Inject private OdsManager odsManager;
	@Inject private NewsManager newsManager;
	@Inject private NetworkUtils networkUtils;


	public GcmService() {
		super(GcmService.class.getSimpleName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		final String registrationId = intent.getExtras().getString(ARGUMENT_REGISTRATION_ID);
		Timber.d("received registration id " + registrationId);

		try {
			List<Alarm> alarms = cepsManager.getAlarms().toBlocking().first();
			Alarm triggeredAlarm = null;
			for (Alarm alarm : alarms) {
				if (alarm.getId().equals(registrationId)) {
					triggeredAlarm = alarm;
					break;
				}
			}
			if (triggeredAlarm == null) {
				Timber.e("failed to find alarm with id " + registrationId);
				GcmBroadcastReceiver.completeWakefulIntent(intent);
				return;
			}

			Timber.i("triggering alarm " + triggeredAlarm.getId());

			if (!(triggeredAlarm.isAlarmWhenAboveLevel() && triggeredAlarm.isAlarmWhenBelowLevel())) {
				showAlarmNotification(triggeredAlarm, triggeredAlarm.isAlarmWhenAboveLevel());

			} else {
				// no info about above or below level, download latest station info to find out
				StationMeasurements measurements = odsManager.getMeasurements(triggeredAlarm.getStation()).toBlocking().first();
				boolean aboveLevel = measurements.getLevel().getValueInCm() > triggeredAlarm.getLevel();
				showAlarmNotification(triggeredAlarm, aboveLevel);
			}
		} catch (Exception e) {
			Timber.e(e, "failed to trigger alarm");
		}

		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}


	private void showAlarmNotification(Alarm alarm, boolean aboveLevel) {
		final String relation = aboveLevel
				? getString(R.string.alarms_triggered_msg_above)
				: getString(R.string.alarms_triggered_msg_below);
		final String station = StringUtils.toProperCase(alarm.getStation().getStationName());
		final String river = StringUtils.toProperCase(alarm.getStation().getBodyOfWater().getName());
		final String title = getString(R.string.alarms_triggered_title, station);
		final String msg = getString(
				R.string.alarms_triggered_msg,
				station + " (" + river + ")",
				relation,
				alarm.getLevel());

		// show notification and pass to news manager
		newsManager.addItem(title, msg, NewsFragment.NAV_ID_ALARMS, true, true, true);

		// vibrate
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(400);
	}

}
