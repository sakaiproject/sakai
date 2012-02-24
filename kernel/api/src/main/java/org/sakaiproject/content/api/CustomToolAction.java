/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.content.api;

import java.util.List;

/**
 * CustomToolAction must be implemented by every ResourceToolAction whose ActionType is 
 * ResourceToolAction.ActionType.CUSTOM_TOOL_ACTION.  The interface defines a method 
 * to allow the Resources tool to query a helper to determine permissions for the action.
 * If the permissions are determined entirely by content permissions corresponding to
 * those defined for a common action in the Resources tool (such as create, revise,
 * delete, duplicate, ...), it is better to define the action as an InteractiveAction 
 * or ServiceLevelAction with ActionType defined to match the action whose permissions 
 * it mimics. If custom permissions are required, the ResourceToolAction must be ot type
 * CUSTOM_TOOL_ACTION, and its definition must implement this interface.
 */
public interface CustomToolAction extends ResourceToolAction
{
	/**
	 * Determine whether the current user can perform this action on the resource. 
	 * 
	 * @param entityId The id of the resource.
	 * @param contentPermissions A list of the "content.*" permissions the current user has in the context of the resource.
	 * @param isCreator A flag indicating whether the user is the creator of the resource.
	 * @return true if the user can perform the action and false otherwise.
	 */
	public boolean isAllowed(String entityId, List<String> contentPermissions, boolean isCreator);

}
