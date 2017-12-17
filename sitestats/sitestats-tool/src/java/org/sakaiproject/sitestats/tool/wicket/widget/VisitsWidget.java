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
package org.sakaiproject.sitestats.tool.wicket.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;

import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.sitestats.api.SitePresence;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.Util;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.tool.facade.Locator;

@Slf4j
public class VisitsWidget extends Panel {
	private static final long		serialVersionUID	= 1L;

	/** The site id. */
	private String					siteId				= null;
	
	private Set<String>				siteUsers			= null;
	private Set<String>				usersWithVisits		= null;
	

	/**
	 * Default constructor.
	 * @param id The wicket:id
	 * @param siteId The related site id
	 */
	public VisitsWidget(String id, final String siteId) {
		super(id);
		this.siteId = siteId;
		setRenderBodyOnly(true);
		setOutputMarkupId(true);
		
		// Single values (MiniStat)
		List<WidgetMiniStat> widgetMiniStats = new ArrayList<WidgetMiniStat>();
		widgetMiniStats.add(getMiniStatVisits());
		widgetMiniStats.add(getMiniStatUniqueVisits());
		widgetMiniStats.add(getMiniStatEnrolledUsers());
		widgetMiniStats.add(getMiniStatEnrolledUsersWithVisits());
		widgetMiniStats.add(getMiniStatEnrolledUsersWithoutVisits());
		if(Locator.getFacade().getStatsManager().isEnableSitePresences()) {
			widgetMiniStats.add(getMiniStatAveragePresenceTime());
		}
		
		// Tabs
		List<AbstractTab> tabs = new ArrayList<AbstractTab>();
		tabs.add(new AbstractTab(new ResourceModel("overview_tab_bydate")) {
			private static final long	serialVersionUID	= 1L;
			@Override
			public Panel getPanel(String panelId) {
				return getWidgetTabByDate(panelId);
			}			
		});
		tabs.add(new AbstractTab(new ResourceModel("overview_tab_byuser")) {
			private static final long	serialVersionUID	= 1L;
			@Override
			public Panel getPanel(String panelId) {
				return getWidgetTabByUser(panelId);
			}			
		});

		// Final Widget object		
		String icon = StatsManager.SILK_ICONS_DIR + "user_gray.png";
		String title = (String) new ResourceModel("overview_title_visits").getObject();
		Widget widget = new Widget("widget", icon, title, widgetMiniStats, tabs);
		add(widget);
	}

