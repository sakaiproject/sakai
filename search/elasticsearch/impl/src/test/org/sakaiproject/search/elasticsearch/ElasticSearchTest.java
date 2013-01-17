package org.sakaiproject.search.elasticsearch;

import com.github.javafaker.Faker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;


import org.mockito.runners.MockitoJUnitRunner;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.*;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.InvalidSearchQueryException;
import org.sakaiproject.search.api.SearchList;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: jbush
 * Date: 1/16/13
 * Time: 9:10 PM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(MockitoJUnitRunner.class)
public class ElasticSearchTest {
    ElasticSearchService elasticSearchService;

    @Mock
    EventTrackingService eventTrackingService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    ServerConfigurationService serverConfigurationService;

    @Mock
    SessionManager sessionManager;

    @Mock
    UserDirectoryService userDirectoryService;

    @Mock
    NotificationService notificationService;

    @Mock
    SiteService siteService;

    @Mock
    SecurityService securityService;

    @Mock
    NotificationEdit notificationEdit;

    ElasticSearchIndexBuilder elasticSearchIndexBuilder;

    @Mock
    Notification notification;

    @Mock
    Event event;

    @Mock
    EntityContentProducer entityContentProducer;

    @Mock
    Site site;

    List<String> siteIds = new ArrayList<String>();

     Faker faker = new Faker();

     String siteId = faker.phoneNumber();

     String resourceName = faker.name();
    String url = "http://localhost/test123";

    private  Map<String, Resource> resources = new HashMap();
    private  List<Event> events = new ArrayList();

    List<Site> sites = new ArrayList<Site>();

    @After
    public void tearDown() {
        elasticSearchService.destroy();
    }

    public void createTestResources() {
        Resource resource = new Resource("asdf te tem temp tempo tempora " + generateContent(), siteId, resourceName);
        resources.put(resourceName, resource);

        when(event.getResource()).thenReturn(resource.getName());
        events.add(event);
        when(entityContentProducer.matches(event)).thenReturn(true);
        when(entityContentProducer.matches(resourceName)).thenReturn(true);

        when(entityContentProducer.getSiteId(resourceName)).thenReturn(siteId);
        when(entityContentProducer.getAction(event)).thenReturn(SearchBuilderItem.ACTION_ADD);
        when(entityContentProducer.getContent(resourceName)).thenReturn("asdf te tem temp tempo tempora " + generateContent());
        when(entityContentProducer.getType(resourceName)).thenReturn("sakai:content");
        when(entityContentProducer.getId(resourceName)).thenReturn(resourceName);


        for (int i=0;i<100;i++) {
            String name = faker.name();
            Event newEvent = mock(Event.class);
            Resource resource1 = new Resource(generateContent(), faker.phoneNumber(), name);
            resources.put(name, resource1);
            when(newEvent.getResource()).thenReturn(resource1.getName());
            when(entityContentProducer.matches(name)).thenReturn(true);
            when(entityContentProducer.matches(newEvent)).thenReturn(true);
            when(entityContentProducer.getSiteId(name)).thenReturn(resource1.getSiteId());
            when(entityContentProducer.getAction(newEvent)).thenReturn(SearchBuilderItem.ACTION_ADD);
            when(entityContentProducer.getContent(name)).thenReturn(resource1.getContent());
            when(entityContentProducer.getType(name)).thenReturn("sakai:content");
            when(entityContentProducer.getId(name)).thenReturn(name);

        }

        when(entityContentProducer.getSiteContentIterator(siteId)).thenReturn(resources.keySet().iterator());
    }

     private String generateContent() {
        StringBuffer sb = new StringBuffer();
        for (int i=0;i<10;i++) {
            sb.append(faker.paragraph(10) + " ");
        }
        return sb.toString();
    }

