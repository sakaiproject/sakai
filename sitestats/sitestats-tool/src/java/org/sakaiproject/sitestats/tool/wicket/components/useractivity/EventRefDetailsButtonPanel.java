package org.sakaiproject.sitestats.tool.wicket.components.useractivity;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.sakaiproject.sitestats.api.event.detailed.DetailedEvent;
import org.sakaiproject.sitestats.api.event.detailed.DetailedEventsManager;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.sitestats.tool.wicket.components.SakaiAjaxButton;

/**
 * A panel showing a button that can be clicked to show more details about an event
 * @author plukasew, bjones86
 */
public class EventRefDetailsButtonPanel extends GenericPanel<DetailedEvent>
{
	public final boolean resolvable;

	private final String siteID;

	/**
	 * Constructor
	 * @param id wicket id
	 * @param model the model
	 * @param siteID site id
	 */
	public EventRefDetailsButtonPanel(String id, IModel<DetailedEvent> model, String siteID)
	{
		super(id, model);
		this.siteID = siteID;
		DetailedEventsManager dem = Locator.getFacade().getDetailedEventsManager();
		resolvable = dem.isResolvable(model.getObject().getEventId());
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		Form<Void> form = new Form<>("moreDetailsForm");
		SakaiAjaxButton button = new SakaiAjaxButton("moreDetailsButton", form)
		{
			@Override
			public void onSubmit(AjaxRequestTarget target, Form form)
			{
				if (target != null)
				{
					DetailedEvent de = EventRefDetailsButtonPanel.this.getModelObject();
					EventRefDetailsPanel panel = new EventRefDetailsPanel("details", de.getEventId(), de.getEventRef(), siteID);
					panel.setOutputMarkupId(true);
					form.getParent().replace(panel);
					target.add(panel);
					setVisible(false);
					target.add(this);
					target.prependJavaScript(String.format("$('#%s').closest('td').removeAttr('data-label');", getMarkupId()));
				}
			}
		};
		form.add(button);
		add(form);

		add(new WebMarkupContainer("details").setOutputMarkupPlaceholderTag(true));
	}

	@Override
	public boolean isVisible()
	{
		return resolvable;
	}
}
