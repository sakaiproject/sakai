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

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.util.ZipContentUtil;
import org.sakaiproject.tool.api.SessionManager;

import static org.sakaiproject.content.util.ZipContentUtil.*;


/**
 * Testing the zip utils
 * Unfortunately, there is not really a very good way to test this stuff right now without a real CHS so there are no real helpful tests :-(
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

    /* No real way to test this without a running CHS -AZ */
    @Ignore
    @Test
    public void testExtractArchive() {
        Assert.fail("Not yet implemented");
    }

    /* No real way to test this without a running CHS -AZ */
    @Ignore
    @Test
    public void testGetZipManifest() {
        Assert.fail("Not yet implemented");
    }

}
