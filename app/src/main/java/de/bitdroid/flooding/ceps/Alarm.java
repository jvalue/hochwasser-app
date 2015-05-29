package de.bitdroid.flooding.ceps;


import com.google.common.base.Objects;

import de.bitdroid.flooding.ods.Station;

/**
 * One alarm which has been registered with the CEPS.
 */
public class Alarm {

	private final String id;
	private final Station station;
	private final double level;
	private final boolean alarmWhenAboveLevel;

	public Alarm(String id, Station station, double level, boolean alarmWhenAboveLevel) {
		this.id = id;
		this.station = station;
		this.level = level;
		this.alarmWhenAboveLevel = alarmWhenAboveLevel;
	}

	public String getId() {
		return id;
	}

	public Station getStation() {
		return station;
	}

	public double getLevel() {
		return level;
	}

	public boolean isAlarmWhenAboveLevel() {
		return alarmWhenAboveLevel;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Alarm alarm = (Alarm) o;
		return Objects.equal(level, alarm.level) &&
				Objects.equal(alarmWhenAboveLevel, alarm.alarmWhenAboveLevel) &&
				Objects.equal(id, alarm.id) &&
				Objects.equal(station, alarm.station);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id, station, level, alarmWhenAboveLevel);
	}


	public static class Builder {

		private String id;
		private Station station;
		private double level;
		private boolean alarmWhenAboveLevel;

		public Builder setId(String id) {
			this.id = id;
			return this;
		}

		public Builder setStation(Station station) {
			this.station = station;
			return this;
		}

		public Builder setLevel(double level) {
			this.level = level;
			return this;
		}

		public Builder setAlarmWhenAboveLevel(boolean alarmWhenAboveLevel) {
			this.alarmWhenAboveLevel = alarmWhenAboveLevel;
			return this;
		}

		public Alarm build() {
			return new Alarm(id, station, level, alarmWhenAboveLevel);
		}
	}

}
