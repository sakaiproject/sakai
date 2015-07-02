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
package org.sakaiproject.sitestats.api.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.sakaiproject.sitestats.api.StatsManager;


public class ReportParams implements Serializable {
	private static final long	serialVersionUID		= 1L;
	private String				siteId;
	private String				what					= ReportManager.WHAT_VISITS;
	private String				whatEventSelType		= ReportManager.WHAT_EVENTS_BYTOOL;
	private List<String>		whatToolIds				= new ArrayList<String>();
	private List<String>		whatEventIds			= new ArrayList<String>();
	private boolean				whatLimitedAction		= false;
	private boolean				whatLimitedResourceIds	= false;
	private List<String>		whatResourceIds			= new ArrayList<String>();
	private String				whatResourceAction		= ReportManager.WHAT_RESOURCES_ACTION_NEW;
	private String				when					= ReportManager.WHEN_LAST7DAYS;
	private Date				whenFrom;
	private Date				whenTo;
	private String				who						= ReportManager.WHO_ALL;
	private String				whoRoleId;
	private String				whoGroupId;
	private List<String>		whoUserIds				= new ArrayList<String>();
	private List<String>		howTotalsBy				= new ArrayList<String>();
	private boolean				howSort					= false;
	private String				howSortBy				= ReportManager.HOW_SORT_DEFAULT;	
	private boolean				howSortAscending		= true;
	public boolean				howLimitedMaxResults	= false;
	private Integer				howMaxResults			= 0;
	private String				howPresentationMode		= ReportManager.HOW_PRESENTATION_TABLE;
	private String				howChartType			= StatsManager.CHARTTYPE_BAR;
	private String				howChartSource			= StatsManager.T_EVENT;
	private String				howChartCategorySource	= StatsManager.T_NONE;
	private String				howChartSeriesSource	= StatsManager.T_TOTAL;
	private String				howChartSeriesPeriod	= StatsManager.CHARTTIMESERIES_DAY;
	
	
	public ReportParams(){		
	}
	
	public ReportParams(String siteId){
		this.siteId = siteId;
		whatToolIds.add(ReportManager.WHAT_EVENTS_ALLTOOLS);
		whenFrom = new Date();
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, -1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		whenFrom = c.getTime();
		whenTo = new Date();
	}
	
	public ReportParams(String siteId, String what, List<String> whatToolIds, List<String> whatEventIds, String whatResourceAction, List<String> whatResourceIds, String when, Date whenFrom, Date whenTo, String who, String whoRoleId, String whoGroupId, List<String> whoUserIds) {
		this.siteId = siteId;
		this.what = what;
		this.whatToolIds = whatToolIds;
		this.whatEventIds = whatEventIds;
		this.whatResourceAction = whatResourceAction;
		this.whatLimitedAction = whatResourceAction != null;
		this.whatResourceIds = whatResourceIds;
		this.whatLimitedResourceIds = whatResourceIds != null;
		this.when = when;
		this.whenFrom = whenFrom;
		this.whenTo = whenTo;
		this.who = who;
		this.whoRoleId = whoRoleId;
		this.whoGroupId = whoGroupId;
		this.whoUserIds = whoUserIds;
	}
	
	/** Get the site id to report for. */
	public String getSiteId() {
		return siteId;
	}
	
	/** Set the site id to report for. */
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	/** Get the what type selection (see {@link ReportManager#WHAT_EVENTS},{@link ReportManager#WHAT_RESOURCES}, {@link ReportManager#WHAT_VISITS}, {@link ReportManager#WHAT_PRESENCES}). */
	public String getWhat() {
		return what;
	}

	/** Set the what type selection (see {@link ReportManager#WHAT_EVENTS},{@link ReportManager#WHAT_RESOURCES}, {@link ReportManager#WHAT_VISITS}, {@link ReportManager#WHAT_PRESENCES}). */
	public void setWhat(String what){
		this.what = what;
	}
	
	/** Get the what type selection (see {@link ReportManager#WHAT_EVENTS_BYEVENTS},{@link ReportManager#WHAT_EVENTS_BYTOOL}). */
	public String getWhatEventSelType() {
		return whatEventSelType;
	}

	/** Set the what type selection (see {@link ReportManager#WHAT_EVENTS_BYEVENTS},{@link ReportManager#WHAT_EVENTS_BYTOOL}). */
	public void setWhatEventSelType(String whatEventSelType) {
		this.whatEventSelType = whatEventSelType;
	}

