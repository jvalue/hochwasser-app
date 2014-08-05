package de.bitdroid.flooding.map;


final class Station {

	private final String name;
	private final double lat, lon;

	public Station(String name, double lat, double lon) {
		this.name = name;
		this.lat = lat;
		this.lon = lon;
	}

	public String getName() {
		return name;
	}


	public double getLat() {
		return lat;
	}


	public double getLon() {
		return lon;
	}

}
