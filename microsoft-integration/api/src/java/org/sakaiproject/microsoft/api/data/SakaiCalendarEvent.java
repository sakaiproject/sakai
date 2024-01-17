/**
 * Copyright (c) 2024 The Apereo Foundation
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
package org.sakaiproject.microsoft.api.data;

import java.util.List;

import org.sakaiproject.site.api.Group;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SakaiCalendarEvent {

	private String eventId;
	private String calendarReference;
	private String title;
	private String description;
	private String type;
	private long init;
	private long duration;
	private List<Group> groups;
	
}