	/** Get the event ids to report against. */
	public List<String> getWhatEventIds() {
		return whatEventIds;
	}

	/** Set the event ids to report against. */
	public void setWhatEventIds(List<String> whatEventIds) {
		this.whatEventIds = whatEventIds;
	}
	
	/** Add event id to the list to report against. */
	public void addWhatEventId(String whatEventId) {
		this.whatEventIds.add(whatEventId);
	}
	
	/** Set if is configured for reporting only on a specific resource action. */
	public void setWhatLimitedAction(boolean whatLimitedAction) {
		this.whatLimitedAction = whatLimitedAction;
	}

	/** Check if is configured for reporting only on a specific resource action. */
	public boolean isWhatLimitedAction() {
		return whatLimitedAction;
	}

	/** Set if reporting will be restricted to specified resource ids in {@link #setWhatResourceIds(List)}. */
	public void setWhatLimitedResourceIds(boolean whatLimitedResourceIds) {
		this.whatLimitedResourceIds = whatLimitedResourceIds;
	}
	
	/** Check if reporting will be restricted to specified resource ids in {@link #setWhatResourceIds(List)}. */
	public boolean isWhatLimitedResourceIds() {
		return whatLimitedResourceIds;
	}
	
	/** Get the resource ids to report against. */
	public List<String> getWhatResourceIds() {
		//if(isWhatLimitedResourceIds()) {
			return whatResourceIds;
		//}else{
		//	return new ArrayList<String>();
		//}
	}

	/** Set the event ids to report against. */
	public void setWhatResourceIds(List<String> whatResourceIds) {
		this.whatResourceIds = whatResourceIds;
	}

	/** Add the event id to the list to report against. */
	public void addWhatResourceId(String whatResourceId) {
		this.whatResourceIds.add(whatResourceId);
	}

	/** Get the resource action to limit report to (see {@link ReportManager#WHAT_RESOURCES_ACTION_NEW}, {@link ReportManager#WHAT_RESOURCES_ACTION_READ}, {@link ReportManager#WHAT_RESOURCES_ACTION_REVS}, {@link ReportManager#WHAT_RESOURCES_ACTION_DEL}). */
	public String getWhatResourceAction(){
		//if(isWhatLimitedAction()) {
			return whatResourceAction;
		//}else{
		//	return null;
		//}
	}

	/** Set the resource action to limit report to (see {@link ReportManager#WHAT_RESOURCES_ACTION_NEW}, {@link ReportManager#WHAT_RESOURCES_ACTION_READ}, {@link ReportManager#WHAT_RESOURCES_ACTION_REVS}, {@link ReportManager#WHAT_RESOURCES_ACTION_DEL}). */
	public void setWhatResourceAction(String whatResourceAction) {
		this.whatResourceAction = whatResourceAction;
	}

	/** Get the well-know tool ids to report against. */
	public List<String> getWhatToolIds() {
		return whatToolIds;
	}

	/** Set the well-know tool ids to report against. */
	public void setWhatToolIds(List<String> whatToolIds) {
		this.whatToolIds = whatToolIds;
	}

	/** Add the well-know tool id to the list to report against. */
	public void addWhatToolIds(String whatToolId) {
		this.whatToolIds.add(whatToolId);
	}

	/** Get the what type selection (see {@link ReportManager#WHEN_ALL}, {@link ReportManager#WHEN_LAST30DAYS}, {@link ReportManager#WHEN_LAST7DAYS}, {@link ReportManager#WHEN_LAST365DAYS}, {@link ReportManager#WHEN_CUSTOM}). */
	public String getWhen() {
		return when;
	}

	/** Set the what type selection (see {@link ReportManager#WHEN_ALL}, {@link ReportManager#WHEN_LAST30DAYS}, {@link ReportManager#WHEN_LAST7DAYS}, {@link ReportManager#WHEN_LAST365DAYS}, {@link ReportManager#WHEN_CUSTOM}). */
	public void setWhen(String when) {
		this.when = when;
	}

	/** Get the start date of report data. */
	public Date getWhenTo() {
		return whenTo;
	}

	/** Set the start date of report data. */
	public void setWhenTo(Date whenTo) {
		this.whenTo = whenTo;
	}

	/** Get the end date of report data. */
	public Date getWhenFrom() {
		return whenFrom;
	}

	/** Set the end date of report data. */
	public void setWhenFrom(Date whenFrom) {
		this.whenFrom = whenFrom;
	}

