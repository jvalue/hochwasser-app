package de.bitdroid.flooding.alarms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import junit.framework.TestCase;

public class LevelAlarmTest extends TestCase {

	private static final ObjectMapper mapper = new ObjectMapper();

	public void testGet() {
		String river = "someRiver";
		String station = "someStation";
		double level = 42;
		boolean alarmWhenAbove = true;
		LevelAlarm alarm = new LevelAlarm(river, station, level, alarmWhenAbove);

		assertEquals(river, alarm.getRiver());
		assertEquals(station, alarm.getStation());
		assertEquals(level, alarm.getLevel());
		assertEquals(alarmWhenAbove, alarm.getAlarmWhenAbove());
	}


	public void testEquals() {
		LevelAlarm alarm1 = new LevelAlarm("river", "station", 42, true);
		LevelAlarm alarm2 = new LevelAlarm("river", "station", 42, true);
		LevelAlarm alarm3 = new LevelAlarm("river", "station", 41, true);
		LevelAlarm alarm4 = new LevelAlarm("otherRiver", "station", 42, true);


		assertTrue(alarm1.equals(alarm2));
		assertFalse(alarm1.equals(alarm3));
		assertFalse(alarm1.equals(alarm4));

		assertTrue(alarm1.hashCode() == alarm2.hashCode());
		assertFalse(alarm1.hashCode() == alarm3.hashCode());
		assertFalse(alarm1.hashCode() == alarm4.hashCode());
	}


	public void testJsonConversion() throws Exception {
		LevelAlarm alarm = new LevelAlarm("river", "station", 42, true);
		JsonNode json = mapper.valueToTree(alarm);
		assertEquals(alarm, mapper.treeToValue(json, Alarm.class));
	}

}
