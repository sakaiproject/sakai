/**
 * Copyright (c) 2005-2016 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.facade;

import org.sakaiproject.tool.assessment.data.dao.assessment.EventLogData;

public class EventLogFacade
implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	private EventLogData data;
	
	public EventLogFacade() {
	}
	
	public EventLogFacade(EventLogData data) {
		this.data = data;
	}
	
	public EventLogData getData() {
		return data;
	}
	
	public void setData(EventLogData data) {
		this.data = data;
	}
}
