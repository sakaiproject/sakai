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

package org.sakaiproject.api.section.coursemanagement;

/**
 * Models a sectionable "class" in higher education.  What a Course actually represents
 * is intentionally ambiguous.  In Sakai 2.1, where multiple sections from a
 * variety of courses may be associated with a site, a Course simply represents the
 * site along with the metadata needed by the Section Manager Tool.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public interface Course extends LearningContext {
	/**
	 * The site associated with this course.
	 * 
	 * @return
	 */
	public String getSiteContext();
	
	/**
	 * Whether the course is externally managed by the enterprise and should be
	 * read-only within the LMS.
	 * 
	 * @return
	 */
	public boolean isExternallyManaged();
	
	/**
	 * Whether students are allowed to register for sections themselves.
	 * 
	 * @return
	 */
	public boolean isSelfRegistrationAllowed();
	
	/**
	 * Whether students are allowed to switch sections themselves.
	 * 
	 * @return
	 */
	public boolean isSelfSwitchingAllowed();
}


/**********************************************************************************
 * $Id$
 *********************************************************************************/
