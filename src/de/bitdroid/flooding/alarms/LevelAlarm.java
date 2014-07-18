package de.bitdroid.flooding.alarms;

import de.bitdroid.flooding.utils.Assert;


final class LevelAlarm extends Alarm {

	private final String river, station;
	private final double level;
	private final boolean alarmWhenAbove;

	public LevelAlarm(
			String river, 
			String station, 
			double level, 
			boolean alarmWhenAbove) {

		Assert.assertNotNull(river, station);
		this.river = river;
		this.station = station;
		this.level = level;
		this.alarmWhenAbove = alarmWhenAbove;
	}


	public String getRiver() {
		return river;
	}


	public String getStation() {
		return station;
	}


	public double getLevel() {
		return level;
	}


	public boolean getAlarmWhenAbove() {
		return alarmWhenAbove;
	}


	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof LevelAlarm)) return false;
		if (other == this) return true;
		LevelAlarm alarm = (LevelAlarm) other;
		return river.equals(alarm.river)
			&& station.equals(alarm.station)
			&& level == alarm.level
			&& alarmWhenAbove == alarm.alarmWhenAbove;
	}


	@Override
	public int hashCode() {
		final int MULT = 17;
		int hash = 13;
		hash = hash + MULT * river.hashCode();
		hash = hash + MULT * station.hashCode();
		hash = hash + MULT * Double.valueOf(river).hashCode();
		hash = hash + MULT * Boolean.valueOf(river).hashCode();
		return hash;
	}


	@Override
	public <P,R> R accept(AlarmVisitor<P,R> visitor, P param) {
		return visitor.visit(this, param);
	}

}
