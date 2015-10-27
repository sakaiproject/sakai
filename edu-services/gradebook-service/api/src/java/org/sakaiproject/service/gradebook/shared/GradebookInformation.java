/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2014 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.service.gradebook.shared;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Represents the settings for the gradebook
 *
 */
public class GradebookInformation implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String selectedGradingScaleUid;
	private Map<String, Double> selectedGradingScaleBottomPercents;
	private boolean displayReleasedGradeItemsToStudents;
	
	private int gradeType;
	private int categoryType;
	private List<CategoryDefinition> categories;
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
		return selectedGradingScaleUid;
	}
	public void setSelectedGradingScaleUid(String selectedGradingScaleUid) {
		this.selectedGradingScaleUid = selectedGradingScaleUid;
	}
	public Map<String, Double> getSelectedGradingScaleBottomPercents() {
		return selectedGradingScaleBottomPercents;
	}
	public void setSelectedGradingScaleBottomPercents(
			Map<String, Double> selectedGradingScaleBottomPercents) {
		this.selectedGradingScaleBottomPercents = selectedGradingScaleBottomPercents;
	}
	public boolean isDisplayReleasedGradeItemsToStudents() {
		return displayReleasedGradeItemsToStudents;
	}
	public void setDisplayReleasedGradeItemsToStudents(
			boolean displayReleasedGradeItemsToStudents) {
		this.displayReleasedGradeItemsToStudents = displayReleasedGradeItemsToStudents;
	}
	public int getGradeType() {
		return gradeType;
	}
	public void setGradeType(int gradeType) {
		this.gradeType = gradeType;
	}
	public int getCategoryType() {
		return categoryType;
	}
	public void setCategoryType(int categoryType) {
		this.categoryType = categoryType;
	}
	public String getGradeScale() {
		return gradeScale;
	}
	public void setGradeScale(String gradeScale) {
		this.gradeScale = gradeScale;
	}
	public boolean isCourseGradeDisplayed() {
		return courseGradeDisplayed;
	}
	public void setCourseGradeDisplayed(boolean courseGradeDisplayed) {
		this.courseGradeDisplayed = courseGradeDisplayed;
	}
	public List<CategoryDefinition> getCategories() {
		return categories;
	}
	public void setCategories(List<CategoryDefinition> categories) {
		this.categories = categories;
	}
	public boolean isCourseLetterGradeDisplayed() {
		return courseLetterGradeDisplayed;
	}
	public void setCourseLetterGradeDisplayed(boolean courseLetterGradeDisplayed) {
		this.courseLetterGradeDisplayed = courseLetterGradeDisplayed;
	}
	public boolean isCoursePointsDisplayed() {
		return coursePointsDisplayed;
	}
	public void setCoursePointsDisplayed(boolean coursePointsDisplayed) {
		this.coursePointsDisplayed = coursePointsDisplayed;
	}
	public boolean isCourseAverageDisplayed() {
		return courseAverageDisplayed;
	}
	public void setCourseAverageDisplayed(boolean courseAverageDisplayed) {
		this.courseAverageDisplayed = courseAverageDisplayed;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}
