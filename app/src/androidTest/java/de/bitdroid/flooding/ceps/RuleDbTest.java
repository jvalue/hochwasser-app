package de.bitdroid.flooding.ceps;

import android.database.sqlite.SQLiteDatabase;

import java.util.Set;

import de.bitdroid.ods.gcm.GcmStatus;
import de.bitdroid.testUtils.BaseAndroidTestCase;
import de.bitdroid.testUtils.PrefsRenamingDelegatingContext;

import static de.bitdroid.ods.cep.RuleDbSchema.TABLE_NAME;


public final class RuleDbTest extends BaseAndroidTestCase {

	private static final String PREFIX = RuleDbTest.class.getSimpleName();

	private RuleDb ruleDb;

	@Override
	public void beforeClass() {
		setContext(new PrefsRenamingDelegatingContext(getContext(), PREFIX));
	}


	@Override
	public void beforeTest() {
		ruleDb = new RuleDb(getContext());
		SQLiteDatabase database = null;
		try {
			database = ruleDb.getWritableDatabase();
			database.delete(TABLE_NAME, null, null);
		} finally {
			if (database != null) database.close();
		}
	}


	public void testInsertDeleteGet() {
		assertEquals(0, ruleDb.getAll().size());

		Rule rule1 = newRule("somePath", 10);
		Rule rule2 = newRule("someOtherPath", 0);
		ruleDb.insert(rule1);
		ruleDb.insert(rule2);

		Set<Rule> rules = ruleDb.getAll();
		assertEquals(2, rules.size());
		assertTrue(rules.contains(rule1));
		assertTrue(rules.contains(rule2));

		ruleDb.delete(rule1);

		rules = ruleDb.getAll();
		assertEquals(1, rules.size());
		assertTrue(rules.contains(rule2));

		ruleDb.delete(rule2);

		assertEquals(0, ruleDb.getAll().size());
	}


	public void testCepsData() {
		Rule rule1 = newRule("somePath", 10);
		Rule rule2 = newRule("someOtherPath", 0);

		ruleDb.insert(rule1);
		ruleDb.insert(rule2);

		assertNull(ruleDb.getRuleForClientId("someId"));
		assertNull(ruleDb.getClientIdForRule(rule1));
		assertNull(ruleDb.getClientIdForRule(rule2));
		assertNull(ruleDb.getStatusForRule(rule1));
		assertNull(ruleDb.getStatusForRule(rule2));

		ruleDb.updateCepsData(rule1, "someClientId", GcmStatus.PENDING_REGISTRATION);

		assertEquals(rule1, ruleDb.getRuleForClientId("someClientId"));
		assertNull(ruleDb.getRuleForClientId("someFakeId"));
		assertEquals("someClientId", ruleDb.getClientIdForRule(rule1));
		assertNull(ruleDb.getClientIdForRule(rule2));
		assertEquals(GcmStatus.PENDING_REGISTRATION, ruleDb.getStatusForRule(rule1));
		assertNull(ruleDb.getStatusForRule(rule2));

		ruleDb.updateCepsData(rule2, "someOtherClientId", GcmStatus.REGISTERED);

		assertEquals(rule1, ruleDb.getRuleForClientId("someClientId"));
		assertEquals(rule2, ruleDb.getRuleForClientId("someOtherClientId"));
		assertEquals("someClientId", ruleDb.getClientIdForRule(rule1));
		assertEquals("someOtherClientId", ruleDb.getClientIdForRule(rule2));
		assertEquals(GcmStatus.PENDING_REGISTRATION, ruleDb.getStatusForRule(rule1));
		assertEquals(GcmStatus.REGISTERED, ruleDb.getStatusForRule(rule2));
	}


	private Rule newRule(String path, int paramCount) {
		Rule.Builder builder = new Rule.Builder(path);
		for (int i = 0; i < paramCount; i++) {
			builder.parameter("key" + i, "value" + i);
		}
		return builder.build();
	}

}
