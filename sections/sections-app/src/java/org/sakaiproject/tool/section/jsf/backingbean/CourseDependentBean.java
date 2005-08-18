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

import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.el.VariableResolver;

import org.sakaiproject.api.section.SectionAwareness;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.tool.section.manager.SectionManager;

public class CourseDependentBean extends InitializableBean {
	private transient CourseBean courseBean;

	private CourseBean getCourseBean() {
		if(courseBean == null) {
			FacesContext context = FacesContext.getCurrentInstance();
			VariableResolver resolver = context.getApplication().getVariableResolver();
			courseBean = (CourseBean)resolver.resolveVariable(context, "courseBean");
		}
		return courseBean;
	}
	
	protected Locale getLocale() {
		return FacesContext.getCurrentInstance().getViewRoot().getLocale();
	}
	
	protected List getTeachingAssistants(String sectionUuid) {
		return getSectionManager().getTeachingAssistants(sectionUuid);
	}
	protected SectionManager getSectionManager() {
		return getCourseBean().getSectionManager();
	}
	
	protected SectionAwareness getSectionAwareness() {
		return getCourseBean().getSectionAwareness();
	}

	protected String getUserUuid() {
		return getCourseBean().authn.getUserUuid();
	}
	
	protected String getSiteContext() {
		return getCourseBean().context.getContext();
	}
	
	protected Course getCourse(String siteContext) {
		return getCourseBean().sectionManager.getCourse(siteContext);
	}
	
	protected Set getAllSiteSections() {
		String siteContext = getSiteContext();
		return getCourseBean().sectionManager.getSectionAwareness().getSections(siteContext);
	}
	
	protected String getCategoryName(String categoryId, Locale locale) {
		return getCourseBean().sectionManager.getSectionAwareness().getCategoryName(categoryId, locale);
	}
	
	protected List getSectionCategories() {
		return getCourseBean().sectionManager.getSectionAwareness().getSectionCategories();
	}
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
