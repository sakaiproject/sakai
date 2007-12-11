package org.sakaiproject.scorm.model.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.adl.api.ecmascript.IErrorManager;
import org.adl.datamodels.IDataManager;
import org.adl.sequencer.ISeqActivityTree;
import org.adl.sequencer.IValidRequests;
import org.adl.validator.contentpackage.ILaunchData;
import org.sakaiproject.scorm.service.api.ScoBean;

public class SessionBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String courseId;
	private String learnerId;
	private String title;
	private long attemptNumber;
	
	private ISeqActivityTree tree;
	
	private String activityId;
	private String scoId;
	private IValidRequests navigationState;
	private ILaunchData launchData;
	private List<?> objectiveStatusSet;
	private ContentPackageManifest manifest;
	
	private Map<String, ScoBean> scoBeans;
	
	private String baseUrl;
	private String completionUrl;
	
	private boolean isStarted = false;
	private boolean isEnded = false;
	private boolean isSuspended = false;
	
	private boolean closeOnNextTerminate = false;
	private boolean isRestart = false;
	
	private IDataManager dataManager;
	private IErrorManager errorManager;
	
	private Attempt attempt;
	
	public SessionBean() {
		this.scoBeans = new ConcurrentHashMap<String, ScoBean>();
	}
	
	public SessionBean(String courseId, String learnerId) {
		this();
		this.courseId = courseId;
		this.learnerId = learnerId;
	}


	public String getCourseId() {
		return courseId;
	}


	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}


	public String getLearnerId() {
		return learnerId;
	}


	public void setLearnerId(String learnerId) {
		this.learnerId = learnerId;
	}


	public ISeqActivityTree getTree() {
		return tree;
	}


	public void setTree(ISeqActivityTree tree) {
		this.tree = tree;
	}


	public String getActivityId() {
		return activityId;
	}


	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}


	public String getScoId() {
		return scoId;
	}


	public void setScoId(String scoId) {
		this.scoId = scoId;
	}


	public IValidRequests getNavigationState() {
		return navigationState;
	}


	public void setNavigationState(IValidRequests navigationState) {
		this.navigationState = navigationState;
	}


	public ILaunchData getLaunchData() {
		return launchData;
	}


	public void setLaunchData(ILaunchData launchData) {
		this.launchData = launchData;
	}


	public List getObjectiveStatusSet() {
		return objectiveStatusSet;
	}


	public void setObjectiveStatusSet(List objectiveStatusSet) {
		this.objectiveStatusSet = objectiveStatusSet;
	}


	public ContentPackageManifest getManifest() {
		return manifest;
	}


	public void setManifest(ContentPackageManifest manifest) {
		this.manifest = manifest;
	}


	public boolean isSuspended() {
		return isSuspended;
	}


	public void setSuspended(boolean isSuspended) {
		this.isSuspended = isSuspended;
	}


	public boolean isEnded() {
		return isEnded;
	}


	public void setEnded(boolean isEnded) {
		this.isEnded = isEnded;
	}


	public String getBaseUrl() {
		return baseUrl;
	}


	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}


	public String getCompletionUrl() {
		return completionUrl;
	}


	public void setCompletionUrl(String completionUrl) {
		this.completionUrl = completionUrl;
	}


	public boolean isStarted() {
		return isStarted;
	}


	public void setStarted(boolean isStarted) {
		this.isStarted = isStarted;
	}

	public Map<String, ScoBean> getScoBeans() {
		return scoBeans;
	}

	public void setScoBeans(Map<String, ScoBean> scoBeans) {
		this.scoBeans = scoBeans;
	}

	public boolean isCloseOnNextTerminate() {
		return closeOnNextTerminate;
	}

	public void setCloseOnNextTerminate(boolean closeOnNextTerminate) {
		this.closeOnNextTerminate = closeOnNextTerminate;
	}

	public IErrorManager getErrorManager() {
		return errorManager;
	}

	public void setErrorManager(IErrorManager errorManager) {
		this.errorManager = errorManager;
	}

	public IDataManager getDataManager() {
		return dataManager;
	}

	public void setDataManager(IDataManager dataManager) {
		this.dataManager = dataManager;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Attempt getAttempt() {
		return attempt;
	}

	public void setAttempt(Attempt attempt) {
		this.attempt = attempt;
	}

	public long getAttemptNumber() {
		return attemptNumber;
	}

	public void setAttemptNumber(long attemptNumber) {
		this.attemptNumber = attemptNumber;
	}

	public boolean isRestart() {
		return isRestart;
	}

	public void setRestart(boolean isRestart) {
		this.isRestart = isRestart;
	}

	
	
	
	
}
