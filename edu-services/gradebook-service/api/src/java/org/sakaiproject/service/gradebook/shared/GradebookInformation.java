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

/**
 * Represents the settings for the gradebook
 *
 */
public class GradebookInformation implements Serializable {
	private static final long serialVersionUID = 1L;

	private String selectedGradingScaleUid;

	/**
	 * The ID of the GradeMapping that should be used for this gradebook
	 */
	private String selectedGradeMappingId;

	/**
	 * The list of GradeMappings defined for this gradebook but as a DTO representation
	 */
	private List<GradeMappingDefinition> gradeMappings;

	/**
	 * The grading schema map currently in use for the this gradebook. For example A+ = 100 etc.
	 */
	private Map<String, Double> selectedGradingScaleBottomPercents;

	private boolean displayReleasedGradeItemsToStudents;
	private int gradeType;
	private int categoryType;
	private List<CategoryDefinition> categories;

	/**
	 * The name of the grading scale, e.g. Pass / Not Pass
	 */
	private String gradeScale;

	/**
	 * Is the course grade to be shown at all?
	 */
	private boolean courseGradeDisplayed;

	/**
	 * If the course grade is displayed, should the letter grade be displayed?
	 */
	private boolean courseLetterGradeDisplayed;

	/**
	 * If the course grade is displayed, should the total points be displayed?
	 */
	private boolean coursePointsDisplayed;

	/**
	 * If the course grade is displayed, should the percentage be displayed?
	 */
	private boolean courseAverageDisplayed;

	public String getSelectedGradingScaleUid() {
		return this.selectedGradingScaleUid;
	}

	public void setSelectedGradingScaleUid(final String selectedGradingScaleUid) {
		this.selectedGradingScaleUid = selectedGradingScaleUid;
	}

	public String getSelectedGradeMappingId() {
		return this.selectedGradeMappingId;
	}

	public void setSelectedGradeMappingId(final String selectedGradeMappingId) {
		this.selectedGradeMappingId = selectedGradeMappingId;
	}

	public List<GradeMappingDefinition> getGradeMappings() {
		return this.gradeMappings;
	}

	public void setGradeMappings(final List<GradeMappingDefinition> gradeMappings) {
		this.gradeMappings = gradeMappings;
	}

	public Map<String, Double> getSelectedGradingScaleBottomPercents() {
		return this.selectedGradingScaleBottomPercents;
	}

	public void setSelectedGradingScaleBottomPercents(
			final Map<String, Double> selectedGradingScaleBottomPercents) {
		this.selectedGradingScaleBottomPercents = selectedGradingScaleBottomPercents;
	}

	public boolean isDisplayReleasedGradeItemsToStudents() {
		return this.displayReleasedGradeItemsToStudents;
	}

	public void setDisplayReleasedGradeItemsToStudents(
			final boolean displayReleasedGradeItemsToStudents) {
		this.displayReleasedGradeItemsToStudents = displayReleasedGradeItemsToStudents;
	}

	public int getGradeType() {
		return this.gradeType;
	}

	public void setGradeType(final int gradeType) {
		this.gradeType = gradeType;
	}

	public int getCategoryType() {
		return this.categoryType;
	}

	public void setCategoryType(final int categoryType) {
		this.categoryType = categoryType;
	}

	public String getGradeScale() {
		return this.gradeScale;
	}

	public void setGradeScale(final String gradeScale) {
		this.gradeScale = gradeScale;
	}

	public boolean isCourseGradeDisplayed() {
		return this.courseGradeDisplayed;
	}

	public void setCourseGradeDisplayed(final boolean courseGradeDisplayed) {
		this.courseGradeDisplayed = courseGradeDisplayed;
	}

	public List<CategoryDefinition> getCategories() {
		return this.categories;
	}

	public void setCategories(final List<CategoryDefinition> categories) {
		this.categories = categories;
	}

	public boolean isCourseLetterGradeDisplayed() {
		return this.courseLetterGradeDisplayed;
	}

	public void setCourseLetterGradeDisplayed(final boolean courseLetterGradeDisplayed) {
		this.courseLetterGradeDisplayed = courseLetterGradeDisplayed;
	}

	public boolean isCoursePointsDisplayed() {
		return this.coursePointsDisplayed;
	}

	public void setCoursePointsDisplayed(final boolean coursePointsDisplayed) {
		this.coursePointsDisplayed = coursePointsDisplayed;
	}

	public boolean isCourseAverageDisplayed() {
		return this.courseAverageDisplayed;
	}

	public void setCourseAverageDisplayed(final boolean courseAverageDisplayed) {
		this.courseAverageDisplayed = courseAverageDisplayed;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}
