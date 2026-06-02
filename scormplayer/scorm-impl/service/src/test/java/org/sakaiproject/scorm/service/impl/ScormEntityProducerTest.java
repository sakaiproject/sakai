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
package org.sakaiproject.scorm.service.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.adl.validator.contentpackage.LaunchData;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.scorm.api.ScormConstants;
import org.sakaiproject.scorm.dao.api.ContentPackageDao;
import org.sakaiproject.scorm.dao.api.ContentPackageManifestDao;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.util.MergeConfig;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ScormEntityProducerTest
{
    private static final String SITE_ID = "site-a";
    private static final String FROM_SITE_ID = "site-source";
    private static final String UUID_1 = "uuid-1";
    private static final String COLLECTION_ID = "/private/scorm/" + UUID_1 + "/";
    private static final String ZIP_NAME = "scorm_" + UUID_1 + ".zip";

    private Path tempRoot;
    private String archivePath;

    private ScormContentService scormContentService;
    private ScormResourceService scormResourceService;
    private ContentHostingService contentHostingService;
    private ContentPackageDao contentPackageDao;
    private ContentPackageManifestDao contentPackageManifestDao;
    private SecurityService securityService;
    private ServerConfigurationService serverConfigurationService;
    private GradingService gradingService;

    private ScormEntityProducer producer;

    @Before
    public void setUp() throws Exception
    {
        tempRoot = Files.createTempDirectory("scorm-archive-tests-" + UUID.randomUUID());
        archivePath = tempRoot.toString() + File.separator;

        scormContentService = mock(ScormContentService.class);
        scormResourceService = mock(ScormResourceService.class);
        contentHostingService = mock(ContentHostingService.class);
        contentPackageDao = mock(ContentPackageDao.class);
        contentPackageManifestDao = mock(ContentPackageManifestDao.class);
        securityService = mock(SecurityService.class);
        serverConfigurationService = mock(ServerConfigurationService.class);
        gradingService = mock(GradingService.class);

        producer = new ScormEntityProducer();
        producer.setScormContentService(scormContentService);
        producer.setScormResourceService(scormResourceService);
        producer.setContentHostingService(contentHostingService);
        producer.setContentPackageDao(contentPackageDao);
        producer.setContentPackageManifestDao(contentPackageManifestDao);
        producer.setSecurityService(securityService);
        producer.setServerConfigurationService(serverConfigurationService);
        producer.setGradingService(gradingService);
    }

    @After
    public void tearDown() throws Exception
    {
        if (tempRoot != null && Files.exists(tempRoot))
        {
            Files.walk(tempRoot)
                .sorted((a, b) -> b.getNameCount() - a.getNameCount())
                .forEach(path -> path.toFile().delete());
        }
    }

    @Test
    public void willArchiveMergeIsEnabled()
    {
        assertTrue(producer.willArchiveMerge());
    }

    @Test
    public void archiveWritesPackageZipAndMetadata() throws Exception
    {
        ContentPackage pkg = new ContentPackage("My Package", UUID_1);
        pkg.setNumberOfTries(3);
        pkg.setShowTOC(true);
        pkg.setShowNavBar(true);
        pkg.setReleaseOn(new Date(1000L));
        pkg.setDueOn(new Date(2000L));
        when(scormContentService.getContentPackages(SITE_ID)).thenReturn(List.of(pkg));

        ContentResource manifest = mock(ContentResource.class);
        when(manifest.getId()).thenReturn(COLLECTION_ID + "imsmanifest.xml");
        when(manifest.isCollection()).thenReturn(false);
        when(manifest.streamContent()).thenReturn(new ByteArrayInputStream("<manifest/>".getBytes(StandardCharsets.UTF_8)));

        ContentCollection collection = mock(ContentCollection.class);
        when(collection.getMemberResources()).thenReturn(List.<ContentEntity>of(manifest));
        when(contentHostingService.getCollection(COLLECTION_ID)).thenReturn(collection);

        Document doc = newDocument();
        Element root = doc.createElement("archive");
        doc.appendChild(root);
        Stack<Element> stack = new Stack<>();
        stack.push(root);

        producer.archive(SITE_ID, doc, stack, archivePath, null);

        // The zip was written next to the archive XML and contains the manifest at the zip root
        File zip = new File(archivePath + ZIP_NAME);
        assertTrue("expected SCORM zip to be written", zip.isFile());
        assertEquals("imsmanifest.xml", firstZipEntryName(zip));

        // Metadata element captured the package settings. The element tag must be the producer
        // label ("scorm") so SiteMerger dispatches/filters the merge correctly.
        Element scormRoot = (Element) root.getElementsByTagName("scorm").item(0);
        assertNotNull(scormRoot);
        Element cp = (Element) scormRoot.getElementsByTagName("contentpackage").item(0);
        assertNotNull(cp);
        assertEquals("My Package", cp.getAttribute("title"));
        assertEquals(UUID_1, cp.getAttribute("resourceId"));
        assertEquals(ZIP_NAME, cp.getAttribute("archive"));
        assertEquals("3", cp.getAttribute("numberOfTries"));
        assertEquals("true", cp.getAttribute("showToc"));
        assertEquals("1000", cp.getAttribute("releaseOn"));
        assertEquals("2000", cp.getAttribute("dueOn"));
    }

    @Test
    public void mergeRecreatesPackageAndRestoresSettings() throws Exception
    {
        // A real zip waiting in the archive folder, as archive() would have produced
        writeZip(new File(archivePath + ZIP_NAME), "imsmanifest.xml", "<manifest/>");

        Document doc = newDocument();
        Element scormRoot = doc.createElement("scorm");
        doc.appendChild(scormRoot);
        Element cp = doc.createElement("contentpackage");
        cp.setAttribute("title", "My Package");
        cp.setAttribute("resourceId", UUID_1);
        cp.setAttribute("archive", ZIP_NAME);
        cp.setAttribute("numberOfTries", "5");
        cp.setAttribute("showToc", "true");
        cp.setAttribute("showNavBar", "true");
        cp.setAttribute("releaseOn", "1000");
        cp.setAttribute("dueOn", "2000");
        scormRoot.appendChild(cp);

        // No existing packages in the destination, so nothing is skipped as a duplicate
        when(scormContentService.getContentPackages(SITE_ID)).thenReturn(List.of());
        when(serverConfigurationService.getString("scorm.zip.encoding", "UTF-8")).thenReturn("UTF-8");
        when(scormResourceService.putArchive(any(), any(), eq("application/zip"), anyBoolean(), anyInt())).thenReturn("res-1");
        when(scormContentService.storeAndValidate(eq("res-1"), anyBoolean(), eq("UTF-8"))).thenReturn(0);

        // Snapshot before import is empty; after storeAndValidate the new package appears.
        // storeAndValidate derives the title from the manifest, so start from a different title
        // to prove merge() restores the archived one.
        ContentPackage created = new ContentPackage("Manifest Title", UUID_1);
        created.setContentPackageId(42L);
        when(contentPackageDao.find(SITE_ID)).thenReturn(List.of()).thenReturn(List.of(created));

        // SiteMerger passes the path to the producer's archive file (scorm.xml), not the directory;
        // merge() must resolve the zip against the file's parent directory.
        String scormXmlPath = archivePath + "scorm.xml";
        producer.merge(SITE_ID, scormRoot, scormXmlPath, FROM_SITE_ID, new MergeConfig());

        verify(scormResourceService).putArchive(any(), any(), eq("application/zip"), anyBoolean(), anyInt());
        verify(scormContentService).storeAndValidate(eq("res-1"), anyBoolean(), eq("UTF-8"));
        verify(contentPackageDao).save(created);

        // Settings from the archive XML were applied to the recreated package
        assertEquals("My Package", created.getTitle());
        assertEquals(5, created.getNumberOfTries());
        assertTrue(created.isShowTOC());
        assertTrue(created.isShowNavBar());
        assertEquals(new Date(1000L), created.getReleaseOn());
        assertEquals(new Date(2000L), created.getDueOn());
    }

    @Test
    public void mergeSkipsPackageWhenTitleAlreadyExists() throws Exception
    {
        writeZip(new File(archivePath + ZIP_NAME), "imsmanifest.xml", "<manifest/>");

        Document doc = newDocument();
        Element scormRoot = doc.createElement("scorm");
        doc.appendChild(scormRoot);
        Element cp = doc.createElement("contentpackage");
        cp.setAttribute("title", "My Package");
        cp.setAttribute("archive", ZIP_NAME);
        scormRoot.appendChild(cp);

        ContentPackage existing = new ContentPackage("My Package", UUID_1);
        when(scormContentService.getContentPackages(SITE_ID)).thenReturn(List.of(existing));

        producer.merge(SITE_ID, scormRoot, archivePath, FROM_SITE_ID, new MergeConfig());

        // Nothing imported because the title collides
        verify(scormResourceService, org.mockito.Mockito.never())
                .putArchive(any(), any(), any(), anyBoolean(), anyInt());
    }

    @Test
    public void archivePersistsScoGradebookBindings() throws Exception
    {
        ContentPackage pkg = new ContentPackage("My Package", UUID_1);
        pkg.setContentPackageId(100L);
        pkg.setManifestId(7L);
        when(scormContentService.getContentPackages(SITE_ID)).thenReturn(List.of(pkg));

        // Minimal content collection so writePackageArchive() succeeds and the element is emitted
        ContentResource manifestFile = mock(ContentResource.class);
        when(manifestFile.getId()).thenReturn(COLLECTION_ID + "imsmanifest.xml");
        when(manifestFile.isCollection()).thenReturn(false);
        when(manifestFile.streamContent()).thenReturn(new ByteArrayInputStream("<manifest/>".getBytes(StandardCharsets.UTF_8)));
        ContentCollection collection = mock(ContentCollection.class);
        when(collection.getMemberResources()).thenReturn(List.<ContentEntity>of(manifestFile));
        when(contentHostingService.getCollection(COLLECTION_ID)).thenReturn(collection);

        // One SCO in the manifest with a gradebook external assessment defined
        LaunchData sco = mock(LaunchData.class);
        when(sco.getSCORMType()).thenReturn("sco");
        when(sco.getItemIdentifier()).thenReturn("ITEM-1");
        ContentPackageManifest manifest = mock(ContentPackageManifest.class);
        when(manifest.getLaunchData()).thenReturn(List.of(sco));
        when(contentPackageManifestDao.load(7L)).thenReturn(manifest);

        when(gradingService.isExternalAssignmentDefined(SITE_ID, "100:ITEM-1")).thenReturn(true);
        Assignment assignment = mock(Assignment.class);
        when(assignment.getName()).thenReturn("Quiz");
        when(assignment.getPoints()).thenReturn(10.0);
        when(assignment.getCategoryId()).thenReturn(5L);
        when(gradingService.getExternalAssignment(SITE_ID, "100:ITEM-1")).thenReturn(assignment);

        Document doc = newDocument();
        Element root = doc.createElement("archive");
        doc.appendChild(root);
        Stack<Element> stack = new Stack<>();
        stack.push(root);

        producer.archive(SITE_ID, doc, stack, archivePath, null);

        Element cp = (Element) root.getElementsByTagName("contentpackage").item(0);
        Element gb = (Element) cp.getElementsByTagName("gradebookitem").item(0);
        assertNotNull("expected a gradebookitem element", gb);
        assertEquals("ITEM-1", gb.getAttribute("itemIdentifier"));
        assertEquals("Quiz", gb.getAttribute("name"));
        assertEquals("10.0", gb.getAttribute("points"));
        assertEquals("5", gb.getAttribute("categoryId"));
    }

    @Test
    public void mergeRecreatesScoGradebookBindings() throws Exception
    {
        writeZip(new File(archivePath + ZIP_NAME), "imsmanifest.xml", "<manifest/>");

        Document doc = newDocument();
        Element scormRoot = doc.createElement("scorm");
        doc.appendChild(scormRoot);
        Element cp = doc.createElement("contentpackage");
        cp.setAttribute("title", "My Package");
        cp.setAttribute("archive", ZIP_NAME);
        cp.setAttribute("dueOn", "2000");
        Element gb = doc.createElement("gradebookitem");
        gb.setAttribute("itemIdentifier", "ITEM-1");
        gb.setAttribute("name", "Quiz");
        gb.setAttribute("points", "10.0");
        gb.setAttribute("categoryId", "5");
        cp.appendChild(gb);
        scormRoot.appendChild(cp);

        when(scormContentService.getContentPackages(SITE_ID)).thenReturn(List.of());
        when(serverConfigurationService.getString("scorm.zip.encoding", "UTF-8")).thenReturn("UTF-8");
        when(scormResourceService.putArchive(any(), any(), eq("application/zip"), anyBoolean(), anyInt())).thenReturn("res-1");
        when(scormContentService.storeAndValidate(eq("res-1"), anyBoolean(), eq("UTF-8"))).thenReturn(0);

        ContentPackage created = new ContentPackage("My Package", UUID_1);
        created.setContentPackageId(42L);
        when(contentPackageDao.find(SITE_ID)).thenReturn(List.of()).thenReturn(List.of(created));
        // No existing assessment under the new contentPackageId yet
        when(gradingService.isExternalAssignmentDefined(SITE_ID, "42:ITEM-1")).thenReturn(false);

        producer.merge(SITE_ID, scormRoot, archivePath + "scorm.xml", FROM_SITE_ID, new MergeConfig());

        // External assessment recreated against the NEW contentPackageId, with the archived settings
        verify(gradingService).addExternalAssessment(eq(SITE_ID), eq(SITE_ID), eq("42:ITEM-1"),
                isNull(), eq("Quiz"), eq(10.0), eq(new Date(2000L)), eq(ScormConstants.SCORM_DFLT_TOOL_NAME),
                isNull(), eq(false), eq(5L), isNull());
    }

    private static Document newDocument() throws Exception
    {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    }

    private static String firstZipEntryName(File zip) throws Exception
    {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zip)))
        {
            ZipEntry entry = zis.getNextEntry();
            return entry == null ? null : entry.getName();
        }
    }

    private static void writeZip(File file, String entryName, String content) throws Exception
    {
        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(file)))
        {
            zos.putNextEntry(new ZipEntry(entryName));
            zos.write(content.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
    }
}
