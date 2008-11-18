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
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;


public class ReportsDataProvider extends SortableSearchableDataProvider {
	private static final long		serialVersionUID	= 1L;
	private static Log				LOG					= LogFactory.getLog(ReportsDataProvider.class);
	public final static String		COL_USERID			= "userId";
	public final static String		COL_USERNAME		= "userName";
	public final static String		COL_EVENT			= "event";
	public final static String		COL_RESOURCE		= "resourceRef";
	public final static String		COL_ACTION			= "resourceAction";
	public final static String		COL_DATE			= "date";
	public final static String		COL_TOTAL			= "count";

	@SpringBean
	private transient SakaiFacade	facade;
	
	private transient Collator		collator			= Collator.getInstance();

	private String					siteId;
	private PrefsData				prefsData;
	private ReportParams			reportParams;
	private Report					report;
	private int 					reportRowCount		= -1;

	public ReportsDataProvider(String siteId, PrefsData prefsData, ReportParams reportParams) {
		InjectorHolder.getInjector().inject(this);
		
		this.siteId = siteId;
		this.prefsData = prefsData;
		this.reportParams = reportParams;
		
        // set default sort
        setSort(COL_USERNAME, true);
	}

	public Iterator iterator(int first, int count) {
		int end = first + count;
		end = end < size()? size() : end;
		end = end < 0? getReport().getReportData().size() : end;
		return getReport().getReportData().subList(first, end).iterator();
		
	}
	
	public Report getReport() {
		if(report == null) {
			report = facade.getReportManager().getReport(siteId, prefsData.isListToolEventsOnlyAvailableInSite(), reportParams, null, null, true);
			LOG.info("Site statistics report generated: "+report.getReportParams().toString());			
		}
		sortReport();
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
	
	public static final Comparator<Stat> getReportDataComparator(final String fieldName, final boolean sortAscending, final Collator collator,
			final StatsManager SST_sm, final EventRegistryService SST_ers, final UserDirectoryService M_uds) {
		return new Comparator<Stat>() {

			public int compare(Stat r1, Stat r2) {
				if(fieldName.equals(COL_USERID)){
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
						if(sortAscending) return res;
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
						if(sortAscending) return res;
						else return -res;
					}else if(fieldName.equals(COL_EVENT)){
						EventStat es1 = (EventStat) r1;
						EventStat es2 = (EventStat) r2;
						String s1 = SST_ers.getEventName(es1.getEventId()).toLowerCase();
						String s2 = SST_ers.getEventName(es2.getEventId()).toLowerCase();
						int res = collator.compare(s1, s2);
						if(sortAscending) return res;
						else return -res;
					}else if(fieldName.equals(COL_RESOURCE)){
						ResourceStat rs1 = (ResourceStat) r1;
						ResourceStat rs2 = (ResourceStat) r2;
						String s1 = SST_sm.getResourceName(rs1.getResourceRef()).toLowerCase();
						String s2 = SST_sm.getResourceName(rs2.getResourceRef()).toLowerCase();
						int res = collator.compare(s1, s2);
						if(sortAscending) return res;
						else return -res;
					}else if(fieldName.equals(COL_ACTION)){
						ResourceStat rs1 = (ResourceStat) r1;
						ResourceStat rs2 = (ResourceStat) r2;
						String s1 = ((String) rs1.getResourceAction()).toLowerCase();
						String s2 = ((String) rs2.getResourceAction()).toLowerCase();
						int res = collator.compare(s1, s2);
						if(sortAscending) return res;
						else return -res;
					}else if(fieldName.equals(COL_DATE)){
						int res = r1.getDate().compareTo(r2.getDate());
						if(sortAscending) return res;
						else return -res;
					}else if(fieldName.equals(COL_TOTAL)){
						int res = Long.valueOf(r1.getCount()).compareTo(Long.valueOf(r2.getCount()));
						if(sortAscending) return res;
						else return -res;
					}
					return 0;
			}
		};
	}

}
