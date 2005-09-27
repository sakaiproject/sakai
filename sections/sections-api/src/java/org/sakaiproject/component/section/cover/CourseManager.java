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

package org.sakaiproject.component.section.cover;

import org.sakaiproject.api.kernel.component.cover.ComponentManager;
import org.sakaiproject.api.section.coursemanagement.Course;

/**
 * A static cover over the section info project's CourseManager.  Note that, since
 * some of CourseManager's interface methods are not implemented in Sakai, and so
 * are not available here.
 * 
 * TODO Move methods not available in sakai into another interface, or implement
 * them via legacy services.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class CourseManager {
	private static org.sakaiproject.api.section.CourseManager instance;

	public static final boolean courseExists(String siteContext) {
		return getInstance().courseExists(siteContext);
	}
	
	public static final Course createCourse(String siteContext, String title, boolean selfRegAllowed, boolean selfSwitchingAllowed, boolean externallyManaged) {
		return getInstance().createCourse(siteContext, title, selfRegAllowed, selfSwitchingAllowed, externallyManaged);
	}

//	public void removeUserFromAllSections(String userUid, String siteContext) {
//		getInstance().removeUserFromAllSections(userUid, siteContext);
//	}

	private static org.sakaiproject.api.section.CourseManager getInstance() {
		if(instance == null) {
			instance = (org.sakaiproject.api.section.CourseManager)ComponentManager.get(
					org.sakaiproject.api.section.CourseManager.class);
		}
		return instance;
	}

}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
