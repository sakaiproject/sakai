package org.sakaiproject.content.tool;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.conditions.api.Condition;
import org.sakaiproject.conditions.api.Rule;
import org.sakaiproject.conditions.api.ConditionService;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.event.api.NotificationLockedException;
import org.sakaiproject.event.api.NotificationNotDefinedException;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;


public class ResourceConditionsHelper extends VelocityPortletPaneledAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3875833398687224551L;

	static final Log logger = LogFactory.getLog(ResourceConditionsHelper.class);
	
	static final ConditionService conditionService = (ConditionService)ComponentManager.get("org.sakaiproject.conditions.api.ConditionService");
	
	/** Resource bundle using current language locale */
    private static ResourceLoader rb = new ResourceLoader("content");
	
	static void saveCondition(ListItem item, ParameterParser params, SessionState state, int index) {
		if (! conditionsEnabled()) {
			return;
		}
		boolean cbSelected = Boolean.valueOf(params.get("cbCondition" + ListItem.DOT + index));
		String selectedConditionValue = params.get("selectCondition" + ListItem.DOT + index);
		if (selectedConditionValue == null) return;
		logger.debug("Selected condition value: " + selectedConditionValue);
		//The selectCondition value must be broken up so we can get at the values
		//that make up the index, submittedFunctionName, missingTermQuery, and operatorValue in that order
		String[] conditionTokens = selectedConditionValue.split("\\|");
		int selectedIndex = Integer.valueOf(conditionTokens[0]);
		String submittedFunctionName = conditionTokens[1];
		String missingTermQuery = conditionTokens[2];
		String operatorValue = conditionTokens[3];
		logger.debug("submittedFunctionName: " + submittedFunctionName);
		logger.debug("missingTermQuery: " + missingTermQuery);
		logger.debug("operatorValue: " + operatorValue);			
		String submittedResourceFilter = params.get("selectResource" + ListItem.DOT + index);
		// the number of grade points are tagging along for the ride. chop this off.
		String assignmentPoints = submittedResourceFilter.substring(submittedResourceFilter.lastIndexOf("/") + 1);
		submittedResourceFilter = submittedResourceFilter.substring(0, submittedResourceFilter.lastIndexOf("/"));
		logger.debug("submittedResourceFilter: " + submittedResourceFilter);
		String eventDataClass = conditionService.getClassNameForEvent(submittedFunctionName);
		Object argument = null;
		if ((selectedIndex == 9) || (selectedIndex == 10)) {
			try {
				argument = Double.valueOf(params.get("assignment_grade" + ListItem.DOT + index));
			} catch (NumberFormatException e) {
				return;
			}
			logger.debug("argument: " + argument);
		}

		if (cbSelected) {
			if (item.useConditionalRelease) {
				logger.debug("Previous condition exists. Removing related notification");
				removeExistingNotification(item, state);
			}
			
			String containingCollectionId = item.containingCollectionId;
			String resourceId = item.getId();
			if (! resourceId.startsWith(containingCollectionId)) {
				resourceId = containingCollectionId + resourceId;
				if (item.isCollection() && !resourceId.endsWith("/")) resourceId = resourceId + "/";
			}
			List<Condition> predicates = new ArrayList();
			Condition resourcePredicate = conditionService.makeBooleanExpression(eventDataClass, missingTermQuery, operatorValue, argument);
			
			predicates.add(resourcePredicate);
			
			Rule resourceConditionRule = conditionService.makeRule(resourceId, predicates, Rule.Conjunction.OR);
			NotificationEdit notification = NotificationService.addNotification();
			notification.addFunction(submittedFunctionName);
			notification.addFunction("cond+" + submittedFunctionName);
			if (missingTermQuery.contains("Date")) {
				notification.addFunction("datetime.update");
			}
			notification.setAction(resourceConditionRule);
			notification.setResourceFilter(submittedResourceFilter);
			notification.getProperties().addProperty(ConditionService.PROP_SUBMITTED_FUNCTION_NAME, submittedFunctionName);
			notification.getProperties().addProperty(ConditionService.PROP_SUBMITTED_RESOURCE_FILTER, submittedResourceFilter);
			notification.getProperties().addProperty(ConditionService.PROP_SELECTED_CONDITION_KEY, selectedConditionValue);
			notification.getProperties().addProperty(ConditionService.PROP_CONDITIONAL_RELEASE_ARGUMENT, params.get("assignment_grade" + ListItem.DOT + index));
			NotificationService.commitEdit(notification);
			
			item.setUseConditionalRelease(true);
			item.setNotificationId(notification.getId());
		} else {
			//only remove the condition if it previously existed
			if (item.useConditionalRelease) {
				item.setUseConditionalRelease(false);
				removeExistingNotification(item, state);
			}			
		}
		
	}

	
	private static boolean conditionsEnabled() {
		return ServerConfigurationService.getBoolean("conditions.service.enabled", Boolean.FALSE);
	}


	void loadConditionData(SessionState state) {
		if (! conditionsEnabled()) {
			return;
		}
		logger.debug("Loading condition data");
		ListItem item = (ListItem) state.getAttribute(ResourcesAction.STATE_REVISE_PROPERTIES_ITEM);
		if ((item != null) && (item.useConditionalRelease)) {
			try {
				Notification notification = NotificationService.getNotification(item.getNotificationId());			
				if (notification != null) {
					item.setSubmittedFunctionName(notification.getProperties().getProperty(ConditionService.PROP_SUBMITTED_FUNCTION_NAME));
					item.setSubmittedResourceFilter(notification.getProperties().getProperty(ConditionService.PROP_SUBMITTED_RESOURCE_FILTER));
					item.setSelectedConditionKey(notification.getProperties().getProperty(ConditionService.PROP_SELECTED_CONDITION_KEY));
					item.setConditionArgument(notification.getProperties().getProperty(ConditionService.PROP_CONDITIONAL_RELEASE_ARGUMENT));					
				}
			} catch (NotificationNotDefinedException e) {
				addAlert(state, rb.getString("notification.load.error"));								
			}					
		}
		
		
		Map<String,String> resourceSelections = conditionService.getEntitiesForServiceAndContext("gradebook", ToolManager.getCurrentPlacement().getContext());
		
		//TODO look this data up
		//Using LinkedHashMap to maintain order
		Map<String,String> conditionSelections = new LinkedHashMap<String,String>();
		conditionSelections.put("1|gradebook.updateAssignment|dueDateHasPassed|no_operator","due date has passed.");
		conditionSelections.put("2|gradebook.updateAssignment|dueDateHasNotPassed|no_operator","due date has not passed.");
		conditionSelections.put("3|gradebook.updateAssignment|isReleasedToStudents|no_operator","is released to students.");
		conditionSelections.put("4|gradebook.updateAssignment|isNotReleasedToStudents|no_operator","is not released to students.");
		conditionSelections.put("5|gradebook.updateAssignment|isIncludedInCourseGrade|no_operator","is included in course grade.");
		conditionSelections.put("6|gradebook.updateAssignment|isNotIncludedInCourseGrade|no_operator","is not included in course grade.");
		conditionSelections.put("7|gradebook.updateItemScore|isScoreBlank|no_operator", "grade is blank.");
		conditionSelections.put("8|gradebook.updateItemScore|isScoreNonBlank|no_operator", "grade is non-blank.");
		conditionSelections.put("9|gradebook.updateItemScore|getScore|less_than","grade is less than:");
		conditionSelections.put("10|gradebook.updateItemScore|getScore|greater_than_equal_to","grade is greater than or equal to:");	
		
		//This isn't the final resting place for this data..see the buildReviseMetadataContext method in this class
		state.setAttribute("resourceSelections", resourceSelections);
		state.setAttribute("conditionSelections", conditionSelections);
		if (item != null) {
			state.setAttribute("conditionArgument", item.getConditionArgument());			
		}
	}

	static void removeExistingNotification(ListItem item, SessionState state) {
		if (! conditionsEnabled()) {
			return;
		}
		logger.debug("Removing condition");	
		try {
			NotificationEdit notificationToRemove = NotificationService.editNotification(item.getNotificationId());
			NotificationService.removeNotification(notificationToRemove);
		} catch (NotificationLockedException e) {
			addAlert(state, rb.getString("disable.condition.error"));				
		} catch (NotificationNotDefinedException e) {
			addAlert(state, rb.getString("disable.condition.error"));								
		}		
	}
	

	
	static void buildConditionContext(Context context, SessionState state) {
		if (! conditionsEnabled()) {
			context.put("conditions_enabled", Boolean.FALSE);
			return;
		}
		context.put("conditions_enabled", Boolean.TRUE);
		context.put("resourceSelections", state.getAttribute("resourceSelections"));
		context.put("conditionSelections", state.getAttribute("conditionSelections"));		
	}
	
	static void notifyCondition(Entity entity) {
		if (! conditionsEnabled()) {
			return;
		}
		Notification resourceNotification = null;
		String notificationId = entity.getProperties().getProperty(ConditionService.PROP_CONDITIONAL_NOTIFICATION_ID);
		if (notificationId != null && !"".equals(notificationId)) {
			try {
				resourceNotification = NotificationService.getNotification(notificationId);
			} catch (NotificationNotDefinedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			
		if (resourceNotification != null) {
			EventTrackingService.post(EventTrackingService.newEvent("cond+" + resourceNotification.getFunction(), resourceNotification.getResourceFilter(), true));
		}
		
	}

}
