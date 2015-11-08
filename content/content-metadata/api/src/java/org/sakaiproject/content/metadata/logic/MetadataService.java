/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.content.metadata.logic;

import java.util.List;

import org.sakaiproject.content.metadata.model.MetadataType;

/**
 * Service providing tools to handle metadata on Content
 *
 * @author Colin Hebert
 */
public interface MetadataService
{
	/**
	 * Get all metadata groups available on the server
	 *
	 * @return A list of metadata fields, or an empty list if there is none.
	 * @param resourceType Restricts the returned values to those applicable to this resourceType. Empty string means all.
	 */
	List<MetadataType> getMetadataAvailable(String resourceType);

	/**
	 * Get all metadata groups available on the server for a specific site and resourceType
	 *
	 * @param siteId			 Site identifier
	 * @param resourceType Restricts the returned values to those applicable to this resourceType. Empty string means all.
	 * @return A list of metadata fields, or an empty list if there is none.
	 */
	List<MetadataType> getMetadataAvailable(String siteId, String resourceType);
}
