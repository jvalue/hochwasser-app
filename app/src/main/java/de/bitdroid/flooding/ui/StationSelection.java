package de.bitdroid.flooding.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import de.bitdroid.flooding.ods.BodyOfWater;
import de.bitdroid.flooding.ods.Station;

/**
 * Helper methods for passing {@link BodyOfWater} and
 * {@link Station} instances via {@link android.content.Intent}s.
 */
public class StationSelection {

	private static final String
			EXTRA_WATER = "EXTRA_WATER",
			EXTRA_STATION = "EXTRA_STATION";

	private final BodyOfWater water;
	private final Station station;

	public StationSelection(Intent data) {
		this((BodyOfWater) data.getParcelableExtra(EXTRA_WATER), (Station) data.getParcelableExtra(EXTRA_STATION));
	}

	public StationSelection() {
		this(null, null);
	}

	public StationSelection(BodyOfWater water) {
		this(water, null);
	}

	public StationSelection(BodyOfWater water, Station station) {
		this.water = water;
		this.station = station;
	}

	public BodyOfWater getWater() {
		return water;
	}

	public Station getStation() {
		return station;
	}

	public Intent toIntent() {
		return setExtras(new Intent());
	}

	public Intent toIntent(Context context, Class<? extends Activity> target) {
		return setExtras(new Intent(context, target));
	}

	private Intent setExtras(Intent intent) {
		intent.putExtra(EXTRA_WATER, water);
		intent.putExtra(EXTRA_STATION, station);
		return intent;
	}

}
