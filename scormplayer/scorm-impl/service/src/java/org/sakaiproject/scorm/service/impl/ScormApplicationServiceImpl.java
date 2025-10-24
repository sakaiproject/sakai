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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

import org.adl.api.ecmascript.APIErrorCodes;
import org.adl.api.ecmascript.IErrorManager;
import org.adl.datamodels.DMErrorCodes;
import org.adl.datamodels.DMFactory;
import org.adl.datamodels.DMInterface;
import org.adl.datamodels.DMProcessingInfo;
import org.adl.datamodels.IDataManager;
import org.adl.datamodels.SCODataManager;
import org.adl.datamodels.ieee.IValidatorFactory;
import org.adl.datamodels.ieee.ValidatorFactory;
import org.adl.datamodels.nav.SCORM_2004_NAV_DM;
import org.adl.sequencer.ADLObjStatus;
import org.adl.sequencer.ADLValidRequests;
import org.adl.sequencer.ISeqActivity;
import org.adl.sequencer.ISeqActivityTree;
import org.adl.sequencer.ISequencer;
import org.adl.sequencer.IValidRequests;
import org.adl.sequencer.SeqNavRequests;
import org.adl.sequencer.SeqObjective;
import org.adl.validator.contentpackage.ILaunchData;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.grading.api.CommentDefinition;
import org.sakaiproject.grading.api.GradingService;
import static org.sakaiproject.scorm.api.ScormConstants.*;
import org.sakaiproject.scorm.adl.ADLConsultant;
import org.sakaiproject.scorm.dao.LearnerDao;
import org.sakaiproject.scorm.dao.api.AttemptDao;
import org.sakaiproject.scorm.dao.api.DataManagerDao;
import org.sakaiproject.scorm.exceptions.LearnerNotDefinedException;
import org.sakaiproject.scorm.model.api.ActivityTreeHolder;
import org.sakaiproject.scorm.model.api.Attempt;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.Learner;
import org.sakaiproject.scorm.model.api.LearnerExperience;
import org.sakaiproject.scorm.model.api.ScoBean;
import org.sakaiproject.scorm.model.api.ScoBeanImpl;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.navigation.INavigable;
import org.sakaiproject.scorm.navigation.INavigationEvent;
import org.sakaiproject.scorm.navigation.impl.NavigationEvent;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormApplicationService;
import org.sakaiproject.scorm.service.api.ScormSequencingService;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public abstract class ScormApplicationServiceImpl implements ScormApplicationService
{
	public static class CmiData
	{
		public boolean setPrimaryObjSuccess = false;
		public boolean setPrimaryObjScore = false;
		public double normalScore = -1.0;
		public String completionStatus = null;
		public String SCOEntry = "unknown";
		public String masteryStatus = "unknown";
		public String sessionTime = null;
		public String score = "unknown";
		public String activityId;
		public String primaryObjectiveId;
		public CmiData() {}
	}

	// Local utility bean (also dependency injected by lookup method)
	protected abstract ADLConsultant adlManager();

	// Data access objects (also dependency injected by lookup method)
	protected abstract AttemptDao attemptDao();

	// Sequencing service
	protected abstract ScormSequencingService scormSequencingService();

	protected abstract DataManagerDao dataManagerDao();
	public abstract GradingService gradingService();
	protected abstract LearnerDao learnerDao();
	protected abstract LearningManagementSystem lms();

	IValidatorFactory validatorFactory = new ValidatorFactory();

	private final ResourceLoader resourceLoader = new ResourceLoader("messages");

	private IValidRequests commit(SessionBean sessionBean, IDataManager dm, ISequencer sequencer)
	{
		log.debug("Service - Commit");

		if (null == dm)
		{
			log.warn("No data manager in commit");
			return null;
		}

		ADLValidRequests validRequests = null;
		try
		{

			if (sessionBean.isSuspended())
			{
				setValue(CMI_EXIT, "suspend", sessionBean, sessionBean.getDisplayingSco());
			}
			else if (sessionBean.isEnded()) {
				setValue(CMI_EXIT, "normal", sessionBean, sessionBean.getDisplayingSco());
			}

			// Call terminate on the data manager to ensure that we're in the appropriate state
			dm.terminate(validatorFactory);

			// Gather information from the data manager
			CmiData cmiData = lookupCmiData(sessionBean.getActivityId(), dm);

			// Process sequencing changes
			if (sequencer != null)
			{
				processTracking(sessionBean, cmiData, dm, sequencer);

				// May need to get the current valid requests
				validRequests = new ADLValidRequests();
				sequencer.getValidRequests(validRequests);

				log.debug("Sequencer is initialized and statuses have been set");

				ISeqActivityTree theTempTree = sequencer.getActivityTree();
				theTempTree.clearSessionState();
			}

			updateDataManager(validRequests, dm);
		}
		catch (Exception e)
		{
			log.error("Caught an exception:", e);
		}

		return validRequests;
	}

	@Override
	public boolean commit(String parameter, SessionBean sessionBean, ScoBean scoBean)
	{
		log.debug("API Commit (argument): {}", parameter);

		// Assume failure
		boolean isSuccessful = false;
		IErrorManager errorManager = sessionBean.getErrorManager();

		// Make sure param is empty string "" - as per the API spec
		// Check for "null" is a workaround described in "Known Problems"
		// in the header.
		if (StringUtils.isEmpty( parameter ) != true)
		{
			log.warn("Non-null or empty param passed to commit");
			errorManager.setCurrentErrorCode(DMErrorCodes.GEN_ARGUMENT_ERROR);
		}
		else
		{
			if (scoBean == null || !scoBean.isInitialized())
			{
				log.warn("Attempting to commit prior to initialization");
				// LMS is not initialized
				errorManager.setCurrentErrorCode(APIErrorCodes.COMMIT_BEFORE_INIT);
			}
			else if (scoBean.isTerminated())
			{
				log.warn("Attempting to commit after termination");
				// LMS is terminated
				errorManager.setCurrentErrorCode(APIErrorCodes.COMMIT_AFTER_TERMINATE);
			}
			else
			{
				IDataManager dataManager = adlManager().getDataManager(sessionBean, scoBean);
				ISeqActivityTree tree = adlManager().getActivityTree(sessionBean);
				ISequencer sequencer = adlManager().getSequencer(tree);
				IValidRequests validRequests = commit(sessionBean, dataManager, sequencer);

				if (validRequests == null)
				{
					log.warn("Valid requests object is null");
					errorManager.setCurrentErrorCode(APIErrorCodes.GENERAL_EXCEPTION);
				}
				else if (null == dataManager)
				{
					log.error("Null data manager!");
					errorManager.setCurrentErrorCode(APIErrorCodes.GENERAL_EXCEPTION);
				}
				else
				{
					errorManager.clearCurrentErrorCode();
					isSuccessful = true;
					SCORM_2004_NAV_DM navDM = (SCORM_2004_NAV_DM) dataManager.getDataModel("adl");

					// Update the ADLValidRequests object from the servlet
					// response object.
					navDM.setValidRequests(validRequests);
				}
			}
		}

		log.debug("API Commit (result): {}", isSuccessful);
		return isSuccessful;
	}

	@Override
	public void discardScoBean(String scoId, SessionBean sessionBean, INavigable agent)
	{
		Map<String, ScoBean> scoBeans = sessionBean.getScoBeans();
		if (null != scoId && scoBeans.containsKey(scoId))
		{
			log.debug("Discarding the ScoBean for the Sco {}", scoId);
			scoBeans.remove(scoId);
			sessionBean.setDisplayingSco(null);
		}

		if (sessionBean.isCloseOnNextTerminate())
		{
			scormSequencingService().navigate(SeqNavRequests.NAV_SUSPENDALL, sessionBean, agent, null);
			Attempt attempt = sessionBean.getAttempt();
			if (attempt != null)
			{
				attempt.setSuspended(true);
				attempt.setNotExited(true);
				attemptDao().save(attempt);
			}
		}
	}

	private Attempt getAttempt(SessionBean sessionBean)
	{
		Attempt attempt = sessionBean.getAttempt();
		if (attempt == null)
		{
			String courseId = sessionBean.getContentPackage().getResourceId();
			String learnerId = sessionBean.getLearnerId();
			long attemptNumber = sessionBean.getAttemptNumber();
			attempt = attemptDao().find(courseId, learnerId, attemptNumber);

			if (attempt == null)
			{
				attempt = new Attempt();

				attempt.setContentPackageId(sessionBean.getContentPackage().getContentPackageId());
				attempt.setCourseId(courseId);
				attempt.setLearnerId(learnerId);
				attempt.setAttemptNumber(attemptNumber);
				attempt.setLearnerName("Unavailable");
				attempt.setBeginDate(new Date());
				try
				{
					Learner learner = learnerDao().load(learnerId);
					if (learner != null)
					{
						attempt.setLearnerName(learner.getDisplayName());
					}
				}
				catch (LearnerNotDefinedException e)
				{
					log.error("Could not find learner {}", learnerId, e);
				}
			}

			sessionBean.setAttempt(attempt);
		}

		return attempt;
	}

	private IDataManager getDataManager(ScoBean scoBean)
	{
		DataManagerDao dataManagerDao = dataManagerDao();
		return dataManagerDao.load(scoBean.getDataManagerId());
	}

	@Override
	public String getDiagnostic(String errorCode, SessionBean sessionBean)
	{
		log.debug("API GetDiagnostic (argument): {}", errorCode);

		IErrorManager errorManager = sessionBean.getErrorManager();
		String result = errorManager.getErrorDiagnostic(errorCode);

		log.debug("API GetDiagnostic (result): {}", result);
		return result;
	}

	@Override
	public String getErrorString(String iErrorCode, SessionBean sessionBean)
	{
		log.debug("API GetErrorString (argument): {}", iErrorCode);

		IErrorManager errorManager = sessionBean.getErrorManager();
		String result = errorManager.getErrorDescription(iErrorCode);

		log.debug("API GetErrorString (result): {}", result);
		return result;
	}

	@Override
	public String getLastError(SessionBean sessionBean)
	{
		log.debug("API GetLastError ");
		IErrorManager errorManager = sessionBean.getErrorManager();
		String result = errorManager.getCurrentErrorCode();

		if (!result.equals("0") && log.isWarnEnabled())
		{
			String description = errorManager.getErrorDescription(result);
			log.warn("API threw an error: {}", description);
		}

		log.debug("API GetLastError (result): {}", result);
		return result;
	}

	private OptionalDouble getRealValue(String element, IDataManager dataManager)
	{
		String result = getValue(element, dataManager);
		if (StringUtils.isBlank(result) || result.equals("unknown"))
		{
			return OptionalDouble.empty();
		}

		OptionalDouble d = OptionalDouble.empty();
		try
		{
			d = OptionalDouble.of(Double.parseDouble(result));
		}
		catch (Exception ex)
		{
			log.error("Unable to parse {} as a double!", result, ex);
		}

		return d;
	}

	private String getValue(String iDataModelElement, IDataManager dataManager)
	{
		// Process 'GET'
		DMProcessingInfo dmInfo = new DMProcessingInfo();

		String result;
		int dmErrorCode = 0;
		dmErrorCode = DMInterface.processGetValue(iDataModelElement, false, dataManager, dmInfo);

		if (dmErrorCode == APIErrorCodes.NO_ERROR)
		{
			result = dmInfo.mValue;
			if (StringUtils.isBlank(result))
			{
				if ("cmi.completion_status".equals(iDataModelElement) || "cmi.success_status".equals(iDataModelElement))
				{
					result = "unknown";
				}
			}
		}
		else
		{
			result = "";
		}

		return result;
	}

	@Override
	public String getValue(String parameter, SessionBean sessionBean, ScoBean scoBean)
	{
		log.debug("API GetValue (argument): {}", parameter);

		String result = "";
		IErrorManager errorManager = sessionBean.getErrorManager();

		// Clear current error codes
		errorManager.clearCurrentErrorCode();

		// Already terminated
		if (scoBean.isTerminated())
		{
			errorManager.setCurrentErrorCode(APIErrorCodes.GET_AFTER_TERMINATE);
			return result;
		}

		if (parameter.length() == 0)
		{
			errorManager.setCurrentErrorCode(DMErrorCodes.GEN_GET_FAILURE);
			return result;
		}

		if (scoBean.isInitialized())
		{
			// Process 'GET'
			DMProcessingInfo dmInfo = new DMProcessingInfo();

			int dmErrorCode = 0;
			IDataManager dataManager = getDataManager(scoBean);
			dmErrorCode = DMInterface.processGetValue(parameter, false, dataManager, dmInfo);
			dataManagerDao().update(dataManager);

			// Set the LMS Error Manager from the Data Model Error Manager
			errorManager.setCurrentErrorCode(dmErrorCode);

			if (dmErrorCode == APIErrorCodes.NO_ERROR)
			{
				result = dmInfo.mValue;
				if (StringUtils.isBlank(result))
				{
					if ("cmi.completion_status".equals(parameter) || "cmi.success_status".equals(parameter))
					{
						result = "unknown";
					}
				}
			}
			else
			{
				result = "";
			}

			log.debug("API GetValue (result): {}", result);
		}
		else
		{
			errorManager.setCurrentErrorCode(APIErrorCodes.GET_BEFORE_INIT);
		}

		return result;
	}

	private String getValueAsString(String element, IDataManager dataManager)
	{
		String result = getValue(element, dataManager);
		if (StringUtils.isBlank(result) || result.equals("unknown"))
		{
			return null;
		}

		return result;
	}

	private IDataManager initialize(SessionBean sessionBean, ScoBean scoBean)
	{
		log.debug("Service - Initialize");

		boolean isNewDataManager = false;
		IDataManager dm = null;

		long contentPackageId = sessionBean.getContentPackage().getContentPackageId();
		String activityId = sessionBean.getActivityId();
		String courseId = sessionBean.getContentPackage().getResourceId();
		String scoId = scoBean.getScoId();
		String learnerId = sessionBean.getLearnerId();
		String title = sessionBean.getActivityTitle();

		ILaunchData launchData = sessionBean.getLaunchData();
		IValidRequests validRequests = sessionBean.getNavigationState();
		List mStatusVector = sessionBean.getObjectiveStatusSet();

		Attempt attempt = getAttempt(sessionBean);
		Long dataManagerId = attempt.getDataManagerId(scoId);

		// FIXME : We need to look in the db first -- but currently it doesn't seem that data is getting saved correctly
		// First, check to see if we have a ScoDataManager persisted
		if (dataManagerId != null && dataManagerId != -1)
		{
			dm = dataManagerDao().load(dataManagerId);
		}

		if (dm == null)
		{
			// If not, create one, which means this is the 
			dm = new SCODataManager(contentPackageId, courseId, scoId, activityId, learnerId, title, sessionBean.getAttemptNumber());

			//  Add a SCORM 2004 Data Model
			dm.addDM(DMFactory.DM_SCORM_2004, validatorFactory);

			//  Add a SCORM 2004 Nav Data Model
			dm.addDM(DMFactory.DM_SCORM_NAV, validatorFactory);

			//  Add a SSP Datamodel
			dm.addDM(DMFactory.DM_SSP, validatorFactory);

			Learner learner = null;
			try
			{
				learner = learnerDao().load(learnerId);
			}
			catch (Exception exc)
			{
				log.error("Unable to find user for {}", learnerId, exc);
			}

			// TODO: Need to replace this with some reasonable alternative
			initSCOData(dm, launchData, learner, courseId, scoId);

			// TODO: Need to replace this with some reasonable alternative
			// SSP addition
			initSSPData(dm, courseId, scoId, String.valueOf(sessionBean.getAttemptNumber()), learnerId);

			isNewDataManager = true;
		}

		ADLObjStatus mObjStatus = new ADLObjStatus();

		// Temporary variables for obj initialization
		int err = 0;
		String obj = "";

		// Initialize Objectives based on global objectives
		if (mStatusVector != null)
		{
			if (isNewDataManager)
			{
				for (int i = 0; i < mStatusVector.size(); i++)
				{
					// initialize objective status from sequencer
					mObjStatus = (ADLObjStatus) mStatusVector.get(i);

					// Set the objectives id
					obj = CMI_OBJECTIVES_ROOT + i + ".id";

					err = DMInterface.processSetValue(obj, mObjStatus.mObjID, true, dm, validatorFactory);

					// Set the objectives success status
					obj = CMI_OBJECTIVES_ROOT + i + ".success_status";

					if (mObjStatus.mStatus.equalsIgnoreCase("satisfied"))
					{
						err = DMInterface.processSetValue(obj, "passed", true, dm, validatorFactory);
					}
					else if (mObjStatus.mStatus.equalsIgnoreCase("notSatisfied"))
					{
						err = DMInterface.processSetValue(obj, "failed", true, dm, validatorFactory);
					}

					// Set the objectives scaled score
					obj = CMI_OBJECTIVES_ROOT + i + ".score.scaled";

					if (mObjStatus.mHasMeasure)
					{
						Double norm = mObjStatus.mMeasure;
						err = DMInterface.processSetValue(obj, norm.toString(), true, dm, validatorFactory);
					}
				}
			}
			else
			{
				for (int i = 0; i < mStatusVector.size(); i++)
				{
					int idx = -1;

					// initialize objective status from sequencer
					mObjStatus = (ADLObjStatus) mStatusVector.get(i);

					// get the count of current objectives
					DMProcessingInfo pi = new DMProcessingInfo();
					int result = DMInterface.processGetValue(CMI_OBJECTIVES_COUNT, true, dm, pi);
					int objCount = (Integer.valueOf(pi.mValue));

					// Find the current index for this objective
					for (int j = 0; j < objCount; j++)
					{
						pi = new DMProcessingInfo();
						obj = CMI_OBJECTIVES_ROOT + j + ".id";
						result = DMInterface.processGetValue(obj, true, dm, pi);
						if (pi.mValue.equals(mObjStatus.mObjID))
						{
							idx = j;
							break;
						}
					}

					if (idx != -1)
					{
						// Set the objectives success status
						obj = CMI_OBJECTIVES_ROOT + idx + ".success_status";

						if (mObjStatus.mStatus.equalsIgnoreCase("satisfied"))
						{
							err = DMInterface.processSetValue(obj, "passed", true, dm, validatorFactory);
						}
						else if (mObjStatus.mStatus.equalsIgnoreCase("notSatisfied"))
						{
							err = DMInterface.processSetValue(obj, "failed", true, dm, validatorFactory);
						}

						// Set the objectives scaled score
						obj = CMI_OBJECTIVES_ROOT + idx + ".score.scaled";

						if (mObjStatus.mHasMeasure)
						{
							Double norm = mObjStatus.mMeasure;
							err = DMInterface.processSetValue(obj, norm.toString(), true, dm, validatorFactory);
						}
					}
					else
					{
						log.warn("  OBJ NOT FOUND --> {}", mObjStatus.mObjID);
					}
				}
			}
		}

		if (isNewDataManager)
		{
			persistDataManager(validRequests, dm);
		}
		else
		{
			updateDataManager(validRequests, dm);
		}

		attempt.setDataManagerId(scoId, dm.getId());
		attemptDao().save(attempt);
		return dm;
	}

	@Override
	public boolean initialize(String parameter, SessionBean sessionBean, ScoBean scoBean)
	{
		log.debug("API Initialize (argument): {}", parameter);

		boolean isSuccessful = false;
		IErrorManager errorManager = sessionBean.getErrorManager();

		if (scoBean.isTerminated())
		{
			errorManager.setCurrentErrorCode(APIErrorCodes.CONTENT_INSTANCE_TERMINATED);
			return isSuccessful;
		}

		scoBean.setTerminated(false);
		scoBean.setVersion(ScoBean.SCO_VERSION_3);

		if (StringUtils.isEmpty( parameter ) != true)
		{
			errorManager.setCurrentErrorCode(DMErrorCodes.GEN_ARGUMENT_ERROR);
		}
		else if (scoBean.isInitialized())
		{
			// If the SCO is already initialized set the appropriate error code
			errorManager.setCurrentErrorCode(APIErrorCodes.ALREADY_INITIALIZED);
		}
		else
		{
			sessionBean.setSuspended(false);
			IDataManager dm = initialize(sessionBean, scoBean);
			if (dm != null)
			{
				scoBean.setDataManagerId(dm.getId());
				scoBean.setInitialized(true);

				// No errors were detected
				errorManager.clearCurrentErrorCode();
				isSuccessful = true;
			}
		}

		log.debug("API Initialize (result): {}", isSuccessful);
		return isSuccessful;
	}

	private void initSCOData(IDataManager ioSCOData, ILaunchData launchData, Learner learner, String iCourseID, String iItemID)
	{
		try
		{
			String masteryScore = "";
			String dataFromLMS = "";
			String maxTime = "";
			String timeLimitAction = "";
			String completionThreshold = "";

			// User preferences
			String audLev = DEFAULT_USER_AUDIO_LEVEL;
			String audCap = DEFAULT_USER_AUDIO_CAPTIONING;
			String delSpd = DEFAULT_USER_DELIVERY_SPEED;
			String lang = DEFAULT_USER_LANGUAGE;

			// Get the learner preference values from Sakai
			Properties props = null;
			if (learner != null)
			{
				props = learner.getProperties();
			}

			if (props != null)
			{
				if (null != props.getProperty(PREF_USER_AUDIO_LEVEL))
				{
					audLev = props.getProperty(PREF_USER_AUDIO_LEVEL);
				}
				if (null != props.getProperty(PREF_USER_AUDIO_CAPTIONING))
				{
					audCap = props.getProperty(PREF_USER_AUDIO_CAPTIONING);
				}
				if (null != props.getProperty(PREF_USER_DELIVERY_SPEED))
				{
					delSpd = props.getProperty(PREF_USER_DELIVERY_SPEED);
				}
				if (null != props.getProperty(PREF_USER_LANGUAGE))
				{
					lang = props.getProperty(PREF_USER_LANGUAGE);
				}
			}

			// Get sco data from manifest
			masteryScore = launchData.getMinNormalizedMeasure();
			dataFromLMS = launchData.getDataFromLMS();
			maxTime = launchData.getAttemptAbsoluteDurationLimit();
			timeLimitAction = launchData.getTimeLimitAction();
			completionThreshold = launchData.getCompletionThreshold();
			String element = "";

			if (null != learner)
			{
				// Initialize the learner id
				element = CMI_LEARNER_ID;
				DMInterface.processSetValue(element, learner.getId(), true, ioSCOData, validatorFactory);

				// Initialize the learner name
				element = CMI_LEARNER_NAME;
				DMInterface.processSetValue(element, learner.getDisplayName(), true, ioSCOData, validatorFactory);
			}

			// Initialize the cmi.credit value
			element = CMI_CREDIT;
			DMInterface.processSetValue(element, "credit", true, ioSCOData, validatorFactory);

			// Initialize the mode
			element = CMI_MODE;
			DMInterface.processSetValue(element, "normal", true, ioSCOData, validatorFactory);

			// Initialize any launch data
			if (dataFromLMS != null && !dataFromLMS.isEmpty())
			{
				element = CMI_LAUNCH_DATA;
				DMInterface.processSetValue(element, dataFromLMS, true, ioSCOData, validatorFactory);
			}

			// Initialize the scaled passing score
			if (masteryScore != null && !masteryScore.isEmpty())
			{
				element = CMI_SCALED_PASSING_SCORE;
				DMInterface.processSetValue(element, masteryScore, true, ioSCOData, validatorFactory);
			}

			// Initialize the time limit action
			if (timeLimitAction != null && !timeLimitAction.isEmpty())
			{
				element = CMI_TIME_LIMIT_ACTION;
				DMInterface.processSetValue(element, timeLimitAction, true, ioSCOData, validatorFactory);
			}

			// Initialize the completion_threshold
			if (completionThreshold != null && !completionThreshold.isEmpty())
			{
				element = CMI_COMPLETION_THRESHOLD;
				DMInterface.processSetValue(element, completionThreshold, true, ioSCOData, validatorFactory);
			}

			// Initialize the max time allowed
			if (maxTime != null && !maxTime.isEmpty())
			{
				element = CMI_MAX_TIME_ALLOWED;
				DMInterface.processSetValue(element, maxTime, true, ioSCOData, validatorFactory);
			}

			// Initialize the learner preferences based on the SRTE
			// learner profile information

			// audio_level
			element = "cmi.learner_preference.audio_level";
			DMInterface.processSetValue(element, audLev, true, ioSCOData, validatorFactory);

			// audio_captioning
			element = "cmi.learner_preference.audio_captioning";
			DMInterface.processSetValue(element, audCap, true, ioSCOData, validatorFactory);

			// delivery_speed
			element = "cmi.learner_preference.delivery_speed";
			DMInterface.processSetValue(element, delSpd, true, ioSCOData, validatorFactory);

			// language
			element = "cmi.learner_preference.language";
			DMInterface.processSetValue(element, lang, true, ioSCOData, validatorFactory);

		}
		catch (Exception e)
		{
			log.error("Caught an exception while trying to initalize the sco data", e);
		}
	}

	private void initSSPData(IDataManager ioSCOData, String iCourseID, String iScoID, String iNumAttempt, String iUserID)
	{
		String element = "";
		int err;

		// Initialize the learner id
		element = "ssp.init.userid";
		err = DMInterface.processSetValue(element, iUserID, true, ioSCOData, validatorFactory);

		// Initialize the course id
		element = "ssp.init.courseid";
		err = DMInterface.processSetValue(element, iCourseID, true, ioSCOData, validatorFactory);

		// Initialize the attempt id
		element = "ssp.init.attemptnum";
		err = DMInterface.processSetValue(element, iNumAttempt, true, ioSCOData, validatorFactory);

		// Initialize the sco id
		element = "ssp.init.scoID";
		err = DMInterface.processSetValue(element, iScoID, true, ioSCOData, validatorFactory);
	}

	private CmiData lookupCmiData(String activityId, IDataManager dm)
	{
		CmiData status = new CmiData();
		status.primaryObjectiveId = null;
		status.activityId = activityId;

		// Get the current completion_status
		status.completionStatus = retrieveValue(CMI_COMPLETION_STATUS, "unknown", dm);

		if (status.completionStatus.equals("not attempted"))
		{
			status.completionStatus = "incomplete";
		}

		// Get the current success_status
		status.masteryStatus = retrieveValue(CMI_SUCCESS_STATUS, "unknown", dm);

		// Get the current entry
		status.SCOEntry = retrieveValueAsAdmin(CMI_ENTRY, "unknown", dm);

		// Get the current scaled score
		status.score = retrieveValueAsAdmin(CMI_SCORE_SCALED, "", dm);

		// Get the current session time
		status.sessionTime = retrieveValueAsAdmin(CMI_SESSION_TIME, "unknown", dm);
		return status;
	}

	@Override
	public INavigationEvent newNavigationEvent()
	{
		return new NavigationEvent();
	}

	private void persistDataManager(IValidRequests validRequests, IDataManager scoDataManager)
	{
		if (null != validRequests)
		{
			SCORM_2004_NAV_DM navDM = (SCORM_2004_NAV_DM) scoDataManager.getDataModel("adl");
			navDM.setValidRequests(validRequests);
			dataManagerDao().save(scoDataManager);
		}
		else
		{
			log.error("Current nav state is null!");
		}
	}

	private void processObjectives(int numObjs, CmiData cmiData, IDataManager dm, ISequencer sequencer)
	{
		boolean foundPrimaryObj = false;
		for (int i = 0; i < numObjs; i++)
		{
			// Get this objectives id
			String objIdRequest = new StringBuilder(CMI_OBJECTIVES_ROOT).append(i).append(".id").toString();
			String objID = retrieveValue(objIdRequest, "", dm);

			foundPrimaryObj = cmiData.primaryObjectiveId != null && objID.equals(cmiData.primaryObjectiveId);

			// Get this objectives mastery
			String objMsRequest = new StringBuilder(CMI_OBJECTIVES_ROOT).append(i).append(".success_status").toString();
			String objMS = retrieveValue(objMsRequest, "", dm);

			// Report the success status
			if (objMS.equals("passed"))
			{
				sequencer.setAttemptObjSatisfied(cmiData.activityId, objID, "satisfied");

				if (foundPrimaryObj)
				{
					cmiData.setPrimaryObjSuccess = true;
				}
			}
			else if (objMS.equals("failed"))
			{
				sequencer.setAttemptObjSatisfied(cmiData.activityId, objID, "notSatisfied");

				if (foundPrimaryObj)
				{
					cmiData.setPrimaryObjSuccess = true;
				}
			}
			else
			{
				sequencer.setAttemptObjSatisfied(cmiData.activityId, objID, "unknown");
			}

			// Get this objectives measure
			String objScoreRequest = new StringBuilder(CMI_OBJECTIVES_ROOT).append(i).append(".score.scaled").toString();
			String objScore = retrieveValue(objScoreRequest, "unknown", dm);

			// Report the measure
			if (!objScore.isEmpty() && !objScore.equals("unknown"))
			{
				try
				{
					cmiData.normalScore = (new Double(objScore));
					sequencer.setAttemptObjMeasure(cmiData.activityId, objID, cmiData.normalScore);

					if (foundPrimaryObj)
					{
						cmiData.setPrimaryObjScore = true;
					}
				}
				catch (Exception e)
				{
					log.error("Found no valid score for this objective: {}", cmiData.normalScore, e);
				}
			}
			else
			{
				sequencer.clearAttemptObjMeasure(cmiData.activityId, objID);
			}
		}
	}

	private void processTracking(SessionBean sessionBean, CmiData cmiData, IDataManager dm, ISequencer sequencer)
	{
		String activityId = sessionBean.getActivityId();
		ActivityTreeHolder treeHolder = sessionBean.getTreeHolder();
		ISeqActivityTree tree = null;

		if (treeHolder == null)
		{
			log.error("Could not find a tree holder!!!");
			return;
		}

		tree = treeHolder.getSeqActivityTree();
		if (tree == null)
		{
			log.error("Could not find a tree!!!");
			return;
		}

		// Only modify the TM if the activity is tracked
		ISeqActivity act = tree.getActivity(activityId);
		if (act != null && act.getIsTracked())
		{

			// Update the activity's status
			log.debug("{} is TRACKED -- ", act.getID());
			log.debug("Performing default mapping to TM");

			// Find the primary objective ID
			List<SeqObjective> objs = act.getObjectives();
			if (objs != null)
			{
				for (int j = 0; j < objs.size(); j++)
				{
					SeqObjective obj = objs.get(j);
					if (obj.mContributesToRollup)
					{
						if (obj.mObjID != null)
						{
							cmiData.primaryObjectiveId = obj.mObjID;
						}

						break;
					}
				}
			}

			// Get the activities objective list
			// Map the DM to the TM
			String size = retrieveValue(CMI_OBJECTIVES_COUNT, "none", dm);
			if (!size.equals("none"))
			{
				try
				{
					Integer s = Integer.valueOf(size);
					int numObjs = s;

					// Loop through objectives updating TM
					processObjectives(numObjs, cmiData, dm, sequencer);

				}
				catch (NumberFormatException nfe)
				{
					log.warn("Caught a number format exception trying to process the string {}", size, nfe);
				}

			}

			// Report the completion status
			sequencer.setAttemptProgressStatus(cmiData.activityId, cmiData.completionStatus);

			if (cmiData.SCOEntry.equals("resume"))
			{
				sequencer.reportSuspension(cmiData.activityId, true);
			}
			else
			{

				// TODO: replace with db code.
				// Clean up session level SSP buckets
				// RTEFileHandler fileHandler = new RTEFileHandler();
				// fileHandler.deleteAttemptSSPData(userId, courseId,
				// scoId);

				sequencer.reportSuspension(cmiData.activityId, false);
			}

			// Report the success status
			if (cmiData.masteryStatus.equals("passed"))
			{
				sequencer.setAttemptObjSatisfied(cmiData.activityId, cmiData.primaryObjectiveId, "satisfied");
			}
			else if (cmiData.masteryStatus.equals("failed"))
			{
				sequencer.setAttemptObjSatisfied(cmiData.activityId, cmiData.primaryObjectiveId, "notSatisfied");
			}
			else
			{
				if (!cmiData.setPrimaryObjSuccess)
				{
					sequencer.setAttemptObjSatisfied(cmiData.activityId, cmiData.primaryObjectiveId, "unknown");
				}
			}

			// Report the measure
			if (!cmiData.score.isEmpty() && !cmiData.score.equals("unknown"))
			{
				try
				{
					cmiData.normalScore = (new Double(cmiData.score));
					sequencer.setAttemptObjMeasure(cmiData.activityId, cmiData.primaryObjectiveId, cmiData.normalScore);
				}
				catch (Exception e)
				{
					log.error("  ::--> ERROR: Invalid score: {}; exception was: {}", cmiData.normalScore, e.getMessage());
				}
			}
			else
			{
				if (!cmiData.setPrimaryObjScore)
				{
					sequencer.clearAttemptObjMeasure(cmiData.activityId, cmiData.primaryObjectiveId);
				}
			}
		}
	}

	@Override
	public ScoBean produceScoBean(String scoId, SessionBean sessionBean)
	{
		ScoBean scoBean = null;
		if (StringUtils.isBlank(scoId) || scoId.equals("undefined"))
		{
			// If no sco id is passed then we simply grab it from the sessionBean 
			scoId = sessionBean.getScoId();
			log.debug("Null sco id -- grabbing current sco id {}", scoId);
		}

		Map<String, ScoBean> scoBeans = sessionBean.getScoBeans();
		if (null != scoId && scoBeans.containsKey(scoId))
		{
			scoBean = scoBeans.get(scoId);
		}
		else
		{
			log.debug("Creating a new ScoBean for the Sco {}", scoId);

			scoBean = new ScoBeanImpl(scoId, sessionBean);
			scoBean.clearState();
			if (null != scoId)
			{
				scoBeans.put(scoId, scoBean);
			}

			sessionBean.setDisplayingSco(scoBean);
		}

		log.debug("SCO is {}", scoId);
		return scoBean;
	}

	private String retrieveValue(String iRequest, String defaultValue, IDataManager scoDataManager)
	{
		return retrieveValue(iRequest, defaultValue, scoDataManager, false);
	}

	private String retrieveValue(String iRequest, String defaultValue, IDataManager scoDataManager, boolean iAdmin)
	{
		int err = 0;
		DMProcessingInfo dmInfo = new DMProcessingInfo();

		// Get the current completion_status
		err = DMInterface.processGetValue(iRequest, iAdmin, true, scoDataManager, dmInfo);

		switch (err) {
			case DMErrorCodes.NO_ERROR:
				return dmInfo.mValue;
			case DMErrorCodes.NOT_INITIALIZED:
				log.info("The data element at {} is not initialized.", iRequest);
				break;
			case DMErrorCodes.DOES_NOT_HAVE_COUNT:
				if (!iRequest.endsWith("._count"))
				{
					log.info("Strange error -- 'Does not have count' for data element " + iRequest);
				}

				break;
			case DMErrorCodes.WRITE_ONLY:
				log.info("This data element {} is write only.", iRequest);
				break;
			default:
				log.info("Found a data model error retrieving value for {} error is {}", iRequest, err);
		}

		return defaultValue == null ? "unknown" : defaultValue;
	}

	private String retrieveValueAsAdmin(String iRequest, String defaultValue, IDataManager scoDataManager)
	{
		return retrieveValue(iRequest, defaultValue, scoDataManager, true);
	}

	@Override
	public boolean setValue(String dataModelElement, String value, SessionBean sessionBean, ScoBean scoBean)
	{
		log.debug("API SetValue (arguments): {}, {}", dataModelElement, value);

		// Assume failure
		boolean isSuccessful = false;

		String setValue = value;
		if (setValue == null)
		{
			setValue = "";
		}

		if (null == sessionBean)
		{
			log.error("Null run state!");
			return isSuccessful;
		}

		IErrorManager errorManager = sessionBean.getErrorManager();

		// already terminated
		if (scoBean.isTerminated())
		{
			errorManager.setCurrentErrorCode(APIErrorCodes.SET_AFTER_TERMINATE);
			return isSuccessful;
		}

		// Clear any existing error codes
		errorManager.clearCurrentErrorCode();

		if (!scoBean.isInitialized())
		{
			// not initialized
			errorManager.setCurrentErrorCode(APIErrorCodes.SET_BEFORE_INIT);
			return isSuccessful;
		}

		// Process 'SET'
		int dmErrorCode = 0;
		IDataManager dataManager = getDataManager(scoBean);
		dmErrorCode = DMInterface.processSetValue(dataModelElement, setValue, false, dataManager, validatorFactory);
		dataManagerDao().update(dataManager);

		// Set the LMS Error Manager from the DataModel Manager
		errorManager.setCurrentErrorCode(dmErrorCode);

		if (errorManager.getCurrentErrorCode().equals("0"))
		{
			// Successful Set
			isSuccessful = true;
		}

		log.debug("API SetValue (result): {}", isSuccessful);
		return isSuccessful;
	}

	public void synchResultWithGradebook(LearnerExperience experience, ContentPackage contentPackage, String itemIdentifier, Attempt latestAttempt)
	{
		SessionBean sessionBean = new SessionBean(experience.getLearnerId(), contentPackage);
		ScoBean scoBean = produceScoBean(itemIdentifier, sessionBean);
		scoBean.setDataManagerId(latestAttempt.getDataManagerId(scoBean.getScoId()));
		synchResultWithGradebook(sessionBean);
	}

	public void synchResultWithGradebook(SessionBean sessionBean)
	{
		ScoBean displayingSco = sessionBean.getDisplayingSco();
		IDataManager dataManager = getDataManager(displayingSco);

		// passed, failed, unknown
		String mode = getValueAsString(CMI_MODE, dataManager);

		// credit, no_credit
		String credit = getValueAsString(CMI_CREDIT, dataManager);

		// (completed, incomplete, not_attempted, unknown)
		String completionStatus = getValueAsString(CMI_COMPLETION_STATUS, dataManager);

		if ("normal".equals(mode) && "completed".equals(completionStatus) && "credit".equals(credit))
		{
			String context = lms().currentContext();
			String learnerID = sessionBean.getLearnerId();
			String assessmentExternalId = "" + sessionBean.getContentPackage().getContentPackageId() + ":" + dataManager.getScoId();

			// A real number with values that is accurate to seven significant decimal figures. The value shall be in the range of -1.0 to +1.0, inclusive.
			OptionalDouble score = getRealValue(CMI_SCORE_SCALED, dataManager);

			// Logic to update score and/or comment lives in below method, pass the necessary data
			updateGradebook(score, context, learnerID, assessmentExternalId);
		}
	}

	@Override
	public boolean terminate(String parameter, INavigationEvent navigationEvent, SessionBean sessionBean, ScoBean scoBean)
	{
		log.debug("API Terminate (argument): {}", parameter);

		// Assume failure
		boolean isSuccessful = false;

		IErrorManager errorManager = sessionBean.getErrorManager();
		if (scoBean == null)
		{
			errorManager.setCurrentErrorCode(APIErrorCodes.TERMINATE_BEFORE_INIT);
			return isSuccessful;
		}

		// already terminated
		if (scoBean.isTerminated())
		{
			errorManager.setCurrentErrorCode(APIErrorCodes.TERMINATE_AFTER_TERMINATE);
			return isSuccessful;
		}

		if (!scoBean.isInitialized())
		{
			errorManager.setCurrentErrorCode(APIErrorCodes.TERMINATE_BEFORE_INIT);
			return isSuccessful;
		}

		IDataManager dataManager = getDataManager(scoBean);

		// Make sure param is empty string "" - as per the API spec
		// Check for "null" is a workaround described in "Known Problems"
		// in the header.
		if (StringUtils.isEmpty( parameter ) != true)
		{
			errorManager.setCurrentErrorCode(DMErrorCodes.GEN_ARGUMENT_ERROR);
		}
		else if (null == dataManager)
		{
			log.error("Null data manager!");
			errorManager.setCurrentErrorCode(APIErrorCodes.GENERAL_EXCEPTION);
		}
		else
		{
			// check if adl.nav.request is equal to suspend all, or if the
			// suspend button was pushed, set cmi.exit equal to suspend.
			SCORM_2004_NAV_DM navDM = (SCORM_2004_NAV_DM) dataManager.getDataModel("adl");
			int event = navDM.getNavEvent();

			if (event == SeqNavRequests.NAV_SUSPENDALL || scoBean.isSuspended())
			{
				// Before we change the cmi.exit, make sure the SCO didn't set it to log-out.
				DMProcessingInfo pi = new DMProcessingInfo();
				int check = DMInterface.processGetValue(CMI_EXIT, true, dataManager, pi);

				if (check != 0 || !pi.mValue.equals("log-out"))
				{
					// Process 'SET' on cmi.exit
					DMInterface.processSetValue(CMI_EXIT, "suspend", true, dataManager, validatorFactory);
				}
			}

			if (!(event == SeqNavRequests.NAV_ABANDON || event == SeqNavRequests.NAV_ABANDONALL))
			{
				isSuccessful = commit("", sessionBean, scoBean);
			}
			else
			{
				// The attempt has been abandoned, so don't persist the data
				isSuccessful = true;
			}

			if (!isSuccessful)
			{
				// General Commit Failure
				errorManager.setCurrentErrorCode(APIErrorCodes.GENERAL_COMMIT_FAILURE);
			}
			else
			{
				// get value of "exit"
				DMProcessingInfo dmInfo = new DMProcessingInfo();
				int dmErrorCode = 0;
				dmErrorCode = DMInterface.processGetValue(CMI_EXIT, true, true, dataManager, dmInfo);
				String exitValue = dmInfo.mValue;

				if (dmErrorCode == APIErrorCodes.NO_ERROR)
				{
					exitValue = dmInfo.mValue;
				}
				else
				{
					exitValue = "";
				}

				if (exitValue.equals("time-out") || exitValue.equals("logout"))
				{
					event = SeqNavRequests.NAV_EXITALL;
					Attempt attempt = sessionBean.getAttempt();
					attempt.setSuspended(false);
					attempt.setNotExited(false);
					attemptDao().save(attempt);
				}

				// handle if sco set nav.request
				if (!scoBean.isSuspended() && event != SeqNavRequests.NAV_NONE)
				{
					navigationEvent.setEvent(event);
					if (event == -1)
					{
						// It's a choice event
						String choiceEvent = navDM.getChoiceEvent();
						navigationEvent.setChoiceEvent(choiceEvent);
					}
				}
			}
		}

		scoBean.setTerminated(true);

		if (isSuccessful)
		{
			scoBean.setInitialized(false);
			synchResultWithGradebook(sessionBean);
		}

		if (dataManager != null)
		{
			dataManagerDao().update(dataManager);
		}

		log.debug("API Terminate (result): {}", isSuccessful);
		return isSuccessful;
	}

	private void updateDataManager(IValidRequests validRequests, IDataManager scoDataManager)
	{
		if (null != validRequests)
		{
			SCORM_2004_NAV_DM navDM = (SCORM_2004_NAV_DM) scoDataManager.getDataModel("adl");
			navDM.setValidRequests(validRequests);
			dataManagerDao().update(scoDataManager);
		}
		else
		{
			log.error("Current nav state is null!");
		}
	}

	/**
	 * Handles all aspects of updating the gradebook when a user completes a module.
	 * @param score contains the scaled score if one was recorded, otherwise empty Optional
	 * @param context the current site ID/gradebook ID
	 * @param learnerID the ID of the user who completed the module
	 * @param externalAssessmentID the ID of the gradebook item to sync with
	 */
	protected void updateGradebook(OptionalDouble score, String context, String learnerID, String externalAssessmentID)
	{
		GradingService gbService = gradingService();

		// Gradebook item exists, carry on...
		if (gbService.isExternalAssignmentDefined(context, externalAssessmentID))
		{
			long internalAssessmentID = gbService.getExternalAssignment(context, externalAssessmentID).getId();
			CommentDefinition cd = gbService.getAssignmentScoreComment(context, internalAssessmentID, learnerID);
			String existingComment = cd != null ? StringUtils.trimToEmpty(cd.getCommentText()) : "";
			String moduleNoScoreRecorded = resourceLoader.getString("moduleNoScoreRecorded");

			if (score.isPresent()) // Module recorded a score...
			{
				// A real number with values that is accurate to seven significant decimal figures. The value shall be in the range of -1.0 to 1.0, inclusive.
				double rawScore = score.getAsDouble() * 100d;

				// We don't care about the presence of an existing grade; push the new/updated one
				gbService.updateExternalAssessmentScore(context, context, externalAssessmentID, learnerID, "" + rawScore);

				// If there's an existing comment, we need to scan it for the presence of the "no grade recorded" message and remove it, but preserve any instructor added comments
				if (existingComment.contains(moduleNoScoreRecorded))
				{
					String comment = existingComment.replaceAll(moduleNoScoreRecorded, "");
					gbService.updateExternalAssessmentComment(context, context, externalAssessmentID, learnerID, comment);
				}
			}
			else // Module did not record a score...
			{
				// If there isn't already a comment indicating that this module didn't record grading data, add it
				if (!existingComment.contains(moduleNoScoreRecorded))
				{
					String comment = moduleNoScoreRecorded + " " + existingComment;
					gbService.updateExternalAssessmentComment(context, context, externalAssessmentID, learnerID, comment);
				}
			}
		}
	}
}
