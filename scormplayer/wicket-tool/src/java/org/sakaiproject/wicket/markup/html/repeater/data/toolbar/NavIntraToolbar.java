/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.wicket.markup.html.repeater.data.toolbar;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.sakaiproject.wicket.markup.html.link.NavIntraLink;

public class NavIntraToolbar extends Panel {

	private static final long serialVersionUID = 1L;
	
	protected final RepeatingView items;

	public NavIntraToolbar(String id) {
		super(id);
		
		add(items = new RepeatingView("items"));
	}
	
	public void addLink(Link link) {
		addLink(link, items);
	}
	
	public void addLink(IModel model, Class<?> pageClass) {
		addLink(new NavIntraLink("link", model, pageClass));
	}
	
	/*
	 * Copied the basic organization of this method from an Apache Wicket class
	 * 	org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable
	 * originally authored by Igor Vaynberg (ivaynberg)
	 */
	private void addLink(Link link, RepeatingView container)
	{
		if (link == null)
		{
			throw new IllegalArgumentException("argument [link] cannot be null");
		}

		if (!link.getId().equals("link"))
		{
			throw new IllegalArgumentException(
					"Link must have component id equal to 'link'");
		}

		link.setRenderBodyOnly(true);

		// create a container item for the toolbar (required by repeating view)
		WebMarkupContainer item = new WebMarkupContainer(container.newChildId());
		item.setRenderBodyOnly(true);
		item.add(link);

		container.add(item);
	}
	
}
