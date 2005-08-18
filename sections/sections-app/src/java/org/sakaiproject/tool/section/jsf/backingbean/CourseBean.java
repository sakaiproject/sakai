/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

import org.sakaiproject.api.section.SectionAwareness;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.facade.manager.Authn;
import org.sakaiproject.api.section.facade.manager.Context;
import org.sakaiproject.tool.section.manager.SectionManager;

public class CourseBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String courseUuid;

	protected SectionManager sectionManager;
    protected Authn authn;
    protected Context context;

	protected String getCourseUuid() {
		// TODO Do we ever have a need to cache the course object? I don't think so, but keep an eye on this
		Course course = sectionManager.getCourse(context.getContext());
		courseUuid = course.getUuid();
		return courseUuid;
	}
	
	public SectionManager getSectionManager() {
		return sectionManager;
	}
	
	public SectionAwareness getSectionAwareness() {
		return sectionManager.getSectionAwareness();
	}
	    
    //// Setters for dep. injection
    public void setSectionManager(SectionManager sectionManager) {
        this.sectionManager = sectionManager;
    }
    
    public void setAuthn(Authn authn) {
        this.authn = authn;
    }

	public void setContext(Context context) {
		this.context = context;
	}

}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
