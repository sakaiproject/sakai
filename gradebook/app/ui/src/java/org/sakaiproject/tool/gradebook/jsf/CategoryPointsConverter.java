/**********************************************************************************
 *
 * $Id: AssignmentPointsConverter.java 20001 2007-04-18 19:41:33Z rjlowe@iupui.edu $
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2007, 2008 The Sakai Foundation, The MIT Corporation
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

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.Category;

/**
 * This formatting-only converver consolidates the rather complex formatting
 * logic for assignment and assignment grade points. If the points are null,
 * they should be displayed in a special way. If the points belong to an
 * assignment which doesn't count toward the final grade, they should be
 * displayed in a special way with a tooltip "title" attribute.
 */
@Slf4j
public class CategoryPointsConverter extends PointsConverter {
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (log.isDebugEnabled()) log.debug("getAsString(" + context + ", " + component + ", " + value + ")");

		String formattedScore;
		boolean notCounted = false;
		Double studentMean = 0.0;
		Double studentTotalPointsEarned = 0.0;
		Double studentTotalPointsPossible = 0.0;
		Category cat = null;
		
		if (value != null) {
			if (value instanceof Map) {
				studentMean = (Double) ((Map)value).get("studentMean");
				studentTotalPointsEarned = (Double) ((Map)value).get("studentTotalPointsEarned");
				studentTotalPointsPossible = (Double) ((Map)value).get("studentTotalPointsPossible");
				
				cat = (Category) ((Map)value).get("category");
			}
		}
		//if Category is null, then this is "Unassigned" therefore n/a
		if( cat == null || studentMean == null){
			formattedScore = FacesUtil.getLocalizedString("overview_unassigned_cat_avg");
		} else {
			//display percentage
			formattedScore = super.getAsString(context, component, studentMean) + "%";
			
			if(ServerConfigurationService.getBoolean("gradebook.roster.showCourseGradePoints", false)){
				Gradebook gradebook = cat.getGradebook();
				int gradeType = gradebook.getGrade_type();
				if(gradeType == GradebookService.GRADE_TYPE_POINTS ){
					formattedScore = super.getAsString(context, component, studentTotalPointsEarned)
					+ "/" + super.getAsString(context, component, studentTotalPointsPossible);
				}
			}
		}
		return formattedScore;
	}
}
