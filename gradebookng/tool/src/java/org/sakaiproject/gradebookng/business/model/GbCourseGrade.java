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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.sakaiproject.service.gradebook.shared.CourseGrade;

import lombok.Getter;
import lombok.Setter;

/**
 * Wraps a {@link CourseGrade} and provides a display string formatted according to the settings from various places in the UI
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GbCourseGrade implements Serializable {

	private static final long serialVersionUID = 1L;

	@Getter
	private final CourseGrade courseGrade;

	@Getter
	@Setter
	private String displayString;

	/**
	 * Constructor. Takes a {@link CourseGrade}. Display string is set afterwards.
	 *
	 * @param courseGrade CourseGrade object
	 */
	public GbCourseGrade(final CourseGrade courseGrade) {
		this.courseGrade = courseGrade;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
