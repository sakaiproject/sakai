/**
 * Copyright (c) 2003-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.archive.api;

import java.io.IOException;

public interface ZipAware {

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
	public String mergeZip(String zipFilePath, String siteId, String siteCreatorId);
	
	/**
	 * Compress a site archive to a zip. Note that the site must have already been archived!
	 * @param siteId - id of site that was archived
	 * @return true if zip successful, false if not.
	 * @throws IOException 
	 */
	public boolean archiveZip(String siteId) throws IOException;
	
	
}
