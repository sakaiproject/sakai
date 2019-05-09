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
package org.sakaiproject.sitestats.tool.wicket.models;

import org.apache.wicket.model.LoadableDetachableModel;
import org.sakaiproject.sitestats.api.event.detailed.DetailedEvent;
import org.sakaiproject.sitestats.api.event.detailed.DetailedEventsManager;
import org.sakaiproject.sitestats.tool.facade.Locator;

/**
 * LoadableDetachableModel for DetailedEvent objects. Loads with the event matching the given id.
 * @author plukasew, bjones86
 */
public class LoadableDetailedEventModel extends LoadableDetachableModel<DetailedEvent>
{
	private final long id;

	/**
	 * Constructor
	 * @param id unique id of the DetailedEvent
	 */
	public LoadableDetailedEventModel(long id)
	{
		this.id = id;
	}

	/**
	 * Constructor. Sets the model object to the given DetailedEvent, will not trigger a load() call.
	 * @param event the new model object
	 */
	public LoadableDetailedEventModel(DetailedEvent event)
	{
		this.id = event.getId();
		setObject(event);
	}

	@Override
	protected DetailedEvent load()
	{
		DetailedEventsManager dem = Locator.getFacade().getDetailedEventsManager();
		return dem.getDetailedEventById(id).orElse(null);
	}
}
