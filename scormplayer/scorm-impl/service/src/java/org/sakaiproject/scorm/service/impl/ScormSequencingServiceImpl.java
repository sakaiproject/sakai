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

import javax.swing.tree.TreeModel;

import org.adl.api.ecmascript.APIErrorManager;
import org.adl.api.ecmascript.IErrorManager;
import org.adl.sequencer.ILaunch;
import org.adl.sequencer.ISeqActivity;
import org.adl.sequencer.ISeqActivityTree;
import org.adl.sequencer.ISequencer;
import org.adl.sequencer.IValidRequests;
import org.adl.sequencer.SeqNavRequests;
import org.adl.sequencer.impl.ADLLaunch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.scorm.adl.ADLConsultant;
import org.sakaiproject.scorm.dao.api.ActivityTreeHolderDao;
import org.sakaiproject.scorm.dao.api.AttemptDao;
import org.sakaiproject.scorm.dao.api.SeqActivityTreeDao;
import org.sakaiproject.scorm.model.api.ActivityTreeHolder;
import org.sakaiproject.scorm.model.api.Attempt;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.navigation.INavigable;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormSequencingService;

public abstract class ScormSequencingServiceImpl implements ScormSequencingService {

	private static Log log = LogFactory.getLog(ScormSequencingServiceImpl.class);
	
	// Dependency injection lookup methods
	protected abstract LearningManagementSystem lms();
	protected abstract ScormContentService scormContentService();
	
	// Local utility bean (also dependency injected by lookup method)
	protected abstract ADLConsultant adlManager();
	
	// Data access objects (also dependency injected by lookup method)
	protected abstract ActivityTreeHolderDao activityTreeHolderDao();
	protected abstract AttemptDao attemptDao();
	
	
	public String navigate(int request, SessionBean sessionBean, INavigable agent, Object target) {
		// SessionBean needs to be populated with courseId and learnerId by this point
		if (log.isDebugEnabled()) {
			log.debug("navigate (" + request + ")");
		
			if (sessionBean.getContentPackage() == null || sessionBean.getLearnerId() == null)
				log.error("Session bean should be populated with content package and learner id");
		}
			
		
		ISeqActivityTree tree = adlManager().getActivityTree(sessionBean);
		
		if (tree.getSuspendAll() != null && request == SeqNavRequests.NAV_START)
			request = SeqNavRequests.NAV_RESUMEALL;
		
		ISequencer sequencer = adlManager().getSequencer(tree);
		ILaunch launch = sequencer.navigate(request);
		ContentPackageManifest manifest = adlManager().getManifest(sessionBean);
		
		update(sessionBean, sequencer, launch, manifest);
		
		if (request == SeqNavRequests.NAV_SUSPENDALL) {
			Attempt attempt = sessionBean.getAttempt();
			if (attempt != null) {
				attempt.setSuspended(true);
				attemptDao().save(attempt);
			}
			sessionBean.setSuspended(true);
			sessionBean.setEnded(true);
			sessionBean.setStarted(false);
		} else if (request == SeqNavRequests.NAV_RESUMEALL)
			sessionBean.setSuspended(false);
		
		if (agent != null)
			agent.displayResource(sessionBean, target);
		
		String result = launch.getLaunchStatusNoContent();
		if ((request == SeqNavRequests.NAV_START || request == SeqNavRequests.NAV_RESUMEALL)) { // Start flag, check if the result is OK
			if (result == null) { // Result is null, so OK
				sessionBean.setStarted(true);
			} else if (launch.getNavState().isContinueEnabled() && ADLLaunch.LAUNCH_SEQ_BLOCKED.equals(result)) { // Expected to be blocked when there is no continue.
				sessionBean.setStarted(true);
			}
		}
		return result;
	}
	
	public void navigate(String choiceRequest, SessionBean sessionBean, INavigable agent, Object target) {
		if (choiceRequest == null) {
			if (log.isDebugEnabled())
				log.debug("navigate with null choice request, ignoring");
			return;
		}
			
		if (log.isDebugEnabled())
			log.debug("navigate (" + choiceRequest + ")");
		
		ISeqActivityTree tree = adlManager().getActivityTree(sessionBean);
		ISequencer sequencer = adlManager().getSequencer(tree);
		ILaunch launch = sequencer.navigate(choiceRequest);
		ContentPackageManifest manifest = adlManager().getManifest(sessionBean);
		
		update(sessionBean, sequencer, launch, manifest);
		
		if (agent != null)
			agent.displayResource(sessionBean, target);
	}
	
	public void navigateToActivity(String activityId, SessionBean sessionBean, INavigable agent, Object target) {
		ISeqActivityTree tree = adlManager().getActivityTree(sessionBean);
		ISequencer sequencer = adlManager().getSequencer(tree);
		sessionBean.setActivityId(activityId);
		
		if (log.isDebugEnabled())
			log.debug("navigate (" + sessionBean.getActivityId() + ")");
			
		ILaunch launch = sequencer.navigate(sessionBean.getActivityId());
		ContentPackageManifest manifest = adlManager().getManifest(sessionBean);
		
		update(sessionBean, sequencer, launch, manifest);
		
		if (agent != null)
			agent.displayResource(sessionBean, target);
	}
	
