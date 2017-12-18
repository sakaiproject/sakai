/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
package org.sakaiproject.component.app.scheduler;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;

@Slf4j
public class ScheduledInvocationTestCommand implements
		ScheduledInvocationCommand {
	public void execute(String opaqueContext) {
		log.info("Command executed!  Context: "+opaqueContext);
	}
}
