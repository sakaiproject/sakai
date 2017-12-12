/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Charles Hedrick, hedrick@rutgers.edu
 *
 * Copyright (c) 2011 Rutgers, the State University of New Jersey
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");                                                                
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.lessonbuildertool.service;

import java.util.Date;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;

/**
 * Interface to Gradebook
 *
 * @author Charles Hedrick <hedrick@rutgers.edu>
 * 
 */
@Slf4j
public class GradebookIfc {
    private static GradebookExternalAssessmentService gbExternalService = null;

    public void setGradebookExternalAssessmentService (GradebookExternalAssessmentService s) {
	gbExternalService = s;
    }

    public boolean addExternalAssessment(final String gradebookUid, final String externalId, final String externalUrl,
					 final String title, final double points, final Date dueDate, final String externalServiceDescription) {
	try {
	    gbExternalService.addExternalAssessment(gradebookUid, externalId, externalUrl, title, points, dueDate, externalServiceDescription);
	} catch (org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException e) {
	    // already exists. Say it worked
	    return true;
	} catch (Exception e) {
	    log.info("failed add " + e);
	    return false;
	}
	return true;
    }

    public boolean updateExternalAssessment(final String gradebookUid, final String externalId, final String externalUrl,
					    final String title, final double points, final Date dueDate) {
	try {
	    gbExternalService.updateExternalAssessment(gradebookUid, externalId, externalUrl, title, points, dueDate);
	} catch (Exception e) {
	    return false;
	}
	return true;
    }



    public boolean removeExternalAssessment(final String gradebookUid, final String externalId) {
	try {
	    gbExternalService.removeExternalAssessment(gradebookUid, externalId);
	} catch (Exception e) {
	    log.info("failed remove " + e);
	    return false;
	}
	return true;
    }

    public boolean updateExternalAssessmentScore(final String gradebookUid, final String externalId,
						 final String studentUid, final String points) {
	try {
	    gbExternalService.updateExternalAssessmentScore(gradebookUid, externalId, studentUid, points);
	} catch (Exception e) {
	    return false;
	}
	return true;
    }

    // map is String studentid to Double points
    public boolean updateExternalAssessmentScores(final String gradebookUid, final String externalId, final Map studentUidsToScores) {
	
	try {
	    gbExternalService.updateExternalAssessmentScoresString(gradebookUid, externalId, studentUidsToScores);
	} catch (Exception e) {
	    return false;
	}
	return true;
    }

}
