package org.sakaiproject.sitestats.tool.wicket.providers;

import java.io.Serializable;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.sitestats.api.EventStat;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.ResourceStat;
import org.sakaiproject.sitestats.api.SiteVisits;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;


public class ReportsDataProvider extends SortableSearchableDataProvider {
	private static final long		serialVersionUID	= 1L;
	private static Log				LOG					= LogFactory.getLog(ReportsDataProvider.class);
	public final static String		COL_SITE			= StatsManager.T_SITE;
	public final static String		COL_USERID			= StatsManager.T_USER;
	public final static String		COL_USERNAME		= "userName";
	public final static String		COL_EVENT			= StatsManager.T_EVENT;
	public final static String		COL_TOOL			= StatsManager.T_TOOL;
	public final static String		COL_RESOURCE		= StatsManager.T_RESOURCE;
	public final static String		COL_ACTION			= StatsManager.T_RESOURCE_ACTION;
	public final static String		COL_DATE			= StatsManager.T_DATE;
	public final static String		COL_TOTAL			= StatsManager.T_TOTAL;
	public final static String		COL_VISITS			= StatsManager.T_VISITS;
	public final static String		COL_UNIQUEVISITS	= StatsManager.T_UNIQUEVISITS;

	@SpringBean
	private transient SakaiFacade	facade;
	
	private transient Collator		collator			= Collator.getInstance();

	private boolean					log					= true;
	private PrefsData				prefsData;
	private ReportDef				reportDef;
	private Report					report;
	private int 					reportRowCount		= -1;

	public ReportsDataProvider(PrefsData prefsData, ReportDef reportDef) {
		this(prefsData, reportDef, true);
	}
	
	public ReportsDataProvider(PrefsData prefsData, ReportDef reportDef, boolean log) {
		InjectorHolder.getInjector().inject(this);
		
		this.prefsData = prefsData;
		this.setReportDef(reportDef);
		this.log = log;
		
        // set default sort
		if(!reportDef.getReportParams().isHowSort() || reportDef.getReportParams().getHowSortBy() == null) {
			setSort(COL_USERNAME, true);
		}else{
			setSort(reportDef.getReportParams().getHowSortBy(), reportDef.getReportParams().getHowSortAscending());
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

	public Iterator iterator(int first, int count) {
		int end = first + count;
		end = end < size()? size() : end;
		end = end < 0? getReport().getReportData().size() : end;
		return getReport().getReportData().subList(first, end).iterator();
		
	}
	
	public Report getReport() {
		if(report == null) {
			report = facade.getReportManager().getReport(getReportDef(), prefsData.isListToolEventsOnlyAvailableInSite(), null);
			if(log && report != null) {
				LOG.info("Site statistics report generated: "+report.getReportDefinition().toString(false));
			}
		}
		if(report != null) {
			sortReport();
		}
		return report;
	}

	public IModel model(Object object) {
		return new Model((Serializable) object);
	}

	public int size() {
		if(reportRowCount == -1) {
			reportRowCount = getReport().getReportData().size();
		}
		return reportRowCount;
	}	

	public void sortReport() {
		Collections.sort(report.getReportData(), getReportDataComparator(getSort().getProperty(), getSort().isAscending(), collator, facade.getStatsManager(), facade.getEventRegistryService(), facade.getUserDirectoryService()));
	}
	
	public final Comparator<Stat> getReportDataComparator(final String fieldName, final boolean sortAscending, final Collator collator,
			final StatsManager SST_sm, final EventRegistryService SST_ers, final UserDirectoryService M_uds) {
		return new Comparator<Stat>() {

			public int compare(Stat r1, Stat r2) {
				if(fieldName.equals(COL_SITE)){
					String s1 = facade.getSiteService().getSiteDisplay(r1.getSiteId()).toLowerCase();
					String s2 = facade.getSiteService().getSiteDisplay(r2.getSiteId()).toLowerCase();
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
					String s1;
					try{
						s1 = M_uds.getUser(r1.getUserId()).getDisplayName().toLowerCase();
					}catch(UserNotDefinedException e){
						s1 = "-";
					}
					String s2;
					try{
						s2 = M_uds.getUser(r2.getUserId()).getDisplayName().toLowerCase();
					}catch(UserNotDefinedException e){
						s2 = "-";
					}
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
				}else if(fieldName.equals(COL_EVENT)){
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
				}
				return 0;
			}
		};
	}

}
