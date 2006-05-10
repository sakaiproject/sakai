/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Regents of the University of California and The Regents of the University of Michigan
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
package org.sakaiproject.api.section.facade.manager;


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
}
