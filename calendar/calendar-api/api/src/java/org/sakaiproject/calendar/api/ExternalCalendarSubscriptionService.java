/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.calendar.api;

import java.util.Collection;
import java.util.Set;

public interface ExternalCalendarSubscriptionService
{

	/** sakai.properties: enable/disable ical external subscriptions */
	public final static String SAK_PROP_EXTSUBSCRIPTIONS_ENABLED = "calendar.external.subscriptions.enable";
	
	/** sakai.properties: merge ical external subscriptions from other sites into My Workspace? */
	public final static String SAK_PROP_EXTSUBSCRIPTIONS_MERGEINTOMYWORKSPACE = "calendar.external.subscriptions.mergeIntoMyworkspace";

	/** sakai.properties: url list of ical external subscriptions */
	public final static String SAK_PROP_EXTSUBSCRIPTIONS_URL = "calendar.external.subscriptions.url";

	/** sakai.properties: name list of ical external subscriptions */
	public final static String SAK_PROP_EXTSUBSCRIPTIONS_NAME = "calendar.external.subscriptions.name";

	/** sakai.properties: event type forced list of ical external subscriptions */
	public final static String SAK_PROP_EXTSUBSCRIPTIONS_EVENTTYPE = "calendar.external.subscriptions.eventtype";

	/**
	 * sakai.properties: number of cached entries for institutional ical external subscriptions
	 */
	public final static String SAK_PROP_EXTSUBSCRIPTIONS_INST_CACHEENTRIES = "calendar.external.subscriptions.user.cacheentries";
	
	/**
	 * sakai.properties: cache time for institutional ical external
	 * subscriptions (iCal updated after expiration)
	 */
	public final static String SAK_PROP_EXTSUBSCRIPTIONS_INST_CACHETIME = "calendar.external.subscriptions.institutional.cachetime";

	/**
	 * sakai.properties: number of cached entries for user-provided ical external subscriptions
	 */
	public final static String SAK_PROP_EXTSUBSCRIPTIONS_USER_CACHEENTRIES = "calendar.external.subscriptions.user.cacheentries";

	/**
	 * sakai.properties: cache time for user-provided ical external
	 * subscriptions (iCal updated after expiration)
	 */
	public final static String SAK_PROP_EXTSUBSCRIPTIONS_USER_CACHETIME = "calendar.external.subscriptions.user.cachetime";

	/** Key value for ToolConfig */
	public final static String TC_PROP_SUBCRIPTIONS = "externalCalendarSubscriptions";
	public final static String TC_PROP_SUBCRIPTIONS_WITH_TZ = "externalCalendarSubscriptionsWithTZ";

	/** Value delimiter for subscription reference in ToolConfig */
	public final static String SUBS_REF_DELIMITER = "_,_";

	/** Value delimiter for subscription name in ToolConfig */
	public final static String SUBS_NAME_DELIMITER = "_::_";

	/** Check whether iCal external subscriptions are enabled */
	public boolean isEnabled();

	/** Build a Reference for an iCal external subscription */
	public String calendarSubscriptionReference(String context, String id);

	/** Get Calendar object from Calendar Subscription */
	public Calendar getCalendarSubscription(String reference);
	public Calendar getCalendarSubscription(String reference, String userId, String tzid);

	/** Get Calendar Subscriptions for specified Calendar channels */
	public Set<ExternalSubscriptionDetails> getCalendarSubscriptionChannelsForChannels(
			String primaryCalendarReference,
			Collection<Object> channels);

	/** Get Calendar channels for specified Calendar channels */
	public Set<ExternalSubscriptionDetails> getCalendarSubscriptionChannelsForChannel(String reference);

	/**
	 * Get list of available institutional calendar subscriptions for a given
	 * Calendar channel
	 */
	public Set<ExternalSubscriptionDetails> getAvailableInstitutionalSubscriptionsForChannel(
			String reference);

	/** Get list of calendar subscriptions for a given Calendar channel */
	public Set<ExternalSubscriptionDetails> getSubscriptionsForChannel(String reference,
			boolean loadCalendar);

	/** Set list of calendar subscriptions for a given Calendar channel */
	public void setSubscriptionsForChannel(String reference,
			Collection<ExternalSubscription> subscriptions);

	/** Get reference id for a specific subscription url */
	public String getIdFromSubscriptionUrl(String url);

	/** Get subscription url from a subscription reference id */
	public String getSubscriptionUrlFromId(String id);

	/** Check if reference references a institutional external calendar */
	public boolean isInstitutionalCalendar(String reference);

}
