package org.sakaiproject.webservices;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.ArrayList;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Test;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.tool.api.Session;


public class SakaiScriptFindSitesByTitleTest extends AbstractCXFTest {
	
	public static final String SESSION_ID = "***SESSION_HAS_BEEN_MOCKERIZED***";
	public static final String NULL_SESSION = "***NULL_SESSION***";
	private static final String SOAP_OPERATION = "findSitesByTitle";

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
		List<String> mockSiteIDs = new ArrayList<String>();
		mockSiteIDs.add("validSiteIDs");
		try {
			when(service.siteService.getSiteIds(SelectionType.ANY, null, "matchingCriteria", null, SortType.NONE, null)).thenReturn(mockSiteIDs);
			when(service.siteService.getSiteIds(SelectionType.ANY, null, "unmatchingCriteria", null, SortType.NONE, null)).thenReturn(null);
		} catch (Exception e) {
		}
		Session mockSession = mock(Session.class);
		when(service.sessionManager.getSession(SESSION_ID)).thenReturn(mockSession);
		when(service.sessionManager.getSession(NULL_SESSION)).thenReturn(null);
	}
	
	@Test
	public void testMatchingCriteria() {
		WebClient client = WebClient.create(getFullEndpointAddress());

		addClientMocks(client);

		// client call
		client.accept("text/plain");
		client.path("/" + getOperation());
		client.query("sessionid", SESSION_ID);
		client.query("criteria", "matchingCriteria");
		
		// client result
		String result = client.get(String.class);

		// test verifications
		assertNotNull(result);
		assertEquals("validSiteIDs", result);
	}

	@Test
	public void testUnmatchingCriteria() {
		WebClient client = WebClient.create(getFullEndpointAddress());

		addClientMocks(client);

		// client call
		client.accept("text/plain");
		client.path("/" + getOperation());
		client.query("sessionid", SESSION_ID);
		client.query("criteria", "unmatchingCriteria");
		
		// client result
		String result = client.get(String.class);

		// test verifications
		assertNotNull(result);
		assertEquals("", result);
	}

}
