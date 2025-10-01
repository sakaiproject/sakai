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

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.hamcrest.collection.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.util.SiteTypeUtil;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;

/**
 * Tests for SiteAction
 */
@SuppressWarnings("deprecation")
@RunWith(MockitoJUnitRunner.class)
public class SiteActionTestTools {

    private static final String[] PROJECT_SITE_TYPES = {"project"};

    private SiteAction siteAction;

    @Mock
    private ToolManager toolManager;

    @Mock
    private SiteService siteService;

    @Mock
    private ServerConfigurationService serverConfigurationService;

    private Object originalSiteService;
    private Object originalServerConfigurationService;

    @Before
    public void setUp() throws Exception {
        siteAction = Mockito.mock(SiteAction.class, Mockito.withSettings().defaultAnswer(Answers.CALLS_REAL_METHODS));
        setField(siteAction, "toolManager", toolManager);
        setField(siteAction, "serverConfigurationService", serverConfigurationService);

        originalSiteService = getStaticField(SiteTypeUtil.class, "siteService");
        originalServerConfigurationService = getStaticField(SiteTypeUtil.class, "serverConfigurationService");

        setStaticField(SiteTypeUtil.class, "siteService", siteService);
        setStaticField(SiteTypeUtil.class, "serverConfigurationService", serverConfigurationService);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = SiteAction.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Object getStaticField(Class<?> type, String fieldName) throws Exception {
        Field field = type.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(null);
    }

    private void setStaticField(Class<?> type, String fieldName, Object value) throws Exception {
        Field field = type.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }

    @After
    public void tearDown() throws Exception {
        setStaticField(SiteTypeUtil.class, "siteService", originalSiteService);
        setStaticField(SiteTypeUtil.class, "serverConfigurationService", originalServerConfigurationService);
    }

    private void configureProjectSiteTypes(String... types) {
        when(siteService.getSiteTypeStrings("project")).thenReturn(Arrays.asList(types));
        when(serverConfigurationService.getString(Mockito.eq("projectSiteTargetType"), Mockito.anyString()))
            .thenAnswer(invocation -> invocation.getArgument(1));
    }

    @Test
    public void testGetToolRegistrationsSimple() {
        Tool projectTool = mock(Tool.class);
        when(toolManager.findTools(singleton("project"), null, false)).thenReturn(singleton(projectTool));
        configureProjectSiteTypes(PROJECT_SITE_TYPES);

        SessionState state = mock(SessionState.class);
        Set<Tool> project = siteAction.getToolRegistrations(state, "project", false);
        assertThat(project, hasItems(projectTool));
    }

    @Test
    public void testGetToolRegistrationNone() {
        configureProjectSiteTypes(PROJECT_SITE_TYPES);
        SessionState state = mock(SessionState.class);
        when(toolManager.findTools(anySet(), Mockito.isNull(), anyBoolean())).thenReturn(Collections.emptySet());

        Set<Tool> other = siteAction.getToolRegistrations(state, "other", false);
        assertTrue(other.isEmpty());
    }

    @Test
    public void testGetToolRegistrationDefault() {
        Tool projectTool = mock(Tool.class);
        SessionState state = mock(SessionState.class);
        when(state.getAttribute(SiteAction.STATE_DEFAULT_SITE_TYPE)).thenReturn("project");

        when(toolManager.findTools(singleton("new"), null, false)).thenReturn(Collections.emptySet());
        when(toolManager.findTools(singleton("project"), null, false)).thenReturn(singleton(projectTool));
        configureProjectSiteTypes("project", "new");

        Set<Tool> tools = siteAction.getToolRegistrations(state, "new", false);
        assertThat(tools, hasItems(projectTool));
    }

    @Test
    public void testOriginalToolIds() {
        Tool singleTool = mock(Tool.class);
        Tool multipleTool = mock(Tool.class);
        when(singleTool.getId()).thenReturn("sakai.single");
        when(multipleTool.getId()).thenReturn("sakai.multiple");

        Set<Tool> availableTools = new HashSet<>();
        availableTools.add(singleTool);
        availableTools.add(multipleTool);
        when(toolManager.findTools(anySet(), Mockito.isNull(), anyBoolean())).thenReturn(availableTools);

        when(toolManager.getTool("sakai.single")).thenReturn(singleTool);
        when(toolManager.getTool("sakai.multiple")).thenReturn(multipleTool);

        Properties singleToolProperties = new Properties();
        singleToolProperties.put("allowMultipleInstances", "false");
        when(singleTool.getRegisteredConfig()).thenReturn(singleToolProperties);

        Properties multipleToolProperties = new Properties();
        multipleToolProperties.put("allowMultipleInstances", "true");
        when(multipleTool.getRegisteredConfig()).thenReturn(multipleToolProperties);
        when(serverConfigurationService.getBoolean("gradebookng.multipleGroupInstances", false)).thenReturn(false);

        configureProjectSiteTypes(PROJECT_SITE_TYPES);

        SessionState state = mock(SessionState.class);
        when(state.getAttribute(SiteAction.STATE_SITE_TYPE)).thenReturn("project");

        assertTrue(siteAction.getOriginalToolIds(singletonList("not.present"), state).isEmpty());
        assertEquals(singletonList("sakai.single"), siteAction.getOriginalToolIds(singletonList("sakai.single"), state));
        assertEquals(singletonList("sakai.single"), siteAction.getOriginalToolIds(Arrays.asList("sakai.single", "not.present"), state));
        assertEquals(Collections.emptyList(), siteAction.getOriginalToolIds(Arrays.asList("sakai.single1", "not.present"), state));
        assertEquals(singletonList("sakai.multiple"), siteAction.getOriginalToolIds(Arrays.asList("sakai.multiple", "not.present"), state));
        assertEquals(singletonList("sakai.multiple"), siteAction.getOriginalToolIds(Arrays.asList("sakai.multiple1", "sakai.multiple2"), state));
        assertEquals(singletonList("sakai.multiple"), siteAction.getOriginalToolIds(singletonList("012345678901234567890123456789012345sakai.multiple"), state));
    }
}
