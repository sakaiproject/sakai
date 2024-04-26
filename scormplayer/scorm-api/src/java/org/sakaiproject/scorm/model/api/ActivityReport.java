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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

public class ActivityReport implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Getter @Setter private String activityId;
	@Getter @Setter private String scoId;
	@Getter @Setter private String title;
	@Getter @Setter private List<Interaction> interactions;
	@Getter @Setter private Map<String, Objective> objectives;
	@Getter @Setter private Progress progress;
	@Getter @Setter private Score score;
	@Getter @Setter private List<CMIData> cmiData;

	public ActivityReport()
	{
		interactions = new LinkedList<>();
		objectives = new HashMap<>();
	}

	public List<Objective> getInteractionObjectives(Interaction interaction)
	{
		List<Objective> list = new LinkedList<>();
		for (String objectiveId : interaction.getObjectiveIds())
		{
			Objective objective = objectives.get(objectiveId);
			if (objective != null)
			{
				list.add(objective);
			}
		}

		return list;
	}
}
