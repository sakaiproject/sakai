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
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.sitestats.tool.wicket.pages.AdminPage;
import org.sakaiproject.sitestats.tool.wicket.pages.OverviewPage;
import org.sakaiproject.sitestats.tool.wicket.pages.PreferencesPage;
import org.sakaiproject.sitestats.tool.wicket.pages.ReportsPage;


/**
 * @author Nuno Fernandes
 */
public class Menu extends Panel {
	private static final long	serialVersionUID	= 1L;

	private String siteId;
	/**
	 * Default constructor.
	 * @param id The wicket:id
	 */
	public Menu(String id) {
		super(id);
		setRenderBodyOnly(true);
		siteId = Locator.getFacade().getToolManager().getCurrentPlacement().getContext();
	}

	/**
	 * Default constructor.
	 * @param id The wicket:id
	 */
	public Menu(String id, String siteId) {
		super(id);
		setRenderBodyOnly(true);
		this.siteId = siteId;
	}
	
	/**
	 * Render Sakai Menu
	 */
	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		// current page
		Class currentPageClass = getPage().getClass();
		PageParameters pageParameters = new PageParameters();
		if(siteId != null) {
			pageParameters.set("siteId", siteId);
		}
		String realSiteId = Locator.getFacade().getToolManager().getCurrentPlacement().getContext();
		boolean isSiteStatsAdminPage = Locator.getFacade().getStatsAuthz().isSiteStatsAdminPage();
		boolean isBrowsingThisSite = siteId != null && siteId.equals(realSiteId);
		
		// Site display
		String siteTitle = null;
		try{
			siteTitle = Locator.getFacade().getSiteService().getSite(siteId).getTitle();
		}catch(IdUnusedException e){
			siteTitle = siteId;
		}
		Label siteDisplay = new Label("siteDisplay", siteTitle);
		boolean siteDisplayVisible = isSiteStatsAdminPage && !isBrowsingThisSite; 
		siteDisplay.setVisible(siteDisplayVisible);
		add(siteDisplay);
		
		// Admin page
		/*boolean adminPageVisible = 
			Locator.getFacade().getStatsAuthz().isUserAbleToViewSiteStatsAdmin(realSiteId)
			&& Locator.getFacade().getStatsAuthz().isSiteStatsAdminPage();
		MenuItem adminPage = new MenuItem("adminPage", new ResourceModel("menu_sitelist"), AdminPage.class, pageParameters, adminPageVisible);
		adminPage.setVisible(adminPageVisible);
		adminPage.add(new AttributeModifier("class", true, new Model("firstToolBarItem")));
		add(adminPage);*/

		// Overview
		boolean overviewVisible = 
			!AdminPage.class.equals(currentPageClass)		
			&&
			(Locator.getFacade().getStatsManager().isEnableSiteVisits() || Locator.getFacade().getStatsManager().isEnableSiteActivity());
		MenuItem overview = new MenuItem("overview", new ResourceModel("menu_overview"), OverviewPage.class, pageParameters, !siteDisplayVisible /*overviewVisible && !adminPageVisible*/);
		overview.setVisible(overviewVisible);
		add(overview);

		// Reports
		MenuItem reports = new MenuItem("reports", new ResourceModel("menu_reports"), ReportsPage.class, pageParameters, false);
		if(!overviewVisible) {
			reports.add(new AttributeModifier("class", new Model("firstToolBarItem")));
		}
		add(reports);

		// Preferences
		MenuItem preferences = new MenuItem("preferences", new ResourceModel("menu_prefs"), PreferencesPage.class, pageParameters, false);
		add(preferences);
		
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
