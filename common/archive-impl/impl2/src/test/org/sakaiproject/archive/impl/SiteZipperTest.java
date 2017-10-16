/**
 * Copyright (c) 2006-2017 The Apereo Foundation
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
package org.sakaiproject.archive.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SiteZipperTest {

    private SiteZipper siteZipper;

    @Before
    public void setUp() {
        siteZipper = new SiteZipper();
    }

    @Test
    public void testUnzip() throws IOException, URISyntaxException {
        String unzipFolder = Files.createTempDirectory("testUnzip").toString();
        URL resource = getClass().getResource("/!admin-20170303050657.zip");

        String zip = Paths.get(resource.toURI()).toFile().getAbsolutePath();
        String folder = siteZipper.unzipArchive(zip, unzipFolder);

        Assert.assertEquals("!admin-archive/", folder);
    }

    @Test
    public void testUnzipNoFolder() throws IOException, URISyntaxException {
        String unzipFolder = Files.createTempDirectory("testUnzip").toString();
        URL resource = getClass().getResource("/!admin-no-folder.zip");

        String zip = Paths.get(resource.toURI()).toFile().getAbsolutePath();
        String folder = siteZipper.unzipArchive(zip, unzipFolder);

        Assert.assertEquals(null, folder);
    }
}
