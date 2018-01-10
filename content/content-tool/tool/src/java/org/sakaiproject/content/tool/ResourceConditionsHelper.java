/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/content/trunk/content-tool/tool/src/java/org/sakaiproject/content/tool/ResourceTypeLabeler.java $
 * $Id: ResourceTypeLabeler.java 59674 2009-04-03 23:05:58Z arwhyte@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009, 2010 The Sakai Foundation
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

package org.sakaiproject.content.tool;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.conditions.api.Condition;
import org.sakaiproject.conditions.api.ConditionService;
import org.sakaiproject.conditions.api.Rule;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.event.api.NotificationLockedException;
import org.sakaiproject.event.api.NotificationNotDefinedException;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class ResourceConditionsHelper {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3875833398687224551L;
	
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
		log.debug("Selected condition value: {}", selectedConditionValue);
		//The selectCondition value must be broken up so we can get at the values
		//that make up the index, submittedFunctionName, missingTermQuery, and operatorValue in that order
		String[] conditionTokens = selectedConditionValue.split("\\|");
		int selectedIndex = Integer.valueOf(conditionTokens[0]);
		String submittedFunctionName = conditionTokens[1];
		String missingTermQuery = conditionTokens[2];
		String operatorValue = conditionTokens[3];
		log.debug("submittedFunctionName: {}", submittedFunctionName);
		log.debug("missingTermQuery: {}", missingTermQuery);
		log.debug("operatorValue: {}", operatorValue);			
		String submittedResourceFilter = params.get("selectResource" + ListItem.DOT + index);
		// the number of grade points are tagging along for the ride. chop this off.
		String[] resourceTokens = submittedResourceFilter.split("/");
		String assignmentPointsString = resourceTokens[4];
		submittedResourceFilter = "/" + resourceTokens[1] + "/" + resourceTokens[2] + "/" + resourceTokens[3];
		String additionalAssignmentInfo = "/" + resourceTokens[4] + "/" + resourceTokens[5] + "/" + resourceTokens[6] + "/" + resourceTokens[7];
		log.debug("submittedResourceFilter: {}", submittedResourceFilter);
		String eventDataClass = conditionService.getClassNameForEvent(submittedFunctionName);
		Object argument = null;
		if ((selectedIndex == 1) || (selectedIndex == 2)) {
			argument = "dateMillis:"+resourceTokens[5];
		}
		if ((selectedIndex == 9) || (selectedIndex == 10)) {
			try {
				argument = Double.valueOf(params.get("assignment_grade" + ListItem.DOT + index));
			} catch (NumberFormatException e) {
				VelocityPortletPaneledAction.addAlert(state, rb.getString("conditions.invalid.condition.argument"));
				return;
			}
			double assignmentPoints = 0;
			try {
				assignmentPoints = new Double(assignmentPointsString);
			} catch (NumberFormatException e) {
				return;
			}
			if (((Double)argument < 0) || ((Double)argument > assignmentPoints)) {
			    VelocityPortletPaneledAction.addAlert(state, rb.getFormattedMessage("conditions.condition.argument.outofrange", new String[] { assignmentPointsString }));
				return;
			}
			log.debug("argument: {}", argument);
		}

		if (cbSelected) {
			if (item.useConditionalRelease) {
				log.debug("Previous condition exists. Removing related notification");
				removeExistingNotification(item, state);
			}
			
			String containingCollectionId = item.containingCollectionId;
			String resourceId = item.getId();
			String extension = params.get("extension" + ListItem.DOT + index);
			if (extension != null && !extension.equals("")) {
				resourceId = resourceId + extension;
			}
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
			notification.setResourceFilter(submittedResourceFilter);
			if (missingTermQuery.contains("Date")) {
				notification.addFunction("datetime.update");
			}
			notification.setAction(resourceConditionRule);
			notification.getProperties().addProperty(ConditionService.PROP_SUBMITTED_FUNCTION_NAME, submittedFunctionName);
			notification.getProperties().addProperty(ConditionService.PROP_SUBMITTED_RESOURCE_FILTER, submittedResourceFilter);
			notification.getProperties().addProperty(ConditionService.PROP_SELECTED_CONDITION_KEY, selectedConditionValue);
			notification.getProperties().addProperty(ConditionService.PROP_CONDITIONAL_RELEASE_ARGUMENT, params.get("assignment_grade" + ListItem.DOT + index));
			notification.getProperties().addProperty("SAKAI:conditionEventState", additionalAssignmentInfo);
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
		return ServerConfigurationService.getBoolean("conditions.service.enabled", Boolean.FALSE)
			&& conditionService != null && !conditionService.getRegisteredServiceNames().isEmpty();
	}


	void loadConditionData(SessionState state) {
		if (! conditionsEnabled()) {
			return;
		}
		log.debug("Loading condition data");
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
				VelocityPortletPaneledAction.addAlert(state, rb.getString("notification.load.error"));								
			}					
		}
		
		
		Map<String,String> resourceSelections = conditionService.getEntitiesForServiceAndContext("gradebook", ToolManager.getCurrentPlacement().getContext());
		
		//TODO look this data up
		//Using LinkedHashMap to maintain order
		Map<String,String> conditionSelections = new LinkedHashMap<String,String>();
		conditionSelections.put("1|gradebook.updateAssignment|dueDateHasPassed|no_operator",rb.getString("conditional.duedate_passed"));
		conditionSelections.put("2|gradebook.updateAssignment|dueDateHasNotPassed|no_operator",rb.getString("conditional.duedate_notpassed"));
		conditionSelections.put("3|gradebook.updateAssignment|isReleasedToStudents|no_operator",rb.getString("conditional.released_to_students"));
		conditionSelections.put("4|gradebook.updateAssignment|isNotReleasedToStudents|no_operator",rb.getString("conditional.not_released_to_students"));
		conditionSelections.put("5|gradebook.updateAssignment|isIncludedInCourseGrade|no_operator",rb.getString("conditional.included_in_course_grade"));
		conditionSelections.put("6|gradebook.updateAssignment|isNotIncludedInCourseGrade|no_operator",rb.getString("conditional.not_included_in_course_grade"));
		conditionSelections.put("7|gradebook.updateItemScore|isScoreBlank|no_operator", rb.getString("conditional.grade_blank"));
		conditionSelections.put("8|gradebook.updateItemScore|isScoreNonBlank|no_operator", rb.getString("conditional.grade_non_blank"));
		conditionSelections.put("9|gradebook.updateItemScore|getScore|less_than",rb.getString("conditional.grade_less_than"));
		conditionSelections.put("10|gradebook.updateItemScore|getScore|greater_than_equal_to",rb.getString("conditional.grade_greather_or_equal"));	
		
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
		log.debug("Removing condition");	
		try {
			NotificationEdit notificationToRemove = NotificationService.editNotification(item.getNotificationId());
			NotificationService.removeNotification(notificationToRemove);
		} catch (NotificationLockedException e) {
			VelocityPortletPaneledAction.addAlert(state, rb.getString("conditions.disable.error"));				
		} catch (NotificationNotDefinedException e) {
			VelocityPortletPaneledAction.addAlert(state, rb.getString("conditions.disable.error"));								
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
				log.error(e.getMessage(), e);
			}
		}
			
		if (resourceNotification != null) {
			String eventDataString = resourceNotification.getProperties().getProperty("SAKAI:conditionEventState");
			// event resource of the form: /gradebook/[gradebook id]/[assignment name]/[points possible]/[due date millis]/[is released]/[is included in course grade]/[has authz]
			String resource = resourceNotification.getResourceFilter();
			if (resource == null) resource = "/gradebook/null/null";
			EventTrackingService.post(EventTrackingService.newEvent("cond+" + resourceNotification.getFunction(), resource + eventDataString, true));
		}
		
	}

}
