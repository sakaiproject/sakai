package org.sakaiproject.conditions.api;

/**
 * 
 * @author Zach A. Thomas <zach@aeroplanesoftware.com>
 * 
 * This is a marker interface that denotes a class that can be
 * used to lookup Rules that must be evaluated in response to an event.
 * 
 * e.g. a GradebookAssignmentKey is an EventKey that contains a 
 * Gradebook ID and an Assignment name. Together these two Strings
 * uniquely identify a single Gradebook assignment that one or more
 * Rules can be bound to.
 * 
 * When the ConditionService is notified of an event, it creates an EventKey
 * from the data in the event. The EventKey can then be used as a lookup key
 * to retrieve Rules that must be evaluated in response to the event.
 *
 */
public interface EventKey {

}
