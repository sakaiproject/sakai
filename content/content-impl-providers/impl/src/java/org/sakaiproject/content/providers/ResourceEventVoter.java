package org.sakaiproject.content.providers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.EventVoter;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * Votes for/against an event based on the availability of the associated resource. If an event is
 * voted against, the event is serialized and scheduled to be rerun at the release date of the
 * resource.
 */
public class ResourceEventVoter implements EventVoter, ScheduledInvocationCommand
{
	private static final Log LOG = LogFactory.getLog(ResourceEventVoter.class);

	private ContentHostingService contentHostingService;
	private EntityManager entityManager;
	private EventTrackingService eventService;
	private TimeService timeService;
	private UsageSessionService usageSessionService;
	private SessionManager sessionManager;
	private UserDirectoryService userDirectoryService;
	private ResourceEventVoterHelper helper;

	public void setContentHostingService(ContentHostingService contentHostingService)
	{
		this.contentHostingService = contentHostingService;
	}

	public void setEventService(EventTrackingService eventService)
	{
		this.eventService = eventService;
	}

	public void setEntityManager(EntityManager entityManager)
	{
		this.entityManager = entityManager;
	}

	public void setTimeService(TimeService timeService)
	{
		this.timeService = timeService;
	}

	public void setUsageSessionService(UsageSessionService usageSessionService)
	{
		this.usageSessionService = usageSessionService;
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService)
	{
		this.userDirectoryService = userDirectoryService;
	}

	public void setSessionManager(SessionManager sessionManager)
	{
		this.sessionManager = sessionManager;
	}

	public void setHelper(ResourceEventVoterHelper helper)
	{
		this.helper = helper;
	}

	public void init()
	{
		eventService.addVoter(this);
	}

	/**
	 * Votes against an event if the associated resource is not yet available. Only looks at
	 * specific events from content hosting (resource add, resource remove, resource write).
	 */
	public boolean vote(Event event)
	{
		boolean retval = true;
		if (ContentHostingService.EVENT_RESOURCE_ADD.equals(event.getEvent())
				|| ContentHostingService.EVENT_RESOURCE_REMOVE.equals(event.getEvent())
				|| ContentHostingService.EVENT_RESOURCE_WRITE.equals(event.getEvent()))
		{
			Time now = timeService.newTime();
			Time releaseDate = getReleaseDate(event);

			// create the context for the delayed invocation
			String userId = event.getUserId();
			if (event.getUserId() == null)
			{
				UsageSession session = usageSessionService.getSession(event.getSessionId());
				if (session != null)
					userId = session.getUserId();
				else
					userId = sessionManager.getCurrentSessionUserId();
			}
			helper.deleteDelay(event);

			if (releaseDate != null && releaseDate.after(now))
			{
				LOG.info("Voting against event due to resource inavailability.");
				retval = false;
				// Schedule the new delayed invocation
				helper.createDelay(event, userId, releaseDate);
			}
		}
		return retval;
	}

	/**
	 * Deserializes the context into an event and refires the event.
	 */
	public void execute(String opaqueContext)
	{
		// need to instantiate components locally because this class is instantiated by the
		// scheduled invocation manager and not pulled from spring context.
		LOG.info("Refiring event that was delayed until resource is available.");
		Event event = helper.popEventDelay(opaqueContext);
		try
		{
			User user = userDirectoryService.getUser(event.getUserId());
			eventService.post(event, user);
		}
		catch (UserNotDefinedException unde)
		{
			// can't find the user so refire the event without user impersonation
			eventService.post(event);
		}
	}

	/**
	 * From DelayableEmailNotification. Allows notifications to be delayed until later. This
	 * implementation returns null as the default implementation has no delay. Should be overridden
	 * by subclasses if delay is required.
	 */
	protected Time getReleaseDate(final Event event)
	{
		Time releaseDate = null;
		Reference ref = entityManager.newReference(event.getResource());
		if (ref.getId() != null)
		{
			try
			{
				boolean isCollection = contentHostingService.isCollection(ref.getId());
				if (isCollection)
				{
					ContentCollection col = contentHostingService.getCollection(ref.getId());
					releaseDate = col.getReleaseDate();
				}
				else
				{
					ContentResource res = contentHostingService.getResource(ref.getId());
					releaseDate = res.getReleaseDate();
				}
			}
			catch (Exception te)
			{
				LOG.info("Unable to get resource info to check delivery delay parameters ["
						+ ref.getId() + "]");
			}
		}
		return releaseDate;
	}
}
