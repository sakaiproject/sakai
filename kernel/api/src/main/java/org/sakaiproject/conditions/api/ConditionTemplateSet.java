package org.sakaiproject.conditions.api;

import java.util.Set;

public interface ConditionTemplateSet {
	
	public Set<ConditionTemplate> getConditionTemplates();
	
	public String getId();
	
	public String getDisplayName();

}