	public SessionBean newSessionBean(ContentPackage contentPackage) {
		String learnerId = lms().currentLearnerId();
		SessionBean sessionBean = new SessionBean(learnerId, contentPackage);
		
		IErrorManager errorManager = new APIErrorManager(IErrorManager.SCORM_2004_API);
		sessionBean.setErrorManager(errorManager);
		
		return sessionBean;
	}

	public boolean isContinueEnabled(SessionBean sessionBean) {
		IValidRequests state = sessionBean.getNavigationState();
		
		return null != state && state.isContinueEnabled() && isControlModeFlow(sessionBean);
	}
	
	public boolean isContinueExitEnabled(SessionBean sessionBean) {
		IValidRequests state = sessionBean.getNavigationState();
		
		return null != state && state.isContinueExitEnabled() && isControlModeFlow(sessionBean);
	}
	
	public boolean isPreviousEnabled(SessionBean sessionBean) {
		IValidRequests state = sessionBean.getNavigationState();
		
		return null != state && state.isPreviousEnabled() && !isControlForwardOnly(sessionBean) && isControlModeFlow(sessionBean);
	}

	public boolean isResumeEnabled(SessionBean sessionBean) {
		IValidRequests state = sessionBean.getNavigationState();
		
		return null != state && state.isResumeEnabled();
	}
	
	public boolean isStartEnabled(SessionBean sessionBean) {
		IValidRequests state = sessionBean.getNavigationState();
		
		return null != state && state.isStartEnabled();
	}
	
	public boolean isSuspendEnabled(SessionBean sessionBean) {
		IValidRequests state = sessionBean.getNavigationState();
		
		return null != state && state.isSuspendEnabled();
	}
	
	public boolean isControlModeFlow(SessionBean sessionBean) {
		ISeqActivity activity = getActivity(sessionBean);
		
		// Default value from spec
		if (activity == null)
			return false;
		
		return activity.getControlModeFlow();
	}
	
	public boolean isControlModeChoice(SessionBean sessionBean) {
		ISeqActivity activity = getActivity(sessionBean);
		
		// Default value from spec
		if (activity == null)
			return true;
		
		return activity.getControlModeChoice();
	}
	
	public boolean isControlModeChoiceExit(SessionBean sessionBean) {
		ISeqActivity activity = getActivity(sessionBean);
		
		// Default value from spec
		if (activity == null)
			return true;
		
		return activity.getControlModeChoiceExit();
	}
	
	public boolean isControlForwardOnly(SessionBean sessionBean) {
		ISeqActivity activity = getActivity(sessionBean);
		
		// Default value from spec
		if (activity == null)
			return false;
		
		return activity.getControlForwardOnly();
	}
	
	public TreeModel getTreeModel(SessionBean sessionBean) {
		IValidRequests requests = sessionBean.getNavigationState();
		
		if (null != requests)
			return requests.getTreeModel();
		
		return null;
	}
	
	private ISeqActivity getActivity(SessionBean sessionBean) {
		ISeqActivityTree tree = adlManager().getActivityTree(sessionBean);
		
		if (tree != null) {
			String activityId = sessionBean.getActivityId();
			
			if (activityId != null)
				return tree.getActivity(activityId);
		}
		
		return null;
	}
	
	
	private void update(SessionBean sessionBean, ISequencer sequencer, ILaunch launch, ContentPackageManifest manifest) {
		sessionBean.setActivityId(launch.getActivityId());
		sessionBean.setScoId(launch.getSco());
		sessionBean.setNavigationState(launch.getNavState());
		sessionBean.setLaunchData(manifest.getLaunchData(sessionBean.getScoId()));
		// FIXME: Currently, not setting this will break the CHH implementation
		//sessionBean.setBaseUrl(manifest.getResourceId());
		sessionBean.setObjectiveStatusSet(sequencer.getObjStatusSet(launch.getActivityId()));
		
		if (log.isDebugEnabled())
			log.debug("SCO is " + launch.getSco());
		
		String status = launch.getLaunchStatusNoContent();
		
		// If its an END_SESSION, clear the active activity
        if ((status != null) && (status.equals("_ENDSESSION_") || status.equals("_COURSECOMPLETE_") 
        		|| status.equals("_SEQABANDONALL_")) ) {
        	sequencer.clearSeqState();
        	
        	if (log.isDebugEnabled())
        		log.debug("Status is " + status + " -- ending course!");
        	if (!sessionBean.isRestart())
        		sessionBean.setEnded(true);
        	else
        		sessionBean.setRestart(false);
        } 
        
		ActivityTreeHolder treeHolder = sessionBean.getTreeHolder();
		ISeqActivityTree tree = null;
		
		if (treeHolder == null) {
			log.error("Could not find a tree holder!!!");
			return;
		}
		
		tree = treeHolder.getSeqActivityTree();
		
		if (tree == null) {
			log.error("Could not find a tree!!!");
			return;
		}
        
        ISeqActivity activity = tree.getActivity(sessionBean.getActivityId());
	        
	    if (activity != null)
	      	sessionBean.setActivityTitle(activity.getTitle());
	    else 
	       	log.debug("Activity is null!!!");
	        
	    activityTreeHolderDao().save(treeHolder);
        
	}
	
}
