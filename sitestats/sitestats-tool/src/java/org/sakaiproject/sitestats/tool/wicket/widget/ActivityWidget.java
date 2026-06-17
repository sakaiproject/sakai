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

import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_ACTIVITY_EVENTS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_ACTIVITY_MOST_ACTIVE_TOOL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_ACTIVITY_MOST_ACTIVE_USER;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_TOOL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_USER;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_ACTIVITY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.Page;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;

import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.sitestats.api.EventStat;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.Util;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.sitestats.tool.wicket.pages.PreferencesPage;
import org.sakaiproject.user.api.UserNotDefinedException;

@Slf4j
public class ActivityWidget extends Panel {
	private static final long		serialVersionUID	= 1L;

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

		StatsAuthz statsAuthz = Locator.getFacade().getStatsAuthz();
		boolean siteStatsAll = statsAuthz.isUserAbleToViewSiteStatsAll(siteId);
		
		// Single values (MiniStat)
		List<WidgetMiniStat> widgetMiniStats = new ArrayList<WidgetMiniStat>();
		widgetMiniStats.add(getMiniStatActivityEvents());
		widgetMiniStats.add(getMiniStatMostActiveTool());
		if (siteStatsAll) {
			widgetMiniStats.add(getMiniStatMostActiveUser());
		}
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
		String title = (String) new ResourceModel("overview_title_activity").getObject();
		Widget widget = new Widget("widget", WIDGET_ACTIVITY, "sakai-poll", title, widgetMiniStats, tabs, siteId);
		add(widget);
	}

	// -------------------------------------------------------------------------------	
	/** MiniStat:: Activity events */
	private WidgetMiniStat getMiniStatActivityEvents() {
		return new WidgetMiniStat() {
			private static final long	serialVersionUID	= 1L;
			@Override
			public String getValue() {
				long start = 0;
				if(log.isDebugEnabled()) start = System.currentTimeMillis();
				String val = Long.toString(Locator.getFacade().getStatsManager().getTotalSiteActivity(siteId, getPrefsdata().getToolEventsStringList()));
				if(log.isDebugEnabled()) log.debug("getMiniStatActivityEvents() in " + (System.currentTimeMillis() - start) + " ms");
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
				return (String) new ResourceModel("overview_title_events_sum").getObject();
			}
			@Override
			public String getReportMetricId() {
				return METRIC_ACTIVITY_EVENTS;
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
				long start = 0;
				if(log.isDebugEnabled()) start = System.currentTimeMillis();
				processData();
				String val = null;
				if(mostActiveTool != null) {
					val = Locator.getFacade().getEventRegistryService().getToolName(mostActiveTool);
				}else{
					val = "-";
				}
				if(log.isDebugEnabled()) log.debug("getMiniStatMostActiveTool() in " + (System.currentTimeMillis() - start) + " ms");
				return val;
			}
			
			@Override
			public String getSecondValue() {
				double percentage = totalActivity==0 ? 0 : Util.round(100 * totalToolActivity / (double) totalActivity, 0);
				return String.valueOf((int) percentage) + '%';
			}
			
			@Override
			public String getTooltip() {
				if(mostActiveTool != null) {
					return Locator.getFacade().getEventRegistryService().getToolName(mostActiveTool);
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
					Report r = Locator.getFacade().getReportManager().getReport(rd, true, null, false);
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
			public String getReportMetricId() {
				return METRIC_ACTIVITY_MOST_ACTIVE_TOOL;
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
				long start = 0;
				if(log.isDebugEnabled()) start = System.currentTimeMillis();
				processData();
				String val = null;
				if(mostActiveUser != null) {
					String id = null;
					if(("-").equals(mostActiveUser) || EventTrackingService.UNKNOWN_USER.equals(mostActiveUser)){
						id = "-";
					}else{
						try{
							id = Locator.getFacade().getUserDirectoryService().getUser(mostActiveUser).getDisplayId();
						}catch(UserNotDefinedException e1){
							id = mostActiveUser;
						}
					}
					val = id;
				}else{
					val = "-";
				}
				if(log.isDebugEnabled()) log.debug("getMiniStatMostActiveUser() in " + (System.currentTimeMillis() - start) + " ms");
				return val;
			}
			
			@Override
			public String getSecondValue() {
				double percentage = totalActivity==0 ? 0 : Util.round(100 * totalUserActivity / (double) totalActivity, 0);
				return String.valueOf((int) percentage) + '%';
			}
			
			@Override
			public String getTooltip() {
				if(mostActiveUser != null) {
					String name = null;
					if(("-").equals(mostActiveUser)) {
						name = (String) new ResourceModel("user_anonymous").getObject();
					}else if(EventTrackingService.UNKNOWN_USER.equals(mostActiveUser)) {
						name = (String) new ResourceModel("user_anonymous_access").getObject();
					}else{
						name = Locator.getFacade().getStatsManager().getUserNameForDisplay(mostActiveUser);
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
					Report r = Locator.getFacade().getReportManager().getReport(getCommonReportDefition(), true, null, false);
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
			public String getReportMetricId() {
				return METRIC_ACTIVITY_MOST_ACTIVE_USER;
			}
		};
	}
	
	/** MiniStat:: Link for Preferences */
	private WidgetMiniStat getMiniStatConfigureLink() {
		return new WidgetMiniStatLink() {
			private static final long	serialVersionUID	= 1L;

			@Override
			public Page getPageLink() {
				return new PreferencesPage(new PageParameters().set("siteId", siteId));
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
		return new WidgetTabTemplate(panelId, ActivityWidget.this.siteId, WIDGET_ACTIVITY, TAB_BY_DATE) {
			private static final long	serialVersionUID	= 1L;

			@Override
			public List<Integer> getFilters() {
				return Arrays.asList(FILTER_DATE, FILTER_ROLE, FILTER_TOOL);
			}
		};
		}

	/** WidgetTab: By user */
	protected WidgetTabTemplate getWidgetTabByUser(String panelId) {
		return new WidgetTabTemplate(panelId, ActivityWidget.this.siteId, WIDGET_ACTIVITY, TAB_BY_USER) {
			private static final long	serialVersionUID	= 1L;

			@Override
			public List<Integer> getFilters() {
				return Arrays.asList(FILTER_DATE, FILTER_ROLE, FILTER_TOOL);
			}
		};
		}

	/** WidgetTab: By tool */
	protected WidgetTabTemplate getWidgetTabByTool(String panelId) {
		return new WidgetTabTemplate(panelId, ActivityWidget.this.siteId, WIDGET_ACTIVITY, TAB_BY_TOOL) {
			private static final long	serialVersionUID	= 1L;

			@Override
			public List<Integer> getFilters() {
				return Arrays.asList(FILTER_DATE, FILTER_ROLE, FILTER_TOOL);
			}
		};
		}

	// -------------------------------------------------------------------------------

	private PrefsData getPrefsdata() {
		if(prefsdata == null) {
			prefsdata = Locator.getFacade().getStatsManager().getPreferences(siteId, true);
		}
		return prefsdata;
	}
}
