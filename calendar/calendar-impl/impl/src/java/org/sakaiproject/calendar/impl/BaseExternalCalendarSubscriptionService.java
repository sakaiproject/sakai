/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.calendar.impl;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.*;
import org.sakaiproject.calendar.api.CalendarEvent.EventAccess;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.*;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.javax.Filter;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.memory.api.SimpleConfiguration;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.sakaiproject.util.FormattedText;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class BaseExternalCalendarSubscriptionService implements
		ExternalCalendarSubscriptionService
{
	/** Logging */
	private static Log m_log = LogFactory.getLog(BaseExternalCalendarSubscriptionService.class);

	/** Schedule tool ID */
	private final static String SCHEDULE_TOOL_ID = "sakai.schedule";

	/** Default context for institutional subscriptions */
	private final static String INSTITUTIONAL_CONTEXT = "!worksite";

	/** Default context for user-provided subscriptions */
	private final static String USER_CONTEXT = "!user";

	/** Default connect timeout when retrieving external subscriptions */
	private final static int TIMEOUT = 30000;

	/** iCal external subscription enable flag */
	private boolean enabled = false;
	
	/** merge iCal external subscriptions from other sites into My Workspace?  */
	private boolean mergeIntoMyworkspace = true;

	/** Column map for iCal processing */
	private Map columnMap = null;

	/** Cache map of Institutional Calendars: <String url, Calendar cal> */
	private SubscriptionCache institutionalSubscriptionCache = null;

	/** Cache map of user Calendars: <String url, Calendar cal> */
	private SubscriptionCache usersSubscriptionCache = null;

	// ######################################################
	// Spring services
	// ######################################################
	/** Dependency: CalendarService. */
	// We depend on the BaseCalendarService so we can call methods outside the calendar service API.
	protected BaseCalendarService m_calendarService = null;

	/** Dependency: SecurityService */
	protected SecurityService m_securityService = null;

	/** Dependency: SessionManager */
	protected SessionManager m_sessionManager = null;
	
	/** Dependency: TimeService */
	protected TimeService m_timeService = null;

	/** Dependency: ToolManager */
	protected ToolManager m_toolManager = null;

	/** Dependency: IdManager. */
	protected IdManager m_idManager;

	/** Dependency: CalendarImporterService. */
	protected CalendarImporterService m_importerService = null;

	/** Dependency: ServerConfigurationService. */
	protected ServerConfigurationService m_configurationService = null;

	/** Dependency: EntityManager. */
	protected EntityManager m_entityManager = null;

	/** Dependency: SiteService. */
	protected SiteService m_siteService = null;

	protected MemoryService m_memoryService = null;

	public void setMemoryService(MemoryService memoryService) {
		this.m_memoryService = memoryService;
	}

	public void setCalendarService(BaseCalendarService service)
	{
		this.m_calendarService = service;
	}

	public void setServerConfigurationService(ServerConfigurationService service)
	{
		this.m_configurationService = service;
	}

	public void setCalendarImporterService(CalendarImporterService service)
	{
		this.m_importerService = service;
	}

	public void setEntityManager(EntityManager service)
	{
		this.m_entityManager = service;
	}

	public void setSiteService(SiteService service)
	{
		this.m_siteService = service;
	}

	/**
	 * Dependency: SecurityService.
	 * 
	 * @param securityService
	 *        The SecurityService.
	 */
	public void setSecurityService(SecurityService securityService)
	{
		m_securityService = securityService;
	}

	/**
	 * Dependency: SessionManager.
	 * @param sessionManager
	 *        The SessionManager.
	 */
	public void setSessionManager(SessionManager sessionManager)
	{
		this.m_sessionManager = sessionManager;
	}

	/**
	 * Dependency: TimeService.
	 * @param timeService
	 *        The TimeService.
	 */
	public void setTimeService(TimeService timeService)
	{
		this.m_timeService = timeService;
	}

	/**
	 * Dependency: ToolManager.
	 * @param toolManager
	 *        The ToolManager.
	 */
	public void setToolManager(ToolManager toolManager)
	{
		this.m_toolManager = toolManager;
	}

	/**
	 * Dependency: IdManager.
	 * @param idManager
	 *        The IdManager.
	 */
	public void setIdManager(IdManager idManager)
	{
		this.m_idManager = idManager;
	}

	/** Dependency: Timer */
	protected Timer m_timer = null;

	public void init()
	{
		// external calendar subscriptions: enable?
		enabled = m_configurationService.getBoolean(SAK_PROP_EXTSUBSCRIPTIONS_ENABLED, true);
		mergeIntoMyworkspace = m_configurationService.getBoolean(SAK_PROP_EXTSUBSCRIPTIONS_MERGEINTOMYWORKSPACE, true); 
		m_log.info("init(): enabled: " + enabled + ", merge from other sites into My Workspace? "+mergeIntoMyworkspace);

		if (enabled)
		{
			// INIT the caches
			long cacheRefreshRate = 43200; // 12 hours
			SimpleConfiguration cacheConfig = new SimpleConfiguration(1000, cacheRefreshRate, 0); // 12 hours
			cacheConfig.setStatisticsEnabled(true);
			institutionalSubscriptionCache = new SubscriptionCache(
					m_memoryService.createCache("org.sakaiproject.calendar.impl.BaseExternalCacheSubscriptionService.institutionalCache", cacheConfig));
			usersSubscriptionCache = new SubscriptionCache(
					m_memoryService.createCache("org.sakaiproject.calendar.impl.BaseExternalCacheSubscriptionService.userCache", cacheConfig));
			// TODO replace this with a real solution for when the caches are distributed by disabling the timer and using jobscheduler
			if (institutionalSubscriptionCache.getCache().isDistributed()) {
				m_log.error(institutionalSubscriptionCache.getCache().getName()+" is distributed but calendar subscription caches have a local timer refresh which means they will cause cache replication storms once every "+cacheRefreshRate+" seconds, do NOT distribute this cache");
			}
			if (usersSubscriptionCache.getCache().isDistributed()) {
				m_log.error(usersSubscriptionCache.getCache().getName()+" is distributed but calendar subscription caches have a local timer refresh which means they will cause cache replication storms once every "+cacheRefreshRate+" seconds, do NOT distribute this cache");
			}
			m_timer = new Timer(); // init timer

			// iCal column map
			try
			{
				columnMap = m_importerService
						.getDefaultColumnMap(CalendarImporterService.ICALENDAR_IMPORT);
			}
			catch (ImportException e1)
			{
				m_log
						.error("Unable to get column map for ICal import. External subscriptions will be disabled.");
				enabled = false;
				return;
			}

			// load institutional calendar subscriptions as timer tasks, this is so that 
			// we don't slow up the loading of sakai.
			for (final InsitutionalSubscription sub: getInstitutionalSubscriptions()) {
				m_timer.schedule(new TimerTask() {
					@Override
					public void run() {
						String reference =  calendarSubscriptionReference(INSTITUTIONAL_CONTEXT, getIdFromSubscriptionUrl(sub.url));
						getCalendarSubscription(reference);
					}
					
				}, 0, cacheRefreshRate);
			}
		}
	}

	public void destroy()
	{
		// Nothing to clean up for now.
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	// ######################################################
	// PUBLIC methods
	// ######################################################
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService#calendarSubscriptionReference(java.lang.String,
	 *      java.lang.String)
	 */
	public String calendarSubscriptionReference(String context, String id)
	{
		return BaseExternalSubscription.calendarSubscriptionReference(context, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.calendar.impl.ExternalCalendarSubscriptionService#getCalendarSubscription(java.lang.String)
	 */
	public Calendar getCalendarSubscription(String reference)
	{
		if (!isEnabled() || reference == null) return null;

		// Get Reference and Subscription URL
		Reference _ref = m_entityManager.newReference(reference);
		String subscriptionUrl = getSubscriptionUrlFromId(_ref.getId());
		if (subscriptionUrl == null || subscriptionUrl.equals("null")) return null;
		m_log.debug("ExternalCalendarSubscriptionService.getCalendarSubscription("
				+ reference + ")");
		m_log.debug(" |-> subscriptionUrl: " + subscriptionUrl);

		ExternalSubscription subscription = getExternalSubscription(subscriptionUrl,
				_ref.getContext());


		m_log.debug(" |-> Subscription is " + subscription);
		if (subscription != null)
		{
			m_log.debug(" |-> Calendar is " + subscription.getCalendar());
			return subscription.getCalendar();
		}
		else
		{
			m_log.debug(" |-> Calendar is NULL");
			return null;
		}
	}

	private ExternalSubscription getExternalSubscription(String subscriptionUrl, String context) {
		// Decide which cache to use.
		SubscriptionCache cache = (getInstitutionalSubscription(subscriptionUrl) != null)? institutionalSubscriptionCache : usersSubscriptionCache;
		
		ExternalSubscription subscription = cache.get(subscriptionUrl);
		// Did we get it?
		if (subscription == null)
		{
			subscription = loadCalendarSubscriptionFromUrl(subscriptionUrl, context);
			cache.put(subscription);
		}
		return subscription;
	}

	public Set<String> getCalendarSubscriptionChannelsForChannels(
			String primaryCalendarReference,
			Collection<Object> channels)
	{
		Set<String> subscriptionChannels = new HashSet<String>();
		Set<String> subscriptionUrlsAdded = new HashSet<String>();
		if(isMyWorkspace(primaryCalendarReference) && (!mergeIntoMyworkspace || m_securityService.isSuperUser())) {
			channels = new ArrayList<Object>();
			channels.add(primaryCalendarReference);
		}
		for (Object channel : channels)
		{
			Set<String> channelSubscriptions = getCalendarSubscriptionChannelsForChannel((String) channel);
			for (String channelSub : channelSubscriptions)
			{
				Reference ref = m_entityManager.newReference(channelSub);
				if (!subscriptionUrlsAdded.contains(ref.getId()))
				{
					subscriptionChannels.add(channelSub);
					subscriptionUrlsAdded.add(ref.getId());
				}
			}
		}
		return subscriptionChannels;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.calendar.impl.ExternalCalendarSubscriptionService#getCalendarSubscriptionChannelsForSite()
	 */
	public Set<String> getCalendarSubscriptionChannelsForChannel(String reference)
	{
		Set<String> channels = new HashSet<String>();
		if (!isEnabled() || reference == null) return channels;

		// get externally subscribed urls from tool config
		Reference ref = m_entityManager.newReference(reference);
		Site site = null;
		try
		{
			site = m_siteService.getSite(ref.getContext());
		}
		catch (IdUnusedException e)
		{
			m_log
					.error("ExternalCalendarSubscriptionService.getCalendarSubscriptionChannelsForChannel(): IdUnusedException for context in reference: "
							+ reference);
			return channels;
		}
		ToolConfiguration tc = site.getToolForCommonId(SCHEDULE_TOOL_ID);
		Properties config = tc == null? null : tc.getConfig();
		if (tc != null && config != null)
		{
			String prop = config.getProperty(TC_PROP_SUBCRIPTIONS);
			if (prop != null)
			{
				String[] chsPair = prop.split(SUBS_REF_DELIMITER);
				for (int i = 0; i < chsPair.length; i++)
				{
					String[] pair = chsPair[i].split(SUBS_NAME_DELIMITER);
					channels.add(pair[0]);
				}
			}
		}

		return channels;
	}

	public Set<ExternalSubscription> getAvailableInstitutionalSubscriptionsForChannel(
			String reference)
	{
		Set<ExternalSubscription> subscriptions = new HashSet<ExternalSubscription>();
		if (!isEnabled() || reference == null) return subscriptions;

		Reference ref = m_entityManager.newReference(reference);
		// If the cache has been flushed then we may need to reload it.
		for (InsitutionalSubscription sub : getInstitutionalSubscriptions()) {
			// Need to have way to load these.
			ExternalSubscription subscription = getExternalSubscription(sub.url, ref.getContext());
			if (subscription != null) {
				subscription.setContext(ref.getContext());
				subscriptions.add(subscription);
				subscription.setCalendar(null);
			}
		}
		return subscriptions;
	}

	public Set<ExternalSubscription> getSubscriptionsForChannel(String reference,
			boolean loadCalendar)
	{
		Set<ExternalSubscription> subscriptions = new HashSet<ExternalSubscription>();
		if (!isEnabled() || reference == null) return subscriptions;

		// get externally subscribed urls from tool config
		Reference ref = m_entityManager.newReference(reference);
		Site site = null;
		try
		{
			site = m_siteService.getSite(ref.getContext());
		}
		catch (IdUnusedException e)
		{
			m_log
					.error("ExternalCalendarSubscriptionService.getSubscriptionsForChannel(): IdUnusedException for context in reference: "
							+ reference);
			return subscriptions;
		}
		ToolConfiguration tc = site.getToolForCommonId(SCHEDULE_TOOL_ID);
		Properties config = tc == null? null : tc.getConfig();
		if (tc != null && config != null)
		{
			String prop = config.getProperty(TC_PROP_SUBCRIPTIONS);
			if (prop != null)
			{
				String[] chsPair = prop.split(SUBS_REF_DELIMITER);
				for (int i = 0; i < chsPair.length; i++)
				{
					String[] pair = chsPair[i].split(SUBS_NAME_DELIMITER);
					String r = pair[0];
					Reference r1 = m_entityManager.newReference(r);
					String url = getSubscriptionUrlFromId(r1.getId());
					String name = null;
					if (pair.length == 2)
						name = pair[1];
					else
					{
						try
						{
							name = institutionalSubscriptionCache.get(url)
									.getSubscriptionName();
						}
						catch (Exception e)
						{
							name = url;
						}
					}
					ExternalSubscription subscription = new BaseExternalSubscription(
							name, url, ref.getContext(),
							loadCalendar ? getCalendarSubscription(r) : null,
							isInstitutionalCalendar(r));
					subscriptions.add(subscription);
				}
			}
		}

		return subscriptions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.calendar.impl.ExternalCalendarSubscriptionService#setSubscriptionsForChannel(String,
	 *      Collection<ExternalSubscription>)
	 */
	public void setSubscriptionsForChannel(String reference,
			Collection<ExternalSubscription> subscriptions)
	{
		if (!isEnabled() || reference == null) return;

		// set externally subscriptions in tool config
		Reference ref = m_entityManager.newReference(reference);
		Site site = null;
		try
		{
			site = m_siteService.getSite(ref.getContext());
		}
		catch (IdUnusedException e)
		{
			m_log
					.error("ExternalCalendarSubscriptionService.setSubscriptionsForChannel(): IdUnusedException for context in reference: "
							+ reference);
			return;
		}

		ToolConfiguration tc = site.getToolForCommonId(SCHEDULE_TOOL_ID);
		if (tc != null)
		{
			boolean first = true;
			StringBuffer tmpStr = new StringBuffer();
			for (ExternalSubscription subscription : subscriptions)
			{
				if (!first) tmpStr.append(SUBS_REF_DELIMITER);
				first = false;

				tmpStr.append(subscription.getReference());
				if (!subscription.isInstitutional())
					tmpStr.append(SUBS_NAME_DELIMITER + subscription.getSubscriptionName());
			}

			Properties config = tc.getConfig();
			config.setProperty(TC_PROP_SUBCRIPTIONS, tmpStr.toString());
			tc.save();
		}
	}

	public boolean isInstitutionalCalendar(String reference)
	{
		// Get Reference and Subscription URL
		Reference _ref = m_entityManager.newReference(reference);
		String subscriptionUrl = getSubscriptionUrlFromId(_ref.getId());
		if (subscriptionUrl == null || subscriptionUrl.equals("null")) return false;

		// Is a institutional subscription?
		String[] subscriptionURLs = m_configurationService
				.getStrings(SAK_PROP_EXTSUBSCRIPTIONS_URL);
		if (subscriptionURLs != null)
		{
			for (String url: subscriptionURLs)
			{
				if (subscriptionUrl.equals(url)) {
					return true;
				}
			}
		}
		return false;
	}

	public String getIdFromSubscriptionUrl(String url)
	{
		return BaseExternalSubscription.getIdFromSubscriptionUrl(url);
	}

	public String getSubscriptionUrlFromId(String id)
	{
		return BaseExternalSubscription.getSubscriptionUrlFromId(id);
	}

	// ######################################################
	// PRIVATE methods
	// ######################################################
	/**
	 * Get the event type for this institutional subscription. 
	 * @param url
	 * @return The forced event type or <code>null</code> if it isn't defined.
	 */
	String getEventType(String url)
	{
		InsitutionalSubscription sub = getInstitutionalSubscription(url);
		return  (sub != null)? sub.eventType: null;
	}
	
	
	/**
	 * Insitutional subscriptions loaded from configuration.
	 */
	class InsitutionalSubscription {
		String url;
		String name;
		String eventType;
	}
	
	InsitutionalSubscription getInstitutionalSubscription(String url)
	{
		for (InsitutionalSubscription sub: getInstitutionalSubscriptions())
		{
			if(sub.url.equals(url))
			{
				return sub;
			}
		}
		return null;
	}
	
	List<InsitutionalSubscription> getInstitutionalSubscriptions() {
		String[] subscriptionURLs = m_configurationService
				.getStrings(SAK_PROP_EXTSUBSCRIPTIONS_URL);
		String[] subscriptionNames = m_configurationService
				.getStrings(SAK_PROP_EXTSUBSCRIPTIONS_NAME);
		String[] subscriptionEventTypes = m_configurationService
				.getStrings(SAK_PROP_EXTSUBSCRIPTIONS_EVENTTYPE);
		ArrayList<InsitutionalSubscription> subs = new ArrayList<InsitutionalSubscription>();
		if (subscriptionURLs != null)
		{
			for (int i = 0; i < subscriptionURLs.length; i++) 
			{
				String name = subscriptionNames[i];
				String eventType = subscriptionEventTypes[i];
				if (name != null) {
					InsitutionalSubscription sub = new InsitutionalSubscription();
					sub.url = subscriptionURLs[i];
					sub.name = name;
					sub.eventType = eventType;
					subs.add(sub);
				}
			}
		}
		return subs;
	}

	ExternalSubscription loadCalendarSubscriptionFromUrl(String url,
			String context)
	{
		InsitutionalSubscription sub = getInstitutionalSubscription(url);
		String name = null;
		String forcedEventType = null;
		if (sub != null)
		{
			name = sub.name;
			forcedEventType = sub.eventType;
		}
		return loadCalendarSubscriptionFromUrl(url, context, name, forcedEventType);	}

	ExternalSubscription loadCalendarSubscriptionFromUrl(String url,
			String context, String calendarName, String forcedEventType)
	{
		ExternalSubscription subscription = new BaseExternalSubscription(calendarName,
				url, context, null, INSTITUTIONAL_CONTEXT.equals(context));
		ExternalCalendarSubscription calendar = null;
		List<CalendarEvent> events = null;
		BufferedInputStream stream = null;
		try
		{
			URL _url = new URL(url);
			if (calendarName == null) calendarName = _url.getFile();

			// connect
			URLConnection conn = _url.openConnection();
			// Must set user agent so we can detect loops.
			conn.addRequestProperty("User-Agent", m_calendarService.getUserAgent());
			conn.setConnectTimeout(TIMEOUT);
			conn.setReadTimeout(TIMEOUT);
			// Now make the connection.
			conn.connect();
			stream =  new BufferedInputStream(conn.getInputStream());
			// import
			events = m_importerService.doImport(CalendarImporterService.ICALENDAR_IMPORT,
					stream, columnMap, null);

			String subscriptionId = getIdFromSubscriptionUrl(url);
			String reference = calendarSubscriptionReference(context, subscriptionId);
			calendar = new ExternalCalendarSubscription(reference);
			for (CalendarEvent event : events)
			{
				String eventType = event.getType();
				if (forcedEventType != null) eventType = forcedEventType;
				calendar.addEvent(event.getRange(), event.getDisplayName(), event
						.getDescription(), eventType, event.getLocation(), event
						.getRecurrenceRule(), null);
			}
			calendar.setName(calendarName);
			subscription.setCalendar(calendar);
			subscription.setInstitutional(getInstitutionalSubscription(url) != null);
			m_log.info("Loaded calendar subscription: " + subscription.toString());
		}
		catch (ImportException e)
		{
			m_log.info("Error loading calendar subscription '" + calendarName
					+ "' (will NOT retry again): " + url);
			String subscriptionId = getIdFromSubscriptionUrl(url);
			String reference = calendarSubscriptionReference(context, subscriptionId);
			calendar = new ExternalCalendarSubscription(reference);
			calendar.setName(calendarName);
			// By setting the calendar to be an empty one we make sure that we don't attempt to re-retrieve it
			// When 2 hours are up it will get refreshed through.
			subscription.setCalendar(calendar);
		}
		catch (PermissionException e)
		{
			// This will never be called (for now)
			e.printStackTrace();
		}
		catch (MalformedURLException e)
		{
			m_log.info("Mal-formed URL in calendar subscription '" + calendarName
					+ "': " + url);
		}
		catch (IOException e)
		{
			m_log.info("Unable to read calendar subscription '" + calendarName
					+ "' from URL (I/O Error): " + url);
		}
		catch (Exception e)
		{
			m_log.info("Unknown error occurred while reading calendar subscription '"
					+ calendarName + "' from URL: " + url);
		}
		finally
		{
			if (stream != null) {
				// Also closes the underlying InputStream
				try {
					stream.close();
				} catch (IOException e) {
					// Ignore
				}
			}
		}
		return subscription;
	}
	
	/**
	 * See if the current tab is the workspace tab (i.e. user site)
	 * @param primaryCalendarReference The primary calendar reference.
	 * @return true if we are currently on the "My Workspace" tab.
	 */
	private boolean isMyWorkspace(String primaryCalendarReference)
	{
		Reference ref = m_entityManager.newReference(primaryCalendarReference);
		String siteId = ref.getContext();
		return m_siteService.isUserSite(siteId);
	}

	// ######################################################
	// Support classes
	// ######################################################

	public class ExternalCalendarSubscription implements Calendar
	{
		/** Memory storage */
		protected Map<String, CalendarEvent> m_storage = new HashMap<String, CalendarEvent>();

		/** The context in which this calendar exists. */
		protected String m_context = null;

		/** Store the unique-in-context calendar id. */
		protected String m_id = null;

		/** Store the calendar name. */
		protected String m_name = null;

		/** The properties. */
		protected ResourcePropertiesEdit m_properties = null;

		protected String modifiedDateStr = null;

		public ExternalCalendarSubscription(String ref)
		{
			// set the ids
			Reference r = m_entityManager.newReference(ref);
			m_context = r.getContext();
			m_id = r.getId();

			// setup for properties
			m_properties = new BaseResourcePropertiesEdit();
		}

		public CalendarEvent addEvent(TimeRange range, String displayName,
				String description, String type, String location, EventAccess access,
				Collection groups, List attachments) throws PermissionException
		{
			return addEvent(range, displayName, description, type, location, attachments);
		}

		public CalendarEvent addEvent(TimeRange range, String displayName,
				String description, String type, String location, List attachments)
				throws PermissionException
		{
			return addEvent(range, displayName, description, type, location, null,
					attachments);
		}

		public CalendarEvent addEvent(TimeRange range, String displayName,
				String description, String type, String location, RecurrenceRule rrule,
				List attachments) throws PermissionException
		{
			// allocate a new unique event id
			// String id = getUniqueId();
			String id = getUniqueIdBasedOnFields(displayName, description, type, location, m_id);

			// create event
			ExternalCalendarEvent edit = new ExternalCalendarEvent(m_context, m_id, id);

			// set it up
			edit.setRange(range);
			edit.setDisplayName(displayName);
			edit.setDescription(description);
			edit.setType(type);
			edit.setLocation(location);
			edit.setCreator();
			if (rrule != null) edit.setRecurrenceRule(rrule);

			// put in storage
			m_storage.put(id, edit);

			return edit;
		}

		public CalendarEventEdit addEvent() throws PermissionException
		{
			// allocate a new unique event id
			// String id = getUniqueId();

			// create event
			// CalendarEventEdit event = new ExternalCalendarEvent(this, id);

			// put in storage
			// m_storage.put(id, event);

			return null;
		}

		public CalendarEvent addEvent(CalendarEvent event)
		{
			// allocate a new unique event id
			String id = event.getId();

			// put in storage
			m_storage.put(id, event);
			return event;
		}

		public Collection<CalendarEvent> getAllEvents()
		{
			return m_storage.values();
		}

		public boolean allowAddCalendarEvent()
		{
			return false;
		}

		public boolean allowAddEvent()
		{
			return false;
		}

		public boolean allowEditEvent(String eventId)
		{
			return false;
		}

		public boolean allowGetEvent(String eventId)
		{
			return true;
		}

		public boolean allowGetEvents()
		{
			return true;
		}

		public boolean allowRemoveEvent(CalendarEvent event)
		{
			return false;
		}

		public void cancelEvent(CalendarEventEdit edit)
		{
		}

		public void commitEvent(CalendarEventEdit edit, int intention)
		{
		}

		public void commitEvent(CalendarEventEdit edit)
		{
		}

		public String getContext()
		{
			return m_context;
		}

		public CalendarEventEdit getEditEvent(String eventId, String editType)
				throws IdUnusedException, PermissionException, InUseException
		{
			return null;
		}

		public CalendarEvent getEvent(String eventId) throws IdUnusedException,
				PermissionException
		{
			return m_storage.get(eventId);
		}

		public String getEventFields()
		{
			return m_properties
					.getPropertyFormatted(ResourceProperties.PROP_CALENDAR_EVENT_FIELDS);
		}

		public List getEvents(TimeRange range, Filter filter) throws PermissionException
		{
			return filterEvents(new ArrayList<CalendarEvent>(m_storage.values()), range);
		}

		public boolean getExportEnabled()
		{
			return false;
		}

		public Collection getGroupsAllowAddEvent()
		{
			return new ArrayList();
		}

		public Collection getGroupsAllowGetEvent()
		{
			return new ArrayList();
		}

		public Collection getGroupsAllowRemoveEvent(boolean own)
		{
			return new ArrayList();
		}

		public Time getModified()
		{
			return m_timeService.newTimeGmt(modifiedDateStr);
		}

		public CalendarEventEdit mergeEvent(Element el) throws PermissionException,
				IdUsedException
		{
			// TODO Implement mergeEvent()
			return null;
		}

		public void removeEvent(CalendarEventEdit edit, int intention)
				throws PermissionException
		{
		}

		public void removeEvent(CalendarEventEdit edit) throws PermissionException
		{
		}

		public void setExportEnabled(boolean enable)
		{
		}

		public void setModified()
		{
		}

		public String getId()
		{
			return m_id;
		}

		public ResourceProperties getProperties()
		{
			return m_properties;
		}

		public String getReference()
		{
			return m_calendarService.calendarSubscriptionReference(m_context, m_id);
		}

		protected void setContext(String context)
		{
			// set the ids
			m_context = context;
			for (CalendarEvent e : m_storage.values())
			{
				// ((ExternalCalendarEvent) e).setCalendar(this);
				((ExternalCalendarEvent) e).setCalendarContext(m_context);
				((ExternalCalendarEvent) e).setCalendarId(m_id);
			}
		}

		public String getReference(String rootProperty)
		{
			return rootProperty + getReference();
		}

		public String getUrl()
		{
			// TODO Auto-generated method stub
			return null;
		}

		public String getUrl(String rootProperty)
		{
			// TODO Auto-generated method stub
			return null;
		}

		public Element toXml(Document doc, Stack stack)
		{
			// TODO Auto-generated method stub
			return null;
		}

		public String getName()
		{
			return m_name;
		}

		public void setName(String calendarName)
		{
			this.m_name = calendarName;
		}

		/**
		 * Access the id generating service and return a unique id.
		 * 
		 * @return a unique id.
		 */
		protected String getUniqueId()
		{
			return m_idManager.createUuid();
		}

		protected String getUniqueIdBasedOnFields(String displayName, String description,
				String type, String location, String calendarId)
		{
			StringBuilder key = new StringBuilder();
			key.append(displayName);
			key.append(description);
			key.append(type);
			key.append(location);
			key.append(calendarId);

			String id = null;
			int n = 0;
			boolean unique = false;
			while (!unique)
			{
				byte[] bytes = key.toString().getBytes();
				try{
					MessageDigest digest = MessageDigest.getInstance("SHA-1");
					digest.update(bytes);
					bytes = digest.digest(); 
					id = getHexStringFromBytes(bytes);
				}catch(NoSuchAlgorithmException e){
					// fall back to Base64
					byte[] encoded = Base64.encodeBase64(bytes);
					id = StringUtils.newStringUtf8(encoded);
				}
				if (!m_storage.containsKey(id)) unique = true;
				else key.append(n++);
			}
			return id;
		}
		
		protected String getHexStringFromBytes(byte[] raw) 
		{
			final String HEXES = "0123456789ABCDEF";
			if(raw == null)
			{
				return null;
			}
			final StringBuilder hex = new StringBuilder(2 * raw.length);
			for(final byte b : raw)
			{
				hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
			}
			return hex.toString();
		}

		/**
		 * Filter the events to only those in the time range.
		 * 
		 * @param events
		 *        The full list of events.
		 * @param range
		 *        The time range.
		 * @return A list of events from the incoming list that overlap the
		 *         given time range.
		 */
		protected List<CalendarEvent> filterEvents(List<CalendarEvent> events,
				TimeRange range)
		{
			List<CalendarEvent> filtered = new ArrayList<CalendarEvent>();
			for (int i = 0; i < events.size(); i++)
			{
				CalendarEvent event = events.get(i);

				// resolve the event to the list of events in this range
				// TODO Support for recurring events
				List<CalendarEvent> resolved = ((ExternalCalendarEvent) event)
						.resolve(range);
				filtered.addAll(resolved);
			}

			return filtered;

		}

		/**
		 * Checks if user has permission to modify any event (or fields) in this calendar
		 * @param function
		 * @return
		 */
		@Override
		public boolean canModifyAnyEvent(String function){
			return CalendarService.AUTH_MODIFY_CALENDAR_ANY.equals(function);
		}
	}

	public class ExternalCalendarEvent implements CalendarEvent
	{
		// protected Calendar m_calendar = null;
		protected String m_calendar_context = null;

		protected String m_calendar_id = null;

		protected ResourcePropertiesEdit m_properties = null;

		protected String m_id = null;

		protected String calendarReference = null;

		protected TimeRange m_range = null;

		protected TimeRange m_baseRange = null;

		protected RecurrenceRule m_singleRule = null;

		protected RecurrenceRule m_exclusionRule = null;

		public ExternalCalendarEvent(String calendarContext, String calendarId, String id)
		{
			this(calendarContext, calendarId, id, null);
		}

		public ExternalCalendarEvent(String calendarContext, String calendarId,
				String id, String eventType)
		{
			m_id = id;
			// m_calendar = calendar;
			m_calendar_context = calendarContext;
			m_calendar_id = calendarId;
			m_properties = new BaseResourcePropertiesEdit();
			if (eventType != null)
				m_properties
						.addProperty(ResourceProperties.PROP_CALENDAR_TYPE, eventType);
		}

		public ExternalCalendarEvent(CalendarEvent other, RecurrenceInstance ri)
		{
			// m_calendar = ((ExternalCalendarEvent) other).m_calendar;
			m_calendar_context = ((ExternalCalendarEvent) other).m_calendar_context;
			m_calendar_id = ((ExternalCalendarEvent) other).m_calendar_id;

			// encode the instance and the other's id into my id
			m_id = '!' + ri.getRange().toString() + '!' + ri.getSequence() + '!'
					+ ((ExternalCalendarEvent) other).m_id;

			// use the new range
			m_range = (TimeRange) ri.getRange().clone();
			m_baseRange = ((ExternalCalendarEvent) other).m_range;

			// point at the properties
			m_properties = ((ExternalCalendarEvent) other).m_properties;

			// point at the rules
			m_singleRule = ((ExternalCalendarEvent) other).m_singleRule;
			m_exclusionRule = ((ExternalCalendarEvent) other).m_exclusionRule;
		}

		public EventAccess getAccess()
		{
			return CalendarEvent.EventAccess.SITE;
		}

		public String getCalendarReference()
		{
			// return m_calendar.getReference();
			return m_calendarService.calendarSubscriptionReference(m_calendar_context,
					m_calendar_id);
		}

		// protected Calendar getCalendar(){
		// return m_calendar;
		// }

		// protected void setCalendar(Calendar calendar) {
		// m_calendar = calendar;
		// }

		protected void setCalendarContext(String calendarContext)
		{
			m_calendar_context = calendarContext;
		}

		protected void setCalendarId(String calendarId)
		{
			m_calendar_id = calendarId;
		}

		public String getCreator()
		{
			return m_properties.getProperty(ResourceProperties.PROP_CREATOR);
		}

		public String getDescription()
		{
			return FormattedText
					.convertFormattedTextToPlaintext(getDescriptionFormatted());
		}

		public String getDescriptionFormatted()
		{
			// %%% JANDERSE the calendar event description can now be formatted
			// text
			// first try to use the formatted text description; if that isn't
			// found, use the plaintext description
			String desc = m_properties
					.getPropertyFormatted(ResourceProperties.PROP_DESCRIPTION + "-html");
			if (desc != null && desc.length() > 0) return desc;
			desc = m_properties.getPropertyFormatted(ResourceProperties.PROP_DESCRIPTION
					+ "-formatted");
			desc = FormattedText.convertOldFormattedText(desc);
			if (desc != null && desc.length() > 0) return desc;
			desc = FormattedText.convertPlaintextToFormattedText(m_properties
					.getPropertyFormatted(ResourceProperties.PROP_DESCRIPTION));
			return desc;
		}

		public String getDisplayName()
		{
			return m_properties
					.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
		}

		public String getField(String name)
		{
			// names are prefixed to form a namespace
			name = ResourceProperties.PROP_CALENDAR_EVENT_FIELDS + "." + name;
			return m_properties.getPropertyFormatted(name);
		}

		public Collection getGroupObjects()
		{
			return new ArrayList();
		}

		public String getGroupRangeForDisplay(Calendar calendar)
		{
			return "";
		}

		public Collection getGroups()
		{
			return new ArrayList();
		}

		public String getLocation()
		{
			return m_properties
					.getPropertyFormatted(ResourceProperties.PROP_CALENDAR_LOCATION);
		}

		public String getModifiedBy()
		{
			return m_properties.getPropertyFormatted(ResourceProperties.PROP_MODIFIED_BY);
		}

		public TimeRange getRange()
		{
			// range might be null in the creation process, before the fields
			// are set in an edit, but
			// after the storage has registered the event and it's id.
			if (m_range == null)
			{
				return m_timeService.newTimeRange(m_timeService.newTime(0));
			}

			// return (TimeRange) m_range.clone();
			return m_range;
		}

		public RecurrenceRule getRecurrenceRule()
		{
			return m_singleRule;
		}

		public RecurrenceRule getExclusionRule()
		{
			if (m_exclusionRule == null)
				m_exclusionRule = new ExclusionSeqRecurrenceRule();
			return m_exclusionRule;
		}

		protected List resolve(TimeRange range)
		{
			List rv = new Vector();

			// for no rules, use the event if it's in range
			if (m_singleRule == null)
			{
				// the actual event
				if (range.overlaps(getRange()))
				{
					rv.add(this);
				}
			}

			// for rules...
			else
			{
				List instances = m_singleRule.generateInstances(this.getRange(), range,
						m_timeService.getLocalTimeZone());

				// remove any excluded
				getExclusionRule().excludeInstances(instances);
				for (Iterator iRanges = instances.iterator(); iRanges.hasNext();)
				{
					RecurrenceInstance ri = (RecurrenceInstance) iRanges.next();

					// generate an event object that is exactly like me but with
					// this range and no rules
					CalendarEvent clone = new ExternalCalendarEvent(this, ri);
					rv.add(clone);
				}
			}
			return rv;
		}

		public void setRecurrenceRule(RecurrenceRule rule)
		{
			m_singleRule = rule;
		}

		public void setExclusionRule(RecurrenceRule rule)
		{
			m_exclusionRule = rule;
		}

		public String getType()
		{
			return m_properties
					.getPropertyFormatted(ResourceProperties.PROP_CALENDAR_TYPE);
		}

		public boolean isUserOwner()
		{
			return false;
		}

		public String getId()
		{
			return m_id;
		}

		protected void setId(String id)
		{
			m_id = id;
		}

		public ResourceProperties getProperties()
		{
			return m_properties;
		}

		public String getReference()
		{
			// return m_calendar.getReference() + Entity.SEPARATOR + m_id;
			return m_calendarService.eventSubscriptionReference(m_calendar_context,
					m_calendar_id, m_id);
		}

		public String getReference(String rootProperty)
		{
			return rootProperty + getReference();
		}

		public String getUrl()
		{
			return null;// m_calendar.getUrl() + getId();
		}

		public String getUrl(String rootProperty)
		{
			return rootProperty + getUrl();
		}

		public Element toXml(Document doc, Stack stack)
		{
			// TODO Auto-generated method stub
			return null;
		}

		public int compareTo(Object o)
		{
			if (!(o instanceof CalendarEvent)) throw new ClassCastException();
			Time mine = getRange().firstTime();
			Time other = ((CalendarEvent) o).getRange().firstTime();

			if (mine.before(other)) return -1;
			if (mine.after(other)) return +1;
			return 0;
		}

		public List getAttachments()
		{
			// TODO Auto-generated method stub
			return null;
		}

		public void setCreator()
		{
			String currentUser = m_sessionManager.getCurrentSessionUserId();
			String now = m_timeService.newTime().toString();
			m_properties.addProperty(ResourceProperties.PROP_CREATOR, currentUser);
			m_properties.addProperty(ResourceProperties.PROP_CREATION_DATE, now);
		}

		public void setLocation(String location)
		{
			m_properties.addProperty(ResourceProperties.PROP_CALENDAR_LOCATION, location);
		}

		public void setType(String type)
		{
			m_properties.addProperty(ResourceProperties.PROP_CALENDAR_TYPE, type);
		}

		public void setDescription(String description)
		{

			setDescriptionFormatted(FormattedText
					.convertPlaintextToFormattedText(description));
		}

		public void setDescriptionFormatted(String description)
		{
			// %%% JANDERSE the calendar event description can now be formatted
			// text
			// save both a formatted and a plaintext version of the description
			m_properties.addProperty(ResourceProperties.PROP_DESCRIPTION + "-html",
					description);
			m_properties.addProperty(ResourceProperties.PROP_DESCRIPTION, FormattedText
					.convertFormattedTextToPlaintext(description));
		}

		public void setDisplayName(String displayName)
		{
			m_properties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, displayName);
		}

		public void setRange(TimeRange range)
		{
			m_range = (TimeRange) range.clone();
		}
		
		/**
		 * Gets a site name for this calendar event
		 */
		public String getSiteName()
		{
			String calendarName = "";
			
			if (m_calendar_context != null)
			{
				try
				{
					Site site = m_siteService.getSite(m_calendar_context);
					if (site != null)
						calendarName = site.getTitle();
				}
				catch (IdUnusedException e)
				{
					m_log.warn(".getSiteName(): " + e);
				}
			}
			
			return calendarName;
		}
		
	}
}
