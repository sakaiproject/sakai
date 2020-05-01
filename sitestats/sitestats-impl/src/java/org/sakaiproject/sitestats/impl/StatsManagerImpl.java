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
 *			   http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentTypeImageService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.EventStat;
import org.sakaiproject.sitestats.api.LessonBuilderStat;
import org.sakaiproject.sitestats.api.Prefs;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.ResourceStat;
import org.sakaiproject.sitestats.api.SiteActivity;
import org.sakaiproject.sitestats.api.SiteActivityByTool;
import org.sakaiproject.sitestats.api.SitePresence;
import org.sakaiproject.sitestats.api.SitePresenceTotal;
import org.sakaiproject.sitestats.api.SiteVisits;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.SummaryActivityChartData;
import org.sakaiproject.sitestats.api.SummaryActivityTotals;
import org.sakaiproject.sitestats.api.SummaryVisitsChartData;
import org.sakaiproject.sitestats.api.SummaryVisitsTotals;
import org.sakaiproject.sitestats.api.UserId;
import org.sakaiproject.sitestats.api.Util;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.impl.event.EventUtil;
import org.sakaiproject.sitestats.impl.parser.DigesterUtil;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

/**
 * @author Nuno Fernandes
 *
 */
@Slf4j
public class StatsManagerImpl extends HibernateDaoSupport implements StatsManager, Observer {

	/** Spring bean members */
	@Getter @Setter private Boolean	enableSiteVisits		= null;
	@Getter @Setter private boolean	enableSiteActivity		= true;
	@Getter @Setter private boolean	enableResourceStats		= true;
	@Getter @Setter private boolean	enableLessonsStats		= true;
	@Getter @Setter private Boolean	enableSitePresences		= null;
	@Getter @Setter private Boolean	visitsInfoAvailable		= null;
	@Getter @Setter private boolean	serverWideStatsEnabled	= true;
	@Setter private boolean			countFilesUsingCHS		= true;
	@Setter private boolean			countPagesUsingLBS		= true;
	@Getter @Setter private String	chartBackgroundColor	= "white";
	@Getter @Setter private boolean	chartIn3D				= true;
	@Getter @Setter private float	chartTransparency		= 0.80f;
	@Getter @Setter private boolean	itemLabelsVisible		= false;
	@Getter @Setter private boolean	lastJobRunDateVisible	= true;
	@Getter @Setter private boolean eventContextSupported   = false;
	@Getter @Setter private boolean	enableReportExport		= true;
	@Getter @Setter private boolean	sortUsersByDisplayName	= false;
	@Getter @Setter private boolean	displayDetailedEvents	= false;

	/** Controller fields */
	@Getter @Setter private boolean	showAnonymousAccessEvents = true;

	@Setter private ResourceLoader resourceLoader;

	/** Sakai services */
	@Setter private EventRegistryService		eventRegistryService;
	@Setter private UserDirectoryService		userService;
	@Setter private SiteService					siteService;
	@Setter private ServerConfigurationService	serverConfigurationService;
	@Setter private ToolManager					toolManager;
	@Setter private SimplePageToolDao			lessonBuilderService;
	@Setter private MemoryService				memoryService;
	@Setter private SessionManager				sessionManager;
	@Setter private EventTrackingService		eventTrackingService;
	@Setter private EntityManager				entityManager;
	@Setter private ContentHostingService		contentHostingService;
	@Setter private ContentTypeImageService		contentTypeImageService;
	
	/** Caching */
	private Cache<String, PrefsData> cachePrefsData = null;

	// ################################################################
	// Spring init/destroy methods
	// ################################################################	
	public void init() {
		boolean testsEnabled = BooleanUtils.toBoolean(System.getProperty("sakai.tests.enabled"));
		if (!testsEnabled) {
			resourceLoader = new ResourceLoader("Messages");
		}
		// Set default properties if not set by spring/sakai.properties
		checkAndSetDefaultPropertiesIfNotSet();
		
		// Checks whether Event.getContext is implemented in Event (from Event API)
		checkForEventContextSupport();
		
		// Initialize cacheReportDef and event observer for preferences invalidation across cluster
		eventTrackingService.addPriorityObserver(this);
		cachePrefsData = memoryService.getCache(PrefsData.class.getName());
		
		logger.info("init(): - (Event.getContext()?, site visits enabled, charts background color, charts in 3D, charts transparency, item labels visible on bar charts) : " +
				eventContextSupported +','+enableSiteVisits+','+chartBackgroundColor+','+chartIn3D+','+chartTransparency+','+itemLabelsVisible);

		// To avoid a circular dependency in spring we set the StatsManager in the EventRegistryService here
		eventRegistryService.setStatsManager(this);
	}
	
	public void checkAndSetDefaultPropertiesIfNotSet() {
		if(enableSiteVisits == null) {
			enableSiteVisits = serverConfigurationService.getBoolean("display.users.present", true) || serverConfigurationService.getBoolean("presence.events.log", true);
		}
		if(visitsInfoAvailable == null) {
			visitsInfoAvailable	= enableSiteVisits;
		}
		if(enableSitePresences == null) {
			// turn off, by default
			enableSitePresences = false;// serverConfigurationService.getBoolean("display.users.present", true) || serverConfigurationService.getBoolean("presence.events.log", true);
		}else if(enableSitePresences.booleanValue()){
			// if turned on, make sure "display.users.present" is true
			// this feature doesn't work properly with "presence.events.log"
			if(serverConfigurationService.getBoolean("display.users.present", true)) {
				enableSitePresences = serverConfigurationService.getBoolean("display.users.present", true);
			}else if(serverConfigurationService.getBoolean("presence.events.log", true)) {
				enableSitePresences = false;
				log.warn("Disabled SiteStats presence tracking: doesn't work properly with 'presence.events.log' => only plays nicely with 'display.users.present'");
			}
		}
	}

	public void destroy(){
		eventTrackingService.deleteObserver(this);
	}

	/** EventTrackingService observer for cache invalidation. */
	public void update(Observable obs, Object o) {
		if(o instanceof Event){
			Event e = (Event) o;
			String event = LOG_APP + '.' + LOG_OBJ_PREFSDATA + '.' + LOG_ACTION_EDIT;
			if(e.getEvent() != null && e.getEvent().equals(event)) {
				String siteId = e.getResource().split("/")[2];
				cachePrefsData.remove(siteId);
				log.debug("Expiring preferences cache for site: "+siteId);
			}
		}
	}
	
	private PrefsData parseSitePrefs(InputStream input) throws Exception{
		Digester digester = new Digester();
		digester.setValidating(false);

		digester = DigesterUtil.configurePrefsDigester(digester);

		return (PrefsData) digester.parse( input );
	}

