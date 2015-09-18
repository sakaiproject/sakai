/**
 * Copyright (c) 2005 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.webservices;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.transport.local.LocalConduit;
import org.apache.cxf.transport.local.LocalTransportFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Unit Tests for CXF webservices
 * <p>
 * These unit tests are not integration tests and as such all sakai services
 * are mocked.
 * <p>
 * The idea is you extend this class and create your test. This class contains
 * all the cxf plumbing that tests will need. See some of the examples.
 * <p>
 * Each test class should be a single soap operation.
 * <p>
 * Important to note is that RPC/Literal SOAP services should not return null
 * so make sure you assertNotNull(result)
 * 
 */
public abstract class AbstractCXFTest extends Assert {
	private static final String LOCAL_ENDPOINT_ADDRESS = "local://";
	private Server server;

	@Before
	public void init() {
		startServer(getTestClass());
	}

	@After
	public void destroy() {
		server.stop();
		server.destroy();
	}

	/**
	 * Starts a local cxf server that listens for defined SOAP operation.
	 * This means that each operation should have its own test.
	 * @param type the implementing type of AbstractWebService, i.e. SakaiLogin.class
	 */
	private <T extends AbstractWebService> void startServer(Class<T> type) {
		JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
		sf.setResourceClasses(type);

		List<Object> providers = new ArrayList<Object>();
		// add custom providers if any
		// CXF supports String, byte[], InputStream, Reader, File, JAXP Source, JAX-RS StreamingOutput, 
		// JAXB-annotated types with application/xml, text/xml and application/json formats as well as JAXBElement
		sf.setProviders(providers);

		AbstractWebService service = MockingAbstractWebService.getMockedAbstractWebService(type);
		addServiceMocks(service);

		sf.setResourceProvider(type, new SingletonResourceProvider(service, true));
		sf.setAddress(getFullEndpointAddress());

		server = sf.create();
	}

	/**
	 * The endpoint that the local server binds to
	 * @return endpoint
	 */
	public String getFullEndpointAddress() {
		return LOCAL_ENDPOINT_ADDRESS + getOperation();
	}

	/**
	 * @return The AbstractWebService class that tests are to run against.
	 * The class is bound to the local cxf server.
	 */
	protected abstract <T extends AbstractWebService> Class<T> getTestClass();

	/**
	 * @return the soap operation that is being tested.
	 */
	protected abstract String getOperation();

	/**
	 * All of the Sakai services are mocked instances but there are no rules defined.
	 * Method that allows rules to be added to the web service that is being tested. 
	 * <p>
	 * For example this creates a mock Session object which is returned by the already
	 * mocked sessionManager.
	 * <code>
	 * Session mockSession = mock(Session.class);
	 * when(service.sessionManager.getSession(SESSION_ID)).thenReturn(mockSession);
	 * </code>
	 * @param service the mocked web service 
	 */
	protected abstract void addServiceMocks(AbstractWebService service);

	/**
	 * This is a utility method that contains some mocks that are required for cxf local testing.
	 * It adds the <code>HTTP.REQUEST</code> to the request context.
	 * @param client the cxf client from the test
	 */
	protected void addCXFClientMocks(WebClient client) {
		Map<String, Object> requestContext = WebClient.getConfig(client).getRequestContext();
		requestContext.put(LocalConduit.DIRECT_DISPATCH, Boolean.TRUE);

		// cxf workaround for using a HttpServletRequest in a local:// call
		HashSet<String> include = new HashSet<>();
		include.add(AbstractHTTPDestination.HTTP_REQUEST);
		requestContext.put(LocalTransportFactory.MESSAGE_INCLUDE_PROPERTIES, include);

		HttpServletRequest mockRequest = mock(HttpServletRequest.class);
		when(mockRequest.getRemoteAddr()).thenReturn("127.0.0.1");
		when(mockRequest.getRequestURL()).thenReturn(new StringBuffer());
		when(mockRequest.getPathInfo()).thenReturn("");
		when(mockRequest.getContextPath()).thenReturn("");
		when(mockRequest.getServletPath()).thenReturn("");
		requestContext.put(AbstractHTTPDestination.HTTP_REQUEST, mockRequest);
	}
}
