package de.bitdroid.ods.cep;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.bitdroid.utils.Assert;

public final class Rule implements Parcelable {

	private final String uuid;
	private final String cepsRulePath;
	private final Map<String, String> params;


	private Rule(
			String uuid,
			String cepsRulePath,
			Map<String, String> params) {

		this.uuid = uuid;
		this.cepsRulePath = cepsRulePath;
		this.params = params;
	}


	private Rule(Parcel parcel) {
		this.uuid = parcel.readString();
		this.cepsRulePath = parcel.readString();
		this.params = new HashMap<String, String>();
		while (parcel.dataAvail() > 0) params.put(parcel.readString(), parcel.readString());
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


	@Override
	public int describeContents() {
		return 0;
	}


	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(uuid);
		dest.writeString(cepsRulePath);
		for (Map.Entry<String, String> param : params.entrySet()) {
			dest.writeString(param.getKey());
			dest.writeString(param.getValue());
		}
	}


	public static final Parcelable.Creator<Rule> CREATOR = new Parcelable.Creator<Rule>() {
		@Override
		public Rule createFromParcel(Parcel in) {
			return new Rule(in);
		}

		@Override
		public Rule[] newArray(int size) {
			return new Rule[size];
		}
	};




	public static class Builder {

		private String uuid;
		private final String cepsRulePath;
		private final Map<String, String> params = new HashMap <String, String>();

		public Builder(String cepsRulePath) {
			Assert.assertNotNull(cepsRulePath);
			this.uuid = UUID.randomUUID().toString();
			this.cepsRulePath = cepsRulePath;
		}


		public Builder uuid(String uuid) {
			Assert.assertNotNull(uuid);
			this.uuid = uuid;
			return this;
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
