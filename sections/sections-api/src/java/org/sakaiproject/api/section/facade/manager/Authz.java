/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The Regents of the University of Michigan,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
//	/**
//	 * Gets the role for a given user in a given site.
//	 * 
//	 * @param userUid The user's uid
//	 * @param siteContext The site id
//	 * 
//	 * @return
//	 */
//	public Role getSiteRole(String userUid, String siteContext);
//	
//	/**
//	 * Gets the role for a given user in a given CourseSection.
//	 * 
//	 * @param userUid The user's uid
//	 * @param sectionUuid The uuid of a CourseSection
//	 * 
//	 * @return
//	 */
//	public Role getSectionRole(String userUid, String sectionUuid);


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



/**********************************************************************************
 * $Id$
 *********************************************************************************/
