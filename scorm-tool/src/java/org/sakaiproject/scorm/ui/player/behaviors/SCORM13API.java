package org.sakaiproject.scorm.ui.player.behaviors;

import org.adl.api.ecmascript.SCORM13APIInterface;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.scorm.model.api.ScoBean;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.navigation.INavigable;
import org.sakaiproject.scorm.navigation.INavigationEvent;
import org.sakaiproject.scorm.service.api.ScormApplicationService;
import org.sakaiproject.scorm.service.api.ScormSequencingService;

public abstract class SCORM13API implements SCORM13APIInterface {

	private static Log log = LogFactory.getLog(SCORM13API.class);
	
	// String value of FALSE for JavaScript returns.
	protected static final String STRING_FALSE = "false";
	// String value of TRUE for JavaScript returns.
	protected static final String STRING_TRUE = "true";
	
	public abstract SessionBean getSessionBean();
	
	public abstract ScormApplicationService getApplicationService();
	
	public abstract ScormSequencingService getSequencingService();
	
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

			if (navigationEvent.isChoiceEvent()) 
				getSequencingService().navigate(navigationEvent.getChoiceEvent(), getSessionBean(), getAgent(), getTarget());
			else 
				getSequencingService().navigate(navigationEvent.getEvent(), getSessionBean(), getAgent(), getTarget());
			
		} 

		return result;
	}

}
