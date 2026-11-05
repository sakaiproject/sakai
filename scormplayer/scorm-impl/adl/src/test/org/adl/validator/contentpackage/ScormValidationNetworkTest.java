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
package org.adl.validator.contentpackage;

import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.adl.parsers.dom.ADLDOMParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class ScormValidationNetworkTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();
    private final AtomicInteger requests = new AtomicInteger();
    private HttpServer server;
    private String endpoint;

    /** Starts a local canary server to detect unexpected validation network requests. */
    @Before
    public void startListener() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            requests.incrementAndGet();
            byte[] body = "<root/>".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(body);
            }
        });
        server.start();
        endpoint = "http://127.0.0.1:" + server.getAddress().getPort() + "/sco.xml";
    }

    /** Stops the local canary server after each test. */
    @After
    public void stopListener() {
        if (server != null) server.stop(0);
    }

    /** External SCO URLs remain valid without network probes while malformed URLs fail. */
    @Test
    public void validatesExternalScoWithoutContactingItsServer() throws Exception {
        assertTrue("External SCO should remain valid", validateResource(endpoint));
        assertEquals("Validation must not probe the external SCO", 0, requests.get());
        assertTrue("HTTPS SCO should remain valid", validateResource("https://example.invalid/course.html"));
        assertFalse("Malformed external SCO must fail", validateResource("http:/missing-host"));
        assertTrue("Local SCO should remain valid", validateResource("course.html"));
    }

    /** Remote file and JAR XML inputs are rejected before contacting their servers. */
    @Test
    public void xmlParserRejectsRemoteInputsWithoutContactingTheirServer() throws Exception {
        for (String reference : new String[] {endpoint, "jar:" + endpoint + "!/rules.xml",
                "file://127.0.0.1/remote.xml", "jar:file://127.0.0.1/remote.jar!/rules.xml"}) {
            ADLDOMParser parser = new ADLDOMParser();
            parser.parseForWellformedness(new URL(reference), false, false);
            assertFalse(reference, parser.getIsWellformed());
        }
        ADLDOMParser parser = new ADLDOMParser();
        parser.parseForWellformedness(endpoint, false, false);
        assertFalse(parser.getIsWellformed());
        assertEquals("Remote XML and JAR inputs must never be fetched", 0, requests.get());
    }

    /** Local files and resources in local JARs remain readable by the XML parser. */
    @Test
    public void xmlParserStillReadsLocalAndBundledResources() throws Exception {
        Path file = Files.writeString(temporary.newFile("rules.xml").toPath(), "<rules/>");
        ADLDOMParser local = new ADLDOMParser();
        local.parseForWellformedness(file.toUri().toURL(), false, false);
        assertTrue(local.getIsWellformed());

        Path jar = temporary.newFile("rules.jar").toPath();
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
            output.putNextEntry(new JarEntry("rules.xml"));
            output.write("<rules/>".getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }
        ADLDOMParser bundled = new ADLDOMParser();
        bundled.parseForWellformedness(new URL("jar:" + jar.toUri() + "!/rules.xml"), false, false);
        assertTrue(bundled.getIsWellformed());
        assertEquals(0, requests.get());
    }

    /** Builds and validates a local SCORM manifest referring to the supplied resource. */
    private boolean validateResource(String href) throws Exception {
        Path archive = temporary.newFolder().toPath();
        Path schema = Files.writeString(archive.resolve("manifest.xsd"), """
                <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
                    targetNamespace="http://www.imsglobal.org/xsd/imscp_v1p1" elementFormDefault="qualified">
                  <xs:element name="manifest"><xs:complexType><xs:sequence>
                    <xs:any minOccurs="0" maxOccurs="unbounded" processContents="lax"/>
                  </xs:sequence><xs:anyAttribute processContents="lax"/></xs:complexType></xs:element>
                </xs:schema>
                """);
        Files.writeString(archive.resolve("course.html"), "course content");
        String escapedHref = href.replace("&", "&amp;").replace("'", "&apos;");
        Path manifest = Files.writeString(archive.resolve("imsmanifest.xml"), """
                <manifest xmlns="http://www.imsglobal.org/xsd/imscp_v1p1"
                    xmlns:adlcp="http://www.adlnet.org/xsd/adlcp_v1p3" identifier="manifest">
                  <metadata><schema>ADL SCORM</schema><schemaversion>2004 3rd Edition</schemaversion></metadata>
                  <organizations/><resources>
                    <resource identifier="resource" type="webcontent" adlcp:scormType="asset" href='%s'>
                      <file href='%s'/>
                    </resource>
                  </resources>
                </manifest>
                """.formatted(escapedHref, escapedHref));
        CPValidator validator = new CPValidator(archive.toString());
        validator.setSchemaLocation("http://www.imsglobal.org/xsd/imscp_v1p1 " + schema.toUri());
        validator.setPerformValidationToSchema(true);
        return validator.validate(manifest.toString(), "media", "resource", false, "UTF-8");
    }
}
