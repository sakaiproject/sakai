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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.request.mapper.parameter.PageParameters;
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

	private WebMarkupContainer		menuItemLinkHolder;
	private BookmarkablePageLink	menuItemLink;
	private Label					menuLinkText;
	private Label					menuItemLabel;

	public MenuItem(String id, IModel itemText, Class itemPageClass, PageParameters pageParameters, boolean first) {
		super(id);

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
			add(new AttributeModifier("class", new Model("firstToolBarItem")));
		}
	}
	
	@Override
	protected void onBeforeRender() {

		menuItemLinkHolder.setVisible(true);
		menuItemLabel.setVisible(false);
		super.onBeforeRender();
	}
}
