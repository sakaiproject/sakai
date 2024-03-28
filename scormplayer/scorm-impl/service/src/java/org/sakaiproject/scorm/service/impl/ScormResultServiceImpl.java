/**
 * Copyright (c) 2007 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.scorm.service.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.adl.api.ecmascript.APIErrorCodes;
import org.adl.datamodels.DMInterface;
import org.adl.datamodels.DMProcessingInfo;
import org.adl.datamodels.IDataManager;

import static org.sakaiproject.scorm.api.ScormConstants.*;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.scorm.dao.LearnerDao;
import org.sakaiproject.scorm.dao.api.AttemptDao;
import org.sakaiproject.scorm.dao.api.DataManagerDao;
import org.sakaiproject.scorm.model.api.ActivityReport;
import org.sakaiproject.scorm.model.api.ActivitySummary;
import org.sakaiproject.scorm.model.api.Attempt;
import org.sakaiproject.scorm.model.api.CMIData;
import org.sakaiproject.scorm.model.api.CMIField;
import org.sakaiproject.scorm.model.api.CMIFieldGroup;
import org.sakaiproject.scorm.model.api.Interaction;
import org.sakaiproject.scorm.model.api.Learner;
import org.sakaiproject.scorm.model.api.LearnerExperience;
import org.sakaiproject.scorm.model.api.Objective;
import org.sakaiproject.scorm.model.api.Progress;
import org.sakaiproject.scorm.model.api.Score;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormResultService;

@Slf4j
public abstract class ScormResultServiceImpl implements ScormResultService
{
	private static String[] fields = { CMI_COMPLETION_STATUS, CMI_SCORE_SCALED, CMI_SUCCESS_STATUS };

	// Daos (also depedency injected)
	protected abstract AttemptDao attemptDao();
	protected abstract DataManagerDao dataManagerDao();
	protected abstract LearnerDao learnerDao();

	// Dependency injection method lookup signatures
	protected abstract LearningManagementSystem lms();

	@Override
	public boolean existsActivityReport(long contentPackageId, String learnerId, long attemptNumber, String scoId)
	{
		IDataManager dataManager = dataManagerDao().find(contentPackageId, learnerId, attemptNumber, scoId);
		return dataManager != null;
	}

	@Override
	public ActivityReport getActivityReport(long contentPackageId, String learnerId, long attemptNumber, String scoId)
	{
		IDataManager dataManager = dataManagerDao().find(contentPackageId, learnerId, attemptNumber, scoId);
		if (dataManager == null)
		{
			return null;
		}

		ActivityReport report = new ActivityReport();
		getCMIDataMap(dataManager);

		report.setTitle(dataManager.getTitle());
		report.setScoId(dataManager.getScoId());
		report.setCmiData(getCMIData(dataManager));

		mapValues(report, dataManager, contentPackageId, learnerId, attemptNumber);

		return report;
	}

	public ActivityReport getActivityReport(long contentPackageId, String activityId, String learnerId, long attemptNumber)
	{
		ActivityReport report = new ActivityReport();

		IDataManager dataManager = dataManagerDao().findByActivityId(contentPackageId, activityId, learnerId, attemptNumber);

		report.setActivityId(activityId);
		report.setTitle(dataManager.getTitle());
		report.setScoId(dataManager.getScoId());
		report.setCmiData(getCMIData(dataManager));

		mapValues(report, dataManager, contentPackageId, learnerId, attemptNumber);

		return report;
	}

	public List<ActivityReport> getActivityReports(long contentPackageId, String learnerId, long attemptNumber)
	{
		List<IDataManager> dataManagers = dataManagerDao().find(contentPackageId, learnerId, attemptNumber);
		List<ActivityReport> reports = new LinkedList<>();

		for (IDataManager dataManager : dataManagers)
		{
			ActivityReport report = new ActivityReport();

			report.setTitle(dataManager.getTitle());
			report.setScoId(dataManager.getScoId());
			report.setCmiData(getCMIData(dataManager));

			mapValues(report, dataManager, contentPackageId, learnerId, attemptNumber);

			reports.add(report);
		}

		return reports;
	}

	@Override
	public List<ActivitySummary> getActivitySummaries(long contentPackageId, String learnerId, long attemptNumber)
	{
		List<IDataManager> dataManagers = dataManagerDao().find(contentPackageId, learnerId, attemptNumber);
		List<ActivitySummary> summaries = new LinkedList<>();

		for (IDataManager dataManager : dataManagers)
		{
			ActivitySummary summary = new ActivitySummary();

			summary.setContentPackageId(contentPackageId);
			summary.setLearnerId(learnerId);
			summary.setTitle(dataManager.getTitle());
			summary.setScoId(dataManager.getScoId());
			summary.setAttemptNumber(attemptNumber);

			mapValues(summary, dataManager);

			summaries.add(summary);
		}

		return summaries;
	}

	@Override
	public Attempt getAttempt(long id)
	{
		return attemptDao().load(id);
	}

	@Override
	public Attempt getAttempt(long contentPackageId, String learnerId, long attemptNumber)
	{
		return attemptDao().lookup(contentPackageId, learnerId, attemptNumber);
	}

	public CMIFieldGroup getAttemptResults(Attempt attempt)
	{

		IDataManager dataManager = dataManagerDao().find(attempt.getCourseId(), "SCO_ID", attempt.getLearnerId(), attempt.getAttemptNumber());
		CMIFieldGroup group = getDefaultFieldGroup();
		List<CMIField> list = group.getList();

		for (CMIField field : list)
		{
			populateValues(field, dataManager);
		}

		return group;
	}

	@Override
	public List<Attempt> getAttempts(long contentPackageId)
	{
		return attemptDao().find(contentPackageId);
	}

	@Override
	public List<Attempt> getAttempts(long contentPackageId, String learnerId)
	{
		return attemptDao().find(contentPackageId, learnerId);
	}

	@Override
	public Attempt getNewstAttempt(long contentPackageId, String learnerId)
	{
		return attemptDao().lookupNewest(contentPackageId, learnerId);
	}

	@Override
	public List<Attempt> getAttempts(String courseId, String learnerId)
	{
		return attemptDao().find(courseId, learnerId);
	}

	private List<CMIData> getCMIData(IDataManager dataManager)
	{
		List<CMIData> cmiData = new LinkedList<>();
		CMIFieldGroup group = getDefaultFieldGroup();
		List<CMIField> list = group.getList();

		for (CMIField field : list)
		{
			cmiData.addAll(populateData(field, dataManager));
		}

		return cmiData;
	}

	private Map<String, CMIData> getCMIDataMap(IDataManager dataManager)
	{
		List<CMIData> cmiData = new LinkedList<>();
		CMIFieldGroup group = getDefaultFieldGroup();
		List<CMIField> list = group.getList();

		for (CMIField field : list)
		{
			cmiData.addAll(populateData(field, dataManager));
		}

		Map<String, CMIData> map = new HashMap<>();
		for (CMIData item : cmiData)
		{
			map.put(item.getFieldName(), item);
		}

		return map;
	}

	private int getCount(String fieldName, IDataManager dataManager)
	{

		String countFieldName = new StringBuilder(fieldName).append("._count").toString();
		String strValue = getValue(countFieldName, dataManager);
		int count = 0;

		if (strValue != null && !strValue.isEmpty())
		{
			try
			{
				count = Integer.parseInt(strValue);
			}
			catch (NumberFormatException nfe)
			{
				log.warn("Count field name {} retrieved a non-numeric result from the data manager", countFieldName, nfe);
			}
		}

		return count;
	}

	private CMIFieldGroup getDefaultFieldGroup()
	{
		CMIFieldGroup group = new CMIFieldGroup();

		List<CMIField> list = group.getList();
		list.add(new CMIField(CMI_COMPLETION_STATUS, "Completion Status"));
		list.add(new CMIField(CMI_COMPLETION_THRESHOLD, "Completion Threshold"));
		list.add(new CMIField(CMI_CREDIT, "Will credit be given?"));
		list.add(new CMIField(CMI_ENTRY, "Has user previously accessed this sco?"));
		list.add(new CMIField(CMI_EXIT, "Exit Type"));

		// Interactions?
		list.add(new CMIField(CMI_LAUNCH_DATA, "Initialization launch data"));
		list.add(new CMIField(CMI_LEARNER_ID, "Learner Id"));
		list.add(new CMIField(CMI_LEARNER_NAME, "Learner Name"));

		// Learner preferences?
		list.add(new CMIField(CMI_LOCATION, "Current location in the sco"));
		list.add(new CMIField(CMI_MAX_TIME_ALLOWED, "Maximum time allowed to use sco"));
		list.add(new CMIField(CMI_MODE, "Mode"));
		list.add(new CMIField(CMI_PROGRESS_MEASURE, "Progress Measure"));
		list.add(new CMIField(CMI_SCALED_PASSING_SCORE, "Scaled passing score required"));
		list.add(new CMIField(CMI_SCORE_SCALED, "Overall Scaled Score"));
		list.add(new CMIField(CMI_SCORE_RAW, "Overall Raw Score"));
		list.add(new CMIField(CMI_SCORE_MIN, "Minimum Raw Score"));
		list.add(new CMIField(CMI_SCORE_MAX, "Maximum Raw Score"));
		list.add(new CMIField(CMI_SESSION_TIME, "Current learner session time"));
		list.add(new CMIField(CMI_SUCCESS_STATUS, "Success Status"));
		list.add(new CMIField(CMI_SUSPEND_DATA, "Suspend data"));
		list.add(new CMIField(CMI_TIME_LIMIT_ACTION, "Action when time expires"));
		list.add(new CMIField(CMI_TOTAL_TIME, "Sum of learner's session times in attempt"));

		list.add(new CMIField(CMI_TIMESTAMP, "Timestamp"));
		list.add(new CMIField(CMI_COMMENTS_FROM_LEARNER, "Comments from Learner"));
		list.add(new CMIField(CMI_OBJECTIVES_COUNT, "Objective count"));
		list.add(new CMIField(CMI_INTERACTIONS_COUNT, "Interaction count"));

		CMIField objectives = new CMIField("cmi.objectives", "Objectives");
		objectives.addChild(new CMIField("id", "Objective Id"));

		objectives.addChild(new CMIField("score.scaled", "Scaled Score"));
		list.add(objectives);
		return group;
	}

	@Override
	public Interaction getInteraction(long contentPackageId, String learnerId, long attemptNumber, String scoId, String interactionId)
	{
		IDataManager dataManager = dataManagerDao().find(contentPackageId, learnerId, attemptNumber, scoId);
		List<Interaction> interactions = new LinkedList<>();
		mapInteractions(interactions, dataManager, contentPackageId, learnerId, attemptNumber, false);
		Interaction interaction = null;

		for (Interaction current : interactions)
		{
			if (current.getInteractionId().equals(interactionId))
			{
				interaction = current;
				break;
			}
		}

		if (interaction != null && interaction.getObjectiveIds().size() > 0)
		{
			Map<String, Objective> objectivesMap = new HashMap<>();
			mapObjectives(objectivesMap, dataManager, contentPackageId, learnerId, attemptNumber);

			List<Objective> objectivesList = interaction.getObjectives();
			for (String objectiveId : interaction.getObjectiveIds())
			{
				Objective objective = objectivesMap.get(objectiveId);
				if (objective != null)
				{
					objectivesList.add(objective);
				}
			}
		}

		return interaction;
	}

	@Override
	public List<LearnerExperience> getLearnerExperiences(long contentPackageId)
	{
		List<LearnerExperience> experiences = new LinkedList<>();
		String context = lms().currentContext();
		List<Learner> learners = learnerDao().find(context);

		for (int i = 0; i < learners.size(); i++)
		{
			Learner learner = learners.get(i);
			LearnerExperience experience = new LearnerExperience(learner, contentPackageId);
			List<Attempt> attempts = getAttempts(contentPackageId, learner.getId());
			int status = NOT_ACCESSED;

			if (attempts != null)
			{
				if (attempts.size() > 0)
				{
					// Grab the latest attempt
					Attempt latestAttempt = attempts.get(0);

					//List<CMIData> data = getSummaryCMIData(latestAttempt);
					experience.setLastAttemptDate(latestAttempt.getBeginDate());
					status = COMPLETED;
				}

				experience.setNumberOfAttempts(attempts.size());
			}

			experience.setStatus(status);
			experiences.add(experience);
		}

		return experiences;
	}

	@Override
	public int getNumberOfAttempts(long contentPackageId, String learnerId)
	{
		return attemptDao().count(contentPackageId, learnerId);
	}

	private double getRealValue(String element, IDataManager dataManager)
	{
		String result = getValue(element, dataManager);
		if (StringUtils.isBlank(result) || result.equals("unknown"))
		{
			return -1.0;
		}

		double d = -1.0;
		try
		{
			d = Double.parseDouble(result);
		}
		catch (NumberFormatException nfe)
		{
			log.error("Unable to parse {} as a double!", result, nfe);
		}

		return d;
	}

	private int getRealValueAsInt(String element, IDataManager dataManager)
	{
		String result = getValue(element, dataManager);
		if (StringUtils.isBlank(result) || result.equals("unknown"))
		{
			return -1;
		}

		int i = -1;
		try
		{
			double d = Double.parseDouble(result);
			i = (int) d;
		}
		catch (NumberFormatException nfe)
		{
			log.error("Unable to parse {} as a double!", result, nfe);
		}

		return i;
	}

	@Override
	public String[] getSiblingIds(long contentPackageId, String learnerId, long attemptNumber, String scoId, String interactionId)
	{
		String[] ids = new String[2];
		String prevId = "";
		String nextId = "";

		// Assume that minimally we have a contentPackageId, a learnerId, and an attemptNumber
		if (scoId == null)
		{
			// We just have the above ids
			String context = lms().currentContext();
			List<Learner> learners = learnerDao().find(context);
			Collections.sort(learners);

			for (int i = 0; i < learners.size(); i++)
			{
				Learner learner = learners.get(i);
				if (learner.getId().equals(learnerId))
				{
					if (i - 1 >= 0)
					{
						prevId = learners.get(i - 1).getId();
					}

					if (i + 1 < learners.size())
					{
						nextId = learners.get(i + 1).getId();
					}

					break;
				}
			}
		}
		else if (interactionId == null)
		{
			// We just have a sco id
			List<IDataManager> dataManagers = dataManagerDao().find(contentPackageId, learnerId, attemptNumber);

			for (int i = 0; i < dataManagers.size(); i++)
			{
				IDataManager dm = dataManagers.get(i);
				if (StringUtils.equals(dm.getScoId(), scoId))
				{
					if (i - 1 >= 0)
					{
						prevId = dataManagers.get(i - 1).getScoId();
					}

					if (i + 1 < dataManagers.size())
					{
						nextId = dataManagers.get(i + 1).getScoId();
					}

					break;
				}
			}
		}
		else
		{
			IDataManager dataManager = dataManagerDao().find(contentPackageId, learnerId, attemptNumber, scoId);

			// We have everything
			List<Interaction> interactions = new LinkedList<>();
			mapInteractions(interactions, dataManager, contentPackageId, learnerId, attemptNumber, true);

			for (int i = 0; i < interactions.size(); i++)
			{
				Interaction interaction = interactions.get(i);
				if (interaction.getInteractionId().equals(interactionId))
				{
					if (i - 1 >= 0)
					{
						prevId = interactions.get(i - 1).getInteractionId();
					}

					if (i + 1 < interactions.size())
					{
						nextId = interactions.get(i + 1).getInteractionId();
					}

					break;
				}
			}
		}

		ids[0] = prevId;
		ids[1] = nextId;
		return ids;
	}

	public List<CMIData> getSummaryCMIData(Attempt attempt)
	{
		// FIXME: Need to replace SCO_ID with a real id
		IDataManager dataManager = dataManagerDao().find(attempt.getCourseId(), "SCO_ID", attempt.getLearnerId(), attempt.getAttemptNumber());
		List<CMIData> cmiData = new LinkedList<>();
		CMIFieldGroup group = getSummaryFieldGroup();
		List<CMIField> list = group.getList();

		for (CMIField field : list)
		{
			cmiData.addAll(populateData(field, dataManager));
		}

		return cmiData;
	}

	private CMIFieldGroup getSummaryFieldGroup()
	{
		CMIFieldGroup group = new CMIFieldGroup();
		List<CMIField> list = group.getList();
		list.add(new CMIField(CMI_SCORE_SCALED, "Overall Scaled Score"));
		list.add(new CMIField(CMI_COMPLETION_STATUS, "Completion Status"));
		list.add(new CMIField(CMI_SUCCESS_STATUS, "Success Status"));
		list.add(new CMIField(CMI_COMPLETION_THRESHOLD, "Completion Threshold"));
		list.add(new CMIField(CMI_PROGRESS_MEASURE, "Progress Measure"));
		return group;
	}

	private String getValue(String iDataModelElement, IDataManager dataManager)
	{
		// Process 'GET'
		DMProcessingInfo dmInfo = new DMProcessingInfo();
		int dmErrorCode = 0;
		dmErrorCode = DMInterface.processGetValue(iDataModelElement, false, dataManager, dmInfo);
		return dmErrorCode == APIErrorCodes.NO_ERROR ? dmInfo.mValue : "";
	}

	private int getValueAsInt(String element, IDataManager dataManager)
	{
		String result = getValue(element, dataManager);
		if (result.trim().length() == 0 || result.equals("unknown"))
		{
			return -1;
		}

		int i = -1;
		try
		{
			i = Integer.parseInt(result);
		}
		catch (NumberFormatException nfe)
		{
			log.error("Unabled to parse {} as an int!", result, nfe);
		}

		return i;
	}

	private String getValueAsString(String element, IDataManager dataManager)
	{
		String result = getValue(element, dataManager);
		if (result.trim().length() == 0 || result.equals("unknown"))
		{
			return null;
		}

		return result;
	}

	public Attempt lookupAttempt(String courseId, String learnerId, long attemptNumber, String[] fields)
	{
		Attempt attempt = attemptDao().find(courseId, learnerId, attemptNumber);
		if (attempt == null)
		{
			attempt = new Attempt();
		}

		IDataManager dataManager = dataManagerDao().find(attempt.getCourseId(), "SCO_ID", attempt.getLearnerId(), attempt.getAttemptNumber());
		attempt.setBeginDate(dataManager.getBeginDate());
		attempt.setLastModifiedDate(dataManager.getLastModifiedDate());
		return attempt;
	}

	private void mapInteractions(List<Interaction> interactions, IDataManager dataManager, long contentPackageId, String learnerId, long attemptNumber, boolean onlyIds)
	{
		int numberOfInteractions = getValueAsInt(CMI_INTERACTIONS_COUNT, dataManager);
		for (int i = 0; i < numberOfInteractions; i++)
		{
			Interaction interaction = new Interaction();
			interaction.setActivityTitle(dataManager.getTitle());
			interaction.setAttemptNumber(attemptNumber);
			interaction.setContentPackageId(contentPackageId);
			interaction.setLearnerId(learnerId);
			interaction.setScoId(dataManager.getScoId());

			String interactionName = new StringBuilder(CMI_INTERACTIONS_ROOT).append(i).append(".").toString();
			String interactionIdName = new StringBuilder(interactionName).append("id").toString();
			interaction.setInteractionId(getValueAsString(interactionIdName, dataManager));

			if (!onlyIds)
			{
				String interactionTypeName = new StringBuilder(interactionName).append("type").toString();
				interaction.setType(getValueAsString(interactionTypeName, dataManager));

				String numberOfObjectiveIdsName = new StringBuilder(interactionName).append("objectives._count").toString();
				int numberOfObjectiveIds = getValueAsInt(numberOfObjectiveIdsName, dataManager);

				List<String> idList = interaction.getObjectiveIds();
				for (int j = 0; j < numberOfObjectiveIds; j++)
				{
					String objectiveIdName = new StringBuilder(interactionName).append("objectives.").append(j).append(".id").toString();
					idList.add(getValueAsString(objectiveIdName, dataManager));
				}

				String interactionTimestampName = new StringBuilder(interactionName).append("timestamp").toString();
				interaction.setTimestamp(getValueAsString(interactionTimestampName, dataManager));

				String numCorrectResponsesName = new StringBuilder(interactionName).append("correct_responses._count").toString();
				int numCorrectResponses = getValueAsInt(numCorrectResponsesName, dataManager);

				List<String> correctResponses = interaction.getCorrectResponses();
				for (int n = 0; n < numCorrectResponses; n++)
				{
					String correctResponsePatternName = new StringBuilder(interactionName).append("correct_responses.").append(n).append(".pattern").toString();
					String correctResponsePattern = getValueAsString(correctResponsePatternName, dataManager);
					correctResponses.add(correctResponsePattern);
				}

				String weightingName = new StringBuilder(interactionName).append("weighting").toString();
				interaction.setWeighting(getRealValue(weightingName, dataManager));

				String learnerResponseName = new StringBuilder(interactionName).append("learner_response").toString();
				interaction.setLearnerResponse(getValueAsString(learnerResponseName, dataManager));

				String resultName = new StringBuilder(interactionName).append("result").toString();
				interaction.setResult(getValueAsString(resultName, dataManager));

				String latencyName = new StringBuilder(interactionName).append("latency").toString();
				interaction.setLatency(getValueAsString(latencyName, dataManager));

				String descriptionName = new StringBuilder(interactionName).append("description").toString();
				interaction.setDescription(getValueAsString(descriptionName, dataManager));
			}

			interactions.add(interaction);
		}
	}

	private void mapObjectives(Map<String, Objective> objectives, IDataManager dataManager, long contentPackageId, String learnerId, long attemptNumber)
	{
		int numberOfObjectives = getValueAsInt(CMI_OBJECTIVES_COUNT, dataManager);
		for (int i = 0; i < numberOfObjectives; i++)
		{
			Objective objective = new Objective();
			String objectiveName = new StringBuilder(CMI_OBJECTIVES_ROOT).append(i).append(".").toString();

			String objectiveIdName = new StringBuilder(objectiveName).append("id").toString();
			objective.setId(getValueAsString(objectiveIdName, dataManager));

			String completionStatusName = new StringBuilder(objectiveName).append("completion_status").toString();
			objective.setCompletionStatus(getValueAsString(completionStatusName, dataManager));

			String descriptionName = new StringBuilder(objectiveName).append("description").toString();
			objective.setDescription(getValueAsString(descriptionName, dataManager));

			String successStatusName = new StringBuilder(objectiveName).append("success_status").toString();
			objective.setSuccessStatus(getValueAsString(successStatusName, dataManager));

			Score objectiveScore = new Score();
			String objectiveScoreName = new StringBuilder(objectiveName).append("score.").toString();

			String objectiveScaledScoreName = new StringBuilder(objectiveScoreName).append("scaled").toString();
			objectiveScore.setScaled(getRealValue(objectiveScaledScoreName, dataManager));

			String objectiveRawScoreName = new StringBuilder(objectiveScoreName).append("raw").toString();
			objectiveScore.setRaw(getRealValue(objectiveRawScoreName, dataManager));

			String objectiveMinScoreName = new StringBuilder(objectiveScoreName).append("min").toString();
			objectiveScore.setMin(getRealValue(objectiveMinScoreName, dataManager));

			String objectiveMaxScoreName = new StringBuilder(objectiveScoreName).append("max").toString();
			objectiveScore.setMax(getRealValue(objectiveMaxScoreName, dataManager));

			String objectiveScaledToPassScoreName = new StringBuilder(objectiveScoreName).append("scaled_passing_score").toString();
			objectiveScore.setScaledToPass(getRealValue(objectiveScaledToPassScoreName, dataManager));

			objective.setScore(objectiveScore);

			String identifier = objective.getId();
			objectives.put(identifier, objective);
		}
	}

	private void mapValues(ActivityReport report, IDataManager dataManager, long contentPackageId, String learnerId, long attemptNumber)
	{
		Progress progress = new Progress();
		progress.setProgressMeasure(getRealValue(CMI_PROGRESS_MEASURE, dataManager));
		progress.setCompletionThreshold(getRealValue(CMI_COMPLETION_THRESHOLD, dataManager));
		progress.setLearnerLocation(getValueAsString(CMI_LOCATION, dataManager));
		progress.setSuccessStatus(getValueAsString(CMI_SUCCESS_STATUS, dataManager));
		progress.setCompletionStatus(getValueAsString(CMI_COMPLETION_STATUS, dataManager));
		progress.setMaxSecondsAllowed(getRealValueAsInt(CMI_MAX_TIME_ALLOWED, dataManager));
		progress.setTotalSessionSeconds(getValueAsString(CMI_TOTAL_TIME, dataManager));
		report.setProgress(progress);

		Score score = new Score();
		score.setScaled(getRealValue(CMI_SCORE_SCALED, dataManager));
		score.setRaw(getRealValue(CMI_SCORE_RAW, dataManager));
		score.setMin(getRealValue(CMI_SCORE_MIN, dataManager));
		score.setMax(getRealValue(CMI_SCORE_MAX, dataManager));
		score.setScaledToPass(getRealValue(CMI_SCORE_SCALED + "_passing_score", dataManager));
		report.setScore(score);

		List<Interaction> interactions = report.getInteractions();
		mapInteractions(interactions, dataManager, contentPackageId, learnerId, attemptNumber, false);

		// Map objectives
		for( Interaction interaction : interactions )
		{
			Map<String, Objective> objectivesMap = new HashMap<>();
			mapObjectives( objectivesMap, dataManager, contentPackageId, learnerId, attemptNumber );

			List<Objective> objectivesList = interaction.getObjectives();
			for( String objectiveID : interaction.getObjectiveIds() )
			{
				Objective objective = objectivesMap.get( objectiveID );
				if( objective != null )
				{
					objectivesList.add( objective );
				}
			}
		}
	}

	private void mapValues(ActivitySummary summary, IDataManager dataManager)
	{
		summary.setProgressMeasure(getRealValue(CMI_PROGRESS_MEASURE, dataManager));
		summary.setCompletionThreshold(getRealValue(CMI_COMPLETION_THRESHOLD, dataManager));
		summary.setLearnerLocation(getValueAsString(CMI_LOCATION, dataManager));
		summary.setSuccessStatus(getValueAsString(CMI_SUCCESS_STATUS, dataManager));
		summary.setCompletionStatus(getValueAsString(CMI_COMPLETION_STATUS, dataManager));
		summary.setMaxSecondsAllowed(getRealValueAsInt(CMI_MAX_TIME_ALLOWED, dataManager));
		summary.setTotalSessionSeconds(getValueAsString(CMI_TOTAL_TIME, dataManager));

		summary.setScaled(getRealValue(CMI_SCORE_SCALED, dataManager));
		summary.setRaw(getRealValue(CMI_SCORE_RAW, dataManager));
		summary.setMin(getRealValue(CMI_SCORE_MIN, dataManager));
		summary.setMax(getRealValue(CMI_SCORE_MAX, dataManager));
		summary.setScaledToPass(getRealValue(CMI_SCORE_SCALED + "_passing_score", dataManager));
	}

	private List<CMIData> populateData(CMIField field, IDataManager dataManager)
	{
		List<CMIData> cmiData = new LinkedList<>();
		if (field.isParent())
		{
			int count = getCount(field.getFieldName(), dataManager);
			if (count != 0)
			{
				for (CMIField child : field.getChildren())
				{
					for (int i = 0; i < count; i++)
					{
						String fieldName = new StringBuilder(field.getFieldName()).append(".").append(i).append(".").append(child.getFieldName()).toString();
						String value = getValue(fieldName, dataManager);

						if (value != null && !value.isEmpty() && !value.equals("unknown"))
						{
							cmiData.add(new CMIData(fieldName, value, child.getDescription()));
						}
					}
				}
			}
		}
		else
		{
			String value = getValue(field.getFieldName(), dataManager);
			if (value != null && !value.isEmpty() && !value.equals("unknown"))
			{
				cmiData.add(new CMIData(field.getFieldName(), value, field.getDescription()));
			}
		}

		return cmiData;
	}

	// FIXME: Doesn't handle arbitrary depth -- only 1 deep with children
	private void populateValues(CMIField field, IDataManager dataManager)
	{
		if (field.isParent())
		{
			int count = getCount(field.getFieldName(), dataManager);
			if (count != 0)
			{
				for (CMIField child : field.getChildren())
				{
					for (int i = 0; i < count; i++)
					{
						String fieldName = new StringBuilder(field.getFieldName()).append(".").append(i).append(".").append(child.getFieldName()).toString();
						String value = getValue(fieldName, dataManager);
						child.addFieldValue(value);
					}
				}
			}
		}
		else
		{
			String value = getValue(field.getFieldName(), dataManager);
			if (value != null && !value.equals("unknown"))
			{
				field.addFieldValue(value);
			}
		}
	}

	@Override
	public void saveAttempt(Attempt attempt)
	{
		attemptDao().save(attempt);
	}

	@Override
	public int countAttempts(long contentPackageId, String learnerId)
	{
		return attemptDao().count(contentPackageId, learnerId);
	}
}