	/** Get the who type selection (see {@link ReportManager#WHO_ALL}, {@link ReportManager#WHO_ROLE}, {@link ReportManager#WHO_GROUPS}, {@link ReportManager#WHO_CUSTOM}, {@link ReportManager#WHO_NONE}). */
	public String getWho() {
		return who;
	}

	/** Set the who type selection (see {@link ReportManager#WHO_ALL}, {@link ReportManager#WHO_ROLE}, {@link ReportManager#WHO_GROUPS}, {@link ReportManager#WHO_CUSTOM}, {@link ReportManager#WHO_NONE}). */
	public void setWho(String who) {
		this.who = who;
	}

	/** Get the group ids to report against. */
	public String getWhoGroupId() {
		return whoGroupId;
	}

	/** SGet the group ids to report against. */
	public void setWhoGroupId(String whoGroupId) {
		this.whoGroupId = whoGroupId;
	}

	/** Get the role ids to report against. */
	public String getWhoRoleId() {
		return whoRoleId;
	}

	/** Set the role ids to report against. */
	public void setWhoRoleId(String whoRoleId) {
		this.whoRoleId = whoRoleId;
	}

	/** Get the user ids to report against. */
	public List<String> getWhoUserIds() {
		//if(ReportManager.WHO_CUSTOM.equals(getWho())) {
			return whoUserIds;
		//}else{
		//	return new ArrayList<String>();
		//}
	}

	/** Set the user ids to report against. */
	public void setWhoUserIds(List<String> whoUserIds) {
		this.whoUserIds = whoUserIds;
	}

	/** Add the user id to the list to report against. */
	public void addWhoUserIds(String whoUserId) {
		this.whoUserIds.add(whoUserId);
	}
	
	/** Get how type, show totals by (see {@link StatsManager#TOTALSBY_EVENT_DEFAULT}, {@link StatsManager#T_USER}, {@link StatsManager#T_EVENT}, {@link StatsManager#T_RESOURCE}, {@link StatsManager#T_RESOURCE_ACTION}, {@link StatsManager#T_DATE}, {@link StatsManager#T_LASTDATE}). */
	public List<String> getHowTotalsBy() {
		howTotalsBy = fixedHowTotalsBy(howTotalsBy);
		return howTotalsBy;
	}
	
	/** Set how type, show totals by (see {@link StatsManager#TOTALSBY_EVENT_DEFAULT}, {@link StatsManager#T_USER}, {@link StatsManager#T_EVENT}, {@link StatsManager#T_RESOURCE}, {@link StatsManager#T_RESOURCE_ACTION}, {@link StatsManager#T_DATE}, {@link StatsManager#T_LASTDATE}). */
	public void setHowTotalsBy(List<String> totalsBy) {
		this.howTotalsBy = totalsBy;
	} 
	
	/** Add a totals by item (see {@link StatsManager#TOTALSBY_EVENT_DEFAULT}, {@link StatsManager#T_USER}, {@link StatsManager#T_EVENT}, {@link StatsManager#T_RESOURCE}, {@link StatsManager#T_RESOURCE_ACTION}, {@link StatsManager#T_DATE}, {@link StatsManager#T_LASTDATE}). */
	public void addHowTotalsBy(String totalsBy) {
		this.howTotalsBy.add(totalsBy);
	}
	
	/** Check if results sorting is specified. */
	public boolean isHowSort() {
		return howSort;
	}
	
	/** Set that results sorting will be specified. */ 
	public void setHowSort(boolean sort) {
		this.howSort = sort;
	}
	
	/** Get how type, sort order (see {@link StatsManager#T_USER}, {@link StatsManager#T_EVENT}, {@link StatsManager#T_RESOURCE}, {@link StatsManager#T_RESOURCE_ACTION}, {@link StatsManager#T_DATE}, {@link StatsManager#T_LASTDATE}, {@link StatsManager#T_TOTAL}). */
	public String getHowSortBy() {
		howSortBy = fixedHowSortBy(howSortBy);
		return howSortBy;
	}
	
	/** Set how type, sort order (see {@link StatsManager#TOTALSBY_EVENT_DEFAULT}, {@link StatsManager#T_USER}, {@link StatsManager#T_EVENT}, {@link StatsManager#T_RESOURCE}, {@link StatsManager#T_RESOURCE_ACTION}, {@link StatsManager#T_DATE}, {@link StatsManager#T_LASTDATE}, {@link StatsManager#T_TOTAL). */
	public void setHowSortBy(String sortBy) {
		this.howSortBy = sortBy;
	}
	
