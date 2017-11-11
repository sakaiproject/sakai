/**
 * Copyright (c) 2003-2011 The Apereo Foundation
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
package org.sakaiproject.conditions.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.conditions.api.Condition;
import org.sakaiproject.conditions.api.ConditionProvider;
import org.sakaiproject.conditions.api.ConditionService;
import org.sakaiproject.conditions.api.ConditionTemplateSet;
import org.sakaiproject.conditions.api.Rule;
import org.sakaiproject.conditions.api.Rule.Conjunction;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.NotificationAction;

public class ToyConditionsService implements ConditionService {
	
	private Map<String, ConditionTemplateSet> conditionTemplateSets = new HashMap<String, ConditionTemplateSet>();
	private Map<String,Collection<Rule>> rules = new HashMap<String, Collection<Rule>>();

	public void registerConditionTemplates(ConditionTemplateSet conditionTemplateSet) {
		this.conditionTemplateSets.put(conditionTemplateSet.getId(), conditionTemplateSet);
	}

	public ConditionTemplateSet getConditionTemplateSetForService(
			String serviceId) {
		return conditionTemplateSets.get(serviceId);
	}

	public Set<String> getRegisteredServiceNames() {
		return conditionTemplateSets.keySet();
	}

	public String addRule(String eventType, Rule rule) {
		Collection<Rule> rulesOfThisType = rules.get(eventType);
		if (rulesOfThisType == null) rulesOfThisType = new ArrayList<Rule>();
		rulesOfThisType.add(rule);
		rules.put(eventType, rulesOfThisType);
		return "123abc";
	}
	
	public void dispatchAnEvent(Event e) {
		Collection<Rule> rulesForEvent = rules.get(e.getEvent());
		if (rulesForEvent == null) return;
		for(Rule r : rules.get(e.getEvent())) {
			if(e.getEvent().equals("gradebook.newgrade")) {
				((NotificationAction)r).notify(null, e);
			}
		}
	}

	public String getClassNameForEvent(String eventName) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getEntitiesForServiceAndContext(
			String serviceName, String contextId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Condition makeCondition(Map<String, String> params) {
		// TODO Auto-generated method stub
		return null;
	}

	public void registerConditionProvider(ConditionProvider provider) {
		// TODO Auto-generated method stub
		
	}

  public ConditionProvider getConditionProvider(String providerId) {
    // TODO Auto-generated method stub
    return null;
  }

  public Condition makeBooleanExpression(String eventDataClass,
			String missingTermQuery, String operatorValue, Object argument) {
		// TODO Auto-generated method stub
		return null;
	}

	public Rule makeRule(String resourceId, List<Condition> predicates,
			Conjunction or) {
		// TODO Auto-generated method stub
		return null;
	}

}
