/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.importer.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.sakaiproject.archive.api.ImportMetadata;
import org.sakaiproject.importer.api.SakaiArchive;

public class SakaiArchiveDataSource extends BasicImportDataSource implements SakaiArchive {
	private String sourceFolder;
	private String localArchiveFolder;
	private String pathToArchive;
	private byte[] fileData;

	public SakaiArchiveDataSource(byte[] fileData, String localArchiveFolder, String pathToArchive) {
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
	
	public void buildSourceFolder(Collection selectedItems) {		
		try {
			File dir = new File(pathToArchive + "/source"); //directory where file would be saved
			if (!dir.exists())
			{
			  dir.mkdirs();
			}
			  for (Iterator i = selectedItems.iterator();i.hasNext();)
			  {
			    ImportMetadata impvalue = (ImportMetadata) i.next();
			    String selectedFileName = impvalue.getFileName();
			    ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(fileData));
				ZipEntry entry;
				String entryName;
				entry = (ZipEntry) zipStream.getNextEntry();
			    while (entry != null) {
			    	entryName = entry.getName();
			    	if (entryName.equals(selectedFileName)) {
			    		File zipEntryFile = new File(dir.getPath() + "/" + entryName);
			            if (!zipEntryFile.isDirectory()) {
				            FileOutputStream ofile = new FileOutputStream(zipEntryFile);
				            byte[] buffer = new byte[1024 * 10];
				            int bytesRead;
				            while ((bytesRead = zipStream.read(buffer)) != -1)
				            {
				              ofile.write(buffer, 0, bytesRead);
				            }
			
				            ofile.close();
			            }
			            zipStream.closeEntry();
			            zipStream.close();
			            break;
			    	}
			    	entry = (ZipEntry) zipStream.getNextEntry();
			    }
			  }
			  // now take care of attachment files
			  ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(fileData));
				ZipEntry entry;
				String entryName;
				entry = (ZipEntry) zipStream.getNextEntry();
			    while (entry != null) {
			    	entryName = entry.getName();
			    	if (!entryName.endsWith(".xml")) {
			    		File zipEntryFile = new File(dir.getPath() + "/" + entryName);
			            if (!zipEntryFile.isDirectory()) {
				            FileOutputStream ofile = new FileOutputStream(zipEntryFile);
				            byte[] buffer = new byte[1024 * 10];
				            int bytesRead;
				            while ((bytesRead = zipStream.read(buffer)) != -1)
				            {
				              ofile.write(buffer, 0, bytesRead);
				            }
			
				            ofile.close();
			            }
			            zipStream.closeEntry();
			    	}
			    	entry = (ZipEntry) zipStream.getNextEntry();
			    }
			    zipStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
