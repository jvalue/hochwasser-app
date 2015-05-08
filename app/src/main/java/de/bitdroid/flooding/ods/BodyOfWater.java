package de.bitdroid.flooding.ods;


import com.google.common.base.Objects;

/**
 * Model class for one body of water (usually a river).
 */
public class BodyOfWater {

	private final String name;

	public BodyOfWater(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		BodyOfWater bodyOfWater = (BodyOfWater) o;
		return Objects.equal(name, bodyOfWater.name);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name);
	}
}
