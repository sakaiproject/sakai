/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

import java.util.Collection;

import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;

/**
 * <p>
 * GroupAwareEdit is an interface that must be implemented to make changes in entities of types that are group aware.
 * </p>
 */
public interface GroupAwareEdit extends GroupAwareEntity, Edit
{
	/**
	 * 
	 * @throws InconsistentException
	 * @throws PermissionException
	 */
	public void clearGroupAccess() throws InconsistentException, PermissionException;
	
	/**
	 * 
	 * @param groups The collection (String) of reference-strings identifying the groups to be added.
	 * @throws InconsistentException
	 * @throws PermissionException
	 */
	public void setGroupAccess(Collection groups) throws InconsistentException, PermissionException;

	/**
	 * 
	 * @throws InconsistentException
	 * @throws PermissionException
	 */
	public void setPublicAccess() throws InconsistentException, PermissionException;
	
	/**
	 * 
	 * @throws InconsistentException
	 * @throws PermissionException
	 */
	public void clearPublicAccess() throws InconsistentException, PermissionException;

}
