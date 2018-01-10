/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008, 2009 The Sakai Foundation
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.activation.MimetypesFileTypeMap;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.sakaiproject.importer.api.ImportDataSource;
import org.sakaiproject.importer.api.ImportFileParser;

@Slf4j
public abstract class ZipFileParser implements ImportFileParser {
	protected MimetypesFileTypeMap mimeTypes = new MimetypesFileTypeMap();
	protected String pathToData;
	protected String localArchiveLocation;

	public boolean isValidArchive(InputStream fileData) {
		ZipInputStream zipStream = new ZipInputStream(fileData);
		ZipEntry entry;
		try {
			entry = (ZipEntry) zipStream.getNextEntry();
		} catch (IOException e) {
			// IOException definitely indicates not a valid archive
			return false;
		} finally {
		    try {
                zipStream.close();
            } catch (IOException e) {
                // we tried
            }
		}
		
	    return (entry != null);
	}

	public ImportDataSource parse(InputStream fileData, String unArchiveLocation) {
		this.localArchiveLocation = unzipArchive(fileData, unArchiveLocation);
		this.pathToData = unArchiveLocation + File.separator + localArchiveLocation;
		awakeFromUnzip(pathToData);
		List categories = new ArrayList();
		Collection items = new ArrayList();
		categories.addAll(getCategoriesFromArchive(pathToData));
		items.addAll(getImportableItemsFromArchive(pathToData));
		
		ZipImportDataSource dataSource = new ZipImportDataSource();
	    dataSource.setItemCategories(categories);
	    dataSource.setItems(items);
		return dataSource;
	}
	
	protected abstract void awakeFromUnzip(String unArchiveLocation);

	protected abstract Collection getImportableItemsFromArchive(String pathToData);

	protected abstract Collection getCategoriesFromArchive(String pathToData);

	protected String unzipArchive(InputStream fileData, String unArchiveLocation) {
	    String localArchiveLocation = Long.toString(new java.util.Date().getTime());
	    String pathToData = unArchiveLocation + "/" + localArchiveLocation;
	    File dir = new File(pathToData); //directory where file would be saved
	    if (!dir.exists()) {
	        dir.mkdirs();
	    }

	    Set<String> dirsMade = new TreeSet<String>();
	    ZipInputStream zipStream = new ZipInputStream(fileData);
	    try {
    	    ZipEntry entry = null;
    	    try {
    	        entry = (ZipEntry) zipStream.getNextEntry();
    	    } catch (IOException e) {
    	        // TODO I think this is actually ok since this basically goes until it fails anyway
    	        entry = null;
    	    }
    	    boolean foundTheManifest = false;
    	    while (entry != null)
    	    {
    	        String zipName = entry.getName();
    	        // if the imsmanifest.xml is at the root, do not recurse the dirs for looking for another manifest
    	        if (!foundTheManifest && zipName.endsWith("imsmanifest.xml") && zipName.startsWith("imsmanifest.xml")) {
    	            foundTheManifest = true;
    	        }
    	        // figure out if the manifest file is buried somewhere below the top of the archive
    	        if (!foundTheManifest && zipName.endsWith("imsmanifest.xml") && !zipName.startsWith("imsmanifest.xml")) {
    	            localArchiveLocation += "/" + zipName.substring(0, zipName.lastIndexOf("/"));
    	            foundTheManifest = true;
    	        }
    	        //for attachment type files
    	        // Get the directory part.
    	        int ix = zipName.lastIndexOf('/');
    	        if (ix <= 0) ix = zipName.lastIndexOf('\\');
    	        if (ix > 0) {
    	            String dirName = zipName.substring(0, ix).replace("\\", "/");
    	            if (!dirsMade.contains(dirName)) {
    	                File d = new File(dir.getPath() + "/" + dirName);
    	                // If it already exists as a dir, don't do anything
    	                if (!(d.exists() && d.isDirectory())) {
    	                    // Try to create the directory, warn if it fails
    	                    if (!d.mkdirs()) {
    	                        //Log.warn("Warning: unable to mkdir " + dir.getPath() + "/" + dirName);
    	                    }  
    	                    dirsMade.add(dirName);
    	                }
    	            }
    	        }
    	        File zipEntryFile = new File(dir.getPath() + "/" + zipName.replace("\\", "/"));
    	        if (!zipEntryFile.isDirectory()) {
    	            FileOutputStream ofile = null;
                    try {
                        ofile = new FileOutputStream(zipEntryFile);
                        byte[] buffer = new byte[1024 * 10];
                        int bytesRead;
                        while ((bytesRead = zipStream.read(buffer)) != -1)
                        {
                            ofile.write(buffer, 0, bytesRead);
                        }
                    } catch (FileNotFoundException e) {
                        log.error(e.getMessage(), e);
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    } finally {
                        if (ofile != null) {
                            try {
                                ofile.close();
                            } catch (IOException e) {
                                // we tried
                            }
                        }
                    }
    	        }
                try {
                    zipStream.closeEntry();
                } catch (IOException e) {
                    // we tried
                }
    	        try {
    	            entry = (ZipEntry) zipStream.getNextEntry();
    	        } catch (IOException e) {
    	            // TODO I think this is actually ok since this basically goes until it fails anyway
    	            entry = null;
    	        }
    	    }
	    } finally {
            try {
                zipStream.closeEntry();
            } catch (IOException e) {
                // we tried
            }
	    }
	    return localArchiveLocation;
	}
	
