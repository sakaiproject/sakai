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
