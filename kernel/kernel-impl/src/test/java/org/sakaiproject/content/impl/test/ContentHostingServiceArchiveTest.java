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
package org.sakaiproject.content.impl.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.Xml;
import org.sakaiproject.util.MergeConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import static org.junit.Assert.*;

/** Exercises archive body references with production Kernel wiring and content storage. */
public class ContentHostingServiceArchiveTest extends SakaiKernelTestBase {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();
    private ContentHostingService content;
    private String siteId;

    /** Loads the production Kernel wiring with real content storage. */
    @BeforeClass
    public static void configureKernel() throws IOException {
        oneTimeSetup("content");
    }

    /** Creates an isolated destination collection under an administrator session. */
    @Before
    public void authenticateAdmin() throws Exception {
        content = getService(ContentHostingService.class);
        Session session = getService(SessionManager.class).getCurrentSession();
        session.setUserId("admin");
        session.setUserEid("admin");
        siteId = "archive-regression-" + UUID.randomUUID();
        content.commitCollection(content.addCollection(content.getSiteCollection(siteId)));
    }

    /** Valid body references resolve beside the archive XML on the native filesystem. */
    @Test
    public void importsBodyRelativeToArchiveXml() throws Exception {
        Path archive = temporary.newFolder().toPath();
        Files.createDirectory(archive.resolve("bodies"));
        Files.writeString(archive.resolve("bodies/file.txt"), "package content");

        String result = mergeBody(archive, "bodies/file.txt", "imported.txt");

        assertTrue(result, result.contains("imported."));
        assertEquals("package content", new String(content.getResource(resourceId("imported.txt")).getContent(), StandardCharsets.UTF_8));
    }

    /** Ordinary archives reject traversal, absolute paths, and escaping symlinks. */
    @Test
    public void rejectsBodyReferencesOutsideArchive() throws Exception {
        Path parent = temporary.newFolder().toPath();
        Path archive = Files.createDirectory(parent.resolve("archive"));
        Path outside = Files.writeString(parent.resolve("canary.txt"), "outside canary");
        Files.createSymbolicLink(archive.resolve("link.txt"), outside);
        int index = 0;
        for (String reference : new String[] {"../canary.txt", "..\\canary.txt", outside.toString(), "link.txt"}) {
            String name = "rejected-" + index++ + ".txt";

            String result = mergeBody(archive, reference, name);

            assertTrue(result, result.contains("InvalidEntryException"));
            assertThrows(IdUnusedException.class, () -> content.getResource(resourceId(name)));
        }
        assertEquals("outside canary", Files.readString(outside));
    }

    /** CC+ deduplication may reference a shared body above sakai_archive within the extraction root. */
    @Test
    public void importsSharedCartridgeBodyWithinTrustedRoot() throws Exception {
        Path cartridge = temporary.newFolder().toPath();
        Path archive = Files.createDirectory(cartridge.resolve("sakai_archive"));
        Files.createDirectory(cartridge.resolve("resources"));
        Files.writeString(cartridge.resolve("resources/shared.txt"), "shared cartridge content");
        String result = mergeBody(archive, "../resources/shared.txt", "shared.txt", cartridge);
        assertTrue(result, result.contains("imported."));
        assertEquals("shared cartridge content", new String(content.getResource(resourceId("shared.txt")).getContent(), StandardCharsets.UTF_8));
    }

    /** A trusted cartridge root must not permit traversal or symlinks outside the extracted package. */
    @Test
    public void rejectsCartridgeBodiesOutsideTrustedRoot() throws Exception {
        Path parent = temporary.newFolder().toPath();
        Path cartridge = Files.createDirectory(parent.resolve("cartridge"));
        Path archive = Files.createDirectory(cartridge.resolve("sakai_archive"));
        Path outside = Files.writeString(parent.resolve("canary.txt"), "outside canary");
        Files.createSymbolicLink(cartridge.resolve("link.txt"), outside);
        int index = 0;
        for (String reference : new String[] {"../../canary.txt", "..\\..\\canary.txt", "../link.txt"}) {
            String name = "outside-" + index++ + ".txt";
            String result = mergeBody(archive, reference, name, cartridge);
            assertTrue(result, result.contains("InvalidEntryException"));
            assertThrows(IdUnusedException.class, () -> content.getResource(resourceId(name)));
        }
        assertEquals("outside canary", Files.readString(outside));
    }

    /** Exercises the legacy ordinary-archive API without a cartridge containment root. */
    private String mergeBody(Path archive, String reference, String name) throws Exception {
        return mergeBody(archive, reference, name, null);
    }

    /** Builds a resource archive and invokes the production merge API with an optional trusted root. */
    private String mergeBody(Path archive, String reference, String name, Path archiveRoot) throws Exception {
        Document document = Xml.createSecureDocumentBuilderFactory().newDocumentBuilder().newDocument();
        Element root = document.createElement("archive");
        document.appendChild(root);
        Element service = document.createElement(ContentHostingService.class.getName());
        root.appendChild(service);
        Stack<Element> stack = new Stack<>();
        stack.push(service);
        ContentResourceEdit source = content.addResource(resourceId("source-" + name));
        Element resource;
        try {
            source.setContentType("text/plain");
            source.setContent("original".getBytes(StandardCharsets.UTF_8));
            resource = source.toXml(document, stack);
        } finally {
            content.cancelResource(source);
        }
        resource.removeAttribute("body");
        resource.setAttribute("rel-id", name);
        resource.setAttribute("body-location", reference);
        String result;
        if (archiveRoot == null) {
            result = content.merge(siteId, service, archive.resolve("content.xml").toString(), "merge",
                    new HashMap<>(), new HashMap<>(), Set.of());
        } else {
            MergeConfig config = new MergeConfig();
            config.archiveRoot = archiveRoot;
            result = content.merge(siteId, service, archive.resolve("content.xml").toString(), "merge", config);
        }
        // Read the persisted resource in a fresh request context.
        getService(ThreadLocalManager.class).clear();
        Session session = getService(SessionManager.class).getCurrentSession();
        session.setUserId("admin");
        session.setUserEid("admin");
        return result;
    }

    /** Builds a resource ID in this test destination site. */
    private String resourceId(String name) {
        return content.getSiteCollection(siteId) + name;
    }
}
