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
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.sitestats.tool.wicket.pages.AdminPage;
import org.sakaiproject.sitestats.tool.wicket.pages.AdminReportsPage;
import org.sakaiproject.sitestats.tool.wicket.pages.ServerWidePage;


/**
 * @author Nuno Fernandes
 */
public class AdminMenu extends Panel {
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor.
	 * @param id The wicket:id
	 */
	public AdminMenu(String id) {
		super(id);
		setRenderBodyOnly(true);
		renderBody();
	}
	
	/**
	 * Render Sakai Menu
	 */
	@SuppressWarnings("unchecked")
	private void renderBody() {
		// site id
		String siteId = Locator.getFacade().getToolManager().getCurrentPlacement().getContext();
		PageParameters pageParameters = new PageParameters().set("siteId", siteId);
				
		// --------- ADMIN SECTION ---------
		
		// Admin page
		boolean adminPageVisible = 
			Locator.getFacade().getStatsAuthz().isUserAbleToViewSiteStatsAdmin(siteId);
		MenuItem adminPage = new MenuItem("adminPage", new ResourceModel("menu_sitelist"), AdminPage.class, pageParameters, true);
		adminPage.setVisible(adminPageVisible);
		add(adminPage);
		
		// Admin reports
		boolean reportsPageVisible = 
			Locator.getFacade().getStatsAuthz().isUserAbleToViewSiteStatsAdmin(siteId);
		MenuItem reportsPage = new MenuItem("reportsPage", new ResourceModel("menu_adminreports"), AdminReportsPage.class, pageParameters, false);
		reportsPage.setVisible(reportsPageVisible);
		add(reportsPage);
		
		// Admin ServerWide page
		boolean serverWidePageVisible = 
			Locator.getFacade().getStatsManager().isServerWideStatsEnabled();
		MenuItem serverWidePage = new MenuItem("serverWidePage", new ResourceModel("menu_serverwide"), ServerWidePage.class, pageParameters, false);
		serverWidePage.setVisible(serverWidePageVisible);
		add(serverWidePage);		
	}

	/* (non-Javadoc)
	 * @see org.apache.wicket.markup.html.panel.Panel#onComponentTag(org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		checkComponentTag(tag, "menu");
	}

}
