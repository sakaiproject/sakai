package org.sakaiproject.sitestats.impl.report.fop;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.CommonStatGrpByDate;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ReportXMLReader extends AbstractObjectReader {	
	/** Resource bundle */
	private ResourceLoader			msgs		= new ResourceLoader("Messages");
	
	/** Sakai services */
	private TimeService				M_ts		= (TimeService) ComponentManager.get(TimeService.class.getName());
	private SiteService				M_ss		= (SiteService) ComponentManager.get(SiteService.class.getName());
	private ToolManager				M_tm		= (ToolManager) ComponentManager.get(ToolManager.class.getName());
	private UserDirectoryService	M_uds		= (UserDirectoryService) ComponentManager.get(UserDirectoryService.class.getName());
	private StatsManager			M_sm		= (StatsManager) ComponentManager.get(StatsManager.class.getName());
	private EventRegistryService	M_ers		= (EventRegistryService) ComponentManager.get(EventRegistryService.class.getName());
	private ReportManager			M_rm		= (ReportManager) ComponentManager.get(ReportManager.class.getName());


	@Override
	public void parse(InputSource input) throws IOException, SAXException {
		if (input instanceof ReportInputSource) {
            parse(((ReportInputSource)input).getReport());
        } else {
            throw new SAXException("Unsupported InputSource specified. "  + "Must be a ReportInputSource");
        }
	}

	public void parse(Report report) throws SAXException {
		if (report == null) {
            throw new NullPointerException("Parameter report must not be null");
        }
        if (handler == null) {
            throw new IllegalStateException("ContentHandler not set");
        }
        
        //Start the document
        handler.startDocument();
        
        //Generate SAX events for the Report
        generateReport(report);
        
        //End the document
        handler.endDocument();
	}
	
	
	
	protected void generateReport(Report report) throws SAXException {
        if (report == null) {
            throw new NullPointerException("Parameter report must not be null");
        }
        if (handler == null) {
            throw new IllegalStateException("ContentHandler not set");
        }
        
        handler.startElement("report");        
        String reportTitle = msgs.getString("reportres_title");
		try{
			String siteTitle = M_ss.getSite(M_tm.getCurrentPlacement().getContext()).getTitle();
			reportTitle += " (" + siteTitle + ")";
		}catch(IdUnusedException e){
			// ignore
		}
        handler.element("title", reportTitle);
        
        // activity based on
		generateReportSummaryHeaderRow(msgs.getString("reportres_summ_act_basedon") ,M_rm.getReportFormattedParams().getReportActivityBasedOn(report));
		String reportResourceAction = M_rm.getReportFormattedParams().getReportResourceAction(report);
		// resources action
		if(reportResourceAction != null)
			generateReportSummaryHeaderRow(M_rm.getReportFormattedParams().getReportResourceActionTitle(report) ,reportResourceAction);
		// activity selection
		String reportActivitySelection = M_rm.getReportFormattedParams().getReportActivitySelection(report);
		if(reportActivitySelection != null)
			generateReportSummaryHeaderRow(M_rm.getReportFormattedParams().getReportActivitySelectionTitle(report) ,reportActivitySelection);
		// time period
		generateReportSummaryHeaderRow(msgs.getString("reportres_summ_timeperiod") ,M_rm.getReportFormattedParams().getReportTimePeriod(report));
		// user selection type
		generateReportSummaryHeaderRow(msgs.getString("reportres_summ_usr_selectiontype") ,M_rm.getReportFormattedParams().getReportUserSelectionType(report));
		// user selection
		String reportUserSelection = M_rm.getReportFormattedParams().getReportUserSelection(report);
		if(reportUserSelection != null)
			generateReportSummaryHeaderRow(M_rm.getReportFormattedParams().getReportUserSelectionTitle(report) ,reportUserSelection);
		// report timestamp
        generateReportSummaryHeaderRow(msgs.getString("reportres_summ_generatedon") ,M_rm.getReportFormattedParams().getReportGenerationDate(report));
        
        String what = report.getReportParams().getWhat();
        handler.element("what", what);
        String who = report.getReportParams().getWho();
        handler.element("who", who);
        
        // report header
        generateReportDataHeader(what, who);
        
        // report data
        generateReportData(report.getReportData(), what, who);
        
        handler.endElement("report");
    }

	protected void generateReportSummaryHeaderRow(String label, String value) throws SAXException {
        if (label == null || value == null) {
            throw new NullPointerException("Parameter label and value must not be null");
        }
        if (handler == null) {
            throw new IllegalStateException("ContentHandler not set");
        }
        
        handler.startElement("summaryheader");
        handler.element("label", label);
        handler.element("value", value);
        handler.endElement("summaryheader");
    }
	
	private void generateReportDataHeader(String what, String who) throws SAXException {
        if (what == null || who == null) {
            throw new NullPointerException("Parameter what and who must not be null");
        }
        if (handler == null) {
            throw new IllegalStateException("ContentHandler not set");
        }
        
        handler.startElement("datarowheader");
        handler.element("what", what);
        handler.element("who", who);
        handler.element("th_id", msgs.getString("th_id"));
        handler.element("th_user", msgs.getString("th_user"));
        if(what.equals(ReportManager.WHAT_RESOURCES)){
            handler.element("th_resource", msgs.getString("th_resource"));
            handler.element("th_action", msgs.getString("th_action"));
        }else{
            handler.element("th_event", msgs.getString("th_event"));	
        }
        handler.element("th_date", msgs.getString("th_date"));
        handler.element("th_total", msgs.getString("th_total"));
        handler.endElement("datarowheader");
	}

	private void generateReportData(List<CommonStatGrpByDate> data, String what, String who) throws SAXException {
        if (data == null || what == null || who == null) {
            throw new NullPointerException("Parameter data, what and who must not be null");
        }
        if (handler == null) {
            throw new IllegalStateException("ContentHandler not set");
        }

        Iterator<CommonStatGrpByDate> i = data.iterator();
        while(i.hasNext()){
        	CommonStatGrpByDate cs = i.next();
            handler.startElement("datarow");
            handler.element("what", what);
            handler.element("who", who);
            
            // user id and name
        	String userId = null;
        	String userName = null;
            try{
				User user = M_uds.getUser(cs.getUserId());
				userId = user.getDisplayId();
				userName = user.getDisplayName();
			}catch(UserNotDefinedException e){
				userId = cs.getUserId();
				userName = "";
			}
            handler.element("userid", userId);
            handler.element("username", userName);
            
            if(!who.equals(ReportManager.WHO_NONE)) {
	            // event or (resource and action)
	            if(what.equals(ReportManager.WHAT_RESOURCES)){
		            String resName = M_sm.getResourceName(cs.getRef());
	            	String resAction = cs.getRefAction();
	            	handler.element("resource", resName == null? "" : resName);
		            handler.element("action", resAction == null? "" : msgs.getString("action_"+resAction) );
		            handler.element("resourceimg", "library://" + M_sm.getResourceImageLibraryRelativePath(cs.getRef()));	            	
	            }else{
	            	String eventRef = cs.getRef();
	            	handler.element("event", M_ers.getEventName(eventRef == null? "" : eventRef));
	            }
	            
	            // last date
	            java.util.Date date = cs.getDate();
	            handler.element("lastdate", date == null? "" :M_ts.newTime(date.getTime()).toStringLocalDate());
	            
	            // count
	            handler.element("count", String.valueOf(cs.getCount()));
            }
            
            handler.endElement("datarow");
        }
	}
}
