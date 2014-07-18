package de.bitdroid.flooding.alarms;


abstract class Alarm {

	protected Alarm() { }

	public abstract <P,R> R accept(AlarmVisitor<P,R> visitor, P param);

}
