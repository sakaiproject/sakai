/**********************************************************************************
 * $URL$
 * $Id$
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

package org.sakaiproject.citation.api;

import org.sakaiproject.entity.api.Entity;

/**
 * CitationHelper describes the contract between the CitationHelper and its clients. 
 */
public interface CitationHelper
{
	/** The identifier by which the CitationHelper can be located (call ToolManager.getTool(HELPER_ID) */
	public static final String CITATION_ID = "sakai.citation.tool";
	
	public static final String SPECIAL_HELPER_ID = "sakai.special.helper.id";
	
	public static final String REFERENCE_ROOT = Entity.SEPARATOR + "citation";
	
	public static final String CITATION_PREFIX = "citation.";
	
	// temporary -- replace these with constants in content-util or content-api
	public static final String RESOURCES_REQUEST_PREFIX = "resources.request.";
	public static final String RESOURCES_SYS_PREFIX = "resources.sys.";
	
	/** The name of the state attribute indicating which mode the helper is in */
	public static final String STATE_HELPER_MODE = CITATION_PREFIX + "helper.mode";

	public static final String CITATION_FRAME_ID = "Citations";

	/** The name that identifies a state attribute which carries an identifier fo the current ContentResource object */
	public static final String RESOURCE_ID = CitationHelper.CITATION_PREFIX + "resource_id";
	public static final String RESOURCE_UUID = CitationHelper.CITATION_PREFIX + "resource_uuid";

	/** The name that identifies a state attribute which carries an identifier fo the current CitationCollection object */
	public static final String CITATION_COLLECTION_ID = CITATION_PREFIX + "current_collection_id";
	
	/** The name that identifies a state attribute which carries the id of the Citation object to edit */
	public static final String CITATION_EDIT_ID = CITATION_PREFIX + "edit_item_id";

	/** The name identifying a ResourceProperty containing the id of a CitationCollection */
	public static final String PROP_CITATION_COLLECTION = "sakai:citation_collection_id";

	/** The name that identifies a state attribute which carries the actual Citation object that is being editted */
	public static final String CITATION_EDIT_ITEM = CITATION_PREFIX + "edit_item_item";

	public static final String CITATION_VIEW_ID = CITATION_PREFIX + "edit_item_id";

	public static final String CITATION_VIEW_ITEM = CITATION_PREFIX + "edit_item_item";

	public static final int DEFAULT_PAGESIZE = 10;
	
	public static final String CITATION_HELPER_INITIALIZED = CITATION_PREFIX + "citation_helper_initialized";

}