	protected boolean fileExistsInArchive(String pathAndFilename, InputStream archive) {
		ZipInputStream zipStream = new ZipInputStream(archive);
		ZipEntry entry;
		String entryName;
		if (pathAndFilename.charAt(0) == '/') {
			pathAndFilename = pathAndFilename.substring(1);
		}
		try {
			entry = (ZipEntry) zipStream.getNextEntry();
		    while (entry != null) {
		    	entryName = entry.getName();
		    	if (entryName.endsWith(pathAndFilename)) return true;
		    	entry = (ZipEntry) zipStream.getNextEntry();
		    }
		    return false;
		} catch (IOException e) {
			
			return false;
		} finally {
		    try {
                zipStream.close();
            } catch (IOException e) {
                // we tried
            }
		}
		
	}
	
	protected Document extractFileAsDOM(String pathAndFilename, InputStream archive) {
		ZipInputStream zipStream = new ZipInputStream(archive);
		ZipEntry entry;
		String entryName;
		if (pathAndFilename.charAt(0) == '/') {
			pathAndFilename = pathAndFilename.substring(1);
		}
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		try {
			docBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			docBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			docBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			docBuilderFactory.setNamespaceAware(true);
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			entry = (ZipEntry) zipStream.getNextEntry();
		    while (entry != null) {
		    	entryName = entry.getName();
		    	if (entryName.endsWith(pathAndFilename)) {
		            return (Document) docBuilder.parse(zipStream);
		    	}
		    	entry = (ZipEntry) zipStream.getNextEntry();
		    }
		    return null;
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return null;
		} catch (ParserConfigurationException e) {
			log.error(e.getMessage(), e);
			return null;
		} catch (FactoryConfigurationError e) {
			log.error(e.getMessage(), e);
			return null;
		} catch (SAXException e) {
			log.error(e.getMessage(), e);
			return null;
		} finally {
		    try {
                zipStream.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
		    
		}
	}
	
	
	protected byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
    
        // Create the byte array to hold the data
        byte[] bytes;
        try {
            // Get the size of the file
            long length = file.length();
   
            // You cannot create an array using a long type.
            // It needs to be an int type.
            // Before converting to an int type, check
            // to ensure that file is not larger than Integer.MAX_VALUE.
            if (length > Integer.MAX_VALUE) {
                // File is too large
            }
   
            bytes = new byte[(int)length];
   
            // Read in the bytes
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length
                   && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
                offset += numRead;
            }
   
            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file "+file.getName());
            }
        } finally {
            // Close the input stream and return bytes
            is.close();
        }
        return bytes;
    }

}
