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
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

public class LearnerExperience implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Setter @Getter private String learnerName;
	@Setter @Getter private String learnerId;
	@Setter @Getter private String progress;
	@Setter @Getter private String score;
	@Setter @Getter private String previousLearnerIds;
	@Setter @Getter private String nextLearnerIds;
	@Setter @Getter private long contentPackageId;
	@Setter @Getter private Date lastAttemptDate;
	@Setter @Getter private int status;
	@Setter @Getter private int numberOfAttempts;

	public LearnerExperience(Learner learner, long contentPackageId)
	{
		this.learnerName = new StringBuilder(learner.getDisplayName()).append(" (").append(learner.getDisplayId()).append(")").toString();
		this.learnerId = learner.getId();
		this.contentPackageId = contentPackageId;
		this.numberOfAttempts = 0;
	}
}
