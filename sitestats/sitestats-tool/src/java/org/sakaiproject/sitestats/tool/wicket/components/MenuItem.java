package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;


/**
 * @author Nuno Fernandes
 */
public class MenuItem extends Panel {
	private static final long	serialVersionUID	= 1L;

	public MenuItem(String id, IModel itemText, Class itemPageClass, PageParameters pageParameters, boolean first) {
		super(id);

		// link version
		WebMarkupContainer menuItemLinkHolder = new WebMarkupContainer("menuItemLinkHolder");
		final BookmarkablePageLink menuItemLink = new BookmarkablePageLink("menuItemLink", itemPageClass, pageParameters);
		final Label menuLinkText = new Label("menuLinkText", itemText);
		menuLinkText.setRenderBodyOnly(true);
		menuItemLink.add(menuLinkText);
		menuItemLinkHolder.add(menuItemLink);
		add(menuItemLinkHolder);

		// span version
		final Label menuItemLabel = new Label("menuItemLabel", itemText);
		menuItemLabel.setRenderBodyOnly(true);
		add(menuItemLabel);
		
		// determine current page
		Class currentPageClass = getRequestCycle().getResponsePageClass();
		if(itemPageClass.equals(currentPageClass)) {
			if(first) {
				menuItemLinkHolder.setVisible(false);
			}else{
				menuItemLink.setVisible(false);
			}
			menuItemLabel.setVisible(true);
		}else{
			menuItemLinkHolder.setVisible(true);
			menuItemLabel.setVisible(false);
		}
		
		if(first) {
			add(new AttributeModifier("class", true, new Model("firstToolBarItem")));
		}
	}
}
