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

package uk.ac.cam.caret.sakai.rwiki.tool.bean;

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;

/**
 * A Bean that has various helper methods to check whether certain things can be
 * done to a RWikiObject
 * 
 * @author andrew
 */

public class PermissionsBean
{

	/**
	 * currently set rwikiObject
	 */
	private RWikiObject rwikiObject;

	/**
	 * ObjectService to perform permissions checks with
	 * 
	 * @see RWikiObjectService
	 */
	private RWikiObjectService objectService;

	public PermissionsBean()
	{

	}

	/**
	 * Creates a fully set-up PermissionsBean
	 * 
	 * @param rwikiObject
	 * @param currentUser
	 * @param objectService
	 */
	public PermissionsBean(RWikiObject rwikiObject,
			RWikiObjectService objectService)
	{
		this.rwikiObject = rwikiObject;
		this.objectService = objectService;
	}

	/**
	 * The currently set RWikiObjectService
	 * 
	 * @return objectService
	 */
	public RWikiObjectService getObjectService()
	{
		return objectService;
	}

	/**
	 * Sets the current RWikiObjectService
	 * 
	 * @param objectService
	 */
	public void setObjectService(RWikiObjectService objectService)
	{
		this.objectService = objectService;
	}

	/**
	 * The current RWikiObject.
	 * 
	 * @return rwikiObject.
	 */
	public RWikiObject getRwikiObject()
	{
		return rwikiObject;
	}

	/**
	 * Sets the current RWikiObject.
	 * 
	 * @param rwikiObject.
	 */
	public void setRwikiObject(RWikiObject rwikiObject)
	{
		this.rwikiObject = rwikiObject;
	}

	/**
	 * Using the current objectService checks whether admin functions can be
	 * performed on the current RWikiObject by the currentUser.
	 * 
	 * @return true if the currentUser has admin rights on this rwikiObject.
	 */
	public boolean isAdminAllowed()
	{
		return objectService.checkAdmin(rwikiObject);
	}

	/**
	 * Using the current objectService checks whether update can be performed on
	 * the current RWikiObject by the currentUser.
	 * 
	 * @return true if the currentUser has update rights on this rwikiObject
	 */
	public boolean isUpdateAllowed()
	{
		return objectService.checkUpdate(rwikiObject);
	}

	/**
	 * Using the current objectService checks whether the current RWikiObject
	 * can be read by the currentUser.
	 * 
	 * @return true if the currentUser has read rights on this rwikiObject.
	 */
	public boolean isReadAllowed()
	{
		return objectService.checkRead(rwikiObject);
	}
}