	/** Get how type, sort ascending? */
	public boolean getHowSortAscending() {
		return howSortAscending;
	}
	
	/** Set how type, sort ascending? */
	public void setHowSortAscending(boolean sortAscending) {
		this.howSortAscending = sortAscending;
	}
	
	/** Check if results limit is set. */
	public boolean isHowLimitedMaxResults() {
		return howLimitedMaxResults;
	}
	
	/** Set that results will be limited. */ 
	public void setHowLimitedMaxResults(boolean limitedMaxResults) {
		this.howLimitedMaxResults = limitedMaxResults;
	}
	
	/** Get how type, max results (0 for no limit). */
	public Integer getHowMaxResults() {
		return howMaxResults;
	}
	
	/** Set how type, max results (0 for no limit). */
	public void setHowMaxResults(Integer maxResults) {
		
		if (maxResults == null)
		{
			howMaxResults = 0;
		}
		else
		{
			howMaxResults = maxResults;
		}
	}
	
	/** Get report presentation mode (see {@link ReportManager#HOW_PRESENTATION_TABLE}, {@link ReportManager#HOW_PRESENTATION_CHART}, {@link ReportManager#HOW_PRESENTATION_BOTH}). */
	public String getHowPresentationMode() {
		return howPresentationMode;
	}

	/** Set report presentation mode (see {@link ReportManager#HOW_PRESENTATION_TABLE}, {@link ReportManager#HOW_PRESENTATION_CHART}, {@link ReportManager#HOW_PRESENTATION_BOTH}). */
	public void setHowPresentationMode(String howPresentationMode) {
		this.howPresentationMode = howPresentationMode;
	}

	/** Get chart type (see {@link StatsManager#CHARTTYPE_BAR}, {@link StatsManager#CHARTTYPE_LINE}, {@link StatsManager#CHARTTYPE_PIE}, {@link StatsManager#CHARTTYPE_TIMESERIES}, {@link StatsManager#CHARTTYPE_TIMESERIESBAR}). */
	public String getHowChartType() {
		return howChartType;
	}

	/** Set chart type (see {@link StatsManager#CHARTTYPE_BAR}, {@link StatsManager#CHARTTYPE_LINE}, {@link StatsManager#CHARTTYPE_PIE}, {@link StatsManager#CHARTTYPE_TIMESERIES}, {@link StatsManager#CHARTTYPE_TIMESERIESBAR}). */
	public void setHowChartType(String howChartType) {
		this.howChartType = howChartType;
	}

	/** Get the chart data source (see {@link StatsManager#T_USER}, {@link StatsManager#T_EVENT}, {@link StatsManager#T_RESOURCE}, {@link StatsManager#T_RESOURCE_ACTION}, {@link StatsManager#T_DATE}, {@link StatsManager#T_LASTDATE}, {@link StatsManager#T_TOTAL}). */
	public String getHowChartSource() {
		return howChartSource;
	}

	/** Set the chart data source (see {@link StatsManager#T_USER}, {@link StatsManager#T_EVENT}, {@link StatsManager#T_RESOURCE}, {@link StatsManager#T_RESOURCE_ACTION}, {@link StatsManager#T_DATE}, {@link StatsManager#T_LASTDATE}, {@link StatsManager#T_TOTAL}). */
	public void setHowChartSource(String howChartSource) {
		this.howChartSource = howChartSource;
	}

	/** Get the chart category that will group the date source (can be null or {@link StatsManager#T_NONE}) (see {@link StatsManager#T_USER}, {@link StatsManager#T_EVENT}, {@link StatsManager#T_RESOURCE}, {@link StatsManager#T_RESOURCE_ACTION}, {@link StatsManager#T_DATE}, {@link StatsManager#T_LASTDATE}, {@link StatsManager#T_TOTAL}). */
	public String getHowChartCategorySource() {
		return howChartCategorySource;
	}

	/** Set the chart category that will group the data source (can be null or {@link StatsManager#T_NONE}) (see {@link StatsManager#T_USER}, {@link StatsManager#T_EVENT}, {@link StatsManager#T_RESOURCE}, {@link StatsManager#T_RESOURCE_ACTION}, {@link StatsManager#T_DATE}, {@link StatsManager#T_LASTDATE}, {@link StatsManager#T_TOTAL}). */
	public void setHowChartCategorySource(String howChartCategorySource) {
		this.howChartCategorySource = howChartCategorySource;
	}

