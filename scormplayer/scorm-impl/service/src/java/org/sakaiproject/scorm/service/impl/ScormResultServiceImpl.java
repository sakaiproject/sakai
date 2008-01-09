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
import org.sakaiproject.scorm.dao.api.SeqActivityDao;
import org.sakaiproject.scorm.model.api.ActivityReport;
import org.sakaiproject.scorm.model.api.ActivitySummary;
import org.sakaiproject.scorm.model.api.ActivitySummary;
import org.sakaiproject.scorm.model.api.Attempt;
import org.sakaiproject.scorm.model.api.CMIData;
import org.sakaiproject.scorm.model.api.CMIField;
import org.sakaiproject.scorm.model.api.CMIFieldGroup;
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
	protected abstract SeqActivityDao seqActivityDao();
	
	
	public ActivityReport getActivityReport(long contentPackageId, String activityId, String learnerId, long attemptNumber) {
		ActivityReport report = new ActivityReport();
		
		IDataManager dataManager = dataManagerDao().findByActivityId(contentPackageId, activityId, learnerId, attemptNumber);
		
		//Map<String,CMIData> map = this.getCMIDataMap(dataManager);
		
		report.setActivityId(activityId);
		report.setTitle(dataManager.getTitle());
		report.setScoId(dataManager.getScoId());
		report.setCmiData(getCMIData(dataManager));
		
		mapValues(report, dataManager);
		
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
			
			mapValues(report, dataManager);
			
			reports.add(report);
		}
		
		return reports;
	}
	
	
	public List<ActivitySummary> getActivitySummaries(long contentPackageId, String learnerId, long attemptNumber) {
		List<IDataManager> dataManagers = dataManagerDao().find(contentPackageId, learnerId, attemptNumber);
		List<ActivitySummary> summaries = new LinkedList<ActivitySummary>();
		
		for (IDataManager dataManager : dataManagers) {
			ActivitySummary summary = new ActivitySummary();
			Map<String,CMIData> map = this.getCMIDataMap(dataManager);
		
			summary.setTitle(dataManager.getTitle());
			summary.setScoId(dataManager.getScoId());
			
			mapValues(summary, dataManager);
			
			summaries.add(summary);
		}
		
		return summaries;
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
	
	
	private void mapValues(ActivityReport report, IDataManager dataManager) {
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
		
		List<Objective> objectives = report.getObjectives();
		
		int numberOfObjectives = getValueAsInt("cmi.objectives._count", dataManager);
		
		for (int i=1;i<=numberOfObjectives;i++) {
			Objective objective = new Objective();
			
			String objectiveName = new StringBuilder("cmi.objectives.").append(i).toString();
			
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
			
			objectives.add(objective);
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
		
		for (Learner learner : learners) {
			LearnerExperience experience = new LearnerExperience(learner, contentPackageId);
			
			List<Attempt> attempts = getAttempts(contentPackageId, learner.getId());
			
			if (attempts != null) {
				if (attempts.size() > 0) {
					// Grab the latest attempt
					Attempt latestAttempt = attempts.get(0);
					
					List<CMIData> data = getSummaryCMIData(latestAttempt);
				}
			
				experience.setNumberOfAttempts(attempts.size());
			}
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
