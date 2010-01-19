package org.sakaiproject.tool.gradebook.facades.sakai2impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.conditions.api.ConditionProvider;
import org.sakaiproject.conditions.api.ConditionService;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.GradebookService;

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
		Map<String, String> rv = new HashMap<String, String>();
		List<Assignment> assignments = gbs.getAssignments(gradebookUid);
		for (Assignment asn : assignments) {
			String assignmentName = asn.getName();
			String assignmentPoints = asn.getPoints().toString();
			rv.put("/gradebook/"+ gradebookUid + "/" + assignmentName + "/" + assignmentPoints, assignmentName + " (" + assignmentPoints + " points)");
		}
		return rv;
	}

	public String getId() {
		return "gradebook";
	}

	public Map<String, String> getEventToDomainClassMapping() {
		return eventLookup;
	}

}
