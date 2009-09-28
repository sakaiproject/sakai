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
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.impl.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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


public class EventRegistryServiceImpl implements EventRegistry, EventRegistryService {
	/** Static fields */
	private static Log					LOG							= LogFactory.getLog(EventRegistryServiceImpl.class);
	private static final String			CACHENAME					= EventRegistryServiceImpl.class.getName();
	private static final String			CACHENAME_EVENTREGISTRY		= "eventRegistry";
	private static ResourceLoader		msgs						= new ResourceLoader("Messages");

	/** Event Registry members */
	private List<String>				toolEventIds				= null;
	private List<String>				anonymousToolEventIds		= null;
	private Map<String, ToolInfo>		eventIdToolMap				= null;
	private Map<String, String>			toolIdIconMap				= null;
	private boolean						checkLocalEventNamesFirst	= false;

	/** Event Registries */
	private FileEventRegistry			fileEventRegistry			= null;
	private EntityBrokerEventRegistry	entityBrokerEventRegistry	= null;

	/** Caching */
	private Cache						eventRegistryCache			= null;

	/** Sakai services */
	private SiteService					M_ss;
	private ToolManager					M_tm;
	private MemoryService				M_ms;

	// ################################################################
	// Spring methods
	// ################################################################
	public void setSiteService(SiteService siteService) {
		this.M_ss = siteService;
	}

	public void setToolManager(ToolManager toolManager) {
		this.M_tm = toolManager;
	}

	public void setMemoryService(MemoryService memoryService) {
		this.M_ms = memoryService;
	}

	public void setFileEventRegistry(FileEventRegistry fileEventRegistry) {
		this.fileEventRegistry = fileEventRegistry;
	}

	public void setEntityBrokerEventRegistry(EntityBrokerEventRegistry ebEventRegistry) {
		this.entityBrokerEventRegistry = ebEventRegistry;
	}
	
	public void setCheckLocalEventNamesFirst(boolean checkLocalEventNamesFirst) {
		this.checkLocalEventNamesFirst = checkLocalEventNamesFirst;
	}

	public void init() {
		String willCheckLocalEventNamesFirst = checkLocalEventNamesFirst ? "Local event names in sitestats-bundles will be checked first" : "Tool specified event names (Statisticable interface) will be checked first";
		LOG.info("init(): " + willCheckLocalEventNamesFirst);
		
		// configure cache
		eventRegistryCache = M_ms.newCache(CACHENAME);		
	}

