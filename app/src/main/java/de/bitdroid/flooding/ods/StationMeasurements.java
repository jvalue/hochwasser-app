package de.bitdroid.flooding.ods;


import android.os.Parcel;
import android.os.Parcelable;

import org.roboguice.shaded.goole.common.base.Objects;


/**
 * Model class for measurements of a station.
 */
public class StationMeasurements implements Parcelable {

	private static final Long NO_VALUE = -999l;

	private final Station station;
	private final Measurement level;
	private final Long levelTimestamp; // unix timestamp in seconds

	// statistical values are in German
	private final Measurement
			mw, // mittlerer Wasserstand
			mhw, // mittlerer hoechster Wasserstand
			mnw; // mittlerer niedrigster Wasserstand
	private final Measurement
			mtnw, // mittleres Tidenniedrigwasser
			mthw, // mittleres Tidenhochwasser
			hthw, // hoechstes Tidenhochwasser
			ntnw; // niedrigstes Tidenhochwasser

	public StationMeasurements(
			Station station,
			Measurement level, Long levelTimestamp,
			Measurement mw, Measurement mhw, Measurement mnw,
			Measurement mtnw, Measurement mthw, Measurement hthw, Measurement ntnw) {

		this.station = station;
		this.level = level;
		this.levelTimestamp = levelTimestamp;
		this.mw = mw;
		this.mhw = mhw;
		this.mnw = mnw;
		this.mtnw = mtnw;
		this.mthw = mthw;
		this.hthw = hthw;
		this.ntnw = ntnw;
	}

	public Station getStation() {
		return station;
	}

	public Measurement getLevel() {
		return level;
	}

	public Long getLevelTimestamp() {
		return levelTimestamp;
	}

	public Measurement getMw() {
		return mw;
	}

	public Measurement getMhw() {
		return mhw;
	}

	public Measurement getMnw() {
		return mnw;
	}

	public Measurement getMtnw() {
		return mtnw;
	}

	public Measurement getMthw() {
		return mthw;
	}

	public Measurement getHthw() {
		return hthw;
	}

	public Measurement getNtnw() {
		return ntnw;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		StationMeasurements measurement = (StationMeasurements) o;
		return Objects.equal(station, measurement.station) &&
				Objects.equal(levelTimestamp, measurement.levelTimestamp) &&
				Objects.equal(level, measurement.level) &&
				Objects.equal(mw, measurement.mw) &&
				Objects.equal(mhw, measurement.mhw) &&
				Objects.equal(mnw, measurement.mnw) &&
				Objects.equal(mtnw, measurement.mtnw) &&
				Objects.equal(mthw, measurement.mthw) &&
				Objects.equal(hthw, measurement.hthw) &&
				Objects.equal(ntnw, measurement.ntnw);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(station, level, levelTimestamp, mw, mhw, mnw, mtnw, mthw, hthw, ntnw);
	}

	protected StationMeasurements(Parcel in) {
		station = (Station) in.readValue(Station.class.getClassLoader());
		level = (Measurement) in.readValue(Measurement.class.getClassLoader());
		long timestamp = in.readLong();
		if (timestamp == NO_VALUE) levelTimestamp = null;
		else levelTimestamp = timestamp;
		mw = (Measurement) in.readValue(Measurement.class.getClassLoader());
		mhw = (Measurement) in.readValue(Measurement.class.getClassLoader());
		mnw = (Measurement) in.readValue(Measurement.class.getClassLoader());
		mtnw = (Measurement) in.readValue(Measurement.class.getClassLoader());
		mthw = (Measurement) in.readValue(Measurement.class.getClassLoader());
		hthw = (Measurement) in.readValue(Measurement.class.getClassLoader());
		ntnw = (Measurement) in.readValue(Measurement.class.getClassLoader());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeValue(station);
		dest.writeValue(level);
		if (levelTimestamp == null) dest.writeLong(NO_VALUE);
		else dest.writeLong(levelTimestamp);
		dest.writeValue(mw);
		dest.writeValue(mhw);
		dest.writeValue(mnw);
		dest.writeValue(mtnw);
		dest.writeValue(mthw);
		dest.writeValue(hthw);
		dest.writeValue(ntnw);
	}

	@SuppressWarnings("unused")
	public static final Parcelable.Creator<StationMeasurements> CREATOR = new Parcelable.Creator<StationMeasurements>() {
		@Override
		public StationMeasurements createFromParcel(Parcel in) {
			return new StationMeasurements(in);
		}

		@Override
		public StationMeasurements[] newArray(int size) {
			return new StationMeasurements[size];
		}
	};


	public static class Builder {
		private final Station station;
		private Measurement level;
		private Long levelTimestamp;
		private Measurement mw, mhw, mnw;
		private Measurement mtnw, mthw, hthw, ntnw;

		public Builder(Station station) {
			this.station = station;
		}

		public Builder setLevel(Measurement level) {
			this.level = level;
			return this;
		}

		public Builder setLevelTimestamp(long levelTimestamp) {
			this.levelTimestamp = levelTimestamp;
			return this;
		}

		public Builder setMw(Measurement mw) {
			this.mw = mw;
			return this;
		}

		public Builder setMhw(Measurement mhw) {
			this.mhw = mhw;
			return this;
		}

		public Builder setMnw(Measurement mnw) {
			this.mnw = mnw;
			return this;
		}

		public Builder setMtnw(Measurement mtnw) {
			this.mtnw = mtnw;
			return this;
		}

		public Builder setMthw(Measurement mthw) {
			this.mthw = mthw;
			return this;
		}

		public Builder setHthw(Measurement hthw) {
			this.hthw = hthw;
			return this;
		}

		public Builder setNtnw(Measurement ntnw) {
			this.ntnw = ntnw;
			return this;
		}

		public StationMeasurements build() {
			return new StationMeasurements(station, level, levelTimestamp, mw, mhw, mnw, mtnw, mthw, hthw, ntnw);
		}
	}
}
