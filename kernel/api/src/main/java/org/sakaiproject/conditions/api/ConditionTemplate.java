package org.sakaiproject.conditions.api;

import java.util.Map;

public interface ConditionTemplate {
	
	public Condition conditionFromParameters(Map<String, String> params);

}
