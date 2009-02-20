package org.sakaiproject.sitestats.tool.wicket.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.sitestats.api.EventStat;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
import org.sakaiproject.sitestats.tool.wicket.pages.PreferencesPage;
import org.sakaiproject.user.api.UserNotDefinedException;

public class ActivityWidget extends Panel {
	private static final long		serialVersionUID	= 1L;
	private static Log				LOG					= LogFactory.getLog(ActivityWidget.class);

	/** Inject Sakai facade */
	@SpringBean
	private transient SakaiFacade	facade;

	/** The site id. */
	private String					siteId				= null;
	private PrefsData				prefsdata			= null;
	

	/**
	 * Default constructor.
	 * @param id The wicket:id
	 * @param siteId The related site id
	 */
	public ActivityWidget(String id, final String siteId) {
		super(id);
		this.siteId = siteId;
		setRenderBodyOnly(true);
		setOutputMarkupId(true);
		
		// Single values (MiniStat)
		List<WidgetMiniStat> widgetMiniStats = new ArrayList<WidgetMiniStat>();
		widgetMiniStats.add(getMiniStatActivityEvents());
		widgetMiniStats.add(getMiniStatMostActiveTool());
		widgetMiniStats.add(getMiniStatMostActiveUser());
		//widgetMiniStats.add(getMiniStatConfigureLink());		
		
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
		tabs.add(new AbstractTab(new ResourceModel("overview_tab_bytool")) {
			private static final long	serialVersionUID	= 1L;
			@Override
			public Panel getPanel(String panelId) {
				return getWidgetTabByTool(panelId);
			}			
		});

		// Final Widget object		
		String icon = "/sakai-sitestats-tool/images/silk/icons/chart_pie.png";
		String title = (String) new ResourceModel("overview_title_activity").getObject();
		Widget widget = new Widget("widget", icon, title, widgetMiniStats, tabs);
		add(widget);		
	}

	// -------------------------------------------------------------------------------	
	/** MiniStat:: Activity events */
	private WidgetMiniStat getMiniStatActivityEvents() {
		return new WidgetMiniStat() {
			private static final long	serialVersionUID	= 1L;
			@Override
			public String getValue() {
				return Long.toString(getFacade().getStatsManager().getTotalSiteActivity(siteId, getPrefsdata().getToolEventsStringList()));
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
				return (String) new ResourceModel("overview_title_events_sum").getObject();
			}
			@Override
			public ReportDef getReportDefinition() {
				ReportDef r = new ReportDef();
				r.setId(0);
				r.setSiteId(siteId);
				ReportParams rp = new ReportParams(siteId);
				// what
				rp.setWhat(ReportManager.WHAT_EVENTS);
				rp.setWhatEventSelType(ReportManager.WHAT_EVENTS_BYEVENTS);
				rp.setWhatEventIds(getPrefsdata().getToolEventsStringList());
				// when
				rp.setWhen(ReportManager.WHEN_ALL);
				// who
				rp.setWho(ReportManager.WHO_ALL);
				// grouping
				List<String> totalsBy = new ArrayList<String>();
				totalsBy.add(StatsManager.T_EVENT);
				rp.setHowTotalsBy(totalsBy);
				// sorting
				rp.setHowSort(true);
				rp.setHowSortBy(StatsManager.T_EVENT);
				rp.setHowSortAscending(true);
				// chart
				rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
				r.setReportParams(rp);
				return r;
			}
		};
	}
	
