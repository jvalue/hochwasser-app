package de.bitdroid.ods.cep;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.bitdroid.utils.Assert;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Rule {

	private final String uuid;
	private final String cepsRulePath;
	private final Map<String, String> params;


	@JsonCreator
	private Rule(
			@JsonProperty("uuid") String uuid,
			@JsonProperty("cepsRulePath") String cepsRulePath,
			@JsonProperty("params") Map<String, String> params) {

		this.uuid = uuid;
		this.cepsRulePath = cepsRulePath;
		this.params = params;
	}


	public String getUuid() {
		return uuid;
	}


	public String getCepsRulePath() {
		return cepsRulePath;
	}


	public Map<String, String> getParams() {
		return params;
	}


	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof Rule)) return false;
		if (other == this) return true;
		Rule rule = (Rule) other;
		return uuid.equals(rule.uuid)
				&& cepsRulePath.equals(rule.cepsRulePath)
				&& params.equals(rule.params);
	}


	@Override
	public int hashCode() {
		final int MULT = 17;
		int hash = 13;
		hash = hash + MULT * uuid.hashCode();
		hash = hash + MULT * cepsRulePath.hashCode();
		hash = hash + MULT * params.hashCode();
		return hash;
	}


	public static class Builder {

		private final String uuid;
		private final String cepsRulePath;
		private final Map<String, String> params = new HashMap <String, String>();

		public Builder(String cepsRulePath) {
			Assert.assertNotNull(cepsRulePath);
			this.uuid = UUID.randomUUID().toString();
			this.cepsRulePath = cepsRulePath;
		}


		public Builder parameter(String key, String param) {
			Assert.assertNotNull(key, param);
			params.put(key, param);
			return this;
		}


		public Rule build() {
			return new Rule(uuid, cepsRulePath, params);
		}

	}
}
