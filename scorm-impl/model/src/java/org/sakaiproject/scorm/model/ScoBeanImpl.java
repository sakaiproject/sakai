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
package org.sakaiproject.scorm.model;

import org.adl.api.ecmascript.IErrorManager;
import org.adl.datamodels.DMErrorCodes;
import org.adl.datamodels.IDataManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.scorm.model.api.ScoBean;
import org.sakaiproject.scorm.model.api.SessionBean;

public class ScoBeanImpl implements ScoBean {
	
	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(ScoBeanImpl.class);
		
	// String value of FALSE for JavaScript returns.
	protected static final String STRING_FALSE = "false";
	// String value of TRUE for JavaScript returns.
	protected static final String STRING_TRUE = "true";
	
	// Indicates if the SCO is in a 'terminated' state.
	protected boolean mTerminateCalled = false;
	
	private boolean isInitialized = false;
	
	private boolean isSuspended = false;
	
	private boolean isTerminated = false;
	
	private int version;
	
	private IDataManager dataManager;

	// The public version attribute of the SCORM API.
	//public static final String version = "1.0";	
	
	private String scoId;
	
	private SessionBean sessionBean;
	
	public ScoBeanImpl(String scoId, SessionBean sessionBean) {
		this.scoId = scoId;
		this.sessionBean = sessionBean;
	}
	
	public String getScoId() {
		return scoId;
	}
	
	public IDataManager getDataManager() {
		return dataManager;
	}

	public void setDataManager(IDataManager dataManager) {
		this.dataManager = dataManager;
	}
	
