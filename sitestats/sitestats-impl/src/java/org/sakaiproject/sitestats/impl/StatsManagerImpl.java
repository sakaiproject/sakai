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

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import org.apache.commons.beanutils.PropertyUtils;
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
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.sitestats.api.CommonStatGrpByDate;
import org.sakaiproject.sitestats.api.EventFactory;
import org.sakaiproject.sitestats.api.EventInfo;
import org.sakaiproject.sitestats.api.EventStat;
import org.sakaiproject.sitestats.api.Prefs;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.Report;
import org.sakaiproject.sitestats.api.ReportParams;
import org.sakaiproject.sitestats.api.ResourceStat;
import org.sakaiproject.sitestats.api.SiteActivity;
import org.sakaiproject.sitestats.api.SiteActivityByTool;
import org.sakaiproject.sitestats.api.SiteVisits;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.SummaryActivityChartData;
import org.sakaiproject.sitestats.api.SummaryActivityTotals;
import org.sakaiproject.sitestats.api.SummaryVisitsChartData;
import org.sakaiproject.sitestats.api.SummaryVisitsTotals;
import org.sakaiproject.sitestats.api.ToolFactory;
import org.sakaiproject.sitestats.api.ToolInfo;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;


/**
 * @author Nuno Fernandes
 *
 */
public class StatsManagerImpl extends HibernateDaoSupport implements StatsManager {
	private Log							LOG										= LogFactory.getLog(StatsManagerImpl.class);
	private static String				bundleName								= "org.sakaiproject.sitestats.impl.bundle.Messages";
	private static ResourceLoader		msgs									= new ResourceLoader(bundleName);

	/** Spring bean members */
	private boolean						enableSiteVisits						= org.sakaiproject.component.cover.ServerConfigurationService.getBoolean("display.users.present", false)
																					|| org.sakaiproject.component.cover.ServerConfigurationService.getBoolean("presence.events.log", false);
	private boolean                     enableSiteActivity						= org.sakaiproject.component.cover.ServerConfigurationService.getBoolean("enableSiteActivity@org.sakaiproject.sitestats.api.StatsManager", true);
	private boolean 				    visitsInfoAvailable						= enableSiteVisits; //org.sakaiproject.component.cover.ServerConfigurationService.getBoolean( "display.users.present", true);
	private boolean						enableServerWideStats				= false;
	private String						customToolEventsDefinitionFile			= null;
	private String						customToolEventsAddDefinitionFile		= null;
	private String						customToolEventsRemoveDefinitionFile	= null;
	private String						chartBackgroundColor					= "white";
	private boolean						chartIn3D								= true;
	private float						chartTransparency						= 0.80f;
	private boolean						itemLabelsVisible						= false;
	private boolean						lastJobRunDateVisible					= true;
	private boolean						isEventContextSupported					= false;

	/** Controller fields */
	private List<ToolInfo>				toolEventsDefinition					= null;
	private List<String>				toolEventIds							= null;
	private Map<String,ToolInfo>		eventIdToolMap;
	private boolean						showAnonymousEvents						= false;

	/** Sakai services */
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
	public void setToolEventsDefinitionFile(String file) {
		customToolEventsDefinitionFile = file;
	}
	
	public String getToolEventsDefinitionFile() {
		return customToolEventsDefinitionFile;
	}
	
	public void setToolEventsAddDefinitionFile(String file) {
		customToolEventsAddDefinitionFile = file;
	}
	
	public String getToolEventsAddDefinitionFile() {
		return customToolEventsAddDefinitionFile;
	}
	
	public void setToolEventsRemoveDefinitionFile(String file) {
		customToolEventsRemoveDefinitionFile = file;
	}
	
	public String getToolEventsRemoveDefinitionFile() {
		return customToolEventsRemoveDefinitionFile;
	}
	
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
		
		// Load events definition file
		loadToolEventsDefinitionFile();
		
