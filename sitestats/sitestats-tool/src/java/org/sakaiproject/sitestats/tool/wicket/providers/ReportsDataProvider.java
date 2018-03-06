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
package org.sakaiproject.sitestats.tool.wicket.providers;

import java.io.Serializable;
import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.sakaiproject.sitestats.api.EventStat;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.ResourceStat;
import org.sakaiproject.sitestats.api.SitePresence;
import org.sakaiproject.sitestats.api.SiteVisits;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

@Slf4j
public class ReportsDataProvider extends SortableSearchableDataProvider {
	private static final long		serialVersionUID	= 1L;
	public final static String		COL_SITE			= StatsManager.T_SITE;
	public final static String		COL_USERID			= StatsManager.T_USER;
	public final static String		COL_USERNAME		= "userName";
	public final static String		COL_EVENT			= StatsManager.T_EVENT;
	public final static String		COL_TOOL			= StatsManager.T_TOOL;
	public final static String		COL_RESOURCE		= StatsManager.T_RESOURCE;
	public final static String		COL_PAGE		    = StatsManager.T_PAGE;
	public final static String		COL_ACTION			= StatsManager.T_RESOURCE_ACTION;
	public final static String		COL_DATE			= StatsManager.T_DATE;
	public final static String		COL_TOTAL			= StatsManager.T_TOTAL;
	public final static String		COL_VISITS			= StatsManager.T_VISITS;
	public final static String		COL_UNIQUEVISITS	= StatsManager.T_UNIQUEVISITS;
	public final static String		COL_DURATION		= StatsManager.T_DURATION;

	private boolean					logInfo					= true;
	private PrefsData				prefsData;
	private ReportDef				reportDef;
	private Report					report;
	private int 					reportRowCount		= -1;

	public ReportsDataProvider(PrefsData prefsData, ReportDef reportDef) {
		this(prefsData, reportDef, true);
	}
	
	public ReportsDataProvider(PrefsData prefsData, ReportDef reportDef, boolean logInfo) {
		Injector.get().inject(this);
		
		this.prefsData = prefsData;
		this.setReportDef(reportDef);
		this.logInfo = logInfo;
		
        // set default sort
		if(!reportDef.getReportParams().isHowSort() || reportDef.getReportParams().getHowSortBy() == null) {
			setSort(COL_USERNAME, SortOrder.ASCENDING);
		}else{
			setSort(reportDef.getReportParams().getHowSortBy(),
					reportDef.getReportParams().getHowSortAscending() ? SortOrder.ASCENDING : SortOrder.DESCENDING);
		}
	}

	public void setReportDef(ReportDef reportDef) {
		this.report = null;
		this.reportRowCount = -1;
		this.reportDef = reportDef;
	}

	public ReportDef getReportDef() {
		return reportDef;
	}

	public Iterator iterator(long first, long count) {
		int end = (int) first + (int) count;
		end = end < size()? (int) size() : end;
		end = end < 0? getReport().getReportData().size() : end;
		return getReport().getReportData().subList( (int) first, end).iterator();
		
	}
	
	public Report getReport() {
		if(report == null) {
			report = Locator.getFacade().getReportManager().getReport(getReportDef(), prefsData.isListToolEventsOnlyAvailableInSite(), null, logInfo);
			if(logInfo && report != null) {
				log.info("Site statistics report generated: "+report.getReportDefinition().toString(false));
			}
		}
		if(report != null) {
			sortReport();
		}
		return report;
	}

	@Override
	public IModel model(Object object) {
		return new Model((Serializable) object);
	}

	@Override
	public long size() {
		if(reportRowCount == -1) {
			reportRowCount = getReport().getReportData().size();
		}
		return reportRowCount;
	}	

	public void sortReport() {
		Collections.sort(report.getReportData(), getReportDataComparator(getSort().getProperty().toString(),
				getSort().isAscending(), Locator.getFacade().getStatsManager(),
				Locator.getFacade().getEventRegistryService(), Locator.getFacade().getUserDirectoryService()));
	}
	
