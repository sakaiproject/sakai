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
package org.sakaiproject.test.section;

import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.CourseOffering;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.facade.manager.Context;
import org.sakaiproject.test.section.manager.CourseOfferingManager;
import org.sakaiproject.tool.section.manager.SectionManager;

/**
 * Each test method is isolated in its own transaction, which is rolled back when
 * the method exits.  Since we can not assume that data will exist, we need to use
 * the SectionManager api to insert data before retrieving it with SectionAwareness.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class SectionManagerTest extends SectionsTestBase{
	private static final Log log = LogFactory.getLog(SectionManagerTest.class);
	
	private Context context;
	private SectionManager secMgr;
	private CourseOfferingManager courseMgr;

    protected void onSetUpInTransaction() throws Exception {
    	context = (Context)applicationContext.getBean("org.sakaiproject.api.section.facade.manager.Context");
        secMgr = (SectionManager)applicationContext.getBean("org.sakaiproject.tool.section.manager.SectionManager");
        courseMgr = (CourseOfferingManager)applicationContext.getBean("org.sakaiproject.test.section.manager.CourseOfferingManager");
    }

    public void testJoinAndSwitchSections() throws Exception {
    	String siteContext = context.getContext();
    	List categories = secMgr.getSectionAwareness().getSectionCategories();
    	
    	// Add a course offering to work from
    	courseMgr.createCourseOffering(siteContext, "A course", false, false, false);
    	CourseOffering course = secMgr.getSectionAwareness().getCourseOffering(siteContext);
    	
    	CourseSection sec = secMgr.addSection(course.getUuid(), "A section", "W,F 9-10AM", 100, "117 Dwinelle", (String)categories.get(0));

    	// Assert that the course offering exists at this context
    	Assert.assertTrue(secMgr.getSectionAwareness().getCourseOffering(siteContext).getUuid().equals(course.getUuid()));
    	
    	// Assert that section awareness can retrieve the new section
    	Set sections = secMgr.getSectionAwareness().getSections(siteContext);
    	Assert.assertTrue(sections.size() == 1);
    	Assert.assertTrue(sections.contains(sec));
    	
    	// Add an enrollment to the section
    	
    	// Assert that the enrollment is in the section
    	
    }
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
