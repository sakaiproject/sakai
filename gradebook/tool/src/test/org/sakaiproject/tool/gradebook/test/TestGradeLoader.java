/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;
import org.sakaiproject.api.section.facade.Role;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.GradeRecordSet;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 *
 */
public class TestGradeLoader extends GradebookLoaderBase {

	public void testPopulate() throws Exception {
        List gradebooks = new ArrayList();

        // Fetch the first gradebook.
        Gradebook gb = gradebookManager.getGradebook(TestGradebookLoader.GRADEBOOK_WITH_GRADES);

		List enrollments = sectionAwareness.getSiteMembersInRole(gb.getUid(), Role.STUDENT);
		List assignments = gradeManager.getAssignments(gb.getId());

		for(Iterator asnIter = assignments.iterator(); asnIter.hasNext();) {
			boolean needToAddScrewyExternalScore = true;
			Assignment asn = (Assignment)asnIter.next();

			// Don't add grades for at least one assignment.
			if (asn.getName().equals(TestGradebookLoader.ASN_NO_DUE_DATE_NAME)) {
				continue;
			}

            GradeRecordSet gradeRecordSet = new GradeRecordSet(asn);
			for(Iterator enrIter = enrollments.iterator(); enrIter.hasNext();) {
				EnrollmentRecord enr = (EnrollmentRecord)enrIter.next();
                // Don't add grades for those no good lazy students
                if(!enr.getUser().getUserUid().equals(StandaloneSectionsDataLoader.AUTHID_WITHOUT_GRADES_1) &&
                        !enr.getUser().getUserUid().equals(StandaloneSectionsDataLoader.AUTHID_WITHOUT_GRADES_2)) {
                    Double grade = new Double(Math.ceil(asn.getPointsPossible().doubleValue() * Math.random()));
                    if(asn.isExternallyMaintained()) {
                    	if (needToAddScrewyExternalScore) {
                    		grade = new Double(grade.doubleValue() + 0.39981);
                    		needToAddScrewyExternalScore = false;
                    	}
                        gradebookService.updateExternalAssessmentScore(gb.getUid(), asn.getExternalId(), enr.getUser().getUserUid(), grade);
                    } else {
                    	AssignmentGradeRecord agr = new AssignmentGradeRecord(asn, enr.getUser().getUserUid(), grade);
                        gradeRecordSet.addGradeRecord(agr);
                    }
                }
			}

            // Save the internal assignment scores
            if(!asn.isExternallyMaintained()) {
            	gradeManager.updateAssignmentGradeRecords(gradeRecordSet);
            }
		}
        // Ensure that this is actually saved to the database
        setComplete();
	}
}


