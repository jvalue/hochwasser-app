package de.bitdroid.flooding.ods;

import com.google.common.base.Objects;

public class Measurement {

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
}
