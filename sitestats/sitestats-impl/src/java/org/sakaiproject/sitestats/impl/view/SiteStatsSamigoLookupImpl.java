/**********************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.sitestats.impl.view;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.tool.assessment.data.dao.assessment.ExtendedTime;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;

@Slf4j
public class SiteStatsSamigoLookupImpl implements SiteStatsSamigoLookup {

	@Override
	public List<SiteStatsSamigoQuiz> publishedQuizzes(String siteId) {
		if (StringUtils.isBlank(siteId)) {
			return Collections.emptyList();
		}
		try {
			PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
			List<PublishedAssessmentData> published = publishedAssessmentService.getAllPublishedAssessmentsForSite(siteId);
			if (published == null || published.isEmpty()) {
				return Collections.emptyList();
			}
			List<SiteStatsSamigoQuiz> quizzes = new ArrayList<SiteStatsSamigoQuiz>();
			for (PublishedAssessmentData assessment : published) {
				if (assessment == null || assessment.getPublishedAssessmentId() == null) {
					continue;
				}
				boolean acceptLate = acceptLate(assessment);
				Instant due = dueDate(assessment);
				Instant retract = toInstant(accessDate(assessment, false));
				quizzes.add(new SiteStatsSamigoQuiz(String.valueOf(assessment.getPublishedAssessmentId()),
						StringUtils.defaultIfBlank(assessment.getTitle(), String.valueOf(assessment.getPublishedAssessmentId())),
						due, SiteStatsSamigoQuiz.closeDate(due, retract, acceptLate), releaseGroupIds(assessment),
						requiresManualGrading(publishedAssessmentService, assessment.getPublishedAssessmentId()),
						acceptLate, dateOverrides(assessment)));
			}
			return quizzes;
		} catch (RuntimeException e) {
			log.warn("Unable to load published assessments for site {}", siteId, e);
			return Collections.emptyList();
		}
	}

	@Override
	public List<SiteStatsSamigoAttempt> submittedAttempts(String publishedAssessmentId) {
		if (StringUtils.isBlank(publishedAssessmentId)) {
			return Collections.emptyList();
		}
		try {
			Long publishedId = Long.valueOf(publishedAssessmentId);
			GradingService gradingService = new GradingService();
			List<?> attempts = gradingService.getLastSubmittedAssessmentGradingList(publishedId);
			if (attempts == null || attempts.isEmpty()) {
				return Collections.emptyList();
			}
			List<SiteStatsSamigoAttempt> submitted = new ArrayList<SiteStatsSamigoAttempt>();
			for (Object attempt : attempts) {
				if (!(attempt instanceof AssessmentGradingData)) {
					continue;
				}
				AssessmentGradingData grading = (AssessmentGradingData) attempt;
				boolean forGrade = Boolean.TRUE.equals(grading.getForGrade());
				submitted.add(new SiteStatsSamigoAttempt(grading.getAgentId(), toInstant(grading.getSubmittedDate()),
						forGrade, Boolean.TRUE.equals(grading.getIsLate()), grading.getGradedDate() != null));
			}
			return submitted;
		} catch (RuntimeException e) {
			log.warn("Unable to load quiz submissions for publishedAssessmentId={}", publishedAssessmentId, e);
			return Collections.emptyList();
		}
	}

	private boolean requiresManualGrading(PublishedAssessmentService publishedAssessmentService, Long publishedAssessmentId) {
		try {
			Set<PublishedSectionData> sections = publishedAssessmentService.getSectionSetForAssessment(publishedAssessmentId);
			if (sections == null || sections.isEmpty()) {
				return false;
			}
			for (PublishedSectionData section : sections) {
				if (section == null || section.getItemSet() == null) {
					continue;
				}
				for (Object itemObj : section.getItemSet()) {
					if (!(itemObj instanceof PublishedItemData)) {
						continue;
					}
					Long typeId = ((PublishedItemData) itemObj).getTypeId();
					if (TypeIfc.ESSAY_QUESTION.equals(typeId) || TypeIfc.AUDIO_RECORDING.equals(typeId)
							|| TypeIfc.FILE_UPLOAD.equals(typeId)) {
						return true;
					}
				}
			}
		} catch (RuntimeException e) {
			log.warn("Unable to inspect quiz question types for publishedAssessmentId={}", publishedAssessmentId, e);
		}
		return false;
	}

	private List<String> releaseGroupIds(PublishedAssessmentData assessment) {
		if (assessment == null || assessment.getAssessmentAccessControl() == null) {
			return Collections.emptyList();
		}
		String releaseTo = assessment.getAssessmentAccessControl().getReleaseTo();
		if (!AssessmentAccessControlIfc.RELEASE_TO_SELECTED_GROUPS.equals(releaseTo)) {
			return Collections.emptyList();
		}
		try {
			List<String> groupIds = PersistenceService.getInstance().getPublishedAssessmentFacadeQueries()
					.getReleaseToGroupIdsForPublishedAssessment(String.valueOf(assessment.getPublishedAssessmentId()));
			return groupIds == null ? Collections.emptyList() : groupIds;
		} catch (RuntimeException e) {
			log.warn("Unable to load quiz release groups for publishedAssessmentId={}",
					assessment.getPublishedAssessmentId(), e);
			return Collections.emptyList();
		}
	}

	private List<SiteStatsSamigoDateOverride> dateOverrides(PublishedAssessmentData assessment) {
		try {
			List<ExtendedTime> entries = PersistenceService.getInstance().getExtendedTimeFacade()
					.getEntriesForPub(assessment);
			if (entries == null || entries.isEmpty()) {
				return Collections.emptyList();
			}
			AssessmentAccessControlIfc access = assessment.getAssessmentAccessControl();
			List<SiteStatsSamigoDateOverride> overrides = new ArrayList<SiteStatsSamigoDateOverride>();
			for (ExtendedTime entry : entries) {
				if (entry == null) {
					continue;
				}
				ExtendedTime synchronizedEntry = new ExtendedTime(entry);
				synchronizedEntry.syncDates(access);
				overrides.add(new SiteStatsSamigoDateOverride(StringUtils.trimToNull(synchronizedEntry.getUser()),
						StringUtils.trimToNull(synchronizedEntry.getGroup()), toInstant(synchronizedEntry.getDueDate()),
						toInstant(synchronizedEntry.getRetractDate())));
			}
			return overrides;
		} catch (RuntimeException e) {
			log.warn("Unable to load quiz date exceptions for publishedAssessmentId={}",
					assessment.getPublishedAssessmentId(), e);
			return Collections.emptyList();
		}
	}

	private boolean acceptLate(PublishedAssessmentData assessment) {
		return assessment.getAssessmentAccessControl() != null
				&& AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION.equals(
						assessment.getAssessmentAccessControl().getLateHandling());
	}

	private Instant dueDate(PublishedAssessmentData assessment) {
		return toInstant(accessDate(assessment, true));
	}

	private Date accessDate(PublishedAssessmentData assessment, boolean due) {
		Date value = due ? assessment.getDueDate() : assessment.getRetractDate();
		if (value != null || assessment.getAssessmentAccessControl() == null) {
			return value;
		}
		return due ? assessment.getAssessmentAccessControl().getDueDate()
				: assessment.getAssessmentAccessControl().getRetractDate();
	}

	private Instant toInstant(Date date) {
		return date == null ? null : date.toInstant();
	}
}
