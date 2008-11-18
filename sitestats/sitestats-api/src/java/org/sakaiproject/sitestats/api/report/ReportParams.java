package org.sakaiproject.sitestats.api.report;

import java.util.Date;
import java.util.List;

import org.sakaiproject.sitestats.api.StatsManager;


public interface ReportParams {
	
	/** Get the site id. */
	public String getSiteId();
	
	/** Set the site id. */
	public void setSiteId(String siteId);

	/** Get the what type selection (see {@link ReportManager#WHAT_EVENTS},{@link ReportManager#WHAT_RESOURCES}, {@link ReportManager#WHAT_VISITS}). */
	public String getWhat();

	/** Set the what type selection (see {@link ReportManager#WHAT_EVENTS},{@link ReportManager#WHAT_RESOURCES}, {@link ReportManager#WHAT_VISITS}). */
	public void setWhat(String what);
	
	/** Get the what type selection (see {@link ReportManager#WHAT_EVENTS_BYEVENTS},{@link ReportManager#WHAT_EVENTS_BYTOOL}). */
	public String getWhatEventSelType();

	/** Set the what type selection (see {@link ReportManager#WHAT_EVENTS_BYEVENTS},{@link ReportManager#WHAT_EVENTS_BYTOOL}). */
	public void setWhatEventSelType(String whatEventSelType);

	/** Get the event ids to report against. */
	public List<String> getWhatEventIds();

	/** Set the event ids to report against. */
	public void setWhatEventIds(List<String> whatEventIds);

	/** Set if reporting will be restricted to specified resource ids in {@link #setWhatResourceIds(List)}. */
	public void setWhatLimitedResourceIds(boolean whatLimitedResourceIds);
	
	/** Check if reporting will be restricted to specified resource ids in {@link #setWhatResourceIds(List)}. */
	public boolean isWhatLimitedResourceIds();
	
	/** Get the resource ids to report against. */
	public List<String> getWhatResourceIds();

	/** Set the event ids to report against. */
	public void setWhatResourceIds(List<String> whatResourceIds);

	/** Get the resource action to limit report to (see {@link ReportManager#WHAT_RESOURCES_ACTION_NEW}, {@link ReportManager#WHAT_RESOURCES_ACTION_READ}, {@link ReportManager#WHAT_RESOURCES_ACTION_REVS}, {@link ReportManager#WHAT_RESOURCES_ACTION_DEL}). */
	public String getWhatResourceAction();

	/** Set the resource action to limit report to (see {@link ReportManager#WHAT_RESOURCES_ACTION_NEW}, {@link ReportManager#WHAT_RESOURCES_ACTION_READ}, {@link ReportManager#WHAT_RESOURCES_ACTION_REVS}, {@link ReportManager#WHAT_RESOURCES_ACTION_DEL}). */
	public void setWhatResourceAction(String whatResourceAction);

	/** Get the well-know tool ids to report against. */
	public List<String> getWhatToolIds();

	/** Set the well-know tool ids to report against. */
	public void setWhatToolIds(List<String> whatToolIds);

	/** Get the what type selection (see {@link ReportManager#WHEN_ALL}, {@link ReportManager#WHEN_LAST30DAYS}, {@link ReportManager#WHEN_LAST7DAYS}, {@link ReportManager#WHEN_CUSTOM}). */
	public String getWhen();

	/** Set the what type selection (see {@link ReportManager#WHEN_ALL}, {@link ReportManager#WHEN_LAST30DAYS}, {@link ReportManager#WHEN_LAST7DAYS}, {@link ReportManager#WHEN_CUSTOM}). */
	public void setWhen(String when);

	/** Get the start date of report data. */
	public Date getWhenTo();

	/** Set the start date of report data. */
	public void setWhenTo(Date whenTo);

	/** Get the end date of report data. */
	public Date getWhenFrom();

	/** Set the end date of report data. */
	public void setWhenFrom(Date whenFrom);

	/** Get the who type selection (see {@link ReportManager#WHO_ALL}, {@link ReportManager#WHO_ROLE}, {@link ReportManager#WHO_GROUPS}, {@link ReportManager#WHO_CUSTOM}, {@link ReportManager#WHO_NONE}). */
	public String getWho();

	/** Set the who type selection (see {@link ReportManager#WHO_ALL}, {@link ReportManager#WHO_ROLE}, {@link ReportManager#WHO_GROUPS}, {@link ReportManager#WHO_CUSTOM}, {@link ReportManager#WHO_NONE}). */
	public void setWho(String who);

	/** Get the group ids to report against. */
	public String getWhoGroupId();

	/** SGet the group ids to report against. */
	public void setWhoGroupId(String whoGroupId);

	/** Get the role ids to report against. */
	public String getWhoRoleId();

	/** Set the role ids to report against. */
	public void setWhoRoleId(String whoRoleId);

	/** Get the user ids to report against. */
	public List<String> getWhoUserIds();

	/** Set the user ids to report against. */
	public void setWhoUserIds(List<String> whoUserIds);
	
	/** Set how type, show totals by (see {@link StatsManager#TOTALSBY_EVENT_DEFAULT}, {@link StatsManager##T_USER}, {@link StatsManager##T_EVENT}, {@link StatsManager##T_DATE}, {@link StatsManager##T_LASTDATE}). */
	public void setHowTotalsBy(List<String> totalsBy); 
	
	/** get how type, show totals by (see {@link StatsManager#TOTALSBY_EVENT_DEFAULT}, {@link StatsManager##T_USER}, {@link StatsManager##T_EVENT}, {@link StatsManager##T_DATE}, {@link StatsManager##T_LASTDATE}). */
	public List<String> getHowTotalsBy();

}