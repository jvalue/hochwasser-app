package de.bitdroid.flooding.alarms;

import java.util.Map;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;


final class AlarmLoader extends AsyncTaskLoader<Map<Long,Alarm>> {

	private final Context context;
	private Map<Long, Alarm> alarms;

	public AlarmLoader(Context context) {
		super(context);
		this.context = context;
	}


	@Override
	public Map<Long, Alarm> loadInBackground() {
		return AlarmManager.getInstance(context).getAll();
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
	}

}
