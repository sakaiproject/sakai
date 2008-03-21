/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.scorm.service.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.adl.api.ecmascript.APIErrorCodes;
import org.adl.datamodels.DMInterface;
import org.adl.datamodels.DMProcessingInfo;
import org.adl.datamodels.IDataManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

public abstract class ScormResultServiceImpl implements ScormResultService {

	private static Log log = LogFactory.getLog(ScormResultServiceImpl.class);
	
	private static String[] fields = {"cmi.completion_status", "cmi.score.scaled", "cmi.success_status" };
	
	// Dependency injection method lookup signatures
	protected abstract LearningManagementSystem lms();
	
	// Daos (also depedency injected)
	protected abstract AttemptDao attemptDao();
	protected abstract DataManagerDao dataManagerDao();
	protected abstract LearnerDao learnerDao();
	
	
	public ActivityReport getActivityReport(long contentPackageId, String activityId, String learnerId, long attemptNumber) {
		ActivityReport report = new ActivityReport();
		
		IDataManager dataManager = dataManagerDao().findByActivityId(contentPackageId, activityId, learnerId, attemptNumber);
		
		report.setActivityId(activityId);
		report.setTitle(dataManager.getTitle());
		report.setScoId(dataManager.getScoId());
		report.setCmiData(getCMIData(dataManager));

		mapValues(report, dataManager, contentPackageId, learnerId, attemptNumber);
		
		return report;
	}
		
	public List<ActivityReport> getActivityReports(long contentPackageId, String learnerId, long attemptNumber) {	
		List<IDataManager> dataManagers = dataManagerDao().find(contentPackageId, learnerId, attemptNumber);
		List<ActivityReport> reports = new LinkedList<ActivityReport>();
		
		for (IDataManager dataManager : dataManagers) {
			ActivityReport report = new ActivityReport();
			Map<String,CMIData> map = this.getCMIDataMap(dataManager);
		
			report.setTitle(dataManager.getTitle());
			report.setScoId(dataManager.getScoId());
			report.setCmiData(getCMIData(dataManager));
			
			mapValues(report, dataManager, contentPackageId, learnerId, attemptNumber);
			
			reports.add(report);
		}
		
		return reports;
	}
	
