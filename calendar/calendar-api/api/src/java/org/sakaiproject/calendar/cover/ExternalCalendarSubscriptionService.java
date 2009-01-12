package org.sakaiproject.calendar.cover;

import java.util.Collection;
import java.util.Set;

import org.sakaiproject.calendar.api.ExternalSubscription;
import org.sakaiproject.component.cover.ComponentManager;


public class ExternalCalendarSubscriptionService {

	/**
	 * Access the component instance: special cover only method.
	 * @return the component instance.
	 */
	public static org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null) m_instance = (org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService) ComponentManager.get(org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService) ComponentManager.get(org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService.class);
		}
	}
	private static org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService m_instance = null;
	
	public static String SAK_PROP_EXTSUBSCRIPTIONS_ENABLED	= org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService.SAK_PROP_EXTSUBSCRIPTIONS_ENABLED;
	public static String TC_PROP_SUBCRIPTIONS				= org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService.TC_PROP_SUBCRIPTIONS;
	public static String SUBS_REF_DELIMITER					= org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService.SUBS_REF_DELIMITER;
	public static String SUBS_NAME_DELIMITER				= org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService.SUBS_NAME_DELIMITER;
	
	public static String calendarSubscriptionReference(String context, String id)
	{

		org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService service = getInstance();
		if (service == null)
			return null;

		return service.calendarSubscriptionReference(context, id);
	
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.calendar.impl.ExternalCalendarSubscriptionService#isEnabled()
	 */
	public static boolean isEnabled() 
	{
		org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService service = getInstance();
		if (service == null)
			return false;

		return service.isEnabled();
	
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.calendar.impl.ExternalCalendarSubscriptionService#getCalendarSubscription(java.lang.String)
	 */
	public static org.sakaiproject.calendar.api.Calendar getCalendarSubscription(String reference)
	{
		org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService service = getInstance();
		if (service == null)
			return null;

		return service.getCalendarSubscription(reference);	
	}
	
	public static Set<String> getCalendarSubscriptionChannelsForChannels(String primaryCalendarReference, Collection<Object> channels)
	{
		org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService service = getInstance();
		if (service == null)
			return null;

		return service.getCalendarSubscriptionChannelsForChannels(primaryCalendarReference, channels);	
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.calendar.impl.ExternalCalendarSubscriptionService#getCalendarSubscriptionChannelsForChannel(java.lang.String)
	 */
	public static Set<String> getCalendarSubscriptionChannelsForChannel(String reference)
	{
		org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService service = getInstance();
		if (service == null)
			return null;

		return service.getCalendarSubscriptionChannelsForChannel(reference);	
	}
	
	public static Set<ExternalSubscription> getAvailableInstitutionalSubscriptionsForChannel(String reference)
	{
		org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService service = getInstance();
		if (service == null)
			return null;

		return service.getAvailableInstitutionalSubscriptionsForChannel(reference);	
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.calendar.impl.ExternalCalendarSubscriptionService#getSubscriptionsForChannel(java.lang.String, boolean)
	 */
	public static Set<ExternalSubscription> getSubscriptionsForChannel(String reference, boolean loadCalendar)
	{
		org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService service = getInstance();
		if (service == null)
			return null;

		return service.getSubscriptionsForChannel(reference, loadCalendar);	
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.calendar.impl.ExternalCalendarSubscriptionService#setCalendarSubscriptionsForSite(java.lang.String, java.util.Collection)
	 */
	public static void setSubscriptionsForChannel(String reference, java.util.Collection<ExternalSubscription> subscriptions)
	{
		org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService service = getInstance();
		if (service != null)
			setSubscriptionsForChannel(reference, subscriptions);	
	}
	
	public static String getIdFromSubscriptionUrl(String url)
	{
		org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService service = getInstance();
		if (service == null)
			return null;

		return service.getIdFromSubscriptionUrl(url);	
	}
	
	public static String getSubscriptionUrlFromId(String id)
	{
		org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService service = getInstance();
		if (service == null)
			return null;

		return service.getSubscriptionUrlFromId(id);	
	}
	
	public static boolean isInstitutionalCalendar(String reference)
	{
		org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService service = getInstance();
		if (service == null)
			return false;

		return service.isInstitutionalCalendar(reference);
	}
}