	// -------------------------------------------------------------------------------	
	/** MiniStat:: Visits */
	private WidgetMiniStat getMiniStatVisits() {
		return new WidgetMiniStat() {
			private static final long	serialVersionUID	= 1L;
			@Override
			public String getValue() {
				long start = 0;
				if(log.isDebugEnabled()) start = System.currentTimeMillis();
				String val = Long.toString(Locator.getFacade().getStatsManager().getTotalSiteVisits(siteId));
				if(log.isDebugEnabled()) log.debug("getMiniStatVisits() in " + (System.currentTimeMillis() - start) + " ms");
				return val;
			}
			@Override
			public String getSecondValue() {
				return null;
			}
			@Override
			public String getTooltip() {
				return null;
			}
			@Override
			public boolean isWiderText() {
				return false;
			}
			@Override
			public String getLabel() {
				return (String) new ResourceModel("overview_title_visits_sum").getObject();
			}
			@Override
			public ReportDef getReportDefinition() {
				ReportDef r = new ReportDef();
				r.setId(0);
				r.setSiteId(siteId);
				ReportParams rp = new ReportParams(siteId);
				rp.setWhat(ReportManager.WHAT_VISITS_TOTALS);
				rp.setWhen(ReportManager.WHEN_ALL);
				rp.setWho(ReportManager.WHO_ALL);
				// grouping
				List<String> totalsBy = new ArrayList<String>();
				totalsBy.add(StatsManager.T_DATE);
				totalsBy.add(StatsManager.T_VISITS);
				totalsBy.add(StatsManager.T_UNIQUEVISITS);
				rp.setHowTotalsBy(totalsBy);
				// sorting
				rp.setHowSort(true);
				rp.setHowSortBy(StatsManager.T_DATE);
				rp.setHowSortAscending(false);
				// chart
				rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_BOTH);
				rp.setHowChartType(StatsManager.CHARTTYPE_TIMESERIESBAR);
				rp.setHowChartSource(StatsManager.T_DATE);
				rp.setHowChartSeriesSource(StatsManager.T_NONE);
				rp.setHowChartSeriesPeriod(StatsManager.CHARTTIMESERIES_DAY);
				r.setReportParams(rp);
				return r;
			}
		};
	}

	/** MiniStat:: Unique Visits */
	private WidgetMiniStat getMiniStatUniqueVisits() {
		return new WidgetMiniStat() {
			private static final long	serialVersionUID	= 1L;
			@Override
			public String getValue() {
				long start = 0;
				if(log.isDebugEnabled()) start = System.currentTimeMillis();
				String val = Long.toString(Locator.getFacade().getStatsManager().getTotalSiteUniqueVisits(siteId));
				if(log.isDebugEnabled()) log.debug("getMiniStatUniqueVisits() in " + (System.currentTimeMillis() - start) + " ms");
				return val;
			}
			@Override
			public String getSecondValue() {
				return null;
			}
			@Override
			public String getTooltip() {
				return null;
			}
			@Override
			public boolean isWiderText() {
				return false;
			}
			@Override
			public String getLabel() {
				return (String) new ResourceModel("overview_title_unique_visits_sum").getObject();
			}
			@Override
			public ReportDef getReportDefinition() {
				ReportDef r = new ReportDef();
				r.setId(0);
				r.setSiteId(siteId);
				ReportParams rp = new ReportParams(siteId);
				rp.setWhat(ReportManager.WHAT_VISITS_TOTALS);
				rp.setWhen(ReportManager.WHEN_ALL);
				rp.setWho(ReportManager.WHO_ALL);
				// grouping
				List<String> totalsBy = new ArrayList<String>();
				totalsBy.add(StatsManager.T_DATE);
				totalsBy.add(StatsManager.T_VISITS);
				totalsBy.add(StatsManager.T_UNIQUEVISITS);
				rp.setHowTotalsBy(totalsBy);
				// sorting
				rp.setHowSort(true);
				rp.setHowSortBy(StatsManager.T_DATE);
				rp.setHowSortAscending(false);
				// chart
				rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_BOTH);
				rp.setHowChartType(StatsManager.CHARTTYPE_TIMESERIESBAR);
				rp.setHowChartSource(StatsManager.T_DATE);
				rp.setHowChartSeriesSource(StatsManager.T_NONE);
				rp.setHowChartSeriesPeriod(StatsManager.CHARTTIMESERIES_DAY);
				r.setReportParams(rp);
				return r;
			}
		};
	}
	
	/** MiniStat: Enrolled Users */
	private WidgetMiniStat getMiniStatEnrolledUsers() {
		return new WidgetMiniStat() {
			private static final long	serialVersionUID	= 1L;
			@Override
			public String getValue() {
				long start = 0;
				if(log.isDebugEnabled()) start = System.currentTimeMillis();
				String val = Long.toString(Locator.getFacade().getStatsManager().getTotalSiteUsers(siteId));
				if(log.isDebugEnabled()) log.debug("getMiniStatEnrolledUsers() in " + (System.currentTimeMillis() - start) + " ms");
				return val;
			}
			@Override
			public String getSecondValue() {
				return null;
			}
			@Override
			public String getTooltip() {
				return null;
			}
			@Override
			public boolean isWiderText() {
				return false;
			}
			@Override
			public String getLabel() {
				return (String) new ResourceModel("overview_title_enrolled_users_sum").getObject();
			}
			@Override
			public ReportDef getReportDefinition() {
				return null;
			}
		};
	}
	
	/** MiniStat: Enrolled Users with visits */
	private WidgetMiniStat getMiniStatEnrolledUsersWithVisits() {
		return new WidgetMiniStat() {
			private static final long	serialVersionUID		= 1L;
			
			@Override
			public String getValue() {
				long start = 0;
				if(log.isDebugEnabled()) start = System.currentTimeMillis();
				int enrUsersWithVisits = 0;
				Set<String> enrUsers = getSiteUsers();
				Set<String> usersWithVisits = getUsersWithVisits();
				for(String enru : enrUsers) {
					if(usersWithVisits.contains(enru)) {
						enrUsersWithVisits++;
					}
				}
				String val = String.valueOf(enrUsersWithVisits);
				if(log.isDebugEnabled()) log.debug("getMiniStatEnrolledUsersWithVisits() in " + (System.currentTimeMillis() - start) + " ms");
				return val;
			}
			@Override
			public String getSecondValue() {
				int totalUsersInSite = getSiteUsers().size();
				int totalEnrUsersWithVisits = Integer.parseInt(getValue());
				double percentage = totalUsersInSite==0 ? 0 : Util.round(100 * totalEnrUsersWithVisits / (double) totalUsersInSite, 0);
				return String.valueOf((int) percentage) + '%';
			}
			@Override
			public String getTooltip() {
				return null;
			}
			@Override
			public boolean isWiderText() {
				return false;
			}
			@Override
			public String getLabel() {
				return (String) new ResourceModel("overview_title_enrolled_users_with_visits_sum").getObject();
			}
			@Override
			public ReportDef getReportDefinition() {
				ReportDef r = new ReportDef();
				r.setId(0);
				r.setSiteId(siteId);
				ReportParams rp = new ReportParams(siteId);
				rp.setWhat(ReportManager.WHAT_VISITS);
				rp.setWhen(ReportManager.WHEN_ALL);
				rp.setWho(ReportManager.WHO_ALL);
				// grouping
				List<String> totalsBy = new ArrayList<String>();
				totalsBy.add(StatsManager.T_USER);
				rp.setHowTotalsBy(totalsBy);
				// sorting
				rp.setHowSort(false);
				// chart
				rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
				r.setReportParams(rp);
				return r;
			}
		};
	}
	
	/** MiniStat: Enrolled Users without visits */
	private WidgetMiniStat getMiniStatEnrolledUsersWithoutVisits() {
		return new WidgetMiniStat() {
			private static final long	serialVersionUID	= 1L;
			
			
			@Override
			public String getValue() {
				long start = 0;
				if(log.isDebugEnabled()) start = System.currentTimeMillis();
				int enrUsersWithoutVisits = 0;
				Set<String> enrUsers = getSiteUsers();
				Set<String> usersWithVisits = getUsersWithVisits();
				for(String enru : enrUsers) {
					if(!usersWithVisits.contains(enru)) {
						enrUsersWithoutVisits++;
					}
				}
				String val = String.valueOf(enrUsersWithoutVisits);
				if(log.isDebugEnabled()) log.debug("getMiniStatEnrolledUsersWithoutVisits() in " + (System.currentTimeMillis() - start) + " ms");
				return val;
			}
			@Override
			public String getSecondValue() {
				int totalUsersInSite = getSiteUsers().size();
				int totalEnrUsersWithoutVisits = Integer.parseInt(getValue());
				double percentage = totalUsersInSite==0 ? 0 : Util.round(100 * totalEnrUsersWithoutVisits / (double) totalUsersInSite, 0);
				return String.valueOf((int) percentage) + '%';
			}
			@Override
			public String getTooltip() {
				return null;
			}
			@Override
			public boolean isWiderText() {
				return false;
			}
			@Override
			public String getLabel() {
				return (String) new ResourceModel("overview_title_enrolled_users_without_visits_sum").getObject();
			}
			@Override
			public ReportDef getReportDefinition() {
				ReportDef r = new ReportDef();
				r.setId(0);
				r.setSiteId(siteId);
				ReportParams rp = new ReportParams(siteId);
				rp.setWhat(ReportManager.WHAT_VISITS);
				rp.setWhen(ReportManager.WHEN_ALL);
				rp.setWho(ReportManager.WHO_NONE);
				// grouping
				List<String> totalsBy = new ArrayList<String>();
				totalsBy.add(StatsManager.T_USER);
				rp.setHowTotalsBy(totalsBy);
				// sorting
				rp.setHowSort(false);
				// chart
				rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
				r.setReportParams(rp);
				return r;
			}
		};
	}
	
	/** MiniStat: Average presence time */
	private WidgetMiniStat getMiniStatAveragePresenceTime() {
		return new WidgetMiniStat() {
			private static final long	serialVersionUID	= 1L;
			@Override
			public String getValue() {
				long start = 0;
				if(log.isDebugEnabled()) start = System.currentTimeMillis();
				
				// Total time in site
				double durationInMs = getTotalTimeInSiteInMs();			
				
				// First presence date
				Date firstPresenceDate = getFirstPresenceDate();
				
				// Total visits since first presence date
				long totalVisits = Locator.getFacade().getStatsManager().getTotalSiteVisits(siteId, firstPresenceDate, null);
				
				// Average presence time (total presence time / total visits)
				double durationInMin = durationInMs == 0 ? 0 : Util.round((durationInMs/totalVisits) / 1000 / 60, 1); // in minutes
				
				StringBuilder val = new StringBuilder();
				val.append(String.valueOf(durationInMin));
				val.append(' ');
				val.append(new ResourceModel("minutes_abbr").getObject());	
				
				if(log.isDebugEnabled()) log.debug("getMiniStatAveragePresenceTime() in " + (System.currentTimeMillis() - start) + " ms");
				return val.toString();
			}
			@Override
			public String getSecondValue() {
				return null;
			}
			@Override
			public String getTooltip() {
				return null;
			}
			@Override
			public boolean isWiderText() {
				return false;
			}
			@Override
			public String getLabel() {
				return (String) new ResourceModel("overview_title_presence_time_avg").getObject();
			}
			@Override
			public ReportDef getReportDefinition() {
				ReportDef r = new ReportDef();
				r.setId(0);
				r.setSiteId(siteId);
				ReportParams rp = new ReportParams(siteId);
				rp.setWhat(ReportManager.WHAT_PRESENCES);
				rp.setWhen(ReportManager.WHEN_ALL);
				rp.setWho(ReportManager.WHO_ALL);
				// grouping
				List<String> totalsBy = new ArrayList<String>();
				totalsBy.add(StatsManager.T_DATE);
				totalsBy.add(StatsManager.T_USER);
				rp.setHowTotalsBy(totalsBy);
				// sorting
				rp.setHowSort(true);
				rp.setHowSortBy(StatsManager.T_DATE);
				rp.setHowSortAscending(false);
				// chart
				rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_BOTH);
				rp.setHowChartType(StatsManager.CHARTTYPE_TIMESERIESBAR);
				rp.setHowChartSource(StatsManager.T_DATE);
				rp.setHowChartSeriesSource(StatsManager.T_NONE);
				rp.setHowChartSeriesPeriod(StatsManager.CHARTTIMESERIES_MONTH);
				r.setReportParams(rp);
				return r;
			}
			
			private double getTotalTimeInSiteInMs() {
				ReportDef r = new ReportDef();
				r.setId(0);
				r.setSiteId(siteId);
				ReportParams rp = new ReportParams(siteId);
				rp.setWhat(ReportManager.WHAT_PRESENCES);
				rp.setWhen(ReportManager.WHEN_ALL);
				rp.setWho(ReportManager.WHO_ALL);
				List<String> totalsBy = new ArrayList<String>();
				totalsBy.add(StatsManager.T_SITE);
				rp.setHowTotalsBy(totalsBy);
				r.setReportParams(rp);
				Report report = Locator.getFacade().getReportManager().getReport(r, true);
				double duration = 0;;
				if(report.getReportData().size() > 0) {
					duration = (double) ((SitePresence)(report.getReportData().get(0))).getDuration();
				}
				return duration;
			}
			
			private Date getFirstPresenceDate() {
				ReportDef r = new ReportDef();
				r.setId(0);
				r.setSiteId(siteId);
				ReportParams rp = new ReportParams(siteId);
				rp.setWhat(ReportManager.WHAT_PRESENCES);
				rp.setWhen(ReportManager.WHEN_ALL);
				rp.setWho(ReportManager.WHO_ALL);
				List<String> totalsBy = new ArrayList<String>();
				totalsBy.add(StatsManager.T_DATE);
				rp.setHowTotalsBy(totalsBy);
				rp.setHowSort(true);
				rp.setHowSortAscending(true);
				rp.setHowSortBy(StatsManager.T_DATE);
				r.setReportParams(rp);
				PagingPosition paging = new PagingPosition();
				Report report = Locator.getFacade().getReportManager().getReport(r, true, paging, false);
				Date firstDate = new Date();
				if(report.getReportData().size() > 0) {
					firstDate = ((SitePresence)(report.getReportData().get(0))).getDate();
				}
				return firstDate;
			}
			
		};
	}
	
	// -------------------------------------------------------------------------------
	
	/** WidgetTab: By date */
	protected WidgetTabTemplate getWidgetTabByDate(String panelId) {
		WidgetTabTemplate wTab = new WidgetTabTemplate(panelId, VisitsWidget.this.siteId) {
			private static final long	serialVersionUID	= 1L;

			@Override
			public List<Integer> getFilters() {
				return Arrays.asList(FILTER_DATE, FILTER_ROLE);
			}

			@Override
			public boolean useChartReportDefinitionForTable() {
				return true;
			}
			
			@Override
			public ReportDef getChartReportDefinition() {
				return getTableReportDefinition();
			}
			
			@Override
			public ReportDef getTableReportDefinition() {
				String dateFilter = getDateFilter();
				String roleFilter = getRoleFilter();
				
				ReportDef r = new ReportDef();
				r.setSiteId(siteId);
				ReportParams rp = new ReportParams(siteId);
				// what
				if(ReportManager.WHO_ALL.equals(roleFilter)) {
					rp.setWhat(ReportManager.WHAT_VISITS_TOTALS);
				}else{
					rp.setWhat(ReportManager.WHAT_EVENTS);
					rp.setWhatEventSelType(ReportManager.WHAT_EVENTS_BYEVENTS);
					rp.setWhatEventIds(Arrays.asList(StatsManager.SITEVISIT_EVENTID));
				}
				// when
				rp.setWhen(dateFilter);
				// who
				if(!ReportManager.WHO_ALL.equals(roleFilter)) {
					rp.setWho(ReportManager.WHO_ROLE);
					rp.setWhoRoleId(roleFilter);
				}
				// grouping
				List<String> totalsBy = new ArrayList<String>();
				if(dateFilter.equals(ReportManager.WHEN_LAST365DAYS) || dateFilter.equals(ReportManager.WHEN_ALL)) {
					totalsBy.add(StatsManager.T_DATEMONTH);
				}else{
					totalsBy.add(StatsManager.T_DATE);
				}
				totalsBy.add(StatsManager.T_VISITS);
				totalsBy.add(StatsManager.T_UNIQUEVISITS);
				rp.setHowTotalsBy(totalsBy);
				// sorting
				rp.setHowSort(true);
				if(dateFilter.equals(ReportManager.WHEN_LAST365DAYS) || dateFilter.equals(ReportManager.WHEN_ALL)) {
					rp.setHowSortBy(StatsManager.T_DATEMONTH);
				}else{
					rp.setHowSortBy(StatsManager.T_DATE);
				}							
				rp.setHowSortAscending(false);
				// chart
				rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_BOTH);
				rp.setHowChartType(StatsManager.CHARTTYPE_TIMESERIESBAR);
				rp.setHowChartSource(StatsManager.T_DATE);
				rp.setHowChartSeriesSource(StatsManager.T_NONE);
				if(dateFilter.equals(ReportManager.WHEN_LAST365DAYS) || dateFilter.equals(ReportManager.WHEN_ALL)) {
					rp.setHowChartSeriesPeriod(StatsManager.CHARTTIMESERIES_MONTH);
				}else if(dateFilter.equals(ReportManager.WHEN_LAST30DAYS)) {
					rp.setHowChartSeriesPeriod(StatsManager.CHARTTIMESERIES_DAY);
				}else{
					rp.setHowChartSeriesPeriod(StatsManager.CHARTTIMESERIES_WEEKDAY);
				}
				r.setReportParams(rp);
				
				return r;
			}					
		};
		return wTab;
	}
	
	/** WidgetTab: By user */
	protected WidgetTabTemplate getWidgetTabByUser(String panelId) {
		WidgetTabTemplate wTab = new WidgetTabTemplate(panelId, VisitsWidget.this.siteId) {
			private static final long	serialVersionUID	= 1L;

			@Override
			public List<Integer> getFilters() {
				return Arrays.asList(FILTER_DATE, FILTER_ROLE);
			}

			@Override
			public boolean useChartReportDefinitionForTable() {
				return false;
			}
			
			@Override
			public ReportDef getTableReportDefinition() {
				ReportDef r = getChartReportDefinition();
				ReportParams rp = r.getReportParams();
				List<String> totalsBy = new ArrayList<String>();
				totalsBy.add(StatsManager.T_USER);
				rp.setHowTotalsBy(totalsBy);
				rp.setHowSortBy(StatsManager.T_TOTAL);
				r.setReportParams(rp);
				return r;
			}
			
			@Override
			public ReportDef getChartReportDefinition() {
				String dateFilter = getDateFilter();
				String roleFilter = getRoleFilter();
				
				ReportDef r = new ReportDef();
				r.setSiteId(siteId);
				ReportParams rp = new ReportParams(siteId);
				// what
				rp.setWhat(ReportManager.WHAT_EVENTS);
				rp.setWhatEventSelType(ReportManager.WHAT_EVENTS_BYEVENTS);
				rp.setWhatEventIds(Arrays.asList(StatsManager.SITEVISIT_EVENTID));
				// when
				rp.setWhen(dateFilter);
				// who
				if(!ReportManager.WHO_ALL.equals(roleFilter)) {
					rp.setWho(ReportManager.WHO_ROLE);
					rp.setWhoRoleId(roleFilter);
				}
				// grouping
				List<String> totalsBy = new ArrayList<String>();
				if(dateFilter.equals(ReportManager.WHEN_LAST365DAYS) || dateFilter.equals(ReportManager.WHEN_ALL)) {
					totalsBy.add(StatsManager.T_DATEMONTH);
				}else{
					totalsBy.add(StatsManager.T_DATE);
				}
				totalsBy.add(StatsManager.T_USER);
				rp.setHowTotalsBy(totalsBy);
				// sorting
				rp.setHowSort(true);
				/*if(dateFilter.equals(ReportManager.WHEN_LAST365DAYS) || dateFilter.equals(ReportManager.WHEN_ALL)) {
					rp.setHowSortBy(StatsManager.T_DATEMONTH);
				}else{
					rp.setHowSortBy(StatsManager.T_DATE);
				}*/		
				rp.setHowSortBy(StatsManager.T_TOTAL);
				rp.setHowSortAscending(false);
				// chart
				rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_BOTH);
				/*rp.setHowChartType(StatsManager.CHARTTYPE_TIMESERIES);
				rp.setHowChartSource(StatsManager.T_DATE);
				rp.setHowChartSeriesSource(StatsManager.T_USER);
				if(dateFilter.equals(ReportManager.WHEN_LAST365DAYS) || dateFilter.equals(ReportManager.WHEN_ALL)) {
					rp.setHowChartSeriesPeriod(StatsManager.CHARTTIMESERIES_MONTH);
				}else if(dateFilter.equals(ReportManager.WHEN_LAST30DAYS)) {
					rp.setHowChartSeriesPeriod(StatsManager.CHARTTIMESERIES_DAY);
				}else{
					rp.setHowChartSeriesPeriod(StatsManager.CHARTTIMESERIES_WEEKDAY);
				}*/
				rp.setHowChartType(StatsManager.CHARTTYPE_PIE);
				rp.setHowChartSource(StatsManager.T_USER);
				r.setReportParams(rp);
				
				return r;
			}
		};
		return wTab;
	}
	
	// -------------------------------------------------------------------------------
	
	private Set<String> getSiteUsers() {
		if(siteUsers == null) {
			siteUsers = Locator.getFacade().getStatsManager().getSiteUsers(siteId);
			if(siteUsers == null) {
				siteUsers = new HashSet<String>();
			}
		}
		return siteUsers;
	}
	
	private Set<String> getUsersWithVisits() {
		if(usersWithVisits == null) {
			usersWithVisits = Locator.getFacade().getStatsManager().getUsersWithVisits(siteId);
			if(usersWithVisits == null) {
				usersWithVisits = new HashSet<String>();
			}
		}
		return usersWithVisits;
	}
}
