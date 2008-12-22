package org.sakaiproject.sitestats.impl.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
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
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.EventStat;
import org.sakaiproject.sitestats.api.ResourceStat;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportFormattedParams;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.impl.parser.DigesterUtil;
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
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;


/**
 * @author Nuno Fernandes
 *
 */
public class ReportManagerImpl extends HibernateDaoSupport implements ReportManager {
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
	private StatsAuthz				M_sa;
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

	public void setStatsAuthz(StatsAuthz statsAuthz) {
		this.M_sa = statsAuthz;
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
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.report.ReportManager#getReport(org.sakaiproject.sitestats.api.report.ReportDef, boolean)
	 */
	public Report getReport(ReportDef reportDef, boolean restrictToToolsInSite) {
		return getReport(reportDef, restrictToToolsInSite, null);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.report.ReportManager#getReportRowCount(org.sakaiproject.sitestats.api.report.ReportDef, boolean)
	 */
	public int getReportRowCount(ReportDef reportDef, boolean restrictToToolsInSite) {
		ReportProcessedParams rpp = processReportParams(reportDef.getReportParams(), restrictToToolsInSite, null);
		if(reportDef.getReportParams().getWhat().equals(ReportManager.WHAT_RESOURCES)){
			return M_sm.getResourceStatsRowCount(rpp.siteId, rpp.resourceAction, rpp.resourceIds, rpp.iDate, rpp.fDate, rpp.userIds, rpp.inverseUserSelection, rpp.totalsBy);
		}else{
			return M_sm.getEventStatsRowCount(rpp.siteId, rpp.events, rpp.iDate, rpp.fDate, rpp.userIds, rpp.inverseUserSelection, rpp.totalsBy);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.report.ReportManager#getReport(org.sakaiproject.sitestats.api.report.ReportDef, boolean, org.sakaiproject.javax.PagingPosition)
	 */
	public Report getReport(ReportDef reportDef, boolean restrictToToolsInSite, PagingPosition pagingPosition) {
		ReportProcessedParams rpp = processReportParams(reportDef.getReportParams(), restrictToToolsInSite, pagingPosition);

		// generate report
		Report report = new ReportImpl();
		List<Stat> data = null;
		if(reportDef.getReportParams().getWhat().equals(ReportManager.WHAT_RESOURCES)){
			data = M_sm.getResourceStats(rpp.siteId, rpp.resourceAction, rpp.resourceIds, rpp.iDate, rpp.fDate, rpp.userIds, rpp.inverseUserSelection, pagingPosition, rpp.totalsBy, rpp.sortBy, rpp.sortAscending, rpp.maxResults);
		}else{
			data = M_sm.getEventStats(rpp.siteId, rpp.events, rpp.iDate, rpp.fDate, rpp.userIds, rpp.inverseUserSelection, pagingPosition, rpp.totalsBy, rpp.sortBy, rpp.sortAscending, rpp.maxResults);
		}
		
		// add missing info in report and its parameters
		if(report != null) {
			reportDef.getReportParams().setWhenFrom(rpp.iDate);
			reportDef.getReportParams().setWhenTo(rpp.fDate);
			reportDef.getReportParams().setWhoUserIds(rpp.userIds);
			reportDef.getReportParams().setHowTotalsBy(rpp.totalsBy);
			report.setReportData(data);
			report.setReportDefinition(reportDef);
			report.setReportGenerationDate(M_ts.newTime());
		}

		return report;
	}
	
	private ReportProcessedParams processReportParams(ReportParams params, boolean restrictToToolsInSite, PagingPosition pagingPosition) {
		ReportProcessedParams rpp = new ReportProcessedParams();
		
		// site
		rpp.siteId = params.getSiteId();
		
		// what (visits, events, resources)
		rpp.events = new ArrayList<String>();
		if(params.getWhat().equals(ReportManager.WHAT_VISITS)){
			rpp.events.add(StatsManager.SITEVISIT_EVENTID);

		}else if(params.getWhat().equals(ReportManager.WHAT_EVENTS)){
			if(params.getWhatEventSelType().equals(ReportManager.WHAT_EVENTS_BYTOOL)){
				Iterator<ToolInfo> iT = null;
				if(rpp.siteId != null) {
					iT = M_ers.getEventRegistry(rpp.siteId, restrictToToolsInSite).iterator();
				}else{
					iT = M_ers.getEventRegistry().iterator();
				}
				while (iT.hasNext()){
					ToolInfo t = iT.next();
					if(params.getWhatToolIds().contains(t.getToolId())
							|| params.getWhatToolIds().contains(WHAT_EVENTS_ALLTOOLS)){
						Iterator<EventInfo> iE = t.getEvents().iterator();
						while (iE.hasNext())
							rpp.events.add(iE.next().getEventId());
					}
				}
			}else rpp.events.addAll(params.getWhatEventIds());

		}else if(params.getWhat().equals(ReportManager.WHAT_RESOURCES)){
			rpp.resourceIds = null;
			rpp.resourceAction = null;
			if(params.isWhatLimitedResourceIds() && params.getWhatResourceIds() != null){
				rpp.resourceIds = new ArrayList<String>();
				Iterator<String> iR = params.getWhatResourceIds().iterator();
				while (iR.hasNext())
					rpp.resourceIds.add("/content" + iR.next());
			}
			if(params.isWhatLimitedAction() && params.getWhatResourceAction() != null) {
				rpp.resourceAction = params.getWhatResourceAction();			
			}
		}

		// when (dates)
		rpp.fDate = null;
		rpp.iDate = null;
		if(params.getWhen().equals(ReportManager.WHEN_CUSTOM)){
			rpp.iDate = params.getWhenFrom();
			rpp.fDate = params.getWhenTo();
		}else rpp.fDate = new Date();
		if(params.getWhen().equals(ReportManager.WHEN_ALL)){
			if(rpp.siteId != null) {
				rpp.iDate = M_sm.getInitialActivityDate(rpp.siteId);
			}else{
				rpp.iDate = null;
			}
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
		}else if(params.getWho().equals(ReportManager.WHO_ROLE) && rpp.siteId != null){
			rpp.userIds = new ArrayList<String>();
			try{
				Site site = M_ss.getSite(rpp.siteId);
				rpp.userIds.addAll(site.getUsersHasRole(params.getWhoRoleId()));
			}catch(IdUnusedException e){
				LOG.error("No site with specified siteId.");
			}

		}else if(params.getWho().equals(ReportManager.WHO_GROUPS) && rpp.siteId != null){
			rpp.userIds = new ArrayList<String>();
			try{
				Site site = M_ss.getSite(rpp.siteId);
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
		
		// how
		rpp.totalsBy = params.getHowTotalsBy();
		if(HOW_SORT_DEFAULT.equals(params.getHowSortBy())) {
			rpp.sortBy = null;
		}else{
			rpp.sortBy = params.getHowSortBy();
		}
		rpp.sortAscending = params.getHowSortAscending();
		if(params.isHowLimitedMaxResults() && params.getHowMaxResults() > 0) {
			rpp.maxResults = params.getHowMaxResults();			
		}else{
			rpp.maxResults = 0;
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
	 * @see org.sakaiproject.sitestats.api.report.ReportManager#isReportColumnAvailable(org.sakaiproject.sitestats.api.report.ReportParams, java.lang.String)
	 */
	public boolean isReportColumnAvailable(ReportParams params, String column) {
		List<String> totalsBy = params.getHowTotalsBy();
		if(column == null) {
			return false;
			
		}else if(column.equals(StatsManager.T_SITE)) {
			return totalsBy.contains(StatsManager.T_SITE);
			
		}else if(column.equals(StatsManager.T_USER)) {
			return totalsBy.contains(StatsManager.T_USER);
			
		}else if(column.equals(StatsManager.T_EVENT)) {
			return totalsBy.contains(StatsManager.T_EVENT) && !ReportManager.WHO_NONE.equals(params.getWho());
			
		}else if(column.equals(StatsManager.T_RESOURCE)) {
			return totalsBy.contains(StatsManager.T_RESOURCE) && !ReportManager.WHO_NONE.equals(params.getWho());
			
		}else if(column.equals(StatsManager.T_RESOURCE_ACTION)) {
			return totalsBy.contains(StatsManager.T_RESOURCE_ACTION) && !ReportManager.WHO_NONE.equals(params.getWho());
			
		}else if(column.equals(StatsManager.T_DATE)) {
			return totalsBy.contains(StatsManager.T_DATE) && !ReportManager.WHO_NONE.equals(params.getWho());
			
		}else if(column.equals(StatsManager.T_LASTDATE)) {
			return totalsBy.contains(StatsManager.T_LASTDATE) && !ReportManager.WHO_NONE.equals(params.getWho());
			
		}else if(column.equals(StatsManager.T_TOTAL)) {
			return !ReportManager.WHO_NONE.equals(params.getWho());
			
		}else{
			LOG.warn("isReportColumnAvailable(): invalid column: "+column);
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.report.ReportManager#getReportDefinition(long)
	 */
	public ReportDef getReportDefinition(final long id) {
		HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				return session.load(ReportDef.class, Long.valueOf(id));
			}
		};
		Object o = getHibernateTemplate().execute(hcb);
		if(o != null) {
			ReportDef reportDef = (ReportDef) o;
			try{
				reportDef.setReportParams(DigesterUtil.convertXmlToReportParams(reportDef.getReportDefinitionXml()));
			}catch(Exception e){
				LOG.warn("getReportDefinition(): unable to parse report parameters.");
			}
			return reportDef;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.report.ReportManager#saveReportDefinition(org.sakaiproject.sitestats.api.report.ReportDef)
	 */
	public boolean saveReportDefinition(final ReportDef reportDef) {
		if(reportDef.getSiteId() == null && !M_sa.isSiteStatsAdminPage()) {
			return false;
		}
		try{
			if(reportDef.getCreatedBy() == null) {
				reportDef.setCreatedBy(M_uds.getCurrentUser().getId());
			}
			if(reportDef.getCreatedOn() == null) {
				reportDef.setCreatedOn(new Date());
			}
			if(reportDef.getModifiedBy() == null) {
				reportDef.setModifiedBy(M_uds.getCurrentUser().getId());
			}
			if(reportDef.getModifiedOn() == null) {
				reportDef.setModifiedOn(new Date());
			}
			reportDef.setReportDefinitionXml(DigesterUtil.convertReportParamsToXml(reportDef.getReportParams()));			
		}catch(Exception e) {
			LOG.warn("saveReportDefinition(): unable to generate xml string from report parameters.");
			return false;
		}
		HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Transaction tx = null;
				try{
					tx = session.beginTransaction();
					session.saveOrUpdate(reportDef);
					tx.commit();
				}catch(Exception e){
					if(tx != null) tx.rollback();
					LOG.warn("Unable to commit transaction: ", e);
					return Boolean.FALSE;
				}
				return Boolean.TRUE;
			}
		};
		return (Boolean) getHibernateTemplate().execute(hcb);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.report.ReportManager#removeReportDefinition(org.sakaiproject.sitestats.api.report.ReportDef)
	 */
	public boolean removeReportDefinition(final ReportDef reportDef) {
		HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Transaction tx = null;
				try{
					tx = session.beginTransaction();
					session.delete(reportDef);
					tx.commit();
				}catch(Exception e){
					if(tx != null) tx.rollback();
					LOG.warn("Unable to commit transaction: ", e);
					return Boolean.FALSE;
				}
				return Boolean.TRUE;
			}
		};
		return (Boolean) getHibernateTemplate().execute(hcb);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.report.ReportManager#getReportDefinitions(java.lang.String, boolean, boolean)
	 */
	public List<ReportDef> getReportDefinitions(final String siteId, final boolean includedPredefined, final boolean includeHidden) {
		HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Criteria c = session.createCriteria(ReportDef.class);
				if(siteId != null) {
					if(includedPredefined) {
						c.add(Expression.or(Expression.eq("siteId", siteId), Expression.isNull("siteId")));
					}else{
						c.add(Expression.eq("siteId", siteId));
					}
				}else{
					c.add(Expression.isNull("siteId"));
				}
				if(!includeHidden) {
					c.add(Expression.eq("hidden", false));
				}
				return c.list();
			}
		};
		Object o = getHibernateTemplate().execute(hcb);
		if(o != null) {
			List<ReportDef> reportDefs = (List<ReportDef>) o;
			for(ReportDef reportDef : reportDefs) {
				try{
					reportDef.setReportParams(DigesterUtil.convertXmlToReportParams(reportDef.getReportDefinitionXml()));
				}catch(Exception e){
					LOG.warn("getReportDefinition(): unable to parse report parameters.");
					reportDef.setReportParams(null);
				}
			}
			return reportDefs;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.report.ReportManager#getReportAsExcel(org.sakaiproject.sitestats.api.report.Report, java.lang.String)
	 */
	public byte[] getReportAsExcel(Report report, String sheetName) {
		List<Stat> statsObjects = report.getReportData();
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(sheetName);
		HSSFRow headerRow = sheet.createRow((short) 0);

		// Add the column headers
		int ix = 0;
		if(isReportColumnAvailable(report.getReportDefinition().getReportParams(), StatsManager.T_USER)) {
			headerRow.createCell((short) (ix++)).setCellValue(msgs.getString("th_id"));
			headerRow.createCell((short) (ix++)).setCellValue(msgs.getString("th_user"));
		}
		if(isReportColumnAvailable(report.getReportDefinition().getReportParams(), StatsManager.T_EVENT)) {
			headerRow.createCell((short) (ix++)).setCellValue(msgs.getString("th_event"));
		}
		if(isReportColumnAvailable(report.getReportDefinition().getReportParams(), StatsManager.T_RESOURCE)) {
			headerRow.createCell((short) (ix++)).setCellValue(msgs.getString("th_resource"));
		}
		if(isReportColumnAvailable(report.getReportDefinition().getReportParams(), StatsManager.T_RESOURCE_ACTION)) {
			headerRow.createCell((short) (ix++)).setCellValue(msgs.getString("th_action"));
		}
		if(isReportColumnAvailable(report.getReportDefinition().getReportParams(), StatsManager.T_DATE)) {
			headerRow.createCell((short) (ix++)).setCellValue(msgs.getString("th_date"));
		}
		if(isReportColumnAvailable(report.getReportDefinition().getReportParams(), StatsManager.T_LASTDATE)) {
			headerRow.createCell((short) (ix++)).setCellValue(msgs.getString("th_lastdate"));
		}
		if(isReportColumnAvailable(report.getReportDefinition().getReportParams(), StatsManager.T_TOTAL)) {
			headerRow.createCell((short) (ix++)).setCellValue(msgs.getString("th_total"));
		}

		// Fill the spreadsheet cells
		Iterator<Stat> i = statsObjects.iterator();
		while (i.hasNext()){
			HSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
			Stat se = i.next();
			ix = 0;
			if(isReportColumnAvailable(report.getReportDefinition().getReportParams(), StatsManager.T_USER)) {
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
				row.createCell((short) ix++).setCellValue(userEid);
				row.createCell((short) ix++).setCellValue(userName);
			}
			if(isReportColumnAvailable(report.getReportDefinition().getReportParams(), StatsManager.T_EVENT)) {
				EventStat es = (EventStat) se;
				row.createCell((short) ix++).setCellValue(M_ers.getEventName(es.getEventId()));
			}
			if(isReportColumnAvailable(report.getReportDefinition().getReportParams(), StatsManager.T_RESOURCE)) {
				ResourceStat rs = (ResourceStat) se;
				row.createCell((short) ix++).setCellValue(rs.getResourceRef());				
			}
			if(isReportColumnAvailable(report.getReportDefinition().getReportParams(), StatsManager.T_RESOURCE_ACTION)) {
				ResourceStat rs = (ResourceStat) se;
				row.createCell((short) ix++).setCellValue(rs.getResourceAction());				
			}
			if(isReportColumnAvailable(report.getReportDefinition().getReportParams(), StatsManager.T_DATE)) {
				row.createCell((short) ix++).setCellValue(se.getDate().toString());				
			}
			if(isReportColumnAvailable(report.getReportDefinition().getReportParams(), StatsManager.T_LASTDATE)) {
				row.createCell((short) ix++).setCellValue(se.getDate().toString());				
			}
			if(isReportColumnAvailable(report.getReportDefinition().getReportParams(), StatsManager.T_TOTAL)) {
				row.createCell((short) ix++).setCellValue(se.getCount());				
			}
		}
		return wb.getBytes();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.report.ReportManager#getReportAsCsv(org.sakaiproject.sitestats.api.report.Report)
	 */
	public String getReportAsCsv(Report report) {
		List<Stat> statsObjects = report.getReportData();
		StringBuilder sb = new StringBuilder();
		boolean isFirst = true;

		// Add the headers
		if(isReportColumnAvailable(report.getReportDefinition().getReportParams(), StatsManager.T_USER)) {
			appendQuoted(sb, msgs.getString("th_id"));
			sb.append(",");
			appendQuoted(sb, msgs.getString("th_user"));
			isFirst = false;
		}
		if(isReportColumnAvailable(report.getReportDefinition().getReportParams(), StatsManager.T_EVENT)) {
			if(!isFirst) {
				sb.append(",");
			}
			appendQuoted(sb, msgs.getString("th_event"));
			isFirst = false;
		}
		if(isReportColumnAvailable(report.getReportDefinition().getReportParams(), StatsManager.T_RESOURCE)) {
			if(!isFirst) {
				sb.append(",");
			}
			appendQuoted(sb, msgs.getString("th_resource"));
			isFirst = false;
		}
		if(isReportColumnAvailable(report.getReportDefinition().getReportParams(), StatsManager.T_RESOURCE_ACTION)) {
			if(!isFirst) {
				sb.append(",");
			}
			appendQuoted(sb, msgs.getString("th_action"));
			isFirst = false;
		}
		if(isReportColumnAvailable(report.getReportDefinition().getReportParams(), StatsManager.T_DATE)) {
			if(!isFirst) {
				sb.append(",");
			}
			appendQuoted(sb, msgs.getString("th_date"));
			isFirst = false;
		}
		if(isReportColumnAvailable(report.getReportDefinition().getReportParams(), StatsManager.T_LASTDATE)) {
			if(!isFirst) {
				sb.append(",");
			}
			appendQuoted(sb, msgs.getString("th_lastdate"));
			isFirst = false;
		}
		if(isReportColumnAvailable(report.getReportDefinition().getReportParams(), StatsManager.T_TOTAL)) {
			if(!isFirst) {
				sb.append(",");
			}
			appendQuoted(sb, msgs.getString("th_total"));
			isFirst = false;
		}
		sb.append("\n");

		// Add the data
		isFirst = true;
		Iterator<Stat> i = statsObjects.iterator();
		while (i.hasNext()){
			Stat se = i.next();
			// user
			if(isReportColumnAvailable(report.getReportDefinition().getReportParams(), StatsManager.T_USER)) {
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
    				userEid = "-";
	    			userName = msgs.getString("user_unknown");
	    		}
				appendQuoted(sb, userEid);
				sb.append(",");
				appendQuoted(sb, userName);
				isFirst = false;
			}
			// event
			if(isReportColumnAvailable(report.getReportDefinition().getReportParams(), StatsManager.T_EVENT)) {
				if(!isFirst) {
					sb.append(",");
				}
				EventStat es = (EventStat) se;
				appendQuoted(sb, M_ers.getEventName(es.getEventId()));
				isFirst = false;
			}
			// resource
			if(isReportColumnAvailable(report.getReportDefinition().getReportParams(), StatsManager.T_RESOURCE)) {
				if(!isFirst) {
					sb.append(",");
				}
				ResourceStat rs = (ResourceStat) se;
				appendQuoted(sb, rs.getResourceRef());
				isFirst = false;
			}
			// resource action
			if(isReportColumnAvailable(report.getReportDefinition().getReportParams(), StatsManager.T_RESOURCE_ACTION)) {
				if(!isFirst) {
					sb.append(",");
				}
				ResourceStat rs = (ResourceStat) se;
				appendQuoted(sb, rs.getResourceAction());
				isFirst = false;
			}
			// date
			if(isReportColumnAvailable(report.getReportDefinition().getReportParams(), StatsManager.T_DATE)) {
				if(!isFirst) {
					sb.append(",");
				}
				appendQuoted(sb, se.getDate().toString());
				isFirst = false;
			}
			// last date
			if(isReportColumnAvailable(report.getReportDefinition().getReportParams(), StatsManager.T_LASTDATE)) {
				if(!isFirst) {
					sb.append(",");
				}
				appendQuoted(sb, se.getDate().toString());
				isFirst = false;
			}
			// total
			if(isReportColumnAvailable(report.getReportDefinition().getReportParams(), StatsManager.T_TOTAL)) {
				if(!isFirst) {
					sb.append(",");
				}
				appendQuoted(sb, Long.toString(se.getCount()));
				isFirst = false;
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
		    fopFactory.setURIResolver(new LibraryURIResolver());			
		    FOUserAgent foUserAgent = fopFactory.newFOUserAgent();			
			
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
		List<Stat> consolidated = new ArrayList<Stat>();
		List<Stat> list = report.getReportData();
		Map<String, Stat> anonMap = new HashMap<String, Stat>();

		for(Stat s : list){
			if(s instanceof EventStat) {
				EventStat es = (EventStat) s;
				String eventId = es.getEventId();
				if(!isAnonymousEvent(eventId)){
					consolidated.add(s);
				}else{
					Stat sMapped = anonMap.get(eventId);
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
		}

		for(Stat s : anonMap.values()){
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

	private StringBuilder appendQuoted(StringBuilder sb, String toQuote) {
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
	
	private static class ReportProcessedParams {
		public String			siteId;
		public List<String>		events;
		public List<String>		anonymousEvents;
		public List<String>		resourceIds;
		public String			resourceAction;
		public Date				iDate;
		public Date				fDate;
		public List<String>		userIds;
		public List<String> 	totalsBy;
		public boolean			inverseUserSelection;
		
		public PagingPosition	page;
		public String			sortBy;
		public boolean			sortAscending;
		public int				maxResults;
	}

	
	class ReportFormattedParamsImpl implements ReportFormattedParams {
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.sitestats.api.report.ReportFormattedParams#getReportSite(org.sakaiproject.sitestats.api.report.Report)
		 */
		public String getReportSite(Report report) {
			String site = report.getReportDefinition().getReportParams().getSiteId();
			if(site != null) {
				return M_ss.getSiteDisplay(site);
			}else{
				return msgs.getString("report_reportsite_all");
			}
		}
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.sitestats.api.report.ReportFormattedParams#getReportTitle(org.sakaiproject.sitestats.api.report.Report)
		 */
		public String getReportTitle(Report report) {
			String title = report.getReportDefinition().getTitle(); 
			if(title != null && title.length() != 0) {
				if(isStringLocalized(title)) {
					return msgs.getString(report.getReportDefinition().getTitleBundleKey());
				}else{
					return title;
				}
			}else{
				return null;
			}	
		}
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.sitestats.api.report.ReportFormattedParams#getReportDescription(org.sakaiproject.sitestats.api.report.ReportDef)
		 */
		public String getReportDescription(Report report) {
			String description = report.getReportDefinition().getDescription(); 
			if(description != null && description.length() != 0) {
				if(isStringLocalized(description)) {
					return msgs.getString(report.getReportDefinition().getDescriptionBundleKey());
				}else{
					return description;
				}
			}else{
				return null;
			}			
		}
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.sitestats.api.report.ReportFormattedParams#isStringLocalized(java.lang.String)
		 */
		public boolean isStringLocalized(String string) {
			if(string.startsWith("${") && string.endsWith("}")) {
				return true;
			}else{
				return false;
			}
		}

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
			if(report.getReportDefinition().getReportParams().getWhat().equals(ReportManager.WHAT_VISITS))
				return msgs.getString("report_what_visits");
			else if(report.getReportDefinition().getReportParams().getWhat().equals(ReportManager.WHAT_EVENTS)){
				StringBuilder buff = new StringBuilder();
				buff.append(msgs.getString("report_what_events"));
				if(report.getReportDefinition().getReportParams().getWhatEventSelType().equals(ReportManager.WHAT_EVENTS_BYTOOL)){
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
			if(report.getReportDefinition().getReportParams().getWhat().equals(ReportManager.WHAT_VISITS))
				return msgs.getString("report_what_visits");
			else if(report.getReportDefinition().getReportParams().getWhat().equals(ReportManager.WHAT_EVENTS)){
				if(report.getReportDefinition().getReportParams().getWhatEventSelType().equals(ReportManager.WHAT_EVENTS_BYTOOL))
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
			if(report.getReportDefinition().getReportParams().getWhat().equals(ReportManager.WHAT_VISITS)){
				// visits
				return null;
			}else if(report.getReportDefinition().getReportParams().getWhat().equals(ReportManager.WHAT_EVENTS)){
				if(report.getReportDefinition().getReportParams().getWhatEventSelType().equals(ReportManager.WHAT_EVENTS_BYTOOL)){
					// tools
					List<String> list = report.getReportDefinition().getReportParams().getWhatToolIds();
					int listSize = list.size();
					StringBuilder buff = new StringBuilder();
					if(listSize > 0) {
						for(int i=0; i<listSize - 1; i++){
							String toolId = list.get(i);
							buff.append(M_ers.getToolName(toolId));
							buff.append(", ");
						}
						String toolId = list.get(listSize - 1);
						buff.append(M_ers.getToolName(toolId));
					}
					return buff.toString();
				}else{
					// events
					List<String> list = report.getReportDefinition().getReportParams().getWhatEventIds();
					int listSize = list.size();
					StringBuilder buff = new StringBuilder();
					if(listSize > 0) {
						for(int i=0; i<listSize - 1; i++){
							String eventId = list.get(i);
							buff.append(M_ers.getEventName(eventId));
							buff.append(", ");
						}
						String eventId = list.get(listSize - 1);
						buff.append(M_ers.getEventName(eventId));
					}
					return buff.toString();
				}
			}else{
				// resources
				List<String> list = report.getReportDefinition().getReportParams().getWhatResourceIds();
				if(report.getReportDefinition().getReportParams().getWhatResourceIds() == null
						|| report.getReportDefinition().getReportParams().getWhatResourceIds().size() == 0 )
					return null;
				if(list.contains("all"))
					return msgs.getString("report_what_all");
				StringBuilder buff = new StringBuilder();
				String siteId = report.getReportDefinition().getReportParams().getSiteId();
				String resourcesCollectionId = M_chs.getSiteCollection(siteId);
				String dropboxCollectionId = M_chs.getDropboxCollection(siteId);
				String attachmentsCollectionId = resourcesCollectionId.replaceFirst(StatsManager.RESOURCES_DIR, StatsManager.ATTACHMENTS_DIR);
				for(int i=0; i<list.size(); i++){
					String resourceId = list.get(i);
					try{
						if(resourceId.endsWith("/")) {
							if(StatsManager.RESOURCES_DIR.equals(resourceId) || resourceId.equals(resourcesCollectionId)) {
								buff.append(M_tm.getTool(StatsManager.RESOURCES_TOOLID).getTitle());
							}else if(StatsManager.DROPBOX_DIR.equals(resourceId) || resourceId.equals(dropboxCollectionId)) {
								buff.append(M_tm.getTool(StatsManager.DROPBOX_TOOLID).getTitle());
							}else if(resourceId.startsWith(dropboxCollectionId)) {
								buff.append(M_tm.getTool(StatsManager.DROPBOX_TOOLID).getTitle());
								buff.append(": ");
								ContentCollection cc = M_chs.getCollection(resourceId);
								String ccName = cc.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);	
								buff.append(ccName);
							}else if(StatsManager.ATTACHMENTS_DIR.equals(resourceId) || resourceId.equals(attachmentsCollectionId)) {
								buff.append(msgs.getString("report_content_attachments"));
							}else if(resourceId.startsWith(attachmentsCollectionId)) {
								buff.append(msgs.getString("report_content_attachments"));
								buff.append(": ");
								ContentCollection cc = M_chs.getCollection(resourceId);
								String ccName = cc.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);	
								buff.append(ccName);
							}else{
								ContentCollection cc = M_chs.getCollection(resourceId);
								String ccName = cc.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);	
								buff.append(ccName);
							}
						}else{
							ContentResource cr = M_chs.getResource(resourceId);
							String crName = cr.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);	
							buff.append(crName);
						}
						if(list.size() > 1 && i != list.size() - 1) {
							buff.append(", ");
						}
					}catch(PermissionException e){
						e.printStackTrace();
					}catch(IdUnusedException e){
						e.printStackTrace();
					}catch(TypeException e){
						e.printStackTrace();
					}
				}
				return buff.toString();
			}
		}
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.sitestats.api.report.ReportFormattedParams#getReportResourceActionTitle(org.sakaiproject.sitestats.api.report.Report)
		 */
		public String getReportResourceActionTitle(Report report) {
			if(report.getReportDefinition().getReportParams().getWhat().equals(ReportManager.WHAT_RESOURCES)
					&& report.getReportDefinition().getReportParams().getWhatResourceAction() != null)
					return msgs.getString("reportres_summ_act_rsrc_action");
			return null;
		}
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.sitestats.api.report.ReportFormattedParams#getReportResourceAction(org.sakaiproject.sitestats.api.report.Report)
		 */
		public String getReportResourceAction(Report report) {
			if(report.getReportDefinition().getReportParams().getWhat().equals(ReportManager.WHAT_RESOURCES)
					&& report.getReportDefinition().getReportParams().getWhatResourceAction() != null){
				return msgs.getString("action_" + report.getReportDefinition().getReportParams().getWhatResourceAction());
			}else
				return null;
		}
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.sitestats.api.report.ReportFormattedParams#getReportTimePeriod(org.sakaiproject.sitestats.api.report.Report)
		 */
		public String getReportTimePeriod(Report report) {
			if(report.getReportDefinition().getReportParams().getWhen().equals(ReportManager.WHEN_ALL)){
				return msgs.getString("report_when_all");
			}else{
				Time from = M_ts.newTime(report.getReportDefinition().getReportParams().getWhenFrom().getTime());
				Time to = M_ts.newTime(report.getReportDefinition().getReportParams().getWhenTo().getTime());
				return from.toStringLocalFull() + " - " + to.toStringLocalFull();
			}
		}
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.sitestats.api.report.ReportFormattedParams#getReportUserSelectionType(org.sakaiproject.sitestats.api.report.Report)
		 */
		public String getReportUserSelectionType(Report report) {
			if(report.getReportDefinition().getReportParams().getWho().equals(ReportManager.WHO_ALL))
				return msgs.getString("report_who_all");
			else if(report.getReportDefinition().getReportParams().getWho().equals(ReportManager.WHO_GROUPS))
				return msgs.getString("report_who_group");
			else if(report.getReportDefinition().getReportParams().getWho().equals(ReportManager.WHO_ROLE))
				return msgs.getString("report_who_role");
			else if(report.getReportDefinition().getReportParams().getWho().equals(ReportManager.WHO_CUSTOM))
				return msgs.getString("report_who_custom");
			else 
				return msgs.getString("report_who_not_match");
		}
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.sitestats.api.report.ReportFormattedParams#getReportUserSelectionTitle(org.sakaiproject.sitestats.api.report.Report)
		 */
		public String getReportUserSelectionTitle(Report report) {
			if(report.getReportDefinition().getReportParams().getWho().equals(ReportManager.WHO_ALL))
				return null;
			else if(report.getReportDefinition().getReportParams().getWho().equals(ReportManager.WHO_GROUPS))
				return msgs.getString("reportres_summ_usr_group_selected");
			else if(report.getReportDefinition().getReportParams().getWho().equals(ReportManager.WHO_ROLE))
				return msgs.getString("reportres_summ_usr_role_selected");
			else if(report.getReportDefinition().getReportParams().getWho().equals(ReportManager.WHO_CUSTOM))
				return msgs.getString("reportres_summ_usr_users_selected");
			else 
				return null;		
		}
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.sitestats.api.report.ReportFormattedParams#getReportUserSelection(org.sakaiproject.sitestats.api.report.Report)
		 */
		public String getReportUserSelection(Report report) {
			if(report.getReportDefinition().getReportParams().getWho().equals(ReportManager.WHO_GROUPS)){
				return getSiteGroupTitle(report.getReportDefinition().getReportParams().getWhoGroupId());
			}else if(report.getReportDefinition().getReportParams().getWho().equals(ReportManager.WHO_ROLE)){
				return report.getReportDefinition().getReportParams().getWhoRoleId();
			}else if(report.getReportDefinition().getReportParams().getWho().equals(ReportManager.WHO_CUSTOM)){
				// users
				List<String> list = report.getReportDefinition().getReportParams().getWhoUserIds();
				StringBuilder buff = new StringBuilder();
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
