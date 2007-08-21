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
package org.sakaiproject.scorm.tool;

import java.io.Serializable;

import org.adl.api.ecmascript.APIErrorCodes;
import org.adl.api.ecmascript.IErrorManager;
import org.adl.api.ecmascript.SCORM13APIInterface;
import org.adl.datamodels.DMErrorCodes;
import org.adl.datamodels.DMInterface;
import org.adl.datamodels.DMProcessingInfo;
import org.adl.datamodels.IDataManager;
import org.adl.datamodels.nav.SCORM_2004_NAV_DM;
import org.adl.sequencer.IValidRequests;
import org.adl.sequencer.SeqNavRequests;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.sakaiproject.scorm.client.api.IRunState;
import org.sakaiproject.scorm.client.api.ScormClientFacade;

public class ScoBean implements SCORM13APIInterface, Serializable {
	private static Log log = LogFactory.getLog(ScoBean.class);
	
	private IErrorManager errorManager;
	
	// String value of FALSE for JavaScript returns.
	protected static final String STRING_FALSE = "false";

	// String value of TRUE for JavaScript returns.
	protected static final String STRING_TRUE = "true";
	
	// Indicates if the SCO is in a 'terminated' state.
	protected boolean mTerminateCalled = false;
	
	private boolean isInitialized = false;
	
	private boolean isTerminated = false;
	
	// Indicates if the current SCO is SCORM Version 1.2.
	protected boolean mSCO_VER_2 = false;

	// Indicates if the current SCO is SCORM 2004.
	protected boolean mSCO_VER_3 = false;

	// The public version attribute of the SCORM API.
	public static final String version = "1.0";	
		
	// Indicates number of the current attempt.
	private long mNumAttempts = 0L;
		
	private ScormClientFacade clientFacade;
	private IRunState runState;
	
	public ScoBean(ScormClientFacade clientFacade, IRunState runState) {
		this.clientFacade = clientFacade;
		this.runState = runState;
		errorManager = clientFacade.getErrorManager();
	}
	
	public String Commit(String iParam) {
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

	public String GetDiagnostic(String iErrorCode) {
		if (log.isDebugEnabled())
			log.debug("API GetDiagnostic (argument): " + iErrorCode);
		
		String result = errorManager.getErrorDiagnostic(iErrorCode);
		
		if (log.isDebugEnabled())
			log.debug("API GetDiagnostic (result): " + result);
		return result;
	}

	public String GetErrorString(String iErrorCode) {
		if (log.isDebugEnabled())
			log.debug("API GetErrorString (argument): " + iErrorCode);
		
		String result = errorManager.getErrorDescription(iErrorCode);
		
		if (log.isDebugEnabled())
			log.debug("API GetErrorString (result): " + result);
		return result;
	}

	public String GetLastError() {
		if (log.isDebugEnabled())
			log.debug("API GetLastError ");
		String result = errorManager.getCurrentErrorCode();
		
		if (!result.equals("0") && log.isWarnEnabled()) {
			String description = errorManager.getErrorDescription(result);
			log.warn("API threw an error: " + description);
		}
		
		if (log.isDebugEnabled())
			log.debug("API GetLastError (result): " + result);
		return result;
	}

	public String GetValue(String iDataModelElement) {
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

	public String Initialize(String iParam) {
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

	public String SetValue(String iDataModelElement, String iValue) {
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
	
	public String Terminate(String iParam) {
		return Terminate(iParam, null);
	}

	public String Terminate(String iParam, AjaxRequestTarget target) {
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
				// Process 'SET' on cmi.exit
				DMInterface.processSetValue("cmi.exit", "suspend", true, runState.getDataManager());
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
	}
	
	protected void setInitialized(boolean isInitialized) {
		this.isInitialized = isInitialized;
	}
	
	protected boolean isInitialized() {
		if ( ( !isTerminated ) &&  ( mSCO_VER_2 ) )
			errorManager.setCurrentErrorCode(DMErrorCodes.GEN_GET_FAILURE);

		return isInitialized;
	}
	
	protected void setTerminated(boolean isTerminated) {
		this.isTerminated = isTerminated;
	}
	
	protected boolean isTerminated() {
		return isTerminated;
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
		log.error("Called jsCall with message: " + message);
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
	

	/**
	 * Sets the number of the current attempt.
	 * 
	 * @param iNumAttempts
	 *            The number of the current attempt.
	 */
	public void setNumAttempts(long iNumAttempts) {
		mNumAttempts = iNumAttempts;
	}

	/**
	 * Sets the number of the current attemptfrom a String parameter.
	 * 
	 * @param iNumAttempts
	 *            The number of the current attempt.
	 */
	public void setNumAttempts(String iNumAttempts) {
		Long tempLong = new Long(iNumAttempts);
		mNumAttempts = tempLong.longValue();
	}

	/**
	 * Clears error codes and sets mInitialedState and mTerminated State to
	 * default values.
	 */
	public void clearState() {
		setInitialized(false);
		setTerminated(false);
		mTerminateCalled = false;
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
	
	
	public void suspendButtonPushed() {
		if (isInitialized()) {
			runState.setSuspended(true);
			Terminate("");
		}
	}

	public IRunState getRunState() {
		return runState;
	}

	public void setRunState(IRunState runState) {
		this.runState = runState;
	}
	
	
	
}
