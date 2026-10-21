/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.contentpackaging;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.util.Xml;
import org.sakaiproject.util.Zip;

/** Operation-scoped QTI content-package extraction. */
@Slf4j
@RequiredArgsConstructor
public class ImportService {
    private final ServerConfigurationService serverConfigurationService;
    private final String agentId;
    @Getter @Setter
    private String qtiFilename;

    public String unzipImportFile(String filename) throws IOException {
        int maxEntries = serverConfigurationService.getInt("samigo.qtiImport.maxEntries", 10000);
        long maxExpandedBytes = serverConfigurationService.getLong("samigo.qtiImport.maxExpandedBytes", 536870912L);
        if (maxEntries <= 0 || maxExpandedBytes <= 0) {
            throw new IOException("QTI import limits must be positive");
        }
        String repositoryPath = serverConfigurationService.getString("samigo.answerUploadRepositoryPath",
            "${sakai.home}/samigo/answerUploadRepositoryPath/");
        Path parent = Path.of(repositoryPath, "jsf", "upload_tmp", "qti_imports", agentId, "unzip_files");
        Files.createDirectories(parent);
        Path root = Files.createTempDirectory(parent, "import-").toRealPath();
        qtiFilename = "exportAssessment.xml";

        try (ZipInputStream zipStream = new ZipInputStream(new FileInputStream(filename))) {
            String tmpName = new File(filename).getName();
            List<String> xmlFilenames = new ArrayList<>();
            boolean manifestQtiSelected = false;
            long entryCount = 0;
            long expandedBytes = 0;
            byte[] buffer = new byte[8192];
            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                if (++entryCount > maxEntries) {
                    throw new IOException("QTI content package exceeds the entry limit");
                }
                String entryName = Zip.validateEntryName(entry.getName());
                Path destination = Zip.resolveEntryPath(root, entryName);
                boolean directory = entryName.endsWith("/");
                if (directory) {
                    Files.createDirectories(destination);
                } else {
                    Files.createDirectories(destination.getParent());
                }
                try (OutputStream output = directory ? OutputStream.nullOutputStream() : Files.newOutputStream(destination)) {
                    int bytesRead;
                    while ((bytesRead = zipStream.read(buffer)) != -1) {
                        if (bytesRead > maxExpandedBytes - expandedBytes) {
                            throw new IOException("QTI content package exceeds the expanded byte limit");
                        }
                        output.write(buffer, 0, bytesRead);
                        expandedBytes += bytesRead;
                    }
                }

                if ("imsmanifest.xml".equals(entryName)) {
                    String manifestHref = null;
                    String manifestBaseUri = null;
                    try {
                        DocumentBuilder db = Xml.createSecureDocumentBuilderFactory().newDocumentBuilder();
                        Document doc = db.parse(destination.toFile());
                        Node fstNode = selectManifestResource(doc);
                        NamedNodeMap namedNodeMap = fstNode.getAttributes();
                        manifestHref = namedNodeMap.getNamedItem("href").getNodeValue();
                        manifestBaseUri = fstNode.getBaseURI();
                    } catch (Exception e) {
                        log.warn("Could not parse imsmanifest.xml: {}", e.toString());
                    }
                    if (manifestHref != null) {
                        Path manifestQtiPath = resolveManifestPath(root, manifestBaseUri, manifestHref);
                        qtiFilename = root.relativize(manifestQtiPath).toString().replace('\\', '/');
                        manifestQtiSelected = true;
                    }
                } else if (entryName.trim().endsWith(".xml")) {
                    String entryNameTrimmed = entryName.trim();
                    xmlFilenames.add(entryNameTrimmed);
                    if (!manifestQtiSelected && !xmlFilenames.contains(qtiFilename.trim())) {
                        if (xmlFilenames.contains("exportAssessment.xml")) {
                            qtiFilename = "exportAssessment.xml";
                        } else if (tmpName.contains("_")
                                && xmlFilenames.contains(tmpName.substring(0, tmpName.lastIndexOf("_")) + ".xml")) {
                            qtiFilename = tmpName.substring(0, tmpName.lastIndexOf("_")) + ".xml";
                        } else {
                            qtiFilename = entryNameTrimmed;
                        }
                    }
                }
                // Reject a manifest href immediately, before a later XML entry can replace it.
                Zip.resolveContainedPath(root, qtiFilename);
                zipStream.closeEntry();
            }
            qtiFilename = root.relativize(Zip.resolveContainedPath(root, qtiFilename)).toString();
        } catch (IOException | RuntimeException e) {
            try (Stream<Path> paths = Files.walk(root)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(path);
                }
            } catch (IOException cleanupFailure) {
                e.addSuppressed(cleanupFailure);
            }
            throw e;
        }
        return root.toString();
    }

    private Node selectManifestResource(Document document) {
        NodeList resources = document.getElementsByTagNameNS("*", "resource");
        for (int i = 0; i < resources.getLength(); i++) {
            Node resource = resources.item(i);
            Node type = resource.getAttributes().getNamedItem("type");
            if (type != null && type.getNodeValue().startsWith("imsqti")) {
                return resource;
            }
        }
        // Retain the established first-resource behavior for manifests without a QTI type.
        return resources.item(0);
    }

    private Path resolveManifestPath(Path root, String baseUri, String href) throws IOException {
        if (baseUri == null) {
            throw new IOException("Invalid base URI in QTI manifest");
        }
        if (href.isBlank()) {
            throw new IOException("Empty reference in QTI manifest");
        }
        try {
            // DOM resolves inherited xml:base values against the manifest's document URI.
            URI base = new URI(baseUri);
            URI reference = new URI(href.replace('\\', '/').replace(" ", "%20"));
            if (reference.isAbsolute() || reference.getRawAuthority() != null || reference.getPath().startsWith("/")) {
                throw new IOException("Absolute reference in QTI manifest");
            }
            URI resolved = base.resolve(reference);
            if (!"file".equalsIgnoreCase(resolved.getScheme()) || resolved.getRawAuthority() != null
                    || resolved.getRawQuery() != null || resolved.getRawFragment() != null) {
                throw new IOException("Non-local reference in QTI manifest");
            }
            // Path.of decodes URI escapes before the shared canonical containment check.
            Path target = Path.of(resolved);
            return Zip.resolveContainedPath(root, root.relativize(target).toString());
        } catch (URISyntaxException | IllegalArgumentException e) {
            throw new IOException("Invalid reference in QTI manifest", e);
        }
    }

}
