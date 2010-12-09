package org.sakaiproject.scorm.ui.player.behaviors;

import org.adl.api.ecmascript.APIErrorCodes;
import org.adl.api.ecmascript.SCORM13APIInterface;
import org.adl.datamodels.DMInterface;
import org.adl.datamodels.DMProcessingInfo;
import org.adl.datamodels.IDataManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.scorm.model.api.ScoBean;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.navigation.INavigable;
import org.sakaiproject.scorm.navigation.INavigationEvent;
import org.sakaiproject.scorm.service.api.ScormApplicationService;
import org.sakaiproject.scorm.service.api.ScormSequencingService;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.ToolManager;

public abstract class SCORM13API implements SCORM13APIInterface {

	private static Log log = LogFactory.getLog(SCORM13API.class);
	
	// String value of FALSE for JavaScript returns.
	protected static final String STRING_FALSE = "false";
	// String value of TRUE for JavaScript returns.
	protected static final String STRING_TRUE = "true";
	
	public abstract SessionBean getSessionBean();
	
	public abstract ScormApplicationService getApplicationService();
	
	public abstract ScormSequencingService getSequencingService();
	public abstract GradebookExternalAssessmentService getGradebookExternalAssessmentService();
	
	public abstract ScoBean getScoBean();
	
	public abstract INavigable getAgent();
	
	public abstract Object getTarget();
	
	
	// Implementation of SCORM13APIInterface
	public String Commit(String parameter) {
		// TODO: Disable UI controls -- or throttle them on server -- don't mess with js
		
		// Assume failure
		String result = STRING_FALSE;

		if (null == getSessionBean()) {
			log.error("Null run state!");
		}
		
		if (getApplicationService().commit(parameter, getSessionBean(), getScoBean()))
			result = STRING_TRUE;
		
		// TODO: Enable UI controls
		
		return result;
	}
	
	public String GetDiagnostic(String errorCode) {
		return getApplicationService().getDiagnostic(errorCode, getSessionBean());
	}

	public String GetErrorString(String errorCode) {
		return getApplicationService().getErrorString(errorCode, getSessionBean());
	}

	public String GetLastError() {
		return getApplicationService().getLastError(getSessionBean());
	}
	
	public String GetValue(String parameter) {
		return getApplicationService().getValue(parameter, getSessionBean(), getScoBean());
	}

	public String Initialize(String parameter) {
		// Assume failure
		String result = STRING_FALSE;

		if (getApplicationService().initialize(parameter, getSessionBean(), getScoBean()))
			result = STRING_TRUE;
		
		return result;
	}	
	
	public String SetValue(String dataModelElement, String value) {
		// Assume failure
		String result = STRING_FALSE;
				
		if (getApplicationService().setValue(dataModelElement, value, getSessionBean(), getScoBean())) {
			result = STRING_TRUE;
		}

		return result;
	}
	
	
	public String Terminate(String parameter) {
		// Assume failure
		String result = STRING_FALSE;
		
		if (null == getSessionBean()) {
			log.error("Null run state!");
			return result;
		}
		
		INavigationEvent navigationEvent = getApplicationService().newNavigationEvent();
		
		boolean isSuccessful = getApplicationService().terminate(parameter, navigationEvent, 
				getSessionBean(), getScoBean());
		
		if (isSuccessful) {
			result = STRING_TRUE;

			if (navigationEvent.isChoiceEvent()) { 
				getSequencingService().navigate(navigationEvent.getChoiceEvent(), getSessionBean(), getAgent(), getTarget());
			} else { 
				getSequencingService().navigate(navigationEvent.getEvent(), getSessionBean(), getAgent(), getTarget());
			}
			
			synchResultWithGradebook();
			
		} 

		return result;
	}

	protected void synchResultWithGradebook() {
		ScoBean displayingSco = getSessionBean().getDisplayingSco();
		IDataManager dataManager = displayingSco.getDataManager();
		String mode = getValueAsString("cmi.mode", dataManager); // passed, failed, unknown
		String credit = getValueAsString("cmi.credit", dataManager); // credit, no_credit 
		String completionStatus = getValueAsString("cmi.completion_status", dataManager); // (completed, incomplete, not_attempted, unknown)
		double score = getRealValue("cmi.score.scaled", dataManager) * 100d; // A real number with values that is accurate to seven significant decimal figures. The value shall be in the range of Ð1.0 to 1.0, inclusive.
		if ("normal".equals(mode) && "completed".equals(completionStatus) && "credit".equals(credit)) {
			Placement placement = ToolManager.getCurrentPlacement();
			String context = placement.getContext();
			long contentPackageId = getSessionBean().getContentPackage().getContentPackageId();
			String assessmentExternalId = ""+contentPackageId+":"+dataManager.getScoId();
			if (getGradebookExternalAssessmentService().isExternalAssignmentDefined(context, assessmentExternalId)) {
				getGradebookExternalAssessmentService().updateExternalAssessmentScore(context, assessmentExternalId, getSessionBean().getLearnerId(), "" + score);
			}
		}
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

}
