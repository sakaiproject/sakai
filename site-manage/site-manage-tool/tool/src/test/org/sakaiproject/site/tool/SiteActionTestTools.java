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

import org.hamcrest.collection.IsCollectionContaining;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.cover.ToolManager;

import java.util.*;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.sakaiproject.site.tool.SiteAction.STATE_DEFAULT_SITE_TYPE;
import static org.sakaiproject.site.tool.SiteAction.STATE_SITE_TYPE;

/**
 * Tests for SiteAction
 */
@SuppressWarnings("deprecation")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ComponentManager.class})
public class SiteActionTestTools {

    private SiteAction siteAction;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(ComponentManager.class);
        // A mock component manager.
        when(ComponentManager.get(any(Class.class))).then(new Answer<Object>() {
            private Map<Class, Object> mocks = new HashMap<>();
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Class classToMock = (Class) invocation.getArguments()[0];
                return mocks.computeIfAbsent(classToMock, k -> mock(classToMock));
            }
        });
        // Mock the Session so that ResourceLoader doesn't NPE on init
        when(ComponentManager.get(SessionManager.class).getCurrentSession()).thenReturn(mock(Session.class));
        siteAction = new SiteAction();
    }

    @Test
    public void testGetToolRegistrationsSimple() {
        // Normal project site type
        Tool projectTool = mock(Tool.class);
        when(ToolManager.findTools(singleton("project"), null)).thenReturn(singleton(projectTool));
        when(SiteService.getSiteTypeStrings("project")).thenReturn(singletonList("project"));
        when(ServerConfigurationService.getString("projectSiteTargetType", "project")).thenReturn("project");

        SessionState state = mock(SessionState.class);
        Set<Tool> project = siteAction.getToolRegistrations(state, "project");
        assertThat(project, IsCollectionContaining.hasItems(projectTool));
    }

    @Test
    public void testGetToolRegistrationNone() {
        // Site type that doesn't exist
        SessionState state = mock(SessionState.class);
        Set<Tool> other = siteAction.getToolRegistrations(state, "other");
        assertTrue(other.isEmpty());
    }

    @Test
    public void testGetToolRegistrationDefault() {
        // Check that we fallback to the default site type
        Tool projectTool = mock(Tool.class);
        when(ToolManager.findTools(singleton("project"), null)).thenReturn(singleton(projectTool));
        when(SiteService.getSiteTypeStrings("new")).thenReturn(Arrays.asList("project", "new"));
        when(ServerConfigurationService.getString("projectSiteTargetType", "project")).thenReturn("project");

        SessionState state = mock(SessionState.class);
        when(state.getAttribute(STATE_DEFAULT_SITE_TYPE)).thenReturn("project");
        Set<Tool> tools = siteAction.getToolRegistrations(state, "new");
        assertThat(tools, IsCollectionContaining.hasItems(projectTool));
    }

    @Test
    public void testOriginalToolIds() {
        // Normal project site type
        Tool singleTool = mock(Tool.class);
        Tool multipleTool = mock(Tool.class);
        when(singleTool.getId()).thenReturn("sakai.single");
        when(multipleTool.getId()).thenReturn("sakai.multiple");
        when(ToolManager.findTools(singleton("project"), null)).thenReturn(new HashSet<>(Arrays.asList(singleTool, multipleTool)));
        when(ToolManager.getTool("sakai.single")).thenReturn(singleTool);
        when(ToolManager.getTool("sakai.multiple")).thenReturn(multipleTool);
        Properties singleToolProperties = new Properties();
        singleToolProperties.put("allowMultipleInstances", "false");
        when(singleTool.getRegisteredConfig()).thenReturn(singleToolProperties);
        Properties multipleToolProperties = new Properties();
        multipleToolProperties.put("allowMultipleInstances", "true");
        when(multipleTool.getRegisteredConfig()).thenReturn(multipleToolProperties);

        when(SiteService.getSiteTypeStrings("project")).thenReturn(singletonList("project"));
        when(ServerConfigurationService.getString("projectSiteTargetType", "project")).thenReturn("project");

        SessionState state = mock(SessionState.class);
        when(state.getAttribute(STATE_SITE_TYPE)).thenReturn("project");

        List<String> filteredToolIds;
        filteredToolIds = siteAction.originalToolIds(singletonList("not.present"), state);
        assertTrue(filteredToolIds.isEmpty());

        filteredToolIds = siteAction.originalToolIds(singletonList("sakai.single"), state);
        assertEquals(Arrays.asList("sakai.single"), filteredToolIds);

        filteredToolIds = siteAction.originalToolIds(Arrays.asList("sakai.single", "not.present"), state);
        assertEquals(Arrays.asList("sakai.single"), filteredToolIds);

        filteredToolIds = siteAction.originalToolIds(Arrays.asList("sakai.single1", "not.present"), state);
        assertEquals(Arrays.asList(), filteredToolIds);

        filteredToolIds = siteAction.originalToolIds(Arrays.asList("sakai.multiple", "not.present"), state);
        assertEquals(Arrays.asList("sakai.multiple"), filteredToolIds);

        filteredToolIds = siteAction.originalToolIds(Arrays.asList("sakai.multiple1", "sakai.multiple2"), state);
        assertEquals(Arrays.asList("sakai.multiple"), filteredToolIds);

        // Has to be 36 characters long.
        filteredToolIds = siteAction.originalToolIds(Arrays.asList("012345678901234567890123456789012345sakai.multiple"), state);
        assertEquals(Arrays.asList("sakai.multiple"), filteredToolIds);

    }



}
