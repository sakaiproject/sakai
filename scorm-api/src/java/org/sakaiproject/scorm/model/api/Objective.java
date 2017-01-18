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

public class Objective implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;

	private Score score;

	private String successStatus;

	private String completionStatus;

	private String description;

	public String getCompletionStatus() {
		return completionStatus;
	}

	public String getDescription() {
		return description;
	}

	public String getId() {
		return id;
	}

	public Score getScore() {
		return score;
	}

	public String getSuccessStatus() {
		return successStatus;
	}

	public void setCompletionStatus(String completionStatus) {
		this.completionStatus = completionStatus;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setScore(Score score) {
		this.score = score;
	}

	public void setSuccessStatus(String successStatus) {
		this.successStatus = successStatus;
	}

}
