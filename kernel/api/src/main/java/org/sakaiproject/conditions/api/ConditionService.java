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
 *       http://www.opensource.org/licenses/ECL-2.0
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

/**
 * Facilities for creating and storing conditions to be evaluated at some future point in time
 * @author Zach Thomas <zach@aeroplanesoftware.com>
 *
 */
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
	
	/** Property for storing the argument that is bundled with a condition and used to evaluate it */
	public static final String PROP_CONDITIONAL_RELEASE_ARGUMENT = "SAKAI:conditionalReleaseArgument";
	
	/**
	 * return a set of names of services that have registered with the conditions service as condition providers
	 * @return <code>Set</code> of names
	 */
	public Set<String> getRegisteredServiceNames();
	
	/**
	 * Condition providers call this method to register themselves with the conditions service,
	 * which makes their services discoverable by condition consumers
	 * 
	 * Registration should typically occur at the time the condition provider starts up
	 * 
	 * @param provider the <code>ConditionProvider</code> to be registered
	 */
	public void registerConditionProvider(ConditionProvider provider);

  /**
   *
   * @param providerId
   * @return the named ConditionProvider, or null if the provider doesn't exist
   */
  public ConditionProvider getConditionProvider(String providerId);
	
	/**
	 * gets the name of the class that the specified event concerns itself with.
	 * For example, when a gradebook.updateItemScore is fired, 
	 * the class we use to encapsulate that event is <code>org.sakaiproject.conditions.impl.AssignmentGrading</code>
	 * 
	 * Getting the class name enables the condition consumer to instantiate an object of this type
	 * 
	 * @param eventName
	 * @return class name that corresponds to the requested event
	 */
	public String getClassNameForEvent(String eventName);

	/**
	 * allows the conditions consumer to access key-value pairs to use in a UI
	 * The key of each pair is an identifier for a particular entity in the target service, e.g. an assignment
	 * The value of the pair is a display name to use in the UI
	 * @param serviceName conditions provider, e.g. 'gradebook'
	 * @param contextId a Sakai context id, such as a site guid
	 * @return <code>Map</code> of strings, where the key uniquely identifies an entity, and the value is a display name for that entity
	 */
	public Map<String, String> getEntitiesForServiceAndContext(String serviceName, String contextId);

	/**
	 * factory method for producing a Condition object which 
	 * @param eventDataClass name of the class that will be constituted when this Condition is evaluated
	 * @param missingTermQuery name of the operation that we're performing on the data, e.g. 'getScore' or 'dueDateHasPassed'
	 * @param operatorValue name which identifies a boolean operator to use in the condition, e.g. 'less_than'
	 * @param argument the value that the expression will be evaluated with.
	 * e.g. for a 'less_than' operator, argument might be an <code>Integer</code> like 79
	 * @return a <code>Condition</code> object that encapsulates the evaluation we wish to perform with the specified terms
	 */
	public Condition makeBooleanExpression(String eventDataClass,
			String missingTermQuery, String operatorValue, Object argument);

	/**
	 * factory method to create a <code>Rule</code> which is a list of conditions,
	 * the id of a resource that the Rule applies to, and a <code>Conjunction</code>
	 * which just says whether the conditions are meant to be OR'd or AND'd together
	 * @param resourceId
	 * @param predicates
	 * @param or
	 * @return the <code>Rule</code> object represented by the included terms
	 */
	public Rule makeRule(String resourceId, List<Condition> predicates,
			Conjunction or);
	
	/**
	 * this is the <code>ConditionService</code>'s facility for persisting rules
	 * in the current implementation, the Sakai event API is used to store a <code>Rule</code>
	 * as the action portion of a <code>NotificationEdit</code>
	 * 
	 * In the long run, though, the condition service should probably handle its own persistence of rules.
	 * @param eventType an event name, like gradebook.updateItemScore. The Rule will be evaluated whenever
	 * that type of event comes over the wire
	 * @param rule the <code>Rule</code> to be persisted
	 * @return a unique id used as a key for this <code>Rule</code>
	 */
	public String addRule(String eventType, Rule rule);
	
	/**
	 * returns the set of <code>ConditionTemplate</code>s supplied by a condition provider
	 * the condition consumers will use these templates to allow users to create new conditions
	 * @param serviceId the name of condition provider to request <code>ConditionTemplate</code>s from, e.g. 'gradebook'
	 * @return
	 */
	public ConditionTemplateSet getConditionTemplateSetForService(String serviceId);
	
	/**
	 * method for condition provider to specify its set of <code>ConditionTemplate</code>s
	 * @param conditionTemplateSet the service's (e.g. gradebook) condition templates
	 */
	public void registerConditionTemplates(ConditionTemplateSet conditionTemplateSet);

}
