/**********************************************************************************
*
* $Id: SampleComponentTest.java 634 2005-07-14 23:54:16Z jholtzman@berkeley.edu $
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

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.tool.section.CourseOfferingImpl;
import org.sakaiproject.tool.section.manager.CourseOfferingManager;
import org.sakaiproject.tool.section.manager.SectionManager;

/**
 * Each test method is isolated in its own transaction, which is rolled back when
 * the method exits.  Since we can not assume that data will exist, we need to use
 * the SectionManager api to insert data before retrieving it with SectionAwareness.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class SectionAwarenessTest extends SectionsTestBase{
	private static final Log log = LogFactory.getLog(SectionAwarenessTest.class);
	
    private CourseOfferingManager com;
	private SectionManager sm;

    protected void onSetUpInTransaction() throws Exception {
    	com = (CourseOfferingManager)applicationContext.getBean("org.sakaiproject.tool.section.manager.CourseOfferingManager");
        sm = (SectionManager)applicationContext.getBean("org.sakaiproject.tool.section.manager.SectionManager");
    }

    public void testGetSections() throws Exception {
    	// Add a course offering to work from
    	String context = "foo";
    	CourseOfferingImpl newCourse = (CourseOfferingImpl)com.createCourseOffering(context, context, false, false);

    	// Assert that the course offering exists at this context
    	Assert.assertTrue(com.getCourseOfferingUuid("foo").equals(newCourse.getUuid()));
    	
    	// Add a section to work from
    	CourseSection newSection = sm.addSection(newCourse.getUuid(), context, context, context, 10, context, null);

    	// Assert that section awareness can retrieve the new section
    	List sections = sm.getSectionAwareness().getSections(context);
    	Assert.assertTrue(sections.size() == 1);
    	Assert.assertTrue(sections.contains(newSection));
    }
}



/**********************************************************************************
 * $Id: $
 *********************************************************************************/
