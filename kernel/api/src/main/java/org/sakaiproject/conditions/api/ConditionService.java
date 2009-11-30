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
