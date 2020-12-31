/**********************************************************************************
 Copyright (c) 2019 Apereo Foundation
 Licensed under the Educational Community License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
           http://opensource.org/licenses/ecl2
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 **********************************************************************************/

package org.sakaiproject.datemanager.api.model;

import java.time.Instant;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DateManagerUpdate {
	public Object object;
	public Instant openDate;
	public Instant dueDate;
	public Instant acceptUntilDate;
	public Instant feedbackStartDate;
	public Instant feedbackEndDate;

	public DateManagerUpdate(Object object, Instant openDate, Instant dueDate, Instant acceptUntilDate) {
		this.object = object;
		this.openDate = openDate;
		this.dueDate = dueDate;
		this.acceptUntilDate = acceptUntilDate;
	}
}