	/** Get the field chart series will be generated from (only applies to TimeSeries charts) (see {@link StatsManager#T_USER}, {@link StatsManager#T_EVENT}, {@link StatsManager#T_RESOURCE}, {@link StatsManager#T_RESOURCE_ACTION}, {@link StatsManager#T_DATE}, {@link StatsManager#T_LASTDATE}, {@link StatsManager#T_TOTAL}). */
	public String getHowChartSeriesSource() {
		return howChartSeriesSource;
	}

	/** Get the field chart series will be generated from (only applies to TimeSeries charts) (see {@link StatsManager#T_USER}, {@link StatsManager#T_EVENT}, {@link StatsManager#T_RESOURCE}, {@link StatsManager#T_RESOURCE_ACTION}, {@link StatsManager#T_DATE}, {@link StatsManager#T_LASTDATE}, {@link StatsManager#T_TOTAL}). */
	public void setHowChartSeriesSource(String howChartSeriesSource) {
		this.howChartSeriesSource = howChartSeriesSource;
	}

	/** Set the date grouping type for time series chart (only applies to TimeSeries charts) (see {@link StatsManager#CHARTTIMESERIES_DAY}, {@link StatsManager#CHARTTIMESERIES_WEEKDAY}, {@link StatsManager#CHARTTIMESERIES_MONTH}, {@link StatsManager#CHARTTIMESERIES_YEAR}). */
	public void setHowChartSeriesPeriod(String howChartSeriesPeriod) {
		this.howChartSeriesPeriod = howChartSeriesPeriod;
	}

	/** Get the date grouping type for time series chart (only applies to TimeSeries charts) (see {@link StatsManager#CHARTTIMESERIES_DAY}, {@link StatsManager#CHARTTIMESERIES_WEEKDAY}, {@link StatsManager#CHARTTIMESERIES_MONTH}, {@link StatsManager#CHARTTIMESERIES_YEAR}). */
	public String getHowChartSeriesPeriod() {
		return howChartSeriesPeriod;
	}

	
	
	private List<String> fixedHowTotalsBy(List<String> list) {
		List<String> fixedList = new ArrayList<String>();
		if(list == null || list.isEmpty()) {
			// set defaults if none specified
			if(getWhat().equals(ReportManager.WHAT_RESOURCES)){
				fixedList = StatsManager.TOTALSBY_RESOURCE_DEFAULT;
			}else{
				fixedList = StatsManager.TOTALSBY_EVENT_DEFAULT;
			}
		}else{
			// remove columns that shouldn't be selected
			for(String t : list) {
				if(t.equals(StatsManager.T_EVENT) || t.equals(StatsManager.T_TOOL)) {
					if(!getWhat().equals(ReportManager.WHAT_RESOURCES)){
						fixedList.add(t);
					}
				}else if(t.equals(StatsManager.T_RESOURCE) || t.equals(StatsManager.T_RESOURCE_ACTION)) {
					if(getWhat().equals(ReportManager.WHAT_RESOURCES)){
						fixedList.add(t);
					}
				}else if(t.equals(StatsManager.T_USER)) {
					if(!getWhat().equals(ReportManager.WHAT_VISITS_TOTALS) && !getWhat().equals(ReportManager.WHAT_ACTIVITY_TOTALS)){
						fixedList.add(t);
					}
				}else if(t.equals(StatsManager.T_VISITS) || t.equals(StatsManager.T_UNIQUEVISITS)) {
					if(getWhat().equals(ReportManager.WHAT_EVENTS) || getWhat().equals(ReportManager.WHAT_VISITS_TOTALS)){
						fixedList.add(t);
					}
				}else{
					fixedList.add(t);
				}
			}
		}
		return fixedList;
	}
	
	private String fixedHowSortBy(String sort) {
		if(sort != null) {
			if(sort.equals(StatsManager.T_EVENT) || sort.equals(StatsManager.T_TOOL)) {
				if(!getWhat().equals(ReportManager.WHAT_RESOURCES)){
					return sort;
				}
			}else if(sort.equals(StatsManager.T_RESOURCE) || sort.equals(StatsManager.T_RESOURCE_ACTION)) {
				if(getWhat().equals(ReportManager.WHAT_RESOURCES)){
					return sort;
				}
			}else if(sort.equals(StatsManager.T_USER)) {
				if(!getWhat().equals(ReportManager.WHAT_VISITS_TOTALS) && !getWhat().equals(ReportManager.WHAT_ACTIVITY_TOTALS)){
					return sort;
				}
			}else if(sort.equals(StatsManager.T_VISITS) || sort.equals(StatsManager.T_UNIQUEVISITS)) {
				if(getWhat().equals(ReportManager.WHAT_EVENTS) || getWhat().equals(ReportManager.WHAT_VISITS_TOTALS)){
					return sort;
				}
			}else if(!StatsManager.T_TOTAL.equals(sort) && !getHowTotalsBy().contains(sort)) {
				return ReportManager.HOW_SORT_DEFAULT;
			}else{
				return sort;
			}
		}else{
			return ReportManager.HOW_SORT_DEFAULT;
		}
		return sort;
	}
	
	
	// ------------------------------------------------------------
	// Output functions (String, XML)
	// ------------------------------------------------------------	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append('{');	
		
