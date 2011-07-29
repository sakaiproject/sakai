/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2011 The Sakai Foundation
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

package org.sakaiproject.dash.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SourceType encapsulates information about the types of entities to be
 * represented as dashboard items, along with information about how and where 
 * notifications about them will be rendered .
 *
 */
@Data 
@NoArgsConstructor
@AllArgsConstructor
public class SourceType {

	protected Long id;
	protected String name;
	protected boolean saveCalendarItem = false;
	protected boolean saveNewsItem = false;
	protected String calendarFormat;
	protected String calendarSummaryFormat;
	protected String newsFormat;
	protected String newsSummaryFormat;

}