	public ActivityReport getActivityReport(long contentPackageId, String learnerId, long attemptNumber, String scoId) {
		IDataManager dataManager = dataManagerDao().find(contentPackageId, learnerId, attemptNumber, scoId);
		
		ActivityReport report = new ActivityReport();
		Map<String,CMIData> map = this.getCMIDataMap(dataManager);
	
		report.setTitle(dataManager.getTitle());
		report.setScoId(dataManager.getScoId());
		report.setCmiData(getCMIData(dataManager));
		
		mapValues(report, dataManager, contentPackageId, learnerId, attemptNumber);

		return report;
	}
	
	
	public List<ActivitySummary> getActivitySummaries(long contentPackageId, String learnerId, long attemptNumber) {
		List<IDataManager> dataManagers = dataManagerDao().find(contentPackageId, learnerId, attemptNumber);
		List<ActivitySummary> summaries = new LinkedList<ActivitySummary>();
		
		for (IDataManager dataManager : dataManagers) {
			ActivitySummary summary = new ActivitySummary();
			Map<String,CMIData> map = this.getCMIDataMap(dataManager);
		
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
	
	public Interaction getInteraction(long contentPackageId, String learnerId, long attemptNumber, String scoId, String interactionId) {
		IDataManager dataManager = dataManagerDao().find(contentPackageId, learnerId, attemptNumber, scoId);
		
		List<Interaction> interactions = new LinkedList<Interaction>();
		mapInteractions(interactions, dataManager, contentPackageId, learnerId, attemptNumber, false);
		
		Interaction interaction = null;
		
		for (Interaction current : interactions) {
			
			if (current.getInteractionId().equals(interactionId)) {
				interaction = current;
				break;
			}
			
		}
		
		if (interaction != null && interaction.getObjectiveIds().size() > 0) {
			Map<String, Objective> objectivesMap = new HashMap<String, Objective>();
			mapObjectives(objectivesMap, dataManager, contentPackageId, learnerId, attemptNumber);
		
			List<Objective> objectivesList = interaction.getObjectives();
			for (String objectiveId : interaction.getObjectiveIds()) {
				Objective objective = objectivesMap.get(objectiveId);
				if (objective != null)
					objectivesList.add(objective);
			}
		}
		
		return interaction;
	}
	
	
	public String[] getSiblingIds(long contentPackageId, String learnerId, long attemptNumber, String scoId, String interactionId) {
		String[] ids = new String[2];
		
		String prevId = "";
		String nextId = "";
		
		// Assume that minimally we have a contentPackageId, a learnerId, and an attemptNumber
		if (scoId == null) {
			// We just have the above ids
			String context = lms().currentContext();
			List<Learner> learners = learnerDao().find(context);	
			
			for (int i=0;i<learners.size();i++) {
				Learner learner = learners.get(i);
				
				if (learner.getId().equals(learnerId)) {
					
					if (i-1 >= 0)
						prevId = learners.get(i-1).getId();
					if (i+1 < learners.size())
						nextId = learners.get(i+1).getId();
					
					break;
				}
				
			}
			
		} else if (interactionId == null) {
			// We just have a sco id
			List<IDataManager> dataManagers = dataManagerDao().find(contentPackageId, learnerId, attemptNumber);
			
			for (int i=0;i<dataManagers.size();i++) {
				IDataManager dm = dataManagers.get(i);
				
				if (dm.getScoId().equals(scoId)) {
					
					if (i-1 >= 0) 
						prevId = dataManagers.get(i-1).getScoId();
					
					if (i+1 < dataManagers.size()) 
						nextId = dataManagers.get(i+1).getScoId();
					
					break;
				}
				
			}
			
			
		} else {
			IDataManager dataManager = dataManagerDao().find(contentPackageId, learnerId, attemptNumber, scoId);
			
			// We have everything
			List<Interaction> interactions = new LinkedList<Interaction>();
			
			mapInteractions(interactions, dataManager, contentPackageId, learnerId, attemptNumber, true);
			
			for (int i=0;i<interactions.size();i++) {
				Interaction interaction = interactions.get(i);
				
				if (interaction.getInteractionId().equals(interactionId)) {
					
					if (i-1 >= 0) {
						prevId = interactions.get(i-1).getInteractionId();
					} 
					
					if (i+1 < interactions.size()) {
						nextId = interactions.get(i+1).getInteractionId();
					}
	
					break;
				}
				
			}
		}
		
		ids[0] = prevId;
		ids[1] = nextId;
		
		return ids;
	}
	
	
	
	public List<CMIData> getSummaryCMIData(Attempt attempt) {
		// FIXME: Need to replace SCO_ID with a real id
		IDataManager dataManager = dataManagerDao().find(attempt.getCourseId(), "SCO_ID", attempt.getLearnerId(), attempt.getAttemptNumber());
		//.load(attempt.getDataManagerId());
		
		List<CMIData> cmiData = new LinkedList<CMIData>();
		
		CMIFieldGroup group = getSummaryFieldGroup();
		List<CMIField> list = group.getList();
		
		for (CMIField field : list) {
			cmiData.addAll(populateData(field, dataManager));
		}
		
		return cmiData;
	}
	
	private List<CMIData> getCMIData(IDataManager dataManager) {
		//if (log.isDebugEnabled())
		//	log.debug("activityId: " + activityId);
		
		//IDataManager dataManager = dataManagerDao().findByActivityId(attempt.getContentPackageId(), activityId, attempt.getLearnerId(), attempt.getAttemptNumber());
		//.load(attempt.getDataManagerId());
		
		List<CMIData> cmiData = new LinkedList<CMIData>();
		
		CMIFieldGroup group = getDefaultFieldGroup();
		List<CMIField> list = group.getList();
		
		for (CMIField field : list) {
			cmiData.addAll(populateData(field, dataManager));
		}
		
		return cmiData;
	}
	
	
	public CMIFieldGroup getAttemptResults(Attempt attempt) {
		
		IDataManager dataManager = dataManagerDao().find(attempt.getCourseId(), "SCO_ID", attempt.getLearnerId(), attempt.getAttemptNumber());
		//.load(attempt.getDataManagerId());
		
		CMIFieldGroup group = getDefaultFieldGroup();
		List<CMIField> list = group.getList();
		
		for (CMIField field : list) {
			populateValues(field, dataManager);
		}
		
		return group;
	}
	
	private List<CMIData> populateData(CMIField field, IDataManager dataManager) {
		List<CMIData> cmiData = new LinkedList<CMIData>();
		
		if (field.isParent()) {
					
			int count = getCount(field.getFieldName(), dataManager);
			
			if (count != 0) {
				for (CMIField child : field.getChildren()) {
					for (int i=0;i<count;i++) {
						String fieldName = new StringBuilder(field.getFieldName()).append(".").append(i).append(".").append(child.getFieldName()).toString();		
						String value = getValue(fieldName, dataManager);
				
						if (value != null && !value.equals("") && !value.equals("unknown"))
							cmiData.add(new CMIData(fieldName, value, child.getDescription()));
					}
				}
			}
		} else {
			String value = getValue(field.getFieldName(), dataManager);
			
			if (value != null && !value.equals("") && !value.equals("unknown"))
				cmiData.add(new CMIData(field.getFieldName(), value, field.getDescription()));
		}
		
		return cmiData;
	}
	
	
	// FIXME: Doesn't handle arbitrary depth -- only 1 deep with children
	private void populateValues(CMIField field, IDataManager dataManager) {
		
		if (field.isParent()) {
					
			int count = getCount(field.getFieldName(), dataManager);
			
			if (count != 0) {
				for (CMIField child : field.getChildren()) {
					for (int i=0;i<count;i++) {
						String fieldName = new StringBuilder(field.getFieldName()).append(".").append(i).append(".").append(child.getFieldName()).toString();		
						String value = getValue(fieldName, dataManager);
				
						child.addFieldValue(value);
					}
				}
			}
		} else {
			String value = getValue(field.getFieldName(), dataManager);
			
			if (value != null && !value.equals("unknown"))
				field.addFieldValue(value);
		}
	}
	
	private void mapValues(ActivitySummary summary, IDataManager dataManager) {

		summary.setProgressMeasure(getRealValue("cmi.progress_measure", dataManager));
		summary.setCompletionThreshold(getRealValue("cmi.completion_threshold", dataManager));
		summary.setLearnerLocation(getValueAsString("cmi.location", dataManager));
		summary.setSuccessStatus(getValueAsString("cmi.success_status", dataManager));
		summary.setCompletionStatus(getValueAsString("cmi.completion_status", dataManager));
		summary.setMaxSecondsAllowed(getRealValueAsInt("cmi.max_time_allowed", dataManager));
		summary.setTotalSessionSeconds(getValueAsString("cmi.total_time", dataManager));

		summary.setScaled(getRealValue("cmi.score.scaled", dataManager));
		summary.setRaw(getRealValue("cmi.score.raw", dataManager));
		summary.setMin(getRealValue("cmi.score.min", dataManager));
		summary.setMax(getRealValue("cmi.score.max", dataManager));
		summary.setScaledToPass(getRealValue("cmi.score.scaled_passing_score", dataManager));

	}
	
	
	private void mapValues(ActivityReport report, IDataManager dataManager, long contentPackageId, String learnerId, long attemptNumber) {
		Progress progress = new Progress();
		
		progress.setProgressMeasure(getRealValue("cmi.progress_measure", dataManager));
		progress.setCompletionThreshold(getRealValue("cmi.completion_threshold", dataManager));
		progress.setLearnerLocation(getValueAsString("cmi.location", dataManager));
		progress.setSuccessStatus(getValueAsString("cmi.success_status", dataManager));
		progress.setCompletionStatus(getValueAsString("cmi.completion_status", dataManager));
		progress.setMaxSecondsAllowed(getRealValueAsInt("cmi.max_time_allowed", dataManager));
		progress.setTotalSessionSeconds(getValueAsString("cmi.total_time", dataManager));
		
		report.setProgress(progress);
		
		
		Score score = new Score();
		
		score.setScaled(getRealValue("cmi.score.scaled", dataManager));
		score.setRaw(getRealValue("cmi.score.raw", dataManager));
		score.setMin(getRealValue("cmi.score.min", dataManager));
		score.setMax(getRealValue("cmi.score.max", dataManager));
		score.setScaledToPass(getRealValue("cmi.score.scaled_passing_score", dataManager));
		
		report.setScore(score);
		
		List<Interaction> interactions = report.getInteractions();
		
		mapInteractions(interactions, dataManager, contentPackageId, learnerId, attemptNumber, false);
		
		//Map<String, Objective> objectives = report.getObjectives();
		//mapObjectives(objectives, dataManager, contentPackageId, learnerId, attemptNumber);
	
	}
	
	
	private void mapInteractions(List<Interaction> interactions, IDataManager dataManager, long contentPackageId, String learnerId, long attemptNumber, boolean onlyIds) {
		int numberOfInteractions = getValueAsInt("cmi.interactions._count", dataManager);
		
		for (int i=0;i<numberOfInteractions;i++) {
			Interaction interaction = new Interaction();
			
			interaction.setActivityTitle(dataManager.getTitle());
			interaction.setAttemptNumber(attemptNumber);
			interaction.setContentPackageId(contentPackageId);
			interaction.setLearnerId(learnerId);
			interaction.setScoId(dataManager.getScoId());
			
			String interactionName = new StringBuilder("cmi.interactions.").append(i).append(".").toString();
			
			String interactionIdName = new StringBuilder(interactionName).append("id").toString();
			interaction.setInteractionId(getValueAsString(interactionIdName, dataManager));
			
			if (! onlyIds) {
				String interactionTypeName = new StringBuilder(interactionName).append("type").toString();
				interaction.setType(getValueAsString(interactionTypeName, dataManager));
				
				String numberOfObjectiveIdsName = new StringBuilder(interactionName).append("objectives._count").toString();
				int numberOfObjectiveIds = getValueAsInt(numberOfObjectiveIdsName, dataManager);
				
				List<String> idList = interaction.getObjectiveIds();
				for (int j=0;j<numberOfObjectiveIds;j++) {
					String objectiveIdName = new StringBuilder(interactionName).append("objectives.")
						.append(j).append(".id").toString();
					
					idList.add(getValueAsString(objectiveIdName, dataManager));
				}
				
				String interactionTimestampName = new StringBuilder(interactionName).append("timestamp").toString();
				interaction.setTimestamp(getValueAsString(interactionTimestampName, dataManager));
				
				String numCorrectResponsesName = new StringBuilder(interactionName).append("correct_responses._count").toString();
				int numCorrectResponses = getValueAsInt(numCorrectResponsesName, dataManager);
				
				List<String> correctResponses = interaction.getCorrectResponses();
				for (int n=0;n<numCorrectResponses;n++) {
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
	
	private void mapObjectives(Map<String, Objective> objectives, IDataManager dataManager, long contentPackageId, String learnerId, long attemptNumber) {
		int numberOfObjectives = getValueAsInt("cmi.objectives._count", dataManager);
		
		for (int i=0;i<numberOfObjectives;i++) {
			Objective objective = new Objective();
			
			String objectiveName = new StringBuilder("cmi.objectives.").append(i).append(".").toString();
			
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
	
	
	private int getCount(String fieldName, IDataManager dataManager) {
		
		String countFieldName = new StringBuilder(fieldName).append("._count").toString();
		
		String strValue = getValue(countFieldName, dataManager);
		
		int count = 0;
		
		if (strValue != null && !strValue.equals("")) {
			try {
				count = Integer.parseInt(strValue);
			} catch (NumberFormatException nfe) {
				log.warn("Count field name " + countFieldName + " retrieved a non-numeric result from the data manager", nfe);
			}
		}
		
		return count;
	}
	
	
	public Attempt lookupAttempt(String courseId, String learnerId, long attemptNumber, 
			String[] fields) {
				
		Attempt attempt = attemptDao().find(courseId, learnerId, attemptNumber);
		
		IDataManager dataManager = dataManagerDao().find(attempt.getCourseId(), "SCO_ID", attempt.getLearnerId(), attempt.getAttemptNumber());
		//.load(attempt.getDataManagerId());
		
		Properties dataProperties = new Properties();
		
		for (int i=0;i<fields.length;i++) {
			String value = getValue(fields[i], dataManager);
			
		}
		
		String completionStatus = getValue("cmi.completion_status", dataManager);
		String scaledScore = getValue("cmi.score.scaled", dataManager);
		String successStatus = getValue("cmi.success_status", dataManager);
		
		if (attempt == null)
			attempt = new Attempt();
		
		attempt.setBeginDate(dataManager.getBeginDate());
		attempt.setLastModifiedDate(dataManager.getLastModifiedDate());
		
		return attempt;
	}
	
	public Attempt getAttempt(long id) {
		return attemptDao().load(id);
	}
	
	public Attempt getAttempt(long contentPackageId, String learnerId, long attemptNumber) {
		return attemptDao().lookup(contentPackageId, learnerId, attemptNumber);
	}
	
	public List<Attempt> getAttempts(long contentPackageId, String learnerId) {
		return attemptDao().find(contentPackageId, learnerId);
	}
	
	public List<Attempt> getAttempts(String courseId, String learnerId) {
		return attemptDao().find(courseId, learnerId);
	}
	
	public List<Attempt> getAttempts(long contentPackageId) {
		return attemptDao().find(contentPackageId);
	}
	
	public int getNumberOfAttempts(long contentPackageId, String learnerId) {
		return attemptDao().count(contentPackageId, learnerId);
	}
	
	public List<LearnerExperience> getLearnerExperiences(long contentPackageId) {
		List<LearnerExperience> experiences = new LinkedList<LearnerExperience>();
		
		String context = lms().currentContext();
		List<Learner> learners = learnerDao().find(context);
		
		for (int i=0;i<learners.size();i++) {
			Learner learner = learners.get(i);
			LearnerExperience experience = new LearnerExperience(learner, contentPackageId);			
			List<Attempt> attempts = getAttempts(contentPackageId, learner.getId());
			
			int status = LearnerExperience.NOT_ACCESSED;
			if (attempts != null) {
				if (attempts.size() > 0) {
					// Grab the latest attempt
					Attempt latestAttempt = attempts.get(0);
					
					//List<CMIData> data = getSummaryCMIData(latestAttempt);
					experience.setLastAttemptDate(latestAttempt.getBeginDate());
	
					status = LearnerExperience.COMPLETED;
				}
			
				experience.setNumberOfAttempts(attempts.size());
				
				
			}
			
			experience.setStatus(status);
			experiences.add(experience);
		}
		
		
		return experiences;
	}
	
	
	public void saveAttempt(Attempt attempt) {
		attemptDao().save(attempt);
	}
	

	/*

// SCORE ELEMENTS
select e1.* from SCORM_ELEMENT_T e1, SCORM_ELEMENT_DESC_T d1
where d1.ED_BINDING = 'scaled'
and d1.ELEM_DESC_ID = e1.DESCRIPTION
and e1.PARENT in (
    select e2.ELEMENT_ID from SCORM_ELEMENT_T e2, SCORM_ELEMENT_DESC_T d2
    where d2.ED_BINDING = 'score'
    and d2.ELEM_DESC_ID = e2.DESCRIPTION
)

// DATAMODELS
select d.* from SCORM_DATAMODEL_T d, SCORM_DATAMANAGER_T dm, SCORM_MAP_DATAMODELS_T mp
where dm.DATAMANAGER_ID = 5
and mp.DATAMANAGER_ID = dm.DATAMANAGER_ID
and mp.DATAMODEL_ID = d.DATAMODEL_ID


// ALL ELEMENTS FOR A DATAMODEL

select mp.ELEMENT_BINDING, e.VALUE from SCORM_MAP_ELEMENTS_T mp, SCORM_DATAMODEL_T d, SCORM_ELEMENT_T e
where d.DATAMODEL_ID = mp.DATAMODEL_ID
and mp.DATAMODEL_ID = 14
and e.ELEMENT_ID = mp.ELEMENT_ID
and e.PARENT is null



	 */
	
	
	private Map<String, CMIData> getCMIDataMap(IDataManager dataManager) {
		
		List<CMIData> cmiData = new LinkedList<CMIData>();
		
		CMIFieldGroup group = getDefaultFieldGroup();
		List<CMIField> list = group.getList();
		
		for (CMIField field : list) {
			cmiData.addAll(populateData(field, dataManager));
		}
		
		Map<String,CMIData> map = new HashMap<String, CMIData>();
		
		for (CMIData item : cmiData) {
			map.put(item.getFieldName(), item);
		}
		
		return map;
	}
	
	private double getRealValue(String element, IDataManager dataManager) {
		String result = getValue(element, dataManager);
		
		if (result.trim().length() == 0 || result.equals("unknown"))
			return -1.0;
		
		double d = -1.0;
		
		try {
			d = Double.parseDouble(result);
			
				
		} catch (NumberFormatException nfe) {
			log.error("Unable to parse " + result + " as a double!");
		}
		
		return d;
	}
	
	private int getRealValueAsInt(String element, IDataManager dataManager) {
		String result = getValue(element, dataManager);
		
		if (result.trim().length() == 0 || result.equals("unknown"))
			return -1;
		
		int i = -1;
		
		try {
			double d = Double.parseDouble(result);
			
			i = (int)d;	
		} catch (NumberFormatException nfe) {
			log.error("Unable to parse " + result + " as a double!");
		}
		
		return i;
	}
	
	private int getRealValueAsIntScaled(String element, IDataManager dataManager) {
		String result = getValue(element, dataManager);
		
		if (result.trim().length() == 0 || result.equals("unknown"))
			return -1;
		
		int i = -1;
		
		try {
			double d = Double.parseDouble(result);
			
			d *= 1000;
			
			i = (int)d;
			
		} catch (NumberFormatException nfe) {
			log.error("Unable to parse " + result + " as a double!");
		}
		
		return i;
	}
	
	private int getValueAsInt(String element, IDataManager dataManager) {
		String result = getValue(element, dataManager);
		
		if (result.trim().length() == 0 || result.equals("unknown"))
			return -1;

		int i = -1;
		
		try {
			i = Integer.parseInt(result);
		} catch (NumberFormatException nfe) {
			log.error("Unabled to parse " + result + " as an int!");
		}
		
        return i;
	}
	
	private String getValueAsString(String element, IDataManager dataManager) {
		String result = getValue(element, dataManager);
		
		if (result.trim().length() == 0 || result.equals("unknown"))
			return null;
			
        return result;
	}
	
	private long getValueAsLong(String element, IDataManager dataManager) {
		String result = getValue(element, dataManager);
		
		if (result.trim().length() == 0 || result.equals("unknown"))
			return -1;

		long l = -1;
		
		try {
			l = Long.parseLong(result);
		} catch (NumberFormatException nfe) {
			log.error("Unabled to parse " + result + " as a long!");
		}
		
        return l;
	}
	
	private String getValue(String iDataModelElement, IDataManager dataManager) {
		// Process 'GET'
        DMProcessingInfo dmInfo = new DMProcessingInfo();
        
        String result;
        int dmErrorCode = 0;
        dmErrorCode = DMInterface.processGetValue(iDataModelElement, false, dataManager, dmInfo);

        if ( dmErrorCode == APIErrorCodes.NO_ERROR ) {
        	result = dmInfo.mValue;
        } else {
            result = new String("");
        }
        
        return result;
	}

	
	private CMIFieldGroup getSummaryFieldGroup() {
		CMIFieldGroup group = new CMIFieldGroup();
		
		List<CMIField> list = group.getList();
		list.add(new CMIField("cmi.score.scaled", "Overall Scaled Score"));
		list.add(new CMIField("cmi.completion_status", "Completion Status"));
		list.add(new CMIField("cmi.success_status", "Success Status"));
		list.add(new CMIField("cmi.completion_threshold", "Completion Threshold"));
		list.add(new CMIField("cmi.progress_measure", "Progress Measure"));
		
		return group;		
	}
	
	private CMIFieldGroup getDefaultFieldGroup() {
		CMIFieldGroup group = new CMIFieldGroup();
		
		List<CMIField> list = group.getList();
		list.add(new CMIField("cmi.completion_status", "Completion Status"));
		list.add(new CMIField("cmi.completion_threshold", "Completion Threshold"));
		list.add(new CMIField("cmi.credit", "Will credit be given?"));
		list.add(new CMIField("cmi.entry", "Has user previously accessed this sco?"));
		list.add(new CMIField("cmi.exit", "Exit Type"));
		// Interactions?
		list.add(new CMIField("cmi.launch_data", "Initialization launch data"));
		list.add(new CMIField("cmi.learner_id", "Learner Id"));
		list.add(new CMIField("cmi.learner_name", "Learner Name"));
		// Learner preferences?
		list.add(new CMIField("cmi.location", "Current location in the sco"));
		list.add(new CMIField("cmi.max_time_allowed", "Maximum time allowed to use sco"));
		list.add(new CMIField("cmi.mode", "Mode"));
		list.add(new CMIField("cmi.progress_measure", "Progress Measure"));
		list.add(new CMIField("cmi.scaled_passing_score", "Scaled passing score required"));
		list.add(new CMIField("cmi.score.scaled", "Overall Scaled Score"));
		list.add(new CMIField("cmi.score.raw", "Overall Raw Score"));
		list.add(new CMIField("cmi.score.min", "Minimum Raw Score"));
		list.add(new CMIField("cmi.score.max", "Maximum Raw Score"));
		list.add(new CMIField("cmi.session_time", "Current learner session time"));
		list.add(new CMIField("cmi.success_status", "Success Status"));
		list.add(new CMIField("cmi.suspend_data", "Suspend data"));
		list.add(new CMIField("cmi.time_limit_action", "Action when time expires"));
		list.add(new CMIField("cmi.total_time", "Sum of learner's session times in attempt"));
		
		list.add(new CMIField("cmi.timestamp", "Timestamp"));
		list.add(new CMIField("cmi.comments_from_learner", "Comments from Learner"));
		list.add(new CMIField("cmi.objectives._count", "Objective count"));
		list.add(new CMIField("cmi.interactions._count", "Interaction count"));
		
		
		CMIField objectives = new CMIField("cmi.objectives", "Objectives");
		objectives.addChild(new CMIField("id", "Objective Id"));
		objectives.addChild(new CMIField("score.scaled", "Scaled Score"));
		
		list.add(objectives);
		
		return group;
	}
	
	
	
	
	

	
}
