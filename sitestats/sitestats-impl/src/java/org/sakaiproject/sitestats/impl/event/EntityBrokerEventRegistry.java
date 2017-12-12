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

import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Statisticable;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityProviderListener;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.event.EventRegistry;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class EntityBrokerEventRegistry extends Observable implements EventRegistry, EntityProviderListener<Statisticable> {
	private static final String		CACHENAME				= EntityBrokerEventRegistry.class.getName();

	/** Event Registry members */
	private List<ToolInfo>			eventRegistry			= new ArrayList<ToolInfo>();
	private Map<String, String>		eventIdToEPPrefix		= new HashMap<String, String>();

	/** Caching */
	private Cache<String, String>					eventNamesCache			= null;

	/** Sakai Services */
	private SessionManager			M_sm;
	private PreferencesService		M_ps;
	private EntityProviderManager	M_epm;
	private MemoryService			M_ms;
	

	// ################################################################
	// Spring methods
	// ################################################################
	public void setSessionManager(SessionManager sessionManager) {
		this.M_sm = sessionManager;
	}

	public void setPreferencesService(PreferencesService preferencesService) {
		this.M_ps = preferencesService;
	}

	public void setEntityProviderManager(EntityProviderManager entityProviderManager) {
		this.M_epm = entityProviderManager;
	}

	public void setMemoryService(MemoryService memoryService) {
		this.M_ms = memoryService;
	}
	
	public void init() {
		log.info("init()");
		
		// configure cache
		eventNamesCache = M_ms.getCache(CACHENAME);
		
		// register EntityBrokerListener
		M_epm.registerListener(this, true);
	}
	

	// ################################################################
	// Event Registry methods
	// ################################################################
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.event.EventRegistry#getEventRegistry()
	 */
	public List<ToolInfo> getEventRegistry() {
		log.debug("getEventRegistry(): #tools implementing Statisticable = "+eventRegistry.size());
		return eventRegistry;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.event.EventRegistry#isEventRegistryExpired()
	 */
	public boolean isEventRegistryExpired() {
		return hasChanged();
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.event.EventRegistry#getEventName(java.lang.String)
	 */
	public String getEventName(String eventId) {
		Locale currentUserLocale = getCurrentUserLocale();
		EventLocaleKey key = new EventLocaleKey(eventId, currentUserLocale.toString());
		String keyString = key.toString();
		String eventName = null;
		if(eventNamesCache.containsKey(keyString)) {
			eventName = (String) eventNamesCache.get(keyString);
			if(eventName != null) {
				return eventName;
			}
			/* If there's an exception we won't re-establish the cache entry below */
			eventNamesCache.remove(keyString);
		}
		try{
			String prefix = eventIdToEPPrefix.get(eventId);
			Statisticable s = M_epm.getProviderByPrefixAndCapability(prefix, Statisticable.class);
			Map<String, String> eventIdNamesMap = s.getEventNames(currentUserLocale);
			if(eventIdNamesMap != null) {
				for(String thisEventId : eventIdNamesMap.keySet()) {
					EventLocaleKey thisCacheKey = new EventLocaleKey(thisEventId, currentUserLocale.toString());
					String thisEventName = eventIdNamesMap.get(thisEventId);
					eventNamesCache.put(thisCacheKey.toString(), thisEventName);
					if(thisEventId.equals(eventId)) {
						eventName = thisEventName;
					}
				}
				log.debug("Cached event names for EB prefix '"+prefix+"', locale: "+currentUserLocale);
			}
		}catch(Exception e) {
			eventName = null;
		}
		return eventName;
	}
	

	// ################################################################
	// EntityProviderListener methods
	// ################################################################
	public Class<Statisticable> getCapabilityFilter() {
		return Statisticable.class;
	}

	public String getPrefixFilter() {
		return null;
	}

	public void run(Statisticable provider) {
		log.info("Statisticable capability registered with prefix: " + provider.getEntityPrefix());
		processStatisticableProvider(provider);
	}

	private void processStatisticableProvider(Statisticable provider) {
		String entityPrefix = provider.getEntityPrefix();
		String entityToolId = provider.getAssociatedToolId();
		String[] entityEventIds = provider.getEventKeys();

		// Build tool for Event Registry (List<ToolInfo>)
		ToolInfo tool = new ToolInfo(entityToolId);
		tool.setSelected(true);
		for(String eventId : entityEventIds) {
			EventInfo event = new EventInfo(eventId);
			event.setSelected(true);
			// Add to eventID -> entityProfider_prefix mapping
			eventIdToEPPrefix.put(eventId, entityPrefix);
			tool.addEvent(event);
		}
		eventRegistry.add(tool);

		// Set expired flag on EventRegistry to true
		setChanged();
		notifyObservers(EventRegistryService.NOTIF_EVENT_REGISTRY_EXPIRED);
	}
	

	// ################################################################
	// Utility Methods
	// ################################################################
	/**
	 * Return current user locale.
	 * @return user's Locale object
	 */
	private Locale getCurrentUserLocale() {
		Locale loc = null;
		try{
			// check if locale is requested for specific user
			String userId = M_sm.getCurrentSessionUserId();
			if(userId != null){
				Preferences prefs = M_ps.getPreferences(userId);
				ResourceProperties locProps = prefs.getProperties(ResourceLoader.APPLICATION_ID);
				String localeString = locProps.getProperty(ResourceLoader.LOCALE_KEY);
				// Parse user locale preference if set
				if(localeString != null){
					String[] locValues = localeString.split("_");
					if(locValues.length > 1)
						// language, country
						loc = new Locale(locValues[0], locValues[1]);
					else if(locValues.length == 1)
						// language
						loc = new Locale(locValues[0]);
				}
				if(loc == null)
					loc = Locale.getDefault();
			}else{
				loc = (Locale) M_sm.getCurrentSession().getAttribute(ResourceLoader.LOCALE_KEY + M_sm.getCurrentSessionUserId());
			}
		}catch(NullPointerException e){
			loc = Locale.getDefault();
		}
		return loc;
	}
	
	
	// ################################################################
	// Utility Classes
	// ################################################################
	public static class EventLocaleKey {
		String	eventId = "";
		String	locale = "";

		public EventLocaleKey(String eventId, String locale) {
			this.eventId = eventId;
			this.locale = locale;
		}

		public String getEventId() {
			return eventId;
		}

		public void setEventId(String eventId) {
			this.eventId = eventId;
		}

		public String getLocale() {
			return locale;
		}

		public void setLocale(String locale) {
			this.locale = locale;
		}
		
		@Override
		public String toString() {
			StringBuilder buff = new StringBuilder();
			buff.append("[");
			buff.append(getEventId());
			buff.append(", ");
			buff.append(getLocale());
			buff.append("]");
			return buff.toString();
		}
		
		@Override
		public int hashCode() {
			return getEventId().hashCode() + getLocale().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if(obj == null || !(obj instanceof EventLocaleKey)) {
				return false;
			}
			EventLocaleKey o = (EventLocaleKey) obj;
			if(o.getEventId().equals(getEventId())
					&& o.getLocale().equals(getLocale())) {
				return true;
			}
			return false;
		}
		
		
	}

}
