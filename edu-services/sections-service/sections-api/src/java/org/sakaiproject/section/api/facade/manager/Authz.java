/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.section.api.facade.manager;


/**
 * A facade that provides answers to the section manager's authorization questions.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public interface Authz {

	/**
	 * Returns whether the current user can manage (add, edit, delete) sections.
	 * 
	 * @param siteContext
	 * @param userUid The user's uid
	 * 
	 * @return
	 */
	public boolean isSectionManagementAllowed(String userUid, String siteContext);

	/**
	 * Returns whether the current user can view all sections and their enrollments.
	 * 
	 * @param siteContext
	 * @param userUid The user's uid
	 * 
	 * @return
	 */
	public boolean isViewAllSectionsAllowed(String userUid, String siteContext);

	/**
	 * Returns whether the current user can change section options in the site.
	 * 
	 * @param siteContext
	 * @param userUid The user's uid
	 * 
	 * @return
	 */
	public boolean isSectionOptionsManagementAllowed(String userUid, String siteContext);
	
	/**
	 * Returns whether the current user can change section enrollments.
	 * 
	 * @param siteContext
	 * @param userUid The user's uid
	 * 
	 * @return
	 */
	public boolean isSectionEnrollmentMangementAllowed(String userUid, String siteContext);
	
	/**
	 * Returns whether the current user can change TA assignments to sections.
	 * 
	 * @param siteContext
	 * @param userUid The user's uid
	 * 
	 * @return
	 */
	public boolean isSectionTaManagementAllowed(String userUid, String siteContext);

	/**
	 * Returns whether the current user is a student in the course
	 * 
	 * @param userUid
	 * @param siteContext
	 * @return
	 */
	public boolean isViewOwnSectionsAllowed(String userUid, String siteContext);
	
	/**
	 * Returns whether the user can be assigned to specific sections.  In general, a TA
	 * can be assigned to a section, while an instructor can not (since they are site-wide
	 * administrators).
	 * 
	 * @param userUid
	 * @param siteContext
	 * @return
	 */
	public boolean isSectionAssignable(String userUid, String siteContext);
	
	/**
	 * Returns the role description for a user in a particular context.  This isn't strictly
	 * authz, but it doesn't seem to require its own facade.
	 * 
	 * @param userUid
	 * @param siteContext
	 * @return
	 */
	public String getRoleDescription(String userUid, String siteContext);
}
