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

import lombok.extern.slf4j.Slf4j;

import uk.ac.cam.caret.sakai.rwiki.tool.api.PopulateService;

/**
 * PrePopulateBean is a helper bean which given the currentPageRealm, the
 * currentUser, and a populateService will populate the current page realm.
 * 
 * @author andrew
 */
@Slf4j
public class PrePopulateBean
{
	private PopulateService populateService;

	private String currentPageRealm;

	private String currentGroup;

	private String woksiteOwner;

	/**
	 * Populates the current realm, relying on the service for caching etc.
	 */
	public void doPrepopulate()
	{
		log.debug(this.getClass().getName() + " current-user: " + woksiteOwner
				+ " pre-populating realm " + currentPageRealm);

		// Populate the realm...

		populateService.populateRealm(woksiteOwner, currentPageRealm,
				currentGroup);
	}

	/**
	 * The current page realm.
	 * 
	 * @return currentPageRealm
	 */
	public String getCurrentPageRealm()
	{
		return currentPageRealm;
	}

	/**
	 * Set the current page realm.
	 * 
	 * @param currentPageRealm
	 */
	public void setCurrentPageRealm(String currentPageRealm)
	{
		this.currentPageRealm = currentPageRealm;
	}

	/**
	 * The current user.
	 * 
	 * @return current user
	 */
	public String getWoksiteOwner()
	{
		return woksiteOwner;
	}

	/**
	 * Set the current user.
	 * 
	 * @param currentUser
	 */
	public void setWoksiteOwner(String currentUser)
	{
		this.woksiteOwner = currentUser;
	}

	/**
	 * The current populateService
	 * 
	 * @return populateService
	 */
	public PopulateService getPopulateService()
	{
		return populateService;
	}

	/**
	 * Set the current populateService.
	 * 
	 * @param populateService
	 */
	public void setPopulateService(PopulateService populateService)
	{
		this.populateService = populateService;
	}

	/**
	 * @return Returns the currentGroup.
	 */
	public String getCurrentGroup()
	{
		return currentGroup;
	}

	/**
	 * @param currentGroup
	 *        The currentGroup to set.
	 */
	public void setCurrentGroup(String currentGroup)
	{
		this.currentGroup = currentGroup;
	}

}
