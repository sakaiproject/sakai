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

package org.sakaiproject.archive.api;

import java.io.IOException;

/**
 * <p>
 * ArchiveService takes care of exporting and importing entities.
 * </p>
 */
public interface ArchiveService
{
	/** This string can be used to find the service in the service manager. */
	static final String SERVICE_NAME = ArchiveService.class.getName();

	/** A tag for the input system. */
	static final String FROM_CT = "CT";

	static final String FROM_WT = "WT";

	static final String FROM_SAKAI = "Sakai 1.0";

    static final String FROM_SAKAI_2_8 = "Sakai 2.8";

	// the list of boolean tells if the imported item needs to be set as draft
	static final boolean SAKAI_msg_draft_import = true;

	static final boolean SAKAI_assign_draft_import = true;

	static final boolean SAKAI_rsc_draft_import = false;

	static final boolean SAKAI_schedule_draft_import = true;

	/**
	 * Create an archive for the resources of a site.
	 * 
	 * @param siteId
	 *        The id of the site to archive.
	 * @return A log of messages from the archive.
	 */
	String archive(String siteId);

	/**
	 * Read in an archived set of resources, and merge the entries into the specified site, and set site creator name
	 * 
	 * @param archiveUrl
	 *        The archive xml file Url.
	 * @param siteId
	 *        The id of the site to merge the content into.
	 * @param siteCreatorId
	 *        the site creator Id
	 * @return A log of messages from the merge.
	 */
	String merge(String archiveUrl, String siteId, String siteCreatorId);
	
	/**
	 * Read in an archived set of resources, and merge the entries into the specified site, and set site creator name
	 * Allows a ZIP file to be used instead of the site archive directory
	 * 
	 * @param zipFilePath
	 *        The archived site as a zip file
	 * @param siteId
	 *        The id of the site to merge the content into.
	 * @param siteCreatorId
	 *        the site creator Id
	 * @return A log of messages from the merge.
	 */
	public String mergeFromZip(String zipFilePath, String siteId, String siteCreatorId);
	
	/**
	 * Archive a site then compress it to a zip. 
	 * @param siteId - id of site to be archived
	 * @return A log of messages from creating the archive
	 * @throws IOException 
	 */
	public String archiveAndZip(String siteId) throws IOException;
}
