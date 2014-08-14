package de.bitdroid.ods.cep;

import android.os.Bundle;

import com.fasterxml.jackson.databind.ObjectMapper;

import junit.framework.TestCase;

public class RuleTest extends TestCase {

	private static final ObjectMapper mapper = new ObjectMapper();

	public void testBuilder() {
		Rule rule = getRule();

		assertNotNull(rule.getUuid());
		assertEquals("dummyPath", rule.getCepsRulePath());
		assertEquals("value1", rule.getParams().get("key1"));
		assertEquals("value2", rule.getParams().get("key2"));
	}


	public void testParcel() {
		Rule rule = getRule();
		Bundle bundle = new Bundle();
		bundle.putParcelable("rule", rule);
		assertEquals(rule, bundle.getParcelable("rule"));
	}


	private Rule getRule() {
		return new Rule.Builder("dummyPath")
				.parameter("key1", "value1")
				.parameter("key2", "value2")
				.build();
	}

}
