package org.sakaiproject.conditions.api;

import org.apache.commons.collections.Predicate;
import org.sakaiproject.event.api.NotificationAction;

/**
 * 
 * @author Zach A. Thomas <zach@aeroplanesoftware.com>
 * 
 * A Rule extends <code>org.apache.commons.collections.Predicate</code> and
 * decorates it with an enum that represents the relationship among the
 * Predicates within the rule: the Predicates that make up a rule are
 * either AND'd together or OR'd together.
 * 
 * A Rule is also a <code>Command</code>, which means it has an execute method that can
 * be invoked to perform some action.
 *
 */
public interface Rule extends Predicate, NotificationAction {

	/**
	 * A Rule may have an AND relationship among its Predicates, or it may have an OR.
	 * This enumeration is a convenience for setting this relationship on the Rule.
	 *
	 */
	enum Conjunction {
		AND, OR
	};
	
}
