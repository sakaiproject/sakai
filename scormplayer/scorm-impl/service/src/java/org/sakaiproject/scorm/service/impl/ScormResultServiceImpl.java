package org.sakaiproject.scorm.service.impl;

import java.util.LinkedList;
import java.util.List;

import org.adl.api.ecmascript.APIErrorCodes;
import org.adl.datamodels.DMInterface;
import org.adl.datamodels.DMProcessingInfo;
import org.adl.datamodels.IDataManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.scorm.dao.api.DataManagerDao;
import org.sakaiproject.scorm.model.api.Attempt;
import org.sakaiproject.scorm.service.api.ScormResultService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

public abstract class ScormResultServiceImpl implements ScormResultService {

	private static Log log = LogFactory.getLog(ScormResultServiceImpl.class);
	
	protected DataManagerDao dataManagerDao;
	protected abstract UserDirectoryService userDirectoryService();
	
	public Attempt lookupAttempt(String courseId, String learnerId, int attemptNumber) {
		
		IDataManager dataManager = dataManagerDao.find(courseId, learnerId);
		
		String completionStatus = getValue(dataManager, "cmi.completion_status");
		String scaledScore = getValue(dataManager, "cmi.score.scaled");
		String successStatus = getValue(dataManager, "cmi.success_status");
		
		Attempt attempt = new Attempt();
		attempt.setCompletionStatus(completionStatus);
		attempt.setScoreScaled(scaledScore);
		attempt.setSuccessStatus(successStatus);
		
		return attempt;
	}
	
	
	public List<Attempt> getAttempts(String courseId) {
		List<IDataManager> dataManagers = dataManagerDao.find(courseId);
		List<Attempt> list = new LinkedList<Attempt>();
		
		for (IDataManager dataManager : dataManagers) {
			Attempt attempt = new Attempt();
			String learnerId = dataManager.getUserId();
			attempt.setLearnerId(learnerId);
			attempt.setAttemptNumber(1);
			
			try {
				User user = userDirectoryService().getUser(learnerId);
				
				if (user != null) 
					attempt.setLearnerName(user.getDisplayName());
				
			} catch (UserNotDefinedException e) {
				log.warn("The current learner is not a valid user according to Sakai.", e);
			}
			
			list.add(attempt);
		}
		
		return list;
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
	
	private String getValue(IDataManager dataManager, String iDataModelElement) {
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



	public DataManagerDao getDataManagerDao() {
		return dataManagerDao;
	}



	public void setDataManagerDao(DataManagerDao dataManagerDao) {
		this.dataManagerDao = dataManagerDao;
	}
	
}
