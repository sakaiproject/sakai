/*
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.importer.impl.handlers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.importer.impl.importables.FileResource;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Exercises the real import handler; only services outside the importer are replaced. */
public class ResourcesHandlerTest {
    private ResourcesHandler handler;
    private ContentHostingService content;
    private SecurityService permissions;

    @Before
    public void configureBoundaries() {
        content = mock(ContentHostingService.class);
        permissions = mock(SecurityService.class);
        SessionManager sessions = mock(SessionManager.class);
        when(sessions.getCurrentSessionUserId()).thenReturn("import-user");
        when(content.getSiteCollection("site")).thenReturn("/group/site/");
        when(content.newResourceProperties()).thenAnswer(invocation -> new BaseResourcePropertiesEdit());
        handler = new ResourcesHandler();
        handler.setContentHostingService(content);
        handler.setSecurityService(permissions);
        handler.setSessionManager(sessions);
    }

    @Test
    public void importsPortableNestedEntryNames() throws Exception {
        handler.handle(resource("nested\\file.txt"), "site");
        verify(content).addResource(eq("/group/site/bundle/nested/file.txt"), anyString(), any(InputStream.class), any(ResourceProperties.class), anyInt());
        verify(permissions).popAdvisor();
    }

    @Test
    public void rejectsInvalidNestedEntriesAndAlwaysReleasesAdvisor() throws Exception {
        for (String name : new String[] {"../outside.txt", "nested/../../outside.txt", "..\\outside.txt", "/outside.txt", "C:/outside.txt", "file:stream"}) {
            configureBoundaries();
            FileResource resource = resource(name);
            assertThrows(UncheckedIOException.class, () -> handler.handle(resource, "site"));
            verify(content, never()).addResource(anyString(), anyString(), any(InputStream.class), any(ResourceProperties.class), anyInt());
            verify(permissions).pushAdvisor(any(SecurityAdvisor.class));
            verify(permissions).popAdvisor();
        }
    }

    private FileResource resource(String name) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            zip.putNextEntry(new ZipEntry(name));
            zip.write("content".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        FileResource resource = new FileResource();
        resource.setFileName("bundle.zip");
        resource.setDestinationResourcePath("bundle.zip");
        resource.setInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        return resource;
    }
}
