/**
 * $URL$
 * $Id$
 * <p>
 * Copyright (c) 2006-2009 The Sakai Foundation
 * <p>
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.opensource.org/licenses/ECL-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.test.perf;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.content.api.ContentTypeImageService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.StatsUpdateManager;
import org.sakaiproject.sitestats.impl.StatsManagerImpl;
import org.sakaiproject.sitestats.impl.StatsUpdateManagerImpl;
import org.sakaiproject.sitestats.test.DB;
import org.sakaiproject.sitestats.test.SiteStatsTestConfiguration;
import org.sakaiproject.sitestats.test.data.FakeData;
import org.sakaiproject.sitestats.test.mocks.FakeSite;
import org.springframework.aop.framework.Advised;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

/**
 *  This not an actual unit test but a way to test load performance
 *  the uses the unit test framework
 *
 *  Also it doesn't make much sense to run this against hsqldb
 *  you would likely want to uncomment and use a more suitable
 *  environment in hibernate.properties
 */
@ContextConfiguration(classes = {SiteStatsTestConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
@Slf4j
@Transactional(transactionManager = "org.sakaiproject.sitestats.SiteStatsTransactionManager")
public class EventAggregatorTestPerf extends AbstractTransactionalJUnit4SpringContextTests {
    private static final int MAX_USERS = 250;
    private static final int MAX_RESOURCES = 50;
    private static final int COUNT_USERS_SMALL = 10;
    private static final int COUNT_USERS_MEDIUM = 100;
    private static final int COUNT_USERS_LARGE = MAX_USERS;
    private static final int COUNT_RESOURCES_SMALL = 5;
    private static final int COUNT_RESOURCES_MEDIUM = 10;
    private static final int COUNT_RESOURCES_LARGE = MAX_RESOURCES;

    @Resource(name = "org.sakaiproject.content.api.ContentTypeImageService")
	private ContentTypeImageService contentTypeImageService;
    @Resource(name = "org.sakaiproject.sitestats.test.DB")
    private DB db;
    @Resource(name = "org.sakaiproject.memory.api.MemoryService")
    private MemoryService memoryService;
    @Resource(name = "org.sakaiproject.site.api.SiteService")
    private SiteService siteService;
    @Resource(name = "org.sakaiproject.sitestats.api.StatsManager")
    private StatsManager statsManager;
    @Resource(name = "org.sakaiproject.sitestats.api.StatsUpdateManager")
    private StatsUpdateManager statsUpdateManager;

    private List<String> siteResources;
    private List<String> siteUsers;

    @Before
    public void onSetUp() throws Exception {
        db.deleteAll();
        memoryService.resetCachers();

        // Setup site users
        siteUsers = new ArrayList<>();
        IntStream.range(0, MAX_USERS).forEach(i -> siteUsers.add("user-" + i));
        siteResources = new ArrayList<>();
        IntStream.range(0, MAX_RESOURCES).forEach(i -> siteResources.add("/content/group/" + FakeData.SITE_A_ID + "/resource-" + i));

        when(contentTypeImageService.getContentTypeImage("folder")).thenReturn("sakai/folder.gif");
        when(contentTypeImageService.getContentTypeImage("image/png")).thenReturn("sakai/image.gif");

        FakeSite userSiteA = spy(FakeSite.class).set("~" + FakeData.USER_A_ID);
        userSiteA.setUsers(new HashSet<>(siteUsers));
        userSiteA.setMembers(new HashSet<>(siteUsers));
        when(siteService.getSiteUserId(FakeData.USER_A_ID)).thenReturn("~" + FakeData.USER_A_ID);
        when(siteService.getSiteUserId("no_user")).thenReturn(null);
        when(siteService.getSite("~" + FakeData.USER_A_ID)).thenReturn(userSiteA);

        // Site A has tools {SiteStats, Chat}, has {user-a,user-b}
        FakeSite siteA = spy(FakeSite.class).set(FakeData.SITE_A_ID, Arrays.asList(StatsManager.SITESTATS_TOOLID, FakeData.TOOL_CHAT, StatsManager.RESOURCES_TOOLID));
        siteA.setUsers(new HashSet<>(Arrays.asList(FakeData.USER_A_ID, FakeData.USER_B_ID)));
        siteA.setMembers(new HashSet<>(Arrays.asList(FakeData.USER_A_ID, FakeData.USER_B_ID)));
        when(siteService.getSite(FakeData.SITE_A_ID)).thenReturn(siteA);
        when(siteService.isUserSite(FakeData.SITE_A_ID)).thenReturn(false);
        when(siteService.isSpecialSite(FakeData.SITE_A_ID)).thenReturn(false);

        // This is needed to make the tests deterministic, otherwise on occasion the collect thread will run
        // and break the tests.
        statsUpdateManager.setCollectThreadEnabled(true);

        ((StatsManagerImpl) ((Advised) statsManager).getTargetSource().getTarget()).setShowAnonymousAccessEvents(true);
        ((StatsManagerImpl) ((Advised) statsManager).getTargetSource().getTarget()).setEnableSitePresences(true);
    }

    @Test
    public void testSmall() {
        try {
            doBasicTest(COUNT_USERS_SMALL, COUNT_RESOURCES_SMALL, 1);
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }

    @Ignore
    @Test
    public void testMedium() {
        try {
            doBasicTest(COUNT_USERS_MEDIUM, COUNT_RESOURCES_MEDIUM, 1);
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }

    @Ignore
    @Test
    public void testLarge() {
        try {
            doBasicTest(COUNT_USERS_LARGE, COUNT_RESOURCES_LARGE, 1);
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }


    private void doBasicTest(int userCount, int resourceCount, int dayCount) throws Exception {
        // Check maximum values
        if (userCount > MAX_USERS) {
            throw new IllegalArgumentException("Configured value for user count exceeds MAX_USERS value.");
        }
        if (resourceCount > MAX_RESOURCES) {
            throw new IllegalArgumentException("Configured value for resource count exceeds MAX_RESOURCES value.");
        }

        // Do basic test
        Date today = new Date();
        String user0 = "user-0";
        String sitePresenceRef = "/presence/" + FakeData.SITE_A_ID + "-presence";
        // 1. Instructor logs in to add files
        Event e1 = statsUpdateManager.buildEvent(today, StatsManager.SITEVISIT_EVENTID, sitePresenceRef, FakeData.SITE_A_REF, user0, "session-id");
        StatsUpdateManagerImpl sumi = ((StatsUpdateManagerImpl) ((Advised) statsUpdateManager).getTargetSource().getTarget());
        sumi.update(null, e1);
        for (int r = 0; r < resourceCount; r++) {
            Event e2 = statsUpdateManager.buildEvent(today, FakeData.EVENT_CONTENTNEW, siteResources.get(r), FakeData.SITE_A_REF, user0, "session-id");
            sumi.update(null, e2);
        }
        // 2. Students logs in to chat and read files
        for (int u = 0; u < userCount; u++) {
            String user = siteUsers.get(u);
            // 2.1. Generate a site visit
            Event e3 = statsUpdateManager.buildEvent(today, StatsManager.SITEVISIT_EVENTID, sitePresenceRef, FakeData.SITE_A_REF, user, "session-id");
            sumi.update(null, e3);
            // 2.2 Read [resourceCount] files
            for (int r = 0; r < resourceCount; r++) {
                Event e4 = statsUpdateManager.buildEvent(today, FakeData.EVENT_CONTENTREAD, siteResources.get(r), FakeData.SITE_A_REF, user, "session-id");
                sumi.update(null, e4);
            }
        }

        // Wait until StatsUpdateManager becomes idle...
        while (!statsUpdateManager.isIdle()) {
            Thread.sleep(250);
        }

        // Print result summary
        StringBuilder sb = new StringBuilder("Results for ");
        sb.append(userCount).append(" users, ").append(resourceCount).append(" resources, ").append(dayCount).append(" days: ");
        sb.append(statsUpdateManager.getMetricsSummary(true));
        statsUpdateManager.resetMetrics();
        log.info(sb.toString());
    }
}
