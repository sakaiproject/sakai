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

import javax.swing.tree.TreeModel;

import lombok.extern.slf4j.Slf4j;

import org.adl.api.ecmascript.APIErrorManager;
import org.adl.api.ecmascript.IErrorManager;
import org.adl.datamodels.DMInterface;
import org.adl.datamodels.IDataManager;
import org.adl.datamodels.ieee.IValidatorFactory;
import org.adl.datamodels.ieee.ValidatorFactory;
import org.adl.sequencer.ILaunch;
import org.adl.sequencer.ISeqActivity;
import org.adl.sequencer.ISeqActivityTree;
import org.adl.sequencer.ISequencer;
import org.adl.sequencer.IValidRequests;
import org.adl.sequencer.SeqNavRequests;
import org.adl.sequencer.impl.ADLLaunch;

import org.apache.commons.lang.StringUtils;

import org.sakaiproject.scorm.adl.ADLConsultant;
import org.sakaiproject.scorm.dao.api.ActivityTreeHolderDao;
import org.sakaiproject.scorm.dao.api.AttemptDao;
import org.sakaiproject.scorm.dao.api.DataManagerDao;
import org.sakaiproject.scorm.model.api.ActivityTreeHolder;
import org.sakaiproject.scorm.model.api.Attempt;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;
import org.sakaiproject.scorm.model.api.ScoBean;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.navigation.INavigable;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormSequencingService;

@Slf4j
public abstract class ScormSequencingServiceImpl implements ScormSequencingService
{
	// Data access objects (also dependency injected by lookup method)
	protected abstract ActivityTreeHolderDao activityTreeHolderDao();
	protected abstract AttemptDao attemptDao();
	protected abstract DataManagerDao dataManagerDao();

	// Local utility bean (also dependency injected by lookup method)
	protected abstract ADLConsultant adlManager();

	// Dependency injection lookup methods
	protected abstract LearningManagementSystem lms();
	protected abstract ScormContentService scormContentService();

	IValidatorFactory validatorFactory = new ValidatorFactory();

	private ISeqActivity getActivity(SessionBean sessionBean)
	{
		ISeqActivityTree tree = adlManager().getActivityTree(sessionBean);
		if (tree != null)
		{
			String activityId = sessionBean.getActivityId();
			if (activityId != null)
			{
				return tree.getActivity(activityId);
			}
		}

		return null;
	}

	@Override
	public TreeModel getTreeModel(SessionBean sessionBean)
	{
		IValidRequests requests = sessionBean.getNavigationState();
		return requests != null ? requests.getTreeModel() : null;
	}

	@Override
	public boolean isContinueEnabled(SessionBean sessionBean)
	{
		IValidRequests state = sessionBean.getNavigationState();
		return null != state && state.isContinueEnabled() && isControlModeFlow(sessionBean);
	}

	@Override
	public boolean isContinueExitEnabled(SessionBean sessionBean)
	{
		IValidRequests state = sessionBean.getNavigationState();
		return null != state && state.isContinueExitEnabled() && isControlModeFlow(sessionBean);
	}

	@Override
	public boolean isControlForwardOnly(SessionBean sessionBean)
	{
		ISeqActivity activity = getActivity(sessionBean);

		// Default value from spec
		return activity == null ? false : activity.getControlForwardOnly();
	}

	@Override
	public boolean isControlModeChoice(SessionBean sessionBean)
	{
		ISeqActivity activity = getActivity(sessionBean);

		// Default value from spec
		return activity == null ? true : activity.getControlModeChoice();
	}

	@Override
	public boolean isControlModeChoiceExit(SessionBean sessionBean)
	{
		ISeqActivity activity = getActivity(sessionBean);

		// Default value from spec
		return activity == null ? true : activity.getControlModeChoiceExit();
	}

	@Override
	public boolean isControlModeFlow(SessionBean sessionBean)
	{
		ISeqActivity activity = getActivity(sessionBean);

		// Default value from spec
		return activity == null ? false : activity.getControlModeFlow();
	}

