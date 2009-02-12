package org.sakaiproject.sitestats.tool.wicket.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
import org.sakaiproject.sitestats.tool.wicket.components.LastJobRun;
import org.sakaiproject.sitestats.tool.wicket.components.Menus;
import org.sakaiproject.sitestats.tool.wicket.widget.ActivityWidget;
import org.sakaiproject.sitestats.tool.wicket.widget.ResourcesWidget;
import org.sakaiproject.sitestats.tool.wicket.widget.VisitsWidget;

/**
 * @author Nuno Fernandes
 */
public class OverviewPage2 extends BasePage {
	private static final long			serialVersionUID	= 1L;

	/** Inject Sakai facade */
	@SpringBean
	private transient SakaiFacade		facade;

	private String						realSiteId;
	private String						siteId;
	
	
	public OverviewPage2() {
		this(null);
	}

	public OverviewPage2(PageParameters pageParameters) {
		realSiteId = facade.getToolManager().getCurrentPlacement().getContext();
		if(pageParameters != null) {
			siteId = pageParameters.getString("siteId");
		}
		if(siteId == null){
			siteId = realSiteId;
		}
		boolean allowed = facade.getStatsAuthz().isUserAbleToViewSiteStats(siteId);
		if(allowed) {
			renderBody();
		}else{
			setResponsePage(NotAuthorizedPage.class);
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		//response.renderJavascriptReference("/library/js/jquery.js");
		response.renderJavascriptReference("/sakai-sitestats-tool/script/jquery-1.3.1.min.js");
		response.renderJavascriptReference("/sakai-sitestats-tool/script/jquery.ifixpng2.js");
		StringBuilder onDomReady = new StringBuilder();
		onDomReady.append("jQuery.ifixpng('/sakai-sitestats-tool/images/transparent.gif');");
		response.renderOnDomReadyJavascript(onDomReady.toString());
		super.renderHead(response);
	}
	
	private void renderBody() {
		setRenderBodyOnly(true);
		add(new Menus("menu", siteId));
		
		// SiteStats services
		StatsManager statsManager = facade.getStatsManager();
		
		// Last job run
		add(new LastJobRun("lastJobRun", siteId));
		
		// Widgets ----------------------------------------------------
		
		// Visits
		boolean visitsVisible = statsManager.isEnableSiteVisits() && statsManager.isVisitsInfoAvailable();
		if(visitsVisible) {
			add(new VisitsWidget("visitsWidget", siteId));
		}else{
			add(new WebMarkupContainer("visitsWidget").setRenderBodyOnly(true));
		}
		
		// Activity
		boolean activityVisible = statsManager.isEnableSiteActivity();
		if(activityVisible) {
			add(new ActivityWidget("activityWidget", siteId));
		}else{
			add(new WebMarkupContainer("activityWidget").setRenderBodyOnly(true));
		}
		
		// Resources
		boolean resourcesVisible = false;
		try{
			resourcesVisible = (facade.getSiteService().getSite(siteId).getToolForCommonId(StatsManager.RESOURCES_TOOLID) != null);
		}catch(Exception e) {
			resourcesVisible = false;
		}
		if(resourcesVisible) {
			add(new ResourcesWidget("resourcesWidget", siteId));
		}else{
			add(new WebMarkupContainer("resourcesWidget").setRenderBodyOnly(true));
		}
	}
}

