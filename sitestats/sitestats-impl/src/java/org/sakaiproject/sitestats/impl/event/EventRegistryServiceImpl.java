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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;
import org.sakaiproject.authz.api.SecurityService;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.Site;
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
	public static final String			CACHENAME					= EventRegistryServiceImpl.class.getName();
	public static final String			CACHENAME_EVENTREGISTRY		= "eventRegistry";
	private static ResourceLoader		msgs						= new ResourceLoader("Messages");
	private static ResourceLoader		EVENT_MSGS					= new ResourceLoader("Events");

	/** Event Registry members */
	private Set<String>				toolEventIds				= null;
	private volatile Set<String>	anonymousToolEventIds		= null;
	private Map<String, ToolInfo>	eventIdToolMap				= null;
	private Map<String, EventInfo>	eventIdEventMap				= null;
	private Map<String, String>		toolIdIconMap				= null;
	@Setter private boolean			checkLocalEventNamesFirst	= false;

	@Setter private EntityBrokerEventRegistry entityBrokerEventRegistry = null;
	/** Event Registries */
	@Setter private FileEventRegistry fileEventRegistry = null;
	@Getter @Setter private List<String> serverEventIds = new ArrayList<>();

	/** Caching */
	private Cache<String, List<ToolInfo>> eventRegistryCache = null;

	/** Sakai services */
	@Setter private StatsManager				statsManager;
	@Setter private SiteService					siteService;
	@Setter private ToolManager					toolManager;
	@Setter private MemoryService				memoryService;
	@Setter private ServerConfigurationService	serverConfigurationService;
	@Setter private SecurityService				securityService;

	// ################################################################
	// Spring methods
	// ################################################################

	public void init() {
		String willCheckLocalEventNamesFirst = checkLocalEventNamesFirst ? "Local event names in sitestats-bundles will be checked first" : "Tool specified event names (Statisticable interface) will be checked first";
		log.info("init(): " + willCheckLocalEventNamesFirst);
		
		// configure cache
		eventRegistryCache = memoryService.getCache(CACHENAME);
		entityBrokerEventRegistry.addObserver(this);
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
			if(statsManager.getEnableSitePresences()) {
				toolEventIds.add(StatsManager.SITEVISITEND_EVENTID);
			}
			toolEventIds = Collections.unmodifiableSet(toolEventIds);
		}
		return toolEventIds;
	}

	@Override
	public Set<String> getAnonymousEventIds() {
		// because the Set is built in a getter, it is important to consider thread safety
		// implements double check locking to prevent more than one thread from creating the Set
		if (anonymousToolEventIds == null) {
			synchronized (this) {
				if (anonymousToolEventIds == null) {
					Set<String> toolEventIds = getEventRegistry().stream()
							.flatMap(t -> t.getEvents().stream())
							.filter(EventInfo::isAnonymous)
							.map(EventInfo::getEventId)
							.collect(Collectors.toSet());

					anonymousToolEventIds = toolEventIds.isEmpty()
							? Collections.emptySet()
							: Collections.unmodifiableSet(toolEventIds);
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
			return EventUtil.getIntersectionWithAvailableToolsInSite(siteService, getMergedEventRegistry(), siteId);
		}else{
			// return the event registry with only tools available in (whole) Sakai
			return EventUtil.getIntersectionWithAvailableToolsInSakaiInstallation(toolManager, getMergedEventRegistry());
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
	@Override
	public String getToolName(String toolId) {
		if(ReportManager.WHAT_EVENTS_ALLTOOLS.equals(toolId)) {
			return msgs.getString("prefs_useAllTools");
		}else if(ReportManager.WHAT_EVENTS_ALLTOOLS_EXCLUDE_CONTENT_READ.equals(toolId)) {
			return msgs.getFormattedMessage("de_allTools_excludeContentRead", EVENT_MSGS.getString("content.read"));
		}else if(StatsManager.PRESENCE_TOOLID.equals(toolId)) {
			return msgs.getString("overview_title_visits");
		}else{
			String toolName;
			try{
				toolName = toolManager.getTool(toolId).getTitle();
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
	 * @see org.sakaiproject.sitestats.impl.event.EventRegistryService#getEventIdToolMap()
	 */
	@Override
	public Map<String, ToolInfo> getEventIdToolMap() {
		if(eventIdToolMap == null){
			buildEventIdMaps();
		}
		return eventIdToolMap;
	}

	@Override
	public Map<String, EventInfo> getEventIdEventMap() {
		if (eventIdEventMap == null) {
			buildEventIdMaps();
		}

		return eventIdEventMap;
	}

	private void buildEventIdMaps()	{
		eventIdToolMap = new HashMap<>();
		eventIdEventMap = new HashMap<>();
		for (ToolInfo t : getMergedEventRegistry()){
			for (EventInfo e : t.getEvents()){
				eventIdToolMap.put(e.getEventId(), t);
				eventIdEventMap.put(e.getEventId(), e);
			}
		}
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

	@Override
	public boolean isResolvableEvent(String eventId) {
		EventInfo event = getEventIdEventMap().get(eventId);
		return event != null && event.isResolvable();
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
			return new ArrayList<>(0);
		}

		// defensively deep copy the event registry before returning it, this prevents outside code from
		// mutating the cached registry entries
		List<ToolInfo> cloneRegistry = new ArrayList<>(eventRegistry.size());
		for (ToolInfo t : eventRegistry) {
			cloneRegistry.add(t.clone());
		}

		// If not admin, remove stealthed tools from clone
		if (!securityService.isSuperUser()) {
			final Site site = getCurrentSite();
			cloneRegistry.removeIf(tool -> isToolStealthed(site, tool.getToolId()));
		}

		return cloneRegistry;
	}

	/**
	 * Utility function to remove stealthed tool from the list:
	 *      1. If user has stealthed tool(s) in site, show stealthed tool(s) present but not those which aren't
	 *      2. If user has no stealthed tools in site, do not show any stealthed tools
	 * @param currentSite the site currently in context
	 * @param toolID tool registration of the tool in question
	 * @return true if the tool is stealthed and not present in the current site; false otherwise
	 */
	private boolean isToolStealthed(Site currentSite, String toolID) {

		boolean isStealthed = toolManager.isStealthed(toolID);

		// If we have a site and the tool is stealthed, check to see if it's present in the site
		if (currentSite != null && isStealthed) {
			isStealthed = !siteService.isStealthedToolPresent(currentSite, toolID);
		}

		return isStealthed;
	}

	/*
	 * Utility function to get the current Site.
	 */
	private Site getCurrentSite() {
		Site site = null;
		try {
			String siteID = toolManager.getCurrentPlacement().getContext();
			site = siteService.getSite(siteID);
		} catch (Exception ex) {} // ignore

		return site;
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
}
