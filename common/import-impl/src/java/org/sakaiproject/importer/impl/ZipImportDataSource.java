/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/common/branches/sakai-10.x/import-impl/src/java/org/sakaiproject/importer/impl/BasicImportDataSource.java $
 * $Id: BasicImportDataSource.java 133338 2014-01-16 17:17:12Z matthew.buckett@it.ox.ac.uk $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.io.File;

import org.sakaiproject.archive.api.ImportMetadata;
import org.sakaiproject.importer.api.ImportDataSource;
import org.sakaiproject.importer.api.Importable;
import org.apache.commons.io.FileUtils;

public class ZipImportDataSource extends BasicImportDataSource {
	
	private String archiveDir;

	public void setArchiveDir(String a) {
	    archiveDir = a;
	}

	public void cleanup() {
	    if (archiveDir == null)
		return;
	    try {
		FileUtils.deleteDirectory(new File(archiveDir));
	    } catch (Exception e) {
		System.out.println("failed to delete " + archiveDir);
	    }
		//items.iterator().next().get
		
	}

}
