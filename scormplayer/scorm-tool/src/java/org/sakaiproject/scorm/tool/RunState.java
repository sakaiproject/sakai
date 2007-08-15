package org.sakaiproject.scorm.tool;

import java.io.Serializable;
import java.util.List;
import java.util.Hashtable;
import java.util.Map;

import org.adl.datamodels.IDataManager;
import org.adl.sequencer.ILaunch;
import org.adl.sequencer.ISeqActivity;
import org.adl.sequencer.ISeqActivityTree;
import org.adl.sequencer.ISequencer;
import org.adl.sequencer.IValidRequests;
import org.adl.sequencer.SeqNavRequests;
import org.adl.validator.contentpackage.ILaunchData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.scorm.client.api.IRunState;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;

public class RunState implements IRunState {
	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(RunState.class);
	
	private ContentPackageManifest manifest;
	private String currentActivityId;
	private ILaunchData currentLaunchData;
	private IValidRequests currentNavState;
	private String currentSco;
	private String currentUserId;
	private String currentCourseId;
	private List currentObjStatusSet;
	
	private Map<String, ScoBean> scoBeans;
	
	private IDataManager dataManager;
	private ISeqActivityTree activityTree = null;
	
	private boolean isSuspended = false;
	
	private String baseHref;
	private String contentPackageId;
	
	ScormClientFacade clientFacade;
	
	public RunState(ScormClientFacade clientFacade, String contentPackageId, String courseId, String userId) {
		this.clientFacade = clientFacade;
		this.contentPackageId = contentPackageId;
		manifest = clientFacade.getManifest(contentPackageId);
		baseHref = manifest.getResourceId();
		currentUserId = userId;
		currentCourseId = courseId;
		scoBeans = new Hashtable<String, ScoBean>();
	}
	
	public ScoBean produceScoBean(String scoId) {
		ScoBean scoBean = null;
		
		if (null == scoId) {
			scoId = getCurrentSco();
			if (log.isDebugEnabled())
				log.debug("Null sco id -- grabbing current sco id " + scoId);
		}
		
		if (null != scoId && scoBeans.containsKey(scoId)) {
			scoBean = scoBeans.get(scoId);
		} else {
			if (log.isDebugEnabled())
				log.debug("Creating a new ScoBean for the Sco " + scoId);
			scoBean = new ScoBean(clientFacade, this);
			
			if (null != scoId) {
				scoBeans.put(scoId, scoBean);
			}
		}
		
		if (log.isDebugEnabled())
			log.debug("SCO is " + scoId);
		
		return scoBean;
	}
	
	public void discardScoBean(String scoId) {
		if (null != scoId && scoBeans.containsKey(scoId)) {
			if (log.isDebugEnabled())
				log.debug("Discarding the ScoBean for the Sco " + scoId);
			scoBeans.remove(scoId);
		}
	}
	
	private void initialize(ISequencer sequencer, ILaunch launch) {
		this.currentActivityId = launch.getActivityId();
		this.currentSco = launch.getSco();
		this.currentNavState = launch.getNavState();
		this.currentLaunchData = manifest.getLaunchData(currentSco);
		this.currentObjStatusSet = sequencer.getObjStatusSet(currentActivityId);
		
		this.activityTree = sequencer.getActivityTree();
		
		String status = launch.getLaunchStatusNoContent();
		
		// If its an END_SESSION, clear the active activity
        if ((status != null) && (status.equals("_ENDSESSION_") || status.equals("_COURSECOMPLETE_") 
        		|| status.equals("_SEQABANDONALL_")) ) {
        	getSequencer().clearSeqState();
        	
        	// FIXME: Add a special 'end session' state.
        } 
	}
	
	private void displayContent(Object target) {
		if (null == target)
			return;
		
		String url = getCurrentHref();
		if (null != url) {
			if (log.isDebugEnabled())
				log.debug("Going to " + url);

			((AjaxRequestTarget)target).appendJavascript("parent.content.location.href='" + url + "'");
		} else {
			log.warn("Url is null!");
		}
	}
	
