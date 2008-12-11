package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
import org.sakaiproject.sitestats.tool.wicket.pages.AdminPage;
import org.sakaiproject.sitestats.tool.wicket.pages.OverviewPage;
import org.sakaiproject.sitestats.tool.wicket.pages.PreferencesPage;
import org.sakaiproject.sitestats.tool.wicket.pages.ReportsPage;


/**
 * @author Nuno Fernandes
 */
public class Menu extends Panel {
	private static final long	serialVersionUID	= 1L;

	@SpringBean
	private transient SakaiFacade facade;

	/**
	 * Default constructor.
	 * @param id The wicket:id
	 */
	public Menu(String id) {
		super(id);
		setRenderBodyOnly(true);
		renderBody();
	}

	/**
	 * Default constructor.
	 * @param id The wicket:id
	 */
	public Menu(String id, String siteId) {
		super(id);
		setRenderBodyOnly(true);
		renderBody(siteId);
	}
	
	/**
	 * Render Sakai Menu for current site
	 */
	@SuppressWarnings("unchecked")
	private void renderBody() {
		renderBody(facade.getToolManager().getCurrentPlacement().getContext());
	}
	
	/**
	 * Render Sakai Menu for specified site id
	 */
	@SuppressWarnings("unchecked")
	private void renderBody(String siteId) {
		// current page
		Class currentPageClass = getRequestCycle().getResponsePageClass();
		PageParameters pageParameters = new PageParameters();
		if(siteId != null) {
			pageParameters.put("siteId", siteId);
		}
		String realSiteId = facade.getToolManager().getCurrentPlacement().getContext();
		boolean isSiteStatsAdminPage = facade.getStatsAuthz().isSiteStatsAdminPage();
		boolean isBrowsingThisSite = siteId != null && siteId.equals(realSiteId);
		
		// Site display
		String siteTitle = null;
		try{
			siteTitle = facade.getSiteService().getSite(siteId).getTitle();
		}catch(IdUnusedException e){
			siteTitle = siteId;
		}
		Label siteDisplay = new Label("siteDisplay", siteTitle);
		boolean siteDisplayVisible = isSiteStatsAdminPage && !isBrowsingThisSite; 
		siteDisplay.setVisible(siteDisplayVisible);
		add(siteDisplay);
		
		// Admin page
		/*boolean adminPageVisible = 
			facade.getStatsAuthz().isUserAbleToViewSiteStatsAdmin(realSiteId)
			&& facade.getStatsAuthz().isSiteStatsAdminPage();
		MenuItem adminPage = new MenuItem("adminPage", new ResourceModel("menu_sitelist"), AdminPage.class, pageParameters, adminPageVisible);
		adminPage.setVisible(adminPageVisible);
		adminPage.add(new AttributeModifier("class", true, new Model("firstToolBarItem")));
		add(adminPage);*/

		// Overview
		boolean overviewVisible = 
			!AdminPage.class.equals(currentPageClass)		
			&&
			(facade.getStatsManager().isEnableSiteVisits() || facade.getStatsManager().isEnableSiteActivity());
		MenuItem overview = new MenuItem("overview", new ResourceModel("menu_overview"), OverviewPage.class, pageParameters, !siteDisplayVisible /*overviewVisible && !adminPageVisible*/);
		overview.setVisible(overviewVisible);
		add(overview);

		// Reports
		MenuItem reports = new MenuItem("reports", new ResourceModel("menu_reports"), ReportsPage.class, pageParameters, false);
		if(!overviewVisible) {
			reports.add(new AttributeModifier("class", true, new Model("firstToolBarItem")));
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