	/*public String Commit(String parameter) {
		if (log.isDebugEnabled())
			log.debug("API Commit (argument): " + parameter);
		
		// TODO: Disable UI controls -- or throttle them on server -- don't mess with js
		
		// Assume failure
		String result = STRING_FALSE;
		
		IErrorManager errorManager = sessionBean.getErrorManager();
		
		if (null == sessionBean) {
			log.error("Null run state!");
			errorManager.setCurrentErrorCode(APIErrorCodes.GENERAL_EXCEPTION);
		}
		
		if (scormApplicationService.commit(parameter, sessionBean, errorManager))
			result = STRING_TRUE;
		
		// TODO: Enable UI controls
		
		if (log.isDebugEnabled())
			log.debug("API Commit (result): " + result);
		
		return result;
	}
	
	public String GetDiagnostic(String iErrorCode) {
		if (log.isDebugEnabled())
			log.debug("API GetDiagnostic (argument): " + iErrorCode);
		
		IErrorManager errorManager = sessionBean.getErrorManager();
		String result = errorManager.getErrorDiagnostic(iErrorCode);
		
		if (log.isDebugEnabled())
			log.debug("API GetDiagnostic (result): " + result);
		return result;
	}

	public String GetErrorString(String iErrorCode) {
		if (log.isDebugEnabled())
			log.debug("API GetErrorString (argument): " + iErrorCode);
		
		IErrorManager errorManager = sessionBean.getErrorManager();
		String result = errorManager.getErrorDescription(iErrorCode);
		
		if (log.isDebugEnabled())
			log.debug("API GetErrorString (result): " + result);
		return result;
	}

	public String GetLastError() {
		if (log.isDebugEnabled())
			log.debug("API GetLastError ");
		IErrorManager errorManager = sessionBean.getErrorManager();
		String result = errorManager.getCurrentErrorCode();
		
		if (!result.equals("0") && log.isWarnEnabled()) {
			String description = errorManager.getErrorDescription(result);
			log.warn("API threw an error: " + description);
		}
		
		if (log.isDebugEnabled())
			log.debug("API GetLastError (result): " + result);
		return result;
	}
	
	public String GetValue(String parameter) {
		if (log.isDebugEnabled())
			log.debug("API GetValue (argument): " + parameter);
		
		String result = "";
		IErrorManager errorManager = sessionBean.getErrorManager();
		
		if (null == sessionBean) {
			log.error("Null run state!");
			errorManager.setCurrentErrorCode(APIErrorCodes.GENERAL_EXCEPTION);
			return result;
		} 
		
		// already terminated
	    if ( isTerminated() ) {
	    	errorManager.setCurrentErrorCode(APIErrorCodes.GET_AFTER_TERMINATE);
	        return result;
	    }
	      
	    if ( parameter.length() == 0 ) {
	    	errorManager.setCurrentErrorCode(DMErrorCodes.GEN_GET_FAILURE);
	        return result;
	    }

	    if ( isInitialized() ) {
		
			result = scormApplicationService.getValue(parameter, sessionBean, errorManager);
	
		    if (log.isDebugEnabled())
		    	log.debug("API GetValue (result): " + result);

	    } else {
	    	errorManager.setCurrentErrorCode(APIErrorCodes.GET_BEFORE_INIT);
	    }
	    
	    return result;
	}

	public String Initialize(String parameter) {
		if (log.isDebugEnabled())
			log.debug("API Initialize (argument): " + parameter);
		
		// Assume failure
		String result = STRING_FALSE;
		
		IErrorManager errorManager = sessionBean.getErrorManager();
		
		if (isTerminated()) {
			errorManager.setCurrentErrorCode(APIErrorCodes.CONTENT_INSTANCE_TERMINATED);
			return result;
		}
		
		setTerminated(false);
		
		mSCO_VER_2 = false;
		mSCO_VER_3 = true;
				
		if ((parameter == null || parameter.equals("")) != true) 
			errorManager.setCurrentErrorCode(DMErrorCodes.GEN_ARGUMENT_ERROR);
		else if (isInitialized) 
			// If the SCO is already initialized set the appropriate error code
			errorManager.setCurrentErrorCode(APIErrorCodes.ALREADY_INITIALIZED);
		else {
			sessionBean.setSuspended(false);
			
			IDataManager dm = scormApplicationService.initialize(sessionBean, errorManager, mNumAttempts);
			
			if (dm != null) {
				result = STRING_TRUE;

				sessionBean.setDataManager(dm);
				
				setInitialized(true);
			}
		}		
			
		if (log.isDebugEnabled())
			log.debug("API Initialize (result): " + result);
		
		return result;
	}	
	
	public String SetValue(String dataModelElement, String value) {
		if (log.isDebugEnabled())
			log.debug("API SetValue (arguments): " + dataModelElement + ", " + value);
		
		// Assume failure
		String result = STRING_FALSE;
		IErrorManager errorManager = sessionBean.getErrorManager();
		
		if (null == sessionBean) {
			log.error("Null run state!");
			errorManager.setCurrentErrorCode(APIErrorCodes.GENERAL_EXCEPTION);
			return result;
		}
		
		// already terminated
		if (isTerminated()) {
			errorManager.setCurrentErrorCode(APIErrorCodes.SET_AFTER_TERMINATE);
			return result;
		}

		// Clear any existing error codes
		errorManager.clearCurrentErrorCode();

		if (!isInitialized()) {
			// not initialized
			errorManager.setCurrentErrorCode(APIErrorCodes.SET_BEFORE_INIT);
			return result;
		}
		
		if (scormApplicationService.setValue(dataModelElement, value, sessionBean, errorManager)) {
			result = STRING_TRUE;
		}

		if (log.isDebugEnabled())
			log.debug("API SetValue (result): " + result);
		
		return result;
	}
	
	
	public String Terminate(String iParam) {
		return Terminate(iParam, null, null);
	}

	public String Terminate(String iParam, INavigable agent, Object target) {
		if (log.isDebugEnabled())
			log.debug("API Terminate (argument): " + iParam);


		// Assume failure
		String result = STRING_FALSE;
		IErrorManager errorManager = sessionBean.getErrorManager();
		
		if (null == sessionBean) {
			log.error("Null run state!");
			errorManager.setCurrentErrorCode(APIErrorCodes.GENERAL_EXCEPTION);
			return result;
		}
		
		INavigationEvent navigationEvent = scormApplicationService.newNavigationEvent();
		
		boolean isSuccessful = scormApplicationService.terminate(iParam, navigationEvent, sessionBean, errorManager, isInitialized(), isTerminated(), sessionBean.isSuspended());
		
		setTerminated(true);
		
		if (isSuccessful) {
			result = STRING_TRUE;
			setInitialized(false);
			
			//String exitValue = clientFacade.applicationProgrammingInterface().getExitValue();
			
			if (navigationEvent.isChoiceEvent()) 
				scormSequencingService.navigate(navigationEvent.getChoiceEvent(), sessionBean, agent, target);
			else
				scormSequencingService.navigate(navigationEvent.getEvent(), sessionBean, agent, target);
			
		} 
		
		
		if (log.isDebugEnabled())
			log.debug("API Terminate (result): " + result);

		return result;
	}*/
	
	
	
