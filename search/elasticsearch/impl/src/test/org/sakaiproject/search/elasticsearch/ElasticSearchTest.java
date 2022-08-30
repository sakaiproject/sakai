/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.search.elasticsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.InvalidSearchQueryException;
import org.sakaiproject.search.api.SearchList;
import org.sakaiproject.search.api.SearchResult;
import org.sakaiproject.search.elasticsearch.filter.impl.SearchSecurityFilter;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;

/**
 *
 *
 * Read this thread http://stackoverflow.com/questions/12935810/integration-test-elastic-search-timing-issue-document-not-found
 *
 * You have to call refresh on the index after you make any changes, before you query it.  Because ES
 * waits a second for more data to arrive.
 *
 *
 * User: jbush
 * Date: 1/16/13
 * Time: 9:10 PM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(MockitoJUnitRunner.class)
@Slf4j
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
    ThreadLocalManager threadLocalManager;// = new ThreadLocalComponent();

    @Mock
    NotificationEdit notificationEdit;

    SiteElasticSearchIndexBuilder elasticSearchIndexBuilder;

    @Mock
    Notification notification;

    @Mock
    Event event;

    @Mock
    EntityContentProducer entityContentProducer;

    @Mock
    Site site;

    private final Map<String, Resource> resources = new HashMap<>();
    private final List<Event> events = new ArrayList<>();

    List<String> siteIds = new ArrayList<>();
    List<Site> sites = new ArrayList<>();
    Faker faker = new Faker();
    String siteId = faker.bothify("########-????????-########");
    String resourceName = faker.name().name() + " key keyboard";
    SearchSecurityFilter filter = new SearchSecurityFilter();

    @After
    public void tearDown() {
        elasticSearchService.destroy();
        elasticSearchIndexBuilder.destroy();
        threadLocalManager.clear();
    }

    public void createTestResources() {
        String content = "asdf organ organize organizations " + generateContent();
        Resource resource = new Resource(content, siteId, resourceName);
        resources.put(resourceName, resource);

        when(event.getResource()).thenReturn(resource.getName());
        when(entityContentProducer.matches(event)).thenReturn(true);
        when(entityContentProducer.matches(resourceName)).thenReturn(true);
        when(entityContentProducer.getSiteId(resourceName)).thenReturn(siteId);
        when(entityContentProducer.getAction(event)).thenReturn(SearchBuilderItem.ACTION_ADD);
        when(entityContentProducer.getContent(resourceName)).thenReturn(content);
        when(entityContentProducer.getType(resourceName)).thenReturn("sakai:content");
        when(entityContentProducer.getId(resourceName)).thenReturn(resourceName);
        when(entityContentProducer.getTitle(resourceName)).thenReturn(resourceName);
        events.add(event);

        for (int i = 0; i < 105; i++) {
            String name = faker.name().name();
            Event newEvent = mock(Event.class);
            Resource resource1 = new Resource(generateContent(), faker.bothify("########-????????-########"), name);
            resources.put(name, resource1);
            events.add(newEvent);
            when(newEvent.getResource()).thenReturn(resource1.getName());
            when(entityContentProducer.matches(newEvent)).thenReturn(true);
            when(entityContentProducer.getSiteId(name)).thenReturn(resource1.getSiteId());
            when(entityContentProducer.getAction(newEvent)).thenReturn(SearchBuilderItem.ACTION_ADD);
            when(entityContentProducer.getContent(name)).thenReturn(resource1.getContent());
            when(entityContentProducer.getType(name)).thenReturn("sakai:content");
            when(entityContentProducer.getId(name)).thenReturn(name);
            when(entityContentProducer.canRead(any(String.class))).thenReturn(true);
        }

        when(entityContentProducer.getSiteContentIterator(siteId)).thenReturn(resources.keySet().iterator());
    }

    private String generateContent() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 10; i++) {
            sb.append(faker.lorem().paragraph(10) + " ");
        }
        return sb.toString();
    }

    @Before
    public void setUp() throws Exception {
        createTestResources();

        when(site.getId()).thenReturn(siteId);
        when(siteService.getSite(site.getId())).thenReturn(site);
        sites.add(site);
        when(serverConfigurationService.getBoolean("search.enable", false)).thenReturn(true);
        when(serverConfigurationService.getConfigData().getItems()).thenReturn(Collections.emptyList());
        long pid = ProcessHandle.current().pid();
        when(serverConfigurationService.getServerId()).thenReturn("node-" + pid);
        when(serverConfigurationService.getServerName()).thenReturn("cluster-" + pid);

        when(serverConfigurationService.getSakaiHomePath()).thenReturn(System.getProperty("java.io.tmpdir") + "/" + new Date().getTime());
        siteIds.add(siteId);
        when(siteService.getSites(SiteService.SelectionType.ANY, null, null, null, SiteService.SortType.NONE, null)).thenReturn(sites);
        when(siteService.isSpecialSite(siteId)).thenReturn(false);
        elasticSearchIndexBuilder = new SiteElasticSearchIndexBuilder();
        elasticSearchIndexBuilder.setName(ElasticSearchIndexBuilder.DEFAULT_INDEX_BUILDER_NAME);
        elasticSearchIndexBuilder.setIndexName(ElasticSearchIndexBuilder.DEFAULT_INDEX_NAME);
        elasticSearchIndexBuilder.setTestMode(true);
        elasticSearchIndexBuilder.setOnlyIndexSearchToolSites(false);
        elasticSearchIndexBuilder.setExcludeUserSites(false);
        elasticSearchIndexBuilder.setSecurityService(securityService);
        elasticSearchIndexBuilder.setSiteService(siteService);
        elasticSearchIndexBuilder.setServerConfigurationService(serverConfigurationService);
        elasticSearchIndexBuilder.setEventTrackingService(eventTrackingService);
        elasticSearchIndexBuilder.setUserDirectoryService(userDirectoryService);
        elasticSearchIndexBuilder.setSiteService(siteService);
        elasticSearchIndexBuilder.setFilter(filter);
        elasticSearchIndexBuilder.setIgnoredSites("!admin,~admin");
        elasticSearchIndexBuilder.setOnlyIndexSearchToolSites(false);
        elasticSearchIndexBuilder.setDelay(200);
        elasticSearchIndexBuilder.setPeriod(10);
        elasticSearchIndexBuilder.setContentIndexBatchSize(50);
        elasticSearchIndexBuilder.setBulkRequestSize(20);
        elasticSearchIndexBuilder.setMappingConfig(
                "\n{\n" +
                "    \"_source\": {\n" +
                "       \"enabled\": \"false\"\n" +
                "    },\n" +
                "    \"properties\": {\n" +
                "        \"siteid\": {\n" +
                "            \"type\": \"keyword\",\n" +
                "            \"store\": \"true\"\n" +
                "        },\n" +
                "        \"title\": {\n" +
                "            \"type\": \"text\",\n" +
                "            \"store\": \"true\",\n" +
                "            \"term_vector\" : \"with_positions_offsets\",\n" +
                "            \"search_analyzer\": \"str_search_analyzer\",\n" +
                "            \"analyzer\": \"str_index_analyzer\"\n" +
                "        },\n" +
                "        \"url\": {\n" +
                "            \"type\": \"keyword\",\n" +
                "            \"store\": \"true\"\n" +
                "        },\n" +
                "        \"reference\": {\n" +
                "            \"type\": \"keyword\",\n" +
                "            \"store\": \"true\"\n" +
                "        },\n" +
                "        \"id\": {\n" +
                "            \"type\": \"keyword\",\n" +
                "            \"store\": \"true\"\n" +
                "        },\n" +
                "        \"tool\": {\n" +
                "            \"type\": \"keyword\",\n" +
                "            \"store\": \"true\"\n" +
                "        },\n" +
                "        \"container\": {\n" +
                "            \"type\": \"keyword\",\n" +
                "            \"store\": \"true\"\n" +
                "        },\n" +
                "        \"type\": {\n" +
                "            \"type\": \"keyword\",\n" +
                "            \"store\": \"true\"\n" +
                "        },\n" +
                "        \"subtype\": {\n" +
                "            \"type\": \"keyword\",\n" +
                "            \"store\": \"true\"\n" +
                "        },\n" +
                "        \"indexed\": {\n" +
                "            \"type\": \"boolean\",\n" +
                "            \"null_value\": \"false\",\n" +
                "            \"store\": \"false\"\n" +
                "        },\n" +
                "        \"contents\": {\n" +
                "            \"type\": \"text\",\n" +
                "            \"analyzer\": \"snowball\",\n" +
                "            \"index\": \"true\",\n" +
                "            \"store\": \"false\"\n" +
                "        }\n" +
                "    }\n" +
                "}\n");
        elasticSearchIndexBuilder.setIndexSettingsConfig(
                "\n{\n" +
                "    \"number_of_shards\": 1,\n" +
                "    \"index\": {\n" +
                "        \"max_ngram_diff\": 20" +
                "    },\n" +
                "    \"analysis\": {\n" +
                "        \"filter\": {\n" +
                "            \"substring\": {\n" +
                "                \"type\": \"ngram\",\n" +
                "                \"min_gram\": 1,\n" +
                "                \"max_gram\": 20\n" +
                "            }\n" +
                "        },\n" +
                "        \"analyzer\": {\n" +
                "            \"standard\": {\n" +
                "                \"type\": \"standard\",\n" +
                "                \"max_token_length\": \"255\"\n" +
                "            },\n" +
                "            \"str_search_analyzer\": {\n" +
                "                \"tokenizer\": \"keyword\",\n" +
                "                \"filter\": [\"lowercase\"]\n" +
                "            },\n" +
                "            \"str_index_analyzer\": {\n" +
                "                \"tokenizer\": \"keyword\",\n" +
                "                \"filter\": [\"lowercase\", \"substring\"]\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}\n");

        filter.setSearchIndexBuilder(elasticSearchIndexBuilder);

        elasticSearchService = new ElasticSearchService();
        elasticSearchService.setTriggerFunctions(new ArrayList<String>());

        elasticSearchService.setServerConfigurationService(serverConfigurationService);
        elasticSearchService.setSessionManager(sessionManager);
        elasticSearchService.setUserDirectoryService(userDirectoryService);
        elasticSearchService.setNotificationService(notificationService);
        elasticSearchService.setThreadLocalManager(threadLocalManager);
        elasticSearchService.setLocalNode(true);
        elasticSearchService.init();

        elasticSearchIndexBuilder.registerEntityContentProducer(entityContentProducer);
        elasticSearchService.registerIndexBuilder(elasticSearchIndexBuilder);

    }

    @Test
    public void testAddingResourceWithNoContent(){
        Resource resource = new Resource(null, "xyz", "resource_with_no_content");

        when(event.getResource()).thenReturn(resource.getName());
        List resourceList = new ArrayList();
        resourceList.add(resource);

        elasticSearchIndexBuilder.addResource(notification, event);

        assertTrue(elasticSearchService.getNDocs() == 0);

    }


    private void addResources() {
        for (Event event : events)  {
            try {
                elasticSearchIndexBuilder.addResource(notification, event);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                assertFalse("problem adding event: " + event.getEvent(), true);
            }
        }
    }

    @Test
    public void testAddResource() {
        elasticSearchIndexBuilder.addResource(notification, event);
        addResources();
        elasticSearchIndexBuilder.refreshIndex();
        long numberOfDocs = elasticSearchService.getNDocs();
        assertTrue("The number of docs is " + numberOfDocs + " expecting 106.", numberOfDocs == 106);
    }

    @Test
    public void testGetSearchSuggestions() {
        elasticSearchIndexBuilder.addResource(notification, event);
        elasticSearchIndexBuilder.refreshIndex();
        String[] suggestions = elasticSearchService.getSearchSuggestions("key", siteId, false);
        List suggestionList = Arrays.asList(suggestions);
        assertTrue(suggestionList.contains(resourceName));

        suggestions = elasticSearchService.getSearchSuggestions("keyboard", siteId, false);
        suggestionList = Arrays.asList(suggestions);
        assertTrue(suggestionList.contains(resourceName));

    }

    @Test
    public void deleteDoc(){
        assertTrue(elasticSearchService.getNDocs() == 0);
        elasticSearchIndexBuilder.addResource(notification, event);
        elasticSearchIndexBuilder.refreshIndex();
        assertTrue(elasticSearchService.getNDocs() == 1);
        elasticSearchIndexBuilder.deleteDocument(resourceName, siteId);
        elasticSearchIndexBuilder.refreshIndex();
        assertTrue(elasticSearchService.getNDocs() == 0);
        try {
            SearchList list = elasticSearchService.search("asdf", siteIds, 0, 10);
            assertFalse(list.size() > 0 );
        } catch (InvalidSearchQueryException e) {
            log.error(e.getMessage(), e);
            fail();
        }

    }

    @Test
    public void deleteAllDocumentForSite(){
        elasticSearchIndexBuilder.addResource(notification, event);
        addResources();
        elasticSearchIndexBuilder.refreshIndex();
        elasticSearchIndexBuilder.deleteAllDocumentForSite(siteId);

        try {
            SearchList list = elasticSearchService.search("asdf", siteIds, 0, 10);
            assertFalse(list.size() > 0);
        } catch (InvalidSearchQueryException e) {
            log.error(e.getMessage(), e);
            fail();
        }
        assertTrue(elasticSearchService.getPendingDocs() == 0);
    }

    protected void wait(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Test
    public void testSearch() {
        elasticSearchIndexBuilder.addResource(notification, event);
        addResources();
        elasticSearchIndexBuilder.refreshIndex();

        try {
            SearchList list = elasticSearchService.search("asdf", siteIds, 0, 10);
            assertNotEquals(0, list.size());
            SearchResult result = list.get(0);
            assertNotNull(result);
            assertEquals(result.getReference(), resourceName);
            assertTrue(result.getSearchResult().toLowerCase().contains("<b>"));

            // Searching the title of the file should also return results
            list = elasticSearchService.search("keyboard", siteIds, 0, 10);
            assertNotEquals(0, list.size());
            assertNotNull(list.get(0));
            assertEquals(list.get(0).getReference(), resourceName);
        } catch (InvalidSearchQueryException e) {
            log.error(e.getMessage(), e);
            fail();
        }
    }

    @Test
    public void testRebuildSiteIndex() {
        elasticSearchIndexBuilder.addResource(notification, event);
        addResources();
        elasticSearchIndexBuilder.rebuildIndex(siteId);
        elasticSearchIndexBuilder.setContentIndexBatchSize(200);
        elasticSearchIndexBuilder.setBulkRequestSize(400);

        elasticSearchIndexBuilder.refreshIndex();

        elasticSearchIndexBuilder.processContentQueue();

        elasticSearchIndexBuilder.refreshIndex();

        log.info("testRebuildSiteIndex: " + elasticSearchService.getNDocs());
        assertTrue(elasticSearchService.getNDocs() == 106);
    }

    @Test
    public void testRefreshSite(){
        elasticSearchIndexBuilder.setContentIndexBatchSize(200);
        elasticSearchIndexBuilder.setBulkRequestSize(20);

        elasticSearchIndexBuilder.addResource(notification, event);
        addResources();

        elasticSearchIndexBuilder.refreshIndex();

        elasticSearchIndexBuilder.processContentQueue();

        elasticSearchIndexBuilder.refreshIndex();

        assertTrue(elasticSearchService.getNDocs() == 106);

        elasticSearchService.refreshSite(siteId);

        assertTrue("the number of pending docs is " + elasticSearchIndexBuilder.getPendingDocuments() + ", expecting 0.",
                elasticSearchIndexBuilder.getPendingDocuments() == 0);
        assertTrue(elasticSearchService.getNDocs() == 106);

    }

    @Test
    public void testRefresh() {
        elasticSearchIndexBuilder.addResource(notification, event);
        addResources();
        elasticSearchService.refreshInstance();
        assertTrue(elasticSearchService.getNDocs() == 106);
    }

    @Test
    public void testRebuild(){
        elasticSearchIndexBuilder.setContentIndexBatchSize(200);
        elasticSearchIndexBuilder.setBulkRequestSize(400);
        elasticSearchIndexBuilder.addResource(notification, event);

        // add in a resource with no content
        String resourceName = "billy bob";
        Resource resource = new Resource(null, siteId, resourceName);
        resources.put(resourceName, resource);

        Event newEvent = mock(Event.class);
        when(newEvent.getResource()).thenReturn(resource.getName());
        when(entityContentProducer.matches(newEvent)).thenReturn(true);
        when(entityContentProducer.getSiteId(resourceName)).thenReturn(siteId);
        when(entityContentProducer.getAction(newEvent)).thenReturn(SearchBuilderItem.ACTION_ADD);
        when(entityContentProducer.getContent(resourceName)).thenReturn(null);
        when(entityContentProducer.getType(resourceName)).thenReturn("sakai:content");
        when(entityContentProducer.getId(resourceName)).thenReturn(resourceName);
        when(entityContentProducer.getTitle(resourceName)).thenReturn(resourceName);
        events.add(newEvent);

        addResources();

        when(entityContentProducer.getSiteContentIterator(siteId)).thenReturn(resources.keySet().iterator());
        elasticSearchService.rebuildInstance();
        elasticSearchIndexBuilder.refreshIndex();
        elasticSearchIndexBuilder.processContentQueue();
        elasticSearchIndexBuilder.refreshIndex();

        verify(entityContentProducer, atLeast(106)).getContent(any(String.class));

        int pendingDocs = elasticSearchIndexBuilder.getPendingDocuments();
        long indexedDocs = elasticSearchService.getNDocs();
        assertTrue("pending doc=" + pendingDocs + ", expecting 0", pendingDocs == 0);
        assertTrue("num doc=" + indexedDocs + ", expecting 106.", indexedDocs == 106);
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
