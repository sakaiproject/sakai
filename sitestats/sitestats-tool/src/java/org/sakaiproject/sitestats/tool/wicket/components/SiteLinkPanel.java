/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.sitestats.tool.wicket.pages.OverviewPage;


/**
 * @author Nuno Fernandes
 */
public class SiteLinkPanel extends Panel {
	private static final long	serialVersionUID	= 1L;

	public SiteLinkPanel(String id, IModel model) {
		super(id);
		final String siteId = ((Site) model.getObject()).getId();
		final String siteTitle = ((Site) model.getObject()).getTitle();
		PageParameters param = new PageParameters().set("siteId", siteId);
		BookmarkablePageLink link = new BookmarkablePageLink("link", OverviewPage.class, param);
		link.add(new Label("label", siteTitle));
		add(link);
	}
}
