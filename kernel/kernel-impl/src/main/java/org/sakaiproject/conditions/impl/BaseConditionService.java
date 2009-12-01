/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/api/src/main/java/org/sakaiproject/antivirus/api/VirusFoundException.java $
 * $Id: VirusFoundException.java 68335 2009-10-29 08:18:43Z david.horwitz@uct.ac.za $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.conditions.api.Condition;
import org.sakaiproject.conditions.api.ConditionProvider;
import org.sakaiproject.conditions.api.ConditionService;
import org.sakaiproject.conditions.api.ConditionTemplateSet;
import org.sakaiproject.conditions.api.Rule;
import org.sakaiproject.conditions.api.Rule.Conjunction;


public class BaseConditionService implements ConditionService, Observer {

	
	private static Log log = LogFactory.getLog(BaseConditionService.class);
	
	private Map<String, String> eventLookup = new HashMap<String, String>();
	private Map<String, ConditionProvider> registeredProviders = new HashMap<String, ConditionProvider>();
	
	public void init() { }

	public String addRule(String eventType, Rule rule) {
		return "foo";
	}

	public ConditionTemplateSet getConditionTemplateSetForService(
			String serviceId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<String> getRegisteredServiceNames() {
		return registeredProviders.keySet();
	}

	public void registerConditionTemplates(
			ConditionTemplateSet conditionTemplateSet) {
		// TODO Auto-generated method stub
		
	}

	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}

	public Condition makeCondition(Map<String, String> params) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Map<String, String> getEntitiesForServiceAndContext(String serviceName, String contextId) {
		return registeredProviders.get(serviceName).getEntitiesForContext(contextId);
	}

	public String getClassNameForEvent(String event) {
		return eventLookup.get(event);
	}

	public void registerConditionProvider(ConditionProvider provider) {
		this.eventLookup.putAll(provider.getEventToDomainClassMapping());
		this.registeredProviders.put(provider.getId(), provider);
	}
	
	public Condition makeBooleanExpression(String eventDataClass, String missingTermQuery, String operatorValue, Object argument) {
		return new BooleanExpression(eventDataClass, missingTermQuery, operatorValue, argument);
	}

	public Rule makeRule(String resourceId, List<Condition> predicates,
			Conjunction or) {
		return new ResourceReleaseRule(resourceId, predicates, Rule.Conjunction.OR);
	}

}
