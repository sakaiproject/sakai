package org.sakaiproject.entitybroker.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.sakaiproject.entitybroker.providers.EntityRequestHandler;
import org.sakaiproject.entitybroker.rest.EntityHandlerImpl;
import org.sakaiproject.entitybroker.config.EntityRestTestConfiguration;
import org.sakaiproject.util.BasicAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests that exercise the full DirectController -> EntityHandlerImpl pipeline
 * using Spring MockMvc.
 *
 * Note: Batch tests are not included because the EntityBatchHandler makes real HTTP
 * calls to itself, which requires an actual running server. Batch functionality is
 * tested separately via EntityHandlerImplTest.
 */
@ContextConfiguration(classes = {EntityRestTestConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class DirectControllerTest {

    @Autowired private EntityHandlerImpl entityRequestHandler;
    @Autowired private EntityProviderManager entityProviderManager;
    @Autowired private ServerConfigurationService serverConfigurationService;

    private MockMvc mockMvc;
    private TestData td;

    /**
     * Sets pathInfo on the MockHttpServletRequest so that
     * DirectController.dispatch() sees the correct path.
     */
    private static RequestPostProcessor pathInfo(String path) {
        return request -> {
            request.setPathInfo(path);
            return request;
        };
    }

    @Before
    public void setUp() throws Exception {
        td = new TestData(entityProviderManager);
        Mockito.when(serverConfigurationService.getServerUrl()).thenReturn("http://localhost:8080");

        BasicAuth mockBasicAuth = mock(BasicAuth.class);
        when(mockBasicAuth.doAuth(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(false);

        DirectController controller = new TestableDirectController(
                entityRequestHandler, mockBasicAuth, TestData.USER_ID);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @After
    public void tearDown() {
        entityProviderManager.unRegistrarAllProvidersAndListeners();
        td = null;
    }

    private String getContent(String path) throws Exception {
        MvcResult result = mockMvc.perform(get(path).with(pathInfo(path)))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getContentAsString();
    }

    @Test
    public void testSimpleXML() throws Exception {
        String content = getContent(TestData.ENTITY_URL4_XML);
        assertNotNull(content);
        assertTrue(content.contains("myPrefix4"));
        assertTrue(content.contains("4-one"));
        assertTrue(content.contains("<id>4-one</id>"));
        assertTrue(content.contains("<entityId>4-one</entityId>"));
        assertTrue(content.contains("/myPrefix4/4-one"));
        assertFalse(content.contains("4-two"));
        assertFalse(content.contains("4-three"));
    }

    // --- Single entity tests ---

    @Test
    public void testSimpleJSON() throws Exception {
        String content = getContent(TestData.ENTITY_URL4_JSON);
        assertNotNull(content);
        assertTrue(content.contains("myPrefix4"));
        assertTrue(content.contains("4-one"));
        assertTrue(content.contains("\"id\": \"4-one\""));
        assertTrue(content.contains("\"entityReference\":"));
        // JSON encodes forward slashes as \/
        assertTrue(content.contains("\\/myPrefix4\\/4-one"));
        assertFalse(content.contains("4-two"));
        assertFalse(content.contains("4-three"));
    }

    @Test
    public void testSimpleCollectionXML() throws Exception {
        String content = getContent(TestData.COLLECTION_URL4_XML);
        assertNotNull(content);
        assertTrue(content.contains("<myPrefix4_collection entityPrefix=\"myPrefix4\">"));
        assertTrue(content.contains("</myPrefix4_collection>"));
        assertTrue(content.contains("4-two"));
        assertTrue(content.contains("4-three"));
        assertTrue(content.contains("myPrefix4"));
        assertTrue(content.contains("4-one"));
        assertTrue(content.contains("<id>4-one</id>"));
        assertTrue(content.contains("<entityId>4-one</entityId>"));
    }

    // --- Collection tests ---

    @Test
    public void testSimpleCollectionJSON() throws Exception {
        String content = getContent(TestData.COLLECTION_URL4_JSON);
        assertNotNull(content);
        assertTrue(content.contains("\"myPrefix4_collection\":"));
        assertTrue(content.contains("myPrefix4"));
        assertTrue(content.contains("4-one"));
        assertTrue(content.contains("\"id\": \"4-one\""));
        assertTrue(content.contains("\"entityReference\":"));
        assertTrue(content.contains("\\/myPrefix4\\/4-one"));
        assertTrue(content.contains("4-two"));
        assertTrue(content.contains("4-three"));
    }

    @Test
    public void testDescribe() throws Exception {
        String path = EntityRequestHandler.SLASH_DESCRIBE;
        String content = getContent(path);
        assertNotNull(content);
        assertTrue(content.contains("<!DOCTYPE html"));
        assertTrue(content.contains("<html"));
        assertTrue(content.contains("<head"));
        assertTrue(content.contains("<title"));
        assertTrue(content.contains("</title>"));
        assertTrue(content.contains("<body"));
        assertTrue(content.contains("</body>"));
        assertTrue(content.contains("</html>"));
    }

    // --- Describe tests ---

    @Test
    public void testDescribeXML() throws Exception {
        String path = EntityRequestHandler.SLASH_DESCRIBE + ".xml";
        String content = getContent(path);
        assertNotNull(content);
        assertTrue(content.contains("<?xml"));
        assertTrue(content.contains("<describe>"));
        assertTrue(content.contains("<prefixes>"));
        assertTrue(content.contains("<prefix>"));
        assertTrue(content.contains("<capabilities>"));
        assertTrue(content.contains("<describeURL>"));
        assertTrue(content.contains("<capability>"));
        assertTrue(content.contains("</prefixes>"));
        assertTrue(content.contains("</describe>"));
    }

    @Test
    public void testDescribeEntity() throws Exception {
        String path = "/" + TestData.PREFIX4 + EntityRequestHandler.SLASH_DESCRIBE;
        String content = getContent(path);
        assertNotNull(content);
        assertTrue(content.contains("<!DOCTYPE html"));
        assertTrue(content.contains("<html"));
        assertTrue(content.contains("<body"));
        assertTrue(content.contains("</body>"));
        assertTrue(content.contains("</html>"));
        assertTrue(content.contains(TestData.PREFIX4));
    }

    @Test
    public void testDescribeEntityXML() throws Exception {
        String path = "/" + TestData.PREFIX4 + EntityRequestHandler.SLASH_DESCRIBE + ".xml";
        String content = getContent(path);
        assertNotNull(content);
        assertTrue(content.contains("<?xml"));
        assertTrue(content.contains("<prefix>"));
        assertTrue(content.contains("<collectionURL>/" + TestData.PREFIX4 + "</collectionURL>"));
        assertTrue(content.contains("<describeURL>"));
        assertTrue(content.contains("<capabilities>"));
        assertTrue(content.contains("<capability>"));
        assertTrue(content.contains(TestData.PREFIX4));
    }

    /**
     * Subclass that avoids Sakai static SessionManager calls and strips Spring MVC
     * internal request attributes that would otherwise pollute entity search queries.
     */
    private static class TestableDirectController extends DirectController {
        private final String userId;

        TestableDirectController(EntityRequestHandler handler, BasicAuth basicAuth, String userId) {
            super(handler, basicAuth);
            this.userId = userId;
        }

        @Override
        protected String currentUserId() {
            return userId;
        }

        @Override
        public void handleDirect(HttpServletRequest req, HttpServletResponse res) throws IOException {
            // Strip Spring MVC internal attributes that DispatcherServlet adds.
            // Without this, RequestStorage picks them up as search restrictions,
            // causing collection queries to return empty results.
            List<String> toRemove = new ArrayList<>();
            Enumeration<String> names = req.getAttributeNames();
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                if (name.startsWith("org.springframework.")) {
                    toRemove.add(name);
                }
            }
            for (String name : toRemove) {
                req.removeAttribute(name);
            }
            super.handleDirect(req, res);
        }
    }
}
