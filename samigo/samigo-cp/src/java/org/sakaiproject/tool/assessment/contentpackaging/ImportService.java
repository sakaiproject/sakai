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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilder;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.util.Xml;

/** Operation-scoped QTI content-package extraction. */
@Slf4j
public class ImportService {
    private final ServerConfigurationService serverConfigurationService;
    private final String agentId;
    private String qtiFilename;

    public ImportService(ServerConfigurationService serverConfigurationService, String agentId) {
        this.serverConfigurationService = serverConfigurationService;
        this.agentId = agentId;
    }

    public String unzipImportFile(String filename) throws IOException {
        String repositoryPath = serverConfigurationService.getString("samigo.answerUploadRepositoryPath",
            "${sakai.home}/samigo/answerUploadRepositoryPath/");
        Path parent = Path.of(repositoryPath, "jsf", "upload_tmp", "qti_imports", agentId, "unzip_files");
        Files.createDirectories(parent);
        Path root = Files.createTempDirectory(parent, "import-").toRealPath();
        qtiFilename = "exportAssessment.xml";

        try (ZipInputStream zipStream = new ZipInputStream(new FileInputStream(filename))) {
            String tmpName = new File(filename).getName();
            List<String> xmlFilenames = new ArrayList<>();
            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                String entryName = entry.getName();
                Path destination = resolveImportPath(root, entryName);
                if (entry.isDirectory()) {
                    Files.createDirectories(destination);
                } else {
                    Files.createDirectories(destination.getParent());
                    try (java.io.OutputStream output = Files.newOutputStream(destination)) {
                        zipStream.transferTo(output);
                    }
                }

                if ("imsmanifest.xml".equals(entryName)) {
                    try {
                        DocumentBuilder db = Xml.createSecureDocumentBuilderFactory().newDocumentBuilder();
                        Document doc = db.parse(destination.toFile());
                        NodeList nodeLst = doc.getElementsByTagName("resource");
                        Node fstNode = nodeLst.item(0);
                        NamedNodeMap namedNodeMap = fstNode.getAttributes();
                        qtiFilename = namedNodeMap.getNamedItem("href").getNodeValue();
                    } catch (Exception e) {
                        log.warn("Could not parse imsmanifest.xml: {}", e.toString());
                    }
                } else if (entryName.trim().endsWith(".xml")) {
                    String entryNameTrimmed = entryName.trim();
                    xmlFilenames.add(entryNameTrimmed);
                    if (!xmlFilenames.contains(qtiFilename.trim())) {
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
                resolveImportPath(root, qtiFilename);
                zipStream.closeEntry();
            }
            qtiFilename = root.relativize(resolveImportPath(root, qtiFilename)).toString();
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

    private Path resolveImportPath(Path root, String name) throws IOException {
        String portableName = name.replace('\\', '/');
        if (portableName.startsWith("/") || (portableName.length() > 1 && portableName.charAt(1) == ':')) {
            throw new IOException("Absolute path in QTI content package");
        }
        Path destination = root.resolve(portableName).toFile().getCanonicalFile().toPath();
        if (!destination.startsWith(root) || destination.equals(root)) {
            throw new IOException("Path escapes QTI content package directory");
        }
        return destination;
    }

    public String getQtiFilename() {
        return qtiFilename;
    }

    public void setQtiFilename(String qtiFilename) {
        this.qtiFilename = qtiFilename;
    }
}
