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
	private static final String			CACHENAME					= "org.sakaiproject.sitestats.api.event.EventRegistryService";
	private static final String			CACHENAME_EVENTREGISTRY		= "eventRegistry";
	private static ResourceLoader		msgs						= new ResourceLoader("Messages");

	/** Event Registry members */
	private List<String>				toolEventIds				= null;
	private List<String>				anonymousToolEventIds		= null;
	private Map<String, ToolInfo>		eventIdToolMap				= null;
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