	@Override
	public boolean isPreviousEnabled(SessionBean sessionBean)
	{
		IValidRequests state = sessionBean.getNavigationState();
		return null != state && state.isPreviousEnabled() && !isControlForwardOnly(sessionBean) && isControlModeFlow(sessionBean);
	}

	@Override
	public boolean isResumeEnabled(SessionBean sessionBean)
	{
		IValidRequests state = sessionBean.getNavigationState();
		return null != state && state.isResumeEnabled();
	}

	@Override
	public boolean isStartEnabled(SessionBean sessionBean)
	{
		IValidRequests state = sessionBean.getNavigationState();
		return null != state && state.isStartEnabled();
	}

	@Override
	public boolean isSuspendEnabled(SessionBean sessionBean)
	{
		IValidRequests state = sessionBean.getNavigationState();
		return null != state && state.isSuspendEnabled();
	}

	@Override
	public String navigate(int request, SessionBean sessionBean, INavigable agent, Object target)
	{
		// SessionBean needs to be populated with courseId and learnerId by this point
		log.debug("navigate ({})", request);
		if (sessionBean.getContentPackage() == null || sessionBean.getLearnerId() == null)
		{
			log.error("Session bean should be populated with content package and learner id");
		}

		ISeqActivityTree tree = adlManager().getActivityTree(sessionBean);
		if (tree.getSuspendAll() != null && request == SeqNavRequests.NAV_START)
		{
			request = SeqNavRequests.NAV_RESUMEALL;
		}

		if (request == SeqNavRequests.NAV_EXITALL)
		{
			ScoBean displayingSco = sessionBean.getDisplayingSco();
			if (displayingSco != null)
			{
				IDataManager dataManager = dataManagerDao().load(displayingSco.getDataManagerId());
				if (dataManager != null)
				{
					DMInterface.processSetValue("adl.nav.request", "_none_", true, dataManager, validatorFactory);
					dataManagerDao().update(dataManager);
				}
			}
		}

		ISequencer sequencer = adlManager().getSequencer(tree);
		ILaunch launch = sequencer.navigate(request);
		ContentPackageManifest manifest = adlManager().getManifest(sessionBean);

		update(sessionBean, sequencer, launch, manifest);
		String result = launch.getLaunchStatusNoContent();

		// Get the attempt
		Attempt attempt = sessionBean.getAttempt();
		// Manage start
		if (request == SeqNavRequests.NAV_START || request == SeqNavRequests.NAV_RESUMEALL)
		{
			// Possible correct outcomes are null or _TOC_
			if (StringUtils.isEmpty(result) || StringUtils.equals(result, ADLLaunch.LAUNCH_TOC) || (StringUtils.equals(result, ADLLaunch.LAUNCH_SEQ_BLOCKED)
					&& launch.getNavState().isContinueEnabled()))
			{
				if (attempt != null)
				{
					attempt.setNotExited(true);
					attempt.setSuspended(false);
				}

				sessionBean.setSuspended(false);
				sessionBean.setStarted(true);
				sessionBean.setEnded(false);
			}
		}

		// The user selects to end & stop
		if (request == SeqNavRequests.NAV_EXITALL || request == SeqNavRequests.NAV_ABANDONALL)
		{
			// Possible outcomes are exit or complete 
			if (StringUtils.equals(result, ADLLaunch.LAUNCH_EXITSESSION) || StringUtils.equals(result, ADLLaunch.LAUNCH_COURSECOMPLETE))
			{
				if (attempt != null)
				{
					attempt.setNotExited(false);
					attempt.setSuspended(false);
				}

				sessionBean.setSuspended(false);
				sessionBean.setEnded(true);
				sessionBean.setStarted(false);
			}
		}
		else if (request == SeqNavRequests.NAV_SUSPENDALL)
		{
			// Possible outcome is _ENDSESSION_
			if (StringUtils.equals(result, ADLLaunch.LAUNCH_EXITSESSION))
			{
				if (attempt != null)
				{
					attempt.setSuspended(true);
					attempt.setNotExited(false);
				}

				sessionBean.setSuspended(true);
				sessionBean.setEnded(true);
				sessionBean.setStarted(false);
			}
		}
		else
		{
			// This is the fallback. If the sequencer legally decided that the session is over, just check the result for such a state.
			if (StringUtils.equals(result, ADLLaunch.LAUNCH_EXITSESSION) || StringUtils.equals(result, ADLLaunch.LAUNCH_COURSECOMPLETE)
					|| StringUtils.equals(result, ADLLaunch.LAUNCH_COURSECOMPLETE))
			{
				if (attempt != null)
				{
					attempt.setSuspended(false);
					attempt.setNotExited(false);
				}

				sessionBean.setSuspended(false);
				sessionBean.setEnded(true);
				sessionBean.setStarted(true);
			}
		}

		if (attempt != null)
		{
			attemptDao().save(attempt);
		}

		// Very important, call AFTER session bean values are set!
		if (agent != null)
		{
			agent.displayResource(sessionBean, target);
		}

		return result;
	}