    @Before
   	public void setUp() throws Exception {
        createTestResources();

        when(site.getId()).thenReturn(siteId);
        sites.add(site);
        when(serverConfigurationService.getBoolean("search.enable", false)).thenReturn(true);
        when(serverConfigurationService.getConfigData().getItems()).thenReturn(new ArrayList());
        when(serverConfigurationService.getServerId()).thenReturn("server1");
        when(serverConfigurationService.getServerName()).thenReturn("clusterName");
        when(serverConfigurationService.getSakaiHomePath()).thenReturn(System.getProperty("java.io.tmpdir")  + new Date().getTime());
        when(notificationService.addTransientNotification()).thenReturn(notificationEdit); siteIds.add(siteId);
        when(siteService.getSites(SiteService.SelectionType.ANY, null, null, null, SiteService.SortType.NONE, null)).thenReturn(sites);
        when(siteService.isSpecialSite(siteId)).thenReturn(false);
        elasticSearchIndexBuilder = new ElasticSearchIndexBuilder();
        elasticSearchIndexBuilder.setOnlyIndexSearchToolSites(false);
        elasticSearchIndexBuilder.setExcludeUserSites(false);
        elasticSearchIndexBuilder.setSecurityService(securityService);
        elasticSearchIndexBuilder.setSiteService(siteService);
        elasticSearchIndexBuilder.setServerConfigurationService(serverConfigurationService);
        elasticSearchIndexBuilder.setDelay(10);
        elasticSearchIndexBuilder.setPeriod(2);

        elasticSearchIndexBuilder.setMapping("{\n" +
                "    \"sakai_doc\" : {\n" +
                "        \"properties\" : {\n" +
                "            \"siteid\" : {\n" +
                "                \"type\" : \"string\",\n" +
                "                \"index\" : \"not_analyzed\"\n" +
                "            },\n" +
                "            \"title\" : {\n" +
                "                \"type\" : \"string\",\n" +
                "                \"index\" : \"not_analyzed\"\n" +
                "            },\n" +
                "            \"url\" : {\n" +
                "                \"type\" : \"string\",\n" +
                "                \"index\" : \"not_analyzed\"\n" +
                "            },\n" +
                "            \"reference\" : {\n" +
                "                \"type\" : \"string\",\n" +
                "                \"index\" : \"not_analyzed\"\n" +
                "            },\n" +
                "            \"id\" : {\n" +
                "                \"type\" : \"string\",\n" +
                "                \"index\" : \"not_analyzed\"\n" +
                "            },\n" +
                "            \"tool\" : {\n" +
                "                \"type\" : \"string\",\n" +
                "                \"index\" : \"not_analyzed\"\n" +
                "            },\n" +
                "            \"container\" : {\n" +
                "                \"type\" : \"string\",\n" +
                "                \"index\" : \"not_analyzed\"\n" +
                "            },\n" +
                "            \"type\" : {\n" +
                "                \"type\" : \"string\",\n" +
                "                \"index\" : \"not_analyzed\"\n" +
                "            },\n" +
                "            \"subtype\" : {\n" +
                "                \"type\" : \"string\",\n" +
                "                \"index\" : \"not_analyzed\"\n" +
                "            },\n" +
                "            \"contents\" : {\n" +
                "                \"type\" : \"string\",\n" +
                "                \"index\" : \"analyzed\"\n" +
                "            }\n" +
                "\n" +
                "\n" +
                "        }\n" +
                "    }\n" +
                "}");
        elasticSearchIndexBuilder.init();

        elasticSearchService = new ElasticSearchService();
        elasticSearchService.setTriggerFunctions(new ArrayList<String>());
        elasticSearchService.setEventTrackingService(eventTrackingService);
        elasticSearchService.setServerConfigurationService(serverConfigurationService);
        elasticSearchService.setSessionManager(sessionManager);
        elasticSearchService.setUserDirectoryService(userDirectoryService);
        elasticSearchService.setNotificationService(notificationService);
        elasticSearchService.setSiteService(siteService);
        elasticSearchService.setIndexBuilder(elasticSearchIndexBuilder);
        elasticSearchIndexBuilder.setOnlyIndexSearchToolSites(false);
        elasticSearchService.init();
        elasticSearchIndexBuilder.assureIndex();

        elasticSearchIndexBuilder.registerEntityContentProducer(entityContentProducer);


    }

    private void addResources() {
        for (Event event : events)  {
            elasticSearchIndexBuilder.addResource(notification, event);
        }
    }

    @Test
    public void testAddResource() {
        elasticSearchIndexBuilder.addResource(notification, event);
        wait(2000);
        assertTrue(elasticSearchService.getNDocs() == 1);
    }

    @Test
    public void testGetSearchSuggestions(){
        elasticSearchIndexBuilder.addResource(notification, event);
        wait(2000);
        String[] suggestions = elasticSearchService.getSearchSuggestions("te", siteId, false);
        List suggestionList = Arrays.asList(suggestions);
        assertTrue(suggestionList.contains("te"));
        assertTrue(suggestionList.contains("tem"));
        assertTrue(suggestionList.contains("tempo"));
        assertTrue(suggestionList.contains("tempora"));

    }

    @Test
    public void deleteAllDocumentForSite(){
        elasticSearchIndexBuilder.addResource(notification, event);
        addResources();
        wait(2000);
        elasticSearchIndexBuilder.deleteAllDocumentForSite(siteId);
        try {
            SearchList list = elasticSearchService.search("asdf", siteIds, 0, 10);
            assertFalse(list.size() > 0 );
        } catch (InvalidSearchQueryException e) {
            e.printStackTrace();
            fail();
        }
        assertTrue(elasticSearchService.getPendingDocs() == 0);
        assertTrue(elasticSearchService.getNDocs() == 0);
    }

    protected void wait(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSearch() {
        elasticSearchIndexBuilder.addResource(notification, event);
        addResources();
        wait(2000);
        try {
            SearchList list = elasticSearchService.search("asdf", siteIds, 0, 10);
            assertNotNull(list.get(0) ) ;
            assertEquals(list.get(0).getReference(),resourceName);
        } catch (InvalidSearchQueryException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testRebuildSiteIndex() {
        elasticSearchIndexBuilder.addResource(notification, event);
        addResources();
        wait(2000);
        elasticSearchIndexBuilder.rebuildIndex(siteId);
    }


    //TODO this test is causing out of memory issue do not turn on until the problem can be addressed
    public void testRefreshSite(){
        elasticSearchIndexBuilder.addResource(notification, event);
        addResources();
        wait(2000);
        elasticSearchService.refreshSite(siteId);
    }


    @Test
    public void testRefresh(){
        elasticSearchIndexBuilder.addResource(notification, event);
        addResources();
        wait(2000);
        elasticSearchService.refreshInstance();
    }

    @Test
    public void testRebuild(){
        elasticSearchIndexBuilder.addResource(notification, event);
        addResources();
        wait(2000);
        elasticSearchService.rebuildInstance();
        wait(1000);
        assertTrue(elasticSearchIndexBuilder.getPendingDocuments() > 0);
        wait(5000);
        verify(entityContentProducer, atLeast(101)).getContent(any(String.class));
        assertTrue(elasticSearchIndexBuilder.getPendingDocuments() == 0);
        assertTrue(elasticSearchService.getNDocs() == 101);
    }

     public class Resource {
        private String content;
        private String siteId;
        private String name;

        Resource(String content, String siteId, String name) {
            this.content = content;
            this.siteId = siteId;
            this.name = name;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getSiteId() {
            return siteId;
        }

        public void setSiteId(String siteId) {
            this.siteId = siteId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