	/*
	
	
	private String old_Commit(String iParam) {
		if (log.isDebugEnabled())
			log.debug("API Commit (argument): " + iParam);
		
		// Assume failure
		String result = STRING_FALSE;

		// Disable UI Controls
		setUIState(false);

		// Make sure param is empty string "" - as per the API spec
		// Check for "null" is a workaround described in "Known Problems"
		// in the header.
		if ((iParam == null || iParam.equals("")) != true) {
			log.warn("Non-null or empty param passed to commit");
			errorManager.setCurrentErrorCode(DMErrorCodes.GEN_ARGUMENT_ERROR);
		} else {
			if (!isInitialized()) {
				log.warn("Attempting to commit prior to initialization");
				// LMS is not initialized
				errorManager.setCurrentErrorCode(APIErrorCodes.COMMIT_BEFORE_INIT);
				return result;
			} else if (isTerminated()) {
				log.warn("Attempting to commit after termination");
				// LMS is terminated
				errorManager.setCurrentErrorCode(APIErrorCodes.COMMIT_AFTER_TERMINATE);
				return result;
			} else {				
				IValidRequests validRequests = clientFacade.commit(runState);
				
				if (validRequests == null) {
					log.warn("Valid requests object is null");
					errorManager.setCurrentErrorCode(APIErrorCodes.GENERAL_EXCEPTION);
				} else if (null == runState) {
					log.error("Null runstate!");
					errorManager.setCurrentErrorCode(APIErrorCodes.GENERAL_EXCEPTION);
				} else if (null == runState.getDataManager()) {
					log.error("Null data manager!");
					errorManager.setCurrentErrorCode(APIErrorCodes.GENERAL_EXCEPTION);
				} else {
					errorManager.clearCurrentErrorCode();

					result = STRING_TRUE;

					IDataManager scoDataManager = runState.getDataManager();
					
					SCORM_2004_NAV_DM navDM = (SCORM_2004_NAV_DM) runState.getDataManager().getDataModel("adl");

					// Update the ADLValidRequests object from the servlet
					// response object.
					navDM.setValidRequests(validRequests);
				}
			}
		}

		// Enable UI Controls
		//setUIState(true);

		// Refresh the Menu frame
		//jsCall("refreshMenu()");

		if (log.isDebugEnabled())
			log.debug("API Commit (result): " + result);
		return result;
	}
	
	
	public String old_GetValue(String iDataModelElement) {
		if (log.isDebugEnabled())
			log.debug("API GetValue (argument): " + iDataModelElement);
		
		String result = "";

		// already terminated
	    if ( isTerminated() ) {
	    	errorManager.setCurrentErrorCode(APIErrorCodes.GET_AFTER_TERMINATE);
	        return result;
	    }
	      
	    if ( iDataModelElement.length() == 0 ) {
	    	errorManager.setCurrentErrorCode(DMErrorCodes.GEN_GET_FAILURE);
	        return result;
	    }


	    if ( isInitialized() ) {
	    	// Clear current error codes
	        errorManager.clearCurrentErrorCode();

	        // Process 'GET'
	        DMProcessingInfo dmInfo = new DMProcessingInfo();
	        
	        int dmErrorCode = 0;
	        dmErrorCode = DMInterface.processGetValue(iDataModelElement, false, runState.getDataManager(), dmInfo);

	        // Set the LMS Error Manager from the Data Model Error Manager
	        errorManager.setCurrentErrorCode(dmErrorCode);

	        if ( dmErrorCode == APIErrorCodes.NO_ERROR ) {
	        	result = dmInfo.mValue;
	        } else {
	            result = new String("");
	        }
	    } else {
	    	errorManager.setCurrentErrorCode(APIErrorCodes.GET_BEFORE_INIT);
	    }
	    
	    if (log.isDebugEnabled())
	    	log.debug("API GetValue (result): " + result);

	    return result;
	}

	public String old_Initialize(String iParam) {
		if (log.isDebugEnabled())
			log.debug("API Initialize (argument): " + iParam);
		// Assume failure
		String result = STRING_FALSE;

		if (isTerminated()) {
			errorManager.setCurrentErrorCode(APIErrorCodes.CONTENT_INSTANCE_TERMINATED);
			return result;
		}

		setTerminated(false);
		mTerminateCalled = false;

		mSCO_VER_2 = false;
		mSCO_VER_3 = true;

		// Make sure param is empty string "" - as per the API spec
		// Check for "null" is a workaround described in "Known Problems"
		// in the header.
		//String tempParm = String.valueOf(iParam);

		if ((iParam == null || iParam.equals("")) != true) 
			errorManager.setCurrentErrorCode(DMErrorCodes.GEN_ARGUMENT_ERROR);
		else if (isInitialized()) 
			// If the SCO is already initialized set the appropriate error code
			errorManager.setCurrentErrorCode(APIErrorCodes.ALREADY_INITIALIZED);
		else {
			runState.setSuspended(false);
			
			clientFacade.initialize(runState, String.valueOf(mNumAttempts));
			
			setInitialized(true);

			// No errors were detected
			errorManager.clearCurrentErrorCode();

			result = STRING_TRUE;
		}

		if (log.isDebugEnabled())
			log.debug("API Initialize (result): " + result);
		return result;
	}

	public String old_SetValue(String iDataModelElement, String iValue) {
		if (log.isDebugEnabled())
			log.debug("API SetValue (arguments): " + iDataModelElement + ", " + iValue);
		
		// Assume failure
		String result = STRING_FALSE;

		// already terminated
		if (isTerminated()) {
			errorManager.setCurrentErrorCode(APIErrorCodes.SET_AFTER_TERMINATE);
			return result;
		}

		// Clear any existing error codes
		errorManager.clearCurrentErrorCode();

		if (!isInitialized()) {
			// not initialized
			errorManager.setCurrentErrorCode(APIErrorCodes.SET_BEFORE_INIT);
			return result;
		}

		String setValue = iValue;
		if (setValue == null)
			setValue = "";
		
		
		// Send off
		if (null == runState || null == runState.getDataManager()) {
			log.error("Null runstate or data manager!");
			return result;
		}
		
		// Process 'SET'
		int dmErrorCode = 0;
		dmErrorCode = DMInterface.processSetValue(iDataModelElement, setValue, false, runState.getDataManager());

		// Set the LMS Error Manager from the DataModel Manager
		errorManager.setCurrentErrorCode(dmErrorCode);

		if (errorManager.getCurrentErrorCode().equals("0")) {
			// Successful Set
			result = STRING_TRUE;
		}

		// TODO: Check to see if this is necessary
		// clear MessageCollection
		//MessageCollection mc = MessageCollection.getInstance();
		//mc.clear();

		if (log.isDebugEnabled())
			log.debug("API SetValue (result): " + result);
		
		return result;
	}
	
	public String old_Terminate(String iParam) {
		return Terminate(iParam, null);
	}

	public String old_Terminate(String iParam, AjaxRequestTarget target) {
		if (log.isDebugEnabled())
			log.debug("API Terminate (argument): " + iParam);

		mTerminateCalled = true;
		// Assume failure
		String result = STRING_FALSE;

		// already terminated
		if (isTerminated()) {
			errorManager.setCurrentErrorCode(APIErrorCodes.TERMINATE_AFTER_TERMINATE);
			return result;
		}
		if (!isInitialized()) {
			errorManager.setCurrentErrorCode(APIErrorCodes.TERMINATE_BEFORE_INIT);
			return result;
		}

		// Make sure param is empty string "" - as per the API spec
		// Check for "null" is a workaround described in "Known Problems"
		// in the header.
		if ((iParam == null || iParam.equals("")) != true) {
			errorManager.setCurrentErrorCode(DMErrorCodes.GEN_ARGUMENT_ERROR);
		} else if (null == runState) {
			log.error("Null runstate!");
			errorManager.setCurrentErrorCode(APIErrorCodes.GENERAL_EXCEPTION);
		} else if (null == runState.getDataManager()) {
			log.error("Null data manager!");
			errorManager.setCurrentErrorCode(APIErrorCodes.GENERAL_EXCEPTION);
		} else {
			// check if adl.nav.request is equal to suspend all, or if the
			// suspend button was pushed, set cmi.exit equal to suspend.
			SCORM_2004_NAV_DM navDM = (SCORM_2004_NAV_DM) runState.getDataManager().getDataModel("adl");
			int event = navDM.getNavEvent();

			if (event == SeqNavRequests.NAV_SUSPENDALL || runState.isSuspended()) {
				// Before we change the cmi.exit, make sure the SCO didn't set it to log-out.
	            DMProcessingInfo pi = new DMProcessingInfo();
	            int check = DMInterface.processGetValue("cmi.exit", true, runState.getDataManager(), pi);
	            
	            if ( check != 0 || !pi.mValue.equals("log-out") )
	            {
	            	// Process 'SET' on cmi.exit
	            	DMInterface.processSetValue("cmi.exit", "suspend", true, runState.getDataManager());
	            }
	        }

			if (!(event == SeqNavRequests.NAV_ABANDON || event == SeqNavRequests.NAV_ABANDONALL)) {
				result = Commit("");
			} else {
				// The attempt has been abandoned, so don't persist the data
				result = STRING_TRUE;
			}

			setTerminated(true);

			if (!result.equals(STRING_TRUE)) {
				// General Commit Failure
				errorManager.setCurrentErrorCode(APIErrorCodes.GENERAL_COMMIT_FAILURE);
			} else {
				setInitialized(false);

				// get value of "exit"
				DMProcessingInfo dmInfo = new DMProcessingInfo();
				int dmErrorCode = 0;
				dmErrorCode = DMInterface.processGetValue("cmi.exit", true, true, runState.getDataManager(), dmInfo);
				String exitValue = dmInfo.mValue;
				//String tempEvent = "_none_";
				boolean isChoice = false;
				String evalValue = null;

				if (dmErrorCode == APIErrorCodes.NO_ERROR) {
					exitValue = dmInfo.mValue;
				} else {
					exitValue = new String("");
				}

				if (exitValue.equals("time-out") || exitValue.equals("logout")) {
					event = SeqNavRequests.NAV_EXITALL;
				} 

				// handle if sco set nav.request
				if (!runState.isSuspended() && event != SeqNavRequests.NAV_NONE) {
					if (event != -1)
						runState.navigate(event, target);
					else {
						// It's a choice event
						String choiceEvent = navDM.getChoiceEvent();
						
						if (null != choiceEvent)
							runState.navigate(choiceEvent, target);
					}
				}
			}
		}
		
		if (log.isDebugEnabled())
			log.debug("API Terminate (result): " + result);

		return result;
	}*/
	
