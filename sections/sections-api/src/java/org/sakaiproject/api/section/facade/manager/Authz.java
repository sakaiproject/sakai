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

import org.sakaiproject.api.section.facade.Role;

/**
 * A facade that provides answers to the section manager's authorization questions.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public interface Authz {
	/**
	 * Gets the role for a given user in a given site.
	 * 
	 * @param userUuid The user's uuid
	 * @param siteContext The site id
	 * 
	 * @return
	 */
	public Role getSiteRole(String userUuid, String siteContext);
	
	/**
	 * Gets the role for a given user in a given CourseSection.
	 * 
	 * @param userUuid The user's uuid
	 * @param sectionUuid The uuid of a CourseSection
	 * 
	 * @return
	 */
	public Role getSectionRole(String userUuid, String sectionUuid);
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
