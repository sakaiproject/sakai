/**********************************************************************************
 * Copyright (c) 2003-2017 The Apereo Foundation
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
 **********************************************************************************/
package org.sakaiproject.calendar.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.calendar.api.RecurrenceRule;

/**
 * Plain, cache-marshalable snapshot of a fetched external calendar subscription.
 * <p>
 * {@code BaseExternalSubscriptionDetails.calendar} (an {@code ExternalCalendarSubscription})
 * and its events ({@code ExternalCalendarEvent}) are non-static inner classes of
 * {@link BaseExternalCalendarSubscriptionService}, so caching them directly would drag the
 * whole singleton service instance (and everything it depends on) into the cached entry.
 * This type carries only the plain data needed to replay the existing
 * {@code ExternalCalendarSubscription.addEvent(...)} construction call on a cache hit.
 * <p>
 * Each event's time range is stored as plain start/end millis rather than a {@code TimeRange}:
 * the concrete {@code TimeRange} implementation ({@code BasicTimeService.MyTimeRange}) is
 * itself a non-static inner class (of {@code BasicTimeService}), so it isn't safe to embed
 * directly either.
 */
public class ExternalCalendarSubscriptionSnapshot {

	public String subscriptionName;
	public String subscriptionUrl;
	public String context;
	public boolean institutional;
	public String userId;
	public String tzid;

	public boolean ok;
	public String error;
	public Instant refreshed;

	/** Null/empty below when !ok, matching a failed fetch having no calendar. */
	public String calendarName;
	public List<EventSnapshot> events = new ArrayList<>();

	public static class EventSnapshot {
		public String displayName;
		public String description;
		public String type;
		public String location;
		public long rangeStartMillis;
		public long rangeEndMillis;
		public RecurrenceRule recurrenceRule;
	}
}