	public void setInitialized(boolean isInitialized) {
		this.isInitialized = isInitialized;
	}
	
	public boolean isInitialized() {
		IErrorManager errorManager = sessionBean.getErrorManager();
		if ( ( !isTerminated ) &&  ( version == ScoBean.SCO_VERSION_2 ) )
			errorManager.setCurrentErrorCode(DMErrorCodes.GEN_GET_FAILURE);

		return isInitialized;
	}
	
	public void setTerminated(boolean isTerminated) {
		this.isTerminated = isTerminated;
	}
	
	public boolean isTerminated() {
		return isTerminated;
	}
	
	public void setVersion(int version) {
		this.version = version;
	}
	
	
	/**
	 * This method implements the interface with the Java Script running on the
	 * client side of the Sample RTE. <br>
	 * <br>
	 * <br>
	 * <br>
	 * 
	 * @param message
	 *            The String that is evaluated by the Java Script eval
	 *            command--usually it is a Java Script function name.<br>
	 *            <br>
	 * 
	 * 
	 */
	public void jsCall(String message) {	   
	    // TODO: Find a way to communicate back to the browser here  
		//JSObject.getWindow(this).eval(message);
		log.warn("Called jsCall with message: " + message);
	}
	
	/**
	 * Toggles the state of the LMS provided UI controls.
	 * 
	 * @param iState
	 *            <code>true</code> if the controls should be enabled, or
	 *            <code>false</code> if the controls should be disabled.
	 */
	private void setUIState(boolean iState) {
		String evalCmd = "setUIState(" + iState + ");";
		jsCall(evalCmd);
	}
	

