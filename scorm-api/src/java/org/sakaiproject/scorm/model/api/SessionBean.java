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

	public String getActivityId() {
		return activityId;
	}

	public String getActivityTitle() {
		return activityTitle;
	}

	public Attempt getAttempt() {
		return attempt;
	}

	public long getAttemptNumber() {
		return attemptNumber;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public String getCompletionUrl() {
		return completionUrl;
	}

	public ContentPackage getContentPackage() {
		return contentPackage;
	}

	public ScoBean getDisplayingSco() {
		return displayingSco;
	}

	public IErrorManager getErrorManager() {
		return errorManager;
	}

	public LaunchData getLaunchData() {
		return launchData;
	}

	public String getLearnerId() {
		return learnerId;
	}

	public ContentPackageManifest getManifest() {
		return manifest;
	}

	public IValidRequests getNavigationState() {
		return navigationState;
	}

	public List getObjectiveStatusSet() {
		return objectiveStatusSet;
	}

	public Map<String, ScoBean> getScoBeans() {
		return scoBeans;
	}

	public String getScoId() {
		return scoId;
	}

	public ActivityTreeHolder getTreeHolder() {
		return treeHolder;
	}

	public boolean isCloseOnNextTerminate() {
		return closeOnNextTerminate;
	}

	public boolean isEnded() {
		return isEnded;
	}

	public boolean isRestart() {
		return isRestart;
	}

	public boolean isStarted() {
		return isStarted;
	}

	public boolean isSuspended() {
		return isSuspended;
	}

	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	public void setActivityTitle(String activityTitle) {
		this.activityTitle = activityTitle;
	}

	public void setAttempt(Attempt attempt) {
		this.attempt = attempt;
	}

	public void setAttemptNumber(long attemptNumber) {
		this.attemptNumber = attemptNumber;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void setCloseOnNextTerminate(boolean closeOnNextTerminate) {
		this.closeOnNextTerminate = closeOnNextTerminate;
	}

	public void setCompletionUrl(String completionUrl) {
		this.completionUrl = completionUrl;
	}

	public void setContentPackage(ContentPackage contentPackage) {
		this.contentPackage = contentPackage;
	}

	public void setDisplayingSco(ScoBean displayingSco) {
		this.displayingSco = displayingSco;
	}

	public void setEnded(boolean isEnded) {
		this.isEnded = isEnded;
	}

	public void setErrorManager(IErrorManager errorManager) {
		this.errorManager = errorManager;
	}

	public void setLaunchData(LaunchData launchData) {
		this.launchData = launchData;
	}

	public void setLearnerId(String learnerId) {
		this.learnerId = learnerId;
	}

	public void setManifest(ContentPackageManifest manifest) {
		this.manifest = manifest;
	}

	public void setNavigationState(IValidRequests navigationState) {
		this.navigationState = navigationState;
	}

	public void setObjectiveStatusSet(List objectiveStatusSet) {
		this.objectiveStatusSet = objectiveStatusSet;
	}

	public void setRestart(boolean isRestart) {
		this.isRestart = isRestart;
	}

	public void setScoBeans(Map<String, ScoBean> scoBeans) {
		this.scoBeans = scoBeans;
	}

	public void setScoId(String scoId) {
		this.scoId = scoId;
	}

	public void setStarted(boolean isStarted) {
		this.isStarted = isStarted;
	}

	public void setSuspended(boolean isSuspended) {
		this.isSuspended = isSuspended;
	}

	public void setTreeHolder(ActivityTreeHolder treeHolder) {
		this.treeHolder = treeHolder;
	}

}