	// ################################################################
	// Event Registry
	// ################################################################
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.event.EventRegistryService#getEventIds()
	 */
	public List<String> getEventIds() {
		if(toolEventIds == null){
			toolEventIds = new ArrayList<String>();
			Iterator<String> i = getEventIdToolMap().keySet().iterator();
			while(i.hasNext())
				toolEventIds.add(i.next());
		}
		return toolEventIds;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.event.EventRegistryService#getAnonymousEventIds()
	 */
	public List<String> getAnonymousEventIds() {
		if(anonymousToolEventIds == null){
			anonymousToolEventIds = new ArrayList<String>();
			for(ToolInfo ti : getEventRegistry()){
				for(EventInfo ei : ti.getEvents()){
					if(ei.isAnonymous()){
						anonymousToolEventIds.add(ei.getEventId());
					}
				}
			}
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
			LOG.warn("Missing resource bundle for event id: "+eventId);
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
					LOG.debug("No sakai tool found for toolId: " + toolId
							+ " (tool undeployed?). Using bundle (if supplied) in sitestats/sitestats-impl/impl/src/bundle/org/sakaiproject/sitestats/impl/bundle/ for tool name.");
					toolName = msgs.getString(toolId, toolId);
				}catch(Exception e1){
					LOG.debug("No translation found for toolId: " + toolId
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
			toolIdIconMap.put("osp.evaluation", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/thumb_up.png");
			toolIdIconMap.put("osp.glossary", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/text_list_bullets.png");
			toolIdIconMap.put("osp.matrix", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/table.png");
			toolIdIconMap.put("osp.presentation", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/briefcase.png");
			toolIdIconMap.put("osp.presLayout", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/layout_content.png");
			toolIdIconMap.put("osp.presTemplate", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/application_view_tile.png");
			toolIdIconMap.put("osp.style", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/style.png");
			toolIdIconMap.put("osp.wizard", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/wand.png");
			toolIdIconMap.put("sakai.announcements", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/flag_blue.png");
			toolIdIconMap.put("sakai.chat", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/user_comment.png");
			toolIdIconMap.put("sakai.datapoint", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/chart_line.png");
			toolIdIconMap.put("sakai.discussion", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/comments.png");
			toolIdIconMap.put("sakai.dropbox", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/folder_page.png");
			toolIdIconMap.put("sakai.gmt", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/award_star_gold_3.png");
			toolIdIconMap.put("sakai.help", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/help.png");
			toolIdIconMap.put("sakai.iframe", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/page_world.png");
			toolIdIconMap.put("sakai.iframe.site", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/house.png");
			toolIdIconMap.put("sakai.mailbox", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/email.png");
			toolIdIconMap.put("sakai.messages", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/comment.png");
			toolIdIconMap.put("sakai.metaobj", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/application_form.png");
			toolIdIconMap.put("sakai.membership", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/group.png");
			toolIdIconMap.put("sakai.news", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/rss.png");
			toolIdIconMap.put("sakai.podcasts", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/ipod_cast.png");
			toolIdIconMap.put("sakai.postem", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/database_table.png");
			toolIdIconMap.put("sakai.preferences", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/cog.png");
			toolIdIconMap.put("sakai.rutgers.linktool", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/application.png");
			toolIdIconMap.put("sakai.sections", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/group_gear.png");
			toolIdIconMap.put("sakai.singleuser", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/user.png");
			toolIdIconMap.put("sakai.syllabus", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/script.png");
			toolIdIconMap.put("blogger", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/book_edit.png");
			toolIdIconMap.put("sakai.assignment.grades", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/page_edit.png");
			toolIdIconMap.put("sakai.forums", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/comments.png");
			toolIdIconMap.put("sakai.gradebook.tool", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/report.png");
			toolIdIconMap.put("sakai.mailtool", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/email_go.png");
			toolIdIconMap.put("sakai.poll", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/chart_bar.png");
			toolIdIconMap.put("sakai.sitestats", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/chart_bar.png");
			toolIdIconMap.put("sakai.presentation", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/monitor.png");
			toolIdIconMap.put("sakai.profile", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/vcard_edit.png");
			toolIdIconMap.put("sakai.reports", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/report_magnify.png");
			toolIdIconMap.put("sakai.resetpass", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/key.png");
			toolIdIconMap.put("sakai.resources", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/folder.png");
			toolIdIconMap.put("sakai.rwiki", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/page_white_edit.png");
			toolIdIconMap.put("sakai.samigo", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/pencil.png");
			toolIdIconMap.put("sakai.schedule", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/calendar.png");
			toolIdIconMap.put("sakai.search", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/find.png");
			toolIdIconMap.put("sakai.siteinfo", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/application_lightning.png");
			toolIdIconMap.put("sakai.sitesetup", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/application_lightning.png");
			toolIdIconMap.put("sakai.site.roster", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/vcard.png");
			toolIdIconMap.put("sakai.synoptic.messagecenter", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/comment.png");
			toolIdIconMap.put("sakai.conferencing", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/webcam.png");
			toolIdIconMap.put("sakai.feeds", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/rss.png");
			toolIdIconMap.put("sakai.blog", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/book_edit.png");
			toolIdIconMap.put("sakai.blogwow", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/book_edit.png");
			toolIdIconMap.put("sakai.yaft", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/book_edit.png");
			toolIdIconMap.put("sakai.mneme", StatsManager.SITESTATS_WEBAPP + "/images/extra-tool-icons/mneme.png");
			toolIdIconMap.put("sakai.jforum.tool", StatsManager.SITESTATS_WEBAPP + "/images/extra-tool-icons/jforum.png");
			toolIdIconMap.put("sakai.melete", StatsManager.SITESTATS_WEBAPP + "/images/extra-tool-icons/modules.png");
			toolIdIconMap.put("sakai.tasklist", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/note.png");
			toolIdIconMap.put("sakai.todolist", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/note.png");
			toolIdIconMap.put("sakai.markup", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/layout_edit.png");
			// admin tools
			toolIdIconMap.put("sakai.users", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/folder_user.png");
			toolIdIconMap.put("sakai.aliases", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/tag_blue.png");
			toolIdIconMap.put("sakai.sites", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/application_cascade.png");
			toolIdIconMap.put("sakai.realms", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/sitemap_color.png");
			toolIdIconMap.put("sakai.online", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/report_user.png");
			toolIdIconMap.put("sakai.memory", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/server_chart.png");
			toolIdIconMap.put("sakai.archive", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/page_white_compressed.png");
			toolIdIconMap.put("sakai.scheduler", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/clock.png");
			toolIdIconMap.put("sakai.su", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/user_go.png");
			toolIdIconMap.put("sakai.usermembership", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/drive_user.png");
			toolIdIconMap.put("sakai.motd", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/house.png");
			toolIdIconMap.put("sakai-sitebrowser", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/world.png");
			toolIdIconMap.put("sakai-createuser", StatsManager.SITESTATS_WEBAPP + "/images/silk/icons/user_add.png");			
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

	// ################################################################
	// Utility Methods
	// ################################################################
	/** Get the merged Event Registry. */
	private List<ToolInfo> getMergedEventRegistry() {
		if(eventRegistryCache.containsKey(CACHENAME_EVENTREGISTRY)
				&& !areEventRegistriesExpired()) {
			return (List<ToolInfo>) eventRegistryCache.get(CACHENAME_EVENTREGISTRY);
		}else{
			// First:  use file Event Registry
			List<ToolInfo> eventRegistry = fileEventRegistry.getEventRegistry();
			
			// Second: add EntityBroker Event Registry, 
			//         replacing events for tools found on this Registry
			//         (but keeping the anonymous flag for events in both Registries)
			eventRegistry = EventUtil.addToEventRegistry(entityBrokerEventRegistry.getEventRegistry(), true, eventRegistry);
			
			// Cache Event Registry
			eventRegistryCache.put(CACHENAME_EVENTREGISTRY, eventRegistry);
			LOG.debug("Cached EventRegistry.");
			return eventRegistry;
		}
	}
	
	/** Check if any of the entity registries has expired. */
	private boolean areEventRegistriesExpired() {
		return fileEventRegistry.isEventRegistryExpired()
			|| entityBrokerEventRegistry.isEventRegistryExpired();
	}

}
