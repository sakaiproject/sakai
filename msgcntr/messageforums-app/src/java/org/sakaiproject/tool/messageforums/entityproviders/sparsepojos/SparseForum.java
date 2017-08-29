/**
 * Copyright (c) 2003-2013 The Apereo Foundation
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
package org.sakaiproject.tool.messageforums.entityproviders.sparsepojos;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.entitybroker.DeveloperHelperService;

/**
 * A json friendly representation of a DiscussionForum. This one adds the topic list to the json.
 * 
 * @author Adrian Fish <adrian.r.fish@gmail.com>
 */
public class SparseForum extends SparsestForum {

	@Getter @Setter
	private List<SparsestTopic> topics;
	
	public SparseForum(DiscussionForum fatForum, DeveloperHelperService dhs) {
		
		super(fatForum,dhs);
	}
}
