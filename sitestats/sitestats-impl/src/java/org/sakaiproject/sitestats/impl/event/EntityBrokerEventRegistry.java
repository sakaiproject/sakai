package org.sakaiproject.sitestats.impl.event;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.sitestats.api.event.EventRegistry;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.util.ResourceLoader;


public class EntityBrokerEventRegistry implements EventRegistry {
	private Log	LOG	= LogFactory.getLog(EntityBrokerEventRegistry.class);
	
	/** Event Registry members */
	private Map<EventLocaleKey, String>	eventNames;
	
	/** Sakai Services */
	private SessionManager				M_sm;
	private PreferencesService			M_ps;
	

	// ################################################################
	// Spring methods
	// ################################################################
	public void setSessionManager(SessionManager sessionManager) {
		this.M_sm = sessionManager;
	}

	public void setPreferencesService(PreferencesService preferencesService) {
		this.M_ps = preferencesService;
	}
	

	// ################################################################
	// Event Registry methods
	// ################################################################
	// was getAllToolEventsDefinition()
	public List<ToolInfo> getEventRegistry() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getEventName(String eventId) {
		String eventName = null;
		try{
			EventLocaleKey key = new EventLocaleKey(eventId, getCurrentUserLocale().toString());
			eventName = eventNames.get(key);
		}catch(Exception e) {
			eventName = null;
		}
		return eventName;
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
	static class EventLocaleKey {
		String	eventId;
		String	locale;

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
	}

}
