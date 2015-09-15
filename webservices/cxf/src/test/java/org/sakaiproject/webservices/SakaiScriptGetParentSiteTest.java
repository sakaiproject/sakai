package org.sakaiproject.webservices;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Test;
import org.sakaiproject.tool.api.Session;

public class SakaiScriptGetParentSiteTest extends AbstractCXFTest {
	public static final String SESSION_ID = "***SESSION_HAS_BEEN_MOCKERIZED***";
	private static final String SOAP_OPERATION = "getParentSite";

	@Override
	protected <T extends AbstractWebService> Class<T> getTestClass() {
		return (Class<T>) SakaiScript.class;
	}

	@Override
	protected String getOperation() {
		return SOAP_OPERATION;
	}

	private void addClientMocks(WebClient client) {
		addCXFClientMocks(client);
	}

	@Override
	protected void addServiceMocks(AbstractWebService service) {
		Session mockSession = mock(Session.class);
		when(service.sessionManager.getSession(SESSION_ID)).thenReturn(mockSession);
		when(service.siteService.getParentSite("admin")).thenReturn(null);
		when(service.siteService.getParentSite("xxx")).thenReturn("yyy");
	}

	@Test
	public void testGetParentSiteNoParent() {
		WebClient client = WebClient.create(getFullEndpointAddress());

		addClientMocks(client);

		// client call
		client.accept("text/plain");
		client.path("/" + getOperation());
		client.query("sessionid", SESSION_ID);
		client.query("siteid", "admin");

		// client result
		String result = client.get(String.class);

		// test verifications
		assertNotNull(result);
		assertEquals("", result);
	}

	@Test
	public void testGetParentSite() {
		WebClient client = WebClient.create(getFullEndpointAddress());

		addClientMocks(client);

		// client call
		client.accept("text/plain");
		client.path("/" + getOperation());
		client.query("sessionid", SESSION_ID);
		client.query("siteid", "xxx");

		// client result
		String result = client.get(String.class);

		// test verifications
		assertNotNull(result);
		assertEquals("yyy", result);
	}
}
