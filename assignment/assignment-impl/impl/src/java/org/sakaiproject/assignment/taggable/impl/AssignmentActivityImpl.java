/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.assignment.taggable.impl;

import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.taggable.api.TaggableActivity;
import org.sakaiproject.taggable.api.TaggableActivityProducer;

public class AssignmentActivityImpl implements TaggableActivity {

	protected Assignment assignment;

	protected TaggableActivityProducer producer;

	public AssignmentActivityImpl(Assignment assignment,
			TaggableActivityProducer producer) {
		this.assignment = assignment;
		this.producer = producer;
	}

	public boolean equals(Object object) {
		if (object instanceof TaggableActivity) {
			TaggableActivity activity = (TaggableActivity) object;
			return activity.getReference().equals(this.getReference());
		}
		return false;
	}

	public String getContext() {
		return assignment.getContext();
	}

	public String getDescription() {
		return assignment.getContent().getInstructions();
	}

	public Object getObject() {
		return assignment;
	}

	public TaggableActivityProducer getProducer() {
		return producer;
	}

	public String getReference() {
		return assignment.getReference();
	}

	public String getTitle() {
		return assignment.getTitle();
	}

	public String getActivityDetailUrl()
	{
		//String url = assignment.getUrl();
		String url = ServerConfigurationService.getServerUrl() + 
			"/direct/assignment/" + assignment.getId() + "/doView_assignment?TB_iframe=true";
		return url;
	}

	public String getTypeName()
	{
		return producer.getName();
	}
}
