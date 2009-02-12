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
	private static final long		serialVersionUID	= 1L;
	private boolean					first				= false;
	private Class					itemPageClass		= null;
	private WebMarkupContainer		menuItemLinkHolder;
	private BookmarkablePageLink	menuItemLink;
	private Label					menuLinkText;
	private Label					menuItemLabel;

	public MenuItem(String id, IModel itemText, Class itemPageClass, PageParameters pageParameters, boolean first) {
		super(id);
		this.first = first;
		this.itemPageClass = itemPageClass;

		// link version
		menuItemLinkHolder = new WebMarkupContainer("menuItemLinkHolder");
		menuItemLink = new BookmarkablePageLink("menuItemLink", itemPageClass, pageParameters);
		menuLinkText = new Label("menuLinkText", itemText);
		menuLinkText.setRenderBodyOnly(true);
		menuItemLink.add(menuLinkText);
		menuItemLinkHolder.add(menuItemLink);
		add(menuItemLinkHolder);

		// span version
		menuItemLabel = new Label("menuItemLabel", itemText);
		menuItemLabel.setRenderBodyOnly(true);
		add(menuItemLabel);
		
		if(first) {
			add(new AttributeModifier("class", true, new Model("firstToolBarItem")));
		}
	}
	
	@Override
	protected void onBeforeRender() {
		Class currentPageClass = getPage().getClass();
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
		super.onBeforeRender();
	}
}
