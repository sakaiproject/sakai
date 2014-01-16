/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.archive.api;

import java.util.List;

import org.w3c.dom.Document;

/**
 * ...
 */
public interface ImportMetadataService
{
	public static final String SERVICE_NAME = ImportMetadataService.class.getName();

	/**
	 * @param doc
	 *        doc containing import_config.xml
	 * @return List containing ImportMetadata
	 */
	public List getImportMetadataElements(Document doc);

	/**
	 * @param id
	 *        id of the MapEntry requested
	 * @return ImportMetada map entry
	 */
	public ImportMetadata getImportMapById(String id);

	/**
	 * @param username
	 *        to compare if it has maintain role in site.xml
	 * @param siteDoc -
	 *        site.xml in document format
	 * @return true / false
	 */
	public boolean hasMaintainRole(String username, Document siteDoc);
}
