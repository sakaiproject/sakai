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
