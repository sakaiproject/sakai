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
package org.sakaiproject.sitestats.impl.event;

import java.util.*;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.event.EventRegistry;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.parser.EventFactory;
import org.sakaiproject.sitestats.api.parser.ToolFactory;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.impl.parser.EventFactoryImpl;
import org.sakaiproject.sitestats.impl.parser.ToolFactoryImpl;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class EventRegistryServiceImpl implements EventRegistry, EventRegistryService, Observer {
	/** Static fields */
	private static final String			CACHENAME					= EventRegistryServiceImpl.class.getName();
	private static final String			CACHENAME_EVENTREGISTRY		= "eventRegistry";
	private static ResourceLoader		msgs						= new ResourceLoader("Messages");

	/** Event Registry members */
	private Set<String>					toolEventIds				= null;
	private Set<String>					anonymousToolEventIds		= null;
	private Map<String, ToolInfo>		eventIdToolMap				= null;
	private Map<String, String>			toolIdIconMap				= null;
	private boolean						checkLocalEventNamesFirst	= false;

	/** Event Registries */
	private FileEventRegistry			fileEventRegistry			= null;
	private EntityBrokerEventRegistry	entityBrokerEventRegistry	= null;
	private List<String> 				serverEventIds				= new ArrayList<String>();

	/** Caching */
	private Cache<String, List>						eventRegistryCache			= null;

	/** Sakai services */
	private StatsManager				M_sm;
	private SiteService					M_ss;
	private ToolManager					M_tm;
	private MemoryService				M_ms;
	private ServerConfigurationService	M_scs;

	// ################################################################
	// Spring methods
	// ################################################################
	@Override
	public void setStatsManager(StatsManager m_sm) {
		M_sm = m_sm;
	}
	
	public void setSiteService(SiteService siteService) {
		this.M_ss = siteService;
	}

	public void setToolManager(ToolManager toolManager) {
		this.M_tm = toolManager;
	}

	public void setMemoryService(MemoryService memoryService) {
		this.M_ms = memoryService;
	}

	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.M_scs = serverConfigurationService;
	}

	public void setFileEventRegistry(FileEventRegistry fileEventRegistry) {
		this.fileEventRegistry = fileEventRegistry;
	}

	public void setEntityBrokerEventRegistry(EntityBrokerEventRegistry ebEventRegistry) {
		this.entityBrokerEventRegistry = ebEventRegistry;
		this.entityBrokerEventRegistry.addObserver(this);
	}
	
	public void setCheckLocalEventNamesFirst(boolean checkLocalEventNamesFirst) {
		this.checkLocalEventNamesFirst = checkLocalEventNamesFirst;
	}

	public void init() {
		String willCheckLocalEventNamesFirst = checkLocalEventNamesFirst ? "Local event names in sitestats-bundles will be checked first" : "Tool specified event names (Statisticable interface) will be checked first";
		log.info("init(): " + willCheckLocalEventNamesFirst);
		
		// configure cache
		eventRegistryCache = M_ms.getCache(CACHENAME);		
	}

	// ################################################################
	// Event Registry
	// ################################################################
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.event.EventRegistryService#getEventIds()
	 */
	public Set<String> getEventIds() {
		if(toolEventIds == null){
			toolEventIds = new HashSet<String>();
			toolEventIds.addAll(getEventIdToolMap().keySet());
			// Add on the presence events if we're interested.
			toolEventIds.add(StatsManager.SITEVISIT_EVENTID);
			if(M_sm.isEnableSitePresences()) {
				toolEventIds.add(StatsManager.SITEVISITEND_EVENTID);
			}
			toolEventIds = Collections.unmodifiableSet(toolEventIds);
		}
		return toolEventIds;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.event.EventRegistryService#getAnonymousEventIds()
	 */
	public Set<String> getAnonymousEventIds() {
		if(anonymousToolEventIds == null){
			anonymousToolEventIds = new HashSet<String>();
			for(ToolInfo ti : getEventRegistry()){
				for(EventInfo ei : ti.getEvents()){
					if(ei.isAnonymous()){
						anonymousToolEventIds.add(ei.getEventId());
					}
				}
			}
			anonymousToolEventIds = Collections.unmodifiableSet(anonymousToolEventIds);
		}
		return anonymousToolEventIds;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.event.EventRegistryService#getEventRegistry()
	 */
	public List<ToolInfo> getEventRegistry() {
		return getEventRegistry(null, false);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.event.EventRegistryService#getEventRegistry(java.lang.String, boolean)
	 */
	public List<ToolInfo> getEventRegistry(String siteId, boolean onlyAvailableInSite) {
		if(siteId == null) {
			// return the full event registry
			return getMergedEventRegistry();
		}else if(onlyAvailableInSite) {
			// return the event registry with only tools available in site
			return EventUtil.getIntersectionWithAvailableToolsInSite(M_ss, getMergedEventRegistry(), siteId);
		}else{
			// return the event registry with only tools available in (whole) Sakai
			return EventUtil.getIntersectionWithAvailableToolsInSakaiInstallation(M_tm, getMergedEventRegistry());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.event.EventRegistry#isEventRegistryExpired()
	 */
	public boolean isEventRegistryExpired() {
		// In this specific class, this has no effect
		return true;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.event.EventRegistryService#getEventName(java.lang.String)
	 */
	public String getEventName(String eventId) {
		if(eventId == null || eventId.trim().equals(""))
			return "";	
		String eventName = null;
		EventRegistry firstEr = null;
		EventRegistry secondEr = null;
		if(checkLocalEventNamesFirst) {
			firstEr = fileEventRegistry;
			secondEr = entityBrokerEventRegistry;
		}else{
			firstEr = entityBrokerEventRegistry;
			secondEr = fileEventRegistry;
		}
		eventName = firstEr.getEventName(eventId);
		if(eventName == null) {
			eventName = secondEr.getEventName(eventId);
		}
		if(eventName == null) {
			log.warn("Missing resource bundle for event id: "+eventId);
			eventName = eventId;
		}
		return eventName;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.event.EventRegistryService#getToolName(java.lang.String)
	 */
	public String getToolName(String toolId) {
		if(ReportManager.WHAT_EVENTS_ALLTOOLS.equals(toolId)) {
			return msgs.getString("all");
		}else{
			String toolName;
			try{
				toolName = M_tm.getTool(toolId).getTitle();
			}catch(Exception e){
				try{
					log.debug("No sakai tool found for toolId: " + toolId
							+ " (tool undeployed?). Using bundle (if supplied) in sitestats/sitestats-impl/impl/src/bundle/org/sakaiproject/sitestats/impl/bundle/ for tool name.");
					toolName = msgs.getString(toolId, toolId);
				}catch(Exception e1){
					log.debug("No translation found for toolId: " + toolId
							+ " - using toolId as tool name. Please specify it in sitestats/sitestats-impl/impl/src/bundle/org/sakaiproject/sitestats/impl/bundle/");
					toolName = toolId;
				}
			}
			return toolName;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.event.EventRegistryService#getToolIcon(java.lang.String)
	 */
	public String getToolIcon(String toolId) {
		if(toolIdIconMap == null) {
			toolIdIconMap = new HashMap<String, String>();
			
			// Defaults: standard tools
			toolIdIconMap.put("osp.evaluation", StatsManager.SILK_ICONS_DIR + "thumb_up.png");
			toolIdIconMap.put("osp.glossary", StatsManager.SILK_ICONS_DIR + "text_list_bullets.png");
			toolIdIconMap.put("osp.matrix", StatsManager.SILK_ICONS_DIR + "table.png");
			toolIdIconMap.put("osp.presentation", StatsManager.SILK_ICONS_DIR + "briefcase.png");
			toolIdIconMap.put("osp.presLayout", StatsManager.SILK_ICONS_DIR + "layout_content.png");
			toolIdIconMap.put("osp.presTemplate", StatsManager.SILK_ICONS_DIR + "application_view_tile.png");
			toolIdIconMap.put("osp.style", StatsManager.SILK_ICONS_DIR + "style.png");
			toolIdIconMap.put("osp.wizard", StatsManager.SILK_ICONS_DIR + "wand.png");
			toolIdIconMap.put("sakai.announcements", StatsManager.SILK_ICONS_DIR + "flag_blue.png");
			toolIdIconMap.put("sakai.chat", StatsManager.SILK_ICONS_DIR + "user_comment.png");
			toolIdIconMap.put("sakai.datapoint", StatsManager.SILK_ICONS_DIR + "chart_line.png");
			toolIdIconMap.put("sakai.discussion", StatsManager.SILK_ICONS_DIR + "comments.png");
			toolIdIconMap.put("sakai.dropbox", StatsManager.SILK_ICONS_DIR + "folder_page.png");
			toolIdIconMap.put("sakai.gmt", StatsManager.SILK_ICONS_DIR + "award_star_gold_3.png");
			toolIdIconMap.put("sakai.help", StatsManager.SILK_ICONS_DIR + "help.png");
			toolIdIconMap.put("sakai.iframe", StatsManager.SILK_ICONS_DIR + "page_world.png");
			toolIdIconMap.put("sakai.iframe.site", StatsManager.SILK_ICONS_DIR + "house.png");
			toolIdIconMap.put("sakai.mailbox", StatsManager.SILK_ICONS_DIR + "email.png");
			toolIdIconMap.put("sakai.messages", StatsManager.SILK_ICONS_DIR + "comment.png");
			toolIdIconMap.put("sakai.metaobj", StatsManager.SILK_ICONS_DIR + "application_form.png");
			toolIdIconMap.put("sakai.membership", StatsManager.SILK_ICONS_DIR + "group.png");
			toolIdIconMap.put("sakai.news", StatsManager.SILK_ICONS_DIR + "rss.png");
			toolIdIconMap.put("sakai.podcasts", StatsManager.SILK_ICONS_DIR + "ipod_cast.png");
			toolIdIconMap.put("sakai.postem", StatsManager.SILK_ICONS_DIR + "database_table.png");
			toolIdIconMap.put("sakai.preferences", StatsManager.SILK_ICONS_DIR + "cog.png");
			toolIdIconMap.put("sakai.rutgers.linktool", StatsManager.SILK_ICONS_DIR + "application.png");
			toolIdIconMap.put("sakai.sections", StatsManager.SILK_ICONS_DIR + "group_gear.png");
			toolIdIconMap.put("sakai.singleuser", StatsManager.SILK_ICONS_DIR + "user.png");
			toolIdIconMap.put("sakai.syllabus", StatsManager.SILK_ICONS_DIR + "script.png");
			toolIdIconMap.put("blogger", StatsManager.SILK_ICONS_DIR + "book_edit.png");
			toolIdIconMap.put("sakai.assignment.grades", StatsManager.SILK_ICONS_DIR + "page_edit.png");
			toolIdIconMap.put("sakai.forums", StatsManager.SILK_ICONS_DIR + "comments.png");
			toolIdIconMap.put("sakai.gradebook.tool", StatsManager.SILK_ICONS_DIR + "report.png");
			toolIdIconMap.put("sakai.mailtool", StatsManager.SILK_ICONS_DIR + "email_go.png");
			toolIdIconMap.put("sakai.poll", StatsManager.SILK_ICONS_DIR + "chart_bar.png");
			toolIdIconMap.put("sakai.sitestats", StatsManager.SILK_ICONS_DIR + "chart_bar.png");
			toolIdIconMap.put("sakai.presentation", StatsManager.SILK_ICONS_DIR + "monitor.png");
			toolIdIconMap.put("sakai.profile", StatsManager.SILK_ICONS_DIR + "vcard_edit.png");
			toolIdIconMap.put("sakai.reports", StatsManager.SILK_ICONS_DIR + "report_magnify.png");
			toolIdIconMap.put("sakai.resetpass", StatsManager.SILK_ICONS_DIR + "key.png");
			toolIdIconMap.put("sakai.resources", StatsManager.SILK_ICONS_DIR + "folder.png");
			toolIdIconMap.put("sakai.rwiki", StatsManager.SILK_ICONS_DIR + "page_white_edit.png");
			toolIdIconMap.put("sakai.samigo", StatsManager.SILK_ICONS_DIR + "pencil.png");
			toolIdIconMap.put("sakai.schedule", StatsManager.SILK_ICONS_DIR + "calendar.png");
			toolIdIconMap.put("sakai.search", StatsManager.SILK_ICONS_DIR + "find.png");
			toolIdIconMap.put("sakai.siteinfo", StatsManager.SILK_ICONS_DIR + "application_lightning.png");
			toolIdIconMap.put("sakai.sitesetup", StatsManager.SILK_ICONS_DIR + "application_lightning.png");
			toolIdIconMap.put("sakai.site.roster", StatsManager.SILK_ICONS_DIR + "vcard.png");
			toolIdIconMap.put("sakai.synoptic.messagecenter", StatsManager.SILK_ICONS_DIR + "comment.png");
			toolIdIconMap.put("sakai.conferencing", StatsManager.SILK_ICONS_DIR + "webcam.png");
			toolIdIconMap.put("sakai.feeds", StatsManager.SILK_ICONS_DIR + "rss.png");
			toolIdIconMap.put("sakai.blog", StatsManager.SILK_ICONS_DIR + "book_edit.png");
			toolIdIconMap.put("sakai.blogwow", StatsManager.SILK_ICONS_DIR + "book_edit.png");
			toolIdIconMap.put("sakai.yaft", StatsManager.SILK_ICONS_DIR + "book_edit.png");
			toolIdIconMap.put("sakai.mneme", StatsManager.SITESTATS_WEBAPP + "/images/extra-tool-icons/mneme.png");
			toolIdIconMap.put("sakai.jforum.tool", StatsManager.SITESTATS_WEBAPP + "/images/extra-tool-icons/jforum.png");
			toolIdIconMap.put("sakai.melete", StatsManager.SITESTATS_WEBAPP + "/images/extra-tool-icons/modules.png");
			toolIdIconMap.put("sakai.tasklist", StatsManager.SILK_ICONS_DIR + "note.png");
			toolIdIconMap.put("sakai.todolist", StatsManager.SILK_ICONS_DIR + "note.png");
			toolIdIconMap.put("sakai.markup", StatsManager.SILK_ICONS_DIR + "layout_edit.png");
			toolIdIconMap.put("sakai.bbb", StatsManager.SILK_ICONS_DIR + "webcam.png");
			toolIdIconMap.put("sakai.basiclti", StatsManager.SILK_ICONS_DIR + "application_go.png");
			
			// Defaults: admin tools
			toolIdIconMap.put("sakai.users", StatsManager.SILK_ICONS_DIR + "folder_user.png");
			toolIdIconMap.put("sakai.aliases", StatsManager.SILK_ICONS_DIR + "tag_blue.png");
			toolIdIconMap.put("sakai.sites", StatsManager.SILK_ICONS_DIR + "application_cascade.png");
			toolIdIconMap.put("sakai.realms", StatsManager.SILK_ICONS_DIR + "sitemap_color.png");
			toolIdIconMap.put("sakai.online", StatsManager.SILK_ICONS_DIR + "report_user.png");
			toolIdIconMap.put("sakai.memory", StatsManager.SILK_ICONS_DIR + "server_chart.png");
			toolIdIconMap.put("sakai.archive", StatsManager.SILK_ICONS_DIR + "page_white_compressed.png");
			toolIdIconMap.put("sakai.scheduler", StatsManager.SILK_ICONS_DIR + "clock.png");
			toolIdIconMap.put("sakai.su", StatsManager.SILK_ICONS_DIR + "user_go.png");
			toolIdIconMap.put("sakai.usermembership", StatsManager.SILK_ICONS_DIR + "drive_user.png");
			toolIdIconMap.put("sakai.motd", StatsManager.SILK_ICONS_DIR + "house.png");
			toolIdIconMap.put("sakai-sitebrowser", StatsManager.SILK_ICONS_DIR + "world.png");
			toolIdIconMap.put("sakai-createuser", StatsManager.SILK_ICONS_DIR + "user_add.png");
			
			// User-specified: process additions and overwrites from sakai.properties (STAT-232)
			String[] tools = M_scs.getStrings("sitestats.toolicons.tools");
			String[] icons = M_scs.getStrings("sitestats.toolicons.icons");
			if(tools != null && icons != null) {
				int count = tools.length;
				if(tools.length != icons.length) {
					log.warn("Number of values for property 'sitestats.toolicons.tools' doesn't match number of values in 'sitestats.toolicons.icons'! Using smaller number.");
					if(icons.length < count) {
						count = icons.length;
					}
				}
				for(int i=0; i<count; i++) {
					toolIdIconMap.put(tools[i], icons[i]);
				}
			}else if((tools != null && icons == null) || (tools == null && icons != null)) {
				log.warn("Both 'sitestats.toolicons.tools' and 'sitestats.toolicons.icons' properties are required!");
			}
		}
		return toolIdIconMap.get(toolId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.event.EventRegistryService#getEventIdToolMap()
	 */
	public Map<String, ToolInfo> getEventIdToolMap() {
		if(eventIdToolMap == null){
			eventIdToolMap = new HashMap<String, ToolInfo>();
			Iterator<ToolInfo> i = getMergedEventRegistry().iterator();
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
	 * @see org.sakaiproject.sitestats.api.event.EventRegistryService#getToolFactory()
	 */
	public ToolFactory getToolFactory() {
		return new ToolFactoryImpl();
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.event.EventRegistryService#getEventFactory()
	 */
	public EventFactory getEventFactory() {
		return new EventFactoryImpl();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isRegisteredEvent(String eventId) {
		return getEventIds().contains(eventId);
	}

	// ################################################################
	// Utility Methods
	// ################################################################
	/** Get the merged Event Registry. */
	@SuppressWarnings("unchecked")
	private List<ToolInfo> getMergedEventRegistry() {
		List<ToolInfo> eventRegistry = (List<ToolInfo>) eventRegistryCache.get(CACHENAME_EVENTREGISTRY);
		if (eventRegistry == null) { // not found in the cache
			// First:  use file Event Registry
			eventRegistry = fileEventRegistry.getEventRegistry();
			// Second: add EntityBroker Event Registry,
			//         replacing events for tools found on this Registry
			//         (but keeping the anonymous flag for events in both Registries)
			eventRegistry = EventUtil.addToEventRegistry(entityBrokerEventRegistry.getEventRegistry(), true, eventRegistry);

			// Cache Event Registry
			eventRegistryCache.put(CACHENAME_EVENTREGISTRY, eventRegistry);
			log.debug("Cached EventRegistry.");
		}
		// STAT-380 ensure we do not return a null from this method
		if (eventRegistry == null) {
			eventRegistry = new ArrayList<ToolInfo>(0);
		}
		return eventRegistry;
	}
	
	/** Process event registry expired notifications */
	public void update(Observable obs, Object obj) {
		if(NOTIF_EVENT_REGISTRY_EXPIRED.equals(obj)) {
			eventRegistryCache.remove(CACHENAME_EVENTREGISTRY);
			eventIdToolMap = null;
			toolEventIds = null;
			anonymousToolEventIds = null;
			log.debug("EventRegistry expired. Reloading...");
		}
	}

	
	public List<String> getServerEventIds() {
		return serverEventIds;
	}
	
	public void setServerEventIds(List<String> eventIds) {
		this.serverEventIds=eventIds;
	}

}
