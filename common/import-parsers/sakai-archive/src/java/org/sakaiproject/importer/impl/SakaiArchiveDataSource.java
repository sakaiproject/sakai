/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.importer.impl;

import java.io.InputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.HashSet;

import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.archive.api.ImportMetadata;
import org.sakaiproject.importer.api.SakaiArchive;
import org.sakaiproject.util.Zip;

@Slf4j
public class SakaiArchiveDataSource extends BasicImportDataSource implements SakaiArchive {
	private String sourceFolder;
	private String localArchiveFolder;
	private String pathToArchive;
	private InputStream fileData;

	public SakaiArchiveDataSource(InputStream fileData, String localArchiveFolder, String pathToArchive) {
		this.fileData = fileData;
		this.localArchiveFolder = localArchiveFolder;
		this.pathToArchive = pathToArchive;
		this.sourceFolder = localArchiveFolder + "/source/";
	}

	public String getSourceFolder() {
		return sourceFolder;
	}

	public void setSourceFolder(String sourceFolder) {
		this.sourceFolder = sourceFolder;
	}
	
    public void buildSourceFolder(Collection<ImportMetadata> selectedItems) {
        Set<String> selectedNames = new HashSet<>();
        for (ImportMetadata item : selectedItems) {
            selectedNames.add(item.getFileName());
        }
        Path root = Path.of(pathToArchive, "source");
        try (ZipInputStream zipStream = new ZipInputStream(fileData)) {
            Files.createDirectories(root);
            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                String entryName = Zip.validateEntryName(entry.getName());
                Path destination = Zip.resolveEntryPath(root, entryName);
                if (entryName.endsWith("/")) {
                    Files.createDirectories(destination);
                } else if (selectedNames.contains(entry.getName()) || !entryName.endsWith(".xml")) {
                    Files.createDirectories(destination.getParent());
                    Files.copy(zipStream, destination, StandardCopyOption.REPLACE_EXISTING);
                }
                zipStream.closeEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to extract site archive", e);
        }
    }
}
