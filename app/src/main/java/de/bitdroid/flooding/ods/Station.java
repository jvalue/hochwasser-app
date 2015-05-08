package de.bitdroid.flooding.ods;


import com.google.common.base.Objects;

/**
 * Model class for meta data of one station.
 */
public class Station {

	private final String uuid;
	private final String stationName;
	private final BodyOfWater bodyOfWater;
	private final float latitude, longitude;
	private final float riverKm;

	public Station(
			String uuid,
			String stationName,
			BodyOfWater bodyOfWater,
			float latitude, float longitude,
			float riverKm) {

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

	public float getLatitude() {
		return latitude;
	}

	public float getLongitude() {
		return longitude;
	}

	public float getRiverKm() {
		return riverKm;
	}

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

	public static class Builder {
		private String uuid;
		private String stationName;
		private BodyOfWater bodyOfWater;
		private float latitude;
		private float longitude;
		private float riverKm;

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
