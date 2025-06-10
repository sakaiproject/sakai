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

package org.sakaiproject.gradebookng.business.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.grading.api.CourseGradeTransferBean;
import org.sakaiproject.grading.api.GradeType;
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.grading.api.MessageHelper;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.util.ResourceLoader;

/**
 * Helper class to handle the formatting of the course grade display string
 *
 * @author Steve Seinsburg (steve.swinsburg@gmail.com)
 */
public class CourseGradeFormatter {

	private final Gradebook gradebook;
	private final GbRole currentUserRole;
	private final boolean isCourseGradeVisible;
	private final boolean showPoints;
	private final boolean showOverride;
	private final boolean showCalculatedGrade;

	@SuppressWarnings("unchecked")
	private static ResourceLoader RL = new ResourceLoader();

	/**
	 * Constructor to initialise the data
	 *
	 * All of this gets passed in ONCE, then reused for every format call
	 *
	 * @param gradebook the gradebook settings
	 * @param currentUserRole role of the current user
	 * @param isCourseGradeVisible if the course grade is visible to the user
	 * @param showPoints if we are to show points
	 * @param showOverride if we are to show the override
	 * @return
	 */
	public CourseGradeFormatter(final Gradebook gradebook, final GbRole currentUserRole,
			final boolean isCourseGradeVisible,
			final boolean showPoints,
			final boolean showOverride,
			final boolean showCalculatedGrade) {

		this.gradebook = gradebook;
		this.currentUserRole = currentUserRole;
		this.isCourseGradeVisible = isCourseGradeVisible;
		this.showPoints = showPoints;
		this.showOverride = showOverride;
		this.showCalculatedGrade = showCalculatedGrade;
	}

	/**
	 * Format the passed in course grade
	 *
	 * @param courseGrade the raw course grade for the student
	 *
	 * @return the formatted display string
	 */
	public String format(final CourseGradeTransferBean courseGrade) {

		String rval = null;

		// something has gone wrong and there's no course grade!
		if (courseGrade == null) {
			rval = MessageHelper.getString("coursegrade.display.none", RL.getLocale());
			// instructor, can view
		} else if (this.currentUserRole == GbRole.INSTRUCTOR) {
			rval = build(courseGrade);
			// TA, permission check
		} else if (this.currentUserRole == GbRole.TA) {
			if (!this.isCourseGradeVisible) {
				rval = MessageHelper.getString("label.coursegrade.nopermission", RL.getLocale());
			} else {
				rval = build(courseGrade);
			}
			// student, check if course grade released, and permission check
		} else {
			if (this.gradebook.getCourseGradeDisplayed()) {
				if (!this.isCourseGradeVisible) {
					rval = MessageHelper.getString("label.coursegrade.nopermission", RL.getLocale());
				} else {
					rval = build(courseGrade);
				}
			} else {
				rval = MessageHelper.getString("label.coursegrade.studentnotreleased", RL.getLocale());
			}
		}

		return rval;

	}

	/**
	 * Takes care of checking the values and configured settings to format the course grade into an applicable display format
	 *
	 * Format:
	 *
	 * Instructor always gets lettergrade + percentage but may also get points depending on setting. TA, same as instructor unless disabled
	 * Student gets whatever is configured
	 *
	 * @return formatted string ready for display
	 */
	private String build(final CourseGradeTransferBean courseGrade) {
		final List<String> parts = new ArrayList<>();

		// letter grade
		String letterGrade = null;
		if (this.showOverride && StringUtils.isNotBlank(courseGrade.getEnteredGrade())) {
			letterGrade = courseGrade.getEnteredGrade();
		} else {
			letterGrade = courseGrade.getMappedGrade();
		}

		if (StringUtils.isNotBlank(letterGrade)
				&& (this.gradebook.getCourseLetterGradeDisplayed() || shouldDisplayFullCourseGrade())) {
			parts.add(letterGrade);
		}

		// percentage
		// not shown in final grade mode
		final String calculatedGrade = FormatHelper.formatStringAsPercentage(courseGrade.getCalculatedGrade());

		if (StringUtils.isNotBlank(calculatedGrade)
				&& (this.gradebook.getCourseAverageDisplayed() || shouldDisplayFullCourseGrade())) {
			if (parts.isEmpty()) {
				parts.add(new StringResourceModel("coursegrade.display.percentage-first").setParameters(calculatedGrade).getString());
			} else {
				parts.add(new StringResourceModel("coursegrade.display.percentage-second").setParameters(calculatedGrade).getString());
			}
		}

		// requested points
		if (this.showPoints) {

			// don't display points for weighted category type
			final Integer categoryType = this.gradebook.getCategoryType();
			if (!Objects.equals(categoryType, GradingConstants.CATEGORY_TYPE_WEIGHTED_CATEGORY)) {

				Double pointsEarned = courseGrade.getPointsEarned();
				Double totalPointsPossible = courseGrade.getTotalPointsPossible();

				// handle the special case in the gradebook service where totalPointsPossible = -1
				if (totalPointsPossible != null && totalPointsPossible == -1) {
					pointsEarned = null;
					totalPointsPossible = null;
				}

				// if instructor, show the points if requested
				// otherwise check the settings
				if (shouldDisplayFullCourseGrade() || this.gradebook.getCoursePointsDisplayed()) {
					if (pointsEarned != null && totalPointsPossible != null) {
						final String pointsEarnedDisplayString = FormatHelper.formatGradeForDisplay(pointsEarned, gradebook.getGradeType());
						final String totalPointsPossibleDisplayString = FormatHelper.formatGradeForDisplay(totalPointsPossible, gradebook.getGradeType());
						if (parts.isEmpty()) {
							parts.add(MessageHelper.getString("coursegrade.display.points-first", RL.getLocale(), pointsEarnedDisplayString,
									totalPointsPossibleDisplayString));
						} else {
							parts.add(MessageHelper.getString("coursegrade.display.points-second", RL.getLocale(), pointsEarnedDisplayString,
									totalPointsPossibleDisplayString));
						}
					}
				}
			}
		}

		// if parts is empty, there are no grades, display a -
		if (parts.isEmpty()) {
			parts.add(MessageHelper.getString("coursegrade.display.none", RL.getLocale()));
		}

		return String.join(" ", parts);
	}

	private boolean shouldDisplayFullCourseGrade() {
		return GbRole.INSTRUCTOR.equals(this.currentUserRole) || GbRole.TA.equals(this.currentUserRole);
	}
}
