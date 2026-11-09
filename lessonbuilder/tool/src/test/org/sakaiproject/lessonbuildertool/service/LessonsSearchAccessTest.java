/**
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.lessonbuildertool.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.sakaiproject.archive.api.ArchiveService;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.condition.api.ConditionService;
import org.sakaiproject.content.api.ContentTypeImageService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageLogEntry;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tasks.api.TaskService;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.ActiveToolManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.api.FormattedText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import uk.org.ponder.messageutil.MessageLocator;

/**
 * The real producer, LessonsAccess, SimplePageBean and DAO against the test database. Only the kernel services and
 * other tools are test substitutes.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {LessonBuilderServiceTestConfiguration.class, LessonsSearchAccessTest.OtherServices.class})
@Transactional
public class LessonsSearchAccessTest {

    private static final String SITE_ID = "site";
    private static final String USER_ID = "member";
    private static final String GROUP_A = "groupA";
    private static final String GROUP_B = "groupB";

    @Configuration
    static class OtherServices {
        @Bean public ArchiveService archiveService() { return mock(ArchiveService.class); }
        @Bean public AssignmentService assignmentService() { return mock(AssignmentService.class); }
        @Bean public ConditionService conditionService() { return mock(ConditionService.class); }
        @Bean public ContentTypeImageService contentTypeImageService() { return mock(ContentTypeImageService.class); }
        @Bean public FormattedText formattedText() { return mock(FormattedText.class); }
        @Bean public IdManager idManager() { return mock(IdManager.class); }
        @Bean public LTIService ltiService() { return mock(LTIService.class); }
        @Bean public LessonBuilderAccessService lessonBuilderAccessService() { return mock(LessonBuilderAccessService.class); }
        @Bean public LessonBuilderEntityProducer lessonBuilderEntityProducer() { return mock(LessonBuilderEntityProducer.class); }
        @Bean public MessageLocator messageLocator() { return mock(MessageLocator.class); }
        @Bean public NotificationService notificationService() { return mock(NotificationService.class); }
        @Bean public TaskService taskService() { return mock(TaskService.class); }
        // beside UserTimeService, as in production, so this context needs the bean's qualifier too
        @Bean(name = "org.sakaiproject.time.api.TimeService") public TimeService timeService() { return mock(TimeService.class); }
        @Bean(name = "org.sakaiproject.lessonbuildertool.service.BltiEntity") public LessonEntity bltiEntity() { return mock(LessonEntity.class); }
        @Bean(name = "org.sakaiproject.lessonbuildertool.service.ScormEntity") public LessonEntity scormEntity() { return mock(LessonEntity.class); }
    }

    @Autowired private LessonsAccess lessonsAccess;
    @Autowired private SimplePageToolDao dao;
    @Autowired private AuthzGroupService authzGroupService;
    @Autowired private SecurityService securityService;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Autowired private SiteService siteService;
    @Autowired @Qualifier("org.sakaiproject.time.api.UserTimeService") private UserTimeService userTimeService;
    @Autowired private ActiveToolManager toolManager;
    @Autowired private org.sakaiproject.user.api.UserDirectoryService userDirectoryService;
    @Autowired private User lessonBuilderTestUser;

    private final Site site = mock(Site.class);
    private MockedStatic<UserDirectoryService> currentUser;
    private LessonsEntityContentProducer producer;
    private SimplePage subpage;
    private SimplePageItem link;
    private SimplePageItem text;

    @Before
    public void setUp() throws Exception {
        reset(authzGroupService, securityService, serverConfigurationService, siteService, toolManager, userTimeService,
                userDirectoryService, lessonBuilderTestUser);
        when(serverConfigurationService.getString(anyString())).thenReturn("");
        when(serverConfigurationService.getString(anyString(), anyString())).thenAnswer(call -> call.getArgument(1));
        when(userTimeService.getLocalTimeZone()).thenReturn(TimeZone.getDefault());
        when(lessonBuilderTestUser.getId()).thenReturn(USER_ID);
        when(userDirectoryService.getCurrentUser()).thenReturn(lessonBuilderTestUser);
        when(siteService.siteReference(SITE_ID)).thenReturn("/site/" + SITE_ID);
        when(siteService.getSite(SITE_ID)).thenReturn(site);
        when(securityService.unlock(SimplePage.PERMISSION_LESSONBUILDER_READ, "/site/" + SITE_ID)).thenReturn(true);
        memberOfGroups();

        currentUser = mockStatic(UserDirectoryService.class);
        currentUser.when(UserDirectoryService::getCurrentUser).thenReturn(lessonBuilderTestUser);

        producer = new LessonsEntityContentProducer();
        producer.setSimplePageToolDao(dao);
        producer.setSecurityService(securityService);
        producer.setSiteService(siteService);
        producer.setLessonsAccess(lessonsAccess);

        SimplePage topPage = dao.makePage("tool", SITE_ID, "Top", null, null);
        assertTrue(dao.quickSaveItem(topPage));
        assertTrue(dao.quickSaveItem(dao.makeItem(0, 0, SimplePageItem.PAGE, Long.toString(topPage.getPageId()), "Top")));
        subpage = dao.makePage("tool", SITE_ID, "Readings", topPage.getPageId(), topPage.getPageId());
        assertTrue(dao.quickSaveItem(subpage));
        link = dao.makeItem(topPage.getPageId(), 1, SimplePageItem.PAGE, Long.toString(subpage.getPageId()), "Readings");
        assertTrue(dao.quickSaveItem(link));
        text = dao.makeItem(subpage.getPageId(), 2, SimplePageItem.TEXT, "", "");
        assertTrue(dao.quickSaveItem(text));
    }

    @After
    public void closeCurrentUser() {
        currentUser.close();
    }

    @Test
    public void memberCanReadTextOnAnUnrestrictedSubpage() {
        assertTrue(canReadText());
    }

    @Test
    public void userWhoCannotVisitTheSiteCannotRead() throws Exception {
        when(siteService.getSiteVisit(SITE_ID)).thenThrow(new PermissionException(USER_ID, "site.visit", SITE_ID));

        assertFalse(canReadText());
    }

    @Test
    public void memberWithoutLessonsReadPermissionCannotRead() {
        when(securityService.unlock(SimplePage.PERMISSION_LESSONBUILDER_READ, "/site/" + SITE_ID)).thenReturn(false);

        assertFalse(canReadText());
    }

    @Test
    public void textOnAHiddenPageCannotBeRead() {
        subpage.setHidden(true);
        assertTrue(dao.quickUpdate(subpage));

        assertFalse(canReadText());
    }

    @Test
    public void textOnAPageNotYetReleasedCannotBeRead() {
        subpage.setReleaseDate(new Date(System.currentTimeMillis() + 86400000L));
        assertTrue(dao.quickUpdate(subpage));

        assertFalse(canReadText());
    }

    @Test
    public void memberCanReadTheLinkToAReleasedSubpage() {
        assertTrue(canReadLink());
    }

    @Test
    public void linkToASubpageNotYetReleasedCannotBeRead() {
        subpage.setReleaseDate(new Date(System.currentTimeMillis() + 86400000L));
        assertTrue(dao.quickUpdate(subpage));

        assertFalse(canReadLink());
    }

    @Test
    public void linkToAHiddenSubpageCannotBeRead() {
        subpage.setHidden(true);
        assertTrue(dao.quickUpdate(subpage));

        assertFalse(canReadLink());
    }

    @Test
    public void linkToASubpageNotYetReleasedCanBeReadByThoseWhoSeeAll() {
        subpage.setReleaseDate(new Date(System.currentTimeMillis() + 86400000L));
        assertTrue(dao.quickUpdate(subpage));

        when(securityService.unlock(SimplePage.PERMISSION_LESSONBUILDER_SEE_ALL, "/site/" + SITE_ID)).thenReturn(true);
        assertTrue(canReadLink());

        reset(securityService);
        when(securityService.unlock(SimplePage.PERMISSION_LESSONBUILDER_READ, "/site/" + SITE_ID)).thenReturn(true);
        when(securityService.unlock(SimplePage.PERMISSION_LESSONBUILDER_UPDATE, "/site/" + SITE_ID)).thenReturn(true);
        assertTrue(canReadLink());
    }

    @Test
    public void linkToAMissingPageCannotBeRead() {
        link.setSakaiId("0");
        assertTrue(dao.quickUpdate(link));

        assertFalse(canReadLink());
    }

    @Test
    public void textBehindAnUnmetPrerequisiteCanBeReadOnlyOnceItIsMet() {
        SimplePageItem required = dao.makeItem(subpage.getPageId(), 1, SimplePageItem.PAGE, Long.toString(emptyPage().getPageId()), "First step");
        required.setRequired(true);
        assertTrue(dao.quickSaveItem(required));
        text.setPrerequisite(true);
        assertTrue(dao.quickUpdate(text));

        assertFalse(canReadText());

        SimplePageLogEntry entry = dao.makeLogEntry(USER_ID, required.getId(), null);
        entry.setComplete(true);
        assertTrue(dao.quickSaveItem(entry));

        assertTrue(canReadText());
    }

    @Test
    public void subpageBehindAPrerequisiteLinkNeedsThatLinkToHaveBeenReached() {
        link.setPrerequisite(true);
        assertTrue(dao.quickUpdate(link));

        assertFalse(canReadText());

        assertTrue(dao.quickSaveItem(dao.makeLogEntry(USER_ID, link.getId(), null)));

        assertTrue(canReadText());
    }

    @Test
    public void groupRestrictedLinkIsFollowedOnlyByItsGroup() {
        restrict(link, GROUP_A);

        memberOfGroups(GROUP_B);
        assertFalse(canReadText());

        memberOfGroups(GROUP_A);
        assertTrue(canReadText());
    }

    @Test
    public void memberOfEitherGroupFollowsTheLinkForTheirOwnGroup() {
        restrict(link, GROUP_A);
        SimplePageItem otherLink = secondLinkRestrictedTo(GROUP_B);

        memberOfGroups(GROUP_B);
        assertTrue(canReadText());
        assertEquals(otherLink.getId(), newBean().findAccessibleLink(link).getId());

        memberOfGroups(GROUP_A);
        assertTrue(canReadText());
        assertEquals(link.getId(), newBean().findAccessibleLink(link).getId());
    }

    @Test
    public void memberOfNeitherGroupHasNoLinkToFollow() {
        restrict(link, GROUP_A);
        secondLinkRestrictedTo(GROUP_B);

        assertFalse(canReadText());
        assertEquals(link.getId(), newBean().findAccessibleLink(link).getId());
    }

    @Test
    public void anotherLinkBehindAnUnmetPrerequisiteIsNotOffered() {
        restrict(link, GROUP_A);
        SimplePageItem otherLink = secondLinkRestrictedTo(GROUP_B);
        SimplePageItem required = dao.makeItem(otherLink.getPageId(), 0, SimplePageItem.PAGE, Long.toString(emptyPage().getPageId()), "First step");
        required.setRequired(true);
        assertTrue(dao.quickSaveItem(required));
        otherLink.setPrerequisite(true);
        assertTrue(dao.quickUpdate(otherLink));
        memberOfGroups(GROUP_B);

        assertEquals(link.getId(), newBean().findAccessibleLink(link).getId());
    }

    private boolean canReadText() {
        return producer.canRead("/lessonbuilder/item/" + text.getId());
    }

    private boolean canReadLink() {
        return producer.canRead("/lessonbuilder/item/" + link.getId());
    }

    // following a link happens inside the Lessons tool, which has a placement
    private SimplePageBean newBean() {
        Placement placement = mock(Placement.class);
        when(placement.getPlacementConfig()).thenReturn(new Properties());
        when(toolManager.getCurrentPlacement()).thenReturn(placement);
        SimplePageBean bean = lessonsAccess.newSimplePageBean();
        bean.setCurrentSiteId(SITE_ID);
        bean.setCurrentPage(subpage);
        bean.setCurrentPageId(subpage.getPageId());
        return bean;
    }

    private SimplePage emptyPage() {
        SimplePage page = dao.makePage("tool", SITE_ID, "First step", subpage.getPageId(), subpage.getTopParent());
        assertTrue(dao.quickSaveItem(page));
        return page;
    }

    private SimplePageItem secondLinkRestrictedTo(String group) {
        SimplePageItem otherLink = dao.makeItem(link.getPageId(), 2, SimplePageItem.PAGE, Long.toString(subpage.getPageId()), "Readings again");
        assertTrue(dao.quickSaveItem(otherLink));
        restrict(otherLink, group);
        return otherLink;
    }

    private void restrict(SimplePageItem item, String group) {
        item.setGroups(group);
        assertTrue(dao.quickUpdate(item));
    }

    // LessonsAccess asks the authz service and the bean asks the site, so both have to agree
    private void memberOfGroups(String... groupIds) {
        Set<String> mine = Set.of(groupIds);
        Collection<Group> groups = mine.stream().map(id -> {
            Group group = mock(Group.class);
            when(group.getId()).thenReturn(id);
            return group;
        }).collect(Collectors.toList());
        when(site.getGroupsWithMember(USER_ID)).thenReturn(groups);
        doAnswer(call -> {
            Collection<String> references = call.getArgument(0);
            return references.stream()
                    .filter(reference -> mine.contains(reference.substring(reference.lastIndexOf('/') + 1)))
                    .collect(Collectors.toList());
        }).when(authzGroupService).getAuthzUserGroupIds(any(), any());
    }
}