		logger.info("init(): - (Event.getContext()?, site visits enabled, charts background color, charts in 3D, charts transparency, item labels visible on bar charts) : " +
							isEventContextSupported+','+enableSiteVisits+','+chartBackgroundColor+','+chartIn3D+','+chartTransparency+','+itemLabelsVisible);		
	}
	

	// ################################################################
	// Registered/configured events
	// ################################################################
	private void loadToolEventsDefinitionFile() {
		boolean loadedCustomDefFile = false;
		
		// user-specified tool events definition
		if(customToolEventsDefinitionFile != null) {
			File customDefs = new File(customToolEventsDefinitionFile);
			if(customDefs.exists()){
				try{
					logger.info("init(): - loading custom tool events definitions from: " + customDefs.getAbsolutePath());
					toolEventsDefinition = parseToolEventsDefinition(new FileInputStream(customDefs));
					loadedCustomDefFile = true;
				}catch(Throwable t){
					logger.warn("init(): - trouble loading tool events definitions from : " + customDefs.getAbsolutePath(), t);
				}
			}else {
				logger.warn("init(): - custom tool events definitions file not found: "+customDefs.getAbsolutePath());
			}
		}
		
		// default tool events definition
		if(!loadedCustomDefFile){
			ClassPathResource defaultDefs = new ClassPathResource("org/sakaiproject/sitestats/config/"+TOOL_EVENTS_DEF_FILE);
			try{
				logger.info("init(): - loading default tool events definitions from: " + defaultDefs.getPath()+". A custom one for adding/removing events can be specified in sakai.properties with the property: toolEventsDefinitionFile@org.sakaiproject.sitestats.api.StatsManager=${sakai.home}/toolEventsdef.xml.");
				toolEventsDefinition = parseToolEventsDefinition(defaultDefs.getInputStream());
				loadedCustomDefFile = true;
			}catch(Throwable t){
				logger.error("init(): - trouble loading default tool events definitions from : " + defaultDefs.getPath(), t);
			}
		}
		
		// add user-specified tool
		List<ToolInfo> additions = null;
		if(customToolEventsAddDefinitionFile != null) {
			File customDefs = new File(customToolEventsAddDefinitionFile);
			if(customDefs.exists()){
				try{
					logger.info("init(): - loading custom tool additions from: " + customDefs.getAbsolutePath());
					additions = parseToolEventsDefinition(new FileInputStream(customDefs));
					loadedCustomDefFile = true;
				}catch(Throwable t){
					logger.warn("init(): - trouble loading custom tool additions from : " + customDefs.getAbsolutePath(), t);
				}
			}else {
				logger.warn("init(): - custom tool additions file not found: "+customDefs.getAbsolutePath());
			}
		}
		if(additions != null)
			addToToolEventsDefinition(additions);

		// remove user-specified tool and/or events
		List<ToolInfo> removals = null;
		if(customToolEventsRemoveDefinitionFile != null) {
			File customDefs = new File(customToolEventsRemoveDefinitionFile);
			if(customDefs.exists()){
				try{
					logger.info("init(): - loading custom tool removals from: " + customDefs.getAbsolutePath());
					removals = parseToolEventsDefinition(new FileInputStream(customDefs));
					loadedCustomDefFile = true;
				}catch(Throwable t){
					logger.warn("init(): - trouble loading custom tool removals from : " + customDefs.getAbsolutePath(), t);
				}
			}else {
				logger.warn("init(): - custom tool removals file not found: "+customDefs.getAbsolutePath());
			}
		}
		if(removals != null)
			removeFromToolEventsDefinition(removals);
		
		
		// debug: print resulting list
//		LOG.info("-------- Printing resulting toolEventsDefinition list:");
//		Iterator<ToolInfo> iT = toolEventsDefinition.iterator();
//		while(iT.hasNext()) LOG.info(iT.next().toString());
//		LOG.info("------------------------------------------------------");
	}
	
	private List<ToolInfo> parseToolEventsDefinition(InputStream input) throws Exception{
		Digester digester = new Digester();
        digester.setValidating(false);
        
        digester = configureToolEventsDefDigester("", digester);

        // eventParserTip tag
        EventParserTipFactoryImpl eventParserTipFactoryImpl = new EventParserTipFactoryImpl();
        digester.addFactoryCreate("toolEventsDef/tool/eventParserTip", eventParserTipFactoryImpl);
        digester.addSetNestedProperties("toolEventsDef/tool/eventParserTip");
        digester.addSetNext("toolEventsDef/tool/eventParserTip", "setEventParserTip" );
        
        return (List<ToolInfo>)digester.parse( input );
	}
	
	private Digester configureToolEventsDefDigester(String prefix, Digester digester) {        
        // root
        digester.addObjectCreate(prefix + "toolEventsDef", ArrayList.class );

        // tool tag
        ToolFactoryImpl toolFactory = new ToolFactoryImpl();
        digester.addFactoryCreate(prefix + "toolEventsDef/tool", toolFactory);
        //digester.addSetProperties(prefix + "toolEventsDef/tool" );
        digester.addBeanPropertySetter(prefix + "toolEventsDef/tool/toolId", "toolId" );
        digester.addBeanPropertySetter(prefix + "toolEventsDef/tool/additionalToolIds", "additionalToolIdsStr" );
        digester.addBeanPropertySetter(prefix + "toolEventsDef/tool/selected", "selected" );
        digester.addSetNext(prefix + "toolEventsDef/tool", "add" );

        // event tag
        EventFactoryImpl eventFactoryImpl = new EventFactoryImpl();
        digester.addFactoryCreate(prefix + "toolEventsDef/tool/event", eventFactoryImpl);
//        digester.addSetProperties(prefix + "toolEventsDef/tool/event" );
        digester.addBeanPropertySetter(prefix + "toolEventsDef/tool/event/eventId", "eventId" );
        digester.addBeanPropertySetter(prefix + "toolEventsDef/tool/event/selected", "selected" );
        digester.addBeanPropertySetter(prefix + "toolEventsDef/tool/event/anonymous", "anonymous" );
        digester.addSetNext(prefix + "toolEventsDef/tool/event", "addEvent" );
        
        return digester;
	}
	
	private Digester configurePrefsDigester(Digester digester) {        
        // prefs root
        digester.addObjectCreate("prefs", PrefsDataImpl.class );
        digester.addSetProperties("prefs" );
        digester.addBeanPropertySetter("prefs/listToolEventsOnlyAvailableInSite", "setListToolEventsOnlyAvailableInSite" );
        digester.addBeanPropertySetter("prefs/chartIn3D", "setChartIn3D" );
        digester.addBeanPropertySetter("prefs/chartTransparency", "setChartTransparency" );
        digester.addBeanPropertySetter("prefs/itemLabelsVisible", "setItemLabelsVisible" );
        
        // prefs tag
//        RoleFactory roleFactory = new RoleFactory();
//        digester.addObjectCreate("prefs/rolesForActivity", ArrayList.class );
//        digester.addFactoryCreate("prefs/rolesForActivity/role", roleFactory );
//        digester.addSetNext("prefs/rolesForActivity/role", "add" );
//        digester.addSetNext("prefs/rolesForActivity", "setRolesForActivity" );

        // toolEventsDef
        digester = configureToolEventsDefDigester("prefs/", digester);
        digester.addSetNext("prefs/toolEventsDef", "setToolEventsDef" );
        
        
        return digester;
	}
	
	private PrefsData parseSitePrefs(InputStream input) throws Exception{
		Digester digester = new Digester();
        digester.setValidating(false);
        
        digester = configurePrefsDigester(digester);
        
        return (PrefsData) digester.parse( input );
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getAllToolEventsDefinition()
	 */
	public List<ToolInfo> getAllToolEventsDefinition() {
		return toolEventsDefinition;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getSiteToolEventsDefinition(java.lang.String, boolean)
	 */
	public List<ToolInfo> getSiteToolEventsDefinition(String siteId, boolean onlyAvailableInSite) {
		if(onlyAvailableInSite)
			return getIntersectionWithAvailableToolsInSite(getAllToolEventsDefinition(), siteId);
		else
			return getIntersectionWithAvailableToolsInSakaiInstallation(getAllToolEventsDefinition());
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getSiteVisitEventId()
	 */
	public String getSiteVisitEventId() {
		return SITEVISIT_EVENTID;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getAllToolEventIds()
	 */
	public List<String> getAllToolEventIds(){
		if(toolEventIds == null){
			toolEventIds = new ArrayList<String>();
			Iterator<String> i = getEventIdToolMap().keySet().iterator();
			while(i.hasNext())
				toolEventIds.add(i.next());
		}
		return toolEventIds;
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
				prefsdata.setToolEventsDef(toolEventsDefinition);
			}else{
				try{
					// parse from stored preferences
					prefsdata = parseSitePrefs(new StringBufferInputStream(prefs.getPrefs()));
				}catch(Exception e){
					// something failed, use default
					LOG.warn("Exception in parseSitePrefs() ",e);
					prefsdata = new PrefsDataImpl();
					prefsdata.setToolEventsDef(toolEventsDefinition);
				}
			}
			
			if(includeUnselected){
				// include unselected tools/events (for Preferences listing)
				prefsdata.setToolEventsDef(getUnionWithAllDefaultToolEvents(prefsdata.getToolEventsDef()));
			}
			if(prefsdata.isListToolEventsOnlyAvailableInSite()){
				// intersect with tools available in site
				prefsdata.setToolEventsDef(getIntersectionWithAvailableToolsInSite(prefsdata.getToolEventsDef(), siteId));
			}else{
				// intersect with tools available in sakai installation
				prefsdata.setToolEventsDef(getIntersectionWithAvailableToolsInSakaiInstallation(prefsdata.getToolEventsDef()));
			}

			prefsdata.setToolEventsDef(addMissingAdditionalToolIds(prefsdata.getToolEventsDef()));

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
	
	private List<ToolInfo> addMissingAdditionalToolIds(List<ToolInfo> list) {
		List<ToolInfo> fullList = getAllToolEventsDefinition();				
		Iterator<ToolInfo> i = list.iterator();
		while(i.hasNext()) {
			ToolInfo t = i.next();
			int ix = fullList.indexOf(new ToolInfoImpl(t.getToolId()));
			t.setAdditionalToolIds(fullList.get(ix).getAdditionalToolIds());
		}
		return list;
	}
	
	private List<ToolInfo> getIntersectionWithAvailableToolsInSite(List<ToolInfo> toolEventsDefinition, String siteId){
		List<ToolInfo> intersected = new ArrayList<ToolInfo>();
		Site site = null;
		try{
			site = M_ss.getSite(siteId);
		}catch(IdUnusedException e){
			LOG.warn("Inexistent site for site id: "+siteId, e);
			return toolEventsDefinition;
		}

		// search the pages
		List<ToolConfiguration> siteTools = new ArrayList<ToolConfiguration>();
		for (Iterator iPages = site.getPages().iterator(); iPages.hasNext();) {
			SitePage page = (SitePage) iPages.next();
			siteTools.addAll(page.getTools());
		}
		
		// add only tools in both lists
		Iterator<ToolInfo> iTED = toolEventsDefinition.iterator();
		while(iTED.hasNext()){
			ToolInfo t = iTED.next();
			Iterator<ToolConfiguration> iST = siteTools.iterator();
			while(iST.hasNext()){
				ToolConfiguration tc = iST.next(); 
				if(tc.getToolId().equals(t.getToolId())){
					intersected.add(t);
					break;
				}
			}
		}
		
		return intersected;
	}
	
	private List<ToolInfo> getIntersectionWithAvailableToolsInSakaiInstallation(List<ToolInfo> toolEventsDefinition){
		List<ToolInfo> intersected = new ArrayList<ToolInfo>();

		// search the pages
		List<org.sakaiproject.tool.api.Tool> sakaiTools = new ArrayList<org.sakaiproject.tool.api.Tool>();
		sakaiTools.addAll(M_tm.findTools(null, null));
		
		// add only tools in both lists
		Iterator<ToolInfo> iTED = toolEventsDefinition.iterator();
		while(iTED.hasNext()){
			ToolInfo t = iTED.next();
			Iterator<org.sakaiproject.tool.api.Tool> iST = sakaiTools.iterator();
			while(iST.hasNext()){
				org.sakaiproject.tool.api.Tool tc = iST.next(); 
				if(tc.getId().equals(t.getToolId())){
					intersected.add(t);
					break;
				}
			}
		}
		
		return intersected;
	}
	
	private List<ToolInfo> getUnionWithAllDefaultToolEvents(List<ToolInfo> toolEventsDefinition) {
		List<ToolInfo> union = new ArrayList<ToolInfo>();

		// add only tools in default list, as unselected
		Iterator<ToolInfo> iAll = getAllToolEventsDefinition().iterator();
		while(iAll.hasNext()){
			ToolInfo t1 = iAll.next();
			Iterator<ToolInfo> iPREFS = toolEventsDefinition.iterator();
			boolean foundTool = false;
			ToolInfo t2 = null;
			while(iPREFS.hasNext()){
				t2 = iPREFS.next(); 
				if(t2.getToolId().equals(t1.getToolId())){
					foundTool = true;
					break;
				}
			}
			if(!foundTool){
				// tool not found, add as unselected
				ToolInfo toAdd = t1;
				toAdd.setSelected(false);
				for(int i=0; i<toAdd.getEvents().size(); i++)
					toAdd.getEvents().get(i).setSelected(false);
				union.add(toAdd);
			}else{
				// tool found, add missing events as unselected
				Iterator<EventInfo> aPREFS = t1.getEvents().iterator();
				while(aPREFS.hasNext()){
					EventInfo e1 = aPREFS.next();
					boolean foundEvent = false;
					for(int i=0; i<t2.getEvents().size(); i++){
						EventInfo e2 = t2.getEvents().get(i);
						if(e2.getEventId().equals(e1.getEventId())){
							foundEvent = true;
							break;
						}
					}
					if(!foundEvent){
						EventInfo toAdd = e1;
						e1.setSelected(false);
						t2.addEvent(toAdd);
					}
				}
				union.add(t2);
			}
		}
		return union;
	}
	
	private void addToToolEventsDefinition(List<ToolInfo> additions) {
		List<ToolInfo> toBeAdded = new ArrayList<ToolInfo>();
		
		// iterate ADD list, add tool if not found in DEFAULT list
		Iterator<ToolInfo> iADDS = additions.iterator();
		while(iADDS.hasNext()){
			ToolInfo newTool = iADDS.next();
			Iterator<ToolInfo> iAll = toolEventsDefinition.iterator();
			boolean foundTool = false;
			ToolInfo existingTool = null;
			while(iAll.hasNext()){
				existingTool = iAll.next(); 
				if(existingTool.equals(newTool)){
					foundTool = true;
					break;
				}
			}
			if(!foundTool){
				// tool not found, add tool and its events
				toBeAdded.add(newTool);
			}else{
				// tool found, add missing events
				Iterator<EventInfo> newToolEvents = newTool.getEvents().iterator();
				while(newToolEvents.hasNext()){
					EventInfo newEvent = newToolEvents.next();
					boolean foundEvent = false;
					for(int i=0; i<existingTool.getEvents().size(); i++){
						EventInfo existingEvent = existingTool.getEvents().get(i);
						if(existingEvent.equals(newEvent)){
							foundEvent = true;
							break;
						}
					}
					if(!foundEvent){
						existingTool.addEvent(newEvent);
					}
				}
			}
		}
		
		toolEventsDefinition.addAll(toBeAdded);
	}
	
	private void removeFromToolEventsDefinition(List<ToolInfo> removals) {
		List<ToolInfo> toBeRemoved = new ArrayList<ToolInfo>();
		
		// iterate REMOVES list, remove tool if found in DEFAULT list
		Iterator<ToolInfo> iREMOVES = removals.iterator();
		while(iREMOVES.hasNext()){
			ToolInfo delTool = iREMOVES.next();
			Iterator<ToolInfo> iAll = toolEventsDefinition.iterator();
			boolean foundTool = false;
			ToolInfo existingTool = null;
			while(iAll.hasNext()){
				existingTool = iAll.next(); 
				if(existingTool.getToolId().equals(delTool.getToolId())){
					foundTool = true;
					break;
				}
			}
			if(foundTool){
				// tool found
				if(delTool.getEvents().size() == 0) {
					// tool selected for removal, remove tool and its events
					toolEventsDefinition.remove(existingTool);
				} else {
					// events selected for removal, remove events
					Iterator<EventInfo> delToolEvents = delTool.getEvents().iterator();
					while(delToolEvents.hasNext()){
						EventInfo delEvent = delToolEvents.next();
						boolean foundEvent = false;
						for(int i=0; i<existingTool.getEvents().size(); i++){
							EventInfo existingEvent = existingTool.getEvents().get(i);
							if(existingEvent.getEventId().equals(delEvent.getEventId())){
								foundEvent = true;
								break;
							}
						}
						if(foundEvent){
							existingTool.removeEvent(delEvent);
						}
					}
				}
			}
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
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getToolFactory()
	 */
	public ToolFactory getToolFactory(){
		return new ToolFactoryImpl();
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getEventFactory()
	 */
	public EventFactory getEventFactory(){
		return new EventFactoryImpl();
	}
	
	
	
	// ################################################################
	// Maps
	// ################################################################		
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getEventName(java.lang.String)
	 */
	public String getEventName(String eventId) {
		if(eventId == null || eventId.trim().equals(""))
			return "";
		String eventName = null;
		try{
			eventName = msgs.getString(eventId, eventId);
		}catch(MissingResourceException e){
			LOG.warn("Missing resource bundle for event id: "+eventId, e);
			eventName = eventId;
		}		
		return eventName;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getToolName(java.lang.String)
	 */
	public String getToolName(String toolId){
		try{
			return M_tm.getTool(toolId).getTitle();
		}catch(Exception e){
			return toolId;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getEventToolMap()
	 */
	public Map<String, ToolInfo> getEventIdToolMap(){
		if(eventIdToolMap == null){
			eventIdToolMap = new HashMap<String, ToolInfo>();
			Iterator<ToolInfo> i = getAllToolEventsDefinition().iterator();
			while (i.hasNext()){
				ToolInfo t = i.next();
				Iterator<EventInfo> iE = t.getEvents().iterator();
				while(iE.hasNext()){
					EventInfo e = iE.next();
					eventIdToolMap.put(e.getEventId(), t);
				}
			}
		}
		return eventIdToolMap;
	}
		
	
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
	 * @see org.sakaiproject.sitestats.api.StatsManager#getResourceImage(java.lang.String)
	 */
	public String getResourceImage(String ref){
		String href = M_scs.getServerUrl() + "/library/image/";
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
				imgLink = href + ContentTypeImageService.getContentTypeImage("folder");			
			else if(rp != null){
				String contentTypePropName = rp.getNamePropContentType();
				String contentType = rp.getProperty(contentTypePropName);
				if(contentType != null)
					imgLink = href + ContentTypeImageService.getContentTypeImage(contentType);
				else
					imgLink = href + "sakai/generic.gif";
			}else
				imgLink = href + "sakai/generic.gif";
		}catch(Exception e){
			imgLink = href + "sakai/generic.gif";
		}
		return imgLink;
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
		
//		long weekDiff = 604800000l;
//		long monthDiff = 2592000000l;
//		long yearDiff = 31536000000l;
//		Date lastWeek = new Date(now.getTime() - weekDiff);
//		Date lastMonth = new Date(now.getTime() - monthDiff);
//		Date lastYear = new Date(now.getTime() - yearDiff);
		
		double last7DaysVisitsAverage = round(getTotalSiteVisits(siteId, lastWeek, now) / 7.0, 1);
		double last30DaysVisitsAverage = round(getTotalSiteVisits(siteId, lastMonth, now) / 30.0, 1);
		double last365DaysVisitsAverage = round(getTotalSiteVisits(siteId, lastYear, now) / 365.0, 1);
		svt.setLast7DaysVisitsAverage(last7DaysVisitsAverage);
		svt.setLast30DaysVisitsAverage(last30DaysVisitsAverage);
		svt.setLast365DaysVisitsAverage(last365DaysVisitsAverage);
		
		long totalSiteUniqueVisits = getTotalSiteUniqueVisits(siteId);
		long totalSiteVisits = getTotalSiteVisits(siteId);
		int totalSiteUsers = getTotalSiteUsers(siteId);
		double percentageOfUsersThatVisitedSite = totalSiteUsers==0 ? 0 : (100 * totalSiteUniqueVisits) / totalSiteUsers;
		svt.setTotalUniqueVisits(totalSiteUniqueVisits);
		svt.setTotalVisits(totalSiteVisits);
		svt.setTotalUsers(totalSiteUsers);
		svt.setPercentageOfUsersThatVisitedSite(percentageOfUsersThatVisitedSite);
		
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
		
//		long weekDiff = 604800000l;
//		long monthDiff = 2592000000l;
//		long yearDiff = 31536000000l;
//		Date lastWeek = new Date(now.getTime() - weekDiff);
//		Date lastMonth = new Date(now.getTime() - monthDiff);
//		Date lastYear = new Date(now.getTime() - yearDiff);
		
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
		return siteVisits.size() > 0? svc : null;
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
		
		if(CHATTYPE_BAR.equals(chartType)){
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
			return siteActivity.size() > 0? sac : null;
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
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getReport(java.lang.String, org.sakaiproject.sitestats.api.PrefsData, org.sakaiproject.sitestats.api.ReportParams)
	 */
	public Report getReport(String siteId, PrefsData prefsdata, ReportParams params) {
		// what (visits, events, resources)
		List<String> eventIds = new ArrayList<String>();
		if(params.getWhat().equals(WHAT_VISITS)){
			eventIds.add(SITEVISIT_EVENTID);
			
		}else if(params.getWhat().equals(WHAT_EVENTS)){
			if(params.getWhatEventSelType().equals(WHAT_EVENTS_BYTOOL)){
				Iterator<ToolInfo> iT = getSiteToolEventsDefinition(siteId, prefsdata.isListToolEventsOnlyAvailableInSite()).iterator();
				while(iT.hasNext()){
					ToolInfo t = iT.next();
					if(params.getWhatToolIds().contains(t.getToolId())){
						Iterator<EventInfo> iE = t.getEvents().iterator();
						while(iE.hasNext())
							eventIds.add(iE.next().getEventId());
					}
				}
			}else
				eventIds.addAll(params.getWhatEventIds());
			
		}
		
		// when (dates)
		Date from = null;
		Date to = null;
		if (params.getWhen().equals(WHEN_CUSTOM)){
			from = params.getWhenFrom();
			to = params.getWhenTo();
		}else
			to = new Date();
		if (params.getWhen().equals(WHEN_ALL)) {
			from =getInitialActivityDate(siteId);
		} else if (params.getWhen().equals(WHEN_LAST7DAYS)) {
			Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, 00);
			c.set(Calendar.MINUTE, 00);
			c.set(Calendar.SECOND, 00);
			c.add(Calendar.DATE, -6);
			from = c.getTime();
//			Date now = new Date();
//			long int7Days = 604800000l; // 1000ms * 60s * 60m * 24h * 7d
//			from = new Date(now.getTime() - int7Days);
		} else if (params.getWhen().equals(WHEN_LAST30DAYS)) {
			Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, 00);
			c.set(Calendar.MINUTE, 00);
			c.set(Calendar.SECOND, 00);
			c.add(Calendar.DATE, -29);
			from = c.getTime();
//			Date now = new Date();
//			long int30Days = 2592000000l; // 1000ms * 60s * 60m * 24h * 30d
//			from = new Date(now.getTime() - int30Days);
		}
		params.setWhenFrom(from);
		params.setWhenTo(to);
		
		// who (users, groups, roles)
		List<String> userIds = null;
		boolean inverseWhoSelection = false;
		if(params.getWho().equals(WHO_ALL)){
			;
		}else if(params.getWho().equals(WHO_ROLE)){
			userIds = new ArrayList<String>();
			try {
				Site site = M_ss.getSite(siteId);
				userIds.addAll(site.getUsersHasRole(params.getWhoRoleId()));
			} catch (IdUnusedException e) {
				LOG.error("No site with specified siteId.");
			}
			
		}else if(params.getWho().equals(WHO_GROUPS)){
			userIds = new ArrayList<String>();
			try {
				Site site = M_ss.getSite(siteId);
				userIds.addAll(site.getGroup(params.getWhoGroupId()).getUsers());
			} catch (IdUnusedException e) {
				LOG.error("No site with specified siteId.");
			}
			
		}else if(params.getWho().equals(WHO_CUSTOM)){
			userIds = params.getWhoUserIds();
		}else{
			// inverse
			inverseWhoSelection = true;
		}
		params.setWhoUserIds(userIds);
		
		
		// generate report
		Report report = new ReportImpl();
		report.setReportParams(params);
		List<CommonStatGrpByDate> data = null;
		if(params.getWhat().equals(WHAT_RESOURCES)){
			List<String> resourceIds = null;
			if(params.getWhatResourceIds() != null){
				resourceIds = new ArrayList<String>();
				Iterator<String> iR = params.getWhatResourceIds().iterator();
				while(iR.hasNext())
					resourceIds.add("/content"+iR.next());				
			}
			String resourceAction = params.getWhatResourceAction();
			data = getResourceStatsGrpByDateAndAction(siteId, resourceAction, resourceIds, from, to, userIds, inverseWhoSelection, null);
		}else{
			data = getEventStatsGrpByDate(siteId, eventIds, from, to, userIds, inverseWhoSelection, null);
		}
		report.setReportData(data);
		
		// consolidate anonymous events
		report = consolidateAnonymousEvents(report);
		
		// add report generation date
		if(report != null)
			report.setReportGenerationDate(M_ts.newTime());
		return report;
	}
	
	
	private Report consolidateAnonymousEvents(Report report) {
		List<CommonStatGrpByDate> consolidated = new ArrayList<CommonStatGrpByDate>();
		List<CommonStatGrpByDate> list = report.getReportData();

		Map<String,CommonStatGrpByDate> anonMap = new HashMap<String, CommonStatGrpByDate>();

		for(CommonStatGrpByDate s : list) {
			String eventId = s.getRef();
			if(!isAnonymousEvent(eventId)) {
				consolidated.add(s);
			} else {
				CommonStatGrpByDate sMapped = anonMap.get(eventId);
				if(sMapped != null) {
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
		
		for(CommonStatGrpByDate s : anonMap.values()) {
			consolidated.add(s);
		}
		
		report.setReportData(consolidated);
		return report;
	}
	
	private boolean isAnonymousEvent(String eventId) {
		for(ToolInfo ti : toolEventsDefinition) {
			for(EventInfo ei : ti.getEvents()) {
				if(ei.getEventId().equals(eventId)) {
					return ei.isAnonymous();
				}
			}
		}
		return false;
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
			if(userIds != null)
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
					if(userIds != null)
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
						q.setMaxResults(page.getLast() - page.getFirst());
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
			if(userIds != null)
				usersStr = "and s.userId in (:users) ";
			if(resourceAction != null)
				resourcesActionStr = "and s.resourceAction = :action ";
			if(resourceIds != null)
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
					if(userIds != null)
						q.setParameterList("users", userIds);
					if(resourceAction != null)
						q.setString("action", resourceAction);
					if(resourceIds != null)
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
						q.setMaxResults(page.getLast() - page.getFirst());
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
					else return new Long(0);	
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
					else return new Long(0);	
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
					else return new Long(0);	
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
						q.setParameterList("eventlist", getAllToolEventIds());
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
						q.setParameterList("eventlist", getAllToolEventIds());
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
						q.setParameterList("eventlist", getAllToolEventIds());
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
						Map<String,ToolInfo> eventIdToolMap = getEventIdToolMap();
						Map<String,SiteActivityByTool> toolidSABT = new HashMap<String, SiteActivityByTool>();
						List<ToolInfo> allTools = getAllToolEventsDefinition();
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
						q.setParameterList("eventlist", getAllToolEventIds());
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
						q.setParameterList("eventlist", getAllToolEventIds());
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
					else return new Long(0);	
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
