/**********************************************************************************
 *
 * $Id$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradebookAssignment;
import org.sakaiproject.tool.gradebook.LetterGradePercentMapping;
import org.sakaiproject.tool.gradebook.ui.AssignmentGradeRow;
import org.sakaiproject.tool.gradebook.ui.GradebookBean;

/**
 * This formatting-only converter consolidates the rather complex formatting
 * logic for the display of class avg. If the avg is null,
 * it should be displayed in a special way. If the avg belongs to an
 * assignment which doesn't count toward the final grade, it should be
 * displayed in a special way with a tooltip "title" attribute. The display
 * also changes based upon the grade entry method.
 */
@Slf4j
public class ClassAvgConverter extends PointsConverter {
    private int averageDecimalPlaces = 0;

    public ClassAvgConverter() {
        // AZ - allows configuration of the decimal points display for class average
        // http://jira.sakaiproject.org/browse/SAK-14520
        ServerConfigurationService configurationService = org.sakaiproject.component.cover.ServerConfigurationService.getInstance();
        if (configurationService == null) {
            log.warn("Unable to get configuration service, using default gradebook averageDecimalPlaces");
        } else {
            averageDecimalPlaces = configurationService.getInt("gradebook.class.average.decimal.places", 0);
        }
        if (averageDecimalPlaces < 0) {
            averageDecimalPlaces = 0;
        }
    }

	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (log.isDebugEnabled()) log.debug("getAsString(" + context + ", " + component + ", " + value + ")");

		String formattedAvg;
		String formattedPtsPossible;
		boolean notCounted = false;
		String entryMethod = null;
		
		final String POINTS = "points";
		final String PERCENT = "percent";
		final String LETTER = "letter";
		
		Object avg = null;
		Object pointsPossible = null;
		int numDecimalPlaces = averageDecimalPlaces;
		Gradebook gradebook;
		
		GradebookBean gbb = (GradebookBean)FacesUtil.resolveVariable("gradebookBean");

		if (value != null) {
			if (value instanceof GradebookAssignment) {
				GradebookAssignment assignment = (GradebookAssignment)value;
				gradebook = assignment.getGradebook();
				pointsPossible = assignment.getPointsPossible();

				if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_POINTS) {
					entryMethod = POINTS;
					avg = assignment.getAverageTotal();
				} else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE) {
					entryMethod = PERCENT;
					avg = assignment.getMean();
				} else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
					entryMethod = LETTER;
					Double mean = assignment.getMean();
					if (mean != null) {
						LetterGradePercentMapping mapping = gbb.getGradebookManager().getLetterGradePercentMapping(gradebook);
						avg = mapping.getGrade(mean);
					}
				}
				
				notCounted = assignment.isNotCounted();
				// if weighting enabled, item is not counted if not assigned
				// a category
				if (!notCounted && gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY) {
					notCounted = assignment.getCategory() == null;
				}
				
			} else if (value instanceof Category) {
				Category category = (Category) value;
				gradebook = category.getGradebook();
				pointsPossible = category.getAverageTotalPoints();
				
				// Unassigned category in weighted gb won't have avg
				if (category.getId() == null && gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY) {
					return FacesUtil.getLocalizedString("overview_unassigned_cat_avg");
				}
				/*else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_POINTS) {
					isPoints = true;
					avg = category.getAverageScore();
				} else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE) {
					isPercent = true;
					avg = category.getMean();
				}*/
				// always display category avgs as %
				entryMethod = PERCENT;
				avg = category.getMean();
				numDecimalPlaces = 2;
		
			} else if (value instanceof CourseGrade) {
				// course grade is always displayed as %
				entryMethod = PERCENT;
				CourseGrade courseGrade = (CourseGrade) value;
				avg = courseGrade.getMean();	
				
			} else if (value instanceof AssignmentGradeRow) {
				AssignmentGradeRow gradeRow = (AssignmentGradeRow) value;
				gradebook = gradeRow.getGradebook();
				avg = gradeRow.getScore();
				if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_POINTS) {
					entryMethod = POINTS;
					pointsPossible = gradeRow.getAssociatedAssignment().getPointsPossible();
				} else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE) {
					entryMethod = PERCENT;
				} else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
					entryMethod = LETTER;
					Double score = gradeRow.getScore();
					if (score != null) {
						LetterGradePercentMapping mapping = gbb.getGradebookManager().getLetterGradePercentMapping(gradebook);
						avg = mapping.getGrade(score);
					}
				}
			} else if (value instanceof CourseGradeRecord) {
				CourseGradeRecord gradeRecord = (CourseGradeRecord) value;
				if (numDecimalPlaces <= 0) {
				    // AZ - maintain default operation
	                numDecimalPlaces = 2;
				}
				entryMethod = PERCENT;
				avg = gradeRecord.getGradeAsPercentage();
			}
		}
		
		formattedAvg = getFormattedValue(context, component, avg, numDecimalPlaces);
		formattedPtsPossible = getFormattedValue(context, component, pointsPossible, 2);
		
		if (avg != null) {
			if (entryMethod.equals(POINTS)) {			
				formattedAvg = FacesUtil.getLocalizedString("overview_avg_display_points", new String[] {formattedAvg, formattedPtsPossible} );
			} else if (entryMethod.equals(PERCENT)) {
				formattedAvg = FacesUtil.getLocalizedString("overview_avg_display_percent", new String[] {formattedAvg} );
			} 

			if (notCounted) {
				formattedAvg = FacesUtil.getLocalizedString("score_not_counted",
						new String[] {formattedAvg, FacesUtil.getLocalizedString("score_not_counted_tooltip")});
			} else if (value instanceof CourseGrade || value instanceof CourseGradeRecord) {
				formattedAvg = FacesUtil.getLocalizedString("course_grade_percent_display", new String[] {formattedAvg});
			}
			
		}
		return formattedAvg;
	}
	
	private String getFormattedValue(FacesContext context, UIComponent component, Object value, int numDecimals) {
		String formattedValue;
		if (value == null || numDecimals < 0) {
			formattedValue = FacesUtil.getLocalizedString("score_null_placeholder");
		} else {
			if (value instanceof Number) {
				// Truncate to given # decimal places.
				value = new Double(FacesUtil.getRoundDown(((Number)value).doubleValue(), numDecimals));
				// AZ - note, this next line always truncs to 2 places by default
				formattedValue = super.getAsString(context, component, value);
			} else {
				formattedValue = value.toString();
			}

		}
		
		return formattedValue;
	}
}
