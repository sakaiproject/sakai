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
import org.adl.datamodels.IDataManager;
import org.adl.sequencer.ILaunch;
import org.adl.sequencer.ISeqActivity;
import org.adl.sequencer.ISeqActivityTree;
import org.adl.sequencer.ISequencer;
import org.adl.sequencer.IValidRequests;
import org.adl.sequencer.SeqNavRequests;
import org.adl.sequencer.impl.ADLSequencer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.scorm.dao.api.ContentPackageManifestDao;
import org.sakaiproject.scorm.dao.api.DataManagerDao;
import org.sakaiproject.scorm.dao.api.SeqActivityTreeDao;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.service.api.INavigable;
import org.sakaiproject.scorm.service.api.ScoBean;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormSequencingService;
import org.sakaiproject.tool.api.SessionManager;

public abstract class ScormSequencingServiceImpl implements ScormSequencingService {

	private static Log log = LogFactory.getLog(ScormSequencingServiceImpl.class);
	
	// Dependency injection lookup methods
	protected abstract ScormContentService scormContentService();
	protected abstract DataManagerDao dataManagerDao();
	protected abstract SeqActivityTreeDao seqActivityTreeDao();
	protected abstract SessionManager sessionManager();
	protected abstract ContentPackageManifestDao contentPackageManifestDao();
	
	
	public String navigate(int request, SessionBean sessionBean, INavigable agent, Object target) {
		// SessionBean needs to be populated with courseId and learnerId by this point
		
		if (log.isDebugEnabled())
			log.debug("navigate (" + request + ")");
		
		ISeqActivityTree tree = getActivityTree(sessionBean);
		
		if (tree.getSuspendAll() != null && request == SeqNavRequests.NAV_START)
			request = SeqNavRequests.NAV_RESUMEALL;
		
		ISequencer sequencer = getSequencer(tree);
		ILaunch launch = sequencer.navigate(request);
		ContentPackageManifest manifest = getManifest(sessionBean);
		
		update(sessionBean, sequencer, launch, manifest);
		
		if (request == SeqNavRequests.NAV_SUSPENDALL) 
			sessionBean.setSuspended(true);
		else if (request == SeqNavRequests.NAV_RESUMEALL)
			sessionBean.setSuspended(false);
		
		if (agent != null)
			agent.displayContent(sessionBean, target);
		
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
		
		ISeqActivityTree tree = getActivityTree(sessionBean);
		ISequencer sequencer = getSequencer(tree);
		ILaunch launch = sequencer.navigate(choiceRequest);
		ContentPackageManifest manifest = getManifest(sessionBean);
		
		update(sessionBean, sequencer, launch, manifest);
		
		if (agent != null)
			agent.displayContent(sessionBean, target);
	}
	
	public void navigateToActivity(String activityId, SessionBean sessionBean, INavigable agent, Object target) {
		ISeqActivityTree tree = getActivityTree(sessionBean);
		ISequencer sequencer = getSequencer(tree);
		sessionBean.setActivityId(activityId);
		
		if (log.isDebugEnabled())
			log.debug("navigate (" + sessionBean.getActivityId() + ")");
			
		ILaunch launch = sequencer.navigate(sessionBean.getActivityId());
		ContentPackageManifest manifest = getManifest(sessionBean);
		
		update(sessionBean, sequencer, launch, manifest);
		
		if (agent != null)
			agent.displayContent(sessionBean, target);
	}
	
	public SessionBean newSessionBean(String courseId) {
		String learnerId = sessionManager().getCurrentSessionUserId();
		SessionBean sessionBean = new SessionBean(courseId, learnerId);
		
		ContentPackageManifest manifest = getManifest(sessionBean);
		
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
		ISeqActivityTree tree = getActivityTree(sessionBean);
		String activityId = sessionBean.getActivityId();
		
		ISeqActivity activity = tree.getActivity(activityId);
		
		return null != activity && activity.getControlModeFlow();
	}
	
	public boolean isControlModeChoice(SessionBean sessionBean) {
		ISeqActivityTree tree = getActivityTree(sessionBean);
		String activityId = sessionBean.getActivityId();
		
		ISeqActivity activity = tree.getActivity(activityId);
		
		return null != activity && activity.getControlModeChoice();
	}

	
	public String getCurrentUrl(SessionBean sessionBean) {
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
	}
	
	public TreeModel getTreeModel(SessionBean sessionBean) {
		IValidRequests requests = sessionBean.getNavigationState();
		
		if (null != requests)
			return requests.getTreeModel();
		
		return null;
	}
	
	public IDataManager getDataManager(SessionBean sessionBean, ScoBean scoBean) {
		if (sessionBean.getDataManager() == null)
			sessionBean.setDataManager(dataManagerDao().find(sessionBean.getCourseId(), sessionBean.getLearnerId(), sessionBean.getAttemptNumber()));
		
		return sessionBean.getDataManager();
	}
	
	
	public ISeqActivityTree getActivityTree(SessionBean sessionBean) {
		// First, we check to see if the tree is cached in the session bean 
		ISeqActivityTree tree = sessionBean.getTree();
		
		if (tree == null) {
			// If not, we look to see if there's a modified version in the data store
			tree = seqActivityTreeDao().find(sessionBean.getCourseId(), sessionBean.getLearnerId());
			
			if (tree == null) {
				// Finally, if all else fails, we look up the prototype version - this is the first time
				// the user has launched the content package
				ContentPackageManifest manifest = getManifest(sessionBean);
				tree = manifest.getActTreePrototype();
				tree.setCourseID(sessionBean.getCourseId());
				tree.setLearnerID(sessionBean.getLearnerId());
			}
			
			sessionBean.setTree(tree);
		}
		
		return tree;
	}
	
	public ISequencer getSequencer(ISeqActivityTree tree) {
        // Create the sequencer and set the tree		
        ISequencer sequencer = new ADLSequencer();
        sequencer.setActivityTree(tree);
        
        return sequencer;
	}
	
	public ContentPackageManifest getManifest(SessionBean sessionBean) {
		// First, check to see if the manifest is cached in the session bean
		ContentPackageManifest manifest = sessionBean.getManifest();
		
		if (manifest == null)
			manifest = contentPackageManifestDao().find(sessionBean.getCourseId());
			
		return manifest;
	}
	
	private void update(SessionBean sessionBean, ISequencer sequencer, ILaunch launch, ContentPackageManifest manifest) {
		sessionBean.setActivityId(launch.getActivityId());
		sessionBean.setScoId(launch.getSco());
		sessionBean.setNavigationState(launch.getNavState());
		sessionBean.setLaunchData(manifest.getLaunchData(sessionBean.getScoId()));
		sessionBean.setBaseUrl(manifest.getResourceId());
		sessionBean.setObjectiveStatusSet(sequencer.getObjStatusSet(launch.getActivityId()));
		
		String status = launch.getLaunchStatusNoContent();
		
		// If its an END_SESSION, clear the active activity
        if ((status != null) && (status.equals("_ENDSESSION_") || status.equals("_COURSECOMPLETE_") 
        		|| status.equals("_SEQABANDONALL_")) ) {
        	sequencer.clearSeqState();
        	
        	log.warn("Status is " + status + " -- ending course!");
        	if (!sessionBean.isRestart())
        		sessionBean.setEnded(true);
        	else
        		sessionBean.setRestart(false);
        } 
        
        seqActivityTreeDao().save(sessionBean.getTree());
	}
	
	
}
