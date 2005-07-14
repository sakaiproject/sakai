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
package org.sakaiproject.tool.gradebook.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.business.GradeManager;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.tool.gradebook.facades.Authn;
import org.sakaiproject.tool.gradebook.facades.Authz;
import org.sakaiproject.tool.gradebook.facades.CourseManagement;
import org.springframework.test.AbstractTransactionalSpringContextTests;

/**
 * Base class for gradebook test classes that provides the spring application
 * context.  The database used is an in-memory hsqldb by default, but this can
 * be overridden to test specific database configurations by setting the "mem"
 * system property to "false".  In the "mem=false" case, the database configuration
 * is set in the hibernate.properties file in the "hibernate.properties.dir" directory.
 * 
 * For tests that should always use the configured database (such as data loading tests),
 * you should extend {@link org.sakaiproject.tool.gradebook.test.GradebookDbTestBase}
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public abstract class GradebookTestBase extends AbstractTransactionalSpringContextTests {
	private static Log log = LogFactory.getLog(GradebookTestBase.class);

    protected Authz authz;
    protected Authn authn;
    protected GradebookManager gradebookManager;
    protected GradeManager gradeManager;
    protected GradebookService gradebookService;
    protected CourseManagement courseManagement;
    
    protected void onSetUpInTransaction() throws Exception {
        authn = (Authn)applicationContext.getBean("org_sakaiproject_tool_gradebook_facades_Authn");
        authz = (Authz)applicationContext.getBean("org_sakaiproject_tool_gradebook_facades_Authz");
        gradebookService = (GradebookService)applicationContext.getBean("org_sakaiproject_service_gradebook_GradebookService");
        gradebookManager = (GradebookManager)applicationContext.getBean("org_sakaiproject_tool_gradebook_business_GradebookManager");
        gradeManager = (GradeManager)applicationContext.getBean("org_sakaiproject_tool_gradebook_business_GradeManager");
        courseManagement = (CourseManagement)applicationContext.getBean("org_sakaiproject_tool_gradebook_facades_CourseManagement");
    }
    
    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations() {
        String mem = System.getProperty("mem");
        
        String[] configLocations = {"", "spring-beans.xml", "spring-beans-test.xml", "spring-hib.xml"};
        
        if("false".equals(mem)) {
            log.debug("Using configured database for testing");
            configLocations[0] = "spring-db.xml";
        } else {
            log.debug("Using in-memory database for testing");
            configLocations[0] = "spring-db-mem.xml";
        }
        return configLocations;
    }
}




