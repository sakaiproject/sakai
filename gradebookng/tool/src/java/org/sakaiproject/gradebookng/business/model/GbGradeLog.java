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
package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.sakaiproject.tool.gradebook.GradingEvent;

import lombok.Getter;

/**
 * DTO for grade log events.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GbGradeLog implements Serializable {

	private static final long serialVersionUID = 1L;

	@Getter
	private final Date dateGraded;

	@Getter
	private final String graderUuid;

	@Getter
	private final String grade;

	public GbGradeLog(final GradingEvent ge) {
		this.dateGraded = ge.getDateGraded();
		this.graderUuid = ge.getGraderId();
		this.grade = ge.getGrade();
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

}
