/***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.archive.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.util.Zip;

@Slf4j
public class SiteZipper {
	
	protected ServerConfigurationService serverConfigurationService = null;
	public void setServerConfigurationService(ServerConfigurationService service) {
		serverConfigurationService = service;
	}
	
	/**
	 * Unzip a zip file into the unzip directory. Only unzips the files that are found within the first folder
	 * contained in the zip archive.
	 * @param zipFilePath Path to ZIP file
	 * @param m_unzipPath Path to directory to unzip file into.
	 * @return The toplevel directory which the zipfile created.
	 * @throws IOException
	 */
	public String unzipArchive(String zipFilePath, String m_unzipPath) throws IOException {
		
		log.debug("zipFilePath: " + zipFilePath);

        Path root = Path.of(m_unzipPath);
        Files.createDirectories(root);
        String unzippedArchivePath = null;
        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                Zip.resolveEntryPath(root, entries.nextElement().getName());
            }
            entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = Zip.validateEntryName(entry.getName());
                Path destination = Zip.resolveEntryPath(root, entryName);
                if (entry.isDirectory()) {
                    Files.createDirectories(destination);
                    if (unzippedArchivePath == null) {
                        unzippedArchivePath = entryName;
                    }
                } else if (unzippedArchivePath != null && entryName.startsWith(unzippedArchivePath)) {
                    Files.createDirectories(destination.getParent());
                    try (InputStream in = zipFile.getInputStream(entry);
                         OutputStream out = Files.newOutputStream(destination)) {
                        IOUtils.copy(in, out);
                    }
                } else {
                    log.info("Ignoring entry: {}", entryName);
                }
            }
        }

		//get original filename, remove timestamp, add -archive

		log.debug("unzippedArchivePath: " + unzippedArchivePath);

		return unzippedArchivePath;
	}

	/**
	 * Zip a site archive. It is stored back in the zip directory
	 * @param siteId			site that has already been archived
	 * @param m_storagePath		path to where the archives are
	 * @return
	 * @throws IOException
	 */
	public boolean zipArchive(String siteId, String m_storagePath) throws IOException {
		
		//get path to archive dir for this site
		//suffix of -archive is hardcoded as per archive service
		String archivePath = m_storagePath + siteId + "-archive";
		
		//setup timestamp
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String timestamp = dateFormat.format(Calendar.getInstance().getTime());
		
		//create path to compressed archive
		String compressedArchivePath = m_storagePath + siteId + "-" + timestamp + ".zip";
		File zipFile = new File(compressedArchivePath);
		
		if(!zipFile.exists()) {
			log.info("Creating zip file: " + compressedArchivePath);
			zipFile.createNewFile();
		}

        FileOutputStream fOut = null;
        FileInputStream zip = null;
        BufferedOutputStream bOut = null;
        ZipArchiveOutputStream zOut = null;
        
        try {
            fOut = new FileOutputStream(zipFile);
            bOut = new BufferedOutputStream(fOut);
            zOut = new ZipArchiveOutputStream(bOut);
            addFileToZip(zOut, archivePath, ""); //add the directory which will then add all files recursively

            //create a sha1 hash of the zip
            String hashPath = m_storagePath + siteId + "-" + timestamp + ".sha1";
            log.info("Creating hash: " + hashPath);
            zip  = new FileInputStream(compressedArchivePath);
            String hash = DigestUtils.sha1Hex(zip);
            FileUtils.writeStringToFile(new File(hashPath), hash);
        } finally {
            zOut.finish();
            zOut.close();
            bOut.close();
            fOut.close();
            zip.close();
        }
		
		return true;
	}
	
	/**
     * Creates a zip entry for the path specified with a name built from the base passed in and the file/directory
     * name. If the path is a directory, a recursive call is made such that the full directory is added to the zip.
     *
     * @param zOut The zip file's output stream
     * @param path The filesystem path of the file/directory being added
     * @param base The base prefix to for the name of the zip file entry
     *
     * @throws IOException If anything goes wrong
     */
    private static void addFileToZip(ZipArchiveOutputStream zOut, String path, String base) throws IOException {
        File f = new File(path);
        String entryName = base + f.getName();
        ZipArchiveEntry zipEntry = new ZipArchiveEntry(f, entryName);
 
        zOut.putArchiveEntry(zipEntry);
 
        if (f.isFile()) {
            FileInputStream fInputStream = null;
            try {
                fInputStream = new FileInputStream(f);
                IOUtils.copy(fInputStream, zOut);
                zOut.closeArchiveEntry();
            } finally {
                IOUtils.closeQuietly(fInputStream);
            }
 
        } else {
            zOut.closeArchiveEntry();
            File[] children = f.listFiles();
 
            if (children != null) {
                for (File child : children) {
                    addFileToZip(zOut, child.getAbsolutePath(), entryName + "/");
                }
            }
        }
    }
    
}
