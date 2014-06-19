package de.bitdroid.flooding.map;


final class Station {

	private final String name;
	private final double km, lat, lon;

	public Station(String name, double km, double lat, double lon) {
		this.name = name;
		this.km = km;
		this.lat = lat;
		this.lon = lon;
	}

	public String getName() {
		return name;
	}


	public double getKm() {
		return km;
	}


	public double getLat() {
		return lat;
	}


	public double getLon() {
		return lon;
	}

}
