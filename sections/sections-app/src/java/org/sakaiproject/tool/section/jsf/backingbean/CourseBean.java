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

package org.sakaiproject.tool.section.jsf.backingbean;

import java.io.Serializable;

import org.sakaiproject.api.section.SectionManager;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.facade.manager.Authn;
import org.sakaiproject.api.section.facade.manager.Authz;
import org.sakaiproject.api.section.facade.manager.Context;

/**
 * Provides the current course uuid for a given user session.  This is also the
 * integration point for JSF backing beans and Spring-manages services.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class CourseBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String courseUuid;

	protected SectionManager sectionManager;
    protected Authn authn;
    protected Authz authz;
    protected Context context;

	protected String getCourseUuid() {
		Course course = sectionManager.getCourse(context.getContext(null));
		courseUuid = course.getUuid();
		return courseUuid;
	}
	
	protected SectionManager getSectionManager() {
		return sectionManager;
	}
	
    //// Setters for dep. injection
    public void setSectionManager(SectionManager sectionManager) {
        this.sectionManager = sectionManager;
    }
    
    public void setAuthn(Authn authn) {
        this.authn = authn;
    }

    public void setAuthz(Authz authz) {
        this.authz = authz;
    }

	public void setContext(Context context) {
		this.context = context;
	}
	
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
