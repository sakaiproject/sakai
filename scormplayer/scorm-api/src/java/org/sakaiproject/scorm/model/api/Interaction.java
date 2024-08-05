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
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class Interaction implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Setter @Getter private String interactionId;
	@Setter @Getter private String type;
	@Setter @Getter private String learnerId;
	@Setter @Getter private String scoId;
	@Setter @Getter private String timestamp;
	@Setter @Getter private String learnerResponse;
	@Setter @Getter private String result;
	@Setter @Getter private String latency;
	@Setter @Getter private String description;
	@Setter @Getter private String activityTitle;
	@Setter @Getter private long contentPackageId;
	@Setter @Getter private long attemptNumber;
	@Setter @Getter private List<Objective> objectives;
	@Setter @Getter private List<String> objectiveIds;
	@Setter @Getter private List<String> correctResponses;
	@Setter @Getter private double weighting;

	public Interaction()
	{
		objectiveIds = new LinkedList<>();
		correctResponses = new LinkedList<>();
		objectives = new LinkedList<>();
	}
}
