package org.sakaiproject.conditions.impl;

import java.util.HashSet;
import java.util.Set;

import org.sakaiproject.conditions.api.ConditionTemplate;
import org.sakaiproject.conditions.api.ConditionTemplateSet;

public class MyConditionTemplateSet implements ConditionTemplateSet {
	private Set<ConditionTemplate> myConditionTemplates = new HashSet<ConditionTemplate>();
	
	public MyConditionTemplateSet() {
		ConditionTemplate aConditionTemplate = new MyConditionTemplate();
		myConditionTemplates.add(aConditionTemplate);
	}

	public Set<ConditionTemplate> getConditionTemplates() {
		return myConditionTemplates;
	}

	public String getDisplayName() {
		return "Gradebook";
	}

	public String getId() {
		return "sakai.service.gradebook";
	}

}
