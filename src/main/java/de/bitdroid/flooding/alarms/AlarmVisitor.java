package de.bitdroid.flooding.alarms;


interface AlarmVisitor<P, R> {

	public R visit(LevelAlarm alarm, P param);

}