		str.append(memberToString("siteId", siteId, true));
		str.append(memberToString("what", what, true));
		if(ReportManager.WHAT_EVENTS.equals(what)) {
			str.append(memberToString("whatEventSelType", whatEventSelType, true));
			if(ReportManager.WHAT_EVENTS_BYTOOL.equals(whatEventSelType)) {
				str.append(memberToString("whatToolIds", whatToolIds, true));
			}else{
				str.append(memberToString("whatEventIds", whatEventIds, true));
			}
		}else if(ReportManager.WHAT_RESOURCES.equals(what)) {
			if(whatResourceAction != null) {
				str.append(memberToString("whatResourceAction", whatResourceAction, true));
			}
			if(whatResourceIds != null) {
				str.append(memberToString("whatResourceIds", whatResourceIds, true));
			}
		}
		str.append(memberToString("when", when, true));
		if(ReportManager.WHEN_CUSTOM.equals(when)) {
			str.append(memberToString("whenFrom", whenFrom.toString(), true));
			str.append(memberToString("whenTo", whenTo.toString(), true));
		}
		str.append(memberToString("who", who, true));
		if(ReportManager.WHO_GROUPS.equals(who)) {
			str.append(memberToString("whoGroupId", whoGroupId, true));
		}
		if(ReportManager.WHO_ROLE.equals(who)) {
			str.append(memberToString("whoRoleId", whoRoleId, true));
		}
		if(ReportManager.WHO_CUSTOM.equals(who)) {
			str.append(memberToString("whoUserIds", whoUserIds, true));
		}	
		str.append(memberToString("howTotalsBy", getHowTotalsBy(), true));
		str.append(memberToString("howSortBy", getHowSortBy() +", asc? "+getHowSortAscending(), true));
		str.append(memberToString("howMaxResults", String.valueOf(getHowMaxResults()), true));
		
		String presentation = getHowPresentationMode();
		boolean hasChart = ReportManager.HOW_PRESENTATION_CHART.equals(presentation) || ReportManager.HOW_PRESENTATION_BOTH.equals(presentation);
		str.append(memberToString("howPresentationMode", getHowPresentationMode(), hasChart));
		if(hasChart) {
			str.append(memberToString("howChartType", getHowChartType(), true));
			str.append(memberToString("howChartSource", getHowChartSource(), true));
			if(StatsManager.CHARTTYPE_BAR.equals(getHowChartType())
				|| StatsManager.CHARTTYPE_LINE.equals(getHowChartType())
				|| StatsManager.CHARTTYPE_PIE.equals(getHowChartType())) {
				str.append(memberToString("howChartCategorySource", getHowChartCategorySource(), false));
			}
			if(StatsManager.CHARTTYPE_TIMESERIES.equals(getHowChartType())
					|| StatsManager.CHARTTYPE_TIMESERIESBAR.equals(getHowChartType())) {
					str.append(memberToString("howChartSeriesSource", getHowChartSeriesSource(), true));
					str.append(memberToString("howChartSeriesPeriod", getHowChartSeriesPeriod(), false));
			}
		}
		
		str.append('}');
		return str.toString();
	}
	
	private String memberToString(String member, String value, boolean hasMore) {
		StringBuilder str = new StringBuilder();
		str.append(member);
		str.append(": ");
		str.append(value);
		if(hasMore) {
			str.append(", ");
		}
		return str.toString();
	}
	
	private String memberToString(String member, List<String> values, boolean hasMore) {
		StringBuilder str = new StringBuilder();
		str.append(member);
		str.append(": ");
		str.append('[');
		boolean first = true;
		if(values != null) {
			for(String value : values) {
				if(!first) {
					str.append(", ");				
				}
				str.append(value);
				first = false;
			}
		}
		str.append(']');
		if(hasMore) {
			str.append(", ");
		}
		return str.toString();
	}
	
}