package de.bitdroid.flooding.ods;


import com.google.common.base.Objects;

/**
 * Model class for one body of water (usually a river).
 */
public class BodyOfWater {

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
