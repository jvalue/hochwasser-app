package de.bitdroid.flooding.ods;

import android.os.Parcel;
import android.os.Parcelable;

import org.roboguice.shaded.goole.common.base.Objects;

import de.bitdroid.flooding.utils.PegelOnlineUtils;


public class Measurement implements  Parcelable {

	private final float value;
	private final String unit;

	public Measurement(float value, String unit) {
		this.value = value;
		this.unit = unit;
	}

	public float getValue() {
		return value;
	}

	public String getUnit() {
		return unit;
	}

	public float getValueInCm() {
		return PegelOnlineUtils.toCm(value, unit);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Measurement that = (Measurement) o;
		return Objects.equal(value, that.value) &&
				Objects.equal(unit, that.unit);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(value, unit);
	}

	protected Measurement(Parcel in) {
		value = in.readFloat();
		unit = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeFloat(value);
		dest.writeString(unit);
	}

	@SuppressWarnings("unused")
	public static final Parcelable.Creator<Measurement> CREATOR = new Parcelable.Creator<Measurement>() {
		@Override
		public Measurement createFromParcel(Parcel in) {
			return new Measurement(in);
		}

		@Override
		public Measurement[] newArray(int size) {
			return new Measurement[size];
		}
	};
}
