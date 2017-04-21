package org.sakaiproject.calendar.api;

/**
 * The basic details of an External Subscription
 */

public interface ExternalSubscription {

    /** Get subscription name */
    String getSubscriptionName();

    /** Get Reference of external subscription */
    String getReference();

}
