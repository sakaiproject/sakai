package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.sitestats.tool.wicket.pages.OverviewPage2;


/**
 * @author Nuno Fernandes
 */
public class SiteLinkPanel extends Panel {
	private static final long	serialVersionUID	= 1L;

	public SiteLinkPanel(String id, IModel model) {
		super(id);
		final String siteId = ((Site) model.getObject()).getId();
		final String siteTitle = ((Site) model.getObject()).getTitle();
		PageParameters param = new PageParameters("siteId=" + siteId);
		BookmarkablePageLink link = new BookmarkablePageLink("link", OverviewPage2.class, param);
		link.add(new Label("label", siteTitle));
		add(link);
	}
}
