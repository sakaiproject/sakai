package org.sakaiproject.sitestats.impl.report.fop;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.EventStat;
import org.sakaiproject.sitestats.api.ResourceStat;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
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
			String siteTitle = M_ss.getSite(report.getReportParams().getSiteId()).getTitle();
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
        
        // set column display info
        setColumnDisplayInfo(report.getReportParams());
        
        // report header
        generateReportDataHeader(report.getReportParams());
        
        // report data
        generateReportData(report.getReportData(), report.getReportParams());
        
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
	
	private void generateReportDataHeader(ReportParams params) throws SAXException {
        if (params == null) {
            throw new NullPointerException("Parameter 'params' must not be null");
        }
        if (handler == null) {
            throw new IllegalStateException("ContentHandler not set");
        }
        
        handler.startElement("datarowheader");
        
        // set column display info
        setColumnDisplayInfo(params);
        
        handler.element("th_id", msgs.getString("th_id"));
        handler.element("th_user", msgs.getString("th_user"));
        handler.element("th_resource", msgs.getString("th_resource"));
        handler.element("th_action", msgs.getString("th_action"));
        handler.element("th_event", msgs.getString("th_event"));	
        handler.element("th_date", msgs.getString("th_date"));
        handler.element("th_lastdate", msgs.getString("th_date"));
        handler.element("th_total", msgs.getString("th_total"));
        
        handler.endElement("datarowheader");
	}

	private void setColumnDisplayInfo(ReportParams params) throws SAXException {
        if (handler == null) {
            throw new IllegalStateException("ContentHandler not set");
        }
        handler.element("what", params.getWhat());
        handler.element("who", params.getWho());
		handler.element("showUser", String.valueOf(M_rm.isReportColumnAvailable(params, StatsManager.T_USER)));
        handler.element("showEvent", String.valueOf(M_rm.isReportColumnAvailable(params, StatsManager.T_EVENT)));
        handler.element("showResource", String.valueOf(M_rm.isReportColumnAvailable(params, StatsManager.T_RESOURCE)));
        handler.element("showResourceAction", String.valueOf(M_rm.isReportColumnAvailable(params, StatsManager.T_RESOURCE_ACTION)));
        handler.element("showDate", String.valueOf(M_rm.isReportColumnAvailable(params, StatsManager.T_DATE)));
        handler.element("showLastDate", String.valueOf(M_rm.isReportColumnAvailable(params, StatsManager.T_LASTDATE)));
        handler.element("showTotal", String.valueOf(M_rm.isReportColumnAvailable(params, StatsManager.T_TOTAL)));
	}

	private void generateReportData(List<Stat> data, ReportParams params) throws SAXException {
        if (data == null || params == null) {
            throw new NullPointerException("Parameter 'data', 'params' must not be null");
        }
        if (handler == null) {
            throw new IllegalStateException("ContentHandler not set");
        }
        
        boolean showUser = M_rm.isReportColumnAvailable(params, StatsManager.T_USER);
        boolean showEvent = M_rm.isReportColumnAvailable(params, StatsManager.T_EVENT);
        boolean showResource = M_rm.isReportColumnAvailable(params, StatsManager.T_RESOURCE);
        boolean showResourceAction = M_rm.isReportColumnAvailable(params, StatsManager.T_RESOURCE_ACTION);
        boolean showDate = M_rm.isReportColumnAvailable(params, StatsManager.T_DATE);
        boolean showLastDate = M_rm.isReportColumnAvailable(params, StatsManager.T_LASTDATE);
        boolean showTotal = M_rm.isReportColumnAvailable(params, StatsManager.T_TOTAL);        

        Iterator<Stat> i = data.iterator();
        while(i.hasNext()){
        	Stat cs = i.next();
            handler.startElement("datarow");
            
            // set column display info
            setColumnDisplayInfo(params);
            
            if(showUser) {
	        	String userId = null;
	        	String userName = null;
	        	String id = cs.getUserId();
	        	if (id != null) {
	    			if(("-").equals(id)) {
	    				userId = "-";
	    				userName = msgs.getString("user_anonymous");
	    			}else if(("?").equals(id)) {
	    				userId = "-";
	    				userName = msgs.getString("user_anonymous_access");
	    			}else{
	    				try{
	    					User user = M_uds.getUser(id);
	    					userId = user.getDisplayId();
	    					userName = user.getDisplayName();
	    				}catch(UserNotDefinedException e1){
	    					userId = id;
	    					userName = msgs.getString("user_unknown");
	    				}
	    			}
	    		}else{
	    			userName = msgs.getString("user_unknown");
	    		}
	            handler.element("userid", userId);
	            handler.element("username", userName);
            }
            if(showEvent) {
            	EventStat es = (EventStat) cs;
            	String eventRef = es.getEventId();
            	handler.element("event", M_ers.getEventName(eventRef == null? "" : eventRef));
            }
            if(showResource) {
            	ResourceStat rs = (ResourceStat) cs;
	            String resName = M_sm.getResourceName(rs.getResourceRef());
	        	handler.element("resource", resName == null? "" : resName);
	            handler.element("resourceimg", "library://" + M_sm.getResourceImageLibraryRelativePath(rs.getResourceRef()));            	
            }
            if(showResourceAction) {
            	ResourceStat rs = (ResourceStat) cs;
            	String resAction = rs.getResourceAction();
	        	handler.element("action", resAction == null? "" : msgs.getString("action_"+resAction) );	            
            }
            if(showDate) {
            	java.util.Date date = cs.getDate();
	            handler.element("date", date == null? "" :M_ts.newTime(date.getTime()).toStringLocalDate());	            
            }
            if(showLastDate) {
            	java.util.Date date = cs.getDate();
	            handler.element("lastdate", date == null? "" :M_ts.newTime(date.getTime()).toStringLocalDate());
            }
            if(showTotal) {
	            handler.element("total", String.valueOf(cs.getCount()));
            }
            
            handler.endElement("datarow");
        }
	}
}
