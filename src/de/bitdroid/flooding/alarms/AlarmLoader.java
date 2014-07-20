package de.bitdroid.flooding.alarms;

import java.util.Map;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;


final class AlarmLoader extends AsyncTaskLoader<Map<Long,Alarm>> implements AlarmUpdateListener {

	private final AlarmManager alarmManager;
	private Map<Long, Alarm> alarms;
	private boolean monitoringAlarms = false;

	public AlarmLoader(Context context) {
		super(context);
		this.alarmManager = AlarmManager.getInstance(context);
	}


	@Override
	public Map<Long, Alarm> loadInBackground() {
		return alarmManager.getAll();
	}


	@Override
	public void deliverResult(Map<Long, Alarm> alarms) {
		if (isReset()) return;

		this.alarms = alarms;

		if (isStarted()) {
			super.deliverResult(alarms);
		}
	}


	@Override
	protected void onStartLoading() {
		if (alarms != null) deliverResult(alarms);

		if (!monitoringAlarms) {
			monitoringAlarms = true;
			alarmManager.registerListener(this);
		}

		if (takeContentChanged() || alarms == null) forceLoad();
	}


	@Override
	protected void onStopLoading() {
		cancelLoad();
	}


	@Override
	public void onCanceled(Map<Long, Alarm> alarms) {
		super.onCanceled(alarms);
	}


	@Override
	protected void onReset() {
		onStopLoading();
		alarms = null;

		if (monitoringAlarms) {
			monitoringAlarms = false;
			alarmManager.unregisterListener(this);
		}
	}


	@Override
	public void onNewAlarm(long id, Alarm alarm) {
		onContentChanged();
	}


	@Override
	public void onDeletedAlarm(long id, Alarm alarm) {
		onContentChanged();
	}

}
