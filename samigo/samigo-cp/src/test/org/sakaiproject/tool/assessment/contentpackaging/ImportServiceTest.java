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
package org.sakaiproject.tool.assessment.contentpackaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ImportServiceTestConfiguration.class)
public class ImportServiceTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();
    @Autowired private ImportService service;
    @Autowired private ServerConfigurationService configuration;
    private Path repository;

    @Before
    public void configureRepository() throws IOException {
        repository = temporary.newFolder("repository").toPath().toRealPath();
        when(configuration.getString(eq("samigo.answerUploadRepositoryPath"), anyString()))
            .thenReturn(repository.toString());
        when(configuration.getInt("samigo.qtiImport.maxEntries", 10000)).thenReturn(10000);
        when(configuration.getLong("samigo.qtiImport.maxExpandedBytes", 536870912L)).thenReturn(536870912L);
    }

    @Test
    public void extractsNestedQtiAndAttachments() throws IOException {
        Map<String, String> entries = new LinkedHashMap<>();
        entries.put("imsmanifest.xml", manifest("assessment/exportAssessment.xml"));
        entries.put("assessment/", "");
        entries.put("assessment/exportAssessment.xml", "<questestinterop/>");
        entries.put("assessment/images/example.txt", "attachment");
        Path root = Path.of(service.unzipImportFile(archive(entries).toString()));
        assertTrue(root.startsWith(repository));
        assertEquals(Path.of("assessment", "exportAssessment.xml"), Path.of(service.getQtiFilename()));
        assertEquals("attachment", Files.readString(root.resolve("assessment/images/example.txt")));
    }

    @Test
    public void extractsPackageWithWindowsSeparators() throws IOException {
        Map<String, String> entries = new LinkedHashMap<>();
        entries.put("imsmanifest.xml", manifest("assessment/exportAssessment.xml"));
        entries.put("assessment\\", "");
        entries.put("assessment\\exportAssessment.xml", "<questestinterop/>");
        Path root = Path.of(service.unzipImportFile(archive(entries).toString()));
        assertEquals("<questestinterop/>", Files.readString(root.resolve("assessment/exportAssessment.xml")));
        assertEquals(Path.of("assessment", "exportAssessment.xml"), Path.of(service.getQtiFilename()));
    }

    @Test
    public void manifestSelectionSurvivesEntryNormalizationAndArchiveOrdering() throws IOException {
        for (String href : new String[] {"./actual.xml", "pages/../actual.xml", ".\\actual.xml"}) {
            for (boolean manifestFirst : new boolean[] {true, false}) {
                Map<String, String> entries = new LinkedHashMap<>();
                if (manifestFirst) {
                    entries.put("imsmanifest.xml", manifest(href));
                }
                entries.put("exportAssessment.xml", "<wrong-assessment/>");
                if (!manifestFirst) {
                    entries.put("imsmanifest.xml", manifest(href));
                }
                entries.put("./actual.xml", "<selected-assessment/>");
                Path root = Path.of(service.unzipImportFile(archive(entries).toString()));
                assertEquals("actual.xml", service.getQtiFilename());
                assertEquals("<selected-assessment/>", Files.readString(root.resolve(service.getQtiFilename())));
            }
        }
    }

    @Test
    public void resolvesInheritedManifestBasesBeforePreservingSelection() throws IOException {
        String[] manifests = {
            "<manifest><resources><resource href=\"actual.xml\" xml:base=\"assessment/\"/></resources></manifest>",
            "<manifest xml:base=\"package/\"><resources xml:base=\"assessment/\">"
                + "<resource href=\"actual.xml\"/></resources></manifest>",
            "<manifest xml:base=\"package/\"><resources xml:base=\"assessment/\">"
                + "<resource xml:base=\"../selected/\" href=\"actual.xml\"/></resources></manifest>",
            "<manifest xml:base=\"package/assessment/\"><resources>"
                + "<resource xml:base=\"\" href=\"actual.xml\"/></resources></manifest>",
            "<cp:manifest xmlns:cp=\"http://www.imsglobal.org/xsd/imscp_v1p1\" xml:base=\"package/\">"
                + "<cp:resources><cp:resource xml:base=\"assessment/\" href=\"actual.xml\"/></cp:resources></cp:manifest>"
        };
        String[] targets = {"assessment/actual.xml", "package/assessment/actual.xml",
            "package/selected/actual.xml", "package/assessment/actual.xml", "package/assessment/actual.xml"};
        for (int i = 0; i < manifests.length; i++) {
            for (boolean manifestFirst : new boolean[] {true, false}) {
                Map<String, String> entries = new LinkedHashMap<>();
                if (manifestFirst) {
                    entries.put("imsmanifest.xml", manifests[i]);
                }
                entries.put("exportAssessment.xml", "<wrong-assessment/>");
                if (!manifestFirst) {
                    entries.put("imsmanifest.xml", manifests[i]);
                }
                entries.put(targets[i], "<selected-assessment/>");
                Path root = Path.of(service.unzipImportFile(archive(entries).toString()));
                assertEquals(Path.of(targets[i]), Path.of(service.getQtiFilename()));
                assertEquals("<selected-assessment/>", Files.readString(root.resolve(service.getQtiFilename())));
            }
        }
    }

    @Test
    public void selectsQtiResourceBeforeApplyingInheritedBase() throws IOException {
        Map<String, String> entries = new LinkedHashMap<>();
        entries.put("imsmanifest.xml", "<manifest xml:base=\"assessment/\"><resources>"
            + "<resource type=\"webcontent\" href=\"image.png\"/>"
            + "<resource type=\"imsqti_xmlv1p2\" href=\"actual.xml\"/></resources></manifest>");
        entries.put("assessment/image.png", "attachment");
        entries.put("exportAssessment.xml", "<wrong-assessment/>");
        entries.put("assessment/actual.xml", "<selected-assessment/>");
        Path root = Path.of(service.unzipImportFile(archive(entries).toString()));
        assertEquals(Path.of("assessment/actual.xml"), Path.of(service.getQtiFilename()));
        assertEquals("<selected-assessment/>", Files.readString(root.resolve(service.getQtiFilename())));
    }

    @Test
    public void decodesManifestReferenceBeforeCheckingContainment() throws IOException {
        for (String href : new String[] {"actual%20file.xml", "actual file.xml"}) {
            Map<String, String> entries = new LinkedHashMap<>();
            entries.put("imsmanifest.xml", manifest(href));
            entries.put("actual file.xml", "<selected-assessment/>");
            Path root = Path.of(service.unzipImportFile(archive(entries).toString()));
            assertEquals("actual file.xml", service.getQtiFilename());
            assertTrue(Files.isRegularFile(root.resolve(service.getQtiFilename())));
        }
        for (String href : new String[] {"", "%2e%2e/outside.xml", "../outside.xml",
                "file:/tmp/outside.xml", "/tmp/outside.xml", "https://example.invalid/actual.xml"}) {
            assertThrows(href, IOException.class, () -> service.unzipImportFile(
                archive(Map.of("imsmanifest.xml", manifest(href))).toString()));
        }
    }

    @Test
    public void rejectsEscapingAndNonLocalManifestBasesBeforeFallback() throws IOException {
        for (String base : new String[] {"../", "%2e%2e/", "file:/tmp/", "https://example.invalid/", "//example.invalid/"}) {
            Map<String, String> entries = new LinkedHashMap<>();
            entries.put("exportAssessment.xml", "<wrong-assessment/>");
            entries.put("imsmanifest.xml", "<manifest xml:base=\"" + base + "\"><resources>"
                + "<resource href=\"actual.xml\"/></resources></manifest>");
            entries.put("actual.xml", "<selected-assessment/>");
            assertThrows(base, IOException.class, () -> service.unzipImportFile(archive(entries).toString()));
            assertNoExtractedFiles();
        }
    }

    @Test
    public void rejectsTraversalAndRemovesPartialExtraction() throws IOException {
        Map<String, String> entries = new LinkedHashMap<>();
        entries.put("exportAssessment.xml", "<questestinterop/>");
        entries.put("../canary.txt", "canary");
        assertThrows(IOException.class, () -> service.unzipImportFile(archive(entries).toString()));
        assertNoExtractedFiles();
    }

    @Test
    public void rejectsTraversalBeyondRepositoryWithoutOverwriting() throws IOException {
        Path canary = temporary.getRoot().toPath().resolve("canary.txt");
        Files.writeString(canary, "original");
        // root is repository/jsf/upload_tmp/qti_imports/test-user/unzip_files/import-.../
        assertThrows(IOException.class, () -> service.unzipImportFile(
            archive(Map.of("../../../../../../../canary.txt", "replacement")).toString()));
        assertEquals("original", Files.readString(canary));
        assertNoExtractedFiles();
    }

    @Test
    public void rejectsAbsoluteAndWindowsTraversalPaths() throws IOException {
        for (String name : new String[] {"/tmp/canary.txt", "C:/canary.txt", "..\\canary.txt", "../import-sibling/canary.txt"}) {
            assertThrows(name, IOException.class, () -> service.unzipImportFile(archive(Map.of(name, "canary")).toString()));
        }
        assertNoExtractedFiles();
    }

    @Test
    public void rejectsEscapingManifestHrefBeforeXmlFallback() throws IOException {
        Map<String, String> entries = new LinkedHashMap<>();
        entries.put("imsmanifest.xml", manifest("../outside.xml"));
        entries.put("exportAssessment.xml", "<questestinterop/>");
        assertThrows(IOException.class, () -> service.unzipImportFile(archive(entries).toString()));
        assertNoExtractedFiles();
    }

    @Test
    public void externalManifestEntityCannotSelectAssessment() throws IOException {
        Path canary = temporary.newFile("manifest-resource.xml").toPath();
        Files.writeString(canary, "<resource href=\"canary-assessment.xml\"/>");
        Map<String, String> entries = new LinkedHashMap<>();
        entries.put("exportAssessment.xml", "<questestinterop/>");
        entries.put("imsmanifest.xml", "<!DOCTYPE manifest [<!ENTITY external SYSTEM \""
            + canary.toUri() + "\">]><manifest><resources>&external;</resources></manifest>");
        Path root = Path.of(service.unzipImportFile(archive(entries).toString()));
        assertEquals("exportAssessment.xml", service.getQtiFilename());
        assertEquals("<questestinterop/>", Files.readString(root.resolve(service.getQtiFilename())));
    }

    @Test
    public void extractionDirectoriesAreUniqueAndStateResetsBetweenImports() throws IOException {
        Path first = Path.of(service.unzipImportFile(archive(Map.of("first.xml", "<questestinterop/>")).toString()));
        Path second = Path.of(service.unzipImportFile(archive(Map.of("exportAssessment.xml", "<questestinterop/>")).toString()));
        assertFalse(first.equals(second));
        assertEquals("exportAssessment.xml", service.getQtiFilename());
    }

    @Test
    public void rejectsEntryCountLimitAndRemovesPartialExtraction() throws IOException {
        when(configuration.getInt("samigo.qtiImport.maxEntries", 10000)).thenReturn(2);
        Map<String, String> entries = new LinkedHashMap<>();
        entries.put("exportAssessment.xml", "<questestinterop/>");
        entries.put("images/", "");
        entries.put("images/attachment.txt", "attachment");
        IOException failure = assertThrows(IOException.class,
            () -> service.unzipImportFile(archive(entries).toString()));
        assertEquals("QTI content package exceeds the entry limit", failure.getMessage());
        assertNoExtractedFiles();
    }

    @Test
    public void rejectsCumulativeExpandedBytesAndRemovesPartialExtraction() throws IOException {
        when(configuration.getLong("samigo.qtiImport.maxExpandedBytes", 536870912L)).thenReturn(4096L);
        Map<String, String> entries = new LinkedHashMap<>();
        entries.put("exportAssessment.xml", "<questestinterop/>");
        entries.put("first.txt", "x".repeat(3000));
        entries.put("second.txt", "x".repeat(3000));
        IOException failure = assertThrows(IOException.class,
            () -> service.unzipImportFile(archive(entries).toString()));
        assertEquals("QTI content package exceeds the expanded byte limit", failure.getMessage());
        assertNoExtractedFiles();
    }

    @Test
    public void acceptsPackageAtConfiguredLimits() throws IOException {
        String qti = "<questestinterop/>";
        when(configuration.getInt("samigo.qtiImport.maxEntries", 10000)).thenReturn(1);
        when(configuration.getLong("samigo.qtiImport.maxExpandedBytes", 536870912L))
            .thenReturn((long) qti.getBytes(StandardCharsets.UTF_8).length);
        Path root = Path.of(service.unzipImportFile(archive(Map.of("exportAssessment.xml", qti)).toString()));
        assertEquals(qti, Files.readString(root.resolve("exportAssessment.xml")));
    }

    @Test
    public void directoryPayloadCannotBypassExpandedByteLimit() throws IOException {
        when(configuration.getLong("samigo.qtiImport.maxExpandedBytes", 536870912L)).thenReturn(1024L);
        IOException failure = assertThrows(IOException.class,
            () -> service.unzipImportFile(archive(Map.of("images/", "x".repeat(2048))).toString()));
        assertEquals("QTI content package exceeds the expanded byte limit", failure.getMessage());
        assertNoExtractedFiles();
    }

    private Path archive(Map<String, String> entries) throws IOException {
        Path zip = Files.createTempFile(temporary.getRoot().toPath(), "assessment_", ".zip");
        try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(zip))) {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                output.putNextEntry(new ZipEntry(entry.getKey()));
                output.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                output.closeEntry();
            }
        }
        return zip;
    }

    private String manifest(String href) {
        return "<manifest><resources><resource href=\"" + href + "\"/></resources></manifest>";
    }

    private void assertNoExtractedFiles() throws IOException {
        try (Stream<Path> files = Files.walk(repository)) {
            assertEquals(0, files.filter(Files::isRegularFile).count());
        }
        Path extractionParent = repository.resolve("jsf/upload_tmp/qti_imports/test-user/unzip_files");
        try (Stream<Path> directories = Files.list(extractionParent)) {
            assertEquals(0, directories.count());
        }
    }
}
