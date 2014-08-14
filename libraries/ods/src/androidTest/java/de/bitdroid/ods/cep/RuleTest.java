package de.bitdroid.ods.cep;

import android.os.Bundle;

import com.fasterxml.jackson.databind.ObjectMapper;

import junit.framework.TestCase;

public class RuleTest extends TestCase {

	private static final ObjectMapper mapper = new ObjectMapper();

	public void testBuilder() {
		Rule rule = getRule(2);

		assertNotNull(rule.getUuid());
		assertEquals("dummyPath", rule.getCepsRulePath());
		assertEquals("value1", rule.getParams().get("key1"));
		assertEquals("value2", rule.getParams().get("key2"));
	}


	public void testParcel() {
		testParcel(0);
		testParcel(1);
		testParcel(2);
		testParcel(10);
	}


	private void testParcel(int paramCount) {
		Rule rule = getRule(paramCount);
		Bundle bundle = new Bundle();
		bundle.putParcelable("rule", rule);
		assertEquals(rule, bundle.getParcelable("rule"));
	}


	private Rule getRule(int paramCount) {
		Rule.Builder builder =  new Rule.Builder("dummyPath");
		for (int i = 0; i < paramCount; i++) {
			builder.parameter("key" + (i + 1), "value" + (i + 1));
		}
		return builder.build();
	}

}
