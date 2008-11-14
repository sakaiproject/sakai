package org.sakaiproject.sitestats.impl.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.CommonStatGrpByDate;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportFormattedParams;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.impl.report.fop.LibraryURIResolver;
import org.sakaiproject.sitestats.impl.report.fop.ReportInputSource;
import org.sakaiproject.sitestats.impl.report.fop.ReportXMLReader;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.core.io.ClassPathResource;


public class ReportManagerImpl implements ReportManager {
	private Log						LOG				= LogFactory.getLog(ReportManagerImpl.class);
	private static ResourceLoader	msgs			= new ResourceLoader("Messages");
	private ReportFormattedParams	formattedParams	= new ReportFormattedParamsImpl();

	/** FOP */
	private FopFactory				fopFactory		= FopFactory.newInstance();
	private Templates				cachedXmlFoXSLT	= null;
	private static final String		XML_FO_XSL_FILE	= "xmlReportToFo.xsl";

	/** Spring bean members */

	/** Sakai services */
	private StatsManager			M_sm;
	private EventRegistryService	M_ers;
	private SiteService				M_ss;
	private UserDirectoryService	M_uds;
	private ContentHostingService	M_chs;
	private ToolManager				M_tm;
	private TimeService				M_ts;
	

	// ################################################################
	// Spring bean methods
	// ################################################################
	public void setStatsManager(StatsManager statsManager) {
		this.M_sm = statsManager;
	}

	public void setEventRegistryService(EventRegistryService eventRegistryService) {
		this.M_ers = eventRegistryService;
	}
	
	public void setSiteService(SiteService siteService) {
		this.M_ss = siteService;
	}

	public void setUserService(UserDirectoryService userService) {
		this.M_uds = userService;
	}

	public void setContentService(ContentHostingService contentService) {
		this.M_chs = contentService;
	}
	
	public void setToolManager(ToolManager toolManager) {
		this.M_tm = toolManager;
	}
	
	public void setTimeService(TimeService timeService) {
		this.M_ts = timeService; 
	}
	

	// ################################################################
	// Interface implementation
	// ################################################################
	public Report getReport(String siteId, PrefsData prefsdata, ReportParams params) {
		return getReport(siteId, prefsdata, params, null, null, null, true);
	}
	
	public int getReportRowCount(String siteId, PrefsData prefsdata, ReportParams params, PagingPosition pagingPosition, String groupBy, String sortBy, boolean sortAscending) {
		ReportProcessedParams rpp = processReportParams(siteId, prefsdata, params, pagingPosition, groupBy, sortBy, sortAscending);
		if(params.getWhat().equals(ReportManager.WHAT_RESOURCES)){
			return M_sm.getResourceStatsRowCount(siteId, rpp.resourceAction, rpp.resourceIds, rpp.iDate, rpp.fDate, rpp.userIds, rpp.inverseUserSelection, pagingPosition, null, sortBy, sortAscending);
		}else{
			return M_sm.getEventStatsRowCount(siteId, rpp.events, rpp.iDate, rpp.fDate, rpp.userIds, rpp.inverseUserSelection, pagingPosition, null, sortBy, sortAscending);
		}
	}
	
	public Report getReport(String siteId, PrefsData prefsdata, ReportParams params, PagingPosition pagingPosition, String groupBy, String sortBy, boolean sortAscending) {
		ReportProcessedParams rpp = processReportParams(siteId, prefsdata, params, pagingPosition, groupBy, sortBy, sortAscending);

		// generate report
		Report report = new ReportImpl();
		List<CommonStatGrpByDate> data = null;
		if(params.getWhat().equals(ReportManager.WHAT_RESOURCES)){
			//data = M_sm.getResourceStatsGrpByDateAndAction(siteId, rpp.resourceAction, rpp.resourceIds, rpp.iDate, rpp.fDate, rpp.userIds, rpp.inverseUserSelection, pagingPosition);
			data = M_sm.getResourceStats(siteId, rpp.resourceAction, rpp.resourceIds, rpp.iDate, rpp.fDate, rpp.userIds, rpp.inverseUserSelection, pagingPosition, null, sortBy, sortAscending);
		}else{
			//data = M_sm.getEventStatsGrpByDate(siteId, rpp.events, rpp.iDate, rpp.fDate, rpp.userIds, rpp.inverseUserSelection, pagingPosition);
			data = M_sm.getEventStats(siteId, rpp.events, rpp.iDate, rpp.fDate, rpp.userIds, rpp.inverseUserSelection, pagingPosition, null, sortBy, sortAscending);
		}
		
		// add missing info in report and its parameters
		if(report != null) {
			report.setReportData(data);
			report.setReportParams(params);
			report.setReportGenerationDate(M_ts.newTime());
		}
		params.setWhenFrom(rpp.iDate);
		params.setWhenTo(rpp.fDate);
		params.setWhoUserIds(rpp.userIds);

		// consolidate anonymous events
		//report = consolidateAnonymousEvents(report);

		return report;
	}
	
