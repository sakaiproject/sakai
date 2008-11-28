package org.sakaiproject.sitestats.tool.wicket.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
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

	/** Inject Sakai facade */
	@SpringBean
	private transient SakaiFacade		facade;

	private String						realSiteId;
	private String						siteId;

	private int							width;
	private int							height;
	private int							maxwidth;
	private int							maxheight;

	// UI Components
	private VisitsPanel					visitsPanel			= null;
	private ActivityPanel				activityPanel		= null;

	private AbstractDefaultAjaxBehavior chartSizeBehavior = null;
	
	
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
			renderAjaxBehavior();
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
		StatsUpdateManager statsUpdateManager = facade.getStatsUpdateManager();
		
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
	
	@SuppressWarnings("serial")
	private void renderAjaxBehavior() {		
		chartSizeBehavior = new AbstractDefaultAjaxBehavior() {
			@Override
			protected void respond(AjaxRequestTarget target) {
				// get chart size
		    	Request req = RequestCycle.get().getRequest();
				try{
					width = (int) Float.parseFloat(req.getParameter("width"));					
				}catch(NumberFormatException e){
					e.printStackTrace();
					width = 400;
				}
				try{
					height = (int) Float.parseFloat(req.getParameter("height"));
				}catch(NumberFormatException e){
					e.printStackTrace();
					height = 200;
				}
				try{
					maxwidth = (int) Float.parseFloat(req.getParameter("maxwidth"));
				}catch(NumberFormatException e){
					e.printStackTrace();
					maxwidth = 640;
				}
				try{
					maxheight = (int) Float.parseFloat(req.getParameter("maxheight"));
				}catch(NumberFormatException e){
					e.printStackTrace();
					maxheight = 300;
				}
				
				// append callbacks to draw charts, sequentially
				if(visitsPanel != null) {
					visitsPanel.setChartSize(width, height, maxwidth, maxheight);
					CharSequence onSuccess = null;
					if(activityPanel != null){
						activityPanel.setChartSize(width, height, maxwidth, maxheight);
						onSuccess = buildCallbackScript(activityPanel.getChartCallbackUrl(), null);
					}
					String callbackScript = buildCallbackScript(visitsPanel.getChartCallbackUrl(), onSuccess);
					target.appendJavascript(callbackScript);
				}else if(activityPanel != null){
					activityPanel.setChartSize(width, height, maxwidth, maxheight);
					target.appendJavascript(buildCallbackScript(activityPanel.getChartCallbackUrl(), null));
				}
			}
			
			private String buildCallbackScript(CharSequence callbackUrl, CharSequence onSuccessCallbackScript) {
				StringBuilder script = new StringBuilder();
				script.append("wicketAjaxGet('");
				script.append(callbackUrl);
				script.append("', function() {");
				if(onSuccessCallbackScript != null) {
					script.append("setTimeout(\"");
					script.append(onSuccessCallbackScript);
					script.append("\",500)");
				}
				script.append("}, function() {});");
				return script.toString();
			}
		};
		add(chartSizeBehavior);
		
		WebMarkupContainer js = new WebMarkupContainer("jsWicketChartSize");
		js.setOutputMarkupId(true);
		add(js);
		WebMarkupContainer jsCall = new WebMarkupContainer("jsWicketChartSizeCall") {
			@Override
			protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
				StringBuilder buff = new StringBuilder();
				buff.append("jQuery(document).ready(function() {");
				buff.append("  var chartSizeCallback = '" + chartSizeBehavior.getCallbackUrl() + "'; ");
				buff.append("  setWicketChartSize(chartSizeCallback);");
				buff.append("});");
				replaceComponentTagBody(markupStream, openTag, buff.toString());
			}	
		};
		jsCall.setOutputMarkupId(true);
		add(jsCall);
	}
}

