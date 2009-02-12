package org.sakaiproject.sitestats.tool.wicket.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;

public class VisitsWidget extends Panel {
	private static final long		serialVersionUID	= 1L;
	private static Log				LOG					= LogFactory.getLog(VisitsWidget.class);

	/** Inject Sakai facade */
	@SpringBean
	private transient SakaiFacade	facade;

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
		String icon = "/sakai-sitestats-tool/images/silk/icons/user_gray.png";
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
				return Long.toString(facade.getStatsManager().getTotalSiteVisits(siteId));
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
				rp.setHowChartSeriesPeriod(StatsManager.CHARTTIMESERIES_MONTH);
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
				return Long.toString(facade.getStatsManager().getTotalSiteUniqueVisits(siteId));
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
				rp.setHowChartSeriesPeriod(StatsManager.CHARTTIMESERIES_MONTH);
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
				return Long.toString(facade.getStatsManager().getTotalSiteUsers(siteId));
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
				Set<String> _siteUsers = getSiteUsers();
				_siteUsers.retainAll(getUsersWithVisits());
				return String.valueOf(_siteUsers.size());
			}
			@Override
			public String getSecondValue() {
				Set<String> _siteUsers = getSiteUsers();
				_siteUsers.retainAll(getUsersWithVisits());
				int totalUsersInSite = getSiteUsers().size();
				double percentage = totalUsersInSite==0 ? 0 : round(100 * _siteUsers.size() / totalUsersInSite, 0);
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
				Set<String> _siteUsers = getSiteUsers();
				_siteUsers.removeAll(getUsersWithVisits());
				return String.valueOf(_siteUsers.size());
			}
			@Override
			public String getSecondValue() {
				Set<String> _siteUsers = getSiteUsers();
				_siteUsers.removeAll(getUsersWithVisits());
				int totalUsersInSite = getSiteUsers().size();
				double percentage = totalUsersInSite==0 ? 0 : round(100 * _siteUsers.size() / totalUsersInSite, 0);
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
				rp.setHowLimitedMaxResults(true);
				rp.setHowMaxResults(MAX_TABLE_ROWS);
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
			siteUsers = facade.getStatsManager().getSiteUsers(siteId);
			if(siteUsers == null) {
				siteUsers = new HashSet<String>();
			}
		}
		return siteUsers;
	}
	
	private Set<String> getUsersWithVisits() {
		if(usersWithVisits == null) {
			usersWithVisits = facade.getStatsManager().getUsersWithVisits(siteId);
			if(usersWithVisits == null) {
				usersWithVisits = new HashSet<String>();
			}
		}
		return usersWithVisits;
	}
	
	private static double round(double val, int places) {
		long factor = (long) Math.pow(10, places);
		// Shift the decimal the correct number of places to the right.
		val = val * factor;
		// Round to the nearest integer.
		long tmp = Math.round(val);
		// Shift the decimal the correct number of places back to the left.
		return (double) tmp / factor;
	}
}
