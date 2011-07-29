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

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CalendarItem encapsulates all information about dashboard items to 
 * appear in the "Calendar" section of users' dashboards.
 *
 */
@Data 
@NoArgsConstructor
@AllArgsConstructor
public class CalendarItem {
	
	protected Long id;
	protected String title;
	protected Date calendarTime;
	protected String entityReference;
	protected String entityUrl;
	protected Context context;
	protected Realm realm;
	protected SourceType sourceType;

}
