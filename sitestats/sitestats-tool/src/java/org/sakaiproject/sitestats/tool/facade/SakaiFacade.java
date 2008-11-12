package org.sakaiproject.sitestats.tool.facade;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.ServerWideReportManager;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.StatsUpdateManager;
import org.sakaiproject.sitestats.api.chart.ChartService;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;


public interface SakaiFacade {

	// SiteStats services
	
	public StatsManager getStatsManager();
	public StatsAuthz getStatsAuthz();
	public EventRegistryService getEventRegistryService();
	public ReportManager getReportManager();
	public ChartService getChartService();
	public StatsUpdateManager getStatsUpdateManager();
	public ServerWideReportManager getServerWideReportManager();

	// Sakai services
	
	public SessionManager getSessionManager();
	public ToolManager getToolManager();
	public SiteService getSiteService();
	public AuthzGroupService getAuthzGroupService();
	public UserDirectoryService getUserDirectoryService();
	public ContentHostingService getContentHostingService();
	public TimeService getTimeService();
	
	
}