	private ReportProcessedParams processReportParams(String siteId, PrefsData prefsdata, ReportParams params, PagingPosition pagingPosition, String groupBy, String sortBy, boolean sortAscending) {
		ReportProcessedParams rpp = new ReportProcessedParams();
		
		// what (visits, events, resources)
		rpp.events = new ArrayList<String>();
		if(params.getWhat().equals(ReportManager.WHAT_VISITS)){
			rpp.events.add(StatsManager.SITEVISIT_EVENTID);

		}else if(params.getWhat().equals(ReportManager.WHAT_EVENTS)){
			if(params.getWhatEventSelType().equals(ReportManager.WHAT_EVENTS_BYTOOL)){
				Iterator<ToolInfo> iT = M_ers.getEventRegistry(siteId, prefsdata.isListToolEventsOnlyAvailableInSite()).iterator();
				while (iT.hasNext()){
					ToolInfo t = iT.next();
					if(params.getWhatToolIds().contains(t.getToolId())){
						Iterator<EventInfo> iE = t.getEvents().iterator();
						while (iE.hasNext())
							rpp.events.add(iE.next().getEventId());
					}
				}
			}else rpp.events.addAll(params.getWhatEventIds());

		}

		// when (dates)
		rpp.fDate = null;
		rpp.iDate = null;
		if(params.getWhen().equals(ReportManager.WHEN_CUSTOM)){
			rpp.iDate = params.getWhenFrom();
			rpp.fDate = params.getWhenTo();
		}else rpp.fDate = new Date();
		if(params.getWhen().equals(ReportManager.WHEN_ALL)){
			rpp.iDate = M_sm.getInitialActivityDate(siteId);
		}else if(params.getWhen().equals(ReportManager.WHEN_LAST7DAYS)){
			Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, 00);
			c.set(Calendar.MINUTE, 00);
			c.set(Calendar.SECOND, 00);
			c.add(Calendar.DATE, -6);
			rpp.iDate = c.getTime();
		}else if(params.getWhen().equals(ReportManager.WHEN_LAST30DAYS)){
			Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, 00);
			c.set(Calendar.MINUTE, 00);
			c.set(Calendar.SECOND, 00);
			c.add(Calendar.DATE, -29);
			rpp.iDate = c.getTime();
		}
		params.setWhenFrom(rpp.iDate);
		params.setWhenTo(rpp.fDate);

		// who (users, groups, roles)
		rpp.userIds = null;
		rpp.inverseUserSelection = false;
		if(params.getWho().equals(ReportManager.WHO_ALL)){
			;
		}else if(params.getWho().equals(ReportManager.WHO_ROLE)){
			rpp.userIds = new ArrayList<String>();
			try{
				Site site = M_ss.getSite(siteId);
				rpp.userIds.addAll(site.getUsersHasRole(params.getWhoRoleId()));
			}catch(IdUnusedException e){
				LOG.error("No site with specified siteId.");
			}

		}else if(params.getWho().equals(ReportManager.WHO_GROUPS)){
			rpp.userIds = new ArrayList<String>();
			try{
				Site site = M_ss.getSite(siteId);
				rpp.userIds.addAll(site.getGroup(params.getWhoGroupId()).getUsers());
			}catch(IdUnusedException e){
				LOG.error("No site with specified siteId.");
			}

		}else if(params.getWho().equals(ReportManager.WHO_CUSTOM)){
			rpp.userIds = params.getWhoUserIds();
		}else{
			// inverse
			rpp.inverseUserSelection = true;
		}
		params.setWhoUserIds(rpp.userIds);

		// generate report
		Report report = new ReportImpl();
		report.setReportParams(params);
		List<CommonStatGrpByDate> data = null;
		if(params.getWhat().equals(ReportManager.WHAT_RESOURCES)){
			rpp.resourceIds = null;
			if(params.getWhatResourceIds() != null){
				rpp.resourceIds = new ArrayList<String>();
				Iterator<String> iR = params.getWhatResourceIds().iterator();
				while (iR.hasNext())
					rpp.resourceIds.add("/content" + iR.next());
			}
			rpp.resourceAction = params.getWhatResourceAction();			
		}

		return rpp;		
	}
	
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.report.ReportManager#getReportFormattedParams()
	 */
	public ReportFormattedParams getReportFormattedParams() {
		return formattedParams;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.report.ReportManager#getReportAsExcel(org.sakaiproject.sitestats.api.report.Report, java.lang.String)
	 */
	public byte[] getReportAsExcel(Report report, String sheetName) {
		List<CommonStatGrpByDate> statsObjects = report.getReportData();
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(sheetName);
		HSSFRow headerRow = sheet.createRow((short) 0);

		// Add the column headers
		headerRow.createCell((short) (0)).setCellValue(msgs.getString("th_id"));
		headerRow.createCell((short) (1)).setCellValue(msgs.getString("th_user"));
		if(!report.getReportParams().getWho().equals(ReportManager.WHO_NONE)) {
			if(report.getReportParams().getWhat().equals(ReportManager.WHAT_RESOURCES)){
				headerRow.createCell((short) (2)).setCellValue(msgs.getString("th_resource"));
				headerRow.createCell((short) (3)).setCellValue(msgs.getString("th_action"));
				headerRow.createCell((short) (4)).setCellValue(msgs.getString("th_date"));
				headerRow.createCell((short) (5)).setCellValue(msgs.getString("th_total"));
			}else{
				headerRow.createCell((short) (2)).setCellValue(msgs.getString("th_event"));
				headerRow.createCell((short) (3)).setCellValue(msgs.getString("th_date"));
				headerRow.createCell((short) (4)).setCellValue(msgs.getString("th_total"));
			}
		}

		// Fill the spreadsheet cells
		Iterator<CommonStatGrpByDate> i = statsObjects.iterator();
		while (i.hasNext()){
			HSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
			CommonStatGrpByDate se = i.next();
			// user name
			String userId = se.getUserId();
			String userEid = null;
			String userName = null;
			if (userId != null) {
    			if(("-").equals(userId)) {
    				userEid = "-";
    				userName = msgs.getString("user_anonymous");
    			}else if(("?").equals(userId)) {
    				userEid = "-";
    				userName = msgs.getString("user_anonymous_access");
    			}else{
    				try{
    					User user = M_uds.getUser(userId);
    					userEid = user.getDisplayId();
    					userName = user.getDisplayName();
    				}catch(UserNotDefinedException e1){
    					userEid = userId;
    					userName = msgs.getString("user_unknown");
    				}
    			}
    		}else{
    			userName = msgs.getString("user_unknown");
    		}
			row.createCell((short) 0).setCellValue(userEid);
			row.createCell((short) 1).setCellValue(userName);
			if(!report.getReportParams().getWho().equals(ReportManager.WHO_NONE)) {
				if(report.getReportParams().getWhat().equals(ReportManager.WHAT_RESOURCES)){
					// resource name
					row.createCell((short) 2).setCellValue(se.getRef());
					// resource action
					row.createCell((short) 3).setCellValue(se.getRefAction());
					// most recent lastDate
					row.createCell((short) 4).setCellValue(se.getDate().toString());
					// total
					row.createCell((short) 5).setCellValue(se.getCount());
				}else{
					// event name
					row.createCell((short) 2).setCellValue(M_ers.getEventName(se.getRef()));
					// most recent lastDate
					row.createCell((short) 3).setCellValue(se.getDate().toString());
					// total
					row.createCell((short) 4).setCellValue(se.getCount());
				}
			}
		}
		return wb.getBytes();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.report.ReportManager#getReportAsCsv(org.sakaiproject.sitestats.api.report.Report)
	 */
	public String getReportAsCsv(Report report) {
		List<CommonStatGrpByDate> statsObjects = report.getReportData();
		StringBuffer sb = new StringBuffer();

		// Add the headers
		appendQuoted(sb, msgs.getString("th_id"));
		sb.append(",");
		appendQuoted(sb, msgs.getString("th_user"));
		if(!report.getReportParams().getWho().equals(ReportManager.WHO_NONE)) {
			sb.append(",");
			if(report.getReportParams().getWhat().equals(ReportManager.WHAT_RESOURCES)){
				appendQuoted(sb, msgs.getString("th_resource"));
				sb.append(",");
				appendQuoted(sb, msgs.getString("th_action"));
			}else{
				appendQuoted(sb, msgs.getString("th_event"));
			}
			sb.append(",");
			appendQuoted(sb, msgs.getString("th_date"));
			sb.append(",");
			appendQuoted(sb, msgs.getString("th_total"));
		}
		sb.append("\n");

		// Add the data
		Iterator<CommonStatGrpByDate> i = statsObjects.iterator();
		while (i.hasNext()){
			CommonStatGrpByDate se = i.next();
			// user id
			String userId = se.getUserId();
			String userEid = null;
			String userName = null;			
			if (userId != null) {
    			if(("-").equals(userId)) {
    				userEid = "-";
    				userName = msgs.getString("user_anonymous");
    			}else if(("?").equals(userId)) {
    				userEid = "-";
    				userName = msgs.getString("user_anonymous_access");
    			}else{
    				try{
    					User user = M_uds.getUser(userId);
    					userEid = user.getDisplayId();
    					userName = user.getDisplayName();
    				}catch(UserNotDefinedException e1){
    					userEid = userId;
    					userName = msgs.getString("user_unknown");
    				}
    			}
    		}else{
    			userName = msgs.getString("user_unknown");
    		}
			appendQuoted(sb, userEid);
			sb.append(",");
			// user name
			appendQuoted(sb, userName);
			if(!report.getReportParams().getWho().equals(ReportManager.WHO_NONE)) {
				sb.append(",");
				if(report.getReportParams().getWhat().equals(ReportManager.WHAT_RESOURCES)){
					// resource name
					appendQuoted(sb, se.getRef());
					sb.append(",");
					// resource action
					appendQuoted(sb, se.getRefAction());
					sb.append(",");
				}else{
					// event name
					appendQuoted(sb, M_ers.getEventName(se.getRef()));
					sb.append(",");
				}
				// most recent lastDate
				appendQuoted(sb, se.getDate().toString());
				sb.append(",");
				// total
				appendQuoted(sb, Long.toString(se.getCount()));
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.report.ReportManager#getReportAsPDF(org.sakaiproject.sitestats.api.report.Report)
	 */
	public byte[] getReportAsPDF(Report report) {
		ByteArrayOutputStream out = null;
		try{
			// Setup a buffer to obtain the content length
		    out = new ByteArrayOutputStream();
		    
		    //ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();		    		    
		    //fopFactory.setURIResolver(new ServletContextURIResolver(servletContext));
		    fopFactory.setURIResolver(new LibraryURIResolver());
			
		    FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
	        // configure foUserAgent as desired
			//File burl = new File(servletContext.getRealPath("/"));
			//foUserAgent.setBaseURL("file://"+ burl.getParent()+"/library/");
			
			
            // Construct fop with desired output format
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

            // Setup XSLT
            if(cachedXmlFoXSLT == null) {            	
            	ClassPathResource xsltCPR = new ClassPathResource("org/sakaiproject/sitestats/config/fop/"+XML_FO_XSL_FILE);
            	InputStream xslt = xsltCPR.getInputStream();
            	TransformerFactory factory = TransformerFactory.newInstance();
	            cachedXmlFoXSLT = factory.newTemplates(new StreamSource(xslt));
            }
            Transformer transformer = cachedXmlFoXSLT.newTransformer();
        
            // Setup input for XSLT transformation
            Source src = new SAXSource(new ReportXMLReader(), new ReportInputSource(report));
        
            // Resulting SAX events (the generated FO) must be piped through to FOP
            Result res = new SAXResult(fop.getDefaultHandler());

            // Start XSLT transformation and FOP processing
            transformer.transform(src, res);
    	    
		}catch(TransformerConfigurationException e){
			LOG.error("TransformerConfigurationException while writing SiteStats PDF report", e);
		}catch(FOPException e){
			LOG.error("FOPException while writing SiteStats PDF report", e);
		}catch(TransformerException e){
			LOG.error("TransformerException while writing SiteStats PDF report", e);
		}catch(Exception e){
			LOG.error("Exception while generating SiteStats PDF report", e);
		}finally{
			try{
				if(out != null) {
					out.close();
					return out.toByteArray();
				}
			}catch(IOException e){
				LOG.error("IOException while writing SiteStats PDF report", e);
			}
		}
		return null;
	}
	

	// ################################################################
	// Utility methods
	// ################################################################

	private Report consolidateAnonymousEvents(Report report) {
		List<CommonStatGrpByDate> consolidated = new ArrayList<CommonStatGrpByDate>();
		List<CommonStatGrpByDate> list = report.getReportData();
		Map<String, CommonStatGrpByDate> anonMap = new HashMap<String, CommonStatGrpByDate>();

		for(CommonStatGrpByDate s : list){
			String eventId = s.getRef();
			if(!isAnonymousEvent(eventId)){
				consolidated.add(s);
			}else{
				CommonStatGrpByDate sMapped = anonMap.get(eventId);
				if(sMapped != null){
					sMapped.setCount(sMapped.getCount() + s.getCount());
					if(s.getDate().after(sMapped.getDate()))
						sMapped.setDate(s.getDate());
					anonMap.put(eventId, sMapped);
				}else{
					s.setUserId(null);
					anonMap.put(eventId, s);
				}
			}
		}

		for(CommonStatGrpByDate s : anonMap.values()){
			consolidated.add(s);
		}

		report.setReportData(consolidated);
		return report;
	}

	private boolean isAnonymousEvent(String eventId) {
		for(ToolInfo ti : M_ers.getEventRegistry()){
			for(EventInfo ei : ti.getEvents()){
				if(ei.getEventId().equals(eventId)){
					return ei.isAnonymous();
				}
			}
		}
		return false;
	}

	private StringBuffer appendQuoted(StringBuffer sb, String toQuote) {
		if((toQuote.indexOf(',') >= 0) || (toQuote.indexOf('"') >= 0)){
			String out = toQuote.replaceAll("\"", "\"\"");
			if(LOG.isDebugEnabled()) LOG.debug("Turning '" + toQuote + "' to '" + out + "'");
			sb.append("\"").append(out).append("\"");
		}else{
			sb.append(toQuote);
		}
		return sb;
	}
	
	private String getUserDisplayId(String userId) {
		String userEid = null;		
		if (userId != null) {
			if(("-").equals(userId) || ("?").equals(userId)) {
				userEid = "-";
			}else{
				try{
					userEid = M_uds.getUser(userId).getDisplayId();
				}catch(UserNotDefinedException e1){
					userEid = userId;
				}
			}
		}else{
			userEid = msgs.getString("user_unknown");
		}
		return userEid;
	}
	
	private String getUserDisplayName(String userId) {
		String userName = null;
		if (userId != null) {
			if(("-").equals(userId)) {
				userName = msgs.getString("user_anonymous");
			}else if(("?").equals(userId)) {
				userName = msgs.getString("user_anonymous_access");
			}else{
				try{
					userName = M_uds.getUser(userId).getDisplayName();
				}catch(UserNotDefinedException e1){
					userName = msgs.getString("user_unknown");
				}
			}
		}else{
			userName = msgs.getString("user_unknown");
		}
		return userName;
	}
	
	public String getSiteGroupTitle(String groupId) {
		try{
			Placement placement = M_tm.getCurrentPlacement();
			Site site = M_ss.getSite(placement.getContext());
			return site.getGroup(groupId).getTitle();
		}catch(IdUnusedException e){
			LOG.warn("ReportManager: unable to get group title with id: " + groupId);
		}
		return null;
	}
	
	private class ReportProcessedParams {
		public String			siteId;
		public List<String>		events;
		public List<String>		anonymousEvents;
		public List<String>		resourceIds;
		public String			resourceAction;
		public Date				iDate;
		public Date				fDate;
		public List<String>		userIds;
		public boolean			inverseUserSelection;
		
		public PagingPosition	page;
		public String			sortBy;
		public boolean			sortAscending;
	}

	
	class ReportFormattedParamsImpl implements ReportFormattedParams {

		/* (non-Javadoc)
		 * @see org.sakaiproject.sitestats.api.report.ReportFormattedParams#getReportGenerationDate(org.sakaiproject.sitestats.api.report.Report)
		 */
		public String getReportGenerationDate(Report report) {
			if(report.getReportGenerationDate() == null)
				report.setReportGenerationDate(M_ts.newTime());
			return report.getReportGenerationDate().toStringLocalFull();
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.sitestats.api.report.ReportFormattedParams#getReportActivityBasedOn(org.sakaiproject.sitestats.api.report.Report)
		 */
		public String getReportActivityBasedOn(Report report) {
			if(report.getReportParams().getWhat().equals(ReportManager.WHAT_VISITS))
				return msgs.getString("report_what_visits");
			else if(report.getReportParams().getWhat().equals(ReportManager.WHAT_EVENTS)){
				StringBuffer buff = new StringBuffer();
				buff.append(msgs.getString("report_what_events"));
				if(report.getReportParams().getWhatEventSelType().equals(ReportManager.WHAT_EVENTS_BYTOOL)){
					buff.append(" (");
					buff.append(msgs.getString("report_what_events_bytool"));
					buff.append(")");
				}else{
					buff.append(" (");
					buff.append(msgs.getString("report_what_events_byevent"));
					buff.append(")");
				}
				return buff.toString();
			}else 
				return msgs.getString("report_what_resources");
		}
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.sitestats.api.report.ReportFormattedParams#getReportActivitySelectionTitle(org.sakaiproject.sitestats.api.report.Report)
		 */
		public String getReportActivitySelectionTitle(Report report) {
			if(report.getReportParams().getWhat().equals(ReportManager.WHAT_VISITS))
				return msgs.getString("report_what_visits");
			else if(report.getReportParams().getWhat().equals(ReportManager.WHAT_EVENTS)){
				if(report.getReportParams().getWhatEventSelType().equals(ReportManager.WHAT_EVENTS_BYTOOL))
					return msgs.getString("reportres_summ_act_tools_selected");
				else 
					return msgs.getString("reportres_summ_act_events_selected");
			}else
				return msgs.getString("reportres_summ_act_rsrc_selected");
		}
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.sitestats.api.report.ReportFormattedParams#getReportActivitySelection(org.sakaiproject.sitestats.api.report.Report)
		 */
		public String getReportActivitySelection(Report report) {
			if(report.getReportParams().getWhat().equals(ReportManager.WHAT_VISITS)){
				// visits
				return null;
			}else if(report.getReportParams().getWhat().equals(ReportManager.WHAT_EVENTS)){
				if(report.getReportParams().getWhatEventSelType().equals(ReportManager.WHAT_EVENTS_BYTOOL)){
					// tools
					List<String> list = report.getReportParams().getWhatToolIds();
					StringBuffer buff = new StringBuffer();
					for(int i=0; i<list.size() - 1; i++){
						String toolId = list.get(i);
						buff.append(M_ers.getToolName(toolId));
						buff.append(", ");
					}
					String toolId = list.get(list.size() - 1);
					buff.append(M_ers.getToolName(toolId));
					return buff.toString();
				}else{
					// events
					List<String> list = report.getReportParams().getWhatEventIds();
					StringBuffer buff = new StringBuffer();
					for(int i=0; i<list.size() - 1; i++){
						String eventId = list.get(i);
						buff.append(M_ers.getEventName(eventId));
						buff.append(", ");
					}
					String eventId = list.get(list.size() - 1);
					buff.append(M_ers.getEventName(eventId));
					return buff.toString();
				}
			}else{
				// resources
				List<String> list = report.getReportParams().getWhatResourceIds();
				if(report.getReportParams().getWhatResourceIds() == null
						|| report.getReportParams().getWhatResourceIds().size() == 0 )
					return null;
				if(list.contains("all"))
					return msgs.getString("report_what_all");
				StringBuffer buff = new StringBuffer();
				for(int i=0; i<list.size() - 1; i++){
					String resourceId = list.get(i);
					try{
						ContentResource cr = M_chs.getResource(resourceId);
						String crName = cr.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);	
						buff.append(crName);
						buff.append(", ");
					}catch(PermissionException e){
						e.printStackTrace();
					}catch(IdUnusedException e){
						e.printStackTrace();
					}catch(TypeException e){
						e.printStackTrace();
					}
				}
				String resourceId = list.get(list.size() - 1);
				try{
					ContentResource cr = M_chs.getResource(resourceId);
					String crName = cr.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);	
					buff.append(crName);
				}catch(PermissionException e){
					e.printStackTrace();
				}catch(IdUnusedException e){
					e.printStackTrace();
				}catch(TypeException e){
					e.printStackTrace();
				}
				return buff.toString();
			}
		}
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.sitestats.api.report.ReportFormattedParams#getReportResourceActionTitle(org.sakaiproject.sitestats.api.report.Report)
		 */
		public String getReportResourceActionTitle(Report report) {
			if(report.getReportParams().getWhat().equals(ReportManager.WHAT_RESOURCES)
					&& report.getReportParams().getWhatResourceAction() != null)
					return msgs.getString("reportres_summ_act_rsrc_action");
			return null;
		}
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.sitestats.api.report.ReportFormattedParams#getReportResourceAction(org.sakaiproject.sitestats.api.report.Report)
		 */
		public String getReportResourceAction(Report report) {
			if(report.getReportParams().getWhat().equals(ReportManager.WHAT_RESOURCES)
					&& report.getReportParams().getWhatResourceAction() != null){
				return msgs.getString("action_" + report.getReportParams().getWhatResourceAction());
			}else
				return null;
		}
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.sitestats.api.report.ReportFormattedParams#getReportTimePeriod(org.sakaiproject.sitestats.api.report.Report)
		 */
		public String getReportTimePeriod(Report report) {
			if(report.getReportParams().getWhen().equals(ReportManager.WHEN_ALL)){
				return msgs.getString("report_when_all");
			}else{
				Time from = M_ts.newTime(report.getReportParams().getWhenFrom().getTime());
				Time to = M_ts.newTime(report.getReportParams().getWhenTo().getTime());
				return from.toStringLocalFull() + " - " + to.toStringLocalFull();
			}
		}
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.sitestats.api.report.ReportFormattedParams#getReportUserSelectionType(org.sakaiproject.sitestats.api.report.Report)
		 */
		public String getReportUserSelectionType(Report report) {
			if(report.getReportParams().getWho().equals(ReportManager.WHO_ALL))
				return msgs.getString("report_who_all");
			else if(report.getReportParams().getWho().equals(ReportManager.WHO_GROUPS))
				return msgs.getString("report_who_group");
			else if(report.getReportParams().getWho().equals(ReportManager.WHO_ROLE))
				return msgs.getString("report_who_role");
			else if(report.getReportParams().getWho().equals(ReportManager.WHO_CUSTOM))
				return msgs.getString("report_who_custom");
			else 
				return msgs.getString("report_who_not_match");
		}
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.sitestats.api.report.ReportFormattedParams#getReportUserSelectionTitle(org.sakaiproject.sitestats.api.report.Report)
		 */
		public String getReportUserSelectionTitle(Report report) {
			if(report.getReportParams().getWho().equals(ReportManager.WHO_ALL))
				return null;
			else if(report.getReportParams().getWho().equals(ReportManager.WHO_GROUPS))
				return msgs.getString("reportres_summ_usr_group_selected");
			else if(report.getReportParams().getWho().equals(ReportManager.WHO_ROLE))
				return msgs.getString("reportres_summ_usr_role_selected");
			else if(report.getReportParams().getWho().equals(ReportManager.WHO_CUSTOM))
				return msgs.getString("reportres_summ_usr_users_selected");
			else 
				return null;		
		}
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.sitestats.api.report.ReportFormattedParams#getReportUserSelection(org.sakaiproject.sitestats.api.report.Report)
		 */
		public String getReportUserSelection(Report report) {
			if(report.getReportParams().getWho().equals(ReportManager.WHO_GROUPS)){
				return getSiteGroupTitle(report.getReportParams().getWhoGroupId());
			}else if(report.getReportParams().getWho().equals(ReportManager.WHO_ROLE)){
				return report.getReportParams().getWhoRoleId();
			}else if(report.getReportParams().getWho().equals(ReportManager.WHO_CUSTOM)){
				// users
				List<String> list = report.getReportParams().getWhoUserIds();
				StringBuffer buff = new StringBuffer();
				for(int i=0; i<list.size() - 1; i++){
					String userId = list.get(i);
					buff.append(getUserDisplayId(userId));
					buff.append(", ");
				}
				String userId = list.get(list.size() - 1);
				buff.append(getUserDisplayId(userId));
				return buff.toString();
			}else
				return null;
		}
	}
}
