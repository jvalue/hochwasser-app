package de.bitdroid.flooding.ods;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.base.Objects;

/**
 * Model class for one body of water (usually a river).
 */
public class BodyOfWater implements Parcelable {

	private final String name;
	private final int stationCount;

	public BodyOfWater(String name, int stationCount) {
		this.name = name;
		this.stationCount = stationCount;
	}

	public String getName() {
		return name;
	}

	public int getStationCount() {
		return stationCount;
	}

	protected BodyOfWater(Parcel in) {
		this.name = in.readString();
		this.stationCount = in.readInt();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeInt(stationCount);
	}

	@SuppressWarnings("unused")
	public static final Parcelable.Creator<BodyOfWater> CREATOR = new Parcelable.Creator<BodyOfWater>() {
		@Override
		public BodyOfWater createFromParcel(Parcel in) {
			return new BodyOfWater(in);
		}

		@Override
		public BodyOfWater[] newArray(int size) {
			return new BodyOfWater[size];
		}
	};

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		BodyOfWater bodyOfWater = (BodyOfWater) o;
		return Objects.equal(name, bodyOfWater.name)
				&& Objects.equal(stationCount, bodyOfWater.stationCount);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name, stationCount);
	}
}
