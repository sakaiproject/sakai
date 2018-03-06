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
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradebookAssignment;
import org.sakaiproject.tool.gradebook.LetterGradePercentMapping;
import org.sakaiproject.tool.gradebook.ui.AssignmentGradeRow;
import org.sakaiproject.tool.gradebook.ui.GradebookBean;
import org.sakaiproject.service.gradebook.shared.GradebookService;

/**
 * This formatting-only converter displays appropriate class avg depending on
 * grade entry method for this gradebook. Truncates with 0 decimals or uses the config setting.
 */
@Slf4j
public class ClassAvgConverterBasicDisplay extends PointsConverter {
	private int averageDecimalPlaces = 0;

	public ClassAvgConverterBasicDisplay() {
        // AZ - allows configuration of the decimal points display for class average
        // http://jira.sakaiproject.org/browse/SAK-14520
	    ServerConfigurationService configurationService = org.sakaiproject.component.cover.ServerConfigurationService.getInstance();
	    if (configurationService == null) {
	        log.warn("Unable to get configuration service, using default gradebook averageDecimalPlaces");
	    } else {
	        averageDecimalPlaces = configurationService.getInt("gradebook.class.average.decimal.places", 0);
	        if (averageDecimalPlaces < 0) {
	            averageDecimalPlaces = 0;
	        }
	    }
	}

	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (log.isDebugEnabled()) log.debug("getAsString(" + context + ", " + component + ", " + value + ")");

		String formattedAvg;
		
		String entryMethod = null;
		final String POINTS = "points";
		final String PERCENT = "percent";
		final String LETTER = "letter";
		
		Object avg = null;
		Gradebook gradebook;

		if (value != null) {
			if (value instanceof GradebookAssignment) {
				GradebookAssignment assignment = (GradebookAssignment)value;
				gradebook = assignment.getGradebook();

				if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_POINTS) {
					entryMethod = POINTS;
					avg = assignment.getAverageTotal();
				} else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE) {
					entryMethod = PERCENT;
					avg = assignment.getMean();
				} else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
					entryMethod = LETTER;
					GradebookBean gbb = (GradebookBean)FacesUtil.resolveVariable("gradebookBean");
					Double mean = assignment.getMean();
					if (mean != null) {
						LetterGradePercentMapping mapping = gbb.getGradebookManager().getLetterGradePercentMapping(gradebook);
						avg = mapping.getGrade(mean);
					}
				}
				
			} else if (value instanceof Category) {
				Category category = (Category) value;
				gradebook = category.getGradebook();
				
				// Unassigned category in weighted gb won't have avg
				if (category.getId() == null && gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY) {
					return FacesUtil.getLocalizedString("overview_unassigned_cat_avg");
				}
				entryMethod = PERCENT;
				avg = category.getMean();
		
			} else if (value instanceof CourseGrade) {
				// course grade is always displayed as %
				entryMethod = PERCENT;
				CourseGrade courseGrade = (CourseGrade) value;
				avg = courseGrade.getMean();	
				
			} else if (value instanceof AssignmentGradeRow) {
				AssignmentGradeRow gradeRow = (AssignmentGradeRow) value;
				gradebook = gradeRow.getGradebook();
				avg = gradeRow.getScore();
				if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE) {
					entryMethod = PERCENT;
				}
			}
		}
		
		formattedAvg = getFormattedValue(context, component, avg);
		
		if (avg != null) {
			if (entryMethod.equals(PERCENT)) {
				formattedAvg = FacesUtil.getLocalizedString("overview_avg_display_percent", new String[] {formattedAvg} );
			}
		}
		return formattedAvg;
	}
	
	private String getFormattedValue(FacesContext context, UIComponent component, Object value) {
		String formattedValue;
		if (value == null) {
			formattedValue = FacesUtil.getLocalizedString("score_null_placeholder");
		} else {
			if (value instanceof Number) {
				// Truncate to averageDecimalPlaces decimal places.
				value = new Double(FacesUtil.getRoundDown(((Number)value).doubleValue(), averageDecimalPlaces));
                // AZ - note, this next line always truncs to 2 places by default
				formattedValue = super.getAsString(context, component, value);
			} else {
				formattedValue = value.toString();
			}
		}
		
		return formattedValue;
	}
}
