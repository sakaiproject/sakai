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
package org.sakaiproject.sitestats.impl.report.fop;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.EventStat;
import org.sakaiproject.sitestats.api.ResourceStat;
import org.sakaiproject.sitestats.api.SitePresence;
import org.sakaiproject.sitestats.api.SiteVisits;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.Util;
import org.sakaiproject.sitestats.api.chart.ChartService;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ReportXMLReader extends AbstractObjectReader {	
	/** Resource bundle */
	private ResourceLoader			msgs		= new ResourceLoader("Messages");
	
	/** Date formatters. */
	private SimpleDateFormat		dateMonthFrmt = new SimpleDateFormat("yyyy-MM");
	private SimpleDateFormat		dateYearFrmt  = new SimpleDateFormat("yyyy");
	
	/** Sakai services */
	private TimeService				M_ts		= (TimeService) ComponentManager.get(TimeService.class.getName());
	private SiteService				M_ss		= (SiteService) ComponentManager.get(SiteService.class.getName());
	private UserDirectoryService	M_uds		= (UserDirectoryService) ComponentManager.get(UserDirectoryService.class.getName());
	private StatsManager			M_sm		= (StatsManager) ComponentManager.get(StatsManager.class.getName());
	private EventRegistryService	M_ers		= (EventRegistryService) ComponentManager.get(EventRegistryService.class.getName());
	private ReportManager			M_rm		= (ReportManager) ComponentManager.get(ReportManager.class.getName());
	private ChartService			M_cs		= (ChartService) ComponentManager.get(ChartService.class.getName());


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
        
        String reportTitle = M_rm.getReportFormattedParams().getReportTitle(report);
        if(reportTitle != null && reportTitle.trim().length() != 0) {
        	reportTitle = msgs.getString("reportres_title_detailed").replaceAll("\\$\\{title\\}", reportTitle);    		
        }else{
        	reportTitle = msgs.getString("reportres_title");
        }
        handler.element("title", reportTitle);

		// description
        String reportDescription = M_rm.getReportFormattedParams().getReportDescription(report);
		if(reportDescription != null) {
			generateReportSummaryHeaderRow(msgs.getString("reportres_summ_description") ,reportDescription);
		}
		// site
        String reportSite = M_rm.getReportFormattedParams().getReportSite(report);
		if(reportSite != null) {
			generateReportSummaryHeaderRow(msgs.getString("reportres_summ_site") ,reportSite);
		}
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
        setColumnDisplayInfo(report.getReportDefinition().getReportParams());
        
        
        // display chart and/or table?
        /*ReportParams params = report.getReportDefinition().getReportParams();
        boolean showChart = ReportManager.HOW_PRESENTATION_BOTH.equals(params.getHowPresentationMode())
				|| ReportManager.HOW_PRESENTATION_CHART.equals(params.getHowPresentationMode());
        boolean showTable = ReportManager.HOW_PRESENTATION_BOTH.equals(params.getHowPresentationMode())
				|| ReportManager.HOW_PRESENTATION_TABLE.equals(params.getHowPresentationMode());*/
        boolean showChart = false;
        boolean showTable = true;
        handler.element("showChart", String.valueOf(showChart));
        handler.element("showTable", String.valueOf(showTable));
        
        // report chart
        if(showChart) {
        	// TODO Embbed image in fop
        	generateReportChart(report);
        }
        
        // report table
        if(showTable) {
            generateReportDataHeader(report.getReportDefinition().getReportParams());
        	generateReportTable(report.getReportData(), report.getReportDefinition().getReportParams());
        }
        
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

        handler.element("th_site", msgs.getString("th_site"));
        handler.element("th_id", msgs.getString("th_id"));
        handler.element("th_user", msgs.getString("th_user"));
        handler.element("th_resource", msgs.getString("th_resource"));
        handler.element("th_action", msgs.getString("th_action"));
        handler.element("th_tool", msgs.getString("th_tool"));	
        handler.element("th_event", msgs.getString("th_event"));	
        handler.element("th_date", msgs.getString("th_date"));
        handler.element("th_lastdate", msgs.getString("th_date"));
        handler.element("th_total", msgs.getString("th_total"));
        handler.element("th_visits", msgs.getString("th_visits"));
        handler.element("th_uniquevisitors", msgs.getString("th_uniquevisitors"));
        handler.element("th_duration", msgs.getString("th_duration") + " (" + msgs.getString("minutes_abbr") + ")");
        
        handler.endElement("datarowheader");
	}

	private void setColumnDisplayInfo(ReportParams params) throws SAXException {
        if (handler == null) {
            throw new IllegalStateException("ContentHandler not set");
        }
        handler.element("what", params.getWhat());
        handler.element("who", params.getWho());
		handler.element("showSite", String.valueOf(M_rm.isReportColumnAvailable(params, StatsManager.T_SITE)));
		handler.element("showUser", String.valueOf(M_rm.isReportColumnAvailable(params, StatsManager.T_USER)));
		handler.element("showTool", String.valueOf(M_rm.isReportColumnAvailable(params, StatsManager.T_TOOL)));
        handler.element("showEvent", String.valueOf(M_rm.isReportColumnAvailable(params, StatsManager.T_EVENT)));
        handler.element("showResource", String.valueOf(M_rm.isReportColumnAvailable(params, StatsManager.T_RESOURCE)));
        handler.element("showResourceAction", String.valueOf(M_rm.isReportColumnAvailable(params, StatsManager.T_RESOURCE_ACTION)));
        handler.element("showDate", String.valueOf(M_rm.isReportColumnAvailable(params, StatsManager.T_DATE) || M_rm.isReportColumnAvailable(params, StatsManager.T_DATEMONTH) || M_rm.isReportColumnAvailable(params, StatsManager.T_DATEYEAR)));
        handler.element("showLastDate", String.valueOf(M_rm.isReportColumnAvailable(params, StatsManager.T_LASTDATE)));
        handler.element("showTotal", String.valueOf(M_rm.isReportColumnAvailable(params, StatsManager.T_TOTAL)));
        handler.element("showTotalVisits", String.valueOf(M_rm.isReportColumnAvailable(params, StatsManager.T_VISITS)));
        handler.element("showTotalUnique", String.valueOf(M_rm.isReportColumnAvailable(params, StatsManager.T_UNIQUEVISITS)));
        handler.element("showDuration", String.valueOf(M_rm.isReportColumnAvailable(params, StatsManager.T_DURATION)));
	}

	private void generateReportChart(Report report) throws SAXException {
        if (report == null) {
            throw new NullPointerException("Parameter 'report'must not be null");
        }
        if (handler == null) {
            throw new IllegalStateException("ContentHandler not set");
        }
        
        // generate chart
        /*PrefsData prefsData = M_sm.getPreferences(report.getReportDefinition().getReportParams().getSiteId(), false);
		int width = 1024;
		int height = 768;
		BufferedImage img = M_cs.generateChart(
				report, width, height,
				prefsData.isChartIn3D(), prefsData.getChartTransparency(),
				prefsData.isItemLabelsVisible()
		);*/
        //handler.element("chart", "sitestats://" + M_ers.getToolIcon(toolId));
	}
	
	private void generateReportTable(List<? extends Stat> data, ReportParams params) throws SAXException {
        if (data == null || params == null) {
            throw new NullPointerException("Parameter 'data', 'params' must not be null");
        }
        if (handler == null) {
            throw new IllegalStateException("ContentHandler not set");
        }
        
        final Map<String,ToolInfo> eventIdToolMap = M_ers.getEventIdToolMap();
		
        boolean showSite = M_rm.isReportColumnAvailable(params, StatsManager.T_SITE);
        boolean showUser = M_rm.isReportColumnAvailable(params, StatsManager.T_USER);
        boolean showTool = M_rm.isReportColumnAvailable(params, StatsManager.T_TOOL);
        boolean showEvent = M_rm.isReportColumnAvailable(params, StatsManager.T_EVENT);
        boolean showResource = M_rm.isReportColumnAvailable(params, StatsManager.T_RESOURCE);
        boolean showResourceAction = M_rm.isReportColumnAvailable(params, StatsManager.T_RESOURCE_ACTION);
        boolean showDate = M_rm.isReportColumnAvailable(params, StatsManager.T_DATE) || M_rm.isReportColumnAvailable(params, StatsManager.T_DATEMONTH) || M_rm.isReportColumnAvailable(params, StatsManager.T_DATEYEAR);
        boolean showLastDate = M_rm.isReportColumnAvailable(params, StatsManager.T_LASTDATE);
        boolean showTotal = M_rm.isReportColumnAvailable(params, StatsManager.T_TOTAL);
        boolean showTotalVisits = M_rm.isReportColumnAvailable(params, StatsManager.T_VISITS);
        boolean showTotalUnique = M_rm.isReportColumnAvailable(params, StatsManager.T_UNIQUEVISITS);
        boolean showDuration = M_rm.isReportColumnAvailable(params, StatsManager.T_DURATION);

        Iterator<? extends Stat> i = data.iterator();
        while(i.hasNext()){
        	Stat cs = i.next();
            handler.startElement("datarow");
            
            // set column display info
            setColumnDisplayInfo(params);
            
            if(showSite) {
            	String siteId = cs.getSiteId();
            	String site = null;
				try{
					site = M_ss.getSite(siteId).getTitle();
				}catch(IdUnusedException e){
					site = msgs.getString("site_unknown");
				}
            	handler.element("site", site);
            }
            if(showUser) {
	        	String userId = null;
	        	String userName = null;
	        	String id = cs.getUserId();
	        	if (id != null) {
	    			if(("-").equals(id)) {
	    				userId = "-";
	    				userName = msgs.getString("user_anonymous");
	    			}else if(EventTrackingService.UNKNOWN_USER.equals(id)) {
	    				userId = "-";
	    				userName = msgs.getString("user_anonymous_access");
	    			}else{
	    				try{
	    					User user = M_uds.getUser(id);
	    					userId = user.getDisplayId();
	    					userName = M_sm.getUserNameForDisplay(user);
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
            if(showTool) {
            	EventStat es = (EventStat) cs;
            	String toolId = es.getToolId();
            	handler.element("tool", M_ers.getToolName(toolId == null? "" : toolId));
            	handler.element("showToolIcon", "true");
            	handler.element("toolicon", "sitestats://" + M_ers.getToolIcon(toolId));            	
            }
            if(showEvent) {
            	EventStat es = (EventStat) cs;
            	String eventRef = es.getEventId();
            	handler.element("event", M_ers.getEventName(eventRef == null? "" : eventRef));
            	ToolInfo toolInfo = eventIdToolMap.get(eventRef);
            	if(toolInfo != null && !showTool) {
            		handler.element("showToolEventIcon", "true");
            		String toolId = toolInfo.getToolId();
            		handler.element("tooleventicon", "sitestats://" + M_ers.getToolIcon(toolId));
            	}else{
            		handler.element("showToolEventIcon", "false");
            	}
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
            	if(M_rm.isReportColumnAvailable(params, StatsManager.T_DATE)) {
            		handler.element("date", date == null? "" :M_ts.newTime(date.getTime()).toStringLocalDate());
            	}else if(M_rm.isReportColumnAvailable(params, StatsManager.T_DATEMONTH)) {
            		handler.element("date", date == null? "" :dateMonthFrmt.format(date));
            	}else if(M_rm.isReportColumnAvailable(params, StatsManager.T_DATEYEAR)) {
            		handler.element("date", date == null? "" :dateYearFrmt.format(date));
            	}
            }
            if(showLastDate) {
            	java.util.Date date = cs.getDate();
	            handler.element("lastdate", date == null? "" :M_ts.newTime(date.getTime()).toStringLocalDate());
            }
            if(showTotal) {
	            handler.element("total", String.valueOf(cs.getCount()));
            }
            if(showTotalVisits) {
            	SiteVisits ss = (SiteVisits) cs;
	            handler.element("totalVisits", String.valueOf(ss.getTotalVisits()));
            }
            if(showTotalUnique) {
            	SiteVisits ss = (SiteVisits) cs;
	            handler.element("totalUnique", String.valueOf(ss.getTotalUnique()));
            }
            if(showDuration) {
            	SitePresence ss = (SitePresence) cs;
            	double durationInMin = ss.getDuration() == 0 ? 0 : Util.round((double)ss.getDuration() / 1000 / 60, 1); // in minutes
	            handler.element("duration", String.valueOf(durationInMin));
            }
            
            handler.endElement("datarow");
        }
        
        // empty report
        if(data.size() == 0) {        	
        	String messageNoData = msgs.getString("no_data");

        	handler.startElement("datarow");
            // set column display info
            setColumnDisplayInfo(params);
            
            if(showSite) {
            	handler.element("site", messageNoData);
            	messageNoData = "";
            }
            if(showUser) {
	            handler.element("userid", messageNoData);
            	messageNoData = "";
	            handler.element("username", messageNoData);
            }
            if(showTool) {
            	handler.element("tool", messageNoData);
            	messageNoData = "";
            	handler.element("showToolIcon", "");
            	handler.element("toolicon", "");            	
            }
            if(showEvent) {
            	handler.element("event", messageNoData);
            	messageNoData = "";
            	handler.element("showToolEventIcon", "false");
            }
            if(showResource) {
	        	handler.element("resource", messageNoData);
            	messageNoData = "";
	            handler.element("resourceimg", "");            	
            }
            if(showResourceAction) {
            	handler.element("action", messageNoData);
            	messageNoData = "";	            
            }
            if(showDate) {
            	handler.element("date", messageNoData);
            	messageNoData = "";
            }
            if(showLastDate) {
            	handler.element("lastdate", messageNoData);
            	messageNoData = "";
            }
            if(showTotal) {
	            handler.element("total", messageNoData);
            	messageNoData = "";
            }
            if(showTotalVisits) {
	            handler.element("totalVisits", messageNoData);
            	messageNoData = "";
            }
            if(showTotalUnique) {
	            handler.element("totalUnique", messageNoData);
            	messageNoData = "";
            }  
            if(showDuration) {
	            handler.element("duration", messageNoData);
            	messageNoData = "";
            }           
            handler.endElement("datarow");
        }
	}
}
