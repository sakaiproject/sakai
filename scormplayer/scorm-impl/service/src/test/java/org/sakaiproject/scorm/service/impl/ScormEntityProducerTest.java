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
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentCaptor;

import org.adl.validator.contentpackage.LaunchData;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ScormTestConfiguration.class)
public class ScormEntityProducerTest
{
    private static final String SITE_ID = "site-a";
    private static final String FROM_SITE_ID = "site-source";
    private static final String UUID_1 = "uuid-1";
    private static final String COLLECTION_ID = "/private/scorm/" + UUID_1 + "/";
    private static final String ZIP_NAME = "scorm_" + UUID_1 + ".zip";

    private Path tempRoot;
    private String archivePath;

    @Autowired private ScormEntityProducer producer;
    @Autowired private ScormContentService scormContentService;
    @Autowired private ScormResourceService scormResourceService;
    @Autowired private ContentHostingService contentHostingService;
    @Autowired private ContentPackageDao contentPackageDao;
    @Autowired private ContentPackageManifestDao contentPackageManifestDao;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Autowired private GradingService gradingService;

    @Before
    public void setUp() throws Exception
    {
        tempRoot = Files.createTempDirectory("scorm-archive-tests-" + UUID.randomUUID());
        archivePath = tempRoot.toString() + File.separator;

        // Context (and its mock beans) is shared across test methods, so reset to isolate each test
        reset(scormContentService, scormResourceService, contentHostingService, contentPackageDao,
                contentPackageManifestDao, serverConfigurationService, gradingService);
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
        stubContentCollection(COLLECTION_ID, "imsmanifest.xml");

        Element root = archive();

        // The zip was written next to the archive XML and contains the manifest at the zip root
        File zip = new File(archivePath + ZIP_NAME);
        assertTrue("expected SCORM zip to be written", zip.isFile());
        assertEquals("imsmanifest.xml", firstZipEntryName(zip));

        // Metadata element captured the package settings. The element tag must be the producer
        // label ("scorm") so SiteMerger dispatches/filters the merge correctly.
        Element scormRoot = firstChild(root, "scorm");
        assertNotNull(scormRoot);
        Element cp = firstChild(scormRoot, "contentpackage");
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
        writeZip(new File(archivePath + ZIP_NAME), "imsmanifest.xml", "<manifest/>");

        Document doc = newDocument();
        Element cp = packageElement(doc, "My Package", ZIP_NAME);
        cp.setAttribute("resourceId", UUID_1);
        cp.setAttribute("numberOfTries", "5");
        cp.setAttribute("showToc", "true");
        cp.setAttribute("showNavBar", "true");
        cp.setAttribute("releaseOn", "1000");
        cp.setAttribute("dueOn", "2000");
        Element scormRoot = scormRootWith(doc, cp);

        when(scormContentService.getContentPackages(SITE_ID)).thenReturn(List.of()); // no duplicate titles
        // storeAndValidate derives the title from the manifest, so start from a different title
        // to prove merge() restores the archived one.
        ContentPackage created = new ContentPackage("Manifest Title", UUID_1);
        created.setContentPackageId(42L);
        stubSuccessfulImport(created);

        merge(scormRoot);

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
        Element scormRoot = scormRootWith(doc, packageElement(doc, "My Package", ZIP_NAME));

        when(scormContentService.getContentPackages(SITE_ID)).thenReturn(List.of(new ContentPackage("My Package", UUID_1)));

        merge(scormRoot);

        // Nothing imported because the title collides
        verify(scormResourceService, never()).putArchive(any(), any(), any(), anyBoolean(), anyInt());
    }

    @Test
    public void archivePersistsScoGradebookBindings() throws Exception
    {
        ContentPackage pkg = new ContentPackage("My Package", UUID_1);
        pkg.setContentPackageId(100L);
        pkg.setManifestId(7L);
        when(scormContentService.getContentPackages(SITE_ID)).thenReturn(List.of(pkg));
        stubContentCollection(COLLECTION_ID, "imsmanifest.xml");
        stubManifestWithSco(7L, "ITEM-1");
        stubGradebookAssignment("100:ITEM-1", "Quiz", 10.0, 5L);

        Element root = archive();

        Element cp = firstChild(firstChild(root, "scorm"), "contentpackage");
        Element gb = firstChild(cp, "gradebookitem");
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
        Element cp = packageElement(doc, "My Package", ZIP_NAME);
        cp.setAttribute("dueOn", "2000");
        cp.appendChild(gradebookItemElement(doc, "ITEM-1", "Quiz", "10.0", "5"));
        Element scormRoot = scormRootWith(doc, cp);

        when(scormContentService.getContentPackages(SITE_ID)).thenReturn(List.of());
        ContentPackage created = new ContentPackage("My Package", UUID_1);
        created.setContentPackageId(42L);
        stubSuccessfulImport(created);
        when(gradingService.isExternalAssignmentDefined(SITE_ID, "42:ITEM-1")).thenReturn(false);

        merge(scormRoot);

        // External assessment recreated against the NEW contentPackageId, carrying the archived settings
        ArgumentCaptor<String> externalId = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Double> points = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Long> categoryId = ArgumentCaptor.forClass(Long.class);
        verify(gradingService).addExternalAssessment(eq(SITE_ID), eq(SITE_ID), externalId.capture(),
                isNull(), eq("Quiz"), points.capture(), eq(new Date(2000L)), eq(ScormConstants.SCORM_DFLT_TOOL_NAME),
                isNull(), eq(false), categoryId.capture(), isNull());
        assertEquals("42:ITEM-1", externalId.getValue());
        assertEquals(Double.valueOf(10.0), points.getValue());
        assertEquals(Long.valueOf(5L), categoryId.getValue());
    }

