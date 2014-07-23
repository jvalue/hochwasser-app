package de.bitdroid.flooding.alarms;


interface AlarmUpdateListener {

	public void onNewAlarm(Alarm alarm);
	public void onDeletedAlarm(Alarm alarm);

}
