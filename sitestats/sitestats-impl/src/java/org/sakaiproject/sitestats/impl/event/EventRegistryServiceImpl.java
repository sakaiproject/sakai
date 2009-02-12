package org.sakaiproject.sitestats.impl.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xalan.transformer.MsgMgr;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.SiteService;
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
			return EventUtil.getIntersectionWithAvailableToolsInSite(getMergedEventRegistry(), siteId);
		}else{
			// return the event registry with only tools available in (whole) Sakai
			return EventUtil.getIntersectionWithAvailableToolsInSakaiInstallation(getMergedEventRegistry());
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
			try{
				return M_tm.getTool(toolId).getTitle();
			}catch(Exception e){
				return toolId;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.event.EventRegistryService#getToolIcon(java.lang.String)
	 */
	public String getToolIcon(String toolId) {
		if(toolIdIconMap == null) {
			toolIdIconMap = new HashMap<String, String>();
			toolIdIconMap.put("osp.evaluation", "/sakai-sitestats-tool/images/silk/icons/thumb_up.png");
			toolIdIconMap.put("osp.glossary", "/sakai-sitestats-tool/images/silk/icons/text_list_bullets.png");
			toolIdIconMap.put("osp.matrix", "/sakai-sitestats-tool/images/silk/icons/table.png");
			toolIdIconMap.put("osp.presentation", "/sakai-sitestats-tool/images/silk/icons/briefcase.png");
			toolIdIconMap.put("osp.presLayout", "/sakai-sitestats-tool/images/silk/icons/layout_content.png");
			toolIdIconMap.put("osp.presTemplate", "/sakai-sitestats-tool/images/silk/icons/application_view_tile.png");
			toolIdIconMap.put("osp.style", "/sakai-sitestats-tool/images/silk/icons/style.png");
			toolIdIconMap.put("osp.wizard", "/sakai-sitestats-tool/images/silk/icons/wand.png");
			toolIdIconMap.put("sakai.announcements", "/sakai-sitestats-tool/images/silk/icons/flag_blue.png");
			toolIdIconMap.put("sakai.chat", "/sakai-sitestats-tool/images/silk/icons/user_comment.png");
			toolIdIconMap.put("sakai.datapoint", "/sakai-sitestats-tool/images/silk/icons/chart_line.png");
			toolIdIconMap.put("sakai.discussion", "/sakai-sitestats-tool/images/silk/icons/comments.png");
			toolIdIconMap.put("sakai.dropbox", "/sakai-sitestats-tool/images/silk/icons/folder_page.png");
			toolIdIconMap.put("sakai.gmt", "/sakai-sitestats-tool/images/silk/icons/award_star_gold_3.png");
			toolIdIconMap.put("sakai.help", "/sakai-sitestats-tool/images/silk/icons/help.png");
			toolIdIconMap.put("sakai.iframe", "/sakai-sitestats-tool/images/silk/icons/page_world.png");
			toolIdIconMap.put("sakai.iframe.site", "/sakai-sitestats-tool/images/silk/icons/house.png");
			toolIdIconMap.put("sakai.mailbox", "/sakai-sitestats-tool/images/silk/icons/email.png");
			toolIdIconMap.put("sakai.messages", "/sakai-sitestats-tool/images/silk/icons/comment.png");
			toolIdIconMap.put("sakai.metaobj", "/sakai-sitestats-tool/images/silk/icons/application_form.png");
			toolIdIconMap.put("sakai.membership", "/sakai-sitestats-tool/images/silk/icons/group.png");
			toolIdIconMap.put("sakai.news", "/sakai-sitestats-tool/images/silk/icons/rss.png");
			toolIdIconMap.put("sakai.podcasts", "/sakai-sitestats-tool/images/silk/icons/ipod_cast.png");
			toolIdIconMap.put("sakai.postem", "/sakai-sitestats-tool/images/silk/icons/database_table.png");
			toolIdIconMap.put("sakai.preferences", "/sakai-sitestats-tool/images/silk/icons/cog.png");
			toolIdIconMap.put("sakai.rutgers.linktool", "/sakai-sitestats-tool/images/silk/icons/application.png");
			toolIdIconMap.put("sakai.sections", "/sakai-sitestats-tool/images/silk/icons/group_gear.png");
			toolIdIconMap.put("sakai.singleuser", "/sakai-sitestats-tool/images/silk/icons/user.png");
			toolIdIconMap.put("sakai.syllabus", "/sakai-sitestats-tool/images/silk/icons/script.png");
			toolIdIconMap.put("blogger", "/sakai-sitestats-tool/images/silk/icons/book_edit.png");
			toolIdIconMap.put("sakai.assignment.grades", "/sakai-sitestats-tool/images/silk/icons/page_edit.png");
			toolIdIconMap.put("sakai.forums", "/sakai-sitestats-tool/images/silk/icons/comments.png");
			toolIdIconMap.put("sakai.gradebook.tool", "/sakai-sitestats-tool/images/silk/icons/report.png");
			toolIdIconMap.put("sakai.mailtool", "/sakai-sitestats-tool/images/silk/icons/email_go.png");
			toolIdIconMap.put("sakai.poll", "/sakai-sitestats-tool/images/silk/icons/chart_bar.png");
			toolIdIconMap.put("sakai.sitestats", "/sakai-sitestats-tool/images/silk/icons/chart_bar.png");
			toolIdIconMap.put("sakai.presentation", "/sakai-sitestats-tool/images/silk/icons/monitor.png");
			toolIdIconMap.put("sakai.profile", "/sakai-sitestats-tool/images/silk/icons/vcard_edit.png");
			toolIdIconMap.put("sakai.reports", "/sakai-sitestats-tool/images/silk/icons/report_magnify.png");
			toolIdIconMap.put("sakai.resetpass", "/sakai-sitestats-tool/images/silk/icons/key.png");
			toolIdIconMap.put("sakai.resources", "/sakai-sitestats-tool/images/silk/icons/folder.png");
			toolIdIconMap.put("sakai.rwiki", "/sakai-sitestats-tool/images/silk/icons/page_white_edit.png");
			toolIdIconMap.put("sakai.samigo", "/sakai-sitestats-tool/images/silk/icons/pencil.png");
			toolIdIconMap.put("sakai.schedule", "/sakai-sitestats-tool/images/silk/icons/calendar.png");
			toolIdIconMap.put("sakai.search", "/sakai-sitestats-tool/images/silk/icons/find.png");
			toolIdIconMap.put("sakai.siteinfo", "/sakai-sitestats-tool/images/silk/icons/application_lightning.png");
			toolIdIconMap.put("sakai.sitesetup", "/sakai-sitestats-tool/images/silk/icons/application_lightning.png");
			toolIdIconMap.put("sakai.site.roster", "/sakai-sitestats-tool/images/silk/icons/vcard.png");
			toolIdIconMap.put("sakai.synoptic.messagecenter", "/sakai-sitestats-tool/images/silk/icons/comment.png");
			toolIdIconMap.put("sakai.conferencing", "/sakai-sitestats-tool/images/silk/icons/webcam.png");
			toolIdIconMap.put("sakai.feeds", "/sakai-sitestats-tool/images/silk/icons/rss.png");
			toolIdIconMap.put("sakai.blog", "/sakai-sitestats-tool/images/silk/icons/book_edit.png");
			toolIdIconMap.put("sakai.blogwow", "/sakai-sitestats-tool/images/silk/icons/book_edit.png");
			toolIdIconMap.put("sakai.yaft", "/sakai-sitestats-tool/images/silk/icons/book_edit.png");
			toolIdIconMap.put("sakai.mneme", "/sakai-sitestats-tool/images/extra-tool-icons/mneme.png");
			toolIdIconMap.put("sakai.jforum.tool", "/sakai-sitestats-tool/images/extra-tool-icons/jforum.png");
			toolIdIconMap.put("sakai.melete", "/sakai-sitestats-tool/images/extra-tool-icons/modules.png");
			toolIdIconMap.put("sakai.tasklist", "/sakai-sitestats-tool/images/silk/icons/note.png");
			toolIdIconMap.put("sakai.todolist", "/sakai-sitestats-tool/images/silk/icons/note.png");
			toolIdIconMap.put("sakai.markup", "/sakai-sitestats-tool/images/silk/icons/layout_edit.png");
			// admin tools
			toolIdIconMap.put("sakai.users", "/sakai-sitestats-tool/images/silk/icons/folder_user.png");
			toolIdIconMap.put("sakai.aliases", "/sakai-sitestats-tool/images/silk/icons/tag_blue.png");
			toolIdIconMap.put("sakai.sites", "/sakai-sitestats-tool/images/silk/icons/application_cascade.png");
			toolIdIconMap.put("sakai.realms", "/sakai-sitestats-tool/images/silk/icons/sitemap_color.png");
			toolIdIconMap.put("sakai.online", "/sakai-sitestats-tool/images/silk/icons/report_user.png");
			toolIdIconMap.put("sakai.memory", "/sakai-sitestats-tool/images/silk/icons/server_chart.png");
			toolIdIconMap.put("sakai.archive", "/sakai-sitestats-tool/images/silk/icons/page_white_compressed.png");
			toolIdIconMap.put("sakai.scheduler", "/sakai-sitestats-tool/images/silk/icons/clock.png");
			toolIdIconMap.put("sakai.su", "/sakai-sitestats-tool/images/silk/icons/user_go.png");
			toolIdIconMap.put("sakai.usermembership", "/sakai-sitestats-tool/images/silk/icons/drive_user.png");
			toolIdIconMap.put("sakai.motd", "/sakai-sitestats-tool/images/silk/icons/house.png");
			toolIdIconMap.put("sakai-sitebrowser", "/sakai-sitestats-tool/images/silk/icons/world.png");
			toolIdIconMap.put("sakai-createuser", "/sakai-sitestats-tool/images/silk/icons/user_add.png");			
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
