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
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.sakaiproject.sitestats.tool.facade.Locator;

public class Menus extends Panel {
	private static final long		serialVersionUID	= 1L;

	private String					siteId;
	private String					realSiteId;

	public Menus(String id) {
		this(id, null);
	}
	
	public Menus(String id, String siteId) {
		super(id);
		
		// site Id
		realSiteId = Locator.getFacade().getToolManager().getCurrentPlacement().getContext();
		if(siteId != null) {
			this.siteId = siteId;
		}else{
			this.siteId = realSiteId;
		}
		
		renderBody();
	}
	
	/** Render Sakai Menu. */
	@SuppressWarnings("unchecked")
	private void renderBody() {
		setRenderBodyOnly(true);
		
		boolean isSiteStatsAdminPage = Locator.getFacade().getStatsAuthz().isSiteStatsAdminPage();
		boolean isBrowsingThisSite = siteId.equals(realSiteId);
		
		// admin menu
		AdminMenu adminMenu = new AdminMenu("adminMenu");
		add(adminMenu);
		
		// standard menu
		WebMarkupContainer standardMenuContainer = new WebMarkupContainer("standardMenuContainer");
		// menu
		Menu standardMenu = new Menu("standardMenu", siteId);
		standardMenuContainer.add(standardMenu);
		add(standardMenuContainer);
		
		// menus rendering
		if(isSiteStatsAdminPage) {
			adminMenu.setVisible(true);
			if(!isBrowsingThisSite) {
				standardMenuContainer.setVisible(true);
				standardMenuContainer.add(new AttributeModifier("style", new Model("margin: 10px 5px 5px 5px;")));
			}else{
				standardMenuContainer.setVisible(false);
			}
		}else{
			adminMenu.setVisible(false);
			standardMenuContainer.setVisible(true);
		}
	}
}
