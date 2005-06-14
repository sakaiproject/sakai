/**********************************************************************************
*
* $Header: /cvs/sakai2/gradebook/tool/src/test/org/sakaiproject/tool/gradebook/test/TestAssessmentIntegration.java,v 1.4 2005/05/26 18:04:55 josh.media.berkeley.edu Exp $
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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingExternalIdException;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.AbstractGradeRecord;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.GradableObjectManager;
import org.sakaiproject.tool.gradebook.business.GradeManager;
import org.sakaiproject.tool.gradebook.business.GradebookManager;

/**
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman </a>
 *
 */
public class TestAssessmentIntegration extends SpringEnabledTestCase {
    private static final Log log = LogFactory.getLog(TestAssessmentIntegration.class);

    static String GB_NAME = "Shared w/ Samigo gradebook";
    static String SAMIGO = "Samigo";
    static String INTERNAL_ASN1 = "First Internal Assignment";
    static String INTERNAL_ASN2 = "Second Internal Assignment";
    static String EXTERNAL_ASN1 = "First External Assignment";
    static String EXTERNAL_ASN2 = "Second External Assignment";
    static Date now = new Date();

    static GradebookManager gradebookManager;
    static GradableObjectManager gradableObjectManager;
    static GradeManager gradeManager;
    static GradebookService gradebookService;

    private static Long internalAsn1Id;
    private static Long internalAsn2Id;

    public static Test suite() {
        TestSetup setup = new TestSetup(new TestSuite(TestAssessmentIntegration.class)) {
            protected void setUp() throws Exception {
                oneTimeSetup();
            }
        };
        return setup;
    }

    private static void oneTimeSetup() throws Exception {
        // Force initialization of spring-managed services
        TestAssessmentIntegration tes = new TestAssessmentIntegration();
        tes.setUp();

        // Create a gradebook
        gradebookService.addGradebook(GB_NAME, GB_NAME);

        // Get the gradebook
        Gradebook gb = gradebookManager.getGradebook(GB_NAME);

        // Create internal assignments
        internalAsn1Id = gradableObjectManager.createAssignment(gb.getId(), INTERNAL_ASN1, new Double(10), now);
        internalAsn2Id = gradableObjectManager.createAssignment(gb.getId(), INTERNAL_ASN2, new Double(10), now);

        // Create external assignments
        gradebookService.addExternalAssessment(gb.getUid(), EXTERNAL_ASN1, null, EXTERNAL_ASN1, 10, now, SAMIGO);
        gradebookService.addExternalAssessment(gb.getUid(), EXTERNAL_ASN2, null, EXTERNAL_ASN2, 10, now, SAMIGO);

        // Get an internal assignment
        Assignment asn = null;
        List assignments = gradableObjectManager.getAssignments(gb.getId());
        for(Iterator iter = assignments.iterator(); iter.hasNext();) {
            Assignment tmp = (Assignment)iter.next();
            if(tmp.getName().equals(INTERNAL_ASN1)) {
                asn = tmp;
            }
        }

        // Create grades for the internal assignments
        Map gradeMap = new HashMap();

        gradeMap.put("student1", new Double(10));
        gradeMap.put("student2", new Double(9));
        gradeMap.put("student3", new Double(8));
        gradeMap.put("student4", new Double(7));
        gradeMap.put("student5", new Double(6));

        gradeManager.updateAssignmentGradeRecords(asn.getId(), gradeMap);
    }

    protected void setUp() throws Exception {
        log.info("Attempting to obtain spring-managed services.");
        initialize("components.xml,components-test.xml");
        gradebookService = (GradebookService)getBean(TestManagers.GB_SERVICE);
        gradebookManager = (GradebookManager)getBean(TestManagers.GB_MANAGER);
        gradableObjectManager = (GradableObjectManager)getBean(TestManagers.GO_MANAGER);
        gradeManager = (GradeManager)getBean(TestManagers.GR_MANAGER);
    }