    // --- helpers -------------------------------------------------------------

    /** Run archive() against a fresh {@code <archive>} root and return it. */
    private Element archive() throws Exception
    {
        Document doc = newDocument();
        Element root = doc.createElement("archive");
        doc.appendChild(root);
        Stack<Element> stack = new Stack<>();
        stack.push(root);
        producer.archive(SITE_ID, doc, stack, archivePath, null);
        return root;
    }

    /** Run merge() with the standard SiteMerger contract: archivePath points at the scorm.xml file. */
    private void merge(Element scormRoot)
    {
        producer.merge(SITE_ID, scormRoot, archivePath + "scorm.xml", FROM_SITE_ID, new MergeConfig());
    }

    private void stubContentCollection(String collectionId, String... fileNames) throws Exception
    {
        List<ContentEntity> members = new ArrayList<>();
        for (String name : fileNames)
        {
            ContentResource resource = mock(ContentResource.class);
            when(resource.getId()).thenReturn(collectionId + name);
            when(resource.isCollection()).thenReturn(false);
            when(resource.streamContent()).thenReturn(new ByteArrayInputStream(("<" + name + "/>").getBytes(StandardCharsets.UTF_8)));
            members.add(resource);
        }
        ContentCollection collection = mock(ContentCollection.class);
        when(collection.getMemberResources()).thenReturn(members);
        when(contentHostingService.getCollection(collectionId)).thenReturn(collection);
    }

    private void stubManifestWithSco(Serializable manifestId, String itemIdentifier)
    {
        LaunchData sco = mock(LaunchData.class);
        when(sco.getSCORMType()).thenReturn("sco");
        when(sco.getItemIdentifier()).thenReturn(itemIdentifier);
        ContentPackageManifest manifest = mock(ContentPackageManifest.class);
        when(manifest.getLaunchData()).thenReturn(List.of(sco));
        when(contentPackageManifestDao.load(manifestId)).thenReturn(manifest);
    }

    private void stubGradebookAssignment(String externalId, String name, Double points, Long categoryId)
    {
        when(gradingService.isExternalAssignmentDefined(SITE_ID, externalId)).thenReturn(true);
        Assignment assignment = mock(Assignment.class);
        when(assignment.getName()).thenReturn(name);
        when(assignment.getPoints()).thenReturn(points);
        when(assignment.getCategoryId()).thenReturn(categoryId);
        when(gradingService.getExternalAssignment(SITE_ID, externalId)).thenReturn(assignment);
    }

    /** Stub the interactive-upload import flow (putArchive -> storeAndValidate) and the before/after find snapshot. */
    private void stubSuccessfulImport(ContentPackage created) throws Exception
    {
        when(serverConfigurationService.getString("scorm.zip.encoding", "UTF-8")).thenReturn("UTF-8");
        when(scormResourceService.putArchive(any(), any(), eq("application/zip"), anyBoolean(), anyInt())).thenReturn("res-1");
        when(scormContentService.storeAndValidate(eq("res-1"), anyBoolean(), eq("UTF-8"))).thenReturn(0);
        when(contentPackageDao.find(SITE_ID)).thenReturn(List.of()).thenReturn(List.of(created));
    }

    private Element scormRootWith(Document doc, Element... contentPackages)
    {
        Element scorm = doc.createElement("scorm");
        doc.appendChild(scorm);
        for (Element cp : contentPackages)
        {
            scorm.appendChild(cp);
        }
        return scorm;
    }

    private Element packageElement(Document doc, String title, String archiveName)
    {
        Element cp = doc.createElement("contentpackage");
        cp.setAttribute("title", title);
        cp.setAttribute("archive", archiveName);
        return cp;
    }

    private Element gradebookItemElement(Document doc, String itemIdentifier, String name, String points, String categoryId)
    {
        Element gb = doc.createElement("gradebookitem");
        gb.setAttribute("itemIdentifier", itemIdentifier);
        gb.setAttribute("name", name);
        gb.setAttribute("points", points);
        gb.setAttribute("categoryId", categoryId);
        return gb;
    }

    private static Element firstChild(Element parent, String tag)
    {
        return (Element) parent.getElementsByTagName(tag).item(0);
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
