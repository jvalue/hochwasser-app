package de.bitdroid.ods.cep;


final class RuleDbSchema {

	private RuleDbSchema() { }


	public static final String
		TABLE_NAME = "rules",
		COLUMN_ID = "_id",
		COLUMN_UUID = "uuid",
		COLUMN_CEPS_RULE_PATH = "ceps_rule_path",
		COLUMN_PARAMS = "params",
		COLUMN_REGISTRATION_STATUS = "registration_status",
		COLUMN_CEPS_CLIENT_ID = "ceps_client_id";

}
