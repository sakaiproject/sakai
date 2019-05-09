/**
 * Copyright (c) 2006-2018 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.api.event.detailed;

import java.util.List;
import java.util.Optional;

/**
 * Manages retrieval of detailed event and user tracking data
 *
 * @author plukasew, bjones86
 */
public interface DetailedEventsManager
{
	/**
	 * Returns the detailed events matching the given tracking parameters, limited by the paging parameters,
	 * and in the order specified by the sorting parameters
	 *
	 * @param trackingParams parameters related to site, user, event types, and date range.
	 * @param pagingParams parameters related to query paging
	 * @param sortingParams parameters related to sort order of the results
	 * @return a sorted list of detailed events that match the tracking and paging specifications
	 */
	public List<DetailedEvent> getDetailedEvents(final TrackingParams trackingParams, final PagingParams pagingParams, final SortingParams sortingParams);

	/**
	 * Retrieves a detailed event by id
	 * @param id the id of the detailed event
	 * @return the detailed event that matches the criteria, or empty if not found
	 */
	public Optional<DetailedEvent> getDetailedEventById(final long id);

	/**
	 * Returns true if the event ref for the given event type can be resolved to provide additional details about the event.
	 * @param eventType the event id
	 * @return true if the event ref can be resolved to provide additional details
	 */
	public boolean isResolvable(String eventType);

	/**
	 * For the given event and reference, attempts to resolve the ref to provide additional details about the event
	 * @param eventType the event id
	 * @param eventRef the event ref to resolve
	 * @param siteID the site the event occurred in
	 * @return object representing additional details about the event
	 */
	public ResolvedEventData resolveEventReference(String eventType, String eventRef, String siteID);

	/**
	 * Get a list of (minified) User objects who can be tracked in the given site (for the user tracking tool's user drop down).
	 * @param siteID the ID of the site to retrieve users for
	 * @return a List of user uuids from the site who can be tracked
	 */
	public List<String> getUsersForTracking(String siteID);
}
