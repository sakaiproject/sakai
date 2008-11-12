/**********************************************************************************
 *
 * Copyright (c) 2006 Universidade Fernando Pessoa
 *
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 *
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/
package org.sakaiproject.sitestats.impl;

import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.digester.Digester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.content.cover.ContentTypeImageService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.CommonStatGrpByDate;
import org.sakaiproject.sitestats.api.EventStat;
import org.sakaiproject.sitestats.api.Prefs;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.ResourceStat;
import org.sakaiproject.sitestats.api.SiteActivity;
import org.sakaiproject.sitestats.api.SiteActivityByTool;
import org.sakaiproject.sitestats.api.SiteVisits;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.SummaryActivityChartData;
import org.sakaiproject.sitestats.api.SummaryActivityTotals;
import org.sakaiproject.sitestats.api.SummaryVisitsChartData;
import org.sakaiproject.sitestats.api.SummaryVisitsTotals;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.impl.event.EventUtil;
import org.sakaiproject.sitestats.impl.event.ToolInfoImpl;
import org.sakaiproject.sitestats.impl.parser.DigesterUtil;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.Validator;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;


/**
 * @author Nuno Fernandes
 *
 */
public class StatsManagerImpl extends HibernateDaoSupport implements StatsManager {
	private Log							LOG										= LogFactory.getLog(StatsManagerImpl.class);
	
	/** Spring bean members */
	private boolean						enableSiteVisits						= org.sakaiproject.component.cover.ServerConfigurationService.getBoolean("display.users.present", false)
																					|| org.sakaiproject.component.cover.ServerConfigurationService.getBoolean("presence.events.log", false);
	private boolean                     enableSiteActivity						= org.sakaiproject.component.cover.ServerConfigurationService.getBoolean("enableSiteActivity@org.sakaiproject.sitestats.api.StatsManager", true);
	private boolean 				    visitsInfoAvailable						= enableSiteVisits; //org.sakaiproject.component.cover.ServerConfigurationService.getBoolean( "display.users.present", true);
	private boolean						enableServerWideStats				= false;
	private String						chartBackgroundColor					= "white";
	private boolean						chartIn3D								= true;
	private float						chartTransparency						= 0.80f;
	private boolean						itemLabelsVisible						= false;
	private boolean						lastJobRunDateVisible					= true;
	private boolean						isEventContextSupported					= false;

	/** Controller fields */
	private boolean						showAnonymousEvents						= false;

	/** Sakai services */
	private EventRegistryService		M_ers;
	private SqlService					M_sql;
	private boolean						autoDdl;
	private UserDirectoryService		M_uds;
	private SiteService					M_ss;
	private ServerConfigurationService	M_scs;
	private ToolManager					M_tm;
	private TimeService					M_ts;
	

	// ################################################################
	// Spring bean methods
	// ################################################################
	public void setEnableSiteVisits(boolean enableSiteVisits) {
		this.enableSiteVisits = enableSiteVisits;
	}
	
	public boolean isEnableSiteVisits() {
		return enableSiteVisits;
	}

	public void setEnableSiteActivity(boolean enableSiteActivity) {
		this.enableSiteActivity = enableSiteActivity;
	}

	public void setServerWideStatsEnabled(boolean enableServerWideStats) {
		this.enableServerWideStats = enableServerWideStats;
	}

	public boolean isServerWideStatsEnabled() {
		return enableServerWideStats;
	}
	
	public boolean isEnableSiteActivity() {
		return enableSiteActivity;
	}
	
	public void setVisitsInfoAvailable(boolean available){
		this.visitsInfoAvailable = available;
	}
	public boolean isVisitsInfoAvailable(){
		return this.visitsInfoAvailable;
	}
	
	public void setChartBackgroundColor(String color) {
		this.chartBackgroundColor = color;
	}
	
	public String getChartBackgroundColor() {
		return chartBackgroundColor;
	}
	
	public void setChartIn3D(boolean value){
		this.chartIn3D = value;
	}
	
	public boolean isChartIn3D() {
		return chartIn3D;
	}
	
	public void setChartTransparency(float value){
		this.chartTransparency = value;
	}
	
	public float getChartTransparency() {
		return chartTransparency;
	}
	
	public void setItemLabelsVisible(boolean itemLabelsVisible) {
		this.itemLabelsVisible = itemLabelsVisible;
	}
	
	public boolean isItemLabelsVisible() {
		return itemLabelsVisible;
	}

	public void setShowAnonymousEvents(boolean value){
		this.showAnonymousEvents = value;
	}
	
	public void setLastJobRunDateVisible(boolean value) {
		this.lastJobRunDateVisible = value;
	}
	
	public boolean isLastJobRunDateVisible(){
		return lastJobRunDateVisible;
	}

	public void setEventRegistryService(EventRegistryService eventRegistryService) {
		this.M_ers = eventRegistryService;
	}

	public void setAutoDdl(boolean autoDdl) {
		this.autoDdl = autoDdl;
	}

	public void setSqlService(SqlService sqlService) {
		this.M_sql = sqlService;
	}

	public void setUserService(UserDirectoryService userService) {
		this.M_uds = userService;
	}

