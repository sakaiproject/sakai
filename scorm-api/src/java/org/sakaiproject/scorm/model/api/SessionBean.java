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
package org.sakaiproject.scorm.model.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.adl.api.ecmascript.IErrorManager;
import org.adl.sequencer.IValidRequests;
import org.adl.validator.contentpackage.LaunchData;

public class SessionBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String learnerId;

	private long attemptNumber;
	
	private ContentPackage contentPackage;
	
	private ActivityTreeHolder treeHolder;
	
	private String activityTitle;
	
	private String activityId;
	private String scoId;
	private IValidRequests navigationState;
	private LaunchData launchData;
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
	
	//private IDataManager dataManager;
	private IErrorManager errorManager;
	
	private ScoBean displayingSco;
	
	private Attempt attempt;
	
	public SessionBean() {
		this.scoBeans = new ConcurrentHashMap<String, ScoBean>();
	}
	
	public SessionBean(String learnerId, ContentPackage contentPackage) {
		this();
		this.learnerId = learnerId;
		this.contentPackage = contentPackage;
	}

	public String getLearnerId() {
		return learnerId;
	}


	public void setLearnerId(String learnerId) {
		this.learnerId = learnerId;
	}


	public ActivityTreeHolder getTreeHolder() {
		return treeHolder;
	}


	public void setTreeHolder(ActivityTreeHolder treeHolder) {
		this.treeHolder = treeHolder;
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


	public LaunchData getLaunchData() {
		return launchData;
	}


	public void setLaunchData(LaunchData launchData) {
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

	public String getActivityTitle() {
		return activityTitle;
	}

	public void setActivityTitle(String activityTitle) {
		this.activityTitle = activityTitle;
	}

	public ContentPackage getContentPackage() {
		return contentPackage;
	}

	public void setContentPackage(ContentPackage contentPackage) {
		this.contentPackage = contentPackage;
	}

	public ScoBean getDisplayingSco() {
		return displayingSco;
	}

	public void setDisplayingSco(ScoBean displayingSco) {
		this.displayingSco = displayingSco;
	}

	
	
	
	
}
