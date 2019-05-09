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

import java.util.List;

import lombok.Setter;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.model.IModel;

import org.sakaiproject.sitestats.api.event.detailed.DetailedEvent;
import org.sakaiproject.sitestats.api.event.detailed.DetailedEventsManager;
import org.sakaiproject.sitestats.api.event.detailed.PagingParams;
import org.sakaiproject.sitestats.api.event.detailed.SortingParams;
import org.sakaiproject.sitestats.api.event.detailed.TrackingParams;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.sitestats.tool.wicket.models.LoadableDetailedEventModel;
import org.sakaiproject.sitestats.tool.wicket.providers.infinite.AbstractSortableInfiniteDataProvider;
import org.sakaiproject.sitestats.tool.wicket.providers.infinite.PagedInfiniteIterator;

/**
 * An InfiniteDataProvider for User Activity searches.
 * @author plukasew, bjones86
 */
public class UserTrackingDataProvider extends AbstractSortableInfiniteDataProvider<DetailedEvent, String>
{
	@Setter private TrackingParams trackingParams;

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
	public PagedInfiniteIterator<DetailedEvent> iterator(long first, long count)
	{
		SortParam sort = getSort();
		DetailedEventsManager dem = Locator.getFacade().getDetailedEventsManager();
		List<DetailedEvent> deList = dem.getDetailedEvents(trackingParams, new PagingParams(first, count + 1),
			new SortingParams(sort.getProperty().toString(), sort.isAscending()));
		int numResults = deList.size();
		boolean hasNextPage = false;
		if (numResults > count)
		{
			hasNextPage = true;
			numResults--;
			deList.remove(numResults);
		}

		return new PagedInfiniteIterator(deList.iterator(), first > 0, hasNextPage, numResults);
	}

	@Override
	public IModel model(DetailedEvent event)
	{
		return new LoadableDetailedEventModel(event);
	}
}
