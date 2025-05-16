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
package org.sakaiproject.sitestats.tool.wicket.components.useractivity;

import java.util.List;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.Model;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;
import org.sakaiproject.sitestats.api.event.detailed.EventDetail;
import org.sakaiproject.sitestats.api.event.detailed.EventDetail.DetailType;
import org.sakaiproject.sitestats.tool.transformers.ResolvedRefTransformer;
import org.sakaiproject.sitestats.tool.wicket.models.LoadableEventRefDetailsModel;

/**
 * Panel presenting the details for a given event as a list of key/value pairs
 * @author plukasew, bjones86
 */
public class EventRefDetailsPanel extends GenericPanel<ResolvedEventData>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * @param id wicket id
	 * @param eventType the event type
	 * @param eventRef the event reference
	 * @param siteID site id
	 */
	public EventRefDetailsPanel(String id, String eventType, String eventRef, String siteID)
	{
		super(id, new LoadableEventRefDetailsModel(eventType, eventRef, siteID));
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		List<EventDetail> detailsList = getDetails();
		add(new ListView<EventDetail>("detailsList", detailsList)
		{
			@Override
			protected void populateItem(ListItem<EventDetail> item)
			{
				EventDetail ref = item.getModelObject();
				item.add(new Label("key", Model.of(ref.getKey())).setRenderBodyOnly(true));
				Fragment frag = buildDetailsFragment(ref);
				item.add(frag);
			}
		});
	}

	private List<EventDetail> getDetails()
	{
		return ResolvedRefTransformer.transform(getModelObject());
	}

	private Fragment buildDetailsFragment(EventDetail rr)
	{
		Fragment frag = null;
		if (DetailType.TEXT.equals(rr.getType()))
		{
			frag = new Fragment("details", "text", this);
			Label label = new Label("displayValue", rr.getDisplayValue());
			label.setEscapeModelStrings(false); // Don't escape HTML in the displayValue
			label.setRenderBodyOnly(true);
			frag.add(label);
		}
		else if (DetailType.LINK.equals(rr.getType()))
		{
			frag = new Fragment("details", "link", this);
			ExternalLink displayLink = new ExternalLink("displayLink", rr.getUrl(), rr.getDisplayValue());
			displayLink.setEscapeModelStrings(false); // Don't escape HTML in the displayValue
			displayLink.add(new AttributeModifier("target", "_blank"));
			displayLink.add(new AttributeModifier("rel", "noreferrer"));
			frag.add(displayLink);
		}

		return frag;
	}
}
