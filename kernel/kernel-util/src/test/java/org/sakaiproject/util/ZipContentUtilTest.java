/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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
 **********************************************************************************/

package org.sakaiproject.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.content.util.ZipContentUtil;
import org.sakaiproject.tool.api.SessionManager;

import static org.sakaiproject.content.util.ZipContentUtil.*;


/**
 * Testing the zip utils
 * Extraction tests exercise the real utility with ContentHostingService as the external boundary.
 * 
 * @author Aaron Zeckoski (azeckoski @ vt.edu)
 */
public class ZipContentUtilTest {

    @Test
    public void testGetMaxZipExtractSize() {
        SessionManager sm = Mockito.mock(SessionManager.class);
        ContentHostingService chs = Mockito.mock(ContentHostingService.class);
        ServerConfigurationService scs = Mockito.mock(ServerConfigurationService.class);
        ResourceLoader resourceLoader = Mockito.mock(ResourceLoader.class);

        Mockito.when(scs.getString(RESOURCECLASS, DEFAULT_RESOURCECLASS)).thenReturn(DEFAULT_RESOURCECLASS);
        Mockito.when(scs.getString(RESOURCEBUNDLE, DEFAULT_RESOURCEBUNDLE)).thenReturn(DEFAULT_RESOURCEBUNDLE);

        try (MockedStatic<Resource> resourceMock = Mockito.mockStatic(Resource.class)) {
            resourceMock.when(() -> Resource.getResourceLoader(DEFAULT_RESOURCECLASS, DEFAULT_RESOURCEBUNDLE)).thenReturn(resourceLoader);

            // test default
            Mockito.when(scs.getInt(ContentHostingService.RESOURCES_ZIP_EXPAND_MAX, MAX_ZIP_EXTRACT_FILES_DEFAULT)).thenReturn(1000);
            ZipContentUtil zipContentUtil = new ZipContentUtil(chs, scs, sm);
            Assert.assertEquals(MAX_ZIP_EXTRACT_FILES_DEFAULT, zipContentUtil.getMaxZipExtractFiles().intValue());

            // test a custom setting that is more than default
            Mockito.when(scs.getInt(ContentHostingService.RESOURCES_ZIP_EXPAND_MAX, MAX_ZIP_EXTRACT_FILES_DEFAULT)).thenReturn(10000);
            zipContentUtil = new ZipContentUtil(chs, scs, sm);
            Assert.assertEquals(10000, zipContentUtil.getMaxZipExtractFiles().intValue());

            // test negative, should revert to default
            Mockito.when(scs.getInt(ContentHostingService.RESOURCES_ZIP_EXPAND_MAX, MAX_ZIP_EXTRACT_FILES_DEFAULT)).thenReturn(-1);
            zipContentUtil = new ZipContentUtil(chs, scs, sm);
            Assert.assertEquals(MAX_ZIP_EXTRACT_FILES_DEFAULT, zipContentUtil.getMaxZipExtractFiles().intValue());
        }
    }

    /* No real way to test this without a running CHS -AZ */
    @Ignore
    @Test
    public void testCompressFolder() {
    }

    @Test
    public void rejectsUnsafeArchiveBeforeCreatingAnyContent() throws Exception {
        for (String name : new String[] {"../escape.txt", "nested/../../escape/", "..\\escape.txt",
                "/escape.txt", "C:/escape.txt", "\\\\server/share/escape.txt"}) {
            ContentHostingService chs = Mockito.mock(ContentHostingService.class);
            Map<String, String> entries = new LinkedHashMap<>();
            entries.put("good.txt", "must not be imported");
            entries.put(name, "unsafe");
            stubArchive(chs, entries);
            ZipContentUtil util = newZipUtil(chs);

            Assert.assertThrows(Zip.InvalidEntryException.class,
                () -> util.extractArchive("/group/site/package.zip"));

            Mockito.verify(chs, Mockito.never()).addCollection(Mockito.anyString());
            Mockito.verify(chs, Mockito.never()).addResource(Mockito.anyString());
            Mockito.verify(chs, Mockito.never()).editResource(Mockito.anyString());
        }
    }

    @Test
    public void importsSafeNestedResourceUsingNormalizedEntryName() throws Exception {
        ContentHostingService chs = Mockito.mock(ContentHostingService.class);
        stubArchive(chs, Map.of("./nested\\file.txt", "content"));
        ContentCollectionEdit collection = Mockito.mock(ContentCollectionEdit.class);
        ContentResourceEdit resource = Mockito.mock(ContentResourceEdit.class);
        Mockito.when(collection.getPropertiesEdit()).thenReturn(Mockito.mock(ResourcePropertiesEdit.class));
        Mockito.when(resource.getPropertiesEdit()).thenReturn(Mockito.mock(ResourcePropertiesEdit.class));
        Mockito.when(chs.addCollection("/group/site/package/")).thenReturn(collection);
        Mockito.when(chs.addResource("/group/site/package/nested/file.txt")).thenReturn(resource);

        newZipUtil(chs).extractArchive("/group/site/package.zip");

        Mockito.verify(chs).addResource("/group/site/package/nested/file.txt");
        Mockito.verify(chs).commitResource(resource, NotificationService.NOTI_NONE);
    }

    private ZipContentUtil newZipUtil(ContentHostingService chs) {
        ServerConfigurationService scs = Mockito.mock(ServerConfigurationService.class);
        Mockito.when(scs.getString(RESOURCECLASS, DEFAULT_RESOURCECLASS)).thenReturn(DEFAULT_RESOURCECLASS);
        Mockito.when(scs.getString(RESOURCEBUNDLE, DEFAULT_RESOURCEBUNDLE)).thenReturn(DEFAULT_RESOURCEBUNDLE);
        Mockito.when(scs.getInt(ContentHostingService.RESOURCES_ZIP_EXPAND_MAX, MAX_ZIP_EXTRACT_FILES_DEFAULT))
            .thenReturn(MAX_ZIP_EXTRACT_FILES_DEFAULT);
        ResourceLoader loader = Mockito.mock(ResourceLoader.class);
        try (MockedStatic<Resource> resourceMock = Mockito.mockStatic(Resource.class)) {
            resourceMock.when(() -> Resource.getResourceLoader(DEFAULT_RESOURCECLASS, DEFAULT_RESOURCEBUNDLE)).thenReturn(loader);
            return new ZipContentUtil(chs, scs, Mockito.mock(SessionManager.class));
        }
    }

    private void stubArchive(ContentHostingService chs, Map<String, String> entries) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                zip.putNextEntry(new ZipEntry(entry.getKey()));
                zip.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        ContentResource resource = Mockito.mock(ContentResource.class);
        ContentCollection parent = Mockito.mock(ContentCollection.class);
        Mockito.when(parent.getId()).thenReturn("/group/site/");
        Mockito.when(resource.getId()).thenReturn("/group/site/package.zip");
        Mockito.when(resource.getContainingCollection()).thenReturn(parent);
        Mockito.when(resource.streamContent()).thenAnswer(invocation -> new ByteArrayInputStream(bytes.toByteArray()));
        Mockito.when(chs.getResource("/group/site/package.zip")).thenReturn(resource);
    }

    /* No real way to test this without a running CHS -AZ */
    @Ignore
    @Test
    public void testGetZipManifest() {
        Assert.fail("Not yet implemented");
    }

}
