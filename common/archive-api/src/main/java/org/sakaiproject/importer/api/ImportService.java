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
 *       http://www.opensource.org/licenses/ECL-2.0
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
import java.io.InputStream;

/**
 *  The ImportService provides a top level framework to handled import data collected from
 *  a content package or other archive.
 *
 */
public interface ImportService {

	/**
	 *  Check the validity of the file data passed.
	 *  Currently if you use this method, it's important you use an InputStream that does a .reset() on .close(). The archive-api's org.sakaiproject.importer.api.ResetOnCloseInputStream is one such example of this.
	 *  This is because this InputStream may be re-used.
	 *
	 *  @param archiveFileData is an input stream of data gathered from an archive file or package.
	 *  @return true if file data is valid.
	 *  @see org.sakaiproject.importer.api.ResetOnCloseInputStream
	 */
	boolean isValidArchive(ResetOnCloseInputStream archiveFileData);

	/**
	 *  Parse the archive file data and create an Import Data Source object containing the results.
	 *  Currently if you use this method, it's important you use an InputStream that does a .reset() on .close(). The archive-api's org.sakaiproject.importer.api.ResetOnCloseInputStream is one such example of this.
	 *  This is because this InputStream may be re-used.
	 *
	 *	@param archiveFileData is an input stream of data gathered from an archive file or package.
	 *  @return ImportDataSource containing parsing results.
	 *  @see org.sakaiproject.importer.api.ResetOnCloseInputStream
	 */
	ImportDataSource parseFromFile(ResetOnCloseInputStream archiveFileData);

	/**
	 *  Put the parsed items into a site.
	 *
	 *  @param importables a collection of importable things to import.
	 *  @param siteId is the the id of the site to import to.
	 */
	void doImportItems(Collection<Importable> importables, String siteId);

}
