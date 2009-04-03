/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 * This odd-looking JUnit class provides a very primitive test for data
 * contention when using the external service API. You'll see what look like
 * the same "tests" several times in a row. The idea is to run the test from
 * multiple windows simultaneously and try for locking exceptions.
 *
 * Pretty cheap stuff, but it took less time to get going than trying to
 * coordinate one of the JUnit multi-threading add-on projects with
 * Spring and Hivernate....
 *
 * Sample usage:
 *
 *   # Build standalone and load up the test database.
 *   cd ../sections/
 *   maven -Dmode=standalone -Dhibernate.properties.dir=C:/java/sakaisettings/mysql-standalone cln bld
 *   cd ../gradebook
 *   maven -Dhibernate.properties.dir=C:/java/sakaisettings/mysql-standalone cln bld
 *   maven -Dhibernate.properties.dir=C:/java/sakaisettings/mysql-standalone load-full-standalone
 *
 *   # Then do this from as many windows as you feel up to.
 *   cd app/standalone-app/
 *   maven -Dhibernate.properties.dir=C:/java/sakaisettings/mysql-standalone test-against-loaded-data
 */
public class TestServiceAgainstLoadedData extends GradebookLoaderBase {
    private static final Log log = LogFactory.getLog(TestServiceAgainstLoadedData.class);

	public TestServiceAgainstLoadedData() {
    	// Don't roll these tests back, since they are intended to act like real-world users.
		setDefaultRollback(false);
	}

    private Assignment getAssignment(Gradebook gradebook, String assignmentName) {
		List assignments = gradebookManager.getAssignments(gradebook.getId());
		for (Iterator iter = assignments.iterator(); iter.hasNext();) {
			Assignment asn = (Assignment)iter.next();
			if (asn.getName().equals(assignmentName)) {
				return asn;
			}
		}
		return null;
    }

	public void testUpdateExternalScores() throws Exception {
        Gradebook gradebook = gradebookManager.getGradebook(TestGradebookLoader.GRADEBOOK_WITH_GRADES);
		List enrollments = sectionAwareness.getSiteMembersInRole(gradebook.getUid(), Role.STUDENT);

		Assignment asn = getAssignment(gradebook, TestGradebookLoader.EXTERNAL_ASN_NAME1);

		Map studentUidsToScores = new HashMap();
		int scoreGoRound = -1;
		for (Iterator iter = enrollments.iterator(); iter.hasNext(); ) {
			EnrollmentRecord enr = (EnrollmentRecord)iter.next();
			String score = (scoreGoRound == -1) ? null : new Integer(scoreGoRound).toString();
			scoreGoRound = (scoreGoRound < 11) ? (scoreGoRound + 1) : -1;
			studentUidsToScores.put(enr.getUser().getUserUid(), score);
		}
		log.warn("about to updateExternalAssessmentScores with " + enrollments.size() + " scores for " + asn.getExternalId());
		gradebookExternalAssessmentService.updateExternalAssessmentScoresString(gradebook.getUid(), asn.getExternalId(), studentUidsToScores);
	}

	public void testUpdateExternalScore() throws Exception {
        Gradebook gradebook = gradebookManager.getGradebook(TestGradebookLoader.GRADEBOOK_WITH_GRADES);
		List enrollments = sectionAwareness.getSiteMembersInRole(gradebook.getUid(), Role.STUDENT);

		Assignment asn = getAssignment(gradebook, TestGradebookLoader.EXTERNAL_ASN_NAME2);

		int scoreGoRound = 2;
		for (Iterator iter = enrollments.iterator(); iter.hasNext(); ) {
			EnrollmentRecord enr = (EnrollmentRecord)iter.next();
			Double score = (scoreGoRound == -1) ? null : new Double(scoreGoRound);
			scoreGoRound = (scoreGoRound < 11) ? (scoreGoRound + 1) : -1;
			gradebookExternalAssessmentService.updateExternalAssessmentScore(gradebook.getUid(), asn.getExternalId(), enr.getUser().getUserUid(), score.toString());
		}
	}

	public void testUpdateExternalScores2() throws Exception {
        Gradebook gradebook = gradebookManager.getGradebook(TestGradebookLoader.GRADEBOOK_WITH_GRADES);
		List enrollments = sectionAwareness.getSiteMembersInRole(gradebook.getUid(), Role.STUDENT);

		Assignment asn = getAssignment(gradebook, TestGradebookLoader.EXTERNAL_ASN_NAME2);

		Map studentUidsToScores = new HashMap();
		int scoreGoRound = -1;
		for (Iterator iter = enrollments.iterator(); iter.hasNext(); ) {
			EnrollmentRecord enr = (EnrollmentRecord)iter.next();
			String score = (scoreGoRound == -1) ? null : new Double(scoreGoRound).toString();
			scoreGoRound = (scoreGoRound < 11) ? (scoreGoRound + 1) : -1;
			studentUidsToScores.put(enr.getUser().getUserUid(), score);
		}
		log.warn("about to updateExternalAssessmentScores with " + enrollments.size() + " scores for " + asn.getExternalId());
		gradebookExternalAssessmentService.updateExternalAssessmentScoresString(gradebook.getUid(), asn.getExternalId(), studentUidsToScores);
	}

	public void testUpdateExternalScore2() throws Exception {
        Gradebook gradebook = gradebookManager.getGradebook(TestGradebookLoader.GRADEBOOK_WITH_GRADES);
		List enrollments = sectionAwareness.getSiteMembersInRole(gradebook.getUid(), Role.STUDENT);

		Assignment asn = getAssignment(gradebook, TestGradebookLoader.EXTERNAL_ASN_NAME1);

		int scoreGoRound = 2;
		for (Iterator iter = enrollments.iterator(); iter.hasNext(); ) {
			EnrollmentRecord enr = (EnrollmentRecord)iter.next();
			Double score = (scoreGoRound == -1) ? null : new Double(scoreGoRound);
			scoreGoRound = (scoreGoRound < 11) ? (scoreGoRound + 1) : -1;
			gradebookExternalAssessmentService.updateExternalAssessmentScore(gradebook.getUid(), asn.getExternalId(), enr.getUser().getUserUid(), score.toString());
		}
	}

	public void testUpdateExternalScores2Same() throws Exception {
        Gradebook gradebook = gradebookManager.getGradebook(TestGradebookLoader.GRADEBOOK_WITH_GRADES);
		List enrollments = sectionAwareness.getSiteMembersInRole(gradebook.getUid(), Role.STUDENT);

		Assignment asn = getAssignment(gradebook, TestGradebookLoader.EXTERNAL_ASN_NAME2);

		Map studentUidsToScores = new HashMap();
		int scoreGoRound = -1;
		for (Iterator iter = enrollments.iterator(); iter.hasNext(); ) {
			EnrollmentRecord enr = (EnrollmentRecord)iter.next();
			String score = (scoreGoRound == -1) ? null : new Double(scoreGoRound).toString();
			scoreGoRound = (scoreGoRound < 11) ? (scoreGoRound + 1) : -1;
			studentUidsToScores.put(enr.getUser().getUserUid(), score);
		}
		log.warn("about to updateExternalAssessmentScores with " + enrollments.size() + " scores for " + asn.getExternalId());
		gradebookExternalAssessmentService.updateExternalAssessmentScoresString(gradebook.getUid(), asn.getExternalId(), studentUidsToScores);
	}
}


