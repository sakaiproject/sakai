package org.sakaiproject.calendar.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.calendar.api.CalendarImporterService;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService;
import org.sakaiproject.calendar.api.ExternalSubscription;
import org.sakaiproject.calendar.api.RecurrenceRule;
import org.sakaiproject.calendar.api.CalendarEvent.EventAccess;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.ImportException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.javax.Filter;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.commonscodec.CommonsCodecBase64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

	/** Default max cached external subscription entries (institutional) */
	private final static int DEFAULT_MAX_INST_CACHED_ENTRIES = 16;
	
	/** Default max cached external subscription entries (user) */
	private final static int DEFAULT_MAX_USER_CACHED_ENTRIES = 16;

	/** Default max cached external subscription time in minutes (institutional) */
	private final static int DEFAULT_MAX_INST_CACHED_TIME = 2 * 60; // 2h

	/** Default max cached external subscription time in minutes (user) */
	private final static int DEFAULT_MAX_USER_CACHED_TIME = 2 * 60; // 2h

	/** iCal external subscription enable flag */
	private boolean enabled = false;
	
	/** merge iCal external subscriptions from other sites into My Workspace?  */
	private boolean mergeIntoMyworkspace = true;

	/** Column map for iCal processing */
	private Map columnMap = null;

	/** Cache map of Institutional Calendars: <String url, Calendar cal> */
	private Map<String, ExternalSubscription> institutionalSubscriptions = null;

	/** Cache map of user Calendars: <String url, Calendar cal> */
	private Map<String, ExternalSubscription> userSubscriptions = null;

	// ######################################################
	// Spring services
	// ######################################################
	/** Dependency: CalendarService. */
	protected CalendarService m_calendarService = null;

	public void setCalendarService(CalendarService service)
	{
		this.m_calendarService = service;
	}

	/** Dependency: ServerConfigurationService. */
	protected ServerConfigurationService m_configurationService = null;

	public void setServerConfigurationService(ServerConfigurationService service)
	{
		this.m_configurationService = service;
	}

	/** Dependency: CalendarImporterService. */
	protected CalendarImporterService m_importerService = null;

	public void setCalendarImporterService(CalendarImporterService service)
	{
		this.m_importerService = service;
	}

	/** Dependency: EntityManager. */
	protected EntityManager m_entityManager = null;

	public void setEntityManager(EntityManager service)
	{
		this.m_entityManager = service;
	}

	/** Dependency: SiteService. */
	protected SiteService m_siteService = null;

	public void setSiteService(SiteService service)
	{
		this.m_siteService = service;
	}

	/** Dependency: IdManager (COVER). */
	protected IdManager m_idManager = org.sakaiproject.id.cover.IdManager.getInstance();

	public void init()
	{
		// external calendar subscriptions: enable?
		enabled = m_configurationService.getBoolean(SAK_PROP_EXTSUBSCRIPTIONS_ENABLED, false);
		mergeIntoMyworkspace = m_configurationService.getBoolean(SAK_PROP_EXTSUBSCRIPTIONS_MERGEINTOMYWORKSPACE, true); 
		m_log.info("init(): enabled: " + enabled + ", merge from other sites into My Workspace? "+mergeIntoMyworkspace);

		if (enabled)
		{
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

			// subscription cache config
			// Institutional subscription defaults: max 16 entries, max 2 hours
			int institutionalMaxSize = m_configurationService.getInt(SAK_PROP_EXTSUBSCRIPTIONS_URL+".count",
					DEFAULT_MAX_INST_CACHED_ENTRIES );
			int institutionalMaxTime = m_configurationService.getInt(
					SAK_PROP_EXTSUBSCRIPTIONS_INST_CACHETIME, DEFAULT_MAX_INST_CACHED_TIME);
			m_log.info("init(): " + institutionalMaxSize
					+ " institutional subscriptions in memory, re-loading every "
					+ institutionalMaxTime + " min");
			institutionalSubscriptions = new SubscriptionCacheMap(institutionalMaxSize,
					institutionalMaxTime * 60 * 1000 );
			// User subscription defaults: max 32 entries, max 2 hours
			int userMaxSize = m_configurationService.getInt(
					SAK_PROP_EXTSUBSCRIPTIONS_USER_CACHEENTRIES, DEFAULT_MAX_USER_CACHED_ENTRIES);
			int userMaxTime = m_configurationService.getInt(
					SAK_PROP_EXTSUBSCRIPTIONS_USER_CACHETIME, DEFAULT_MAX_USER_CACHED_TIME);
			m_log.info("init(): max " + userMaxSize
					+ " user subscriptions in memory, re-loading every " + userMaxTime
					+ " min");
			userSubscriptions = new SubscriptionCacheMap(userMaxSize,
					userMaxTime * 60 * 1000);

			// add reload-on-expire listener
			SubscriptionExpiredListener listener = new SubscriptionReloadOnExpiredListener();
			((SubscriptionCacheMap) institutionalSubscriptions)
					.setSubscriptionExpiredListener(listener);
			((SubscriptionCacheMap) userSubscriptions)
					.setSubscriptionExpiredListener(listener);

			// load institutional calendar subscriptions
			loadInstitutionalSubscriptions();
		}
	}

	public void destroy()
	{
		m_log.info("destroy()");
		try
		{
			((SubscriptionCacheMap) institutionalSubscriptions).stopCleanerThread();
			((SubscriptionCacheMap) userSubscriptions).stopCleanerThread();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
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
		return CalendarService.REFERENCE_ROOT + Entity.SEPARATOR
				+ CalendarService.REF_TYPE_CALENDAR_SUBSCRIPTION + Entity.SEPARATOR
				+ context + Entity.SEPARATOR + id;
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

		ExternalSubscription subscription = null;
		// 1. Is a institutional subscription (cached)?
		if (institutionalSubscriptions.containsKey(subscriptionUrl))
		{
			m_log.debug(" |-> Is a institutional subscription");
			subscription = institutionalSubscriptions.get(subscriptionUrl);
			// may not have this one loaded yet...
			if (subscription == null || subscription.getCalendar() == null)
			{
				m_log.debug(" |-> Not cached yet...");
				reloadInstitutionalSubscription(subscriptionUrl, INSTITUTIONAL_CONTEXT);
				subscription = institutionalSubscriptions.get(subscriptionUrl);
			}
			if (subscription != null) subscription.setContext(_ref.getContext());
		}
		// 2. Is the user subscription cached?
		else if (userSubscriptions.containsKey(subscriptionUrl))
		{
			m_log.debug(" |-> Is a user subscription");
			subscription = userSubscriptions.get(subscriptionUrl);
		}

		// 3. Is a user subscription but is not cached.
		if (!institutionalSubscriptions.containsKey(subscriptionUrl)
				&& (subscription == null || subscription.getCalendar() == null))
		{
			m_log.debug(" |-> Not cached yet...");
			subscription = loadCalendarSubscriptionFromUrl(subscriptionUrl, _ref
					.getContext());
			if (subscription != null)
			{
				userSubscriptions.put(subscriptionUrl, subscription);
				subscription.setContext(_ref.getContext());
			}
		}

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

	public Set<String> getCalendarSubscriptionChannelsForChannels(
			String primaryCalendarReference,
			Collection<Object> channels)
	{
		Set<String> subscriptionChannels = new HashSet<String>();
		Set<String> subscriptionUrlsAdded = new HashSet<String>();
		if(isOnWorkspaceTab() && (!mergeIntoMyworkspace || SecurityService.isSuperUser())) {
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
		for (ExternalSubscription subscription : institutionalSubscriptions.values())
		{
			subscription.setContext(ref.getContext());
			subscriptions.add(subscription);
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
							name = institutionalSubscriptions.get(url)
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
			String tmpStr = "";
			for (ExternalSubscription subscription : subscriptions)
			{
				if (!first) tmpStr += SUBS_REF_DELIMITER;
				first = false;

				tmpStr += subscription.getReference();
				if (!subscription.isInstitutional())
					tmpStr += SUBS_NAME_DELIMITER + subscription.getSubscriptionName();
			}

			Properties config = tc.getConfig();
			config.setProperty(TC_PROP_SUBCRIPTIONS, tmpStr);
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
		return institutionalSubscriptions.containsKey(subscriptionUrl);
	}

	public String getIdFromSubscriptionUrl(String url)
	{
		// use Base64
		byte[] encoded = CommonsCodecBase64.encodeBase64(url.getBytes());
		// '/' cannot be used in Reference => use '.' instead (not part of
		// Base64 alphabet)
		String encStr = new String(encoded).replaceAll("/", "\\.");
		return encStr;
	}

	public String getSubscriptionUrlFromId(String id)
	{
		// use Base64
		byte[] decoded = CommonsCodecBase64.decodeBase64(id.replaceAll("\\.", "/")
				.getBytes());
		return new String(decoded);
	}

	// ######################################################
	// PRIVATE methods
	// ######################################################
	private void loadInstitutionalSubscriptions()
	{
		String[] subscriptionURLs = m_configurationService
				.getStrings(SAK_PROP_EXTSUBSCRIPTIONS_URL);
		String[] subscriptionNames = m_configurationService
				.getStrings(SAK_PROP_EXTSUBSCRIPTIONS_NAME);
		String[] subscriptionEventTypes = m_configurationService
				.getStrings(SAK_PROP_EXTSUBSCRIPTIONS_EVENTTYPE);
		if (subscriptionURLs != null)
		{
			for (int i = 0; i < subscriptionURLs.length; i++)
			{
				if (!institutionalSubscriptions.containsKey(subscriptionURLs[i])
						|| institutionalSubscriptions.get(subscriptionURLs[i]) == null
						|| institutionalSubscriptions.get(subscriptionURLs[i])
								.getCalendar() == null)
				{
					String calendarName = subscriptionURLs[i];
					if (subscriptionNames != null && subscriptionNames.length > i)
						calendarName = subscriptionNames[i];
					String forcedEventType = null;
					if (subscriptionEventTypes != null
							&& subscriptionEventTypes.length > i)
						forcedEventType = subscriptionEventTypes[i];

					ExternalSubscription subscription = loadCalendarSubscriptionFromUrl(
							subscriptionURLs[i], INSTITUTIONAL_CONTEXT, calendarName,
							forcedEventType);
					institutionalSubscriptions.put(subscriptionURLs[i], subscription);
				}
			}
		}
	}

	private void reloadInstitutionalSubscription(String subscriptionUrl, String context)
	{
		String[] subscriptionURLs = m_configurationService
				.getStrings(SAK_PROP_EXTSUBSCRIPTIONS_URL);
		String[] subscriptionNames = m_configurationService
				.getStrings(SAK_PROP_EXTSUBSCRIPTIONS_NAME);
		String[] subscriptionEventTypes = m_configurationService
				.getStrings(SAK_PROP_EXTSUBSCRIPTIONS_EVENTTYPE);
		if (subscriptionURLs != null)
		{
			for (int i = 0; i < subscriptionURLs.length; i++)
			{
				if (subscriptionURLs[i].equals(subscriptionUrl))
				{
					String calendarName = null;
					if (subscriptionNames != null && subscriptionNames.length > i)
						calendarName = subscriptionNames[i];
					String forcedEventType = null;
					if (subscriptionEventTypes != null
							&& subscriptionEventTypes.length > i)
						forcedEventType = subscriptionEventTypes[i];

					ExternalSubscription subscription = loadCalendarSubscriptionFromUrl(
							subscriptionURLs[i], context, calendarName, forcedEventType);
					subscription.setInstitutional(true);
					institutionalSubscriptions.put(subscriptionURLs[i], subscription);
					break;
				}
			}
		}
	}

	private ExternalSubscription loadCalendarSubscriptionFromUrl(String url,
			String context)
	{
		return loadCalendarSubscriptionFromUrl(url, context, null, null);
	}

	private ExternalSubscription loadCalendarSubscriptionFromUrl(String url,
			String context, String calendarName, String forcedEventType)
	{
		ExternalSubscription subscription = new BaseExternalSubscription(calendarName,
				url, context, null, INSTITUTIONAL_CONTEXT.equals(context));
		ExternalCalendarSubscription calendar = null;
		List<CalendarEvent> events = null;
		try
		{
			URL _url = new URL(url);
			if (calendarName == null) calendarName = _url.getFile();

			// connect
			URLConnection conn = _url.openConnection();
			conn.setConnectTimeout(TIMEOUT);
			conn.setReadTimeout(TIMEOUT);
			InputStream stream = conn.getInputStream();
			BufferedInputStream buffStream = new BufferedInputStream(stream);
			// import
			events = m_importerService.doImport(CalendarImporterService.ICALENDAR_IMPORT,
					buffStream, columnMap, null);

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
			m_log.info("Loaded calendar subscription: " + subscription.toString());
			buffStream.close();
			stream.close();
		}
		catch (ImportException e)
		{
			m_log.error("Error loading calendar subscription '" + calendarName
					+ "' (will NOT retry again): " + url, e);
			String subscriptionId = getIdFromSubscriptionUrl(url);
			String reference = calendarSubscriptionReference(context, subscriptionId);
			calendar = new ExternalCalendarSubscription(reference);
			calendar.setName(calendarName);
			subscription.setCalendar(calendar);
		}
		catch (PermissionException e)
		{
			// This will never be called (for now)
			e.printStackTrace();
		}
		catch (MalformedURLException e)
		{
			m_log.error("Mal-formed URL in calendar subscription '" + calendarName
					+ "': " + url, e);
		}
		catch (IOException e)
		{
			m_log.error("Unable to read calendar subscription '" + calendarName
					+ "' from URL (I/O Error): " + url, e);
		}
		catch (Exception e)
		{
			m_log.error("Unknown error occurred while reading calendar subscription '"
					+ calendarName + "' from URL: " + url, e);
		}
		return subscription;
	}
	
	/**
	 * See if the current tab is the workspace tab (i.e. user site)
	 * @return true if we are currently on the "My Workspace" tab.
	 */
	private boolean isOnWorkspaceTab()
	{
		return m_siteService.isUserSite(ToolManager.getCurrentPlacement().getContext());
	}

	// ######################################################
	// Support classes
	// ######################################################

	public class BaseExternalSubscription implements ExternalSubscription
	{
		private String subscriptionName;

		private String subscriptionUrl;

		private String reference;

		private String context;

		private Calendar calendar;

		private boolean isInstitutional;

		public BaseExternalSubscription()
		{
		}

		public BaseExternalSubscription(String subscriptionName, String subscriptionUrl,
				String context, Calendar calendar, boolean isInstitutional)
		{
			setSubscriptionName(subscriptionName);
			setSubscriptionUrl(subscriptionUrl);
			setCalendar(calendar);
			setContext(context);
			setInstitutional(isInstitutional);
			if (calendar != null) setReference(calendar.getReference());
		}

		public String getSubscriptionName()
		{
			return subscriptionName;
		}

		public void setSubscriptionName(String subscriptionName)
		{
			this.subscriptionName = subscriptionName;
		}

		public String getSubscriptionUrl()
		{
			return subscriptionUrl;
		}

		public void setSubscriptionUrl(String subscriptionUrl)
		{
			this.subscriptionUrl = subscriptionUrl;
		}

		public String getContext()
		{
			return context;
		}

		public void setContext(String context)
		{
			this.context = context;
			if (calendar != null)
				((ExternalCalendarSubscription) calendar).setContext(context);
		}

		public void setReference(String reference)
		{
			this.reference = reference;
		}

		public String getReference()
		{
			if (calendar != null)
				return calendar.getReference();
			else
				return calendarSubscriptionReference(context,
						getIdFromSubscriptionUrl(subscriptionUrl));
		}

		public Calendar getCalendar()
		{
			return calendar;
		}

		public void setCalendar(Calendar calendar)
		{
			this.calendar = calendar;
		}

		public boolean isInstitutional()
		{
			return isInstitutional;
		}

		public void setInstitutional(boolean isInstitutional)
		{
			this.isInstitutional = isInstitutional;
		}

		@Override
		public boolean equals(Object o)
		{
			if (o instanceof BaseExternalSubscription)
				return getReference().equals(
						((BaseExternalSubscription) o).getReference());
			return false;
		}

		@Override
		public String toString()
		{
			StringBuilder buff = new StringBuilder();
			buff.append(getSubscriptionName() != null ? getSubscriptionName() : "");
			buff.append('|');
			buff.append(getSubscriptionUrl());
			buff.append('|');
			buff.append(getReference());
			return buff.toString();
		}
	}

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
			String id = getUniqueIdBasedOnFields(displayName, description, type, location);

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
			return TimeService.newTimeGmt(modifiedDateStr);
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
			this.m_name = m_name;
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
				String type, String location)
		{
			String key = displayName + description + type + location;
			String id = null;
			int n = 0;
			boolean unique = false;
			while (!unique)
			{
				byte[] encoded = CommonsCodecBase64.encodeBase64(key.getBytes());
				key += n++;
				id = new String(encoded);
				if (!m_storage.containsKey(id)) unique = true;
			}
			return id;
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
				return TimeService.newTimeRange(TimeService.newTime(0));
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
						TimeService.getLocalTimeZone());

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
			String currentUser = SessionManager.getCurrentSessionUserId();
			String now = TimeService.newTime().toString();
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

	/**
	 * Hash table and linked list implementation of the Map interface,
	 * access-ordered. Older entries will be removed if map exceeds the maximum
	 * capacity specified.
	 * 
	 * @author nfernandes
	 */
	class SubscriptionCacheMap extends LinkedHashMap<String, ExternalSubscription>
			implements Runnable
	{
		private static final long serialVersionUID = 1L;

		private final static float DEFAULT_LOAD_FACTOR = 0.75f;

		private int maxCachedEntries;

		private int maxCachedTime;

		private Thread threadCleaner;

		private boolean threadCleanerRunning = false;

		private Object threadCleanerRunningSemaphore = new Object();

		private Map<String, Long> cacheTime;

		private SubscriptionExpiredListener listener = null;

		public SubscriptionCacheMap()
		{
			this(DEFAULT_MAX_USER_CACHED_ENTRIES, DEFAULT_MAX_USER_CACHED_TIME);
		}

		/**
		 * LinkedHashMap implementation that removes least accessed entry and
		 * (optionally) removes entries with more that maxCachedTime.
		 * 
		 * @param maxCachedEntries
		 *        Maximum number of entries to keep cached.
		 * @param maxCachedTime
		 *        If > 0, entries will be removed after being 'maxCachedTime' in
		 *        cache.
		 */
		public SubscriptionCacheMap(int maxCachedEntries, int maxCachedTime)
		{
			super(maxCachedEntries, DEFAULT_LOAD_FACTOR, true);
			this.maxCachedEntries = maxCachedEntries;
			this.maxCachedTime = maxCachedTime;
			if (maxCachedTime > 0)
			{
				cacheTime = new ConcurrentHashMap<String, Long>();
				startCleanerThread();
			}
		}

		public void setSubscriptionExpiredListener(SubscriptionExpiredListener listener)
		{
			if(this.listener != null)
			{
				synchronized(this.listener)
				{
					this.listener = listener;
				}
			}
			else
				this.listener = listener;
		}

		public void removeSubscriptionExpiredListener()
		{
			synchronized(this.listener)
			{
				this.listener = null;
			}
		}

		@Override
		public ExternalSubscription get(Object arg0)
		{
			ExternalSubscription e = null;
			synchronized (this)
			{
				e = super.get(arg0);
			}
			return e;
		}

		@Override
		public ExternalSubscription put(String key, ExternalSubscription value)
		{
			if (maxCachedTime > 0 && key != null)
			{
				cacheTime.put(key, System.currentTimeMillis());
			}
			return super.put(key, value);
		}

		@Override
		public void putAll(Map<? extends String, ? extends ExternalSubscription> map)
		{
			if (maxCachedTime > 0 && map != null)
			{
				for (String key : map.keySet())
				{
					cacheTime.put(key, System.currentTimeMillis());
				}
			}
			if ( map != null )
				super.putAll(map);
		}

		@Override
		public void clear()
		{
			if (maxCachedTime > 0)
			{
				cacheTime.clear();
			}
			super.clear();
		}

		@Override
		public ExternalSubscription remove(Object key)
		{
			if (maxCachedTime > 0 && key != null)
			{
				if (cacheTime.containsKey(key)) cacheTime.remove(key);
			}
			return super.remove(key);
		}

		public void setMaxCachedEntries(int maxCachedEntries)
		{
			this.maxCachedEntries = maxCachedEntries;
		}

		@Override
		protected boolean removeEldestEntry(Entry<String, ExternalSubscription> arg0)
		{
			return size() > maxCachedEntries;
		}

		public void run()
		{
			try
			{
				while (threadCleanerRunning)
				{
					// clean expired entries
					List<String> toClear = new ArrayList<String>();
					for (String key : this.keySet())
					{
						long cachedFor = System.currentTimeMillis() - cacheTime.get(key);
						if (cachedFor > maxCachedTime)
						{
							toClear.add(key);
						}
					}
					// cleaning is not object removal but, Calendar removal from
					// value (ExternalSubscription)
					for (String key : toClear)
					{
						synchronized (this)
						{
							ExternalSubscription e = this.get(key);
							if (e != null && e.getCalendar() != null)
							{
								Calendar c = e.getCalendar();
								e.setCalendar(null);
								this.put(key, e);
								m_log
										.debug("Cleared cache for expired Calendar Subscription: "
												+ key);
								synchronized(listener)
								{
									if (listener != null)
									{
										listener.subscriptionExpired(key, e);
									}
								}
							}
						}
					}

					// sleep if no work to do
					if (!threadCleanerRunning) break;
					try
					{
						synchronized (threadCleanerRunningSemaphore)
						{
							threadCleanerRunningSemaphore.wait(maxCachedTime);
						}
					}
					catch (InterruptedException e)
					{
						m_log.warn("Failed to sleep SmallCacheMap entry cleaner thread",
								e);
					}
				}
			}
			catch (Throwable t)
			{
				m_log.debug("Failed to execute SmallCacheMap entry cleaner thread", t);
			}
			finally
			{
				if (threadCleanerRunning)
				{
					// thread was stopped by an unknown error: restart
					m_log
							.debug("SmallCacheMap entry cleaner thread was stoped by an unknown error: restarting...");
					startCleanerThread();
				}
				else
					m_log.debug("Finished SmallCacheMap entry cleaner thread");
			}
		}

		/** Start the update thread */
		private void startCleanerThread()
		{
			threadCleanerRunning = true;
			threadCleaner = null;
			threadCleaner = new Thread(this, this.getClass().getName());
			threadCleaner.start();
		}

		/** Stop the update thread */
		private void stopCleanerThread()
		{
			threadCleanerRunning = false;
			synchronized (threadCleanerRunningSemaphore)
			{
				threadCleanerRunningSemaphore.notifyAll();
			}
		}
	}

	interface SubscriptionExpiredListener
	{
		public void subscriptionExpired(String subscriptionUrl,
				ExternalSubscription subscription);
	}

	class SubscriptionReloadOnExpiredListener implements SubscriptionExpiredListener
	{
		public void subscriptionExpired(String subscriptionUrl,
				ExternalSubscription subscription)
		{
			if (institutionalSubscriptions.containsKey(subscriptionUrl))
			{
				// if is a Institutional calendar, re-load expired
				m_log.debug("Re-loading institutional calendar: " + subscriptionUrl);
				reloadInstitutionalSubscription(subscriptionUrl, subscription
						.getContext());
			}
			else
			{
				// if is a User-specified calendar, re-load expired
				m_log.debug("Re-loading user-specified calendar: " + subscriptionUrl);
				ExternalSubscription s = loadCalendarSubscriptionFromUrl(subscriptionUrl,
						subscription.getContext());
				if (subscription != null)
					userSubscriptions.put(subscriptionUrl, subscription);
			}
		}
	}
}
