/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010 Sakai Foundation
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
package org.sakaiproject.conditions.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.conditions.api.Condition;
import org.sakaiproject.conditions.api.ConditionProvider;
import org.sakaiproject.conditions.api.ConditionService;
import org.sakaiproject.conditions.api.ConditionTemplateSet;
import org.sakaiproject.conditions.api.Rule;
import org.sakaiproject.conditions.api.Rule.Conjunction;

@Slf4j
public class BaseConditionService implements ConditionService, Observer {
	private Map<String, String> eventLookup = new HashMap<String, String>();
	private Map<String, ConditionProvider> registeredProviders = new HashMap<String, ConditionProvider>();
	
	public void init() { 
		log.info("init()");
	}

	public String addRule(String eventType, Rule rule) {
		return "foo";
	}

	public ConditionTemplateSet getConditionTemplateSetForService(
			String serviceId) {
		return null;
	}

	public Set<String> getRegisteredServiceNames() {
		return registeredProviders.keySet();
	}

	public void registerConditionTemplates(
			ConditionTemplateSet conditionTemplateSet) {		
	}

	public void update(Observable o, Object arg) {		
	}
	
	public Map<String, String> getEntitiesForServiceAndContext(String serviceName, String contextId) {
		ConditionProvider provider = registeredProviders.get(serviceName);
		
		if (provider == null) {
			return new HashMap<String,String>();
		}
		
		return provider.getEntitiesForContext(contextId);
	}

	public String getClassNameForEvent(String event) {
		return eventLookup.get(event);
	}

	public void registerConditionProvider(ConditionProvider provider) {
		this.eventLookup.putAll(provider.getEventToDomainClassMapping());
		this.registeredProviders.put(provider.getId(), provider);
	}

	public ConditionProvider getConditionProvider(String providerId) {
		return this.registeredProviders.get(providerId);
	}
	
	public Condition makeBooleanExpression(String eventDataClass, String missingTermQuery, String operatorValue, Object argument) {
		return new BooleanExpression(eventDataClass, missingTermQuery, operatorValue, argument);
	}

	public Rule makeRule(String resourceId, List<Condition> predicates,
			Conjunction or) {
		return new ResourceReleaseRule(resourceId, predicates, Rule.Conjunction.OR);
	}

}
