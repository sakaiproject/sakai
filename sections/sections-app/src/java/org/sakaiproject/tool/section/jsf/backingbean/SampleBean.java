/**********************************************************************************
*
* $Id: SampleBean.java 637 2005-07-15 16:35:46Z jholtzman@berkeley.edu $
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.tool.section.decorator.CourseSectionDecorator;

/**
 * A sample jsf backing bean.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class SampleBean extends CourseDependentBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(SampleBean.class);
    
    
    // Fields for the UI (initialize these in init())
    private List sections;
    private String userName;
    private String siteContext;
    private String courseOfferingUuid;
    private List categoryItems;
    
    // Fields for UI Components
    private String title;
    private String category;

	/**
     * Makes any queries necessary to initialize the state of this backing bean.
     * 
     * @see org.sakaiproject.tool.section.jsf.beans.InitializableBean#init()
     */
    protected void init() {
    	super.init();
        if(log.isInfoEnabled()) log.info("SampleBean initializing...");
        
        // Get user and site context from facades
        userName = super.getUserUuid();
        siteContext = super.getSiteContext();

        // Get the course
        Course course = getCourse(siteContext);
    	courseOfferingUuid = course.getUuid();
        
        // Decorate the sections
        Set dbSections = getAllSiteSections();
        sections = new ArrayList();
        for(Iterator iter = dbSections.iterator(); iter.hasNext();) {
        	CourseSection section = (CourseSection)iter.next();
        	CourseSectionDecorator sectionForUi;
        	String catId = section.getCategory();
        	String catName = getCategoryName(catId, FacesContext.getCurrentInstance().getViewRoot().getLocale());
        	sectionForUi = new CourseSectionDecorator(section, catName);
        	sections.add(sectionForUi);
        }

        // Get the category select items
        categoryItems = new ArrayList();
        List categories = getSectionCategories();
        for(Iterator iter = categories.iterator(); iter.hasNext();) {
        	String category = (String)iter.next();
        	String displayName = getSectionAwareness().getCategoryName(category, getLocale());
        	categoryItems.add(new SelectItem(category, displayName));
        }
    }
    
    // Manually initialize the bean
    // TODO Replace with flowState initialization
    public String getConfigureBean() {
        log.info("Manually configuring SampleBean");
        init();
        return "";
    }
    
    //// Action events
    public void processCreateSection(ActionEvent e) {
        if(log.isInfoEnabled()) log.info("Creating section with title = " + title);        
        getSectionManager().addSection(courseOfferingUuid, title, "M,W,F 9-10am", 100,
        		"117 Dwinelle", category);
    }
    
    //// Bean getters / setters for UI

    // Immutable
    public List getSections() {
        return sections;
    }
    public String getUserName() {
        return userName;
    }
	public String getSiteContext() {
		return siteContext;
	}
	public List getCategoryItems() {
		return categoryItems;
	}

	// Mutable
	public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getCourseOfferingUuid() {
		return courseOfferingUuid;
	}
	public void setCourseOfferingUuid(String courseOfferingUuid) {
		this.courseOfferingUuid = courseOfferingUuid;
	}
}


/**********************************************************************************
 * $Id: SampleBean.java 637 2005-07-15 16:35:46Z jholtzman@berkeley.edu $
 *********************************************************************************/
