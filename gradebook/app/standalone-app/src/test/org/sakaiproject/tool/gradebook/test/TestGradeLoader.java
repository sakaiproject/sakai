/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation, The MIT Corporation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 *
 */
public class TestGradeLoader extends GradebookLoaderBase {

	public void testPopulate() throws Exception {
        // Fetch the first gradebook.
        Gradebook gb = gradebookManager.getGradebook(TestGradebookLoader.GRADEBOOK_WITH_GRADES);

		List enrollments = sectionAwareness.getSiteMembersInRole(gb.getUid(), Role.STUDENT);
		List assignments = gradebookManager.getAssignments(gb.getId());

		for(Iterator asnIter = assignments.iterator(); asnIter.hasNext();) {
			boolean needToAddScrewyExternalScore = true;
			Assignment asn = (Assignment)asnIter.next();

			// Don't add grades for at least one assignment.
			if (asn.getName().equals(TestGradebookLoader.ASN_NO_DUE_DATE_NAME)) {
				continue;
			}

            List gradeRecords = new ArrayList();
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
                    	gradebookExternalAssessmentService.updateExternalAssessmentScore(gb.getUid(), asn.getExternalId(), enr.getUser().getUserUid(), grade.toString());
                    } else {
                    	AssignmentGradeRecord agr = new AssignmentGradeRecord(asn, enr.getUser().getUserUid(), grade);
                        gradeRecords.add(agr);
                    }
                }
			}

            // Save the internal assignment scores
            if(!asn.isExternallyMaintained()) {
            	gradebookManager.updateAssignmentGradeRecords(asn, gradeRecords);
            }
		}
        // Ensure that this is actually saved to the database
        setComplete();
	}
}


