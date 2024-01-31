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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import lombok.Setter;

import org.sakaiproject.grading.api.ConflictingAssignmentNameException;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.lessonbuildertool.api.LessonBuilderConstants;

/**
 * Interface to Gradebook
 *
 * @author Charles Hedrick <hedrick@rutgers.edu>
 * 
 */
@Slf4j
public class GradebookIfc {
	@Setter private static GradingService gradingService;

	public boolean addExternalAssessment(final List<String> gradebookUids, final String siteId, final String externalId, final String externalUrl,
					 final String title, final double points, final Date dueDate, final String externalServiceDescription) {
		try {
			for (String gradebookUid : gradebookUids) {
				gradingService.addExternalAssessment(gradebookUid, siteId, externalId, externalUrl, title, points, dueDate, externalServiceDescription, null, null, null, null);
			}
		} catch (ConflictingAssignmentNameException cane) {
			// already exists
			log.warn("ConflictingAssignmentNameException for title {} : {} ", title, cane.getMessage());
			throw cane;
		} catch (Exception e) {
			log.info("failed add " + e);
			return false;
		}
		return true;
	}

	public boolean updateExternalAssessment(final List<String> gradebookUids, final String externalId, final String externalUrl,
						final String title, final double points, final Date dueDate) {
		try {
			for (String gradebookUid : gradebookUids) {
				gradingService.updateExternalAssessment(gradebookUid, externalId, externalUrl, null, title, null, points, dueDate, null);
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public boolean removeExternalAssessment(final String gradebookUid, final String externalId) {
		try {
			gradingService.removeExternalAssignment(null, externalId, LessonBuilderConstants.TOOL_ID);
		} catch (Exception e) {
			log.info("failed remove " + e);
			return false;
		}
		return true;
	}

	public boolean updateExternalAssessmentScore(final String gradebookUid, final String siteId, final String externalId,
						 final String studentUid, final String points) {
		try {
			gradingService.updateExternalAssessmentScore(gradebookUid, siteId, externalId, studentUid, points);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public boolean updateExternalAssessmentScores(final String gradebookUid, final String siteId, final String externalId, final Map studentUidsToScores) {
		try {
			gradingService.updateExternalAssessmentScoresString(gradebookUid, siteId, externalId, studentUidsToScores);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public boolean isGradebookGroupEnabled(final String siteId) {
		return gradingService.isGradebookGroupEnabled(siteId);
	}

	public List<String> getGradebookGroupInstances(String siteId) {
		return gradingService.getGradebookGroupInstances(siteId).stream()
				.map(Gradebook::getUid)
				.collect(Collectors.toList());
	}
}
