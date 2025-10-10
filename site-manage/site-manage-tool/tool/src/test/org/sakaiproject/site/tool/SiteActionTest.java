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
package org.sakaiproject.site.tool;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.archive.api.ArchiveService;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.userauditservice.api.UserAuditRegistration;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.util.api.LinkMigrationHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class SiteActionTest {

    private static final String[] PROJECT_SITE_TYPES = {"project"};

    @Mock private AliasService aliasService;
    @Mock private ArchiveService archiveService;
    @Mock private AuthzGroupService authzGroupService;
    @Mock private ContentHostingService contentHostingService;
    @Mock private CourseManagementService courseManagementService;
    @Mock private EntityManager entityManager;
    @Mock private EventTrackingService eventTrackingService;
    @Mock private FormattedText formattedText;
    @Mock private IdManager idManager;
    @Mock private LinkMigrationHelper linkMigrationHelper;
    @Mock private LTIService ltiService;
    @Mock private MemoryService memoryService;
    @Mock private PreferencesService preferencesService;
    @Mock private SecurityService securityService;
    @Mock private ServerConfigurationService serverConfigurationService;
    @Mock private SessionManager sessionManager;
    @Mock private SiteService siteService;
    @Mock private ThreadLocalManager threadLocalManager;
    @Mock private ToolManager toolManager;
    @Mock private UserAuditRegistration userAuditRegistration;
    @Mock private UserDirectoryService userDirectoryService;
    @Mock private UserTimeService userTimeService;

    private MockedStatic<ComponentManager> componentManagerMock;
    private SiteAction siteAction;

    @Before
    public void setUp() throws Exception {
        componentManagerMock = Mockito.mockStatic(ComponentManager.class);

        componentManagerMock.when(() -> ComponentManager.get(AliasService.class)).thenReturn(aliasService);
        componentManagerMock.when(() -> ComponentManager.get(ArchiveService.class)).thenReturn(archiveService);
        componentManagerMock.when(() -> ComponentManager.get(AuthzGroupService.class)).thenReturn(authzGroupService);
        componentManagerMock.when(() -> ComponentManager.get(CourseManagementService.class)).thenReturn(courseManagementService);
        componentManagerMock.when(() -> ComponentManager.get(EntityManager.class)).thenReturn(entityManager);
        componentManagerMock.when(() -> ComponentManager.get(EventTrackingService.class)).thenReturn(eventTrackingService);
        componentManagerMock.when(() -> ComponentManager.get(FormattedText.class)).thenReturn(formattedText);
        componentManagerMock.when(() -> ComponentManager.get(IdManager.class)).thenReturn(idManager);
        componentManagerMock.when(() -> ComponentManager.get(MemoryService.class)).thenReturn(memoryService);
        componentManagerMock.when(() -> ComponentManager.get(PreferencesService.class)).thenReturn(preferencesService);
        componentManagerMock.when(() -> ComponentManager.get(SecurityService.class)).thenReturn(securityService);
        componentManagerMock.when(() -> ComponentManager.get(ServerConfigurationService.class)).thenReturn(serverConfigurationService);
        componentManagerMock.when(() -> ComponentManager.get(SessionManager.class)).thenReturn(sessionManager);
        componentManagerMock.when(() -> ComponentManager.get(SiteService.class)).thenReturn(siteService);
        componentManagerMock.when(() -> ComponentManager.get(ThreadLocalManager.class)).thenReturn(threadLocalManager);
        componentManagerMock.when(() -> ComponentManager.get(ToolManager.class)).thenReturn(toolManager);
        componentManagerMock.when(() -> ComponentManager.get(UserDirectoryService.class)).thenReturn(userDirectoryService);
        componentManagerMock.when(() -> ComponentManager.get(UserTimeService.class)).thenReturn(userTimeService);
        componentManagerMock.when(() -> ComponentManager.get("org.sakaiproject.content.api.ContentHostingService")).thenReturn(contentHostingService);
        componentManagerMock.when(() -> ComponentManager.get("org.sakaiproject.util.api.LinkMigrationHelper")).thenReturn(linkMigrationHelper);
        componentManagerMock.when(() -> ComponentManager.get("org.sakaiproject.lti.api.LTIService")).thenReturn(ltiService);
        componentManagerMock.when(() -> ComponentManager.get("org.sakaiproject.userauditservice.api.UserAuditRegistration.sitemanage")).thenReturn(userAuditRegistration);

        siteAction = new SiteAction();
    }

    @After
    public void tearDown() {
        if (componentManagerMock != null) {
            componentManagerMock.close();
        }
    }

    private void configureProjectSiteTypes(String... types) {
        Mockito.when(siteService.getSiteTypeStrings("project")).thenReturn(Arrays.asList(types));
        Mockito.when(serverConfigurationService.getString(Mockito.eq("projectSiteTargetType"), Mockito.anyString()))
                .thenAnswer(invocation -> invocation.getArgument(1));
    }

    @Test
    public void testGetToolRegistrationsSimple() {
        Tool projectTool = Mockito.mock(Tool.class);
        Mockito.when(toolManager.findTools(Collections.singleton("project"), null, false)).thenReturn(Collections.singleton(projectTool));
        configureProjectSiteTypes(PROJECT_SITE_TYPES);

        SessionState state = Mockito.mock(SessionState.class);
        Set<Tool> project = siteAction.getToolRegistrations(state, "project", false);
        Assert.assertThat(project, CoreMatchers.hasItems(projectTool));
    }

    @Test
    public void testGetToolRegistrationNone() {
        configureProjectSiteTypes(PROJECT_SITE_TYPES);
        SessionState state = Mockito.mock(SessionState.class);
        Mockito.when(toolManager.findTools(ArgumentMatchers.anySet(), Mockito.isNull(), ArgumentMatchers.anyBoolean())).thenReturn(Collections.emptySet());

        Set<Tool> other = siteAction.getToolRegistrations(state, "other", false);
        Assert.assertTrue(other.isEmpty());
    }

    @Test
    public void testGetToolRegistrationDefault() {
        Tool projectTool = Mockito.mock(Tool.class);
        SessionState state = Mockito.mock(SessionState.class);
        Mockito.when(state.getAttribute(SiteAction.STATE_DEFAULT_SITE_TYPE)).thenReturn("project");

        Mockito.when(toolManager.findTools(Collections.singleton("new"), null, false)).thenReturn(Collections.emptySet());
        Mockito.when(toolManager.findTools(Collections.singleton("project"), null, false)).thenReturn(Collections.singleton(projectTool));
        configureProjectSiteTypes("project", "new");

        Set<Tool> tools = siteAction.getToolRegistrations(state, "new", false);
        Assert.assertThat(tools, CoreMatchers.hasItems(projectTool));
    }

    @Test
    public void testOriginalToolIds() {
        Tool singleTool = Mockito.mock(Tool.class);
        Tool multipleTool = Mockito.mock(Tool.class);
        Mockito.when(singleTool.getId()).thenReturn("sakai.single");
        Mockito.when(multipleTool.getId()).thenReturn("sakai.multiple");

        Set<Tool> availableTools = new HashSet<>();
        availableTools.add(singleTool);
        availableTools.add(multipleTool);
        Mockito.when(toolManager.findTools(ArgumentMatchers.anySet(), Mockito.isNull(), ArgumentMatchers.anyBoolean())).thenReturn(availableTools);

        Mockito.when(toolManager.getTool("sakai.single")).thenReturn(singleTool);
        Mockito.when(toolManager.getTool("sakai.multiple")).thenReturn(multipleTool);

        Properties singleToolProperties = new Properties();
        singleToolProperties.put("allowMultipleInstances", "false");
        Mockito.when(singleTool.getRegisteredConfig()).thenReturn(singleToolProperties);

        Properties multipleToolProperties = new Properties();
        multipleToolProperties.put("allowMultipleInstances", "true");
        Mockito.when(multipleTool.getRegisteredConfig()).thenReturn(multipleToolProperties);

        configureProjectSiteTypes(PROJECT_SITE_TYPES);

        SessionState state = Mockito.mock(SessionState.class);
        Mockito.when(state.getAttribute(SiteAction.STATE_SITE_TYPE)).thenReturn("project");

        Assert.assertTrue(siteAction.getOriginalToolIds(Collections.singletonList("not.present"), state).isEmpty());
        Assert.assertEquals(Collections.singletonList("sakai.single"), siteAction.getOriginalToolIds(Collections.singletonList("sakai.single"), state));
        Assert.assertEquals(Collections.singletonList("sakai.single"), siteAction.getOriginalToolIds(Arrays.asList("sakai.single", "not.present"), state));
        Assert.assertEquals(Collections.emptyList(), siteAction.getOriginalToolIds(Arrays.asList("sakai.single1", "not.present"), state));
        Assert.assertEquals(Collections.singletonList("sakai.multiple"), siteAction.getOriginalToolIds(Arrays.asList("sakai.multiple", "not.present"), state));
        Assert.assertEquals(Collections.singletonList("sakai.multiple"), siteAction.getOriginalToolIds(Arrays.asList("sakai.multiple1", "sakai.multiple2"), state));
        Assert.assertEquals(Collections.singletonList("sakai.multiple"), siteAction.getOriginalToolIds(Collections.singletonList("012345678901234567890123456789012345sakai.multiple"), state));
    }
}
