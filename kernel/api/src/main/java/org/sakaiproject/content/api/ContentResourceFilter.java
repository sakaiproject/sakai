/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

public interface ContentResourceFilter 
{
	/**
	 * Implement this method to control which actions are allowed for 
	 * ContentCollections.  This deals with "create" actions in particular. 
	 * Implementation should inspect the actions in the list and include
	 * them in the returned list if they are can result in creation of 
	 * resources that can be selected. Otherwise they should not be 
	 * included.
	 * @param actions A collection of actions to test
	 * @return A list of actions that should be shown in the filepicker
	 * 	for each collection.
	 */
	public List<ResourceToolAction> filterAllowedActions(List<ResourceToolAction> actions);

	/**
	 * Implement this method to control which resources are allowed
	 * to be selected.  Implementation should inspect the resource and
	 * return true if the resource should be selectable and false if not.
	 * @param contentResource resource to test
	 * @return true if resource should be selectable, false if not
	 */
	public boolean allowSelect(ContentResource contentResource);

	/**
	 * Implement this method to control which resources are viewable.
	 * Implementation should inspect the resource and
	 * return true if the resource should be presented in the list
	 * and false if not.
	 * @param contentResource resource to test
	 * @return true if resource should be viewable, false if not
	 */
	public boolean allowView(ContentResource contentResource);
}
