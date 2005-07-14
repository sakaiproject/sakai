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

import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.extensions.TestSetup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.GradebookArchiveService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.GradeManager;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.w3c.dom.Document;

/**
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman </a>
 *
 */
public class TestArchive extends GradebookTestBase {
    private static final Log log = LogFactory.getLog(TestArchive.class);
    private static String GRADEBOOK_SERVICE_BEAN = "org.sakaiproject.service.gradebook.GradebookService";
    private static String GRADABLE_OBJECT_BEAN = "org_sakaiproject_tool_gradebook_business_GradeManager";
    private static String ARCHIVE_BEAN = "org.sakaiproject.service.gradebook.shared.GradebookArchiveService";
    private static String GRADEBOOK_COPY = "copiedFromArchive";
    
    static GradebookManager gradebookManager;
    static GradeManager gradeManager;
    static GradebookService gradebookService;
    static GradebookArchiveService archiveService;
    
    public static Test suite() {
        TestSetup setup = new TestSetup(new TestSuite(TestArchive.class)) {
            protected void setUp() throws Exception {
                oneTimeSetup();
            }
        };
        return setup;
    }

    private static void oneTimeSetup() throws Exception {
        // Force initialization of spring-managed services
        TestArchive test = new TestArchive();
        test.setUp();
    }

    protected void onSetUpInTransaction() throws Exception {
        gradebookManager = (GradebookManager)applicationContext.getBean("org_sakaiproject_tool_gradebook_business_GradebookManager");
        gradeManager = (GradeManager)applicationContext.getBean(GRADABLE_OBJECT_BEAN);
        gradebookService = (GradebookService)applicationContext.getBean(GRADEBOOK_SERVICE_BEAN);
        archiveService = (GradebookArchiveService)applicationContext.getBean(ARCHIVE_BEAN);
    }

    public void testMarshallAndUnmarshall() throws Exception {
        // Create the xml doc
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        // Create the archive
        Gradebook originalGradebook = gradebookManager.getGradebook(this.getClass().getName());
        String messages = archiveService.createArchive(originalGradebook.getUid(), doc);

        // Create a new gradebook from the archive
        archiveService.createGradebookFromArchive(GRADEBOOK_COPY, doc);
        
        // Make sure the gradebook was created
        Assert.assertTrue(gradebookService.gradebookExists(GRADEBOOK_COPY));
        
        // Make sure its properties were carried over
        Gradebook gradebook = gradebookManager.getGradebook(GRADEBOOK_COPY);
        Assert.assertTrue(originalGradebook.isAssignmentsDisplayed() == gradebook.isAssignmentsDisplayed());
        Assert.assertTrue(originalGradebook.isCourseGradeDisplayed() == gradebook.isCourseGradeDisplayed());
        
        // Make sure its grade mappings were carried over
        GradeMapping originalGradeMapping = originalGradebook.getSelectedGradeMapping();
        GradeMapping gradeMapping = gradebook.getSelectedGradeMapping();
        Assert.assertTrue(gradeMapping.getGradeMap().equals(originalGradeMapping.getGradeMap()));
        
        for(Iterator iter = gradebook.getGradeMappings().iterator(); iter.hasNext();) {
            GradeMapping mapping = (GradeMapping)iter.next();
            for(Iterator gradeIter = mapping.getGrades().iterator(); gradeIter.hasNext();) {
                Assert.assertTrue(mapping.getGradeMap().containsKey(gradeIter.next()));
            }
        }
        
        // Make sure it has the same assignments as the original
        List assignments = gradeManager.getAssignments(gradebook.getId());
        List originalAssignments = gradeManager.getAssignments(originalGradebook.getId());
        
        for(int i = 0; i < assignments.size(); i++) {
            Assignment newAsn = (Assignment)assignments.get(i);
            Assignment copiedAsn = (Assignment)originalAssignments.get(i);
            Assert.assertTrue(newAsn.getName().equals(copiedAsn.getName()));
            Assert.assertTrue(newAsn.getPointsPossible().equals(copiedAsn.getPointsPossible()));
        }
        
    }
}


