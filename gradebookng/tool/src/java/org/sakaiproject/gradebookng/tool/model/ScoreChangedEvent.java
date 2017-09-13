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
package org.sakaiproject.gradebookng.tool.model;

import org.apache.wicket.ajax.AjaxRequestTarget;

import lombok.Getter;

public class ScoreChangedEvent {

	@Getter
	private final AjaxRequestTarget target;

	@Getter
	private final Long categoryId;

	@Getter
	private final String studentUuid;

	public ScoreChangedEvent(final String studentUuid, final Long categoryId, final AjaxRequestTarget target) {
		this.studentUuid = studentUuid;
		this.categoryId = categoryId;
		this.target = target;
	}
}