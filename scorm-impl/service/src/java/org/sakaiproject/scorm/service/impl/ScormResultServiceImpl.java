package org.sakaiproject.scorm.service.impl;

import java.util.List;
import java.util.Properties;

import org.adl.api.ecmascript.APIErrorCodes;
import org.adl.datamodels.DMInterface;
import org.adl.datamodels.DMProcessingInfo;
import org.adl.datamodels.IDataManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.scorm.dao.api.AttemptDao;
import org.sakaiproject.scorm.dao.api.DataManagerDao;
import org.sakaiproject.scorm.model.api.Attempt;
import org.sakaiproject.scorm.model.api.CMIField;
import org.sakaiproject.scorm.model.api.CMIFieldGroup;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormResultService;
import org.sakaiproject.user.api.UserDirectoryService;

public abstract class ScormResultServiceImpl implements ScormResultService {

	private static Log log = LogFactory.getLog(ScormResultServiceImpl.class);
	
	private static String[] fields = {"cmi.completion_status", "cmi.score.scaled", "cmi.success_status" };
	
	// Dependency injection method lookup signatures
	protected abstract DataManagerDao dataManagerDao();
	protected abstract AttemptDao attemptDao();
	
	public CMIFieldGroup getAttemptResults(Attempt attempt) {
		
		IDataManager dataManager = dataManagerDao().load(attempt.getDataManagerId());
		
		CMIFieldGroup group = getDefaultFieldGroup();
		List<CMIField> list = group.getList();
		
		for (CMIField field : list) {
			populateValues(field, dataManager);
		}
		
		return group;
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
		
		IDataManager dataManager = dataManagerDao().load(attempt.getDataManagerId());
		
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
	
	public List<Attempt> getAttempts(long contentPackageId, String learnerId) {
		return attemptDao().find(contentPackageId, learnerId);
	}
	
	public List<Attempt> getAttempts(String courseId, String learnerId) {
		return attemptDao().find(courseId, learnerId);
	}
	
	
	public List<Attempt> getAttempts(long contentPackageId) {
		return attemptDao().find(contentPackageId);
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

	
	private CMIFieldGroup getDefaultFieldGroup() {
		CMIFieldGroup group = new CMIFieldGroup();
		
		List<CMIField> list = group.getList();
		list.add(new CMIField("cmi.entry", "Access"));
		list.add(new CMIField("cmi.exit", "Exit Type"));
		list.add(new CMIField("cmi.learner_id", "Learner Id"));
		
		CMIField objectives = new CMIField("cmi.objectives", "Objectives");
		objectives.addChild(new CMIField("id", "Objective Id"));
		objectives.addChild(new CMIField("score.scaled", "Scaled Score"));
		
		list.add(objectives);
		
		return group;
	}
	
	
	
	
	

	
}
