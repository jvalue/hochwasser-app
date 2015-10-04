package de.bitdroid.flooding.gcm;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Vibrator;

import java.util.List;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.ceps.Alarm;
import de.bitdroid.flooding.ceps.CepsManager;
import de.bitdroid.flooding.network.NetworkUtils;
import de.bitdroid.flooding.news.NewsManager;
import de.bitdroid.flooding.ui.NewsFragment;
import de.bitdroid.flooding.utils.StringUtils;
import roboguice.service.RoboService;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * Processes GCM messages.
 */
public class GcmService extends RoboService {

	private static final String ARGUMENT_REGISTRATION_ID = "client";

	@Inject private CepsManager cepsManager;
	@Inject private NewsManager newsManager;
	@Inject private NetworkUtils networkUtils;
	private int runningDownloadsCount = 0; // indicates how many async operations are currently running. Used to stop this service.


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		final String registrationId = intent.getExtras().getString(ARGUMENT_REGISTRATION_ID);
		Timber.d("received registration id " + registrationId);

		++runningDownloadsCount;

		cepsManager.getAlarms()
				.compose(networkUtils.<List<Alarm>>getDefaultTransformer())
				.subscribe(new Action1<List<Alarm>>() {
					@Override
					public void call(List<Alarm> alarms) {
						assertProperShutdown();

						Alarm triggeredAlam = null;
						for (Alarm alarm : alarms) {
							if (alarm.getId().equals(registrationId)) triggeredAlam = alarm;
						}
						if (triggeredAlam == null) {
							Timber.e("failed to find alarm with id " + registrationId);
							return;
						}

						onTriggeredAlarm(triggeredAlam);
					}
				}, new Action1<Throwable>() {
					@Override
					public void call(Throwable throwable) {
						assertProperShutdown();
						Timber.e(throwable, "failed to download alarms");
					}
				});

		return START_STICKY;
	}


	private void assertProperShutdown() {
		--runningDownloadsCount;
		if (runningDownloadsCount == 0) stopSelf();
	}


	private void onTriggeredAlarm(Alarm alarm) {
		// TODO
		Timber.i("triggering alarm " + alarm.getId());

		// show news
		String station = StringUtils.toProperCase(alarm.getStation().getStationName());
		String river = StringUtils.toProperCase(alarm.getStation().getBodyOfWater().getName());

		String title = getString(R.string.alarms_triggered_title, station);

		String relation = alarm.isAlarmWhenAboveLevel()
				? getString(R.string.alarms_triggered_msg_above)
				: getString(R.string.alarms_triggered_msg_below);

		String msg = getString(
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
