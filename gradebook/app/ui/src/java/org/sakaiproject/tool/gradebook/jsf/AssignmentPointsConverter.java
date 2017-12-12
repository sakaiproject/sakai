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

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.AbstractGradeRecord;
import org.sakaiproject.tool.gradebook.GradebookAssignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 * This formatting-only converver consolidates the rather complex formatting
 * logic for assignment and assignment grade points. If the points are null,
 * they should be displayed in a special way. If the points belong to an
 * assignment which doesn't count toward the final grade, they should be
 * displayed in a special way with a tooltip "title" attribute.
 */
@Slf4j
public class AssignmentPointsConverter extends PointsConverter {
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (log.isDebugEnabled()) log.debug("getAsString(" + context + ", " + component + ", " + value + ")");

		String formattedScore;
		boolean notCounted = false;
		Object workingValue = value;
		boolean percentage = false;
		
		if (value != null) {
			if (value instanceof GradebookAssignment) {
				GradebookAssignment assignment = (GradebookAssignment)value;
				workingValue = assignment.getPointsPossible();
				notCounted = assignment.isNotCounted();
				// if weighting enabled, item is not counted if not assigned
				// a category
				if (!notCounted && assignment.getGradebook().getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY) {
					notCounted = assignment.getCategory() == null;
				}
			} else if (value instanceof AssignmentGradeRecord) {
				Gradebook gradebook = ((GradableObject)((AbstractGradeRecord)value).getGradableObject()).getGradebook();
				int gradeType = gradebook.getGrade_type();
				AssignmentGradeRecord agr = (AssignmentGradeRecord)value;
				if (agr.isUserAbleToView()) {
					if(gradeType == GradebookService.GRADE_TYPE_POINTS ){
						//if grade by points and no category weighting
						workingValue = ((AbstractGradeRecord)value).getPointsEarned();	
					} else if (gradeType == GradebookService.GRADE_TYPE_LETTER) {
						workingValue = agr.getLetterEarned();
					} else {
						//display percentage
						percentage = true;
						workingValue = agr.getPercentEarned();
					}
				} else {
					workingValue = " ";
				}

				GradebookAssignment assignment = agr.getAssignment();
				notCounted = assignment.isNotCounted();
				// if weighting enabled, item is only counted if assigned
				// a category
				if (!notCounted && assignment.getGradebook().getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY) {
					notCounted = assignment.getCategory() == null;
				}

			} else if (value instanceof CourseGradeRecord) {
				// display percentage
				percentage = true;
				workingValue = ((AbstractGradeRecord)value).getGradeAsPercentage();
				if(ServerConfigurationService.getBoolean("gradebook.roster.showCourseGradePoints", false)){
					Gradebook gradebook = ((GradableObject)((AbstractGradeRecord)value).getGradableObject()).getGradebook();
					int gradeType = gradebook.getGrade_type();
					if(gradeType == GradebookService.GRADE_TYPE_POINTS && gradebook.getCategory_type() != GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY){
						percentage = false;
						workingValue = super.getAsString(context, component, ((CourseGradeRecord)value).getPointsEarned())
						+ "/" + super.getAsString(context, component, ((CourseGradeRecord)value).getTotalPointsPossible());
					}
				}
			}
		}
		formattedScore = super.getAsString(context, component, workingValue);
		if (notCounted) {
			formattedScore = FacesUtil.getLocalizedString("score_not_counted",
					new String[] {formattedScore, FacesUtil.getLocalizedString("score_not_counted_tooltip")});
		}
		if(percentage && workingValue != null){
			formattedScore += "%";
		}
        if(value != null && value instanceof AssignmentGradeRecord){
            AssignmentGradeRecord agr = (AssignmentGradeRecord)value;
            if(agr.getDroppedFromGrade()) {
                formattedScore = "<strike>" + formattedScore + "</strike>";
            }
        }
        if(value != null && value instanceof CourseGradeRecord && !ServerConfigurationService.getBoolean("gradebook.roster.showCourseGradePoints", false)){
        	if(((CourseGradeRecord) value).getEnteredGrade() != null){
        		formattedScore = "<span style='color: red'>" + formattedScore + "</span>";
			}
        }
		return formattedScore;
	}
}
