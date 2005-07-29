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

import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.facade.manager.Authn;
import org.sakaiproject.api.section.facade.manager.Context;
import org.sakaiproject.api.section.coursemanagement.CourseOffering;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.tool.section.decorator.CourseSectionDecorator;
import org.sakaiproject.tool.section.manager.CourseOfferingManager;
import org.sakaiproject.tool.section.manager.SectionManager;

/**
 * A sample jsf backing bean.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class SampleBean extends InitializableBean implements Serializable {


	private static final Log log = LogFactory.getLog(SampleBean.class);
    
    // TODO Centralize the local services in a base backing bean
    private SectionManager sectionManager;
    private CourseOfferingManager courseOfferingManager;
    
    private Authn authn;
    private Context context;
    
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
        userName = authn.getUserUid();
        siteContext = context.getContext();

        // Get the course offering id, and create one if necessary
        courseOfferingUuid = courseOfferingManager.getCourseOfferingUuid(siteContext);
        if(courseOfferingUuid == null) {
        	CourseOffering co = courseOfferingManager.createCourseOffering(siteContext, siteContext, false, false);
        	courseOfferingUuid = co.getUuid();
        	if(log.isInfoEnabled()) log.info("Creating course offering uuid=" + co.getUuid());
        }
        
        // Decorate the sections
        List dbSections = sectionManager.getSectionAwareness().getSections(context.getContext());
        sections = new ArrayList();
        for(Iterator iter = dbSections.iterator(); iter.hasNext();) {
        	CourseSectionDecorator section = new CourseSectionDecorator((CourseSection)iter.next());
        	String cat = section.getCategory();
        	if(cat != null) {
            	section.setCategoryForDisplay(messageBundle.getString(cat));
        	}
        	sections.add(section);
        }
                
        // Get the category select items
        categoryItems = new ArrayList();
        List categories = sectionManager.getSectionAwareness().getSectionCategories();
        for(Iterator iter = categories.iterator(); iter.hasNext();) {
        	String category = (String)iter.next();
        	String displayName = messageBundle.getString(category);
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
        log.info("Creating section with title = " + title + " for course uuid=" + courseOfferingUuid);        
        sectionManager.addSection(courseOfferingUuid, title, "M,W,F 9-10am", null,
        		100, "117 Dwinelle", category);
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

	public void setCourseOfferingManager(CourseOfferingManager courseOfferingManager) {
		this.courseOfferingManager = courseOfferingManager;
	}
}


/**********************************************************************************
 * $Id: SampleBean.java 637 2005-07-15 16:35:46Z jholtzman@berkeley.edu $
 *********************************************************************************/
