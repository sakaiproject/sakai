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
	
	public static final String PREFIX = "resourceToolAction.";
	public static final String ACTION_CANCELED = PREFIX + "action_canceled";
	public static final String ACTION_ERROR = PREFIX + "action_error";
	public static final String ACTION_SUCCEEDED = PREFIX + "action_succeeded";
	
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