    public void testCreateBadExternalAssignments() throws Exception {
        Gradebook gb = gradebookManager.getGradebook(GB_NAME);

        boolean badGradebookUid = false;
        try {
            gradebookService.addExternalAssessment("badUid", "some id", null, "some title", 10, now, SAMIGO);
        } catch (GradebookNotFoundException e) {
            badGradebookUid = true;
        }
        Assert.assertTrue(badGradebookUid);

        boolean conflictingName = false;
        try {
            gradebookService.addExternalAssessment(gb.getUid(), "some new external id", null, EXTERNAL_ASN1, 10, now, SAMIGO);
        } catch (ConflictingAssignmentNameException e) {
            conflictingName = true;
        }
        Assert.assertTrue(conflictingName);

        boolean conflictingExternalId = false;
        try {
            gradebookService.addExternalAssessment(gb.getUid(), EXTERNAL_ASN1, null, "some new name", 10, now, SAMIGO);
        } catch (ConflictingExternalIdException e) {
            conflictingExternalId = true;
        }
        Assert.assertTrue(conflictingExternalId);
    }

    public void testModifyExternalAssessment() throws Exception {
        // Check whether the gradebook exists
        Assert.assertTrue(gradebookService.gradebookExists(GB_NAME));
        Assert.assertTrue(!gradebookService.gradebookExists("No such gradebook"));

        Gradebook gb = gradebookManager.getGradebook(GB_NAME);
        gradebookService.updateExternalAssessment(gb.getUid(), EXTERNAL_ASN1, null, EXTERNAL_ASN1, 20, now);

        // Find the assessment and ensure that it has been updated
        Assignment asn = null;
        List assignments = gradableObjectManager.getAssignments(gb.getId());
        for(Iterator iter = assignments.iterator(); iter.hasNext();) {
            Assignment tmp = (Assignment)iter.next();
            if(tmp.getExternalId() != null && tmp.getExternalId().equals(EXTERNAL_ASN1)) {
                asn = tmp;
            }
        }
        Assert.assertEquals(asn.getPointsPossible(), new Double(20));

        // Ensure that the total points possible in the gradebook reflects the updated assessment's points
        Assert.assertTrue(gradableObjectManager.getTotalPoints(gb.getId()) == 50);
    }

    public void testCreateExternalGradeRecords() throws Exception {
        Gradebook gb = gradebookManager.getGradebook(GB_NAME);
        gradebookService.updateExternalAssessmentScore(gb.getUid(), EXTERNAL_ASN1, "student1", new Double(5));

        // Ensure that the course grade record for student1 has been updated
        List grades = gradeManager.getStudentGradeRecords(gb.getId(), "student1");
        for(Iterator iter = grades.iterator(); iter.hasNext();) {
            AbstractGradeRecord agr = (AbstractGradeRecord)iter.next();
            if(agr.isCourseGradeRecord()) {
                Assert.assertTrue(agr.getPointsEarned().equals(new Double(15))); // 10 points on internal, 5 points on external
            }
        }
    }

    public void testModifyExternalGradeRecords() throws Exception {
        Gradebook gb = gradebookManager.getGradebook(GB_NAME);
        gradebookService.updateExternalAssessmentScore(gb.getUid(), EXTERNAL_ASN1, "student1", new Double(15));

        // Ensure that the course grade record for student1 has been updated
        List grades = gradeManager.getStudentGradeRecords(gb.getId(), "student1");
        for(Iterator iter = grades.iterator(); iter.hasNext();) {
            AbstractGradeRecord agr = (AbstractGradeRecord)iter.next();
            if(agr.isCourseGradeRecord()) {
                Assert.assertTrue(agr.getPointsEarned().equals(new Double(25))); // 10 points on internal, 15 points on external
            }
        }
    }

    public void testRemoveExternalAssignment() throws Exception {
        Gradebook gb = gradebookManager.getGradebook(GB_NAME);
        gradebookService.removeExternalAssessment(gb.getUid(), EXTERNAL_ASN1);

        // Ensure that the course grade record for student1 has been updated
        List grades = gradeManager.getStudentGradeRecords(gb.getId(), "student1");
        for(Iterator iter = grades.iterator(); iter.hasNext();) {
            AbstractGradeRecord agr = (AbstractGradeRecord)iter.next();
            if(agr.isCourseGradeRecord()) {
                Assert.assertTrue(agr.getPointsEarned().equals(new Double(10)));// 10 points on internal, 0 points on external
            }
        }
    }
}
/**************************************************************************************************************************************************************************************************************************************************************
 * $Header: /cvs/sakai2/gradebook/tool/src/test/org/sakaiproject/tool/gradebook/test/TestAssessmentIntegration.java,v 1.4 2005/05/26 18:04:55 josh.media.berkeley.edu Exp $
 *************************************************************************************************************************************************************************************************************************************************************/