	/*public void setNumAttempts(long iNumAttempts) {
		mNumAttempts = iNumAttempts;
	}

	public void setNumAttempts(String iNumAttempts) {
		Long tempLong = new Long(iNumAttempts);
		mNumAttempts = tempLong.longValue();
	}*/

	/**
	 * Clears error codes and sets mInitialedState and mTerminated State to
	 * default values.
	 */
	public void clearState() {
		setInitialized(false);
		setTerminated(false);
		mTerminateCalled = false;
		IErrorManager errorManager = sessionBean.getErrorManager();
		if (errorManager != null)
			errorManager.clearCurrentErrorCode();
	}
	
	/**
	 * Insert a backward slash (\) before each double quote (") or backslash (\)
	 * to allow the character to be displayed in the data model log. Receives
	 * the value and returns the newly formatted value
	 */
	public String formatValue(String baseString) {
		int indexQuote = baseString.indexOf("\"");
		int indexSlash = baseString.indexOf("\\");

		if (indexQuote >= 0 || indexSlash >= 0) {
			int index = 0;
			String temp = new String();
			String strFirst = new String();
			String strLast = new String();
			char insertValue = '\\';

			while (index < baseString.length()) {
				if ((baseString.charAt(index) == '\"')
						|| (baseString.charAt(index) == '\\')) {
					strFirst = baseString.substring(0, index);
					strLast = baseString.substring(index, baseString.length());
					baseString = strFirst.concat(
							Character.toString(insertValue)).concat(strLast);
					index += 2;
				} else {
					index++;
				}
			}
		}
		return baseString;
	}
	
	
	/*public void suspendButtonPushed() {
		if (isInitialized()) {
			sessionBean.setSuspended(true);
			Terminate("");
		}
	}*/

	public boolean isSuspended() {
		return isSuspended;
	}

	public void setSuspended(boolean isSuspended) {
		this.isSuspended = isSuspended;
	}	
	
	
}
