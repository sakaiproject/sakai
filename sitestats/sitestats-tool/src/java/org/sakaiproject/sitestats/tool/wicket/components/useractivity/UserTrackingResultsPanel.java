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
package org.sakaiproject.sitestats.tool.wicket.components.useractivity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.AttributeModifier;
import org.sakaiproject.sitestats.tool.wicket.components.SakaiDataTable;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.sitestats.api.event.detailed.DetailedEvent;
import org.sakaiproject.sitestats.api.event.detailed.TrackingParams;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.sitestats.tool.wicket.components.SakaiResponsiveAbstractColumn;
import org.sakaiproject.sitestats.tool.wicket.components.SakaiResponsivePropertyColumn;
import org.sakaiproject.sitestats.tool.wicket.providers.UserTrackingDataProvider;

/**
 * Panel displaying the results of a User Activity search using standard Wicket DataTable
 * @author plukasew, bjones86
 */
public class UserTrackingResultsPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	private final UserTrackingDataProvider provider;

	private String siteID;

	private SakaiDataTable resultsTable;

	/**
	 * Constructor
	 * @param id wicket id
	 * @param trackingParams parameters of the search
	 */
	public UserTrackingResultsPanel(String id, TrackingParams trackingParams)
	{
		super(id);
		siteID = trackingParams.siteId;
		provider = new UserTrackingDataProvider(trackingParams);
		
		// Add a CSS class to the panel for styling
		add(new AttributeModifier("class", "sakaiUserTrackingPanel"));
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		
		// Create a standard data table with Sakai navigation
		resultsTable = new SakaiDataTable(
			"table", (List)getTableColumns(), provider, true);
		
		resultsTable.setOutputMarkupId(true);
		resultsTable.setVersioned(false);
		resultsTable.add(new AttributeModifier("class", "table table-hover table-striped table-bordered userTrackingResults sakai-cardTable640"));
		
		add(resultsTable);
	}
	

	/**
	 * Sets the parameters to use for the search. Resets paging back to the first page since search params have changed.
	 * @param value the search parameters
	 */
	public void setTrackingParams(TrackingParams value)
	{
		provider.setTrackingParams(value);
		siteID = value.siteId;
		resultsTable.setCurrentPage(0); // new params, reset paging
	}

	private List<IColumn<DetailedEvent, String>> getTableColumns()
	{
		List<IColumn<DetailedEvent, String>> cols = new ArrayList<>();

		cols.add(new SakaiResponsivePropertyColumn<DetailedEvent, String>(new ResourceModel("de_resultsTable_timestamp"), "eventDate", "eventDate")
		{
			@Override
			public IModel getDataModel(IModel rowModel)
			{
				// Get the event date
				DetailedEvent event = (DetailedEvent) rowModel.getObject();
				Instant time = event.getEventDate().toInstant();
				return Model.of(Locator.getFacade().getUserTimeService().shortPreciseLocalizedTimestamp(time, getSession().getLocale()));
			}
		});

		cols.add(new SakaiResponsivePropertyColumn<DetailedEvent, String>(new ResourceModel("de_resultsTable_event"), "eventId", "eventId")
		{
			@Override
			public boolean isSortable()
			{
				return false;
			}

			@Override
			public IModel getDataModel(IModel rowModel)
			{
				// Get the event ID
				DetailedEvent event = (DetailedEvent) rowModel.getObject();
				String eventID = event.getEventId();

				// Get the fancy name for the event ID
				String fancyName = Locator.getFacade().getEventRegistryService().getEventName(eventID);
				return new Model<>(fancyName);
			}
		});

		cols.add(new SakaiResponsiveAbstractColumn<DetailedEvent, String>(new ResourceModel("de_resultsTable_details"), "")
		{
			@Override
			public void populateItemContribution(final Item<ICellPopulator<DetailedEvent>> item, final String componentId, final IModel<DetailedEvent> rowModel)
			{
				item.add(new EventRefDetailsButtonPanel(componentId, rowModel, siteID));
			}

			@Override
			public boolean isSortable()
			{
				return false;
			}
		});

		return cols;
	}
}
