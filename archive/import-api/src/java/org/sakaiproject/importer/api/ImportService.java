/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.importer.api;

import java.util.Collection;

/**
 *  The ImportService provides a top level framework to handled import data collected from
 *  a content package or other archive.
 *
 */
public interface ImportService {

	/**
	 *  Check the validity of the file data passed.
	 *
	 *  @param archiveFileData is a byte array of data gathered from an archive file or package.
	 *  @return true if file data is valid.
	 */
	boolean isValidArchive(byte[] archiveFileData);

	/**
	 *  Parse the archive file data and create an Import Data Source object containing the results.
	 *
	 *	@param archiveFileData is a byte array of data gathered from an archive file or package.
	 *  @return ImportDataSource containing parsing results.
	 */
	ImportDataSource parseFromFile(byte[] archiveFileData);

	/**
	 *  doImportItems
	 *
	 *  @param importable a collection of things to import (?)
	 *  @param siteId is the the id of the site to import to.
	 */
	void doImportItems(Collection importables, String siteId);

}
