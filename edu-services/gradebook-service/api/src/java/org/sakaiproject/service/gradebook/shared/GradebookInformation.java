/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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

package org.sakaiproject.service.gradebook.shared;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents the settings for the gradebook
 *
 */
public class GradebookInformation implements Serializable {
	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private String selectedGradingScaleUid;

	/**
	 * The ID of the GradeMapping that should be used for this gradebook
	 */
	@Getter
	@Setter
	private String selectedGradeMappingId;

	/**
	 * The list of GradeMappings defined for this gradebook but as a DTO representation
	 */
	@Getter
	@Setter
	private List<GradeMappingDefinition> gradeMappings;

	/**
	 * The grading schema map currently in use for the this gradebook. For example A+ = 100 etc.
	 */
	@Getter
	@Setter
	private Map<String, Double> selectedGradingScaleBottomPercents;

	@Getter
	@Setter
	private boolean displayReleasedGradeItemsToStudents;

	@Getter
	@Setter
	private int gradeType;

	@Getter
	@Setter
	private int categoryType;

	@Getter
	@Setter
	private List<CategoryDefinition> categories;

	/**
	 * The name of the grading scale, e.g. Pass / Not Pass
	 */
	@Getter
	@Setter
	private String gradeScale;

	/**
	 * Is the course grade to be shown at all?
	 */
	@Getter
	@Setter
	private boolean courseGradeDisplayed;

	/**
	 * If the course grade is displayed, should the letter grade be displayed?
	 */
	@Getter
	@Setter
	private boolean courseLetterGradeDisplayed;

	/**
	 * If the course grade is displayed, should the total points be displayed?
	 */
	@Getter
	@Setter
	private boolean coursePointsDisplayed;

	/**
	 * If the course grade is displayed, should the percentage be displayed?
	 */
	@Getter
	@Setter
	private boolean courseAverageDisplayed;

	/**
	 * Are assignment stats to be shown to students?
	 */
	@Getter
	@Setter
	private boolean assignmentStatsDisplayed;

	/**
	 * Are course grade stats to be shown to students?
	 */
	@Getter
	@Setter
	private boolean courseGradeStatsDisplayed;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}
