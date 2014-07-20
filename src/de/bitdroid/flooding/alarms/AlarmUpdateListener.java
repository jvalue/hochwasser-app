package de.bitdroid.flooding.alarms;


interface AlarmUpdateListener {

	public void onNewAlarm(long id, Alarm alarm);
	public void onDeletedAlarm(long id, Alarm alarm);

}