	@Override
	public PrefsData getPreferences(final String siteId, final boolean includeUnselected) {
		if (siteId == null) throw new IllegalArgumentException("Site Id was null");

		PrefsData prefsdata = null;
		PrefsData cached = cachePrefsData.get(siteId);
		if (cached != null) {
			prefsdata = cached;
			log.debug("Getting preferences for site {} from cache", siteId);
		} else {
			HibernateCallback<Prefs> hcb = session -> {
				Criteria c = session.createCriteria(PrefsImpl.class).add(Restrictions.eq("siteId", siteId));
				try {
					Prefs prefs = (Prefs) c.uniqueResult();
					return prefs;
				} catch (Exception e) {
					log.warn("Error getting preferences for site {}", siteId, e);
					return null;
				}
			};
			Prefs prefs = getHibernateTemplate().execute(hcb);
			if (prefs == null) {
				// get default settings
				prefsdata = new PrefsData();
				prefsdata.setChartIn3D(isChartIn3D());
				prefsdata.setChartTransparency(getChartTransparency());
				prefsdata.setItemLabelsVisible(isItemLabelsVisible());
				prefsdata.setToolEventsDef(eventRegistryService.getEventRegistry());
			} else {
				try {
					// parse from stored preferences
					prefsdata = parseSitePrefs(new ByteArrayInputStream(prefs.getPrefs().getBytes()));
					// preferences doesn't store additionalToolIds, add them back
					EventUtil.addMissingAdditionalToolIds(prefsdata.getToolEventsDef(), eventRegistryService.getEventRegistry());
				} catch (Exception e) {
					log.warn("Failure parsing stored preferences for site {}", siteId, e);
					prefsdata = new PrefsData();
					prefsdata.setToolEventsDef(eventRegistryService.getEventRegistry());
				}
			}
			cachePrefsData.put(siteId, prefsdata);
		}

		if (prefsdata.isUseAllTools()) {
			List<ToolInfo> allTools = eventRegistryService.getEventRegistry();
			for (ToolInfo ti : allTools) {
				ti.setSelected(true);
				for (EventInfo ei : ti.getEvents()) {
					ei.setSelected(true);
				}
			}
			prefsdata.setToolEventsDef(allTools);
		}
		if (includeUnselected) {
			// include unselected tools/events (for Preferences listing)
			prefsdata.setToolEventsDef(EventUtil.getUnionWithAllDefaultToolEvents(prefsdata.getToolEventsDef(), eventRegistryService.getEventRegistry()));
		}
		if (prefsdata.isListToolEventsOnlyAvailableInSite()) {
			// intersect with tools available in site
			prefsdata.setToolEventsDef(EventUtil.getIntersectionWithAvailableToolsInSite(siteService, prefsdata.getToolEventsDef(), siteId));
		} else {
			// intersect with tools available in sakai installation
			prefsdata.setToolEventsDef(EventUtil.getIntersectionWithAvailableToolsInSakaiInstallation(toolManager, prefsdata.getToolEventsDef()));
		}

		prefsdata.setToolEventsDef(EventUtil.addMissingAdditionalToolIds(prefsdata.getToolEventsDef(), eventRegistryService.getEventRegistry()));

		return prefsdata;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#setPreferences(java.lang.String, org.sakaiproject.sitestats.api.PrefsData)
	 */
	public boolean setPreferences(final String siteId, final PrefsData prefsdata) {
		if (siteId == null || prefsdata == null) throw new IllegalArgumentException("Site Id or preferences were null");
		HibernateCallback<Void> hcb = session -> {
				Criteria c = session.createCriteria(PrefsImpl.class).add(Restrictions.eq("siteId", siteId));
				Prefs prefs = (Prefs) c.uniqueResult();
				if (prefs == null) {
					prefs = new PrefsImpl();
					prefs.setSiteId(siteId);
				}
				prefs.setPrefs(prefsdata.toXmlPrefs());
				session.saveOrUpdate(prefs);
				return null;
		};
		try {
			getHibernateTemplate().execute(hcb);
			cachePrefsData.remove(siteId);
			logEvent(prefsdata, LOG_ACTION_EDIT, siteId, false);
			return true;
		} catch (DataAccessException dae) {
			log.warn("Exception while saving preferences: {}", dae.getMessage(), dae);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getSiteUsers(java.lang.String)
	 */
	public Set<String> getSiteUsers(String siteId) {
		try{
			if(siteId == null) {
				siteId = toolManager.getCurrentPlacement().getContext();
			}
			return siteService.getSite(siteId).getUsers();
		}catch(IdUnusedException e){
			log.warn("Inexistent site for site id: "+siteId);
		}
		return null;
	}
	
	public String getUserNameForDisplay(String userId) {
		String name = null;
		try{
			User user = userService.getUser(userId);
			name = getUserNameForDisplay(user);
		}catch(UserNotDefinedException e){
			name = resourceLoader.getString("user_unknown");
		}
		return name;
	}
	
	public String getUserNameForDisplay(User user) {
		if(isSortUsersByDisplayName()) {
			return user.getDisplayName();
		}else{
			return user.getSortName();
		}
	}

	@Override
	public String getUserInfoForDisplay(String userId, String siteId) {
		try{
			User u = userService.getUser(userId);
			return resourceLoader.getFormattedMessage("user_info", getUserNameForDisplay(u), u.getDisplayId(siteId));
		}catch (UserNotDefinedException e){
			return resourceLoader.getFormattedMessage("user_info", resourceLoader.getString("user_unknown"), userId);
		}
	}
	
	public Set<String> getUsersWithVisits(final String siteId) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			HibernateCallback<List<String>> hcb = session -> {
                Query q = session.createQuery("select distinct s.userId from EventStatImpl as s where s.siteId = :siteid and s.eventId = :eventId");
                q.setString("siteid", siteId);
                q.setString("eventId", SITEVISIT_EVENTID);
                return q.list();
            };
			return new HashSet<>(getHibernateTemplate().execute(hcb));
		}
	}
	
	
	
	// ################################################################
	// Resources related
	// ################################################################		
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getResourceName(java.lang.String)
	 */
	public String getResourceName(String ref){
		return getResourceName(ref, true);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getResourceName(java.lang.String, boolean)
	 */
	public String getResourceName(String ref, boolean includeLocationPrefix) {
		if(ref == null) {
			return null;
		}
		String parts[] = ref.split("\\/");
		Reference r = entityManager.newReference(ref);
		ResourceProperties rp = null;
		// determine resource name
		String name = null;
		if(r != null) {
			rp = r.getProperties();
			if(rp != null){
				// resource exists
				name = rp.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
			}else{
				// resource was deleted
				if(parts.length >= 2) {
					name = parts[parts.length - 1];
				}
				if("".equals(name) && parts.length >= 3) {
					name = parts[parts.length - 2];
				}
			}
		}
		
		StringBuffer _fileName = new StringBuffer("");		
		if(includeLocationPrefix) {
			if(parts.length >= 4 && parts[2].equals("user")){
				// My Workspace
				_fileName.append("[");
				try{
					_fileName.append(siteService.getSite(siteService.getSiteUserId(parts[3])).getTitle());
				}catch(IdUnusedException e){
					_fileName.append("My Workspace");
				}
				_fileName.append("] ");
				
			}else if(parts[2].equals("attachment")){
				// attachment
				if(parts.length >= 5){
					_fileName.append("[");
					_fileName.append(resourceLoader.getString("report_content_attachments"));
					_fileName.append(": ");
					_fileName.append(parts[4]);
					_fileName.append("] ");
				}else{
					_fileName.append("[");
					_fileName.append(resourceLoader.getString("report_content_attachments"));
					_fileName.append("] ");
				}
				
			}else if(parts.length > 4 && parts[2].equals("group")){
				// resource (standard)
				
			}else if(parts.length > 4 && parts[2].equals("group-user")){
				// dropbox
				_fileName.append("[");
				_fileName.append(toolManager.getTool(StatsManager.DROPBOX_TOOLID).getTitle());
				if(parts.length > 5){
					_fileName.append(": ");
					String user = null;
					try{
						StringBuilder refU = new StringBuilder();
						for(int i=0; i<5; i++) {
							refU.append(parts[i]);
							refU.append('/');
						}
						Reference rU = entityManager.newReference(refU.toString());
						ResourceProperties rpU = rU.getProperties();
						user = rpU.getProperty(ResourceProperties.PROP_DISPLAY_NAME);						
					}catch(Exception e1){
						try{
							user = userService.getUserEid(parts[4]);
						}catch(UserNotDefinedException e2){
							user = parts[4];
						}
					}
					_fileName.append(user);
					_fileName.append("] ");
				}else{
					_fileName.append("] ");					
				}
			}
		}
		
		_fileName.append(name);
		return _fileName.toString();		
	}
	
	@Deprecated
	private String getResourceName_ManualParse(String ref) {
		if(ref == null) {
			return null;
		}
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
				userEid = userService.getUserEid(parts[4]);
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
		Reference r = entityManager.newReference(ref);
		ResourceProperties rp = null;
		if(r != null) {
			rp = r.getProperties();
		}
		
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
				imgLink = contentTypeImageService.getContentTypeImage("folder");			
			else if(rp != null){
				String contentType = rp.getProperty(rp.getNamePropContentType());
				if(contentType != null)
					imgLink = contentTypeImageService.getContentTypeImage(contentType);
				else{
					imgLink = "sakai/generic.gif";
				}
			}else{
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
		return serverConfigurationService.getServerUrl() + "/library/" + getResourceImageLibraryRelativePath(ref);
	}	
	
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getResourceURL(java.lang.String)
	 */
	public String getResourceURL(String ref){
		try{
			String tmp = ref.replaceFirst("/content", "");
			if(tmp.endsWith("/"))
				contentHostingService.checkCollection(tmp);
			else
				contentHostingService.checkResource(tmp);
		}catch(IdUnusedException e){
			return null;
		}catch(Exception e){
			// TypeException or PermissionException
			// It's OK since it exists
		}
		Reference r = entityManager.newReference(ref);
		if(r != null) {
			return StringEscapeUtils.escapeHtml4(r.getUrl());
		}else{
			return null;
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getTotalResources(java.lang.String, boolean)
	 */
	public int getTotalResources(final String siteId, final boolean excludeFolders) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			if(countFilesUsingCHS) {
				// Use ContentHostingService (very slow if there are hundreds of files in site
				String siteCollectionId = contentHostingService.getSiteCollection(siteId);
				return contentHostingService.getAllResources(siteCollectionId).size();
			}else{
				// Use SiteStats tables (very fast, relies on resource events)
				// Build common HQL
				String hql_ = "select s.siteId, sum(s.count) " 
					+ "from ResourceStatImpl as s " 
					+ "where s.siteId = :siteid " 
					+ "and s.resourceAction = :resourceAction "
					+ "and s.resourceRef like :resourceRefLike ";
				if(excludeFolders) {
					hql_ += "and s.resourceRef not like :resourceRefNotLike ";
				}
				hql_ +=  "group by s.siteId";
				final String hql = hql_;
				final String resourceRefLike = "/content/group/" + siteId + "/%";
				final String resourceRefNotLike = "%/";
				
				// New files
				HibernateCallback<Long> hcb1 = session -> {
                    Query q = session.createQuery(hql);
                    q.setString("siteid", siteId);
                    q.setString("resourceAction", "new");
                    q.setString("resourceRefLike", resourceRefLike);
                    if(excludeFolders){
                        q.setString("resourceRefNotLike", resourceRefNotLike);
                    }
                    List<Object[]> list = q.list();
                    Long total = Long.valueOf(0);
                    if(list != null && list.size() > 0) {
                        try{
                            total = (Long) (list.get(0))[1];
                        }catch(ClassCastException e) {
                            total = ((Integer) (list.get(0))[1]).longValue();
                        }
                    }
                    return total;
                };
				Long totalNew = getHibernateTemplate().execute(hcb1);
				
				// Deleted files
				HibernateCallback<Long> hcb2 = session -> {
                    Query q = session.createQuery(hql);
                    q.setString("siteid", siteId);
                    q.setString("resourceAction", "delete");
                    q.setString("resourceRefLike", resourceRefLike);
                    if(excludeFolders){
                        q.setString("resourceRefNotLike", resourceRefNotLike);
                    }
                    List<Object[]> list = q.list();
                    Long total = Long.valueOf(0);
                    if(list != null && list.size() > 0) {
                        try{
                            total = (Long) (list.get(0))[1];
                        }catch(ClassCastException e) {
                            total = ((Integer) (list.get(0))[1]).longValue();
                        }
                    }
                    return total;
                };
				Long totalDel = getHibernateTemplate().execute(hcb2);
				
				return (int) (totalNew - totalDel);
			}
		}
	}

	public String getLessonPageTitle(long pageId) {

		SimplePage lbPage = lessonBuilderService.getPage(pageId);
		if (lbPage != null) {
			return lbPage.getTitle();
		} else {
			return resourceLoader.getString("page_unknown");
		}
	}

	public int getTotalLessonPages(final String siteId) {

		if (siteId == null){
			throw new IllegalArgumentException("Null siteId");
		} if(countPagesUsingLBS){
			 List<SimplePage> sitePages = lessonBuilderService.getSitePages(siteId);
			 return sitePages != null ? sitePages.size() : 0;
		} else {
			// Use SiteStats tables (very fast, relies on resource events)
			// Build common HQL
			String hql_ = "select s.siteId, sum(s.count) "
				+ "from LessonBuilderStatImpl as s "
				+ "where s.siteId = :siteid "
				+ "and s.pageAction = :pageAction "
				+ "and s.pageRef like :pageRefLike "
				+ "group by s.siteId";
			final String hql = hql_;
			final String pageRefLike = "/lessonbuilder/page/%";

			// New files
			HibernateCallback<Long> hcb1 = session -> {
                Query q = session.createQuery(hql);
                q.setString("siteid", siteId);
                q.setString("pageAction", "create");
                q.setString("pageRefLike", pageRefLike);
                List<Object[]> list = q.list();
                Long total = Long.valueOf(0);
                if (list != null && list.size() > 0) {
                    try {
                        total = (Long) (list.get(0))[1];
                    } catch (ClassCastException e) {
                        total = ((Integer) (list.get(0))[1]).longValue();
                    }
                }
                return total;
            };
			Long totalNew = getHibernateTemplate().execute(hcb1);

			// Deleted files
			HibernateCallback<Long> hcb2 = session -> {
                Query q = session.createQuery(hql);
                q.setString("siteid", siteId);
                q.setString("pageAction", "delete");
                q.setString("pageRefLike", pageRefLike);
                List<Object[]> list = q.list();
                Long total = Long.valueOf(0);
                if (list != null && list.size() > 0) {
                    try {
                        total = (Long) (list.get(0))[1];
                    } catch (ClassCastException e) {
                        total = ((Integer) (list.get(0))[1]).longValue();
                    }
                }
                return total;
            };
			Long totalDel = getHibernateTemplate().execute(hcb2);

			return (int) (totalNew - totalDel);
		}
	}

	public int getTotalReadLessonPages(final String siteId) {

		if (siteId == null){
			throw new IllegalArgumentException("Null siteId");
		} else {
			// Use SiteStats tables (very fast, relies on resource events)
			// Build common HQL
			String hql_ = "select distinct s.pageRef "
				+ "from LessonBuilderStatImpl as s "
				+ "where s.siteId = :siteid "
				+ "and s.pageAction = :pageAction "
				+ "and s.pageRef like :pageRefLike ";
			final String hql = hql_;
			final String pageRefLike = "/lessonbuilder/page/%";

			// New files
			HibernateCallback<List<Object[]>> hcb1 = session -> {
                Query q = session.createQuery(hql);
                q.setString("siteid", siteId);
                q.setString("pageAction", "read");
                q.setString("pageRefLike", pageRefLike);
                return q.list();
            };

			List<Object[]> read = getHibernateTemplate().execute(hcb1);

			// Deleted files
			HibernateCallback<List<String>> hcb2 = session -> {
                Query q = session.createQuery(hql);
                q.setString("siteid", siteId);
                q.setString("pageAction", "delete");
                q.setString("pageRefLike", pageRefLike);
                return q.list();
            };

			List<String> deleted = getHibernateTemplate().execute(hcb2);

			int totalRead = read.size();

			for (Iterator i = read.iterator(); i.hasNext();) {
				Object o = i.next();
				if (deleted.contains(o)) {
					totalRead -= 1;
				}
			}

			return totalRead;
		}
	}

	public String getMostReadLessonPage(final String siteId) {

		if (siteId == null){
			throw new IllegalArgumentException("Null siteId");
		} else {
			// Use SiteStats tables (very fast, relies on resource events)
			// Build common HQL
			final String hql = "select s.pageRef, s.pageId, sum(s.count) as total "
				+ "from LessonBuilderStatImpl as s "
				+ "where s.siteId = :siteid "
				+ "and s.pageAction = :pageAction "
				+ "and s.pageRef like :pageRefLike "
				+ "and s.userId != '?' group by s.pageRef, s.pageId order by total DESC";

			HibernateCallback<List<Object[]>> hcb = session -> {
                Query q = session.createQuery(hql);
                q.setString("siteid", siteId);
                q.setString("pageAction", "read");
                q.setString("pageRefLike", "/lessonbuilder/page/%");
                return q.list();
            };

			List<Object[]> read = getHibernateTemplate().execute(hcb);

			if (read.size() > 0) {
				Object[] topRow = read.get(0);
				String page = getLessonPageTitle((Long) topRow[1]);
				if (page == null) {
					page = (String) topRow[0];
				}
				return page;
			} else {
				return "-";
			}
		}
	}

    public String getMostActiveLessonPageReader(final String siteId) {

		if (siteId == null){
			throw new IllegalArgumentException("Null siteId");
		} else {
			// Use SiteStats tables (very fast, relies on resource events)
			// Build common HQL
			final String hql = "select s.userId as user, sum(s.count) as total "
				+ "from LessonBuilderStatImpl as s "
				+ "where s.siteId = :siteid "
				+ "and s.pageAction = :pageAction "
				+ "and s.pageRef like :pageRefLike "
				+ "group by s.userId order by total DESC";

			HibernateCallback<List<Object[]>> hcb = session -> {

                Query q = session.createQuery(hql);
                q.setString("siteid", siteId);
                q.setString("pageAction", "read");
                q.setString("pageRefLike", "/lessonbuilder/page/%");
                return q.list();
            };

			List<Object[]> userRows = getHibernateTemplate().execute(hcb);

			if (userRows.size() > 0) {
				return (String) userRows.get(0)[0];
			} else {
				return "-";
			}
		}
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
		
		double last7DaysVisitsAverage = Util.round(getTotalSiteVisits(siteId, lastWeek, now) / 7.0, 1);
		double last30DaysVisitsAverage = Util.round(getTotalSiteVisits(siteId, lastMonth, now) / 30.0, 1);
		double last365DaysVisitsAverage = Util.round(getTotalSiteVisits(siteId, lastYear, now) / 365.0, 1);
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
		svt.setPercentageOfUsersThatVisitedSite(Util.round(percentageOfUsersThatVisitedSite, 1));
		
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
		
		double last7DaysActivityAverage = Util.round(getTotalSiteActivity(siteId, prefsdata.getToolEventsStringList(), lastWeek, now) / 7.0, 1);
		double last30DaysActivityAverage = Util.round(getTotalSiteActivity(siteId, prefsdata.getToolEventsStringList(), lastMonth, now) / 30.0, 1);
		double last365DaysActivityAverage = Util.round(getTotalSiteActivity(siteId, prefsdata.getToolEventsStringList(), lastYear, now) / 365.0, 1);
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
		//log.info("siteVisits of [siteId:"+siteId+"] from ["+initialDate.toGMTString()+"] to ["+finalDate.toGMTString()+"]: "+siteVisits.toString());
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
			//log.info("siteActivity of [siteId:"+siteId+"] from ["+initialDate.toGMTString()+"] to ["+finalDate.toGMTString()+"]: "+siteActivity.toString());
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
			//log.info("siteActivityByTool of [siteId:"+siteId+"] from ["+initialDate.toGMTString()+"] to ["+finalDate.toGMTString()+"]: "+siteActivityByTool.toString());
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
	public List<Stat> getEventStats(String siteId, List<String> events) {
		return getEventStats(siteId, events, getInitialActivityDate(siteId), null, null, false, null, null, null, true, 0);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getEventStats(java.lang.String, java.util.List, java.lang.String, java.util.Date, java.util.Date)
	 */
	@Deprecated public List<EventStat> getEventStats(final String siteId, final List<String> events, 
			final String searchKey, final Date iDate, final Date fDate) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			final List<String> userIdList = searchUsers(searchKey, siteId);
			/* return if no users matched */
			if(userIdList != null && userIdList.size() == 0)				
				return new ArrayList<EventStat>();
			
			HibernateCallback<List<EventStat>> hcb = session -> {
                Criteria c = session.createCriteria(EventStatImpl.class)
                        .add(Expression.eq("siteId", siteId))
                        .add(Expression.in("eventId", events));
                if(!showAnonymousAccessEvents)
                    c.add(Expression.ne("userId", EventTrackingService.UNKNOWN_USER));
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
            };
			return getHibernateTemplate().execute(hcb);
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getEventStats(java.lang.String, java.util.List, java.util.Date, java.util.Date, java.util.List, boolean, org.sakaiproject.javax.PagingPosition, java.lang.String, java.lang.String, boolean)
	 */
	public List<Stat> getEventStats(
			final String siteId,
			final List<String> events,
			final Date iDate, final Date fDate,
			final List<String> userIds,
			final boolean inverseUserSelection,
			final PagingPosition page, 
			final List<String> totalsBy, 
			final String sortBy, 
			boolean sortAscending,
			final int maxResults) {
		
		final Set<String> anonymousEvents = eventRegistryService.getAnonymousEventIds();
		StatsSqlBuilder sqlBuilder = new StatsSqlBuilder(getDbVendor(),
				Q_TYPE_EVENT, totalsBy, siteId, 
				events, anonymousEvents, showAnonymousAccessEvents, null, null, 
				iDate, fDate, userIds, inverseUserSelection, sortBy, sortAscending);
		final String hql = sqlBuilder.getHQL();
		final Map<Integer,Integer> columnMap = sqlBuilder.getHQLColumnMap();
		
		// DO IT!
		HibernateCallback<List<Stat>> hcb = session -> {
            Query q = session.createQuery(hql);
            if(siteId != null) {
                q.setString("siteid", siteId);
            }
            if(events != null) {
                if(events.isEmpty()) {
                    events.add("");
                }
                q.setParameterList("events", events);
            }
            if(userIds != null && !userIds.isEmpty()) {
                if(userIds.size() <= 1000) {
                    q.setParameterList("users", userIds);
                }else{
                    int nUsers = userIds.size();
                    int blockId = 0, startIndex = 0;
                    int blocks = (int) (nUsers / 1000);
                    blocks = (blocks*1000 == nUsers) ? blocks : blocks+1;
                    for(int i=0; i<blocks-1; i++) {
                        q.setParameterList("users"+blockId, userIds.subList(startIndex, startIndex+1000));
                        blockId++;
                        startIndex += 1000;
                    }
                    q.setParameterList("users"+blockId, userIds.subList(startIndex, nUsers));
                }
            }
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
            if(columnMap.containsKey(StatsSqlBuilder.C_USER) && anonymousEvents != null && anonymousEvents.size() > 0) {
                q.setParameterList("anonymousEvents", anonymousEvents);
            }
            if(page != null){
                q.setFirstResult(page.getFirst() - 1);
                q.setMaxResults(page.getLast() - page.getFirst() + 1);
            }
            if(maxResults > 0) {
                q.setMaxResults(maxResults);
            }
            log.debug("getEventStats(): " + q.getQueryString());
            List<Object[]> records = q.list();
            List<Stat> results = new ArrayList<>();
            Set<String> siteUserIds = null;
            if(inverseUserSelection)
                siteUserIds = getSiteUsers(siteId);
            if(records.size() > 0){
                Calendar cal = Calendar.getInstance();
                Map<String,ToolInfo> eventIdToolMap = eventRegistryService.getEventIdToolMap();
                boolean groupByTool = columnMap.containsKey(StatsSqlBuilder.C_TOOL) && !columnMap.containsKey(StatsSqlBuilder.C_EVENT);
                boolean hasVisitsData = columnMap.containsKey(StatsSqlBuilder.C_VISITS);
                for(Iterator<Object[]> iter = records.iterator(); iter.hasNext();) {
                    if(!inverseUserSelection){
                        Object[] s = iter.next();
                        Stat c = null;
                        String toolId = null;
                        if(!hasVisitsData) {
                            c = new EventStatImpl();
                        }else{
                            c = new SiteVisitsImpl();
                        }
                        if(columnMap.containsKey(StatsSqlBuilder.C_SITE)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_SITE);
                            c.setSiteId((String)s[ix]);
                        }
                        if(columnMap.containsKey(StatsSqlBuilder.C_USER)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_USER);
                            c.setUserId((String)s[ix]);
                        }
                        if(columnMap.containsKey(StatsSqlBuilder.C_EVENT) && !hasVisitsData) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_EVENT);
                            ((EventStat) c).setEventId((String)s[ix]);
                            ToolInfo ti = eventIdToolMap.get((String)s[ix]);
                            toolId = ti != null? ti.getToolId() : (String)s[ix];
                            ((EventStat) c).setToolId(toolId);
                        }
                        if(columnMap.containsKey(StatsSqlBuilder.C_TOOL) && !hasVisitsData) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_TOOL);
                            ToolInfo ti = eventIdToolMap.get((String)s[ix]);
                            toolId = ti != null? ti.getToolId() : (String)s[ix];
                            //
                            ((EventStat) c).setToolId(toolId);
                        }
                        if(columnMap.containsKey(StatsSqlBuilder.C_DATE)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_DATE);
                            c.setDate((Date)s[ix]);
                        }
                        if(columnMap.containsKey(StatsSqlBuilder.C_DATEMONTH)
                            && columnMap.containsKey(StatsSqlBuilder.C_DATEYEAR)) {
                            int ixY = (Integer) columnMap.get(StatsSqlBuilder.C_DATEYEAR);
                            int ixM = (Integer) columnMap.get(StatsSqlBuilder.C_DATEMONTH);
                            int yr = 0, mo = 0;
                            if(getDbVendor().equals("oracle")){
                                yr = Integer.parseInt((String)s[ixY]);
                                mo = Integer.parseInt((String)s[ixM]) - 1;
                            }else{
                                yr = ((Integer)s[ixY]).intValue();
                                mo = ((Integer)s[ixM]).intValue() - 1;
                            }
                            cal.set(Calendar.YEAR, yr);
                            cal.set(Calendar.MONTH, mo);
                            c.setDate(cal.getTime());
                        }else if(columnMap.containsKey(StatsSqlBuilder.C_DATEYEAR)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_DATEYEAR);
                            int yr = 0;
                            if(getDbVendor().equals("oracle")){
                                yr = Integer.parseInt((String)s[ix]);
                            }else{
                                yr = ((Integer)s[ix]).intValue();
                            }
                            cal.set(Calendar.YEAR, yr);
                            c.setDate(cal.getTime());
                        }
                        if(columnMap.containsKey(StatsSqlBuilder.C_TOTAL)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_TOTAL);
                            c.setCount(c.getCount() + ((Long)s[ix]).longValue());
                        }
                        if(columnMap.containsKey(StatsSqlBuilder.C_VISITS)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_VISITS);
                            try{
                                ((SiteVisits) c).setTotalVisits(((Long)s[ix]).longValue());
                            }catch(ClassCastException cce) {
                                ((SiteVisits) c).setTotalVisits(((Integer)s[ix]).intValue());
                            }
                        }
                        if(columnMap.containsKey(StatsSqlBuilder.C_UNIQUEVISITS)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_UNIQUEVISITS);
                            try{
                                ((SiteVisits) c).setTotalUnique(((Long)s[ix]).longValue());
                            }catch(ClassCastException cce) {
                                ((SiteVisits) c).setTotalUnique(((Integer)s[ix]).intValue());
                            }
                        }
                        if(!groupByTool) {
                            results.add(c);
                        }else{
                            // Special case:
                            //	- group by tool (& event not part of grouping)
                            boolean toolAggregated = false;
                            for(Stat s_ : results) {
                                EventStat es_ = (EventStat) s_;
                                if(es_.equalExceptForCount(c)) {
                                    es_.setCount(es_.getCount() + c.getCount());
                                    toolAggregated = true;
                                    break;
                                }
                            }
                            if(!toolAggregated) {
                                results.add(c);
                            }
                        }
                    }else{
                        if(siteUserIds != null) {
                            siteUserIds.remove((Object) iter.next());
                        }
                    }
                }
            }
            if(inverseUserSelection){
                long id = 0;
                Iterator<String> iU = siteUserIds.iterator();
                while(iU.hasNext()){
                    String userId = iU.next();
                    EventStat c = new EventStatImpl();
                    c.setId(id++);
                    c.setUserId(userId);
                    c.setSiteId(siteId);
                    c.setCount(0);
                    results.add(c);
                }
            }
            // hack for hibernate-oracle bug producing duplicate lines
            else if(getDbVendor().equals("oracle") && totalsBy.contains(T_USER) && anonymousEvents != null && anonymousEvents.size() > 0) {
                List<Stat> consolidated = new ArrayList<>();
                for(Stat s : results) {
                    EventStat es = (EventStat) s;
                    boolean found = false;
                    for(Stat c : consolidated) {
                        EventStat esc = (EventStat) c;
                        if(esc.equalExceptForCount((Object)es)) {
                            esc.setCount(esc.getCount() + es.getCount());
                            found = true;
                            break;
                        }
                    }
                    if(!found) {
                        consolidated.add(es);
                    }
                }
                results = consolidated;
            }
            return results;
        };
		return getHibernateTemplate().execute(hcb);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getEventStatsRowCount(java.lang.String, java.util.List, java.util.Date, java.util.Date, java.util.List, boolean, org.sakaiproject.javax.PagingPosition, java.lang.String, java.lang.String, boolean)
	 */
	public int getEventStatsRowCount(
			final String siteId,
			final List<String> events,
			final Date iDate, final Date fDate,
			final List<String> userIds,
			final boolean inverseUserSelection,
			final List<String> totalsBy) {
		
		final Set<String> anonymousEvents = eventRegistryService.getAnonymousEventIds();
		StatsSqlBuilder sqlBuilder = new StatsSqlBuilder(getDbVendor(),
				Q_TYPE_EVENT, totalsBy,
				siteId, events, anonymousEvents, showAnonymousAccessEvents, null, null, 
				iDate, fDate, userIds, inverseUserSelection, null, true);
		final String hql = sqlBuilder.getHQL();
		final Map<Integer,Integer> columnMap = sqlBuilder.getHQLColumnMap();

		// DO IT!
		HibernateCallback<Integer> hcb = session -> {
            Query q = session.createQuery(hql);
            if(siteId != null){
                q.setString("siteid", siteId);
            }
            if(events != null && !events.isEmpty()){
                q.setParameterList("events", events);
            }
            if(userIds != null && !userIds.isEmpty()) {
                if(userIds.size() <= 1000) {
                    q.setParameterList("users", userIds);
                }else{
                    int nUsers = userIds.size();
                    int blockId = 0, startIndex = 0;
                    int blocks = (int) (nUsers / 1000);
                    blocks = (blocks*1000 == nUsers) ? blocks : blocks+1;
                    for(int i=0; i<blocks-1; i++) {
                        q.setParameterList("users"+blockId, userIds.subList(startIndex, startIndex+1000));
                        blockId++;
                        startIndex += 1000;
                    }
                    q.setParameterList("users"+blockId, userIds.subList(startIndex, nUsers));
                }
            }
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
            if(columnMap.containsKey(StatsSqlBuilder.C_USER) && anonymousEvents != null && anonymousEvents.size() > 0){
                q.setParameterList("anonymousEvents", anonymousEvents);
            }
            log.debug("getEventStatsRowCount(): " + q.getQueryString());
            Integer rowCount = q.list().size();
            if(!inverseUserSelection){
                return rowCount;
            }else{
                return getSiteUsers(siteId).size() - rowCount;
            }
        };
		return getHibernateTemplate().execute(hcb);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getEventStats(java.lang.String, java.util.List, java.util.Date, java.util.Date, java.util.List, boolean, org.sakaiproject.javax.PagingPosition, java.lang.String, java.lang.String, boolean)
	 */
	public List<Stat> getPresenceStats(
			final String siteId,
			final Date iDate, final Date fDate,
			final List<String> userIds,
			final boolean inverseUserSelection,
			final PagingPosition page, 
			final List<String> totalsBy, 
			final String sortBy, 
			boolean sortAscending,
			final int maxResults) {
		
		StatsSqlBuilder sqlBuilder = new StatsSqlBuilder(getDbVendor(),
				Q_TYPE_PRESENCE, totalsBy, siteId,
				(Set<String>)null, null, showAnonymousAccessEvents, null, null, 
				iDate, fDate, userIds, inverseUserSelection, sortBy, sortAscending);
		final String hql = sqlBuilder.getHQL();
		final Map<Integer,Integer> columnMap = sqlBuilder.getHQLColumnMap();
		
		// DO IT!
		HibernateCallback<List<Stat>> hcb = session -> {
            Query q = session.createQuery(hql);
            if(siteId != null) {
                q.setString("siteid", siteId);
            }
            if(userIds != null && !userIds.isEmpty()) {
                if(userIds.size() <= 1000) {
                    q.setParameterList("users", userIds);
                }else{
                    int nUsers = userIds.size();
                    int blockId = 0, startIndex = 0;
                    int blocks = (int) (nUsers / 1000);
                    blocks = (blocks*1000 == nUsers) ? blocks : blocks+1;
                    for(int i=0; i<blocks-1; i++) {
                        q.setParameterList("users"+blockId, userIds.subList(startIndex, startIndex+1000));
                        blockId++;
                        startIndex += 1000;
                    }
                    q.setParameterList("users"+blockId, userIds.subList(startIndex, nUsers));
                }
            }
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
            if(maxResults > 0) {
                q.setMaxResults(maxResults);
            }
            log.debug("getPresenceStats(): " + q.getQueryString());
            List<Object[]> records = q.list();
            List<Stat> results = new ArrayList<Stat>();
            Set<String> siteUserIds = null;
            if(inverseUserSelection)
                siteUserIds = getSiteUsers(siteId);
            if(records.size() > 0){
                Calendar cal = Calendar.getInstance();
                for(Iterator<Object[]> iter = records.iterator(); iter.hasNext();) {
                    if(!inverseUserSelection){
                        Object[] s = iter.next();
                        SitePresence c = new SitePresenceImpl();
                        if(columnMap.containsKey(StatsSqlBuilder.C_SITE)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_SITE);
                            c.setSiteId((String)s[ix]);
                        }
                        if(columnMap.containsKey(StatsSqlBuilder.C_USER)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_USER);
                            c.setUserId((String)s[ix]);
                        }
                        if(columnMap.containsKey(StatsSqlBuilder.C_DATE)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_DATE);
                            c.setDate((Date)s[ix]);
                        }
                        if(columnMap.containsKey(StatsSqlBuilder.C_DATEMONTH)
                            && columnMap.containsKey(StatsSqlBuilder.C_DATEYEAR)) {
                            int ixY = (Integer) columnMap.get(StatsSqlBuilder.C_DATEYEAR);
                            int ixM = (Integer) columnMap.get(StatsSqlBuilder.C_DATEMONTH);
                            int yr = 0, mo = 0;
                            if(getDbVendor().equals("oracle")){
                                yr = Integer.parseInt((String)s[ixY]);
                                mo = Integer.parseInt((String)s[ixM]) - 1;
                            }else{
                                yr = ((Integer)s[ixY]).intValue();
                                mo = ((Integer)s[ixM]).intValue() - 1;
                            }
                            cal.set(Calendar.YEAR, yr);
                            cal.set(Calendar.MONTH, mo);
                            c.setDate(cal.getTime());
                        }else if(columnMap.containsKey(StatsSqlBuilder.C_DATEYEAR)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_DATEYEAR);
                            int yr = 0;
                            if(getDbVendor().equals("oracle")){
                                yr = Integer.parseInt((String)s[ix]);
                            }else{
                                yr = ((Integer)s[ix]).intValue();
                            }
                            cal.set(Calendar.YEAR, yr);
                            c.setDate(cal.getTime());
                        }
                        {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_DURATION);
                            c.setDuration(c.getDuration() + ((Long)s[ix]).longValue());
                        }
                        results.add(c);
                    }else{
                        if(siteUserIds != null) {
                            siteUserIds.remove((Object) iter.next());
                        }
                    }
                }
            }
            if(inverseUserSelection){
                long id = 0;
                Iterator<String> iU = siteUserIds.iterator();
                while(iU.hasNext()){
                    String userId = iU.next();
                    SitePresence c = new SitePresenceImpl();
                    c.setId(id++);
                    c.setUserId(userId);
                    c.setSiteId(siteId);
                    c.setDuration(0);
                    c.setCount(0);
                    results.add(c);
                }
            }
            return results;
        };
		return getHibernateTemplate().execute(hcb);
	}
	
	public int getPresenceStatsRowCount(
			final String siteId,
			final Date iDate, final Date fDate,
			final List<String> userIds,
			final boolean inverseUserSelection,
			final List<String> totalsBy) {
		
		StatsSqlBuilder sqlBuilder = new StatsSqlBuilder(getDbVendor(),
				Q_TYPE_PRESENCE, totalsBy,
				null, (Set<String>)null, null, showAnonymousAccessEvents, null, null, 
				iDate, fDate, userIds, inverseUserSelection, null, true);
		final String hql = sqlBuilder.getHQL();

		// DO IT!
		HibernateCallback<Integer> hcb = session -> {
            Query q = session.createQuery(hql);
            if(siteId != null){
                q.setString("siteid", siteId);
            }
            if(userIds != null && !userIds.isEmpty()) {
                if(userIds.size() <= 1000) {
                    q.setParameterList("users", userIds);
                }else{
                    int nUsers = userIds.size();
                    int blockId = 0, startIndex = 0;
                    int blocks = (int) (nUsers / 1000);
                    blocks = (blocks*1000 == nUsers) ? blocks : blocks+1;
                    for(int i=0; i<blocks-1; i++) {
                        q.setParameterList("users"+blockId, userIds.subList(startIndex, startIndex+1000));
                        blockId++;
                        startIndex += 1000;
                    }
                    q.setParameterList("users"+blockId, userIds.subList(startIndex, nUsers));
                }
            }
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
            log.debug("getPresenceStatsRowCount(): " + q.getQueryString());
            Integer rowCount = q.list().size();
            if(!inverseUserSelection){
                return rowCount;
            }else{
                return getSiteUsers(siteId).size() - rowCount;
            }
        };
		return getHibernateTemplate().execute(hcb);
	}

	public Map<String, SitePresenceTotal> getPresenceTotalsForSite(final String siteId) {

		HibernateCallback<List<SitePresenceTotal>> hcb = session -> {
            String hql = "FROM SitePresenceTotalImpl st WHERE st.siteId = :siteId";
            Query q = session.createQuery(hql);
            q.setString("siteId", siteId);
            log.debug("getPresenceTotalsForSite(): " + q.getQueryString());
            return q.list();
        };

		final Map<String, SitePresenceTotal> totals = new HashMap<String, SitePresenceTotal>();
		List<SitePresenceTotal> siteTotals = getHibernateTemplate().execute(hcb);
		for (SitePresenceTotal total : siteTotals) {
			totals.put(total.getUserId(), total);
		}
		return totals;
	}

	
	// ################################################################
	// Resource related methods
	// ################################################################
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getResourceStats(java.lang.String)
	 */
	public List<Stat> getResourceStats(String siteId) {
		return getResourceStats(siteId, null, null, getInitialActivityDate(siteId), null, null, false, null, null, null, true, 0);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getResourceStats(java.lang.String, java.lang.String, java.util.Date, java.util.Date, boolean)
	 */
	@Deprecated public List<ResourceStat> getResourceStats(final String siteId, final String searchKey, 
			final Date iDate, final Date fDate) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			final List<String> userIdList = searchUsers(searchKey, siteId);		
			/* return if no users matched */
			if(userIdList != null && userIdList.size() == 0)				
				return new ArrayList<ResourceStat>();	
			
			HibernateCallback<List<ResourceStat>> hcb = session -> {
                Criteria c = session.createCriteria(ResourceStatImpl.class)
                        .add(Expression.eq("siteId", siteId));
                if(!showAnonymousAccessEvents)
                    c.add(Expression.ne("userId", EventTrackingService.UNKNOWN_USER));
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
            };
			return getHibernateTemplate().execute(hcb);
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getResourceStats(java.lang.String, java.lang.String, java.util.List, java.util.Date, java.util.Date, java.util.List, boolean, org.sakaiproject.javax.PagingPosition, java.lang.String, java.lang.String, boolean)
	 */
	public List<Stat> getResourceStats(
			final String siteId,
			final String resourceAction, final List<String> resourceIds,
			final Date iDate, final Date fDate,
			final List<String> userIds,
			final boolean inverseUserSelection,
			final PagingPosition page, 
			final List<String> totalsBy,
			final String sortBy, 
			final boolean sortAscending,
			final int maxResults) {
		
		StatsSqlBuilder sqlBuilder = new StatsSqlBuilder(getDbVendor(),
				Q_TYPE_RESOURCE, totalsBy, 
				siteId, (Set<String>)null, null, showAnonymousAccessEvents, resourceAction, resourceIds, 
				iDate, fDate, userIds, inverseUserSelection, sortBy, sortAscending);
		final String hql = sqlBuilder.getHQL();
		final Map<Integer,Integer> columnMap = sqlBuilder.getHQLColumnMap();

		HibernateCallback<List<Stat>> hcb = session -> {
            Query q = session.createQuery(hql);
            if(siteId != null){
                q.setString("siteid", siteId);
            }
            if(userIds != null && !userIds.isEmpty()) {
                if(userIds.size() <= 1000) {
                    q.setParameterList("users", userIds);
                }else{
                    int nUsers = userIds.size();
                    int blockId = 0, startIndex = 0;
                    int blocks = (int) (nUsers / 1000);
                    blocks = (blocks*1000 == nUsers) ? blocks : blocks+1;
                    for(int i=0; i<blocks-1; i++) {
                        q.setParameterList("users"+blockId, userIds.subList(startIndex, startIndex+1000));
                        blockId++;
                        startIndex += 1000;
                    }
                    q.setParameterList("users"+blockId, userIds.subList(startIndex, nUsers));
                }
            }
            if(resourceAction != null)
                q.setString("action", resourceAction);
            if(resourceIds != null && !resourceIds.isEmpty()) {
                List<String> simpleResourceIds = new ArrayList<String>();
                List<String> wildcardResourceIds = new ArrayList<String>();
                for(String rId : resourceIds) {
                    if(rId.endsWith("/")) {
                        wildcardResourceIds.add(rId + "%");
                    }else{
                        simpleResourceIds.add(rId);
                    }
                }
                if(simpleResourceIds.size() > 0) {
                    q.setParameterList("resources", resourceIds);
                }
                for(int i=0; i<wildcardResourceIds.size(); i++) {
                    q.setString("resource"+i, wildcardResourceIds.get(i));
                }
            }
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
            if(maxResults > 0) {
                q.setMaxResults(maxResults);
            }
            log.debug("getResourceStats(): " + q.getQueryString());
            List<Object[]> records = q.list();
            List<Stat> results = new ArrayList<>();
            Set<String> siteUserIds = null;
            if(inverseUserSelection){
                siteUserIds = getSiteUsers(siteId);
            }
            if(records.size() > 0){
                Calendar cal = Calendar.getInstance();
                for(Iterator<Object[]> iter = records.iterator(); iter.hasNext();){
                    if(!inverseUserSelection){
                        Object[] s = iter.next();
                        ResourceStat c = new ResourceStatImpl();
                        if(columnMap.containsKey(StatsSqlBuilder.C_SITE)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_SITE);
                            c.setSiteId((String)s[ix]);
                        }
                        if(columnMap.containsKey(StatsSqlBuilder.C_USER)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_USER);
                            c.setUserId((String)s[ix]);
                        }
                        if(columnMap.containsKey(StatsSqlBuilder.C_RESOURCE)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_RESOURCE);
                            c.setResourceRef((String)s[ix]);
                        }
                        if(columnMap.containsKey(StatsSqlBuilder.C_RESOURCE_ACTION)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_RESOURCE_ACTION);
                            c.setResourceAction((String)s[ix]);
                        }
                        if(columnMap.containsKey(StatsSqlBuilder.C_DATE)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_DATE);
                            c.setDate((Date)s[ix]);
                        }
                        if(columnMap.containsKey(StatsSqlBuilder.C_DATEMONTH)
                                && columnMap.containsKey(StatsSqlBuilder.C_DATEYEAR)) {
                                int ixY = (Integer) columnMap.get(StatsSqlBuilder.C_DATEYEAR);
                                int ixM = (Integer) columnMap.get(StatsSqlBuilder.C_DATEMONTH);
                                int yr = 0, mo = 0;
                                if(getDbVendor().equals("oracle")){
                                    yr = Integer.parseInt((String)s[ixY]);
                                    mo = Integer.parseInt((String)s[ixM]) - 1;
                                }else{
                                    yr = ((Integer)s[ixY]).intValue();
                                    mo = ((Integer)s[ixM]).intValue() - 1;
                                }
                                cal.set(Calendar.YEAR, yr);
                                cal.set(Calendar.MONTH, mo);
                                c.setDate(cal.getTime());
                            }else if(columnMap.containsKey(StatsSqlBuilder.C_DATEYEAR)) {
                                int ix = (Integer) columnMap.get(StatsSqlBuilder.C_DATEYEAR);
                                int yr = 0;
                                if(getDbVendor().equals("oracle")){
                                    yr = Integer.parseInt((String)s[ix]);
                                }else{
                                    yr = ((Integer)s[ix]).intValue();
                                }
                                cal.set(Calendar.YEAR, yr);
                                c.setDate(cal.getTime());
                            }
                        if(columnMap.containsKey(StatsSqlBuilder.C_TOTAL)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_TOTAL);
                            c.setCount(((Long)s[ix]).longValue());
                        }
                        results.add(c);
                    }else{
                        if(siteUserIds != null) {
                            siteUserIds.remove((Object) iter.next());
                        }
                    }
                }
            }
            if(inverseUserSelection){
                long id = 0;
                Iterator<String> iU = siteUserIds.iterator();
                while (iU.hasNext()){
                    String userId = iU.next();
                    ResourceStat c = new ResourceStatImpl();
                    c.setId(id++);
                    c.setUserId(userId);
                    c.setSiteId(siteId);
                    c.setCount(0);
                    results.add(c);
                }
            }
            return results;
        };
		return getHibernateTemplate().execute(hcb);
	}

	public List<Stat> getLessonBuilderStats(final String siteId,
										final String pageAction,
										final List<String> resourceIds,
										final Date iDate,
										final Date fDate,
										final List<String> userIds,
										final boolean inverseUserSelection,
										final PagingPosition page,
										final List<String> totalsBy,
										final String sortBy,
										final boolean sortAscending,
										final int maxResults) {

		StatsSqlBuilder sqlBuilder = new StatsSqlBuilder(getDbVendor(),
				Q_TYPE_LESSON, totalsBy,
				siteId, (Set<String>)null, null, false, pageAction, resourceIds,
				iDate, fDate, userIds, inverseUserSelection, sortBy, sortAscending);

		final String hql = sqlBuilder.getHQL();

		final Map<Integer,Integer> columnMap = sqlBuilder.getHQLColumnMap();

		HibernateCallback<List<Stat>> hcb = session -> {

            Query q = session.createQuery(hql);
            q.setString("siteid", siteId);

            if (userIds != null && !userIds.isEmpty()) {
                if (userIds.size() <= 1000) {
                    q.setParameterList("users", userIds);
                } else {
                    int nUsers = userIds.size();
                    int blockId = 0, startIndex = 0;
                    int blocks = (int) (nUsers / 1000);
                    blocks = (blocks*1000 == nUsers) ? blocks : blocks+1;
                    for (int i = 0; i < blocks - 1; i++) {
                        q.setParameterList("users" + blockId, userIds.subList(startIndex, startIndex + 1000));
                        blockId++;
                        startIndex += 1000;
                    }
                    q.setParameterList("users" + blockId, userIds.subList(startIndex, nUsers));
                }
            }
            if (pageAction != null) {
                q.setString("action", pageAction);
            }

            if (resourceIds != null && !resourceIds.isEmpty()) {
                List<String> simpleResourceIds = new ArrayList<String>();
                List<String> wildcardResourceIds = new ArrayList<String>();
                for (String rId : resourceIds) {
                    if (rId.endsWith("/")) {
                        wildcardResourceIds.add(rId + "%");
                    } else {
                        simpleResourceIds.add(rId);
                    }
                }
                if (simpleResourceIds.size() > 0) {
                    q.setParameterList("resources", resourceIds);
                }
                for (int i=0; i<wildcardResourceIds.size(); i++) {
                    q.setString("resource"+i, wildcardResourceIds.get(i));
                }
            }

            if (iDate != null) {
                q.setDate("idate", iDate);
            }
            if (fDate != null) {
                // adjust final date
                Calendar c = Calendar.getInstance();
                c.setTime(fDate);
                c.add(Calendar.DAY_OF_YEAR, 1);
                Date fDate2 = c.getTime();
                q.setDate("fdate", fDate2);
            }
            if (page != null) {
                q.setFirstResult(page.getFirst() - 1);
                q.setMaxResults(page.getLast() - page.getFirst() + 1);
            }
            if (maxResults > 0) {
                q.setMaxResults(maxResults);
            }

if (log.isDebugEnabled()) {
                log.debug("getLessonBuilderStats(): " + q.getQueryString());
}

            List<Object[]> records = q.list();
            List<Stat> results = new ArrayList<>();
            Set<String> siteUserIds = null;
            if (inverseUserSelection) {
                siteUserIds = getSiteUsers(siteId);
            }
            if (records.size() > 0) {
                Calendar cal = Calendar.getInstance();
                for (Iterator<Object[]> iter = records.iterator(); iter.hasNext();) {
                    if (!inverseUserSelection) {
                        Object[] s = iter.next();
                        LessonBuilderStat stat = new LessonBuilderStatImpl();
                        if (columnMap.containsKey(StatsSqlBuilder.C_SITE)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_SITE);
                            stat.setSiteId((String)s[ix]);
                        }
                        if (columnMap.containsKey(StatsSqlBuilder.C_USER)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_USER);
                            stat.setUserId((String)s[ix]);
                        }
                        if (columnMap.containsKey(StatsSqlBuilder.C_PAGE)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_PAGE);
                            stat.setPageRef((String)s[ix]);
                        }
                        if (columnMap.containsKey(StatsSqlBuilder.C_PAGE_ACTION)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_PAGE_ACTION);
                            stat.setPageAction((String)s[ix]);
                        }
                        if (columnMap.containsKey(StatsSqlBuilder.C_PAGE_ID)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_PAGE_ID);
                            stat.setPageId((Long)s[ix]);
                        }
                        if (columnMap.containsKey(StatsSqlBuilder.C_DATE)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_DATE);
                            stat.setDate((Date)s[ix]);
                        }
                        if (columnMap.containsKey(StatsSqlBuilder.C_DATEMONTH)
                                && columnMap.containsKey(StatsSqlBuilder.C_DATEYEAR)) {
                            int ixY = (Integer) columnMap.get(StatsSqlBuilder.C_DATEYEAR);
                            int ixM = (Integer) columnMap.get(StatsSqlBuilder.C_DATEMONTH);
                            int yr = 0, mo = 0;
                            if (getDbVendor().equals("oracle")){
                                yr = Integer.parseInt((String)s[ixY]);
                                mo = Integer.parseInt((String)s[ixM]) - 1;
                            } else {
                                yr = ((Integer)s[ixY]).intValue();
                                mo = ((Integer)s[ixM]).intValue() - 1;
                            }
                            cal.set(Calendar.YEAR, yr);
                            cal.set(Calendar.MONTH, mo);
                            stat.setDate(cal.getTime());
                        } else if (columnMap.containsKey(StatsSqlBuilder.C_DATEYEAR)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_DATEYEAR);
                            int yr = 0;
                            if (getDbVendor().equals("oracle")){
                                yr = Integer.parseInt((String)s[ix]);
                            } else {
                                yr = ((Integer)s[ix]).intValue();
                            }
                            cal.set(Calendar.YEAR, yr);
                            stat.setDate(cal.getTime());
                        }
                        if (columnMap.containsKey(StatsSqlBuilder.C_TOTAL)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_TOTAL);
                            Long total = (Long) s[ix];
                            if (total != null) {
                                stat.setCount(total.longValue());
                            }
                        }

                        stat.setPageTitle(getLessonPageTitle(stat.getPageId()));

                        results.add(stat);
                    } else {
                        if (siteUserIds != null) {
                            siteUserIds.remove((Object) iter.next());
                        }
                    }
                }
            }
            if (inverseUserSelection) {
                long id = 0;
                Iterator<String> iU = siteUserIds.iterator();
                while (iU.hasNext()) {
                    String userId = iU.next();
                    LessonBuilderStat c = new LessonBuilderStatImpl();
                    c.setId(id++);
                    c.setUserId(userId);
                    c.setSiteId(siteId);
                    c.setCount(0);
                    results.add(c);
                }
            }
            return results;
        };
		return getHibernateTemplate().execute(hcb);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getResourceStatsRowCount(java.lang.String, java.lang.String, java.util.List, java.util.Date, java.util.Date, java.util.List, boolean, org.sakaiproject.javax.PagingPosition, java.lang.String, java.lang.String, boolean)
	 */
	public int getResourceStatsRowCount(
			final String siteId,
			final String resourceAction, final List<String> resourceIds,
			final Date iDate, final Date fDate,
			final List<String> userIds,
			final boolean inverseUserSelection,
			final List<String> totalsBy) {

		StatsSqlBuilder sqlBuilder = new StatsSqlBuilder(getDbVendor(),
				Q_TYPE_RESOURCE, totalsBy, 
				siteId, (Set<String>)null, null, showAnonymousAccessEvents, resourceAction, resourceIds, 
				iDate, fDate, userIds, inverseUserSelection, null, true);
		final String hql = sqlBuilder.getHQL();

		HibernateCallback<Integer> hcb = session -> {
            Query q = session.createQuery(hql);
            if(siteId != null){
                q.setString("siteid", siteId);
            }
            if(userIds != null && !userIds.isEmpty()) {
                if(userIds.size() <= 1000) {
                    q.setParameterList("users", userIds);
                }else{
                    int nUsers = userIds.size();
                    int blockId = 0, startIndex = 0;
                    int blocks = (int) (nUsers / 1000);
                    blocks = (blocks*1000 == nUsers) ? blocks : blocks+1;
                    for(int i=0; i<blocks-1; i++) {
                        q.setParameterList("users"+blockId, userIds.subList(startIndex, startIndex+1000));
                        blockId++;
                        startIndex += 1000;
                    }
                    q.setParameterList("users"+blockId, userIds.subList(startIndex, nUsers));
                }
            }
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
            log.debug("getEventStatsRowCount(): " + q.getQueryString());
            Integer rowCount = q.list().size();
            if(!inverseUserSelection){
                return rowCount;
            }else{
                return getSiteUsers(siteId).size() - rowCount;
            }
        };
		return getHibernateTemplate().execute(hcb);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getVisitsTotalsStats(java.lang.String, java.util.Date, java.util.Date, org.sakaiproject.javax.PagingPosition, java.util.List, java.lang.String, boolean, int)
	 */
	public List<Stat> getVisitsTotalsStats(
			final String siteId, 
			final Date iDate, final Date fDate, 
			final PagingPosition page, 
			final List<String> totalsBy, 
			final String sortBy, 
			final boolean sortAscending, 
			final int maxResults) {
		
		StatsSqlBuilder sqlBuilder = new StatsSqlBuilder(getDbVendor(),
				Q_TYPE_VISITSTOTALS, totalsBy, siteId, 
				(Set<String>)null, null, showAnonymousAccessEvents, null, null, 
				iDate, fDate, null, false, sortBy, sortAscending);
		final String hql = sqlBuilder.getHQL();
		final Map<Integer,Integer> columnMap = sqlBuilder.getHQLColumnMap();
		
		// DO IT!
		HibernateCallback<List<Stat>> hcb = session -> {
            Query q = session.createQuery(hql);
            if(siteId != null) {
                q.setString("siteid", siteId);
            }
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
            if(maxResults > 0) {
                q.setMaxResults(maxResults);
            }
            log.debug("getVisitsTotalsStats(): " + q.getQueryString());
            List<Object[]> records = q.list();
            List<Stat> results = new ArrayList<>();
            if(records.size() > 0){
                Calendar cal = Calendar.getInstance();
                for(Iterator<Object[]> iter = records.iterator(); iter.hasNext();) {
                        Object[] s = iter.next();
                        SiteVisits c = new SiteVisitsImpl();
                        if(columnMap.containsKey(StatsSqlBuilder.C_SITE)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_SITE);
                            c.setSiteId((String)s[ix]);
                        }
                        if(columnMap.containsKey(StatsSqlBuilder.C_DATE)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_DATE);
                            c.setDate((Date)s[ix]);
                        }
                        if(columnMap.containsKey(StatsSqlBuilder.C_DATEMONTH)
                            && columnMap.containsKey(StatsSqlBuilder.C_DATEYEAR)) {
                            int ixY = (Integer) columnMap.get(StatsSqlBuilder.C_DATEYEAR);
                            int ixM = (Integer) columnMap.get(StatsSqlBuilder.C_DATEMONTH);
                            int yr = 0, mo = 0;
                            if(getDbVendor().equals("oracle")){
                                yr = Integer.parseInt((String)s[ixY]);
                                mo = Integer.parseInt((String)s[ixM]) - 1;
                            }else{
                                yr = ((Integer)s[ixY]).intValue();
                                mo = ((Integer)s[ixM]).intValue() - 1;
                            }
                            cal.set(Calendar.YEAR, yr);
                            cal.set(Calendar.MONTH, mo);
                            c.setDate(cal.getTime());
                        }else if(columnMap.containsKey(StatsSqlBuilder.C_DATEYEAR)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_DATEYEAR);
                            int yr = 0;
                            if(getDbVendor().equals("oracle")){
                                yr = Integer.parseInt((String)s[ix]);
                            }else{
                                yr = ((Integer)s[ix]).intValue();
                            }
                            cal.set(Calendar.YEAR, yr);
                            c.setDate(cal.getTime());
                        }
                        if(columnMap.containsKey(StatsSqlBuilder.C_VISITS)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_VISITS);
                            try{
                                c.setTotalVisits(((Long)s[ix]).longValue());
                            }catch(ClassCastException cce) {
                                c.setTotalVisits(((Integer)s[ix]).intValue());
                            }
                        }
                        if(columnMap.containsKey(StatsSqlBuilder.C_UNIQUEVISITS)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_UNIQUEVISITS);
                            try{
                                c.setTotalUnique(((Long)s[ix]).longValue());
                            }catch(ClassCastException cce) {
                                c.setTotalUnique(((Integer)s[ix]).intValue());
                            }
                        }
                        results.add(c);
                }
            }
            return results;
        };
		return getHibernateTemplate().execute(hcb);
	}
	
	public List<Stat> getActivityTotalsStats(
			final String siteId, 
			final List<String> events, 
			final Date iDate, final Date fDate, 
			final PagingPosition page, 
			final List<String> totalsBy, 
			final String sortBy, 
			final boolean sortAscending, 
			final int maxResults) {
		
		final Set<String> anonymousEvents = eventRegistryService.getAnonymousEventIds();
		StatsSqlBuilder sqlBuilder = new StatsSqlBuilder(getDbVendor(),
				Q_TYPE_ACTIVITYTOTALS, totalsBy, siteId, 
				events, anonymousEvents, showAnonymousAccessEvents, null, null, 
				iDate, fDate, null, false, sortBy, sortAscending);
		final String hql = sqlBuilder.getHQL();
		final Map<Integer,Integer> columnMap = sqlBuilder.getHQLColumnMap();
		
		// DO IT!
		HibernateCallback<List> hcb = session -> {
            Query q = session.createQuery(hql);
            if(siteId != null) {
                q.setString("siteid", siteId);
            }
            if(events != null) {
                if(events.isEmpty()) {
                    events.add("");
                }
                q.setParameterList("events", events);
            }
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
            if(columnMap.containsKey(StatsSqlBuilder.C_USER) && anonymousEvents != null && anonymousEvents.size() > 0) {
                q.setParameterList("anonymousEvents", anonymousEvents);
            }
            if(page != null){
                q.setFirstResult(page.getFirst() - 1);
                q.setMaxResults(page.getLast() - page.getFirst() + 1);
            }
            if(maxResults > 0) {
                q.setMaxResults(maxResults);
            }
            log.debug("getActivityTotalsStats(): " + q.getQueryString());
            List<Object[]> records = q.list();
            List<EventStat> results = new ArrayList<>();
            if(records.size() > 0){
                Calendar cal = Calendar.getInstance();
                Map<String,ToolInfo> eventIdToolMap = eventRegistryService.getEventIdToolMap();
                Map<String,Integer> toolIdEventStatIxMap = new HashMap<String,Integer>();
                boolean groupByTool = columnMap.containsKey(StatsSqlBuilder.C_TOOL) && !columnMap.containsKey(StatsSqlBuilder.C_EVENT);
                for(Iterator<Object[]> iter = records.iterator(); iter.hasNext();) {
                        Object[] s = iter.next();
                        EventStat c = null;
                        int eventStatListIndex = -1;
                        String toolId = null;
                        if(!groupByTool) {
                            c = new EventStatImpl();
                        }else{
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_TOOL);
                            ToolInfo ti = eventIdToolMap.get((String)s[ix]);
                            toolId = ti != null? ti.getToolId() : (String)s[ix];
                            Integer esIx = toolIdEventStatIxMap.get(toolId);
                            if(esIx == null) {
                                c = new EventStatImpl();
                            }else{
                                eventStatListIndex = esIx.intValue();
                                c = results.get(eventStatListIndex);
                            }
                        }
                        if(columnMap.containsKey(StatsSqlBuilder.C_SITE)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_SITE);
                            c.setSiteId((String)s[ix]);
                        }
                        if(columnMap.containsKey(StatsSqlBuilder.C_EVENT)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_EVENT);
                            c.setEventId((String)s[ix]);
                            ToolInfo ti = eventIdToolMap.get((String)s[ix]);
                            toolId = ti != null? ti.getToolId() : (String)s[ix];
                            c.setToolId(toolId);
                        }
                        if(columnMap.containsKey(StatsSqlBuilder.C_TOOL)) {
                            c.setToolId(toolId);
                        }
                        if(columnMap.containsKey(StatsSqlBuilder.C_DATE)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_DATE);
                            c.setDate((Date)s[ix]);
                        }
                        if(columnMap.containsKey(StatsSqlBuilder.C_DATEMONTH)
                            && columnMap.containsKey(StatsSqlBuilder.C_DATEYEAR)) {
                            int ixY = (Integer) columnMap.get(StatsSqlBuilder.C_DATEYEAR);
                            int ixM = (Integer) columnMap.get(StatsSqlBuilder.C_DATEMONTH);
                            int yr = 0, mo = 0;
                            if(getDbVendor().equals("oracle")){
                                yr = Integer.parseInt((String)s[ixY]);
                                mo = Integer.parseInt((String)s[ixM]) - 1;
                            }else{
                                yr = ((Integer)s[ixY]).intValue();
                                mo = ((Integer)s[ixM]).intValue() - 1;
                            }
                            cal.set(Calendar.YEAR, yr);
                            cal.set(Calendar.MONTH, mo);
                            c.setDate(cal.getTime());
                        }else if(columnMap.containsKey(StatsSqlBuilder.C_DATEYEAR)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_DATEYEAR);
                            int yr = 0;
                            if(getDbVendor().equals("oracle")){
                                yr = Integer.parseInt((String)s[ix]);
                            }else{
                                yr = ((Integer)s[ix]).intValue();
                            }
                            cal.set(Calendar.YEAR, yr);
                            c.setDate(cal.getTime());
                        }
                        if(columnMap.containsKey(StatsSqlBuilder.C_TOTAL)) {
                            int ix = (Integer) columnMap.get(StatsSqlBuilder.C_TOTAL);
                            c.setCount(c.getCount() + ((Long)s[ix]).longValue());
                        }
                        if(eventStatListIndex == -1) {
                            results.add(c);
                            toolIdEventStatIxMap.put(toolId, results.size()-1);
                        }else{
                            results.set(eventStatListIndex, c);
                        }
                }
            }
            return results;
        };
		return getHibernateTemplate().execute(hcb);
	}
	
	
	// ################################################################
	//  Statistics SQL builder class
	// ################################################################
	private static class StatsSqlBuilder {
		public static final Integer		C_SITE				= 0;
		public static final Integer		C_USER				= 1;
		public static final Integer		C_EVENT				= 2;
		public static final Integer		C_TOOL				= 3;
		public static final Integer		C_RESOURCE			= 4;
		public static final Integer		C_RESOURCE_ACTION	= 5;
		public static final Integer		C_DATE				= 6;
		public static final Integer		C_DATEYEAR			= 7;
		public static final Integer		C_DATEMONTH			= 8;
		public static final Integer		C_TOTAL				= 9;
		public static final Integer		C_VISITS			= 10;
		public static final Integer		C_UNIQUEVISITS		= 11;
		public static final Integer		C_DURATION			= 12;
		public static final Integer		C_PAGE			    = 13;
		public static final Integer		C_PAGE_ACTION	    = 14;
		public static final Integer		C_PAGE_ID	    	= 15;

		private Map<Integer, Integer>	columnMap;
		
		private String					dbVendor;

		private int						queryType;
		private List<String>			totalsBy;
		private String					siteId;
		private Set<String>				events;
		private Set<String>				anonymousEvents;
		private boolean					showAnonymousAccessEvents;
		private Date					iDate;
		private Date					fDate;
		private List<String>			userIds;
		private String					resourceAction;
		private List<String>			resourceIds;
		private boolean					inverseUserSelection;
		private String					sortBy;
		private boolean					sortAscending;	
		
		public StatsSqlBuilder(
				final String dbVendor,
				final int queryType,
				final List<String> totalsBy,
				final String siteId,
				final Set<String> events, 
				final Set<String> anonymousEvents,
				final boolean showAnonymousAccessEvents,
				final String resourceAction,
				final List<String> resourceIds,
				final Date iDate, final Date fDate,
				final List<String> userIds,
				final boolean inverseUserSelection,
				final String sortBy, final boolean sortAscending) {
			this.columnMap = new HashMap<Integer, Integer>();
			this.dbVendor = dbVendor;
			this.queryType = queryType;
			if(totalsBy == null) {
				if(queryType == Q_TYPE_EVENT) {
					this.totalsBy = TOTALSBY_EVENT_DEFAULT;
				}else if(queryType == Q_TYPE_RESOURCE){
					this.totalsBy = TOTALSBY_RESOURCE_DEFAULT;
				}else if(queryType == Q_TYPE_VISITSTOTALS){
					this.totalsBy = TOTALSBY_VISITSTOTALS_DEFAULT;
				}else if(queryType == Q_TYPE_ACTIVITYTOTALS){
					this.totalsBy = TOTALSBY_ACTIVITYTOTALS_DEFAULT;
				}else if(queryType == Q_TYPE_PRESENCE){
					this.totalsBy = TOTALSBY_PRESENCE_DEFAULT;
				}else if(queryType == Q_TYPE_LESSON){
					this.totalsBy = TOTALSBY_LESSONS_DEFAULT;
				}
			}else{
				this.totalsBy = totalsBy;
			}
			this.siteId = siteId;
			this.events = events;
			this.anonymousEvents = anonymousEvents;
			this.showAnonymousAccessEvents = showAnonymousAccessEvents;
			this.resourceAction = resourceAction;
			this.resourceIds = resourceIds;
			this.iDate = iDate;
			this.fDate = fDate;
			this.userIds = userIds;
			this.inverseUserSelection = inverseUserSelection;
			this.sortBy = sortBy;
			this.sortAscending = sortAscending;
		}
		
		/**
		 * This constructor take older style arguments (List of events) which may
		 * be null and converts them to a set then passes them through.
		 */
		public StatsSqlBuilder(
				final String dbVendor,
				final int queryType,
				final List<String> totalsBy,
				final String siteId,
				final List<String> events, 
				final Set<String> anonymousEvents,
				final boolean showAnonymousAccessEvents,
				final String resourceAction,
				final List<String> resourceIds,
				final Date iDate, final Date fDate,
				final List<String> userIds,
				final boolean inverseUserSelection,
				final String sortBy, final boolean sortAscending) {
			this(dbVendor, queryType, totalsBy, siteId, (events == null) ? null
					: new HashSet<String>(events), anonymousEvents,
					showAnonymousAccessEvents, resourceAction, resourceIds,
					iDate, fDate, userIds, inverseUserSelection, sortBy,
					sortAscending);
		}
		
		public String getHQL() {
			StringBuilder hql = new StringBuilder();
			hql.append(getSelectClause());
			hql.append(getFromClause());
			hql.append(getWhereClause());
			hql.append(getGroupByClause());
			hql.append(getSortByClause());
			return hql.toString();
		}
		
		public Map<Integer, Integer> getHQLColumnMap() {
			return columnMap;
		}
		
		private String getSelectClause() {
			StringBuilder _hql = new StringBuilder();
			List<String> selectFields = new ArrayList<String>();
			int columnIndex = 0;
			
			// normal query
			if(!inverseUserSelection) {
				// site
				if(siteId != null || totalsBy.contains(T_SITE)) {
					selectFields.add("s.siteId as site");
					columnMap.put(C_SITE, columnIndex++);
				}
				// user
				if(totalsBy.contains(T_USER)) {
					if(queryType == Q_TYPE_EVENT && anonymousEvents != null && anonymousEvents.size() > 0) {
						selectFields.add("(CASE WHEN s.eventId not in (:anonymousEvents) THEN s.userId ELSE '-' END) as user");						
					}else{
						selectFields.add("s.userId as user");
					}
					columnMap.put(C_USER, columnIndex++);
				}
				// event
				if(totalsBy.contains(T_EVENT)) {
					selectFields.add("s.eventId as event");
					columnMap.put(C_EVENT, columnIndex++);
				}
				// tool
				if(totalsBy.contains(T_TOOL)) {
					selectFields.add("s.eventId as event");
					columnMap.put(C_TOOL, columnIndex++);
				}
				// resource
				if(totalsBy.contains(T_RESOURCE)) {
					selectFields.add("s.resourceRef as resourceRef");
					columnMap.put(C_RESOURCE, columnIndex++);
				}
				// resource action
				if(totalsBy.contains(T_RESOURCE_ACTION)) {
					selectFields.add("s.resourceAction as resourceAction");
					columnMap.put(C_RESOURCE_ACTION, columnIndex++);
				}
				// page
				if(totalsBy.contains(T_PAGE)) {
					selectFields.add("s.pageRef as pageRef");
					columnMap.put(C_PAGE, columnIndex++);
				}
				// lesson page id
				if (queryType == Q_TYPE_LESSON) {
					selectFields.add("s.pageId as pageId");
					columnMap.put(C_PAGE_ID, columnIndex++);
				}
				// page action
				if(totalsBy.contains(T_PAGE_ACTION)) {
					selectFields.add("s.pageAction as pageAction");
					columnMap.put(C_PAGE_ACTION, columnIndex++);
				}
				// date
				if(totalsBy.contains(T_DATE)) {
					selectFields.add("s.date as date");
					columnMap.put(C_DATE, columnIndex++);
				}else if(totalsBy.contains(T_LASTDATE)) {
					selectFields.add("max(s.date) as date");
					columnMap.put(C_DATE, columnIndex++);
				}else if(totalsBy.contains(T_DATEMONTH)) {
					if(dbVendor.equals("oracle")) {
						selectFields.add("to_char(s.date,'YYYY') as year");
						selectFields.add("to_char(s.date,'MM') as month");
					}else{
						selectFields.add("year(s.date) as year");
						selectFields.add("month(s.date) as month");
					}
					columnMap.put(C_DATEYEAR, columnIndex++);
					columnMap.put(C_DATEMONTH, columnIndex++);
				}else if(totalsBy.contains(T_DATEYEAR)) {
					if(dbVendor.equals("oracle")) {
						selectFields.add("to_char(s.date,'YYYY') as year");
					}else{
						selectFields.add("year(s.date) as year");
					}
					columnMap.put(C_DATEYEAR, columnIndex++);
				}
				// total
				if((queryType == Q_TYPE_EVENT && !totalsBy.contains(T_VISITS) && !totalsBy.contains(T_UNIQUEVISITS))
					|| queryType == Q_TYPE_RESOURCE || queryType == Q_TYPE_LESSON) {
					selectFields.add("sum(s.count) as total");
					columnMap.put(C_TOTAL, columnIndex++);
				}else if(queryType == Q_TYPE_ACTIVITYTOTALS) {
					selectFields.add("sum(s.count) as total");
					columnMap.put(C_TOTAL, columnIndex++);
				}else if(queryType == Q_TYPE_PRESENCE) {
					selectFields.add("sum(s.duration) as duration");
					columnMap.put(C_DURATION, columnIndex++);
				}else {
					if(queryType == Q_TYPE_EVENT
						|| totalsBy.contains(T_DATEMONTH) || totalsBy.contains(T_DATEYEAR)) {
						// unique visits by month or year must come from SST_EVENTS instead!
						selectFields.add("sum(s.count) as totalVisits");
						columnMap.put(C_VISITS, columnIndex++);
						selectFields.add("count(distinct s.userId) as totalUnique");
						columnMap.put(C_UNIQUEVISITS, columnIndex++);
					}else{
						selectFields.add("sum(s.totalVisits) as totalVisits");
						columnMap.put(C_VISITS, columnIndex++);
						selectFields.add("sum(s.totalUnique) as totalUnique");
						columnMap.put(C_UNIQUEVISITS, columnIndex++);
					}
				}
				
			// inverse query (users not matching conditions)
			}else{
				if(queryType == Q_TYPE_EVENT && anonymousEvents != null && anonymousEvents.size() > 0) {
					selectFields.add("distinct(case when s.eventId not in (:anonymousEvents) then s.userId else '-' end) as user");
				}else{
					selectFields.add("distinct s.userId as user");
				}
				columnMap.put(C_USER, columnIndex++);
			}
			
			// build 'select' clause
			_hql.append("select ");
			for(int i=0; i<selectFields.size() - 1; i++) {
				_hql.append(selectFields.get(i));
				_hql.append(", ");
			}
			_hql.append(selectFields.get(selectFields.size() - 1));
			_hql.append(' ');
			
			return _hql.toString();
		}
		
		private String getFromClause() {
			if(queryType == Q_TYPE_EVENT) {
				return "from EventStatImpl as s ";
			}else if(queryType == Q_TYPE_RESOURCE){
				return "from ResourceStatImpl as s ";
			}else if(queryType == Q_TYPE_VISITSTOTALS){
				if(totalsBy.contains(T_DATEMONTH) || totalsBy.contains(T_DATEYEAR)) {
					// unique visits by month or year must come from SST_EVENTS instead!
					return "from EventStatImpl as s ";
				}else{
					return "from SiteVisitsImpl as s ";
				}
			}else if(queryType == Q_TYPE_PRESENCE){
				return "from SitePresenceImpl as s ";
			}else if(queryType == Q_TYPE_LESSON){
				return "from LessonBuilderStatImpl as s ";
			}else{
				//if(queryType == Q_TYPE_ACTIVITYTOTALS){
				return "from SiteActivityImpl as s ";
			}
		}
		
		private String getWhereClause() {
			StringBuilder _hql = new StringBuilder();
			List<String> whereFields = new ArrayList<String>();
			
			if(siteId != null) {
				whereFields.add("s.siteId = :siteid");
			}
			if((queryType == Q_TYPE_EVENT || queryType == Q_TYPE_ACTIVITYTOTALS)
				&& events != null /*&& !events.isEmpty()*/) {
				whereFields.add("s.eventId in (:events)");
			}else if(queryType == Q_TYPE_VISITSTOTALS
					&& (totalsBy.contains(T_DATEMONTH) || totalsBy.contains(T_DATEYEAR))) {
				whereFields.add("s.eventId = '"+SITEVISIT_EVENTID+"'");
			}

			if(queryType == Q_TYPE_RESOURCE && resourceAction != null) {
				whereFields.add("s.resourceAction = :action");
			}
			if(queryType == Q_TYPE_LESSON && resourceAction != null) {
				whereFields.add("s.pageAction = :action");
			}
			if((queryType == Q_TYPE_RESOURCE || queryType == Q_TYPE_LESSON) && resourceIds != null && !resourceIds.isEmpty()) {
				int simpleSelectionCount = 0;
				int wildcardSelectionCount = 0;
				for(String rId : resourceIds) {
					if(rId.endsWith("/")) {
						wildcardSelectionCount++;
					}else{
						simpleSelectionCount++;
					}
				}
                final String refType = (queryType == Q_TYPE_RESOURCE) ? "s.resourceRef" : "s.pageRef";
				if(simpleSelectionCount > 0) {
					whereFields.add(refType + " in (:resources)");
				}
				for(int i=0; i<wildcardSelectionCount; i++) {
					whereFields.add(refType + " like (:resource"+i+")");
				}
			}
			if((queryType == Q_TYPE_EVENT || queryType == Q_TYPE_RESOURCE  || queryType == Q_TYPE_PRESENCE || queryType == Q_TYPE_LESSON) 
				&& userIds != null) {
				if(!userIds.isEmpty()) {
					if(userIds.size() <= 1000) {
						whereFields.add("s.userId in (:users)");
					}else{
						int nUsers = userIds.size();
						int blockId = 0;
						StringBuilder buff = new StringBuilder();
						buff.append("(");
						int blocks = (int) (nUsers / 1000);
						blocks = (blocks*1000 == nUsers) ? blocks : blocks+1;
						for(int i=0; i<blocks-1; i++) {
							buff.append("s.userId in (:users"+blockId+")");
							buff.append(" OR ");
							blockId++;
						}
						buff.append("s.userId in (:users"+blockId+")");
						buff.append(")");
						whereFields.add(buff.toString());
					}
				}else{
					whereFields.add("s.userId=''");
				}
			}
			if(iDate != null) {
				whereFields.add("s.date >= :idate");
			}
			if(fDate != null) {
				whereFields.add("s.date < :fdate");
			}
			if((queryType == Q_TYPE_EVENT || queryType == Q_TYPE_RESOURCE || queryType == Q_TYPE_PRESENCE || queryType == Q_TYPE_LESSON)
				&& !showAnonymousAccessEvents) {
				whereFields.add("s.userId != '?'");
			}
			
			// build 'where' clause
			if (whereFields.size() > 0) {
				_hql.append("where ");
				for (int i=0; i<whereFields.size() - 1; i++) {
					String previousField = (i != 0) ? whereFields.get(i-1) : null;
					String currentField = whereFields.get(i);
					String nextField = whereFields.get(i+1);
					if (currentField.startsWith("s.resourceRef")
					    || currentField.startsWith("s.pageRef")) {
						// this is a resource condition
						if (i!= 0 && !previousField.startsWith("s.resourceRef") && !previousField.startsWith("s.pageRef")) {
							_hql.append("(");
						}
						_hql.append(currentField);
						if (nextField.startsWith("s.resourceRef")
						    || nextField.startsWith("s.pageRef")) {
							 // and so is next
							_hql.append(" or ");
						} else{
							// and next is not
							_hql.append(") and ");
						}
					} else {
						_hql.append(currentField);
						_hql.append(" and ");
					}
				}
				_hql.append(whereFields.get(whereFields.size() - 1));
				if(whereFields.size() > 1) {
					String lastField = whereFields.get(whereFields.size() - 2);
					if (lastField.startsWith("s.resourceRef")
						|| lastField.startsWith("s.pageeRef")) {
						// last was also a resource condition
					    _hql.append(')');
				    }
				}
			}			
			_hql.append(' ');			
			
			return _hql.toString();
		}
		
		private String getGroupByClause() {
			StringBuilder _hql = new StringBuilder();
			List<String> groupFields = new ArrayList<String>();

			if(!inverseUserSelection && (siteId != null || totalsBy.contains(T_SITE))) {
				groupFields.add("s.siteId");
			}
			// User: new approach		
			if(totalsBy.contains(T_USER)) {
				if(queryType == Q_TYPE_EVENT && anonymousEvents != null && anonymousEvents.size() > 0) {
					if(dbVendor.equals("oracle")) {
						// unfortunately, this produces results different from the expected:
						//	- hibernate-oracle bug (sometimes) producing duplicate lines
						//	- hack fix in getEventStats() method
						groupFields.add("s.eventId");
						groupFields.add("s.userId");
						// it should be: ( but doesn't work in Hibernate :( )
						//groupFields.add("(CASE WHEN s.eventId not in (:anonymousEvents) THEN s.userId ELSE '-' END)");
					} else {
						groupFields.add("s.userId");
					}
				} else {
					groupFields.add("s.userId");
				}
			}			
			if((queryType == Q_TYPE_EVENT || queryType == Q_TYPE_ACTIVITYTOTALS)
					&& (
						totalsBy.contains(T_EVENT) 
						|| totalsBy.contains(T_TOOL)
						)
				) {
				groupFields.add("s.eventId");
			}
			if(queryType == Q_TYPE_RESOURCE && totalsBy.contains(T_RESOURCE)) {
				groupFields.add("s.resourceRef");
			}
			if((queryType == Q_TYPE_RESOURCE || queryType == Q_TYPE_LESSON) && totalsBy.contains(T_RESOURCE_ACTION)) {
				groupFields.add("s.resourceAction");
			}
			if(queryType == Q_TYPE_LESSON && totalsBy.contains(T_PAGE)) {
				groupFields.add("s.pageRef");
			}
			if(queryType == Q_TYPE_LESSON && totalsBy.contains(T_PAGE_ACTION)) {
				groupFields.add("s.pageAction");
			}
			if(queryType == Q_TYPE_LESSON) {
				groupFields.add("s.pageId");
			}
			if(totalsBy.contains(T_DATE)) {
				groupFields.add("s.date");
			}
			if(totalsBy.contains(T_LASTDATE) && groupFields.size() == 0) {
				groupFields.add("s.date");
			}
			if(totalsBy.contains(T_DATEMONTH)) {
				if(dbVendor.equals("oracle")) {
					groupFields.add("to_char(s.date,'YYYY')");
					groupFields.add("to_char(s.date,'MM')");
				}else{
					groupFields.add("year(s.date)");
					groupFields.add("month(s.date)");
				}
			}
			if(totalsBy.contains(T_DATEYEAR)) {
				if(dbVendor.equals("oracle")) {
					groupFields.add("to_char(s.date,'YYYY')");
				}else{
					groupFields.add("year(s.date)");
				}
			}
			
			// build 'group by' clause
			if(groupFields.size() > 0) {
				_hql.append("group by ");
				for(int i=0; i<groupFields.size() - 1; i++) {
					_hql.append(groupFields.get(i));
					_hql.append(", ");
				}
				_hql.append(groupFields.get(groupFields.size() - 1));
				_hql.append(' ');
			}
			
			return _hql.toString();
		}
		
		private String getSortByClause() {
			if(sortBy != null){
				StringBuilder _hql = new StringBuilder();
				String sortField = null;
	
				if(sortBy.equals(T_SITE) && totalsBy.contains(T_SITE)) {
					sortField = "s.siteId";
				}
				if(sortBy.equals(T_USER) && totalsBy.contains(T_USER)) {
					sortField = "s.userId";
				}
				if((queryType == Q_TYPE_EVENT || queryType == Q_TYPE_ACTIVITYTOTALS) 
					&& (sortBy.equals(T_EVENT) || sortBy.equals(T_TOOL)) && (totalsBy.contains(T_EVENT) || totalsBy.contains(T_TOOL))) {
					sortField = "s.eventId";
				}
				if(queryType == Q_TYPE_RESOURCE && sortBy.equals(T_RESOURCE) && totalsBy.contains(T_RESOURCE)) {
					sortField = "s.resourceRef";
				}
				if(queryType == Q_TYPE_RESOURCE && sortBy.equals(T_RESOURCE_ACTION) && totalsBy.contains(T_RESOURCE_ACTION)) {
					sortField = "s.resourceAction";
				}
				if(queryType == Q_TYPE_LESSON && sortBy.equals(T_PAGE) && totalsBy.contains(T_PAGE)) {
					sortField = "s.pageRef";
				}
				if(queryType == Q_TYPE_LESSON && sortBy.equals(T_PAGE_ACTION) && totalsBy.contains(T_PAGE_ACTION)) {
					sortField = "s.pageAction";
				}
				if((sortBy.equals(T_DATE) || sortBy.equals(T_LASTDATE)) 
						&& 
						(totalsBy.contains(T_DATE) || totalsBy.contains(T_LASTDATE) )) {
					sortField = "s.date";
				}
				if(sortBy.equals(T_DURATION)) {
					sortField = "sum(s.duration)";
				}
				if(sortBy.equals(T_TOTAL)) {
					sortField = "sum(s.count)";
				}
				if(sortBy.equals(T_VISITS)) {
					if (queryType == Q_TYPE_EVENT || totalsBy.contains(T_DATEMONTH) || totalsBy.contains(T_DATEYEAR)) {
						sortField = "sum(s.count)";
					} else {
						sortField = "sum(s.totalVisits)";
					}
				}
				if(sortBy.equals(T_UNIQUEVISITS)) {
					if (queryType == Q_TYPE_EVENT || totalsBy.contains(T_DATEMONTH) || totalsBy.contains(T_DATEYEAR)) {
						sortField = "count(distinct s.userId)";
					} else {
						sortField = "sum(s.totalUnique)";
					}
				}
			
				// build 'sort by' clause
				if(sortField != null) {
					_hql.append("order by ");
					_hql.append(sortField);
					_hql.append(' ');
					_hql.append(sortAscending ? "ASC" : "DESC");
					_hql.append(' ');
				}
				return _hql.toString();
			}
			return "";
		}
	}
	

	
	// ################################################################
	//	Site visits related methods
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
			HibernateCallback<List<SiteVisits>> hcb = session -> {
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
            };
			return getHibernateTemplate().execute(hcb);
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
			String usersStr = "";
			if(getDbVendor().equals("oracle")){
				if(iDate != null)
					iDateStr = "and es.EVENT_DATE >= :idate ";
				if(fDate != null)
					fDateStr = "and es.EVENT_DATE < :fdate ";
				if(!showAnonymousAccessEvents)
					usersStr = "and es.USER_ID != '?' ";
			}else{
				if(iDate != null)
					iDateStr = "and es.date >= :idate ";
				if(fDate != null)
					fDateStr = "and es.date < :fdate ";
				if(!showAnonymousAccessEvents)
					usersStr = "and es.userId != '?' ";
			}
			final String hql = "select es.siteId, sum(es.count) ,count(distinct es.userId), year(es.date), month(es.date)"+
				"from EventStatImpl as es " +
				"where es.siteId = :siteid " +
				iDateStr + fDateStr +
				usersStr +
				"  and es.eventId = '"+SITEVISIT_EVENTID+"' " +
				"group by es.siteId, year(es.date), month(es.date)";
			final String oracleSql = "select es.SITE_ID as actSiteId, sum(es.EVENT_COUNT) as actVisits, count(distinct es.USER_ID) as actUnique, "+
				"  to_char(es.EVENT_DATE,'YYYY') as actYear, to_char(es.EVENT_DATE,'MM') as actMonth "+
				"from SST_EVENTS es " +
				"where es.SITE_ID = :siteid " +
				iDateStr + fDateStr +
				usersStr +
				"  and es.EVENT_ID = '"+SITEVISIT_EVENTID+"' " + 
				"group by es.SITE_ID,to_char(es.EVENT_DATE,'YYYY'), to_char(es.EVENT_DATE,'MM')";
			
			HibernateCallback<List<SiteVisits>> hcb = session -> {
                Query q = null;
                if(getDbVendor().equals("oracle")){
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
                        if(getDbVendor().equals("oracle")){
                            c.setSiteId((String)s[0]);
                            c.setTotalVisits(((BigDecimal)s[1]).longValue());
                            c.setTotalUnique(((BigDecimal)s[2]).longValue());
                            cal.set(Calendar.YEAR, Integer.parseInt((String)s[3]));
                            cal.set(Calendar.MONTH, Integer.parseInt((String)s[4]) - 1);
                        }else{
                            c.setSiteId((String)s[0]);
                            c.setTotalVisits(((Long)s[1]).longValue());
                            try{
                                c.setTotalUnique(((Integer)s[2]).intValue());
                            }catch(ClassCastException e) {
                                c.setTotalUnique(((Long)s[2]).intValue());
                            }
                            cal.set(Calendar.YEAR, ((Integer)s[3]).intValue());
                            cal.set(Calendar.MONTH, ((Integer)s[4]).intValue() - 1);
                        }
                        c.setDate(cal.getTime());
                        results.add(c);
                    }
                    return results;
                }
                else return results;
            };
			return getHibernateTemplate().execute(hcb);
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
			
			HibernateCallback<Long> hcb = session -> {
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
                List<Long> res = q.list();
                if(res.size() > 0) return res.get(0);
                else return 0L;
            };
			return getHibernateTemplate().execute(hcb);
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getTotalSiteVisits(java.lang.String)
	 */
	public long getTotalSiteVisitsForUser(String siteId, String userId) {
		if(siteId == null || userId == null){
			throw new IllegalArgumentException("Null siteId or userId");
		}else{
			final String hql = "select sum(es.count) " +
					"from EventStatImpl as es " +
					"where es.siteId = :siteid " +
					"and es.userId = :userid " +
					"and es.eventId = 'pres.begin' ";

			HibernateCallback<Long> hcb = session -> {
				Query q = session.createQuery(hql);
				q.setString("siteid", siteId);
				q.setString("userid", userId);
				List<Long> res = q.list();
				if(res.size() > 0 && res.get(0) != null) return res.get(0);
				else return 0L;
			};
			try{
				return getHibernateTemplate().execute(hcb);
			}catch(ClassCastException e) {
				log.error("Cannot get total site visits for user: {} on site: {}", userId, siteId);
				return 0l;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getTotalSiteUniqueVisits(java.lang.String, boolean)
	 */
	public long getTotalSiteUniqueVisits(final String siteId) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			String usersStr = "";
			if(!showAnonymousAccessEvents)
				usersStr = "and es.userId != '?' ";
			final String hql = "select count(distinct es.userId) " +
					"from EventStatImpl as es " +
					"where es.siteId = :siteid " +
					"and es.eventId = 'pres.begin' " +
					usersStr;
			
			HibernateCallback<Long> hcb = session -> {
                Query q = session.createQuery(hql);
                q.setString("siteid", siteId);
                List<Long> res = q.list();
                if(res.size() > 0) return res.get(0);
                else return 0L;
            };
			try{
				return getHibernateTemplate().execute(hcb);
			}catch(ClassCastException e) {
				return getHibernateTemplate().execute(hcb);
			}
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
			String usersStr = "";
			if(iDate != null)
				iDateStr = "and es.date >= :idate ";
			if(fDate != null)
				fDateStr = "and es.date < :fdate ";
			if(!showAnonymousAccessEvents)
				usersStr = "and es.userId != '?' ";
			final String hql = "select count(distinct es.userId) " +
					"from EventStatImpl as es " +
					"where es.siteId = :siteid " +
					"and es.eventId = 'pres.begin'" +
					usersStr +
					iDateStr + fDateStr;
			
			HibernateCallback<Long> hcb = session -> {
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
                List<Long> res = q.list();
                if(res.size() > 0) return res.get(0);
                else return 0L;
            };
			try{
				return getHibernateTemplate().execute(hcb);
			}catch(ClassCastException e) {
				return getHibernateTemplate().execute(hcb);
			}
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
				return siteService.getSite(siteId).getMembers().size();
			}catch(IdUnusedException e){
				log.warn("Unable to get total site users for site id: "+siteId, e);
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
			HibernateCallback<List<SiteActivity>> hcb = session -> {
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
            };
			return getHibernateTemplate().execute(hcb);
		}
	}
	
	public List<SiteActivity> getSiteActivityByDay(final String siteId, final List<String> events, final Date iDate, final Date fDate) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			String iDateStr = "";
			String fDateStr = "";
			if(getDbVendor().equals("oracle")){
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
			final String oracleSql = 
					"select s.SITE_ID as actSiteId, sum(s.ACTIVITY_COUNT) as actCount, to_char(s.ACTIVITY_DATE,'YYYY') as actYear, to_char(s.ACTIVITY_DATE,'MM') as actMonth, to_char(s.ACTIVITY_DATE,'DD') as actDay " + 
					"from SST_SITEACTIVITY s " +
					"where s.SITE_ID = :siteid " +
					"and s.EVENT_ID in (:eventlist) " +
					iDateStr + fDateStr +
					"group by s.SITE_ID, to_char(s.ACTIVITY_DATE,'YYYY'), to_char(s.ACTIVITY_DATE,'MM'), to_char(s.ACTIVITY_DATE,'DD')";
			
			HibernateCallback<List<SiteActivity>> hcb = session -> {
                Query q = null;
                if(getDbVendor().equals("oracle")){
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
                    q.setParameterList("eventlist", eventRegistryService.getEventIds());
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
                        if(getDbVendor().equals("oracle")){
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
            };
			return getHibernateTemplate().execute(hcb);
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
			if(getDbVendor().equals("oracle")){
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
			
			HibernateCallback<List<SiteActivity>> hcb = session -> {
                Query q = null;
                if(getDbVendor().equals("oracle")){
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
                    q.setParameterList("eventlist", eventRegistryService.getEventIds());
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
                        if(getDbVendor().equals("oracle")){
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
            };
			return getHibernateTemplate().execute(hcb);
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
			if(getDbVendor().equals("oracle")){
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
			final String oracleSql = 
					"select s.SITE_ID as actSiteId, sum(s.ACTIVITY_COUNT) as actCount, s.EVENT_ID as actEventId " + 
					"from SST_SITEACTIVITY s " +
					"where s.SITE_ID = :siteid " +
					"and s.EVENT_ID in (:eventlist) " +
					iDateStr + fDateStr +
					"group by s.SITE_ID, s.EVENT_ID";
			
			HibernateCallback<List<SiteActivityByTool>> hcb = session -> {
                Query q = null;
                if(getDbVendor().equals("oracle")){
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
                    q.setParameterList("eventlist", eventRegistryService.getEventIds());
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
                    Map<String,ToolInfo> eventIdToolMap = eventRegistryService.getEventIdToolMap();
                    Map<String,SiteActivityByTool> toolidSABT = new HashMap<String, SiteActivityByTool>();
                    List<ToolInfo> allTools = eventRegistryService.getEventRegistry();
                    for(Iterator<Object[]> iter = records.iterator(); iter.hasNext();) {
                        Object[] s = iter.next();
                        SiteActivityByTool c = new SiteActivityByToolImpl();
                        if(getDbVendor().equals("oracle")){
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
                                int ix = allTools.indexOf(new ToolInfo(toolId));
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
            };
			return getHibernateTemplate().execute(hcb);
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
			
			HibernateCallback<List<SiteActivity>> hcb = session -> {
                Query q = session.createQuery(hql);
                //q.setFlushMode(FlushMode.MANUAL);
                q.setString("siteid", siteId);
                if(events != null && events.size() > 0)
                    q.setParameterList("eventlist", events);
                else
                    q.setParameterList("eventlist", eventRegistryService.getEventIds());
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
            };
			return getHibernateTemplate().execute(hcb);
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
			
			HibernateCallback<Long> hcb = session -> {
                Query q = session.createQuery(hql);
                q.setString("siteid", siteId);
                if(events != null && events.size() > 0)
                    q.setParameterList("eventlist", events);
                else
                    q.setParameterList("eventlist", eventRegistryService.getEventIds());
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
                List<Long> res = q.list();
                if(res.size() > 0) return res.get(0);
                else return 0L;
            };
			return getHibernateTemplate().execute(hcb);
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
			date = new Date(siteService.getSite(siteId).getCreatedTime().getTime());
		}catch(Exception e){
			return new Date(0);
		}
		return date;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#logEvent(java.lang.Object, java.lang.String)
	 */
	public void logEvent(Object object, String logAction) {
		logEvent(object, logAction, toolManager.getCurrentPlacement().getContext(), false);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#logEvent(java.lang.Object, java.lang.String, java.lang.String, boolean)
	 */
	public void logEvent(Object object, String logAction, String siteId, boolean oncePerSession) {
		boolean log = true;
		
		// event: common
		StringBuilder event = new StringBuilder();
		event.append(LOG_APP);
		
		// ref: common
		StringBuilder ref = new StringBuilder();
		ref.append("/site/");
		ref.append(siteId);
		
		// event, ref: object specific
		if(object != null) {
			if(object instanceof PrefsData) {
				event.append('.');
				event.append(LOG_OBJ_PREFSDATA);
				ref.append('/');
				ref.append(LOG_OBJ_PREFSDATA);
			}else if(object instanceof ReportDef) {
				event.append('.');
				event.append(LOG_OBJ_REPORTDEF);
				ref.append('/');
				ref.append(LOG_OBJ_REPORTDEF);
				ref.append('/');
				ref.append(((ReportDef) object).getId());
			}else if(object instanceof UserId) {
				ref.append('/')
					.append(LOG_OBJ_USER)
					.append('/')
					.append(((UserId) object).uuid);
			}else if(object instanceof String) {
				String str = ((String) object).toLowerCase();
				event.append('.');
				event.append(str);
				ref.append('/');
				ref.append(str);
			}else if(object instanceof Class<?>) {
				String className = ((Class<?>) object).getSimpleName().toLowerCase();
				event.append('.');
				event.append(className);
				ref.append('/');
				ref.append(className);
			}else{
				String className = object.getClass().getSimpleName().toLowerCase();
				event.append('.');
				event.append(className);
				ref.append('/');
				ref.append(className);
				ref.append('/');
				try{
					Object id = object.getClass().getMethod("getId", (Class[]) null).invoke(object, (Object[]) null);
					ref.append(id);
				}catch(Exception e) {
					ref.append(object);
				}
			}
		}
		event.append('.');
		event.append(logAction);
		
		// if only once per session, check if already logged
		if(oncePerSession) {
			String sessionValue = (String) sessionManager.getCurrentSession().getAttribute(event.toString() + ref.toString());
			log = sessionValue == null || sessionValue.equals("");
		}
		// log...
		if(log) {
			boolean modify = LOG_ACTION_NEW.equals(logAction) || LOG_ACTION_EDIT.equals(logAction) || LOG_ACTION_DELETE.equals(logAction);
			Event e = null;
			try{
				// Sakai >= 2.6
				// Invoke: newEvent(String event, String resource, String context, boolean modify, int priority)
				Method m = eventTrackingService.getClass().getMethod("newEvent", new Class[]{String.class, String.class, String.class, boolean.class, int.class});
				e = (Event) m.invoke(eventTrackingService, new Object[] { event.toString(), ref.toString(), siteId, modify, NotificationService.NOTI_OPTIONAL });
			}catch(Exception ex) {
				// Sakai < 2.6
				// Invoke: newEvent(String event, String resource, boolean modify)
				e = eventTrackingService.newEvent(event.toString(), ref.toString(), modify);
			}
			eventTrackingService.post(e);
			if(oncePerSession) {
				sessionManager.getCurrentSession().setAttribute(event.toString() + ref.toString(), "true");
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getLocalSakaiName()
	 */
	@Override
	public String getLocalSakaiName() {
		return serverConfigurationService.getString("ui.service", "Sakai");
	}

	private void checkForEventContextSupport() {
		try{
			Event.class.getMethod("getContext", null);
			eventContextSupported = true;
			logger.info("init(): - Event.getContext() method IS supported.");
		}catch(SecurityException e){
			eventContextSupported = false;
			logger.warn("init(): - security exception while checking for Event.getContext() method.", e);
		}catch(NoSuchMethodException e){
			eventContextSupported = false;
			logger.info("init(): - Event.getContext() method is NOT supported.");
		}catch(Exception e){
			eventContextSupported = false;
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
					User u = userService.getUser(userId);
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

		HibernateCallback<List<String>> hcb = session -> {
            Query q = session.createQuery(hql);
            q.setString("siteid", siteId);
            return q.list();
        };
		return getHibernateTemplate().execute(hcb);
	}
	
	private String getDbVendor() {
		String dialectStr = null;
		if(serverConfigurationService.getString("sitestats.db", "internal").equals("internal")) {
			dialectStr = serverConfigurationService.getString("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
		}else{
			dialectStr = serverConfigurationService.getString("sitestats.externalDb.hibernate.dialect","org.hibernate.dialect.HSQLDialect");
		}
		if(dialectStr.toLowerCase().contains("mysql")) {
			return "mysql";
		}else if(dialectStr.toLowerCase().contains("oracle")) {
			return "oracle";
		}else{
			return "hsql";
		}
	}
}
