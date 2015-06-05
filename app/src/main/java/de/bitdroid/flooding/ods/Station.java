package de.bitdroid.flooding.ods;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.base.Objects;

/**
 * Model class for meta data of one station.
 */
public class Station implements Parcelable {

	private static final float NO_VALUE = -999f;

	private final String uuid;
	private final String stationName;
	private final BodyOfWater bodyOfWater;
	private final Float latitude, longitude;
	private final Float riverKm;

	public Station(
			String uuid,
			String stationName,
			BodyOfWater bodyOfWater,
			Float latitude, Float longitude,
			Float riverKm) {

		this.uuid = uuid;
		this.stationName = stationName;
		this.bodyOfWater = bodyOfWater;
		this.latitude = latitude;
		this.longitude = longitude;
		this.riverKm = riverKm;
	}

	public String getUuid() {
		return uuid;
	}

	public String getStationName() {
		return stationName;
	}

	public BodyOfWater getBodyOfWater() {
		return bodyOfWater;
	}

	public Float getLatitude() {
		return latitude;
	}

	public Float getLongitude() {
		return longitude;
	}

	public Float getRiverKm() {
		return riverKm;
	}

	protected Station(Parcel in) {
		this.uuid = in.readString();
		this.stationName = in.readString();
		this.bodyOfWater = (BodyOfWater) in.readValue(BodyOfWater.class.getClassLoader());
		this.latitude = valueToNull(in.readFloat());
		this.longitude = valueToNull(in.readFloat());
		this.riverKm = valueToNull(in.readFloat());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(uuid);
		dest.writeString(stationName);
		dest.writeValue(bodyOfWater);
		dest.writeFloat(assertValueNotNull(latitude));
		dest.writeFloat(assertValueNotNull(longitude));
		dest.writeFloat(assertValueNotNull(riverKm));
	}

	@SuppressWarnings("unused")
	public static final Parcelable.Creator<Station> CREATOR = new Parcelable.Creator<Station>() {
		@Override
		public Station createFromParcel(Parcel in) {
			return new Station(in);
		}

		@Override
		public Station[] newArray(int size) {
			return new Station[size];
		}
	};

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Station station = (Station) o;
		return Objects.equal(uuid, station.uuid) &&
				Objects.equal(latitude, station.latitude) &&
				Objects.equal(longitude, station.longitude) &&
				Objects.equal(riverKm, station.riverKm) &&
				Objects.equal(stationName, station.stationName) &&
				Objects.equal(bodyOfWater, station.bodyOfWater);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(uuid, stationName, bodyOfWater, latitude, longitude, riverKm);
	}

	private float assertValueNotNull(Float value) {
		if (value == null) return NO_VALUE;
		else return value;
	}


	private Float valueToNull(float value) {
		if (value == NO_VALUE) return null;
		else return value;
	}


	public static class Builder {
		private String uuid;
		private String stationName;
		private BodyOfWater bodyOfWater;
		private Float latitude, longitude;
		private Float riverKm;

		public Builder setUuid(String uuid) {
			this.uuid = uuid;
			return this;
		}

		public Builder setStationName(String stationName) {
			this.stationName = stationName;
			return this;
		}

		public Builder setBodyOfWater(BodyOfWater bodyOfWater) {
			this.bodyOfWater = bodyOfWater;
			return this;
		}

		public Builder setLatitude(float latitude) {
			this.latitude = latitude;
			return this;
		}

		public Builder setLongitude(float longitude) {
			this.longitude = longitude;
			return this;
		}

		public Builder setRiverKm(float riverKm) {
			this.riverKm = riverKm;
			return this;
		}

		public Station build() {
			return new Station(uuid, stationName, bodyOfWater, latitude, longitude, riverKm);
		}
	}
}