	public final Comparator<Stat> getReportDataComparator(final String fieldName, final boolean sortAscending, 
			final StatsManager SST_sm, final EventRegistryService SST_ers, final UserDirectoryService M_uds) {
		return new Comparator<Stat>() {
			private transient Collator collator= Collator.getInstance();
			{
				try{
					collator= new RuleBasedCollator(((RuleBasedCollator)Collator.getInstance()).getRules().replaceAll("<'\u005f'", "<' '<'\u005f'"));
				}catch(ParseException e){
				    log.error("Unable to create RuleBasedCollator");
				}		
			}			
			
			public int compare(Stat r1, Stat r2) {
				if(fieldName.equals(COL_SITE)){
					String s1 = Locator.getFacade().getSiteService().getSiteDisplay(r1.getSiteId()).toLowerCase();
					String s2 = Locator.getFacade().getSiteService().getSiteDisplay(r2.getSiteId()).toLowerCase();
					int res = collator.compare(s1, s2);
					if(sortAscending)
						return res;
					else return -res;
				}else if(fieldName.equals(COL_USERID)){
					String s1;
					try{
						s1 = M_uds.getUser(r1.getUserId()).getDisplayId();
					}catch(UserNotDefinedException e){
						s1 = "-";
					}
					String s2;
					try{
						s2 = M_uds.getUser(r2.getUserId()).getDisplayId();
					}catch(UserNotDefinedException e){
						s2 = "-";
					}
					int res = collator.compare(s1, s2);
					if(sortAscending)
						return res;
					else return -res;
				}else if(fieldName.equals(COL_USERNAME)){
					String s1 = Locator.getFacade().getStatsManager().getUserNameForDisplay(r1.getUserId()).toLowerCase();
					String s2 = Locator.getFacade().getStatsManager().getUserNameForDisplay(r2.getUserId()).toLowerCase();
					int res = collator.compare(s1, s2);
					if(sortAscending)
						return res;
					else return -res;
				}else if(fieldName.equals(COL_EVENT)){
					EventStat es1 = (EventStat) r1;
					EventStat es2 = (EventStat) r2;
					String s1 = SST_ers.getEventName(es1.getEventId()).toLowerCase();
					String s2 = SST_ers.getEventName(es2.getEventId()).toLowerCase();
					int res = collator.compare(s1, s2);
					if(sortAscending)
						return res;
					else return -res;
				}else if(fieldName.equals(COL_TOOL)){
					EventStat es1 = (EventStat) r1;
					EventStat es2 = (EventStat) r2;
					String s1 = SST_ers.getToolName(es1.getToolId()).toLowerCase();
					String s2 = SST_ers.getToolName(es2.getToolId()).toLowerCase();
					int res = collator.compare(s1, s2);
					if(sortAscending)
						return res;
					else return -res;
				}else if(fieldName.equals(COL_RESOURCE)){
					ResourceStat rs1 = (ResourceStat) r1;
					ResourceStat rs2 = (ResourceStat) r2;
					String s1 = SST_sm.getResourceName(rs1.getResourceRef()).toLowerCase();
					String s2 = SST_sm.getResourceName(rs2.getResourceRef()).toLowerCase();
					int res = collator.compare(s1, s2);
					if(sortAscending)
						return res;
					else return -res;
				}else if(fieldName.equals(COL_ACTION)){
					ResourceStat rs1 = (ResourceStat) r1;
					ResourceStat rs2 = (ResourceStat) r2;
					String s1 = ((String) rs1.getResourceAction()).toLowerCase();
					String s2 = ((String) rs2.getResourceAction()).toLowerCase();
					int res = collator.compare(s1, s2);
					if(sortAscending)
						return res;
					else return -res;
				}else if(fieldName.equals(COL_DATE)){
					int res = r1.getDate() != null ? r1.getDate().compareTo(r2.getDate()) : -1;
					if(sortAscending)
						return res;
					else return -res;
				}else if(fieldName.equals(COL_TOTAL)){
					int res = Long.valueOf(r1.getCount()).compareTo(Long.valueOf(r2.getCount()));
					if(sortAscending)
						return res;
					else return -res;
				}else if(fieldName.equals(COL_VISITS)){
					SiteVisits sv1 = (SiteVisits) r1;
					SiteVisits sv2 = (SiteVisits) r2;
					int res = Long.valueOf(sv1.getTotalVisits()).compareTo(Long.valueOf(sv2.getTotalVisits()));
					if(sortAscending)
						return res;
					else return -res;
				}else if(fieldName.equals(COL_UNIQUEVISITS)){
					SiteVisits sv1 = (SiteVisits) r1;
					SiteVisits sv2 = (SiteVisits) r2;
					int res = Long.valueOf(sv1.getTotalUnique()).compareTo(Long.valueOf(sv2.getTotalUnique()));
					if(sortAscending)
						return res;
					else return -res;
				}else if(fieldName.equals(COL_DURATION)){
					SitePresence sv1 = (SitePresence) r1;
					SitePresence sv2 = (SitePresence) r2;
					int res = Long.valueOf(sv1.getDuration()).compareTo(Long.valueOf(sv2.getDuration()));
					if(sortAscending)
						return res;
					else return -res;
				}
				return 0;
			}
		};
	}

}
