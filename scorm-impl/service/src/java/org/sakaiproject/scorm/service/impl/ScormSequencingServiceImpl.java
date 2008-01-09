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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.scorm.adl.ADLConsultant;
import org.sakaiproject.scorm.dao.api.SeqActivityTreeDao;
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
	protected abstract SeqActivityTreeDao seqActivityTreeDao();
	
	
	
	public String navigate(int request, SessionBean sessionBean, INavigable agent, Object target) {
		// SessionBean needs to be populated with courseId and learnerId by this point
		if (log.isDebugEnabled()) {
			log.debug("navigate (" + request + ")");
		
			if (sessionBean.getCourseId() == null || sessionBean.getLearnerId() == null)
				log.error("Session bean should be populated with course id and learner id");
		}
			
		
		ISeqActivityTree tree = adlManager().getActivityTree(sessionBean);
		
		if (tree.getSuspendAll() != null && request == SeqNavRequests.NAV_START)
			request = SeqNavRequests.NAV_RESUMEALL;
		
		ISequencer sequencer = adlManager().getSequencer(tree);
		ILaunch launch = sequencer.navigate(request);
		ContentPackageManifest manifest = adlManager().getManifest(sessionBean);
		
		update(sessionBean, sequencer, launch, manifest);
		
		if (request == SeqNavRequests.NAV_SUSPENDALL) 
			sessionBean.setSuspended(true);
		else if (request == SeqNavRequests.NAV_RESUMEALL)
			sessionBean.setSuspended(false);
		
		if (agent != null)
			agent.displayResource(sessionBean, target);
		
		String result = launch.getLaunchStatusNoContent();
		
		if (result == null && (request == SeqNavRequests.NAV_START || request == SeqNavRequests.NAV_RESUMEALL))
			sessionBean.setStarted(true);
			
		return result;
	}
	
	public void navigate(String choiceRequest, SessionBean sessionBean, INavigable agent, Object target) {
		if (choiceRequest == null) {
			if (log.isInfoEnabled())
				log.info("navigate with null choice request, ignoring");
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
	
	public SessionBean newSessionBean(String courseId, long contentPackageId) {
		String learnerId = lms().currentLearnerId();
		SessionBean sessionBean = new SessionBean(courseId, learnerId, contentPackageId);
		
		ContentPackageManifest manifest = adlManager().getManifest(sessionBean);
		
		if (manifest != null)
			sessionBean.setTitle(manifest.getTitle());
		else 
			log.error("Could not retrieve manifest for this Scorm Package: " + courseId);
		
		IErrorManager errorManager = new APIErrorManager(IErrorManager.SCORM_2004_API);
		sessionBean.setErrorManager(errorManager);
		
		return sessionBean;
	}

	public boolean isContinueEnabled(SessionBean sessionBean) {
		IValidRequests state = sessionBean.getNavigationState();
		
		return null != state && state.isContinueEnabled();
	}
	
	public boolean isContinueExitEnabled(SessionBean sessionBean) {
		IValidRequests state = sessionBean.getNavigationState();
		
		return null != state && state.isContinueExitEnabled();
	}
	
	public boolean isPreviousEnabled(SessionBean sessionBean) {
		IValidRequests state = sessionBean.getNavigationState();
		
		return null != state && state.isPreviousEnabled();
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
		ISeqActivityTree tree = adlManager().getActivityTree(sessionBean);
		String activityId = sessionBean.getActivityId();
		
		ISeqActivity activity = tree.getActivity(activityId);
		
		return null != activity && activity.getControlModeFlow();
	}
	
	public boolean isControlModeChoice(SessionBean sessionBean) {
		ISeqActivityTree tree = adlManager().getActivityTree(sessionBean);
		String activityId = sessionBean.getActivityId();
		
		ISeqActivity activity = tree.getActivity(activityId);
		
		return null != activity && activity.getControlModeChoice();
	}

	
	/*public String getCurrentUrl(SessionBean sessionBean) {
		log.warn("THIS IS BROKEN -- sessionBean.getBaseUrl will return NULL");
		
		if (null != sessionBean.getLaunchData()) {
			String launchLine = sessionBean.getLaunchData().getLaunchLine();
			String baseUrl = sessionBean.getBaseUrl();
			StringBuffer fullPath = new StringBuffer().append(baseUrl);
			
			if (!baseUrl.endsWith(Entity.SEPARATOR) && !launchLine.startsWith(Entity.SEPARATOR))
				fullPath.append(Entity.SEPARATOR);

			fullPath.append(launchLine);
						
			return fullPath.toString();
		}
		return null;
	}*/
	
	public TreeModel getTreeModel(SessionBean sessionBean) {
		IValidRequests requests = sessionBean.getNavigationState();
		
		if (null != requests)
			return requests.getTreeModel();
		
		return null;
	}
	
	
	
	private void update(SessionBean sessionBean, ISequencer sequencer, ILaunch launch, ContentPackageManifest manifest) {
		sessionBean.setActivityId(launch.getActivityId());
		sessionBean.setScoId(launch.getSco());
		sessionBean.setNavigationState(launch.getNavState());
		sessionBean.setLaunchData(manifest.getLaunchData(sessionBean.getScoId()));
		sessionBean.setBaseUrl(manifest.getResourceId());
		sessionBean.setObjectiveStatusSet(sequencer.getObjStatusSet(launch.getActivityId()));
		
		if (log.isDebugEnabled())
			log.debug("SCO is " + launch.getSco());
		
		String status = launch.getLaunchStatusNoContent();
		
		// If its an END_SESSION, clear the active activity
        if ((status != null) && (status.equals("_ENDSESSION_") || status.equals("_COURSECOMPLETE_") 
        		|| status.equals("_SEQABANDONALL_")) ) {
        	sequencer.clearSeqState();
        	
        	if (log.isWarnEnabled())
        		log.warn("Status is " + status + " -- ending course!");
        	if (!sessionBean.isRestart())
        		sessionBean.setEnded(true);
        	else
        		sessionBean.setRestart(false);
        } 
        
        ISeqActivityTree tree = sessionBean.getTree();
        
        if (tree != null) {
	        ISeqActivity activity = tree.getActivity(sessionBean.getActivityId());
	        
	        if (activity != null)
	        	sessionBean.setActivityTitle(activity.getTitle());
	        else 
	        	log.warn("Activity is null!!!");
	        
	        seqActivityTreeDao().save(tree);
        } else {
        	log.warn("Seq activity tree is null!!!");
        }
	}
	
}
