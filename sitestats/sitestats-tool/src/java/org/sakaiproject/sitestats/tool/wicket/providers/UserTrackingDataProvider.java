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
package org.sakaiproject.sitestats.tool.wicket.providers;

import java.util.Iterator;
import java.util.List;

import lombok.Setter;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.sakaiproject.sitestats.api.event.detailed.DetailedEvent;
import org.sakaiproject.sitestats.api.event.detailed.DetailedEventsManager;
import org.sakaiproject.sitestats.api.event.detailed.PagingParams;
import org.sakaiproject.sitestats.api.event.detailed.SortingParams;
import org.sakaiproject.sitestats.api.event.detailed.TrackingParams;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.sitestats.tool.wicket.models.LoadableDetailedEventModel;

/**
 * A standard SortableDataProvider for User Activity searches.
 * @author plukasew, bjones86
 */
public class UserTrackingDataProvider extends SortableDataProvider<DetailedEvent, String>
{
	private static final long serialVersionUID = 1L;
	@Setter private TrackingParams trackingParams;
	private boolean hasNextPage = false;
	
	// Keep track of the last counts for better pagination display
	private long lastOffset = 0;
	private long lastReturnedCount = 0;

	/**
	 * Constructor
	 * @param trackingParams search parameters
	 */
	public UserTrackingDataProvider(TrackingParams trackingParams)
	{
		this.trackingParams = trackingParams;
		setSort("eventDate", SortOrder.ASCENDING);
	}

	@Override
	public Iterator<? extends DetailedEvent> iterator(long first, long count)
	{
		SortParam<String> sort = getSort();
		DetailedEventsManager dem = Locator.getFacade().getDetailedEventsManager();
		
		// Save for pagination display
		this.lastOffset = first;
		
		// Request one extra item to determine if there are more pages
		List<DetailedEvent> deList = dem.getDetailedEvents(trackingParams, new PagingParams(first, count + 1),
			new SortingParams(sort.getProperty(), sort.isAscending()));
		
		// Check if we have more pages
		hasNextPage = false;
		if (deList.size() > count)
		{
			hasNextPage = true;
			deList.remove(deList.size() - 1); // Remove the extra item
		}

		// Save for pagination display
		this.lastReturnedCount = deList.size();
		
		return deList.iterator();
	}

	@Override
	public long size()
	{
		// Get the actual total count from the DetailedEventsManager
		DetailedEventsManager dem = Locator.getFacade().getDetailedEventsManager();
		return dem.getDetailedEventsCount(trackingParams);
	}

	@Override
	public IModel<DetailedEvent> model(DetailedEvent event)
	{
		return new LoadableDetailedEventModel(event);
	}
	

	/**
	 * Returns true if there is a next page based on the last iterator call
	 * @return true if there are more results
	 */
	public boolean hasNextPage()
	{
		return hasNextPage;
	}
}