	@Override
	public void navigate(String choiceRequest, SessionBean sessionBean, INavigable agent, Object target)
	{
		if (choiceRequest == null)
		{
			log.debug("navigate with null choice request, ignoring");
			return;
		}

		log.debug("navigate ({})", choiceRequest);

		ISeqActivityTree tree = adlManager().getActivityTree(sessionBean);
		ISequencer sequencer = adlManager().getSequencer(tree);
		ILaunch launch = sequencer.navigate(choiceRequest);
		ContentPackageManifest manifest = adlManager().getManifest(sessionBean);

		update(sessionBean, sequencer, launch, manifest);

		if (agent != null)
		{
			agent.displayResource(sessionBean, target);
		}
	}

	@Override
	public void navigateToActivity(String activityId, SessionBean sessionBean, INavigable agent, Object target)
	{
		ISeqActivityTree tree = adlManager().getActivityTree(sessionBean);
		ISequencer sequencer = adlManager().getSequencer(tree);
		sessionBean.setActivityId(activityId);

		log.debug("navigate ({})", sessionBean.getActivityId());

		ILaunch launch = sequencer.navigate(sessionBean.getActivityId());
		ContentPackageManifest manifest = adlManager().getManifest(sessionBean);

		update(sessionBean, sequencer, launch, manifest);

		if (agent != null)
		{
			agent.displayResource(sessionBean, target);
		}
	}

	@Override
	public SessionBean newSessionBean(ContentPackage contentPackage)
	{
		String learnerId = lms().currentLearnerId();
		SessionBean sessionBean = new SessionBean(learnerId, contentPackage);
		IErrorManager errorManager = new APIErrorManager(IErrorManager.SCORM_2004_API);
		sessionBean.setErrorManager(errorManager);
		return sessionBean;
	}

	private void update(SessionBean sessionBean, ISequencer sequencer, ILaunch launch, ContentPackageManifest manifest)
	{
		sessionBean.setActivityId(launch.getActivityId());
		sessionBean.setScoId(launch.getSco());
		sessionBean.setNavigationState(launch.getNavState());
		sessionBean.setLaunchData(manifest.getLaunchData(sessionBean.getScoId()));
		// FIXME: Currently, not setting this will break the CHH implementation
		//sessionBean.setBaseUrl(manifest.getResourceId());
		sessionBean.setObjectiveStatusSet(sequencer.getObjStatusSet(launch.getActivityId()));

		log.debug("SCO is {}", launch.getSco());

		String status = launch.getLaunchStatusNoContent();

		// If its an END_SESSION, clear the active activity
		if ((status != null) && (status.equals("_ENDSESSION_") || status.equals("_COURSECOMPLETE_") || status.equals("_SEQABANDONALL_")))
		{
			sequencer.clearSeqState();
			log.debug("Status is {} -- ending course!", status);
		}

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

		ISeqActivity activity = tree.getActivity(sessionBean.getActivityId());
		if (activity != null)
		{
			sessionBean.setActivityTitle(activity.getTitle());
		}
		else
		{
			log.debug("Activity is null!!!");
		}

		activityTreeHolderDao().save(treeHolder);
	}
}