	public String navigate(int navRequest, Object target) {
		log.info("navigate (" + navRequest + ")");
		ISequencer sequencer = getSequencer();
		ILaunch launch = sequencer.navigate(navRequest);
		initialize(sequencer, launch);
		
		if (navRequest == SeqNavRequests.NAV_SUSPENDALL) 
			isSuspended = true;
		else if (navRequest == SeqNavRequests.NAV_RESUMEALL)
			isSuspended = false;
		
		displayContent(target);
		
		return launch.getLaunchStatusNoContent();
	}
	
	public void navigate(String navRequest, Object target) {
		log.info("navigate (" + navRequest + ")");
		ISequencer sequencer = getSequencer();
		ILaunch launch = sequencer.navigate(navRequest);
		initialize(sequencer, launch);
		displayContent(target);
	}
	
	public void navigate(ISeqActivity activity, Object target) {
		ISequencer sequencer = getSequencer();
		this.currentActivityId = activity.getID();
		
		if (log.isDebugEnabled())
			log.debug("navigate (" + currentActivityId + ")");
			
		ILaunch launch = sequencer.navigate(currentActivityId);
		initialize(sequencer, launch);
		displayContent(target);
	}
	
	public ISeqActivityTree getActivityTree(boolean isFresh) {
		if (activityTree == null)
			activityTree = clientFacade.getActivityTree(contentPackageId, currentCourseId, currentUserId, isFresh);
	
		return activityTree;
	}
	
	public ISequencer getSequencer() {
		ISeqActivityTree tree = getActivityTree(true);
		
		return clientFacade.getSequencer(tree);
	}
	
	public ISeqActivity getCurrentActivity() {
		ISeqActivityTree tree = getActivityTree(false);
		
		return tree.getActivity(currentActivityId);
	}
	
	public String getCurrentActivityId() {
		return currentActivityId;
	}
	
	public String getCurrentHref() {
		if (null != currentLaunchData) {
			String launchLine = currentLaunchData.getLaunchLine();
			StringBuffer fullPath = new StringBuffer().append(baseHref);
			
			if (!baseHref.endsWith(Entity.SEPARATOR) && !launchLine.startsWith(Entity.SEPARATOR))
				fullPath.append(Entity.SEPARATOR);
				
			fullPath.append(launchLine);
			
			return fullPath.toString();
		}
		return null;
	}
	
	public ILaunchData getCurrentLaunchData() {
		return currentLaunchData;
	}
	
	public IValidRequests getCurrentNavState() {
		return currentNavState;
	}
	
	public String getCurrentUserId() {
		return currentUserId;
	}
	
	public String getCurrentCourseId() {
		return currentCourseId;
	}
	
	public String getCurrentSco() {
		return currentSco;
	}
	
	public boolean isSuspended() {
		return isSuspended;
	}
	
	public void setSuspended(boolean isSuspended) {
		this.isSuspended = isSuspended;
	}
	
	public boolean isTreeVisible() {
		ISeqActivity activity = getCurrentActivity();
		return null != activity && activity.getControlModeChoice();
	}
	
	public boolean isNextVisible() {
		ISeqActivity activity = getCurrentActivity();
		return null != activity && activity.getControlModeFlow();
	}
	
	public boolean isContinueEnabled() {
		return null != currentNavState && currentNavState.isContinueEnabled();
	}
	
	public boolean isContinueExitEnabled() {
		return null != currentNavState && currentNavState.isContinueExitEnabled();
	}
	
	public boolean isPreviousEnabled() {
		return null != currentNavState && currentNavState.isPreviousEnabled();
	}

	public boolean isResumeEnabled() {
		return null != currentNavState && currentNavState.isResumeEnabled();
	}
	
	public boolean isStartEnabled() {
		return null != currentNavState && currentNavState.isStartEnabled();
	}
	
	public boolean isSuspendEnabled() {
		return null != currentNavState && currentNavState.isSuspendEnabled();
	}

	public IDataManager getDataManager() {
		return dataManager;
	}

	public void setDataManager(IDataManager dataManager) {
		this.dataManager = dataManager;
	}

	public List getCurrentObjStatusSet() {
		return currentObjStatusSet;
	}
}
