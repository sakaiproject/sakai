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

import lombok.Getter;
import lombok.Setter;

import org.adl.api.ecmascript.IErrorManager;
import org.adl.sequencer.IValidRequests;
import org.adl.validator.contentpackage.LaunchData;

public class SessionBean implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Setter @Getter private String learnerId;
	@Setter @Getter private String activityTitle;
	@Setter @Getter private String activityId;
	@Setter @Getter private String scoId;
	@Setter @Getter private String baseUrl;
	@Setter @Getter private String completionUrl;
	@Setter @Getter private long attemptNumber;
	@Setter @Getter private ContentPackage contentPackage;
	@Setter @Getter private ActivityTreeHolder treeHolder;
	@Setter @Getter private IValidRequests navigationState;
	@Setter @Getter private LaunchData launchData;
	@Setter @Getter private IErrorManager errorManager;
	@Setter @Getter private ScoBean displayingSco;
	@Setter @Getter private Attempt attempt;
	@Setter @Getter private ContentPackageManifest manifest;
	@Setter @Getter private List<?> objectiveStatusSet;
	@Setter @Getter private Map<String, ScoBean> scoBeans;
	@Setter @Getter private boolean isStarted = false;
	@Setter @Getter private boolean isEnded = false;
	@Setter @Getter private boolean isSuspended = false;
	@Setter @Getter private boolean closeOnNextTerminate = false;
	@Setter @Getter private boolean isRestart = false;

	public SessionBean()
	{
		this.scoBeans = new ConcurrentHashMap<>();
	}

	public SessionBean(String learnerId, ContentPackage contentPackage)
	{
		this();
		this.learnerId = learnerId;
		this.contentPackage = contentPackage;
	}
}
