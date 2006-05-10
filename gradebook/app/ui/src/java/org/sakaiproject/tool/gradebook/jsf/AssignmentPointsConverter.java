/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
*
* Licensed under the Educational Community License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.gradebook.AbstractGradeRecord;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.GradableObject;

/**
 * This formatting-only converver consolidates the rather complex formatting
 * logic for assignment and assignment grade points. If the points are null,
 * they should be displayed in a special way. If the points belong to an
 * assignment which doesn't count toward the final grade, they should be
 * displayed in a special way with a tooltip "title" attribute.
 */
public class AssignmentPointsConverter extends PointsConverter {
	private static final Log log = LogFactory.getLog(AssignmentPointsConverter.class);

	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (log.isDebugEnabled()) log.debug("getAsString(" + context + ", " + component + ", " + value + ")");

		String formattedScore;
		boolean notCounted = false;
		Object workingValue = value;

		if (value != null) {
			if (value instanceof GradableObject) {
				workingValue = ((GradableObject)value).getPointsForDisplay();
				if (value instanceof Assignment) {
					notCounted = ((Assignment)value).isNotCounted();
				}
			} else if (value instanceof AbstractGradeRecord) {
				workingValue = ((AbstractGradeRecord)value).getPointsEarned();
				if (value instanceof AssignmentGradeRecord) {
					notCounted = ((AssignmentGradeRecord)value).getAssignment().isNotCounted();
				}
			}
		}
		formattedScore = super.getAsString(context, component, workingValue);
		if (notCounted) {
			formattedScore = FacesUtil.getLocalizedString("score_not_counted",
				new String[] {formattedScore, FacesUtil.getLocalizedString("score_not_counted_tooltip")});
		}
		return formattedScore;
	}
}
