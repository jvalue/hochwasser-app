package de.bitdroid.flooding.ods.cep;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import java.util.HashMap;
import java.util.Map;

import de.bitdroid.flooding.ods.gcm.GcmStatus;
import de.bitdroid.testUtils.BaseAndroidTestCase;
import de.bitdroid.testUtils.PrefsRenamingDelegatingContext;
import de.bitdroid.testUtils.SharedPreferencesHelper;

public class CepManagerImplTest extends BaseAndroidTestCase {

	private static final String PREFIX = CepManagerImplTest.class.getSimpleName();
	private static final ObjectMapper mapper = new ObjectMapper();

	private CepManager manager;

	@Override
	public void beforeClass() {
		setContext(new PrefsRenamingDelegatingContext(getContext(), PREFIX));
		SharedPreferencesHelper.clearAll((PrefsRenamingDelegatingContext) getContext());
		this.manager = CepManagerFactory.createCepManager(getContext());
	}


	public void testServerName() {
		String serverName1 = "http://somedomain.com";
		String serverName2 = "http://someotherdomain.com";

		manager.setCepServerName(serverName1);
		assertEquals(serverName1, manager.getCepServerName());

		manager.setCepServerName(serverName2);
		assertEquals(serverName2, manager.getCepServerName());
	}


	public void testEplStmtCrud() throws Exception {
		String stmt1 = "select * from *";
		String stmt2 = "select foo from bar";

		MockWebServer server = new MockWebServer();
		server.enqueue(getRegistrationResponse("someId1"));
		server.enqueue(getRegistrationResponse("someId2"));
		server.enqueue(new MockResponse());
		server.enqueue(new MockResponse());
		server.play();
		manager.setCepServerName(server.getUrl("").toString());

		assertEquals(GcmStatus.UNREGISTERED, manager.getRegistrationStatus(stmt1));
		assertEquals(GcmStatus.UNREGISTERED, manager.getRegistrationStatus(stmt2));
		assertEquals(0, manager.getRegisteredStmts().size());

		manager.registerEplStmt(stmt1);
		Thread.sleep(300);

		assertEquals(GcmStatus.REGISTERED, manager.getRegistrationStatus(stmt1));
		assertEquals(GcmStatus.UNREGISTERED, manager.getRegistrationStatus(stmt2));
		assertEquals(1, manager.getRegisteredStmts().size());
		assertTrue(manager.getRegisteredStmts().contains(stmt1));

		manager.registerEplStmt(stmt2);
		Thread.sleep(300);

		assertEquals(GcmStatus.REGISTERED, manager.getRegistrationStatus(stmt1));
		assertEquals(GcmStatus.REGISTERED, manager.getRegistrationStatus(stmt2));
		assertEquals(2, manager.getRegisteredStmts().size());
		assertTrue(manager.getRegisteredStmts().contains(stmt1));
		assertTrue(manager.getRegisteredStmts().contains(stmt2));

		manager.unregisterEplStmt(stmt1);
		Thread.sleep(300);

		assertEquals(GcmStatus.UNREGISTERED, manager.getRegistrationStatus(stmt1));
		assertEquals(GcmStatus.REGISTERED, manager.getRegistrationStatus(stmt2));
		assertEquals(1, manager.getRegisteredStmts().size());
		assertTrue(manager.getRegisteredStmts().contains(stmt2));

		manager.unregisterEplStmt(stmt2);
		Thread.sleep(300);

		assertEquals(GcmStatus.UNREGISTERED, manager.getRegistrationStatus(stmt1));
		assertEquals(GcmStatus.UNREGISTERED, manager.getRegistrationStatus(stmt2));
		assertEquals(0, manager.getRegisteredStmts().size());

		server.shutdown();
	}


	private MockResponse getRegistrationResponse(String clientId) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("clientId", clientId);
		JsonNode json = mapper.valueToTree(map);
		return new MockResponse().setBody(((Object) json).toString());
	}

}
