package de.bitdroid.flooding.map;


public final class Station {

	private final String name;
	private final String river;
	private final double lat, lon;

	public Station(String name, String river, double lat, double lon) {
		this.name = name;
		this.river = river;
		this.lat = lat;
		this.lon = lon;
	}

	public String getName() {
		return name;
	}


	public String getRiver() {
		return river;
	}

	public double getLat() {
		return lat;
	}


	public double getLon() {
		return lon;
	}

}
