package org.sakaiproject.conditions.api;

import java.util.Map;

public interface ConditionProvider {
	
	String getId();
	
	Map<String, String> getEntitiesForContext(String contextId);
	
	Map<String, String> getEventToDomainClassMapping();
}