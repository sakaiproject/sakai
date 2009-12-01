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
package org.sakaiproject.conditions.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.conditions.api.Rule.Conjunction;

public interface ConditionService {
	
	
	/** Property for whether or not to apply a condition on the release of this entity [boolean] */
	public static final String PROP_CONDITIONAL_RELEASE = "SAKAI:conditionalrelease";
	
	/** Property for storing the notification id for the condition for this entity */
	public static final String PROP_CONDITIONAL_NOTIFICATION_ID = "SAKAI:conditionalNotificationId";
	
	/** Property for storing the submittedFunctionName for conditional release on this entity */
	public static final String PROP_SUBMITTED_FUNCTION_NAME = "SAKAI:submittedFunctionName";

	/** Property for storing the submittedResourceFilter for conditional release on this entity */
	public static final String PROP_SUBMITTED_RESOURCE_FILTER = "SAKAI:submittedResourceFilter";

	/** Property for storing the selectedConditionKey for conditional release on this entity */	
	public static final String PROP_SELECTED_CONDITION_KEY = "SAKAI:selectedConditionKey";
	
	public static final String PROP_CONDITIONAL_RELEASE_ARGUMENT = "SAKAI:conditionalReleaseArgument";
	
	public String addRule(String eventType, Rule rule);
	
	public Set<String> getRegisteredServiceNames();
	
	public ConditionTemplateSet getConditionTemplateSetForService(String serviceId);
	
	public void registerConditionTemplates(ConditionTemplateSet conditionTemplateSet);
	
	public void registerConditionProvider(ConditionProvider provider);
	
	public Condition makeCondition(Map<String,String> params);
	
	public String getClassNameForEvent(String eventName);

	public Map<String, String> getEntitiesForServiceAndContext(String serviceName, String contextId);

	public Condition makeBooleanExpression(String eventDataClass,
			String missingTermQuery, String operatorValue, Object argument);

	public Rule makeRule(String resourceId, List<Condition> predicates,
			Conjunction or);


}
