/**
 * Copyright (c) 2006-2019 The Apereo Foundation
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
package org.sakaiproject.sitestats.tool.wicket.models;

import org.apache.wicket.model.LoadableDetachableModel;

import org.sakaiproject.sitestats.api.event.detailed.DetailedEventsManager;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;
import org.sakaiproject.sitestats.tool.facade.Locator;

/**
 * LoadableDetachableModel for ResolvedEventData objects. Loads from an event reference.
 * @author plukasew, bjones86
 */
public class LoadableEventRefDetailsModel extends LoadableDetachableModel<ResolvedEventData>
{
	private final String eventType, eventRef, siteID;

	/**
	 * Constructor
	 * @param eventType event type
	 * @param eventRef event reference
	 * @param siteID site id
	 */
	public LoadableEventRefDetailsModel(String eventType, String eventRef, String siteID)
	{
		this.eventType = eventType;
		this.eventRef = eventRef;
		this.siteID = siteID;
	}

	@Override
	protected ResolvedEventData load()
	{
		DetailedEventsManager dem = Locator.getFacade().getDetailedEventsManager();
		return dem.resolveEventReference(eventType, eventRef, siteID);
	}
}
