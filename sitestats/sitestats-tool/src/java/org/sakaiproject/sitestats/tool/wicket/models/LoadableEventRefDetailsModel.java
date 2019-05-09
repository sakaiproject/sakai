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
