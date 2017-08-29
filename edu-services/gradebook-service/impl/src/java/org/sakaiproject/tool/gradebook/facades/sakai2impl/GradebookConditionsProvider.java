/**
 * Copyright (c) 2003-2015 The Apereo Foundation
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
package org.sakaiproject.tool.gradebook.facades.sakai2impl;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.conditions.api.ConditionProvider;
import org.sakaiproject.conditions.api.ConditionService;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.SortType;

public class GradebookConditionsProvider implements ConditionProvider {
	
	private Map<String, String> eventLookup = new HashMap<String, String>();
	
	private ConditionService conditionService;
	public void setConditionService(ConditionService conditionService) {
		this.conditionService = conditionService;
	}
	
	private GradebookService gbs;
	public void setGradebookService(GradebookService gradebookService) {
		this.gbs = gradebookService;
	}
	
	public void init() {
		eventLookup.put("gradebook.updateItemScore", "org.sakaiproject.conditions.impl.AssignmentGrading");
		eventLookup.put("gradebook.updateAssignment", "org.sakaiproject.conditions.impl.AssignmentUpdate");
		conditionService.registerConditionProvider(this);
	}

	public Map<String, String> getEntitiesForContext(String gradebookUid) {

		Map<String, String> rv = new LinkedHashMap<String, String>();

		if (!gbs.isGradebookDefined(gradebookUid)) {
			return rv;
		}
		
		List<Assignment> assignments = gbs.getAssignments(gradebookUid, SortType.SORT_BY_NAME);
		for (Assignment asn : assignments) {
			String assignmentName = asn.getName();
			String assignmentPoints = asn.getPoints().toString();
			boolean isReleasedToStudents = asn.isReleased();
			boolean isUsedInGradeCalculation = asn.isCounted();
			Date dueDate = asn.getDueDate();
			long dueDateMillis = 0;
			if (dueDate != null) dueDateMillis = dueDate.getTime();
			// event resource of the form: /gradebook/[gradebook id]/[assignment name]/[points possible]/[due date millis]/[is released]/[is included in course grade]/[has authz]
			rv.put("/gradebook/"+ gradebookUid + "/" + assignmentName + "/" + assignmentPoints + "/" + dueDateMillis + "/" + isReleasedToStudents + "/" + isUsedInGradeCalculation , assignmentName + " (" + assignmentPoints + " points)");
		}
		return rv;
	}

	public String getId() {
		return "gradebook";
	}

	public Map<String, String> getEventToDomainClassMapping() {
		return eventLookup;
	}

  public Map<String, String> getData(String type, String context) {
    Map<String, String> rv = new HashMap<String, String>();
    if ("grades".equals(type)) {
      String[] contextParts = context.split("\\|");
      String gradebookId = contextParts[0];
      String assignmentName = contextParts[1];
      String studentId = contextParts[2];
      Long assignmentId = null;
      for (Object assignment : gbs.getAssignments(gradebookId)) {
        if (((Assignment)assignment).getName().equals(assignmentName)) {
          assignmentId = ((Assignment)assignment).getId();
          break;
        }
      }
      if (assignmentId != null) {
        String score = gbs.getAssignmentScoreString(gradebookId, assignmentId, studentId);
        if (score == null) {
          score = "";
        }
        rv.put("score", score);
      }
    }

    return rv;
  }

}
