/**********************************************************************************
 * $URL:  $
 * $Id:  $
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

package org.sakaiproject.content.api;

import java.util.Map;

import org.sakaiproject.entity.api.Reference;

/**
 * 
 * 
 */
public interface ResourceToolAction 
{
	public static final String CREATE = "create";
	public static final String DELETE = "delete";
	public static final String COPY = "copy";
	public static final String REVISE_CONTENT = "revise";
	public static final String ACCESS_CONTENT = "access";
	public static final String ACCESS_PROPERTIES = "info";
	public static final String DUPLICATE = "duplicate";
	public static final String MOVE = "move";
	
	/**
	 * Prefix for all keys to Tool Session attributes used in messaging between ResourcesAction and
	 * helpers related to registered resource tool actions. Removing all attributes whose keys begin
	 * with this prefix cleans up the tool session after a helper completes its work. 
	 * ResourcesAction will cleanup tool session.
	 */
	public static final String PREFIX = "resourceToolAction.";
	
	/** 
	 * Key for Tool Session attribute indicating that user canceled the action in the helper. Any (non-null) value indicates true. 
	 * (supplied by helper before stopping helper)
	 */
	public static final String ACTION_CANCELED = PREFIX + "action_canceled";
	
	/** 
	 * Key for Tool Session attribute indicating that the action resulted in an error in the helper that effectively canceled the action. 
	 * Any (non-null) value indicates true. (supplied by helper before stopping helper) 
	 */
	public static final String ACTION_ERROR = PREFIX + "action_error";
	
	/** 
	 * Key for Tool Session attribute indicating that the action succeeded in the helper. Any (non-null) value indicates true. 
	 * (supplied by helper before stopping helper) 
	 */
	public static final String ACTION_SUCCEEDED = PREFIX + "action_succeeded";
	
	/** 
	 * Key for Tool Session attribute identifying the type of resource being created/edited/etc 
	 * (supplied by ResourcesAction before starting helper). 
	 */
	public static final String RESOURCE_TYPE = PREFIX + "resource_type";
	
	/** 
	 * Key for Tool Session attribute identifying the action being invoked 
	 * (supplied by ResourcesAction before starting helper). 
	 */
	public static final String ACTION_ID = PREFIX + "action";

	/** 
	 * Key for Tool Session attribute that provides a Reference object identifying the collection in which an item is to be created 
	 * (supplied by ResourcesAction before starting helper). 
	 */
	public static final String COLLECTION_REFERENCE = PREFIX + "collection_reference";
	
	/** 
	 * Key for Tool Session attribute that provides a String object containing the "content" of the resource
	 * (supplied by ResourcesAction before starting helper). 
	 */
	public static final String RESOURCE_CONTENT = PREFIX + "resource_content";
	
	/** 
	 * Key for Tool Session attribute that provides a String object containing the revised "content" of the 
	 * resource (supplied by helper before stopping helper if the action can create/update the content). 
	 */
	public static final String REVISED_RESOURCE_CONTENT = PREFIX + "revised_resource_content";
	
	/**
	 * Key for Tool Session attribute that provides a Map containing the key-value pairs mapping Strings to Objects
	 * identifying values of ResourceProperties related to the action. If the action registration indicates that 
	 * ResourcesAction should provide existing values for properties, the values of those properties will be 
	 * passed to the helper in this attribute.  If a value for a required property is not yet defined, no entry 
	 * for that property will be included in the Map.  
	 */
	public static final String RESOURCE_PROPERTIES = PREFIX + "resource_properties";
	
	/**
	 * Key for Tool Session attribute that provides a Map containing the key-value pairs mapping Strings to Objects
	 * identifying values of ResourceProperties updated during the action. If the action registration indicates that 
	 * ResourcesAction should provide existing values for properties, the values of those properties will be 
	 * passed to the helper in the RESOURCE_PROPERTIES attribute, and a value should also be provided by the helper 
	 * for each of those properties in this attribute. On completion of the helper's part of the action, the 
	 * Map will include entries for any properties that should be added or updated as a result of the helper's 
	 * part of the action.  If this attribute is defined and the Map contains entries, ResourcesAction will 
	 * include those values in the ResourceProperties for the resource.  If an entry was included for a key in the 
	 * RESOURCE_PROPERTIES attribute and no entry for that key in this attribute, the value associated with that
	 * key will be removed from the entity's resource properties.
	 */
	public static final String REVISED_RESOURCE_PROPERTIES = PREFIX + "revised_resource_properties";
	
	/**
	 * Access the id of this action (which must be unique within this type and must be limited to alphnumeric characters).
	 * @return
	 */
	public String getId();
	
	/**
	 * Access the id of the ResourceType this action relates to.
	 * @return
	 */
	public String getTypeId();
	
	/**
	 * @return
	 */
	public String getLabel();
	
}
