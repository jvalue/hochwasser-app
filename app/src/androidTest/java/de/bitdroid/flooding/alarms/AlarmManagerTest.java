package de.bitdroid.flooding.alarms;

import java.lang.reflect.Constructor;

import de.bitdroid.flooding.ods.cep.CepManager;
import de.bitdroid.flooding.ods.gcm.GcmStatus;
import de.bitdroid.flooding.testUtils.BaseAndroidTestCase;
import de.bitdroid.flooding.testUtils.PrefsRenamingDelegatingContext;
import de.bitdroid.flooding.testUtils.SharedPreferencesHelper;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AlarmManagerTest extends BaseAndroidTestCase {

	private static final String PREFIX = AlarmManagerTest.class.getSimpleName();

	private int addedCounter;
	private int deletedCounter;

	@Override
	public void beforeClass() {
		setContext(new PrefsRenamingDelegatingContext(getContext(), PREFIX));
	}


	@Override
	public void beforeTest() {
		SharedPreferencesHelper.clearAll((PrefsRenamingDelegatingContext) getContext());
		addedCounter = 0;
		deletedCounter = 0;
	}


	public void testSuccess() throws Exception {
		final Alarm alarm1 = new LevelAlarm("river", "station", 32, true);
		final Alarm alarm2 = new LevelAlarm("river", "station", 32, false);

		EplStmtCreator stmtCreator = new EplStmtCreator();
		String stmt1 = alarm1.accept(stmtCreator, null);
		String stmt2 = alarm2.accept(stmtCreator, null);

		CepManager cepManager = mock(CepManager.class);
		AlarmManager alarmManager = newAlarmManager(cepManager);

		AlarmUpdateListener listener = new AlarmUpdateListener() {
			@Override
			public void onNewAlarm(Alarm alarm) {
				assertAlarm(addedCounter, alarm);
				addedCounter++;
			}

			@Override
			public void onDeletedAlarm(Alarm alarm) {
				assertAlarm(deletedCounter, alarm);
				deletedCounter++;
			}

			private void assertAlarm(int counter, Alarm alarm) {
				switch(counter) {
					case 0:
						assertEquals(alarm1, alarm);
						break;
					case 1:
						assertEquals(alarm2, alarm);
						break;
					default:
						fail();
				}
			}
		};

		alarmManager.registerListener(listener);
		when(cepManager.getRegistrationStatus(anyString())).thenReturn(GcmStatus.UNREGISTERED);

		assertEquals(0, alarmManager.getAll().size());
		assertFalse(alarmManager.isRegistered((alarm1)));
		assertFalse(alarmManager.isRegistered((alarm2)));
		assertEquals(GcmStatus.UNREGISTERED, alarmManager.getRegistrationStatus(alarm1));
		assertEquals(GcmStatus.UNREGISTERED, alarmManager.getRegistrationStatus(alarm2));

		alarmManager.register(alarm1);
		when(cepManager.getRegistrationStatus(stmt1)).thenReturn(GcmStatus.REGISTERED);

		verify(cepManager).registerEplStmt(stmt1);
		assertEquals(1, alarmManager.getAll().size());
		assertTrue(alarmManager.getAll().contains(alarm1));
		assertTrue(alarmManager.isRegistered((alarm1)));
		assertFalse(alarmManager.isRegistered((alarm2)));
		assertEquals(GcmStatus.REGISTERED, alarmManager.getRegistrationStatus(alarm1));
		assertEquals(GcmStatus.UNREGISTERED, alarmManager.getRegistrationStatus(alarm2));
		assertEquals(1, addedCounter);
		assertEquals(0, deletedCounter);

		alarmManager.register(alarm2);
		when(cepManager.getRegistrationStatus(stmt2)).thenReturn(GcmStatus.REGISTERED);

		verify(cepManager).registerEplStmt(stmt2);
		assertEquals(2, alarmManager.getAll().size());
		assertTrue(alarmManager.getAll().contains(alarm1));
		assertTrue(alarmManager.getAll().contains(alarm2));
		assertTrue(alarmManager.isRegistered((alarm1)));
		assertTrue(alarmManager.isRegistered((alarm2)));
		assertEquals(GcmStatus.REGISTERED, alarmManager.getRegistrationStatus(alarm1));
		assertEquals(GcmStatus.REGISTERED, alarmManager.getRegistrationStatus(alarm2));
		assertEquals(2, addedCounter);
		assertEquals(0, deletedCounter);

		alarmManager.unregister(alarm1);
		when(cepManager.getRegistrationStatus(stmt1)).thenReturn(GcmStatus.UNREGISTERED);

		verify(cepManager).unregisterEplStmt(stmt1);
		assertEquals(1, alarmManager.getAll().size());
		assertTrue(alarmManager.getAll().contains(alarm2));
		assertFalse(alarmManager.isRegistered((alarm1)));
		assertTrue(alarmManager.isRegistered((alarm2)));
		assertEquals(GcmStatus.UNREGISTERED, alarmManager.getRegistrationStatus(alarm1));
		assertEquals(GcmStatus.REGISTERED, alarmManager.getRegistrationStatus(alarm2));
		assertEquals(2, addedCounter);
		assertEquals(1, deletedCounter);

		alarmManager.unregisterListener(listener);
		alarmManager.unregister(alarm2);
		when(cepManager.getRegistrationStatus(stmt2)).thenReturn(GcmStatus.UNREGISTERED);

		verify(cepManager).unregisterEplStmt(stmt2);
		assertEquals(0, alarmManager.getAll().size());
		assertFalse(alarmManager.isRegistered((alarm1)));
		assertFalse(alarmManager.isRegistered((alarm2)));
		assertEquals(GcmStatus.UNREGISTERED, alarmManager.getRegistrationStatus(alarm1));
		assertEquals(GcmStatus.UNREGISTERED, alarmManager.getRegistrationStatus(alarm2));
		assertEquals(2, addedCounter);
		assertEquals(1, deletedCounter);
	}


	public void  testFailure() throws Exception {
		CepManager cepManager = mock(CepManager.class);
		AlarmManager alarmManager = newAlarmManager(cepManager);
		when(cepManager.getRegistrationStatus(anyString())).thenReturn(GcmStatus.UNREGISTERED);

		Alarm alarm = new LevelAlarm("river", "station", 32, true);

		assertEquals(0, alarmManager.getAll().size());
		assertFalse(alarmManager.isRegistered((alarm)));
		assertEquals(GcmStatus.UNREGISTERED, alarmManager.getRegistrationStatus(alarm));

		alarmManager.register(alarm);

		assertEquals(1, alarmManager.getAll().size());
		assertTrue(alarmManager.getAll().contains(alarm));
		assertTrue(alarmManager.isRegistered((alarm)));
		assertEquals(GcmStatus.UNREGISTERED, alarmManager.getRegistrationStatus(alarm));

		when(cepManager.getRegistrationStatus(anyString())).thenReturn(GcmStatus.REGISTERED);
		alarmManager.register(alarm);

		assertEquals(1, alarmManager.getAll().size());
		assertTrue(alarmManager.getAll().contains(alarm));
		assertTrue(alarmManager.isRegistered((alarm)));
		assertEquals(GcmStatus.REGISTERED, alarmManager.getRegistrationStatus(alarm));

		when(cepManager.getRegistrationStatus(anyString())).thenReturn(GcmStatus.UNREGISTERED);
		alarmManager.unregister(alarm);

		assertEquals(0, alarmManager.getAll().size());
		assertFalse(alarmManager.isRegistered((alarm)));
		assertEquals(GcmStatus.UNREGISTERED, alarmManager.getRegistrationStatus(alarm));
	}


	private AlarmManager newAlarmManager(CepManager cepManager) throws Exception {
		Constructor<AlarmManager> constructor = AlarmManager.class.getDeclaredConstructor(
				CepManager.class,
				AlarmDb.class);
		constructor.setAccessible(true);
		AlarmDb alarmDb = new AlarmDb(getContext());
		return constructor.newInstance(cepManager, alarmDb);
	}
}
