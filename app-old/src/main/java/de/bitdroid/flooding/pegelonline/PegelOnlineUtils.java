package de.bitdroid.flooding.pegelonline;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

import de.bitdroid.flooding.utils.Assert;


public final class PegelOnlineUtils {

	private PegelOnlineUtils() { }


	private static final String relativeCmUnit = "cm";
	private static final Set<String> absoluteMeterUnits = new HashSet<String>();
	static {
		absoluteMeterUnits.add("m+nn");
		absoluteMeterUnits.add("m+nhn");
		absoluteMeterUnits.add("m+hn");
		absoluteMeterUnits.add("m+pnp");
		absoluteMeterUnits.add("nhn+m");
		absoluteMeterUnits.add("pnp+m");
		absoluteMeterUnits.add("hn+m");
		absoluteMeterUnits.add("m. \u00FC. nhn");
		absoluteMeterUnits.add("m. \u00FC. nn");
		absoluteMeterUnits.add("m. \u00FC. hn");
		absoluteMeterUnits.add("m. \u00FC. pnp");
	}

	private static final DateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ");


	public static double toCm(
			double value,
			String unit) {

		Assert.assertNotNull(unit);

		unit = unit.toLowerCase();
		
		if (unit.equals(relativeCmUnit)) return value;
		else if (absoluteMeterUnits.contains(unit)) return value * 100;
		else throw new IllegalArgumentException("Unknown unit " + unit);
	}


	public static boolean isRelativeCmUnit(String unit) {
		return unit.toLowerCase().equals(relativeCmUnit);
	}

	public static long parseStringTimestamp(String stringTimestamp) {
		Assert.assertNotNull(stringTimestamp);
		try {
			return dateParser.parse(stringTimestamp).getTime();
		} catch (ParseException pe) {
			throw new IllegalArgumentException(pe);
		}
	}
}
