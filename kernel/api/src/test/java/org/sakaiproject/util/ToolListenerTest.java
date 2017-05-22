/* *********************************************************************************
 * $URL$
 * $Id$
 * *********************************************************************************
 *
 * Copyright (c) 2016 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ********************************************************************************/
package org.sakaiproject.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.tool.api.ActiveToolManager;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

/**
 * Tests the basic functions of the tool listener
 */
@RunWith(MockitoJUnitRunner.class)
public class ToolListenerTest {

    private ToolListener listener;

    @Mock
    private ServletContextEvent event;

    @Mock
    private ServletContext context;

    @Mock
    private ServerConfigurationService serverConfigurationService;

    @Mock
    private ActiveToolManager activeToolManager;

    private Path sakaiHome;
    private Path toolsFolder;

    @Before
    public void setUp() throws IOException {
        listener = new ToolListener(activeToolManager, serverConfigurationService);
        when(event.getServletContext()).thenReturn(context);
        // Create the tools folder inside our pretend sakai-home
        sakaiHome = Files.createTempDirectory("ToolListenerTest");
        toolsFolder = Files.createDirectories(sakaiHome.resolve("tools"));
        when(serverConfigurationService.getSakaiHomePath()).thenReturn(sakaiHome.toString());
        doAnswer(invocation -> "/webapp"+ invocation.getArgumentAt(0, String.class))
                .when(context).getRealPath(anyString());
    }

    // Testing that it loads inside the /tools folder
    @Test
    public void testToolsRegistration() {
        when(context.getResourcePaths("/tools/")).thenReturn(Stream.of("/tools/", "/tools/sakai-tool.xml").collect(Collectors.toSet()));
        InputStream inputStream = mock(InputStream.class);
        when(context.getResourceAsStream("/tools/sakai-tool.xml")).thenReturn(inputStream);
        listener.contextInitialized(event);
        verify(activeToolManager).register(inputStream, context);
    }

    // Testing that it loads inside the /WEB-INF/tools folder
    @Test
    public void testWebInfRegistration() {
        when(context.getResourcePaths("/WEB-INF/tools/")).thenReturn(Stream.of("/WEB-INF/tools/", "/WEB-INF/tools/sakai-tool.xml").collect(Collectors.toSet()));
        InputStream inputStream = mock(InputStream.class);
        when(context.getResourceAsStream("/WEB-INF/tools/sakai-tool.xml")).thenReturn(inputStream);
        listener.contextInitialized(event);
        verify(activeToolManager).register(inputStream, context);
    }

    // Testing is doesn't fall over when no registrations found
    @Test
    public void testNoRegistration() {
        when(context.getResourcePaths("/tools/")).thenReturn(Stream.of("/tools/" ).collect(Collectors.toSet()));
        when(context.getResourcePaths("/WEB-INF/tools/")).thenReturn(Stream.of("/WEB-INF/tools/" ).collect(Collectors.toSet()));
        InputStream inputStream = mock(InputStream.class);
        listener.contextInitialized(event);
        verify(activeToolManager, never()).register(inputStream, context);
    }

    // Check multiple registrations work across multiple locations.
    @Test
    public void testMultipleRegistrations() {
        when(context.getResourcePaths("/tools/")).thenReturn(Stream.of("/tools/", "/tools/sakai-tool.xml").collect(Collectors.toSet()));
        when(context.getResourcePaths("/WEB-INF/tools/")).thenReturn(Stream.of("/WEB-INF/tools/", "/WEB-INF/tools/sakai-tool.xml", "/WEB-INF/tools/another-tool.xml").collect(Collectors.toSet()));
        InputStream inputStream = mock(InputStream.class);
        when(context.getResourceAsStream(anyString())).thenReturn(inputStream);
        listener.contextInitialized(event);
        verify(activeToolManager, times(3)).register(inputStream, context);
    }

    // Check the folder and bad extension are ignored.
    @Test
    public void testIgnoreFiles() {
        when(context.getResourcePaths("/WEB-INF/tools/")).thenReturn(Stream.of("/WEB-INF/tools/", "/WEB-INF/tools/sakai-tool.ignored", "/WEB-INF/tools/folder/").collect(Collectors.toSet()));
        InputStream inputStream = mock(InputStream.class);
        when(context.getResourceAsStream(anyString())).thenReturn(inputStream);
        listener.contextInitialized(event);
        verify(activeToolManager, never()).register(inputStream, context);
    }

    // Check that a custom location is set it doesn't use the standard ones.
    @Test
    public void testCustomLocation() {
        when(context.getInitParameter(ToolListener.PATH)).thenReturn("/custom");
        when(context.getResourcePaths("/custom/")).thenReturn(Stream.of("/custom/", "/custom/sakai-tool.xml").collect(Collectors.toSet()));
        InputStream inputStream = mock(InputStream.class);
        when(context.getResourceAsStream("/custom/sakai-tool.xml")).thenReturn(inputStream);
        listener.contextInitialized(event);
        verify(activeToolManager).register(inputStream, context);
        verify(context, never()).getResourcePaths("/tools/");
        verify(context, never()).getResourcePaths("/WEB-INF/tools/");
    }

    // Check that a tool override is loaded from sakai.home ok.
    @Test
    public void testToolOverride() throws IOException {
        Path sakaiTool = Files.createFile(toolsFolder.resolve("sakai-tool.xml"));
        Path otherTool = Files.createFile(toolsFolder.resolve("other-tool.xml"));
        when(context.getResourcePaths("/tools/")).thenReturn(Stream.of("/tools/", "/tools/sakai-tool.xml").collect(Collectors.toSet()));
        when(context.getResourcePaths("/WEB-INF/tools/")).thenReturn(Stream.of("/WEB-INF/tools/", "/WEB-INF/tools/other-tool.xml").collect(Collectors.toSet()));
        listener.contextInitialized(event);
        verify(activeToolManager, never()).register(any(InputStream.class), eq(context));
        verify(activeToolManager).register(sakaiTool.toFile(), context);
        verify(activeToolManager).register(otherTool.toFile(), context);
    }

    // Check that locale files are loaded out of the old tools folder.
    @Test
    public void testMessageBundle() {
        when(context.getResourcePaths("/tools/")).thenReturn(Stream.of("/tools/", "/tools/sakai-tool.xml", "/tools/sakai-tool.properties", "/tools/sakai-tool_fr.properties").collect(Collectors.toSet()));
        listener.contextInitialized(event);
        verify(activeToolManager).setResourceBundle("sakai-tool", "/webapp/tools/sakai-tool.properties");
        verify(activeToolManager).setResourceBundle("sakai-tool", "/webapp/tools/sakai-tool_fr.properties");
    }

    // Check that locale files are loaded out of the new tools folder.
    @Test
    public void testMessageBundleWebInf() {
        when(context.getResourcePaths("/WEB-INF/tools/")).thenReturn(Stream.of("/WEB-INF/tools/", "/WEB-INF/tools/sakai-tool.xml", "/WEB-INF/tools/sakai-tool.properties", "/WEB-INF/tools/sakai-tool_fr.properties").collect(Collectors.toSet()));
        listener.contextInitialized(event);
        verify(activeToolManager).setResourceBundle("sakai-tool", "/webapp/WEB-INF/tools/sakai-tool.properties");
        verify(activeToolManager).setResourceBundle("sakai-tool", "/webapp/WEB-INF/tools/sakai-tool_fr.properties");
    }

}
