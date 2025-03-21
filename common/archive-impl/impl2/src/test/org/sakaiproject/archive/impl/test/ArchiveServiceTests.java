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
package org.sakaiproject.archive.impl.test;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;

import org.sakaiproject.archive.api.ArchiveService;
import org.sakaiproject.archive.impl.ArchiveService2Impl;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.Xml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.AopTestUtils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.UUID;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ArchiveServiceTestConfiguration.class})
public class ArchiveServiceTests extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired private ArchiveService archiveService;
    @Autowired private AuthzGroupService authzGroupService;
    @Autowired private EntityManager entityManager;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Autowired private SiteService siteService;
    @Autowired private TimeService timeService;
    @Autowired private UserDirectoryService userDirectoryService;

    private String siteId = "xyz";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void archive() throws IOException {

        EntityProducer rubricsProducer = mock(EntityProducer.class);
        when(rubricsProducer.getLabel()).thenReturn("rubrics");
        when(rubricsProducer.willArchiveMerge()).thenReturn(true);
        when(rubricsProducer.archive(any(), any(), any(), any(), any())).thenAnswer(invocation -> {

            Document doc = (Document) invocation.getArgument(1);
            Stack<Element> stack = (Stack<Element>) invocation.getArgument(2);
            ((Element) stack.peek()).appendChild(doc.createElement("rubrics"));
            return "";
        });

        EntityProducer conversationsProducer = mock(EntityProducer.class);
        when(conversationsProducer.getLabel()).thenReturn("conversations");
        when(conversationsProducer.willArchiveMerge()).thenReturn(true);
        when(conversationsProducer.archive(any(), any(), any(), any(), any())).thenAnswer(invocation -> {

            Document doc = (Document) invocation.getArgument(1);
            Stack<Element> stack = (Stack<Element>) invocation.getArgument(2);
            ((Element) stack.peek()).appendChild(doc.createElement("conversations"));
            return "";
        });

        when(entityManager.getEntityProducers()).thenReturn(List.of(rubricsProducer, conversationsProducer));

        File archiveDir = folder.newFolder("archive");

        ((ArchiveService2Impl) AopTestUtils.getTargetObject(archiveService)).setStoragePath(archiveDir.getCanonicalPath() + File.separator);

        Time time = mock(Time.class);
        when(time.toString()).thenReturn("deprecated time :(");
        when(timeService.newTime()).thenReturn(time);

        try {
            when(authzGroupService.getAuthzGroup(any())).thenReturn(mock(AuthzGroup.class));
        } catch (Exception e) {
        }

        String user1Id = UUID.randomUUID().toString();
        String user1Eid = "user1";

        User user1 = mock(User.class);
        when(user1.getId()).thenReturn(user1Id);
        when(user1.getEid()).thenReturn(user1Eid);
        when(user1.toXml(any(), any())).thenAnswer(invocation -> {

            Document doc = (Document) invocation.getArgument(0);
            Stack stack = (Stack) invocation.getArgument(1);

            Element userEl = doc.createElement("user");
            ((Element) stack.peek()).appendChild(userEl);
            return userEl;
        });

        when(userDirectoryService.getUsers(any())).thenReturn(List.of(user1));

        Site site = mock(Site.class);
        when(site.getId()).thenReturn(siteId);
        when(site.toXml(any(), any())).thenAnswer(invocation -> {

            Document doc = (Document) invocation.getArgument(0);
            Stack stack = (Stack) invocation.getArgument(1);

            Element siteEl = doc.createElement("site");
            ((Element) stack.peek()).appendChild(siteEl);
            return siteEl;
        });

        try {
            when(siteService.getSite(siteId)).thenReturn(site);
        } catch (Exception e) {
        }

        when(serverConfigurationService.getString("archive.toolproperties.excludefilter","password|secret")).thenReturn("password|secret");

        archiveService.archive(siteId);

        File[] files = archiveDir.listFiles();
        assertEquals(1, files.length);

        File siteArchiveDir = files[0];

        assertEquals(siteId + "-archive", siteArchiveDir.getName());

        assertEquals(5, siteArchiveDir.list().length);

        List<String> fileNames = Arrays.asList(siteArchiveDir.list());

        assertTrue(fileNames.contains("site.xml"));
        Document doc = Xml.readDocument(siteArchiveDir + File.separator + "site.xml");
        assertNotNull(doc);
        Element rootElement = doc.getDocumentElement();
        assertEquals("archive", rootElement.getTagName());
        NodeList siteNodes = rootElement.getElementsByTagName("site");
        assertEquals(1, siteNodes.getLength());

        assertTrue(fileNames.contains("user.xml"));
        doc = Xml.readDocument(siteArchiveDir + File.separator + "user.xml");
        assertNotNull(doc);
        rootElement = doc.getDocumentElement();
        assertEquals("archive", rootElement.getTagName());
        NodeList usersNodes = rootElement.getElementsByTagName(UserDirectoryService.APPLICATION_ID);
        assertEquals(1, usersNodes.getLength());
        NodeList userNodes = ((Element) usersNodes.item(0)).getElementsByTagName("user");
        assertEquals(1, userNodes.getLength());

        assertTrue(fileNames.contains("archive.xml"));
        doc = Xml.readDocument(siteArchiveDir + File.separator + "archive.xml");
        assertNotNull(doc);
        rootElement = doc.getDocumentElement();
        assertEquals("archive", rootElement.getTagName());
        NodeList logNodes = rootElement.getElementsByTagName("log");
        assertEquals(1, logNodes.getLength());

        assertTrue(fileNames.contains(rubricsProducer.getLabel() + ".xml"));
        doc = Xml.readDocument(siteArchiveDir + File.separator + "rubrics.xml");
        assertNotNull(doc);
        rootElement = doc.getDocumentElement();
        assertEquals("archive", rootElement.getTagName());
        NodeList rubricsNodes = rootElement.getElementsByTagName("rubrics");
        assertEquals(1, rubricsNodes.getLength());

        assertTrue(fileNames.contains(conversationsProducer.getLabel() + ".xml"));
        doc = Xml.readDocument(siteArchiveDir + File.separator + "conversations.xml");
        assertNotNull(doc);
        rootElement = doc.getDocumentElement();
        assertEquals("archive", rootElement.getTagName());
        NodeList conversationsNodes = rootElement.getElementsByTagName("conversations");
        assertEquals(1, conversationsNodes.getLength());
    }

    @Test
    public void merge() throws IOException, URISyntaxException {

        String siteId = "xyz";

        File archiveDir = folder.newFolder("archive");

        // Set the sakai home path to be the topmost component of the archive dir path. So,
        // definitely above the archive directory
        //String sakaiHomePath = File.separator + archiveDir.toPath().subpath(0,1).toString();
        when(serverConfigurationService.getSakaiHomePath()).thenReturn(folder.getRoot().getCanonicalPath());

        File siteArchiveDir = new File(archiveDir, siteId + "-archive");
        siteArchiveDir.mkdir();

        ((ArchiveService2Impl) AopTestUtils.getTargetObject(archiveService)).setStoragePath(archiveDir.getCanonicalPath() + File.separator);

        String archiveBasePath = siteArchiveDir.getCanonicalPath();

        Path siteXmlPath = Paths.get(Objects.requireNonNull(ArchiveServiceTests.class.getResource("/archive/site.xml")).toURI());
        assertNotNull(siteXmlPath);
        Path archiveSiteXmlPath = Paths.get(archiveBasePath, File.separator, "site.xml");
        Files.copy(siteXmlPath, archiveSiteXmlPath, StandardCopyOption.REPLACE_EXISTING);

        Path userXmlPath = Paths.get(Objects.requireNonNull(ArchiveServiceTests.class.getResource("/archive/user.xml")).toURI());
        assertNotNull(userXmlPath);
        Path archiveUserXmlPath = Paths.get(archiveBasePath, File.separator, "user.xml");
        Files.copy(userXmlPath, archiveUserXmlPath, StandardCopyOption.REPLACE_EXISTING);

        Path rubricsXmlPath = Paths.get(Objects.requireNonNull(ArchiveServiceTests.class.getResource("/archive/rubrics.xml")).toURI());
        assertNotNull(rubricsXmlPath);
        Path archiveRubricsXmlPath = Paths.get(archiveBasePath, File.separator, "rubrics.xml");
        Files.copy(rubricsXmlPath, archiveRubricsXmlPath, StandardCopyOption.REPLACE_EXISTING);

        Path conversationsXmlPath = Paths.get(Objects.requireNonNull(ArchiveServiceTests.class.getResource("/archive/conversations.xml")).toURI());
        assertNotNull(conversationsXmlPath);
        Path archiveConversationsXmlPath = Paths.get(archiveBasePath, File.separator, "conversations.xml");
        Files.copy(conversationsXmlPath, archiveConversationsXmlPath, StandardCopyOption.REPLACE_EXISTING);

        String toSiteId = "abc";

        Site site = mock(Site.class);
        try {
            when(siteService.getSite(toSiteId)).thenReturn(site);
        } catch (Exception e) {
        }

        EntityProducer userProducer = mock(EntityProducer.class);
        when(userProducer.getLabel()).thenReturn(UserDirectoryService.APPLICATION_ID);
        when(userProducer.willArchiveMerge()).thenReturn(true);

        EntityProducer rubricsProducer = mock(EntityProducer.class);
        when(rubricsProducer.getLabel()).thenReturn("rubrics");
        when(rubricsProducer.willArchiveMerge()).thenReturn(true);

        EntityProducer conversationsProducer = mock(EntityProducer.class);
        when(conversationsProducer.getLabel()).thenReturn("conversations");
        when(conversationsProducer.willArchiveMerge()).thenReturn(true);

        when(entityManager.getEntityProducers()).thenReturn(List.of(userProducer, rubricsProducer, conversationsProducer));

        archiveService.merge(siteId + "-archive", toSiteId, "admin");

        verify(userProducer).merge(any(), any(), any(), any(), any());
        verify(rubricsProducer).merge(any(), any(), any(), any(), any());
        verify(conversationsProducer).merge(any(), any(), any(), any(), any());
    }
}
