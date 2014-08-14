package de.bitdroid.ods.cep;

import com.fasterxml.jackson.databind.JsonNode;
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


	public void testJson() throws Exception {
		Rule rule = getRule();
		JsonNode json = mapper.valueToTree(rule);
		assertEquals(rule, mapper.treeToValue(json, Rule.class));
	}


	private Rule getRule() {
		return new Rule.Builder("dummyPath")
				.parameter("key1", "value1")
				.parameter("key2", "value2")
				.build();
	}

}
