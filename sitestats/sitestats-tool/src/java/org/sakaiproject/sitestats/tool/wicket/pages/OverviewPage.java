package org.sakaiproject.sitestats.tool.wicket.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.StatsUpdateManager;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
import org.sakaiproject.sitestats.tool.wicket.components.LastJobRun;
import org.sakaiproject.sitestats.tool.wicket.components.Menu;
import org.sakaiproject.sitestats.tool.wicket.components.Menus;
import org.sakaiproject.sitestats.tool.wicket.panels.ActivityPanel;
import org.sakaiproject.sitestats.tool.wicket.panels.VisitsPanel;

/**
 * @author Nuno Fernandes
 */
public class OverviewPage extends BasePage {
	private static final long			serialVersionUID	= 1L;

	/** Inject Sakai facade */
	@SpringBean
	private transient SakaiFacade		facade;

	private String						realSiteId;
	private String						siteId;

	// UI Components
	private VisitsPanel					visitsPanel			= null;
	private ActivityPanel				activityPanel		= null;
	
	
	public OverviewPage() {
		this(null);
	}

	public OverviewPage(PageParameters pageParameters) {
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
		response.renderJavascriptReference("/library/js/jquery.js");
		response.renderJavascriptReference("/sakai-sitestats-tool/script/common.js");
		super.renderHead(response);
	}
	
	private void renderBody() {
		add(new Menus("menu", siteId));
		
		// SiteStats services
		StatsManager statsManager = facade.getStatsManager();
		
		// Last job run
		add(new LastJobRun("lastJobRun", siteId));
		
		// Visits
		final WebMarkupContainer visits = new WebMarkupContainer("visits");
		boolean visitsVisible = statsManager.isEnableSiteVisits() && statsManager.isVisitsInfoAvailable();
		visits.setVisible(visitsVisible);
		if(visitsVisible) {
			visitsPanel = new VisitsPanel("visitsPanel", siteId);
			visits.add(visitsPanel);
		}else{
			WebMarkupContainer panel = new WebMarkupContainer("visitsPanel");
			visits.add(panel);
		}
		add(visits);
		
		// Activity
		final WebMarkupContainer activity = new WebMarkupContainer("activity");
		boolean activityVisible = statsManager.isEnableSiteActivity();
		activity.setVisible(activityVisible);
		if(activityVisible) {
			activityPanel = new ActivityPanel("activityPanel", siteId);
			activity.add(activityPanel);
		}else{
			WebMarkupContainer panel = new WebMarkupContainer("activityPanel");
			activity.add(panel);
		}
		add(activity);	
	}
}

