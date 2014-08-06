package de.bitdroid.flooding.alarms;

import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;

import de.bitdroid.ods.cep.CepManager;


final class AlarmLoader extends AsyncTaskLoader<Set<Alarm>> implements AlarmUpdateListener {

	private final AlarmManager alarmManager;
	private final BroadcastReceiver registrationListener;

	private Set<Alarm> alarms;
	private boolean monitoringAlarms = false;

	public AlarmLoader(Context context) {
		super(context);
		this.alarmManager = AlarmManager.getInstance(context);

		this.registrationListener = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				onContentChanged();
			}
		};
	}


	@Override
	public Set<Alarm> loadInBackground() {
		return alarmManager.getAll();
	}


	@Override
	public void deliverResult(Set<Alarm> alarms) {
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
			IntentFilter filter = new IntentFilter(CepManager.ACTION_REGISTRATION_STATUS_CHANGED);
			getContext().registerReceiver(registrationListener, filter);
		}

		if (takeContentChanged() || alarms == null) forceLoad();
	}


	@Override
	protected void onStopLoading() {
		cancelLoad();
	}


	@Override
	public void onCanceled(Set<Alarm> alarms) {
		super.onCanceled(alarms);
	}


	@Override
	protected void onReset() {
		onStopLoading();
		alarms = null;

		if (monitoringAlarms) {
			monitoringAlarms = false;
			alarmManager.unregisterListener(this);
			getContext().unregisterReceiver(registrationListener);
		}
	}


	@Override
	public void onNewAlarm(Alarm alarm) {
		onContentChanged();
	}


	@Override
	public void onDeletedAlarm(Alarm alarm) {
		onContentChanged();
	}

}
