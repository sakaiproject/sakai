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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.archive.api.ImportMetadata;
import org.sakaiproject.importer.api.SakaiArchive;

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
	    File dir = new File(pathToArchive + "/source"); //directory where file would be saved
	    if (!dir.exists())
	    {
	        dir.mkdirs();
	    }
	    for (ImportMetadata impvalue: selectedItems)
	    {
	        String selectedFileName = impvalue.getFileName();
	        ZipInputStream zipStream = new ZipInputStream(fileData);
	        try {
    	        ZipEntry entry;
    	        String entryName;
    	        try {
                    entry = (ZipEntry) zipStream.getNextEntry();
                } catch (IOException e) {
                    entry = null; // I think this is ok
                }
    	        while (entry != null) {
    	            entryName = entry.getName();
    	            if (entryName.equals(selectedFileName)) {
    	                // TODO this code block is identical to the code below? (See A1)
    	                File zipEntryFile = new File(dir.getPath() + "/" + entryName);
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
                                        // tried
                                    }
                                }
                            }
    	                }
    	                try {
                            zipStream.closeEntry();
                        } catch (IOException e) {
                            // tried
                        }
    	                try {
                            zipStream.close();
                        } catch (IOException e) {
                            // tried
                        }
    	                break;
    	            }
    	            try {
    	                entry = (ZipEntry) zipStream.getNextEntry();
    	            } catch (IOException e) {
    	                entry = null; // I think this is ok
    	            }
    	        }
	        } finally {
                try {
                    zipStream.closeEntry();
                } catch (IOException e) {
                    // tried
                }
                try {
                    zipStream.close();
                } catch (IOException e) {
                    // tried
                }
	        }
	    }
	    // now take care of attachment files
	    ZipInputStream zipStream = new ZipInputStream(fileData);
	    try {
    	    ZipEntry entry;
    	    String entryName;
            try {
                entry = (ZipEntry) zipStream.getNextEntry();
            } catch (IOException e) {
                entry = null; // I think this is ok
            }
    	    while (entry != null) {
    	        entryName = entry.getName();
    	        if (!entryName.endsWith(".xml")) {
                    // TODO A1 this code block is identical to the code above?
    	            File zipEntryFile = new File(dir.getPath() + "/" + entryName);
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
                                    // tried
                                }
                            }
                        }
    	            }
    	            try {
                        zipStream.closeEntry();
                    } catch (IOException e) {
                        // tried
                    }
    	        }
                try {
                    entry = (ZipEntry) zipStream.getNextEntry();
                } catch (IOException e) {
                    entry = null; // I think this is ok
                }
    	    }
	    } finally {
            try {
                zipStream.closeEntry();
            } catch (IOException e) {
                // tried
            }
            try {
                zipStream.close();
            } catch (IOException e) {
                // tried
            }
	    }
	}


}
