package de.bitdroid.flooding.alarms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import junit.framework.TestCase;

public class AlarmTest extends TestCase {

	private static final ObjectMapper mapper = new ObjectMapper();

	public void testJsonConversion() throws Exception {
		Alarm alarm = new DummyAlarm();
		JsonNode json = mapper.valueToTree(alarm);
		assertNotNull(mapper.treeToValue(json, DummyAlarm.class));
	}


	private static final class DummyAlarm extends Alarm {
		@Override
		public <P,R> R accept(AlarmVisitor<P,R> visitor, P param) {
			return null;
		}
	}

}
