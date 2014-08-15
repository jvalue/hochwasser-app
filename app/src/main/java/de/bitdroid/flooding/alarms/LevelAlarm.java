package de.bitdroid.flooding.alarms;

import de.bitdroid.ods.cep.Rule;
import de.bitdroid.utils.Assert;


/**
 * Wrapper for nicer access to the underlying data.
 */
final class LevelAlarm {

	private static final String
		CEPS_RULE_PATH = "cep/register/de/pegelonline/levelAlarm";

	private static final String
		PARAM_RIVER = "river",
		PARAM_STATION = "station",
		PARAM_LEVEL = "level",
		PARAM_ABOVE = "above";


	private final Rule rule;

	public LevelAlarm(Rule rule) {
		Assert.assertNotNull(rule);
		this.rule = rule;
	}


	public LevelAlarm(String river, String station, double level, boolean alarmWhenAbove) {
		Assert.assertNotNull(river, station);
		this.rule = new Rule.Builder(CEPS_RULE_PATH)
				.parameter(PARAM_RIVER, river)
				.parameter(PARAM_STATION, station)
				.parameter(PARAM_LEVEL, String.valueOf(level))
				.parameter(PARAM_ABOVE, String.valueOf(alarmWhenAbove))
				.build();
	}


	public String getRiver() {
		return rule.getParams().get(PARAM_RIVER);
	}


	public String getStation() {
		return rule.getParams().get(PARAM_STATION);
	}


	public double getLevel() {
		return Double.valueOf(rule.getParams().get(PARAM_LEVEL));
	}


	public boolean getAlarmWhenAbove() {
		return Boolean.valueOf(rule.getParams().get(PARAM_ABOVE));
	}


	public Rule getRule() {
		return rule;
	}


	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof LevelAlarm)) return false;
		if (other == this) return true;
		LevelAlarm alarm = (LevelAlarm) other;
		return rule.equals(alarm.getRule());
	}


	@Override
	public int hashCode() {
		return rule.hashCode();
	}

}