	/** MiniStat:: Most active tool */
	private WidgetMiniStat getMiniStatMostActiveTool() {
		return new WidgetMiniStat() {
			private static final long	serialVersionUID	= 1L;
			private String				mostActiveTool		= null;
			private long				totalToolActivity	= 0;
			private long				totalActivity		= 0;
			
			@Override
			public String getValue() {
				processData();
				if(mostActiveTool != null) {
					return getFacade().getEventRegistryService().getToolName(mostActiveTool);
				}else{
					return "-";
				}
			}
			
			@Override
			public String getSecondValue() {
				double percentage = totalActivity==0 ? 0 : round(100 * totalToolActivity / (double) totalActivity, 0);
				return String.valueOf((int) percentage) + '%';
			}
			
			@Override
			public String getTooltip() {
				if(mostActiveTool != null) {
					return getFacade().getEventRegistryService().getToolName(mostActiveTool);
				}else{
					return null;
				}
			}
			
			@Override
			public boolean isWiderText() {
				return true;
			}
			
			@Override
			public String getLabel() {
				return (String) new ResourceModel("overview_title_mostactivetool_sum").getObject();
			}
			
			private ReportDef getCommonReportDefition() {
				ReportDef r = new ReportDef();
				r.setId(0);
				r.setSiteId(siteId);
				ReportParams rp = new ReportParams(siteId);
				// what
				rp.setWhat(ReportManager.WHAT_EVENTS);
				rp.setWhatEventSelType(ReportManager.WHAT_EVENTS_BYEVENTS);
				rp.setWhatEventIds(getPrefsdata().getToolEventsStringList());
				// when
				rp.setWhen(ReportManager.WHEN_ALL);
				// who
				rp.setWho(ReportManager.WHO_ALL);
				// grouping
				List<String> totalsBy = new ArrayList<String>();
				totalsBy.add(StatsManager.T_EVENT);
				rp.setHowTotalsBy(totalsBy);
				// sorting
				rp.setHowSort(true);
				rp.setHowSortBy(StatsManager.T_EVENT);
				rp.setHowSortAscending(true);
				// chart
				rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
				r.setReportParams(rp);
				return r;
			}
			
			private void processData() {
				if(mostActiveTool == null) {
					ReportDef rd = getCommonReportDefition();
					ReportParams rp = rd.getReportParams();
					List<String> totalsBy = new ArrayList<String>();
					totalsBy.add(StatsManager.T_TOOL);
					rp.setHowTotalsBy(totalsBy);
					rp.setHowSort(true);
					rp.setHowSortBy(StatsManager.T_TOTAL);
					rp.setHowSortAscending(false);
					Report r = getFacade().getReportManager().getReport(rd, true, null, false);
					try{
						boolean first = true;
						for(Stat s : r.getReportData()) {
							EventStat es = (EventStat) s;
							if(first) {
								mostActiveTool = es.getToolId();
								totalToolActivity = es.getCount();
								first = false;
							}
							totalActivity += es.getCount();
						}
					}catch(Exception e) {
						mostActiveTool = null;
					}
				}
			}
			
			@Override
			public ReportDef getReportDefinition() {
				ReportDef rd = getCommonReportDefition();
				ReportParams rp = rd.getReportParams();
				List<String> totalsBy = new ArrayList<String>();
				totalsBy.add(StatsManager.T_TOOL);
				rp.setHowTotalsBy(totalsBy);
				rp.setHowSortBy(StatsManager.T_TOOL);
				rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_BOTH);
				rp.setHowChartType(StatsManager.CHARTTYPE_PIE);
				rp.setHowChartSource(StatsManager.T_TOOL);
				return rd;
			}
		};
	}
	
	/** MiniStat:: Most active user */
	private WidgetMiniStat getMiniStatMostActiveUser() {
		return new WidgetMiniStat() {
			private static final long	serialVersionUID	= 1L;
			private String				mostActiveUser		= null;
			private long				totalUserActivity	= 0;
			private long				totalActivity		= 0;
			
			@Override
			public String getValue() {
				processData();
				if(mostActiveUser != null) {
					String id = null;
					if(("-").equals(mostActiveUser) || ("?").equals(mostActiveUser)){
						id = "-";
					}else{
						try{
							id = getFacade().getUserDirectoryService().getUser(mostActiveUser).getDisplayId();
						}catch(UserNotDefinedException e1){
							id = mostActiveUser;
						}
					}
					return id;
				}else{
					return "-";
				}
			}
			
			@Override
			public String getSecondValue() {
				double percentage = totalActivity==0 ? 0 : round(100 * totalUserActivity / (double) totalActivity, 0);
				return String.valueOf((int) percentage) + '%';
			}
			
			@Override
			public String getTooltip() {
				if(mostActiveUser != null) {
					String name = null;
					if(("-").equals(mostActiveUser)) {
						name = (String) new ResourceModel("user_anonymous").getObject();
					}else if(("?").equals(mostActiveUser)) {
						name = (String) new ResourceModel("user_anonymous_access").getObject();
					}else{
						try{
							name = getFacade().getUserDirectoryService().getUser(mostActiveUser).getDisplayName();
						}catch(UserNotDefinedException e1){
							name = (String) new ResourceModel("user_unknown").getObject();
						}
					}
					return name;
				}else{
					return null;
				}
			}
			
			@Override
			public boolean isWiderText() {
				return true;
			}
			
			@Override
			public String getLabel() {
				return (String) new ResourceModel("overview_title_mostactiveuser_sum").getObject();
			}
			
			private ReportDef getCommonReportDefition() {
				ReportDef r = new ReportDef();
				r.setId(0);
				r.setSiteId(siteId);
				ReportParams rp = new ReportParams(siteId);
				// what
				rp.setWhat(ReportManager.WHAT_EVENTS);
				rp.setWhatEventSelType(ReportManager.WHAT_EVENTS_BYEVENTS);
				rp.setWhatEventIds(getPrefsdata().getToolEventsStringList());
				// when
				rp.setWhen(ReportManager.WHEN_ALL);
				// who
				rp.setWho(ReportManager.WHO_ALL);
				// grouping
				List<String> totalsBy = new ArrayList<String>();
				totalsBy.add(StatsManager.T_USER);
				rp.setHowTotalsBy(totalsBy);
				// sorting
				rp.setHowSort(true);
				rp.setHowSortBy(StatsManager.T_TOTAL);
				rp.setHowSortAscending(false);
				// chart
				rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
				r.setReportParams(rp);
				return r;
			}
			
			private void processData() {
				if(mostActiveUser == null) {
					Report r = getFacade().getReportManager().getReport(getCommonReportDefition(), true, null, false);
					try{
						boolean first = true;
						for(Stat s : r.getReportData()) {
							EventStat es = (EventStat) s;
							if(first) {
								mostActiveUser = es.getUserId();
								totalUserActivity = es.getCount();
								first = false;
							}
							totalActivity += es.getCount();
						}
					}catch(Exception e) {
						mostActiveUser = null;
					}
				}
			}
			
			@Override
			public ReportDef getReportDefinition() {
				return getCommonReportDefition();
			}
		};
	}
	
	/** MiniStat:: Link for Preferences */
	private WidgetMiniStat getMiniStatConfigureLink() {
		return new WidgetMiniStatLink() {
			private static final long	serialVersionUID	= 1L;

			@Override
			public Page getPageLink() {
				return new PreferencesPage(new PageParameters("siteId="+siteId));
			}
			
			@Override
			public String getLabel() {
				return (String) new ResourceModel("overview_title_configure_activity").getObject();
			}
			
			@Override
			public String getPageLinkTooltip() {
				return (String) new ResourceModel("overview_title_configure_activity_tip").getObject();
			};
		};
	}
	
	// -------------------------------------------------------------------------------
	
	/** WidgetTab: By date */
	protected WidgetTabTemplate getWidgetTabByDate(String panelId) {
		WidgetTabTemplate wTab = new WidgetTabTemplate(panelId, ActivityWidget.this.siteId) {
			private static final long	serialVersionUID	= 1L;

			@Override
			public List<Integer> getFilters() {
				return Arrays.asList(FILTER_DATE, FILTER_ROLE, FILTER_TOOL);
			}

			@Override
			public boolean useChartReportDefinitionForTable() {
				return true;
			}
			
			@Override
			public ReportDef getTableReportDefinition() {
				return getChartReportDefinition();
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
				rp.setWhatEventIds(getToolEventsFilter());
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
		WidgetTabTemplate wTab = new WidgetTabTemplate(panelId, ActivityWidget.this.siteId) {
			private static final long	serialVersionUID	= 1L;

			@Override
			public List<Integer> getFilters() {
				return Arrays.asList(FILTER_DATE, FILTER_ROLE, FILTER_TOOL);
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
				rp.setHowChartType(StatsManager.CHARTTYPE_PIE);
				rp.setHowChartSource(StatsManager.T_USER);
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
				rp.setWhatEventIds(getToolEventsFilter());
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
				if(dateFilter.equals(ReportManager.WHEN_LAST365DAYS) || dateFilter.equals(ReportManager.WHEN_ALL)) {
					rp.setHowSortBy(StatsManager.T_DATEMONTH);
				}else{
					rp.setHowSortBy(StatsManager.T_DATE);
				}							
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
	
	/** WidgetTab: By tool */
	protected WidgetTabTemplate getWidgetTabByTool(String panelId) {
		WidgetTabTemplate wTab = new WidgetTabTemplate(panelId, ActivityWidget.this.siteId) {
			private static final long	serialVersionUID	= 1L;

			@Override
			public List<Integer> getFilters() {
				return Arrays.asList(FILTER_DATE, FILTER_ROLE, FILTER_TOOL);
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
				totalsBy.add(StatsManager.T_TOOL);
				rp.setHowTotalsBy(totalsBy);
				rp.setHowSortBy(StatsManager.T_TOTAL);
				rp.setHowChartType(StatsManager.CHARTTYPE_PIE);
				rp.setHowChartSource(StatsManager.T_TOOL);
				rp.setHowLimitedMaxResults(true);
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
				rp.setWhatEventIds(getToolEventsFilter());
				// when
				rp.setWhen(dateFilter);
				// who
				if(!ReportManager.WHO_ALL.equals(roleFilter)) {
					rp.setWho(ReportManager.WHO_ROLE);
					rp.setWhoRoleId(roleFilter);
				}
				// grouping
				List<String> totalsBy = new ArrayList<String>();
				/*if(dateFilter.equals(ReportManager.WHEN_LAST365DAYS) || dateFilter.equals(ReportManager.WHEN_ALL)) {
					totalsBy.add(StatsManager.T_DATEMONTH);
				}else{
					totalsBy.add(StatsManager.T_DATE);
				}*/
				totalsBy.add(StatsManager.T_TOOL);
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
				rp.setHowChartType(StatsManager.CHARTTYPE_PIE);
				rp.setHowChartSource(StatsManager.T_TOOL);
				r.setReportParams(rp);
				
				return r;
			}					
		};
		return wTab;
	}
	
	// -------------------------------------------------------------------------------

	
	
	// -------------------------------------------------------------------------------
	
	private SakaiFacade getFacade() {
		if(facade == null) {
			InjectorHolder.getInjector().inject(this);
		}
		return facade;
	}
	
	private PrefsData getPrefsdata() {
		if(prefsdata == null) {
			prefsdata = getFacade().getStatsManager().getPreferences(siteId, true);
		}
		return prefsdata;
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