	public void setSiteService(SiteService siteService) {
		this.M_ss = siteService;
	}
	
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.M_scs = serverConfigurationService;
	}
	
	public void setToolManager(ToolManager toolManager) {
		this.M_tm = toolManager;
	}
	
	public void setTimeService(TimeService timeService) {
		this.M_ts = timeService; 
	}

	
	// ################################################################
	// Spring init/destroy methods
	// ################################################################	
	public void init(){
		// Create missing db indexes, if appropriate
		if (autoDdl && M_sql != null) {
			DBHelper dbHelper = new DBHelper(M_sql);
			dbHelper.updateIndexes();
		}
		
		// Checks whether Event.getContext is implemented in Event (from Event API)
		checkForEventContextSupport();
		
		logger.info("init(): - (Event.getContext()?, site visits enabled, charts background color, charts in 3D, charts transparency, item labels visible on bar charts) : " +
							isEventContextSupported+','+enableSiteVisits+','+chartBackgroundColor+','+chartIn3D+','+chartTransparency+','+itemLabelsVisible);		
	}
	
	private PrefsData parseSitePrefs(InputStream input) throws Exception{
		Digester digester = new Digester();
        digester.setValidating(false);
        
        digester = DigesterUtil.configurePrefsDigester(digester);
        
        return (PrefsData) digester.parse( input );
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getPreferences(java.lang.String, boolean)
	 */
	public PrefsData getPreferences(final String siteId, final boolean includeUnselected){
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Criteria c = session.createCriteria(PrefsImpl.class)
							.add(Expression.eq("siteId", siteId));
					try{
						Prefs prefs = (Prefs) c.uniqueResult();
						return prefs;
					}catch(Exception e){
						LOG.warn("Exception in getPreferences() ",e);
						return null;
					}
				}
			};
			Prefs prefs = (Prefs) getHibernateTemplate().execute(hcb);
			PrefsData prefsdata = null;
			if(prefs == null){
				// get default list
				prefsdata = new PrefsDataImpl();
				prefsdata.setToolEventsDef(M_ers.getEventRegistry());
			}else{
				try{
					// parse from stored preferences
					prefsdata = parseSitePrefs(new StringBufferInputStream(prefs.getPrefs()));
				}catch(Exception e){
					// something failed, use default
					LOG.warn("Exception in parseSitePrefs() ",e);
					prefsdata = new PrefsDataImpl();
					prefsdata.setToolEventsDef(M_ers.getEventRegistry());
				}
			}
			
			if(includeUnselected){
				// include unselected tools/events (for Preferences listing)
				prefsdata.setToolEventsDef(EventUtil.getUnionWithAllDefaultToolEvents(prefsdata.getToolEventsDef(), M_ers.getEventRegistry()));
			}
			if(prefsdata.isListToolEventsOnlyAvailableInSite()){
				// intersect with tools available in site
				prefsdata.setToolEventsDef(EventUtil.getIntersectionWithAvailableToolsInSite(prefsdata.getToolEventsDef(), siteId));
			}else{
				// intersect with tools available in sakai installation
				prefsdata.setToolEventsDef(EventUtil.getIntersectionWithAvailableToolsInSakaiInstallation(prefsdata.getToolEventsDef()));
			}

			prefsdata.setToolEventsDef(EventUtil.addMissingAdditionalToolIds(prefsdata.getToolEventsDef(), M_ers.getEventRegistry()));

			return prefsdata;
		}		
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#setPreferences(java.lang.String, org.sakaiproject.sitestats.api.PrefsData)
	 */
	public boolean setPreferences(final String siteId, final PrefsData prefsdata){
		final String prefsdataStr = prefsdata.toXmlPrefs();
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Transaction tx = null;
					try{
						tx = session.beginTransaction();
						Criteria c = session.createCriteria(PrefsImpl.class)
								.add(Expression.eq("siteId", siteId));
						Prefs prefs = (Prefs) c.uniqueResult();
						if(prefs == null){
							prefs = new PrefsImpl();
							prefs.setSiteId(siteId);
						}
						prefs.setPrefs(prefsdataStr);
						session.saveOrUpdate(prefs);
						tx.commit();
						return Boolean.TRUE;
					}catch(Exception e){
						if(tx != null) tx.rollback();
						LOG.warn("Unable to commit transaction: ", e);
						return Boolean.FALSE;
					}
				}
			};
			return ((Boolean) getHibernateTemplate().execute(hcb)).booleanValue();
		}
	}
	
	private List<String> getSiteUsers(String siteId) {
		List<String> siteUserIds = new ArrayList<String>();
		try{
			siteUserIds.addAll(M_ss.getSite(siteId).getUsers());
		}catch(IdUnusedException e){
			LOG.warn("Inexistent site for site id: "+siteId, e);
		}
		return siteUserIds;
	}
	
	
	
	// ################################################################
	// Maps
	// ################################################################		
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getResourceName(java.lang.String)
	 */
	public String getResourceName(String ref){
		Reference r = EntityManager.newReference(ref);
		ResourceProperties rp = r.getProperties();
		if(rp == null){
			return getResourceName_ManualParse(ref);
		}
		String name = rp.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
		StringBuffer _fileName = new StringBuffer("");
		
		String parts[] = ref.split("\\/");		
		if(parts[2].equals("user")){
			_fileName.append("[workspace]");
			_fileName.append(SEPARATOR);
		}else if(parts[2].equals("attachment")){
			if(parts.length > 6 && parts[4].equals("Choose File")){
				// assignment/annoucement attachment
				_fileName.append("[attachment]");
				_fileName.append(SEPARATOR);
			}else if(parts.length > 4){
				// mail attachment
				_fileName.append("[attachment]");
				_fileName.append(SEPARATOR);
			}
		}else if(parts.length > 4  && parts[2].equals("group")){
			// resource (standard)
		}else if(parts.length > 5 && parts[2].equals("group-user")){
			// mail attachment
			_fileName.append("[dropbox");
			_fileName.append(SEPARATOR);
			String userEid = null;
			try{
				userEid = M_uds.getUserEid(parts[4]);
			}catch(UserNotDefinedException e){
				userEid = parts[4];
			}
			_fileName.append(userEid);
			_fileName.append("]");
			_fileName.append(SEPARATOR);
		}
		
		_fileName.append(name);
		return _fileName.toString();		
	}
	
	private String getResourceName_ManualParse(String ref) {
		String parts[] = ref.split("\\/");
		StringBuffer _fileName = new StringBuffer("");
		// filename
		if(parts == null || parts.length < 3 || parts[2].equals("user")){ return null; }
		if(parts[2].equals("attachment")){
			if(parts.length <= 4) return null;
			if(parts[4].equals("Choose File")){
				// assignment/annoucement attachment
				if(parts.length <= 6) return null;
				_fileName.append("attachment");
				_fileName.append(SEPARATOR);
				for(int i = 6; i < parts.length - 1; i++)
					_fileName.append(parts[i] + SEPARATOR);
				_fileName.append(parts[parts.length - 1]);
			}else{
				// mail attachment
				return null;

			}
			// append filename
		}else if(parts[2].equals("group")){
			if(parts.length <= 4) return null;
			for(int i = 4; i < parts.length - 1; i++)
				_fileName.append(parts[i] + SEPARATOR);
			_fileName.append(parts[parts.length - 1]);
		}else if(parts[2].equals("group-user")){
			if(parts.length <= 5) return null;
			// append user eid
			String userEid = null;
			try{
				userEid = M_uds.getUserEid(parts[4]);
			}catch(UserNotDefinedException e){
				userEid = parts[4];
			}
			_fileName.append(userEid);
			_fileName.append(SEPARATOR);
			// append filename
			for(int i = 5; i < parts.length - 1; i++)
				_fileName.append(parts[i] + SEPARATOR);
			_fileName.append(parts[parts.length - 1]);
		}
		String fileName = _fileName.toString();
		if(fileName.trim().equals("")) return null;
		return fileName;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getResourceImageLibraryRelativePath(java.lang.String)
	 */
	public String getResourceImageLibraryRelativePath(String ref){
		Reference r = EntityManager.newReference(ref);
		ResourceProperties rp = r.getProperties();
		
		boolean isCollection;
		if(rp != null){
			try{
				isCollection = rp.getBooleanProperty(rp.getNamePropIsCollection());
			}catch(EntityPropertyNotDefinedException e){
				isCollection = false;
			}catch(EntityPropertyTypeException e){
				isCollection = false;
			}
		}else
			isCollection = false;
		
		String imgLink = "";
		try{
			if(isCollection)
				imgLink = ContentTypeImageService.getContentTypeImage("folder");			
			else if(rp != null){
				String contentTypePropName = rp.getNamePropContentType();
				String contentType = rp.getProperty(contentTypePropName);
				if(contentType != null)
					imgLink = ContentTypeImageService.getContentTypeImage(contentType);
				else{
					imgLink = "sakai/generic.gif";
				}
			}else{;
				imgLink = "sakai/generic.gif";
			}
		}catch(Exception e){
			imgLink = "sakai/generic.gif";
		}
		return "image/" + imgLink;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getResourceImage(java.lang.String)
	 */
	public String getResourceImage(String ref){
		return M_scs.getServerUrl() + "/library/" + getResourceImageLibraryRelativePath(ref);
	}	
	
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getResourceURL(java.lang.String)
	 */
	public String getResourceURL(String ref){
		try{
			String tmp = ref.replaceFirst("/content", "");
			if(tmp.endsWith("/"))
				ContentHostingService.checkCollection(tmp);
			else
				ContentHostingService.checkResource(tmp);
		}catch(IdUnusedException e){
			return null;
		}catch(Exception e){
			// TypeException or PermissionException
			// It's OK since it exists
		}
		Reference r = EntityManager.newReference(ref);
		return Validator.escapeHtml(r.getUrl());
	}

	
	// ################################################################
	// Summary/report methods
	// ################################################################	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getSummaryVisitsTotals(java.lang.String)
	 */
	public SummaryVisitsTotals getSummaryVisitsTotals(String siteId){
		SummaryVisitsTotals svt = new SummaryVisitsTotalsImpl();
		
		Date now = new Date();
		
		Calendar c = Calendar.getInstance();
		Calendar cl = null;
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		
		cl = (Calendar) c.clone();		
		cl.add(Calendar.DATE, -6);
		Date lastWeek = cl.getTime();
		cl = (Calendar) c.clone();		
		cl.add(Calendar.DATE, -29);
		Date lastMonth = cl.getTime();
		cl = (Calendar) c.clone();		
		c.add(Calendar.MONTH, -11);
		Date lastYear = cl.getTime();
		
		double last7DaysVisitsAverage = round(getTotalSiteVisits(siteId, lastWeek, now) / 7.0, 1);
		double last30DaysVisitsAverage = round(getTotalSiteVisits(siteId, lastMonth, now) / 30.0, 1);
		double last365DaysVisitsAverage = round(getTotalSiteVisits(siteId, lastYear, now) / 365.0, 1);
		svt.setLast7DaysVisitsAverage(last7DaysVisitsAverage);
		svt.setLast30DaysVisitsAverage(last30DaysVisitsAverage);
		svt.setLast365DaysVisitsAverage(last365DaysVisitsAverage);
		
		long totalSiteUniqueVisits = getTotalSiteUniqueVisits(siteId);
		long totalSiteVisits = getTotalSiteVisits(siteId);
		int totalSiteUsers = getTotalSiteUsers(siteId);
		double percentageOfUsersThatVisitedSite = totalSiteUsers==0 ? 0 : (100 * totalSiteUniqueVisits) / (double) totalSiteUsers;
		svt.setTotalUniqueVisits(totalSiteUniqueVisits);
		svt.setTotalVisits(totalSiteVisits);
		svt.setTotalUsers(totalSiteUsers);
		svt.setPercentageOfUsersThatVisitedSite(round(percentageOfUsersThatVisitedSite, 1));
		
		return svt;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getSummaryActivityTotals(java.lang.String)
	 */
	public SummaryActivityTotals getSummaryActivityTotals(String siteId){
		PrefsData prefsdata = getPreferences(siteId, false);
		return getSummaryActivityTotals(siteId, prefsdata);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getSummaryActivityTotals(java.lang.String, org.sakaiproject.sitestats.api.PrefsData)
	 */
	public SummaryActivityTotals getSummaryActivityTotals(String siteId, PrefsData prefsdata){
		SummaryActivityTotals sat = new SummaryActivityTotalsImpl();
		
		Date now = new Date();
		
		Calendar c = Calendar.getInstance();
		Calendar cl = null;
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		
		cl = (Calendar) c.clone();		
		cl.add(Calendar.DATE, -6);
		Date lastWeek = cl.getTime();
		cl = (Calendar) c.clone();		
		cl.add(Calendar.DATE, -29);
		Date lastMonth = cl.getTime();
		cl = (Calendar) c.clone();		
		c.add(Calendar.MONTH, -11);
		Date lastYear = cl.getTime();
		
		double last7DaysActivityAverage = round(getTotalSiteActivity(siteId, prefsdata.getToolEventsStringList(), lastWeek, now) / 7.0, 1);
		double last30DaysActivityAverage = round(getTotalSiteActivity(siteId, prefsdata.getToolEventsStringList(), lastMonth, now) / 30.0, 1);
		double last365DaysActivityAverage = round(getTotalSiteActivity(siteId, prefsdata.getToolEventsStringList(), lastYear, now) / 365.0, 1);
		sat.setLast7DaysActivityAverage(last7DaysActivityAverage);
		sat.setLast30DaysActivityAverage(last30DaysActivityAverage);
		sat.setLast365DaysActivityAverage(last365DaysActivityAverage);
		
		long totalActivity = getTotalSiteActivity(siteId, prefsdata.getToolEventsStringList());
		sat.setTotalActivity(totalActivity);
		
		return sat;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getSummaryVisitsChartData(java.lang.String, java.lang.String)
	 */
	public SummaryVisitsChartData getSummaryVisitsChartData(String siteId, String viewType){
		SummaryVisitsChartData svc = new SummaryVisitsChartDataImpl(viewType);
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		Date finalDate = c.getTime();
		Date initialDate = null;
		
		List<SiteVisits> siteVisits = null;
		if(VIEW_WEEK.equals(viewType)){
			c.add(Calendar.DATE, -6);
			initialDate = c.getTime();
			siteVisits = getSiteVisits(siteId, initialDate, finalDate);
		}else if(VIEW_MONTH.equals(viewType)){
			c.add(Calendar.DATE, -29);
			initialDate = c.getTime();
			siteVisits = getSiteVisits(siteId, initialDate, finalDate);
		}else if(VIEW_YEAR.equals(viewType)){
			c.add(Calendar.MONTH, -11);
			initialDate = c.getTime();
			siteVisits = getSiteVisitsByMonth(siteId, initialDate, finalDate);
		}
		//LOG.info("siteVisits of [siteId:"+siteId+"] from ["+initialDate.toGMTString()+"] to ["+finalDate.toGMTString()+"]: "+siteVisits.toString());
		svc.setSiteVisits(siteVisits);
		return (siteVisits != null && siteVisits.size() > 0)? svc : null;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getSummaryActivityChartData(java.lang.String, java.lang.String, java.lang.String)
	 */
	public SummaryActivityChartData getSummaryActivityChartData(String siteId, String viewType, String chartType){
		PrefsData prefsdata = getPreferences(siteId, false);
		return getSummaryActivityChartData(siteId, prefsdata, viewType, chartType);
	}
	
	/**
	 * @param siteId
	 * @param prefsdata
	 * @param viewType
	 * @param chartType
	 * @return
	 */
	public SummaryActivityChartData getSummaryActivityChartData(String siteId, PrefsData prefsdata, String viewType, String chartType){
		SummaryActivityChartData sac = new SummaryActivityChartDataImpl(viewType, chartType);
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		Date finalDate = c.getTime();
		Date initialDate = null;
		
		if(CHARTTYPE_BAR.equals(chartType)){
			List<SiteActivity> siteActivity = null;
			if(VIEW_WEEK.equals(viewType)){
				c.add(Calendar.DATE, -6);
				initialDate = c.getTime();
				siteActivity = getSiteActivityByDay(siteId, prefsdata.getToolEventsStringList(), initialDate, finalDate);
			}else if(VIEW_MONTH.equals(viewType)){
				c.add(Calendar.DATE, -29);
				initialDate = c.getTime();
				siteActivity = getSiteActivityByDay(siteId, prefsdata.getToolEventsStringList(), initialDate, finalDate);
			}else if(VIEW_YEAR.equals(viewType)){
				c.add(Calendar.MONTH, -11);
				initialDate = c.getTime();
				siteActivity = getSiteActivityByMonth(siteId, prefsdata.getToolEventsStringList(), initialDate, finalDate);
			}
			//LOG.info("siteActivity of [siteId:"+siteId+"] from ["+initialDate.toGMTString()+"] to ["+finalDate.toGMTString()+"]: "+siteActivity.toString());
			sac.setSiteActivity(siteActivity);
			return (siteActivity != null && siteActivity.size() > 0)? sac : null;
		}else{
			List<SiteActivityByTool> siteActivityByTool = null;
			if(VIEW_WEEK.equals(viewType)){
				c.add(Calendar.DATE, -6);
				initialDate = c.getTime();
			}else if(VIEW_MONTH.equals(viewType)){
				c.add(Calendar.DATE, -29);
				initialDate = c.getTime();
			}else if(VIEW_YEAR.equals(viewType)){
				c.add(Calendar.MONTH, -11);
				initialDate = c.getTime();
			}
			siteActivityByTool = getSiteActivityByTool(siteId, prefsdata.getToolEventsStringList(), initialDate, finalDate);
			//LOG.info("siteActivityByTool of [siteId:"+siteId+"] from ["+initialDate.toGMTString()+"] to ["+finalDate.toGMTString()+"]: "+siteActivityByTool.toString());
			sac.setSiteActivityByTool(siteActivityByTool);
			return siteActivityByTool.size() > 0? sac : null;
		}
	}

	// ################################################################
	// EventInfo related methods
	// ################################################################
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getEventStats(java.lang.String, java.util.List)
	 */
	public List<EventStat> getEventStats(String siteId, List<String> events) {
		return getEventStats(siteId, events, null, getInitialActivityDate(siteId), null);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getEventStats(java.lang.String, java.util.List, java.lang.String, java.util.Date, java.util.Date)
	 */
	public List<EventStat> getEventStats(final String siteId, final List<String> events, 
			final String searchKey, final Date iDate, final Date fDate) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			final List<String> userIdList = searchUsers(searchKey, siteId);
			/* return if no users matched */
			if(userIdList != null && userIdList.size() == 0)				
				return new ArrayList<EventStat>();
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Criteria c = session.createCriteria(EventStatImpl.class)
							.add(Expression.eq("siteId", siteId))
							.add(Expression.in("eventId", events));
					if(!showAnonymousEvents)
						c.add(Expression.ne("userId", "?"));
					if(userIdList != null && userIdList.size() > 0)
						c.add(Expression.in("userId", userIdList));
					if(iDate != null)
						c.add(Expression.ge("date", iDate));
					if(fDate != null){
						// adjust final date
						Calendar ca = Calendar.getInstance();
						ca.setTime(fDate);
						ca.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = ca.getTime();
						c.add(Expression.lt("date", fDate2));
					}
					return c.list();
				}
			};
			return (List<EventStat>) getHibernateTemplate().execute(hcb);
		}
	}
	
	/*public List<CommonStatGrpByDate> getEventStatsGrpByDate(final String siteId, final List<String> events, 
			final String searchKey, final Date iDate, final Date fDate, final PagingPosition page) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			final List<String> userIdList = searchUsers(searchKey, siteId);
			if(userIdList != null && userIdList.size() == 0)				
				return new ArrayList<CommonStatGrpByDate>();

			String usersStr = "";
			String iDateStr = "";
			String fDateStr = "";
			if(userIdList != null && userIdList.size() > 0)
				usersStr = "and s.userId in (:users) ";
			if(iDate != null)
				iDateStr = "and s.date >= :idate ";
			if(fDate != null)
				fDateStr = "and s.date < :fdate ";
			if(!showAnonymousEvents)
				usersStr += " and s.userId != '?' ";
			final String hql = "select s.siteId, s.userId, s.eventId, sum(s.count), max(s.date) " + 
					"from EventStatImpl as s " +
					"where s.siteId = :siteid " +
					"and s.eventId in (:events) " +
					usersStr + iDateStr + fDateStr +
					"group by s.siteId, s.userId, s.eventId";
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = session.createQuery(hql);
					q.setString("siteid", siteId);
					q.setParameterList("events", events);
					if(userIdList != null && userIdList.size() > 0)
						q.setParameterList("users", userIdList);
					if(iDate != null)
						q.setDate("idate", iDate);
					if(fDate != null){
						// adjust final date
						Calendar c = Calendar.getInstance();
						c.setTime(fDate);
						c.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = c.getTime();
						q.setDate("fdate", fDate2);
					}
					if(page != null){
						q.setFirstResult(page.getFirst() - 1);
						q.setMaxResults(page.getLast() - page.getFirst());
					}
					List<Object> records = q.list();
					List<CommonStatGrpByDate> results = new ArrayList<CommonStatGrpByDate>();
					if(records.size() > 0){
						for(Iterator<Object> iter = records.iterator(); iter.hasNext();) {
							Object[] s = (Object[]) iter.next();
							CommonStatGrpByDate c = new CommonStatGrpByDateImpl();
							c.setSiteId((String)s[0]);
							c.setUserId((String)s[1]);
							c.setRef((String)s[2]);
							c.setCount(((Long)s[3]).longValue());
							c.setDate((Date)s[4]);
							results.add(c);
						}
						return results;
					}
					else return results;	
				}
			};
			return (List<CommonStatGrpByDate>) getHibernateTemplate().execute(hcb);
		}
	}*/
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getEventStatsGrpByDate(java.lang.String, java.util.List, java.util.Date, java.util.Date, java.util.List, boolean, org.sakaiproject.javax.PagingPosition)
	 */
	public List<CommonStatGrpByDate> getEventStatsGrpByDate(
			final String siteId,
			final List<String> events, 
			final Date iDate, final Date fDate,
			final List<String> userIds,
			final boolean inverseUserSelection,
			final PagingPosition page) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			String usersStr = "";
			String iDateStr = "";
			String fDateStr = "";
			if(userIds != null && !userIds.isEmpty())
				usersStr = "and s.userId in (:users) ";
			if(iDate != null)
				iDateStr = "and s.date >= :idate ";
			if(fDate != null)
				fDateStr = "and s.date < :fdate ";
			if(!showAnonymousEvents)
				usersStr += " and s.userId != '?' ";
			final String hql = "select s.siteId, s.userId, s.eventId, sum(s.count), max(s.date) " + 
					"from EventStatImpl as s " +
					"where s.siteId = :siteid " +
					"and s.eventId in (:events) " +
					usersStr + iDateStr + fDateStr +
					"group by s.siteId, s.userId, s.eventId";
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = session.createQuery(hql);
					q.setString("siteid", siteId);
					q.setParameterList("events", events);
					if(userIds != null && !userIds.isEmpty())
						q.setParameterList("users", userIds);
					if(iDate != null)
						q.setDate("idate", iDate);
					if(fDate != null){
						// adjust final date
						Calendar c = Calendar.getInstance();
						c.setTime(fDate);
						c.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = c.getTime();
						q.setDate("fdate", fDate2);
					}
					if(page != null){
						q.setFirstResult(page.getFirst() - 1);
						q.setMaxResults(page.getLast() - page.getFirst() + 1);
					}
					List<Object[]> records = q.list();
					List<CommonStatGrpByDate> results = new ArrayList<CommonStatGrpByDate>();
					List<String> siteUserIds = null;
					if(inverseUserSelection)
						siteUserIds = getSiteUsers(siteId);
					if(records.size() > 0){
						for(Iterator<Object[]> iter = records.iterator(); iter.hasNext();) {
							Object[] s = iter.next();
							if(!inverseUserSelection){
								CommonStatGrpByDate c = new CommonStatGrpByDateImpl();
								c.setSiteId((String)s[0]);
								c.setUserId((String)s[1]);
								c.setRef((String)s[2]);
								c.setCount(((Long)s[3]).longValue());
								c.setDate((Date)s[4]);
								results.add(c);
							}else{
								siteUserIds.remove((String)s[1]);
							}
						}
					}
					if(inverseUserSelection){
						long id = 0;
						Iterator<String> iU = siteUserIds.iterator();
						while(iU.hasNext()){
							String userId = iU.next();
							CommonStatGrpByDate c = new CommonStatGrpByDateImpl();
							c.setId(id++);
							c.setUserId(userId);
							c.setSiteId(siteId);
							c.setCount(0);
							results.add(c);
						}
					}
					return results;	
				}
			};
			return (List<CommonStatGrpByDate>) getHibernateTemplate().execute(hcb);
		}
	}
	
	public List<CommonStatGrpByDate> getEventStats(
			final String siteId,
			final List<String> events,
			final Date iDate, final Date fDate,
			final List<String> userIds,
			final boolean inverseUserSelection,
			final PagingPosition page, final String groupBy, final String sortBy, boolean sortAscending) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			final List<String> anonymousEvents = M_ers.getAnonymousEventIds();
			final String hql = buildEventStatsHql(siteId, events, anonymousEvents, iDate, fDate, userIds, inverseUserSelection, false, page, groupBy, sortBy, sortAscending);
			
			// DO IT!
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = session.createQuery(hql);
					q.setString("siteid", siteId);
					q.setParameterList("events", events);
					if(userIds != null && !userIds.isEmpty())
						q.setParameterList("users", userIds);
					if(iDate != null)
						q.setDate("idate", iDate);
					if(fDate != null){
						// adjust final date
						Calendar c = Calendar.getInstance();
						c.setTime(fDate);
						c.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = c.getTime();
						q.setDate("fdate", fDate2);
					}
					if(anonymousEvents != null && anonymousEvents.size() > 0) {
						q.setParameterList("anonymousEvents", anonymousEvents);
					}
					if(page != null){
						q.setFirstResult(page.getFirst() - 1);
						q.setMaxResults(page.getLast() - page.getFirst() + 1);
					}
					LOG.debug("getEventStats(): " + q.getQueryString());
					List<Object[]> records = q.list();
					List<CommonStatGrpByDate> results = new ArrayList<CommonStatGrpByDate>();
					List<String> siteUserIds = null;
					if(inverseUserSelection)
						siteUserIds = getSiteUsers(siteId);
					if(records.size() > 0){
						for(Iterator<Object[]> iter = records.iterator(); iter.hasNext();) {
							if(!inverseUserSelection){
								Object[] s = iter.next();
								CommonStatGrpByDate c = new CommonStatGrpByDateImpl();
								c.setSiteId((String)s[0]);
								c.setUserId((String)s[1]);
								c.setRef((String)s[2]);
								c.setCount(((Long)s[3]).longValue());
								c.setDate((Date)s[4]);
								results.add(c);
							}else{
								//siteUserIds.remove((String)s[1]);
								siteUserIds.remove((Object) iter.next());
							}
						}
					}
					if(inverseUserSelection){
						long id = 0;
						Iterator<String> iU = siteUserIds.iterator();
						while(iU.hasNext()){
							String userId = iU.next();
							CommonStatGrpByDate c = new CommonStatGrpByDateImpl();
							c.setId(id++);
							c.setUserId(userId);
							c.setSiteId(siteId);
							c.setCount(0);
							results.add(c);
						}
					}
					return results;	
				}
			};
			return (List<CommonStatGrpByDate>) getHibernateTemplate().execute(hcb);
		}
	}
	
	public int getEventStatsRowCount(
			final String siteId,
			final List<String> events,
			final Date iDate, final Date fDate,
			final List<String> userIds,
			final boolean inverseUserSelection,
			final PagingPosition page, final String groupBy, final String sortBy, boolean sortAscending) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			final List<String> anonymousEvents = M_ers.getAnonymousEventIds();
			final String hql = buildEventStatsHql(siteId, events, anonymousEvents, iDate, fDate, userIds, inverseUserSelection, true, page, groupBy, sortBy, sortAscending);
			
			// DO IT!
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = session.createQuery(hql);
					q.setString("siteid", siteId);
					q.setParameterList("events", events);
					if(userIds != null && !userIds.isEmpty())
						q.setParameterList("users", userIds);
					if(iDate != null)
						q.setDate("idate", iDate);
					if(fDate != null){
						// adjust final date
						Calendar c = Calendar.getInstance();
						c.setTime(fDate);
						c.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = c.getTime();
						q.setDate("fdate", fDate2);
					}
					if(anonymousEvents != null && anonymousEvents.size() > 0) {
						q.setParameterList("anonymousEvents", anonymousEvents);
					}
					LOG.debug("getEventStatsRowCount(): " + q.getQueryString());
					Integer rowCount = q.list().size();
					if(!inverseUserSelection) {
						return rowCount;
					}else{
						return getSiteUsers(siteId).size() - rowCount;
					}	
				}
			};
			return (Integer) getHibernateTemplate().execute(hcb);
		}
	}
	
	private String buildEventStatsHql(
			final String siteId,
			final List<String> events, 
			final List<String> anonymousEvents,
			final Date iDate, final Date fDate,
			final List<String> userIds,
			final boolean inverseUserSelection,
			final boolean countOnly,
			final PagingPosition page, final String groupBy, final String sortBy, boolean sortAscending) {
		
		StringBuilder _hql = new StringBuilder();
		
		// SELECT 
		if(!inverseUserSelection) {
			_hql.append("select s.siteId as site, ");
			if(anonymousEvents != null && anonymousEvents.size() > 0) {
				_hql.append("case when s.eventId not in (:anonymousEvents) then s.userId else '-' end as user, ");
			}else{
				_hql.append("s.userId as user, ");
			}
			_hql.append("s.eventId as ref, sum(s.count) as total, max(s.date) as date ");
		}else{
			if(anonymousEvents != null && anonymousEvents.size() > 0) {
				_hql.append("select distinct(case when s.eventId not in (:anonymousEvents) then s.userId else '-' end) as user ");
			}else{
				_hql.append("select distinct s.userId as user ");
			}
		}
		
		// FROM
		_hql.append("from EventStatImpl as s ");
		
		// WHERE
		_hql.append("where s.siteId = :siteid and s.eventId in (:events) ");
		if(userIds != null && !userIds.isEmpty()) {
			_hql.append("and s.userId in (:users) ");
		}
		if(iDate != null) {
			_hql.append("and s.date >= :idate ");
		}
		if(fDate != null) {
			_hql.append("and s.date < :fdate ");
		}
		if(!showAnonymousEvents) {
			_hql.append(" and s.userId != '?' ");
		}
		
		// GROUP BY
		if(groupBy == null) {
			_hql.append("group by s.siteId, s.userId, s.eventId ");
		}else{
			_hql.append(groupBy);
			_hql.append(' ');
		}
		
		// ORDER BY
		if(sortBy != null) {
			_hql.append("order by ");
			_hql.append(sortBy);
			_hql.append(sortAscending? "ASC" : "DESC");
			_hql.append(' ');
		}
		
		// get row count only
		//if(countOnly) {
		//	_hql.insert(0, "select count(*) as rowCount from (");
		//	_hql.append(") as theQuery");
		//}
		
		return _hql.toString();
	}
	
	public List<CommonStatGrpByDate> getResourceStats(
			final String siteId,
			final String resourceAction, final List<String> resourceIds,
			final Date iDate, final Date fDate,
			final List<String> userIds,
			final boolean inverseUserSelection,
			final PagingPosition page, 
			final String groupBy,
			final String sortBy, 
			final boolean sortAscending) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			final String hql = buildResourceStatsHql(siteId, resourceAction, resourceIds, iDate, fDate, userIds, inverseUserSelection, false, page, groupBy, sortBy, sortAscending);
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = session.createQuery(hql);
					q.setString("siteid", siteId);
					if(userIds != null && !userIds.isEmpty())
						q.setParameterList("users", userIds);
					if(resourceAction != null)
						q.setString("action", resourceAction);
					if(resourceIds != null && !resourceIds.isEmpty())
						q.setParameterList("resources", resourceIds);
					if(iDate != null)
						q.setDate("idate", iDate);
					if(fDate != null){
						// adjust final date
						Calendar c = Calendar.getInstance();
						c.setTime(fDate);
						c.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = c.getTime();
						q.setDate("fdate", fDate2);
					}
					if(page != null){
						q.setFirstResult(page.getFirst() - 1);
						q.setMaxResults(page.getLast() - page.getFirst() + 1);
					}
					LOG.debug("getResourceStats(): " + q.getQueryString());
					List<Object[]> records = q.list();
					List<CommonStatGrpByDate> results = new ArrayList<CommonStatGrpByDate>();
					List<String> siteUserIds = null;
					if(inverseUserSelection) {
						siteUserIds = getSiteUsers(siteId);
					}
					if(records.size() > 0){
						for(Iterator<Object[]> iter = records.iterator(); iter.hasNext();) {
							if(!inverseUserSelection){
								Object[] s = iter.next();
								CommonStatGrpByDate c = new CommonStatGrpByDateImpl();
								c.setSiteId((String)s[0]);
								c.setUserId((String)s[1]);
								c.setRef((String)s[2]);
								c.setRefImg(getResourceImage((String)s[2]));
								c.setRefUrl(getResourceURL((String)s[2]));
								c.setRefAction((String)s[3]);
								c.setCount(((Long)s[4]).longValue());
								c.setDate((Date)s[5]);
								results.add(c);
							}else{
								//siteUserIds.remove((String)s[1]);
								siteUserIds.remove((Object) iter.next());
							}
						}
					}
					if(inverseUserSelection){
						long id = 0;
						Iterator<String> iU = siteUserIds.iterator();
						while(iU.hasNext()){
							String userId = iU.next();
							CommonStatGrpByDate c = new CommonStatGrpByDateImpl();
							c.setId(id++);
							c.setUserId(userId);
							c.setSiteId(siteId);
							c.setCount(0);
							results.add(c);
						}
					}
					return results;	
				}
			};
			return (List<CommonStatGrpByDate>) getHibernateTemplate().execute(hcb);
		}
	}
	
	public int getResourceStatsRowCount(
			final String siteId,
			final String resourceAction, final List<String> resourceIds,
			final Date iDate, final Date fDate,
			final List<String> userIds,
			final boolean inverseUserSelection,
			final PagingPosition page, 
			final String groupBy,
			final String sortBy, 
			final boolean sortAscending) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			final String hql = buildResourceStatsHql(siteId, resourceAction, resourceIds, iDate, fDate, userIds, inverseUserSelection, false, page, groupBy, sortBy, sortAscending);
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = session.createQuery(hql);
					q.setString("siteid", siteId);
					if(userIds != null && !userIds.isEmpty())
						q.setParameterList("users", userIds);
					if(resourceAction != null)
						q.setString("action", resourceAction);
					if(resourceIds != null && !resourceIds.isEmpty())
						q.setParameterList("resources", resourceIds);
					if(iDate != null)
						q.setDate("idate", iDate);
					if(fDate != null){
						// adjust final date
						Calendar c = Calendar.getInstance();
						c.setTime(fDate);
						c.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = c.getTime();
						q.setDate("fdate", fDate2);
					}
					LOG.debug("getEventStatsRowCount(): " + q.getQueryString());
					Integer rowCount = q.list().size();
					if(!inverseUserSelection) {
						return rowCount;
					}else{
						return getSiteUsers(siteId).size() - rowCount;
					}	
				}
			};
			return (Integer) getHibernateTemplate().execute(hcb);
		}
	}
	
	private String buildResourceStatsHql(
			final String siteId,
			final String resourceAction, final List<String> resourceIds,
			final Date iDate, final Date fDate,
			final List<String> userIds,
			final boolean inverseUserSelection,
			final boolean countOnly,
			final PagingPosition page, final String groupBy, final String sortBy, boolean sortAscending) {
				
		StringBuilder _hql = new StringBuilder();
		
		// SELECT 
		if(!inverseUserSelection) {
			_hql.append("select s.siteId as site, s.userId as user, ");
			_hql.append("s.resourceRef, s.resourceAction, sum(s.count) as total, max(s.date) as date ");
		}else{
			_hql.append("select distinct s.userId as user ");
		}
		
		// FROM
		_hql.append("from ResourceStatImpl as s ");
		
		// WHERE
		_hql.append("where s.siteId = :siteid ");
		if(resourceAction != null) {
			_hql.append("and s.resourceAction = :action ");
		}
		if(resourceIds != null && !resourceIds.isEmpty()) {
			_hql.append("and s.resourceRef in (:resources) ");
		}
		if(userIds != null && !userIds.isEmpty()) {
			_hql.append("and s.userId in (:users) ");
		}
		if(iDate != null) {
			_hql.append("and s.date >= :idate ");
		}
		if(fDate != null) {
			_hql.append("and s.date < :fdate ");
		}
		if(!showAnonymousEvents) {
			_hql.append(" and s.userId != '?' ");
		}
		
		// GROUP BY
		if(groupBy == null) {
			_hql.append("group by s.siteId, s.userId, s.resourceRef, s.resourceAction ");
		}else{
			_hql.append("group by ");
			_hql.append(groupBy);
			_hql.append(' ');
		}
		
		// ORDER BY
		if(sortBy != null) {
			_hql.append("order by ");
			_hql.append(sortBy);
			_hql.append(sortAscending? "ASC" : "DESC");
			_hql.append(' ');
		}
		
		// get row count only
		//if(countOnly) {
		//	_hql.insert(0, "select count(*) as rowCount from (");
		//	_hql.append(") as theQuery");
		//}
		
		return _hql.toString();
	}
	
	/*public int countEventStatsGrpByDate(final String siteId, final List<String> events, 
			final String searchKey, final Date iDate, final Date fDate) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			final List userIdList = searchUsers(searchKey, siteId);
			if(userIdList != null && userIdList.size() == 0)				
				return 0;

			String usersStr = "";
			String iDateStr = "";
			String fDateStr = "";
			if(userIdList != null && userIdList.size() > 0)
				usersStr = "and s.userId in (:users) ";
			if(iDate != null)
				iDateStr = "and s.date >= :idate ";
			if(fDate != null)
				fDateStr = "and s.date < :fdate ";
			if(!showAnonymousEvents)
				usersStr += " and s.userId != '?' ";
			final String hql = "select count(*) " + 
					"from EventStatImpl as s " +
					"where s.siteId = :siteid " +
					"and s.eventId in (:events) " +
					usersStr + iDateStr + fDateStr +
					"group by s.siteId, s.userId, s.eventId";
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = session.createQuery(hql);
					q.setString("siteid", siteId);
					q.setParameterList("events", events);
					if(userIdList != null && userIdList.size() > 0)
						q.setParameterList("users", userIdList);
					if(iDate != null)
						q.setDate("idate", iDate);
					if(fDate != null){
						// adjust final date
						Calendar c = Calendar.getInstance();
						c.setTime(fDate);
						c.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = c.getTime();
						q.setDate("fdate", fDate2);
					}
					return new Integer(q.list().size());	
				}
			};
			return ((Integer) getHibernateTemplate().execute(hcb)).intValue();
		}
	}*/

	
	// ################################################################
	// Resource related methods
	// ################################################################
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getResourceStats(java.lang.String)
	 */
	public List<ResourceStat> getResourceStats(String siteId) {
		return getResourceStats(siteId, null, getInitialActivityDate(siteId), null);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getResourceStats(java.lang.String, java.lang.String, java.util.Date, java.util.Date, boolean)
	 */
	public List<ResourceStat> getResourceStats(final String siteId, final String searchKey, 
			final Date iDate, final Date fDate) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			final List<String> userIdList = searchUsers(searchKey, siteId);		
			/* return if no users matched */
			if(userIdList != null && userIdList.size() == 0)				
				return new ArrayList<ResourceStat>();	
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Criteria c = session.createCriteria(ResourceStatImpl.class)
							.add(Expression.eq("siteId", siteId));
					if(!showAnonymousEvents)
						c.add(Expression.ne("userId", "?"));
					if(userIdList != null && userIdList.size() > 0)
						c.add(Expression.in("userId", userIdList));
					if(iDate != null)
						c.add(Expression.ge("date", iDate));
					if(fDate != null){
						// adjust final date
						Calendar ca = Calendar.getInstance();
						ca.setTime(fDate);
						ca.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = ca.getTime();
						c.add(Expression.lt("date", fDate2));
					}
					return c.list();
				}
			};
			return (List<ResourceStat>) getHibernateTemplate().execute(hcb);
		}
	}
	
	/*public List<CommonStatGrpByDate> getResourceStatsGrpByDateAndAction(final String siteId, final String searchKey, 
			final Date iDate, final Date fDate, final PagingPosition page) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			final List<String> userIdList = searchUsers(searchKey, siteId);	
			if(userIdList != null && userIdList.size() == 0)				
				return new ArrayList();		
			
			String usersStr = "";
			String iDateStr = "";
			String fDateStr = "";
			if(userIdList != null && userIdList.size() > 0)
				usersStr = "and s.userId in (:users) ";
			if(iDate != null)
				iDateStr = "and s.date >= :idate ";
			if(fDate != null)
				fDateStr = "and s.date < :fdate ";
			if(!showAnonymousEvents)
				usersStr += " and s.userId != '?' ";
			final String hql = "select s.siteId, s.userId, s.resourceRef, s.resourceAction, sum(s.count), max(s.date) " + 
					"from ResourceStatImpl as s " +
					"where s.siteId = :siteid " +
					usersStr + iDateStr + fDateStr +
					"group by s.siteId, s.userId, s.resourceRef, s.resourceAction";
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = session.createQuery(hql);
					q.setString("siteid", siteId);
					if(userIdList != null && userIdList.size() > 0)
						q.setParameterList("users", userIdList);
					if(iDate != null)
						q.setDate("idate", iDate);
					if(fDate != null){
						// adjust final date
						Calendar c = Calendar.getInstance();
						c.setTime(fDate);
						c.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = c.getTime();
						q.setDate("fdate", fDate2);
					}
					if(page != null){
						q.setFirstResult(page.getFirst() - 1);
						q.setMaxResults(page.getLast() - page.getFirst());
					}
					List records = q.list();
					List results = new ArrayList();
					if(records.size() > 0){
						for(Iterator iter = records.iterator(); iter.hasNext();) {
							Object[] s = (Object[]) iter.next();
							CommonStatGrpByDate c = new CommonStatGrpByDateImpl();
							c.setSiteId((String)s[0]);
							c.setUserId((String)s[1]);
							c.setRef((String)s[2]);
							c.setRefImg(getResourceImage((String)s[2]));
							c.setRefUrl(getResourceURL((String)s[2]));
							c.setRefAction((String)s[3]);
							c.setCount(((Long)s[4]).longValue());
							c.setDate((Date)s[5]);
							results.add(c);
						}
						return results;
					}
					else return results;	
				}
			};
			return (List) getHibernateTemplate().execute(hcb);
		}
	}*/
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getResourceStatsGrpByDateAndAction(java.lang.String, java.lang.String, java.util.List, java.util.Date, java.util.Date, java.util.List, boolean, org.sakaiproject.javax.PagingPosition)
	 */
	public List<CommonStatGrpByDate> getResourceStatsGrpByDateAndAction(
			final String siteId,  
			final String resourceAction,
			final List<String> resourceIds,
			final Date iDate, final Date fDate,
			final List<String> userIds,
			final boolean inverseUserSelection,
			final PagingPosition page) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{			
			String usersStr = "";
			String resourcesActionStr = "";
			String resourcesStr = "";
			String iDateStr = "";
			String fDateStr = "";
			if(userIds != null && !userIds.isEmpty())
				usersStr = "and s.userId in (:users) ";
			if(resourceAction != null)
				resourcesActionStr = "and s.resourceAction = :action ";
			if(resourceIds != null && !resourceIds.isEmpty())
				resourcesStr = "and s.resourceRef in (:resources) ";
			if(iDate != null)
				iDateStr = "and s.date >= :idate ";
			if(fDate != null)
				fDateStr = "and s.date < :fdate ";
			if(!showAnonymousEvents)
				usersStr += " and s.userId != '?' ";
			final String hql = "select s.siteId, s.userId, s.resourceRef, s.resourceAction, sum(s.count), max(s.date) " + 
					"from ResourceStatImpl as s " +
					"where s.siteId = :siteid " +
					usersStr + resourcesActionStr + resourcesStr + iDateStr + fDateStr +
					"group by s.siteId, s.userId, s.resourceRef, s.resourceAction";
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = session.createQuery(hql);
					q.setString("siteid", siteId);
					if(userIds != null && !userIds.isEmpty())
						q.setParameterList("users", userIds);
					if(resourceAction != null)
						q.setString("action", resourceAction);
					if(resourceIds != null && !resourceIds.isEmpty())
						q.setParameterList("resources", resourceIds);
					if(iDate != null)
						q.setDate("idate", iDate);
					if(fDate != null){
						// adjust final date
						Calendar c = Calendar.getInstance();
						c.setTime(fDate);
						c.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = c.getTime();
						q.setDate("fdate", fDate2);
					}
					if(page != null){
						q.setFirstResult(page.getFirst() - 1);
						q.setMaxResults(page.getLast() - page.getFirst() + 1);
					}
					List<Object[]> records = q.list();
					List<CommonStatGrpByDate> results = new ArrayList<CommonStatGrpByDate>();
					List<String> siteUserIds = null;
					if(inverseUserSelection)
						siteUserIds = getSiteUsers(siteId);
					if(records.size() > 0){
						for(Iterator<Object[]> iter = records.iterator(); iter.hasNext();) {
							Object[] s = iter.next();
							if(!inverseUserSelection){
								CommonStatGrpByDate c = new CommonStatGrpByDateImpl();
								c.setSiteId((String)s[0]);
								c.setUserId((String)s[1]);
								c.setRef((String)s[2]);
								c.setRefImg(getResourceImage((String)s[2]));
								c.setRefUrl(getResourceURL((String)s[2]));
								c.setRefAction((String)s[3]);
								c.setCount(((Long)s[4]).longValue());
								c.setDate((Date)s[5]);
								results.add(c);
							}else{
								siteUserIds.remove((String)s[1]);
							}
						}
					}
					if(inverseUserSelection){
						long id = 0;
						Iterator<String> iU = siteUserIds.iterator();
						while(iU.hasNext()){
							String userId = iU.next();
							CommonStatGrpByDate c = new CommonStatGrpByDateImpl();
							c.setId(id++);
							c.setUserId(userId);
							c.setSiteId(siteId);
							c.setCount(0);
							results.add(c);
						}
					}
					return results;	
				}
			};
			return (List<CommonStatGrpByDate>) getHibernateTemplate().execute(hcb);
		}
	}
	
	/*public int countResourceStatsGrpByDateAndAction(final String siteId, final String searchKey,
			final Date iDate, final Date fDate){
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			final List userIdList = searchUsers(searchKey, siteId);	
			if(userIdList != null && userIdList.size() == 0)				
				return 0;		
			
			String usersStr = "";
			String iDateStr = "";
			String fDateStr = "";
			if(userIdList != null && userIdList.size() > 0)
				usersStr = "and s.userId in (:users) ";
			if(iDate != null)
				iDateStr = "and s.date >= :idate ";
			if(fDate != null)
				fDateStr = "and s.date < :fdate ";
			if(!showAnonymousEvents)
				usersStr += " and s.userId != '?' ";
			final String hql = "select count(*) " + 
					"from ResourceStatImpl as s " +
					"where s.siteId = :siteid " +
					usersStr + iDateStr + fDateStr +
					"group by s.siteId, s.userId, s.resourceRef, s.resourceAction";
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = session.createQuery(hql);
					q.setString("siteid", siteId);
					if(userIdList != null && userIdList.size() > 0)
						q.setParameterList("users", userIdList);
					if(iDate != null)
						q.setDate("idate", iDate);
					if(fDate != null){
						// adjust final date
						Calendar c = Calendar.getInstance();
						c.setTime(fDate);
						c.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = c.getTime();
						q.setDate("fdate", fDate2);
					}
					return new Integer(q.list().size());						
				}
			};
			return ((Integer) getHibernateTemplate().execute(hcb)).intValue();
		}
	}*/

	
	// ################################################################
	//  Site visits related methods
	// ################################################################
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getSiteVisits(java.lang.String)
	 */
	public List<SiteVisits> getSiteVisits(String siteId) {
		return getSiteVisits(siteId, getInitialActivityDate(siteId), null);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getSiteVisits(java.lang.String, java.util.Date, java.util.Date, boolean)
	 */
	public List<SiteVisits> getSiteVisits(final String siteId, final Date iDate, final Date fDate) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Criteria c = session.createCriteria(SiteVisitsImpl.class)
							.add(Expression.eq("siteId", siteId));
					if(iDate != null)
						c.add(Expression.ge("date", iDate));
					if(fDate != null){
						// adjust final date
						Calendar ca = Calendar.getInstance();
						ca.setTime(fDate);
						ca.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = ca.getTime();
						c.add(Expression.lt("date", fDate2));
					}
					return c.list();
				}
			};
			return (List<SiteVisits>) getHibernateTemplate().execute(hcb);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getSiteVisitsByMonth(java.lang.String, java.util.Date, java.util.Date, boolean)
	 */
	public List<SiteVisits> getSiteVisitsByMonth(final String siteId, final Date iDate, final Date fDate) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			String iDateStr = "";
			String fDateStr = "";
			if(M_sql.getVendor().equals("oracle")){
				if(iDate != null)
					iDateStr = "and es.EVENT_DATE >= :idate ";
				if(fDate != null)
					fDateStr = "and es.EVENT_DATE < :fdate ";
			}else{
				if(iDate != null)
					iDateStr = "and es.date >= :idate ";
				if(fDate != null)
					fDateStr = "and es.date < :fdate ";
			}
			final String hql = "select es.siteId, sum(es.count) ,count(distinct es.userId), year(es.date), month(es.date)"+
				"from EventStatImpl as es " +
				"where es.siteId = :siteid " +
				iDateStr + fDateStr +
				"  and es.userId != '?' " +
				"  and es.eventId = '"+SITEVISIT_EVENTID+"' " +
				"group by es.siteId, year(es.date), month(es.date)";
			final String oracleSql = "select es.SITE_ID as actSiteId, sum(es.EVENT_COUNT) as actVisits, count(distinct es.USER_ID) as actUnique, "+
				"  to_char(es.EVENT_DATE,'YYYY') as actYear, to_char(es.EVENT_DATE,'MM') as actMonth "+
				"from SST_EVENTS es " +
				"where es.SITE_ID = :siteid " +
				iDateStr + fDateStr +
				"  and es.USER_ID != '?' " +
				"  and es.EVENT_ID = '"+SITEVISIT_EVENTID+"' " + 
				"group by es.SITE_ID,to_char(es.EVENT_DATE,'YYYY'), to_char(es.EVENT_DATE,'MM')";
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = null;
					if(M_sql.getVendor().equals("oracle")){
						q = session.createSQLQuery(oracleSql)
							.addScalar("actSiteId")
							.addScalar("actVisits")
							.addScalar("actUnique")
							.addScalar("actYear")
							.addScalar("actMonth");
						
					}else{
						q = session.createQuery(hql);
					}
					q.setString("siteid", siteId);
					if(iDate != null)
						q.setDate("idate", iDate);
					if(fDate != null){
						// adjust final date
						Calendar c = Calendar.getInstance();
						c.setTime(fDate);
						c.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = c.getTime();
						q.setDate("fdate", fDate2);
					}
					List<Object[]> records = q.list();
					List<SiteVisits> results = new ArrayList<SiteVisits>();
					Calendar cal = Calendar.getInstance();
					if(records.size() > 0){
						for(Iterator<Object[]> iter = records.iterator(); iter.hasNext();) {
							Object[] s = iter.next();
							SiteVisits c = new SiteVisitsImpl();
							if(M_sql.getVendor().equals("oracle")){
								c.setSiteId((String)s[0]);
								c.setTotalVisits(((BigDecimal)s[1]).longValue());
								c.setTotalUnique(((BigDecimal)s[2]).longValue());
								cal.set(Calendar.YEAR, Integer.parseInt((String)s[3]));
								cal.set(Calendar.MONTH, Integer.parseInt((String)s[4]) - 1);							
							}else{
								c.setSiteId((String)s[0]);
								c.setTotalVisits(((Long)s[1]).longValue());
								c.setTotalUnique(((Integer)s[2]).intValue());
								cal.set(Calendar.YEAR, ((Integer)s[3]).intValue());
								cal.set(Calendar.MONTH, ((Integer)s[4]).intValue() - 1);
							}							
							c.setDate(cal.getTime());
							results.add(c);
						}
						return results;
					}
					else return results;	
				}
			};
			return (List<SiteVisits>) getHibernateTemplate().execute(hcb);
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getTotalSiteVisits(java.lang.String)
	 */
	public long getTotalSiteVisits(String siteId) {
		return getTotalSiteVisits(siteId, getInitialActivityDate(siteId), null);
	}

	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getTotalSiteVisits(java.lang.String, java.util.Date, java.util.Date, boolean)
	 */
	public long getTotalSiteVisits(final String siteId, final Date iDate, final Date fDate) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			String iDateStr = "";
			String fDateStr = "";
			if(iDate != null)
				iDateStr = "and ss.date >= :idate ";
			if(fDate != null)
				fDateStr = "and ss.date < :fdate ";
			final String hql = "select sum(ss.totalVisits) " +
					"from SiteVisitsImpl as ss " +
					"where ss.siteId = :siteid " +
					iDateStr + fDateStr +
					"group by ss.siteId";
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = session.createQuery(hql);
					q.setString("siteid", siteId);
					if(iDate != null)
						q.setDate("idate", iDate);
					if(fDate != null){
						// adjust final date
						Calendar c = Calendar.getInstance();
						c.setTime(fDate);
						c.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = c.getTime();
						q.setDate("fdate", fDate2);
					}
					List<Object[]> res = q.list();
					if(res.size() > 0) return res.get(0);
					else return Long.valueOf(0);	
				}
			};
			return ((Long) getHibernateTemplate().execute(hcb)).longValue();
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getTotalSiteUniqueVisits(java.lang.String, boolean)
	 */
	public long getTotalSiteUniqueVisits(final String siteId) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			final String hql = "select count(distinct es.userId) " +
					"from EventStatImpl as es " +
					"where es.siteId = :siteid " +
					"and es.userId != '?' " +
					"and es.eventId = 'pres.begin'";
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = session.createQuery(hql);
					q.setString("siteid", siteId);
					List<Object[]> res = q.list();
					if(res.size() > 0) return res.get(0);
					else return Long.valueOf(0);	
				}
			};
			return ((Integer) getHibernateTemplate().execute(hcb)).longValue();
		}
	}

	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getTotalSiteUniqueVisits(java.lang.String, java.util.Date, java.util.Date, boolean)
	 */
	public long getTotalSiteUniqueVisits(final String siteId, final Date iDate, final Date fDate) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			String iDateStr = "";
			String fDateStr = "";
			if(iDate != null)
				iDateStr = "and es.date >= :idate ";
			if(fDate != null)
				fDateStr = "and es.date < :fdate ";
			final String hql = "select count(distinct es.userId) " +
					"from EventStatImpl as es " +
					"where es.siteId = :siteid " +
					"and es.userId != '?'" +
					iDateStr + fDateStr +
					"and es.eventId = 'pres.begin'";
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = session.createQuery(hql);
					q.setString("siteid", siteId);
					if(iDate != null)
						q.setDate("idate", iDate);
					if(fDate != null){
						// adjust final date
						Calendar c = Calendar.getInstance();
						c.setTime(fDate);
						c.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = c.getTime();
						q.setDate("fdate", fDate2);
					}
					List<Object[]> res = q.list();
					if(res.size() > 0) return res.get(0);
					else return Long.valueOf(0);	
				}
			};
			return ((Integer) getHibernateTemplate().execute(hcb)).longValue();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getTotalSiteUsers(java.lang.String)
	 */
	public int getTotalSiteUsers(String siteId){
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			try{
				return M_ss.getSite(siteId).getMembers().size();
			}catch(IdUnusedException e){
				LOG.warn("Unable to get total site users for site id: "+siteId, e);
				return 0;
			}
		}
	}


	// ################################################################
	// Site activity related methods
	// ################################################################	
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getSiteActivity(java.lang.String, boolean)
	 */
	public List<SiteActivity> getSiteActivity(String siteId, List<String> events) {
		return getSiteActivity(siteId, events, getInitialActivityDate(siteId), null);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getSiteActivity(java.lang.String, java.util.Date, java.util.Date, boolean)
	 */
	public List<SiteActivity> getSiteActivity(final String siteId, final List<String> events, final Date iDate, final Date fDate) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Criteria c = session.createCriteria(SiteActivityImpl.class)
							.add(Expression.eq("siteId", siteId))
							.add(Expression.in("eventId", events));
					if(iDate != null)
						c.add(Expression.ge("date", iDate));
					if(fDate != null){
						// adjust final date
						Calendar ca = Calendar.getInstance();
						ca.setTime(fDate);
						ca.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = ca.getTime();
						c.add(Expression.lt("date", fDate2));
					}
					return c.list();
				}
			};
			return (List<SiteActivity>) getHibernateTemplate().execute(hcb);
		}
	}
	
	public List<SiteActivity> getSiteActivityByDay(final String siteId, final List<String> events, final Date iDate, final Date fDate) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			String iDateStr = "";
			String fDateStr = "";
			if(M_sql.getVendor().equals("oracle")){
				if(iDate != null)
					iDateStr = "and s.ACTIVITY_DATE >= :idate ";
				if(fDate != null)
					fDateStr = "and s.ACTIVITY_DATE < :fdate ";
			}else{
				if(iDate != null)
					iDateStr = "and s.date >= :idate ";
				if(fDate != null)
					fDateStr = "and s.date < :fdate ";
			}
			final String hql = "select s.siteId, sum(s.count), year(s.date), month(s.date), day(s.date) " + 
					"from SiteActivityImpl as s " +
					"where s.siteId = :siteid " +
					"and s.eventId in (:eventlist) " +
					iDateStr + fDateStr +
					"group by s.siteId, year(s.date), month(s.date), day(s.date)";
			// to_char(to_date(sa.ACTIVITY_DATE,'DD-Mon-YY'),'YYYY') as actYear
			final String oracleSql = 
				"select s.SITE_ID as actSiteId, sum(s.ACTIVITY_COUNT) as actCount, to_char(s.ACTIVITY_DATE,'YYYY') as actYear, to_char(s.ACTIVITY_DATE,'MM') as actMonth, to_char(s.ACTIVITY_DATE,'DD') as actDay " + 
				"from SST_SITEACTIVITY s " +
				"where s.SITE_ID = :siteid " +
				"and s.EVENT_ID in (:eventlist) " +
				iDateStr + fDateStr +
				"group by s.SITE_ID, to_char(s.ACTIVITY_DATE,'YYYY'), to_char(s.ACTIVITY_DATE,'MM'), to_char(s.ACTIVITY_DATE,'DD')";
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = null;
					if(M_sql.getVendor().equals("oracle")){
						q = session.createSQLQuery(oracleSql)
							.addScalar("actSiteId")
							.addScalar("actCount")
							.addScalar("actYear")
							.addScalar("actMonth")
							.addScalar("actDay");
						
					}else{
						q = session.createQuery(hql);
					}
					q.setString("siteid", siteId);
					if(events != null && events.size() > 0)
						q.setParameterList("eventlist", events);
					else
						q.setParameterList("eventlist", M_ers.getEventIds());
					if(iDate != null)
						q.setDate("idate", iDate);
					if(fDate != null){
						// adjust final date
						Calendar c = Calendar.getInstance();
						c.setTime(fDate);
						c.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = c.getTime();
						q.setDate("fdate", fDate2);
					}
					List<Object[]> records = q.list();
					List<SiteActivity> results = new ArrayList<SiteActivity>();
					Calendar cal = Calendar.getInstance();
					if(records.size() > 0){
						for(Iterator<Object[]> iter = records.iterator(); iter.hasNext();) {
							Object[] s = iter.next();
							SiteActivity c = new SiteActivityImpl();
							if(M_sql.getVendor().equals("oracle")){
								c.setSiteId((String)s[0]);
								c.setCount(((BigDecimal)s[1]).longValue());
								cal.set(Calendar.YEAR, Integer.parseInt((String)s[2]));
								cal.set(Calendar.MONTH, Integer.parseInt((String)s[3]) - 1);
								cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt((String)s[4]));								
							}else{
								c.setSiteId((String)s[0]);
								c.setCount(((Long)s[1]).longValue());
								cal.set(Calendar.YEAR, ((Integer)s[2]).intValue());
								cal.set(Calendar.MONTH, ((Integer)s[3]).intValue() - 1);
								cal.set(Calendar.DAY_OF_MONTH, ((Integer)s[4]).intValue());
							}
							c.setDate(cal.getTime());
							c.setEventId(null);
							results.add(c);
						}
						return results;
					}
					else return results;	
				}
			};
			return (List<SiteActivity>) getHibernateTemplate().execute(hcb);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getSiteActivityByMonth(java.lang.String, java.util.List, java.util.Date, java.util.Date, boolean)
	 */
	public List<SiteActivity> getSiteActivityByMonth(final String siteId, final List<String> events, final Date iDate, final Date fDate) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			String iDateStr = "";
			String fDateStr = "";
			if(M_sql.getVendor().equals("oracle")){
				if(iDate != null)
					iDateStr = "and s.ACTIVITY_DATE >= :idate ";
				if(fDate != null)
					fDateStr = "and s.ACTIVITY_DATE < :fdate ";
			}else{
				if(iDate != null)
					iDateStr = "and s.date >= :idate ";
				if(fDate != null)
					fDateStr = "and s.date < :fdate ";
			}
			final String hql = "select s.siteId, sum(s.count), year(s.date), month(s.date) " + 
					"from SiteActivityImpl as s " +
					"where s.siteId = :siteid " +
					"and s.eventId in (:eventlist) " +
					iDateStr + fDateStr +
					"group by s.siteId, year(s.date), month(s.date)";
			final String oracleSql = "select s.SITE_ID as actSiteId, sum(s.ACTIVITY_COUNT) as actCount, to_char(s.ACTIVITY_DATE,'YYYY') as actYear, to_char(s.ACTIVITY_DATE,'MM') as actMonth " + 
			"from SST_SITEACTIVITY s " +
			"where s.SITE_ID = :siteid " +
			"and s.EVENT_ID in (:eventlist) " +
			iDateStr + fDateStr +
			"group by s.SITE_ID, to_char(s.ACTIVITY_DATE,'YYYY'), to_char(s.ACTIVITY_DATE,'MM')";
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = null;
					if(M_sql.getVendor().equals("oracle")){
						q = session.createSQLQuery(oracleSql)
							.addScalar("actSiteId")
							.addScalar("actCount")
							.addScalar("actYear")
							.addScalar("actMonth");
						
					}else{
						q = session.createQuery(hql);
					}
					q.setString("siteid", siteId);
					if(events != null && events.size() > 0)
						q.setParameterList("eventlist", events);
					else
						q.setParameterList("eventlist", M_ers.getEventIds());
					if(iDate != null)
						q.setDate("idate", iDate);
					if(fDate != null){
						// adjust final date
						Calendar c = Calendar.getInstance();
						c.setTime(fDate);
						c.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = c.getTime();
						q.setDate("fdate", fDate2);
					}
					List<Object[]> records = q.list();
					List<SiteActivity> results = new ArrayList<SiteActivity>();
					Calendar cal = Calendar.getInstance();
					if(records.size() > 0){
						for(Iterator<Object[]> iter = records.iterator(); iter.hasNext();) {
							Object[] s = iter.next();
							SiteActivity c = new SiteActivityImpl();
							if(M_sql.getVendor().equals("oracle")){
								c.setSiteId((String)s[0]);
								c.setCount(((BigDecimal)s[1]).longValue());
								cal.set(Calendar.YEAR, Integer.parseInt((String)s[2]));
								cal.set(Calendar.MONTH, Integer.parseInt((String)s[3]) - 1);								
							}else{
								c.setSiteId((String)s[0]);
								c.setCount(((Long)s[1]).longValue());
								cal.set(Calendar.YEAR, ((Integer)s[2]).intValue());
								cal.set(Calendar.MONTH, ((Integer)s[3]).intValue() - 1);
							}							
							c.setDate(cal.getTime());
							c.setEventId(null);
							results.add(c);
						}
						return results;
					}
					else return results;	
				}
			};
			return (List<SiteActivity>) getHibernateTemplate().execute(hcb);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getSiteActivityByTool(java.lang.String, java.util.List, java.util.Date, java.util.Date)
	 */
	public List<SiteActivityByTool> getSiteActivityByTool(final String siteId, final List<String> events, final Date iDate, final Date fDate) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			String iDateStr = "";
			String fDateStr = "";
			if(M_sql.getVendor().equals("oracle")){
				if(iDate != null)
					iDateStr = "and s.ACTIVITY_DATE >= :idate ";
				if(fDate != null)
					fDateStr = "and s.ACTIVITY_DATE < :fdate ";
			}else{
				if(iDate != null)
					iDateStr = "and s.date >= :idate ";
				if(fDate != null)
					fDateStr = "and s.date < :fdate ";
			}
			final String hql = "select s.siteId, sum(s.count), s.eventId " + 
					"from SiteActivityImpl as s " +
					"where s.siteId = :siteid " +
					"and s.eventId in (:eventlist) " +
					iDateStr + fDateStr +
					"group by s.siteId, s.eventId";
			// to_char(to_date(sa.ACTIVITY_DATE,'DD-Mon-YY'),'YYYY') as actYear
			final String oracleSql = 
				"select s.SITE_ID as actSiteId, sum(s.ACTIVITY_COUNT) as actCount, s.EVENT_ID as actEventId " + 
				"from SST_SITEACTIVITY s " +
				"where s.SITE_ID = :siteid " +
				"and s.EVENT_ID in (:eventlist) " +
				iDateStr + fDateStr +
				"group by s.SITE_ID, s.EVENT_ID";
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = null;
					if(M_sql.getVendor().equals("oracle")){
						q = session.createSQLQuery(oracleSql)
							.addScalar("actSiteId")
							.addScalar("actCount")
							.addScalar("actEventId");
						
					}else{
						q = session.createQuery(hql);
					}
					q.setString("siteid", siteId);
					if(events != null && events.size() > 0)
						q.setParameterList("eventlist", events);
					else
						q.setParameterList("eventlist", M_ers.getEventIds());
					if(iDate != null)
						q.setDate("idate", iDate);
					if(fDate != null){
						// adjust final date
						Calendar c = Calendar.getInstance();
						c.setTime(fDate);
						c.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = c.getTime();
						q.setDate("fdate", fDate2);
					}
					List<Object[]> records = q.list();
					List<SiteActivityByTool> results = new ArrayList<SiteActivityByTool>();
					if(records.size() > 0){
						Map<String,ToolInfo> eventIdToolMap = M_ers.getEventIdToolMap();
						Map<String,SiteActivityByTool> toolidSABT = new HashMap<String, SiteActivityByTool>();
						List<ToolInfo> allTools = M_ers.getEventRegistry();
						for(Iterator<Object[]> iter = records.iterator(); iter.hasNext();) {
							Object[] s = iter.next();
							SiteActivityByTool c = new SiteActivityByToolImpl();
							if(M_sql.getVendor().equals("oracle")){
								c.setSiteId((String)s[0]);
								c.setCount(((BigDecimal)s[1]).longValue());								
							}else{
								c.setSiteId((String)s[0]);
								c.setCount(((Long)s[1]).longValue());
							}
							ToolInfo toolInfo = eventIdToolMap.get((String)s[2]);
							if(toolInfo != null) {
								String toolId = toolInfo.getToolId();
								SiteActivityByTool existing = toolidSABT.get(toolId);
								if(existing != null){
									// increment count for same tool
									existing.setCount(existing.getCount() + c.getCount());
									toolidSABT.put(toolId, existing);
								}else{
									// add new tool count
									int ix = allTools.indexOf(new ToolInfoImpl(toolId));
									c.setTool(allTools.get(ix));
									toolidSABT.put(toolId, c);
								}
							}
						}
						// aggregate
						results.addAll(toolidSABT.values());
						return results;
					}
					else return results;	
				}
			};
			return (List<SiteActivityByTool>) getHibernateTemplate().execute(hcb);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getSiteActivityGrpByDate(java.lang.String, java.util.List, java.util.Date, java.util.Date, boolean)
	 */
	public List<SiteActivity> getSiteActivityGrpByDate(final String siteId, final List<String> events, final Date iDate, final Date fDate) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			String iDateStr = "";
			String fDateStr = "";
			if(iDate != null)
				iDateStr = "and s.date >= :idate ";
			if(fDate != null)
				fDateStr = "and s.date < :fdate ";
			final String hql = "select s.siteId, sum(s.count),s.date " + 
					"from SiteActivityImpl as s " +
					"where s.siteId = :siteid " +
					"and s.eventId in (:eventlist) " +
					iDateStr + fDateStr +
					"group by s.siteId, s.date";
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = session.createQuery(hql);
					q.setString("siteid", siteId);
					if(events != null && events.size() > 0)
						q.setParameterList("eventlist", events);
					else
						q.setParameterList("eventlist", M_ers.getEventIds());
					if(iDate != null)
						q.setDate("idate", iDate);
					if(fDate != null){
						// adjust final date
						Calendar c = Calendar.getInstance();
						c.setTime(fDate);
						c.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = c.getTime();
						q.setDate("fdate", fDate2);
					}
					List<Object[]> records = q.list();
					List<SiteActivity> results = new ArrayList<SiteActivity>();
					if(records.size() > 0){
						for(Iterator<Object[]> iter = records.iterator(); iter.hasNext();) {
							Object[] s = iter.next();
							SiteActivity c = new SiteActivityImpl();
							c.setSiteId((String)s[0]);
							c.setCount(((Long)s[1]).longValue());
							Date recDate = (Date)s[2];
							c.setDate(recDate);
							c.setEventId(null);
							results.add(c);
						}
						return results;
					}
					else return results;	
				}
			};
			return (List<SiteActivity>) getHibernateTemplate().execute(hcb);
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getTotalSiteActivity(java.lang.String, boolean)
	 */
	public long getTotalSiteActivity(String siteId, List<String> events) {
		return getTotalSiteActivity(siteId, events, getInitialActivityDate(siteId), null);
	}

	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getTotalSiteActivity(java.lang.String, java.util.Date, java.util.Date, boolean)
	 */
	public long getTotalSiteActivity(final String siteId, final List<String> events, final Date iDate, final Date fDate) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			String iDateStr = "";
			String fDateStr = "";
			if(iDate != null)
				iDateStr = "and ss.date >= :idate ";
			if(fDate != null)
				fDateStr = "and ss.date < :fdate ";
			final String hql = "select sum(ss.count) " +
					"from SiteActivityImpl as ss " +
					"where ss.eventId in (:eventlist) " +
					"and ss.siteId = :siteid " +
					iDateStr + fDateStr +
					"group by ss.siteId";
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = session.createQuery(hql);
					q.setString("siteid", siteId);
					if(events != null && events.size() > 0)
						q.setParameterList("eventlist", events);
					else
						q.setParameterList("eventlist", M_ers.getEventIds());
					if(iDate != null)
						q.setDate("idate", iDate);
					if(fDate != null){
						// adjust final date
						Calendar c = Calendar.getInstance();
						c.setTime(fDate);
						c.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = c.getTime();
						q.setDate("fdate", fDate2);
					}
					List<Object[]> res = q.list();
					if(res.size() > 0) return res.get(0);
					else return Long.valueOf(0);	
				}
			};
			return ((Long) getHibernateTemplate().execute(hcb)).longValue();
		}
	}

	
	// ################################################################
	// Utility methods
	// ################################################################
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getInitialActivityDate(java.lang.String)
	 */
	public Date getInitialActivityDate(String siteId) {
		Date date = null;
		try{
			date = new Date(M_ss.getSite(siteId).getCreatedTime().getTime());
		}catch(Exception e){
			return new Date(0);
		}
		return date;
	}
	
	public boolean isEventContextSupported() {
		return isEventContextSupported;
	}
	
	private void checkForEventContextSupport() {
		try{
			Event.class.getMethod("getContext", null);
			isEventContextSupported = true;
			logger.info("init(): - Event.getContext() method IS supported.");
		}catch(SecurityException e){
			isEventContextSupported = false;
			logger.warn("init(): - security exception while checking for Event.getContext() method.", e);
		}catch(NoSuchMethodException e){
			isEventContextSupported = false;
			logger.info("init(): - Event.getContext() method is NOT supported.");
		}catch(Exception e){
			isEventContextSupported = false;
			logger.warn("init(): - unknown exception while checking for Event.getContext() method.", e);
		}
	}
	
	private List<String> searchUsers(String searchKey, String siteId){
		if(searchKey == null || searchKey.trim().equals(""))
			return null;
		List<String> usersWithStats = getUsersWithStats(siteId);
		List<String> userIdList = new ArrayList<String>();
		Iterator<String> i = usersWithStats.iterator();
		while(i.hasNext()){
			String userId = i.next();
			boolean match = false;
			if(userId.toLowerCase().matches("(.*)"+searchKey.toLowerCase()+"(.*)"))
				match = true;
			else
				try{
					User u = M_uds.getUser(userId);
					if(u.getEid().toLowerCase().matches("(.*)"+searchKey.toLowerCase()+"(.*)")
							|| u.getFirstName().toLowerCase().matches("(.*)"+searchKey.toLowerCase()+"(.*)")
							|| u.getLastName().toLowerCase().matches("(.*)"+searchKey.toLowerCase()+"(.*)"))
						match = true;
				}catch(Exception e) {
					match = false;
				}
			
			if(match)
				userIdList.add(userId);
		}
		return userIdList;
	}
	
	private List<String> getUsersWithStats(final String siteId){
		final String hql = "select distinct(ss.userId) " +
		"from EventStatImpl as ss " +
		"where ss.siteId = :siteid ";

		HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query q = session.createQuery(hql);
				q.setString("siteid", siteId);
				return q.list();	
			}
		};
		return ((List<String>) getHibernateTemplate().execute(hcb));
	}

	/**
	 * Checks if a given user has permissions access a tool in a site. This is
	 * checked agains the following tags in tool xml file:<br>
	 * <configuration name="roles.allow" value="maintain,Instructor" /><br>
	 * <configuration name="roles.deny" value="access,guest" /> <br>
	 * Both, one or none of this configuration tags can be specified. By
	 * default, an user has permissions to see the tool in site.<br>
	 * Permissions are checked in the order: Allow, Deny.
	 * @param tool ToolInfo to check permissions on.
	 * @param roleId Current user's role.
	 * @return Whether user has permissions to this tool in this site.
	 */
	/*private boolean isToolAllowedForRole(org.sakaiproject.tool.api.Tool tool, String roleId) {
		if(tool == null) return false;
		String TOOL_CFG_ROLES_ALLOW = "roles.allow";
		String TOOL_CFG_ROLES_DENY = "roles.deny";
		Properties roleConfig = tool.getRegisteredConfig();
		String toolTitle = tool.getTitle();
		boolean allowRuleSpecified = roleConfig.containsKey(TOOL_CFG_ROLES_ALLOW);
		boolean denyRuleSpecified = roleConfig.containsKey(TOOL_CFG_ROLES_DENY);
		
		// allow by default, when no config keys are present
		if(!allowRuleSpecified && !allowRuleSpecified) return true;
		
		boolean allowed = true;
		if(allowRuleSpecified){
			allowed = false;
			boolean found = false;
			String[] result = roleConfig.getProperty(TOOL_CFG_ROLES_ALLOW).split("\\,");
		    for (int x=0; x<result.length; x++){
		    	if(result[x].trim().equals(roleId)){
		        	 found = true;
		        	 break;
		         }
		    }
			if(found){
				LOG.debug("ToolInfo config '"+TOOL_CFG_ROLES_ALLOW+"' allowed access to '"+roleId+"' in "+toolTitle);
				allowed = true;
			}
		}
		if(denyRuleSpecified){
			if(!allowRuleSpecified)
				allowed = true;
			boolean found = false;
			String[] result = roleConfig.getProperty(TOOL_CFG_ROLES_DENY).split("\\,");
		    for (int x=0; x<result.length; x++){
		    	if(result[x].trim().equals(roleId)){
		        	 found = true;
		        	 break;
		         }
		    }
			if(found){
				LOG.debug("ToolInfo config '"+TOOL_CFG_ROLES_DENY+"' denied access to '"+roleId+"' in "+toolTitle);
				allowed = false;
			}
		}else if(!allowRuleSpecified)
			allowed = true;
		LOG.debug("Allowed access to '"+roleId+"' in "+toolTitle+"? "+allowed);
		return allowed;
	}*/

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
