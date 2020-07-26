/**
 * Copyright (c) 2006-2019 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.impl.event.detailed;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import lombok.Setter;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.api.app.podcasts.PodcastService;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.event.detailed.DetailedEvent;
import org.sakaiproject.sitestats.api.event.detailed.DetailedEventsManager;
import org.sakaiproject.sitestats.api.event.detailed.PagingParams;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;
import org.sakaiproject.sitestats.api.event.detailed.SortingParams;
import org.sakaiproject.sitestats.api.event.detailed.TrackingParams;
import org.sakaiproject.sitestats.impl.DetailedEventImpl;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.AnnouncementReferenceResolver;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.AssignmentReferenceResolver;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.CalendarReferenceResolver;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.ContentReferenceResolver;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.LessonsReferenceResolver;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.MsgForumsReferenceResolver;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.NewsReferenceResolver;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.PodcastReferenceResolver;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.PollReferenceResolver;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.SamigoReferenceResolver;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.WebContentReferenceResolver;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.WikiReferenceResolver;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.PublishedItemService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;

import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiSecurityService;

/**
 *
 * @author plukasew, bjones86, bbailla2
 */
@Slf4j
public class DetailedEventsManagerImpl extends HibernateDaoSupport implements DetailedEventsManager
{
	private static final String USER_ID_COL = "userId";
	private static final String EVENT_ID_COL = "eventId";
	private static final String EVENT_DATE_COL = "eventDate";
	private static final String SITE_ID_COL = "siteId";

	private static final String HQL_BY_ID = "SELECT de.id, de.userId, de.eventDate, de.eventId, de.eventRef, de.siteId FROM DetailedEventImpl as de WHERE de.id = :id";

	@Setter private StatsManager statMan;
	@Setter private AssignmentService asnServ;
	@Setter private SimplePageToolDao lsnServ;
	@Setter private DiscussionForumManager forumMan;
	@Setter private UIPermissionsManager forumPermMan;
	@Setter private EntityBroker broker;
	@Setter private DeveloperHelperService devHlprServ;
	@Setter private PollListManager pollServ;
	@Setter private AnnouncementService anncServ;
	@Setter private CalendarService calServ;
	@Setter private PodcastService podServ;
	@Setter private StatsAuthz statsAuthz;
	@Setter private RWikiSecurityService wikiAuthz;
	@Setter private EventRegistryService regServ;
	@Setter private SiteService siteServ;
	@Setter private ContentHostingService contentHostServ;
	@Setter private AuthzGroupService authzServ;
	// Samigo services cannot be injected by Spring
	private AssessmentService assessServ;
	private PublishedAssessmentService pubAssessServ;
	private GradingService samGradeServ;
	private ItemService samItemServ;
	private PublishedItemService samPubItemServ;

	public void init()
	{
		boolean testsEnabled = BooleanUtils.toBoolean(System.getProperty("sakai.tests.enabled"));
		if (!testsEnabled) {
			assessServ = new AssessmentService();
			pubAssessServ = new PublishedAssessmentService();
			samGradeServ = new GradingService();
			samItemServ = new ItemService();
			samPubItemServ = new PublishedItemService();
		}
	}

	/* End Spring methods */

	private Optional<Criteria> basicCriteriaForTrackingParams(Session session, final TrackingParams params)
	{
		Criteria crit = session.createCriteria(DetailedEventImpl.class);
		if (StringUtils.isNotBlank(params.siteId))
		{
			crit.add(Restrictions.eq(SITE_ID_COL, params.siteId));
		}
		if (!params.events.isEmpty())
		{
			crit.add(Restrictions.in(EVENT_ID_COL, params.events));
		}

		// Filter out any users who do not have the can be tracked permission in the site
		List<String> filtered = params.userIds.stream()
				.filter(u -> statsAuthz.canUserBeTracked(params.siteId, u))
				.collect(Collectors.toList());
		// must have at least one user
		if (filtered.isEmpty())
		{
			return Optional.empty();
		}
		crit.add(Restrictions.in(USER_ID_COL, filtered));

		if (!TrackingParams.NO_DATE.equals(params.startDate))
		{
			crit.add(Restrictions.ge(EVENT_DATE_COL, Date.from(params.startDate)));
		}
		if (!TrackingParams.NO_DATE.equals(params.endDate))
		{
			crit.add(Restrictions.lt(EVENT_DATE_COL, Date.from(params.endDate)));
		}

		// filter out anonymous events
		Set<String> anonEvents = regServ.getAnonymousEventIds();
		if (!anonEvents.isEmpty())
		{
			crit.add(Restrictions.not(Restrictions.in(EVENT_ID_COL, anonEvents)));
		}

		return Optional.of(crit);
	}

	@Override
	public List<DetailedEvent> getDetailedEvents(final TrackingParams trackingParams, final PagingParams pagingParams, final SortingParams sortingParams)
	{
		if (!statMan.isDisplayDetailedEvents() || !statsAuthz.canCurrentUserTrackInSite(trackingParams.siteId))
		{
			return Collections.emptyList();
		}

		HibernateCallback<List<DetailedEvent>> hcb = session ->
		{
			Optional<Criteria> critOpt = basicCriteriaForTrackingParams(session, trackingParams);
			if (!critOpt.isPresent())
			{
				return Collections.emptyList();
			}
			Criteria crit = critOpt.get();

			if (pagingParams.startInt >= 0 && pagingParams.pageSizeInt > 0)
			{
				crit.setFirstResult(pagingParams.startInt);
				crit.setMaxResults(pagingParams.pageSizeInt);
			}

			if (sortingParams != null && StringUtils.isNotBlank(sortingParams.sortProp))
			{
				String sortProp = sortingParams.sortProp;
				crit.addOrder(sortingParams.asc ? Order.asc(sortProp) : Order.desc(sortProp));
			}

			List<DetailedEvent> results = crit.list();

			return results;
		};

		return (List<DetailedEvent>) getHibernateTemplate().execute(hcb);
	}

	@Override
	public Optional<DetailedEvent> getDetailedEventById(final long id)
	{
		if (!statMan.isDisplayDetailedEvents())
		{
			return Optional.empty();
		}

		HibernateCallback<Optional<DetailedEvent>> hcb = session ->
		{
			Query q = session.createQuery(HQL_BY_ID);
			q.setLong("id", id);
			if (log.isDebugEnabled())
			{
				log.debug("getDetailedEvents(): " + q.getQueryString());
			}

			List<Object[]> records = q.list();
			if (records.size() > 1)
			{
				log.error("getDetailedEvents(): query for id " + id + " returned more than one result.");
				return Optional.empty();
			}
			else if (records.isEmpty())
			{
				return Optional.empty();
			}

			Object[] record = records.get(0);
			String userID = (String) record[1];
			String siteID = (String) record[5];
			// Only return the event if the current user is is allowed to track, and the target user is allowed to be tracked in the site
			if (statsAuthz.canCurrentUserTrackInSite(siteID) && statsAuthz.canUserBeTracked(siteID, userID))
			{
				DetailedEvent de = new DetailedEventImpl();
				de.setId((Long) record[0]);
				de.setUserId(userID);
				de.setEventDate((Date) record[2]);
				de.setEventId((String) record[3]);
				de.setEventRef((String) record[4]);
				de.setSiteId(siteID);

				// do not return if anonymous
				if (!regServ.getAnonymousEventIds().contains(de.getEventId()))
				{
					return Optional.of(de);
				}
			}

			return Optional.empty();
		};

		return getHibernateTemplate().execute(hcb);

	}

	@Override
	public boolean isResolvable(String eventType)
	{
		return regServ.isResolvableEvent(eventType);
	}

	@Override
	public ResolvedEventData resolveEventReference(String eventType, String eventRef, String siteID)
	{
		if (!statMan.isDisplayDetailedEvents() || !statsAuthz.canCurrentUserTrackInSite(siteID) || !isResolvable(eventType))
		{
			return ResolvedEventData.ERROR;
		}

		Map<String, ToolInfo> toolMap = regServ.getEventIdToolMap();
		Map<String, EventInfo> eventMap = regServ.getEventIdEventMap();
		ToolInfo tool = toolMap.get(eventType);
		EventInfo event = eventMap.get(eventType);
		if (tool != null && event != null)
		{
			String toolId = StringUtils.trimToEmpty(tool.getToolId());
			switch (toolId)
			{
				case PollReferenceResolver.TOOL_ID:
					return PollReferenceResolver.resolveReference(eventRef, tool.getEventParserTips(), pollServ);
				case WikiReferenceResolver.TOOL_ID:
					// Wiki services perform no permission checks so we ensure the user has admin permissions in Wiki
					// before allowing them to see all wiki details
					if (wikiAuthz.checkAdminPermission(siteServ.siteReference(siteID)))
					{
						return WikiReferenceResolver.resolveReference(eventRef, devHlprServ, tool.getEventParserTips(), siteServ);
					}
					return ResolvedEventData.PERM_ERROR;
				case NewsReferenceResolver.TOOL_ID:
					return NewsReferenceResolver.resolveReference(eventRef, tool.getEventParserTips(), siteServ);
				case AnnouncementReferenceResolver.TOOL_ID:
					return AnnouncementReferenceResolver.resolveReference(eventRef, tool.getEventParserTips(), anncServ);
				case CalendarReferenceResolver.TOOL_ID:
					return CalendarReferenceResolver.resolveReference(eventType, eventRef, tool.getEventParserTips(), calServ);
				case MsgForumsReferenceResolver.FORUMS_TOOL_ID:
				case MsgForumsReferenceResolver.MESSAGES_TOOL_ID:
					return MsgForumsReferenceResolver.resolveEventReference(eventType, eventRef, tool.getEventParserTips(),
							forumMan, forumPermMan, broker);
				case LessonsReferenceResolver.TOOL_ID:
					// Lessons services perform no permission checks so we ensure the user has update permissions in Lessons
					// before allowing them to see all lessons details
					if (PersistenceService.getInstance().getAuthzQueriesFacade().hasPrivilege(SimplePage.PERMISSION_LESSONBUILDER_UPDATE))
					{
						return LessonsReferenceResolver.resolveReference(eventRef, tool.getEventParserTips(), lsnServ);
					}
					return ResolvedEventData.PERM_ERROR;
				case AssignmentReferenceResolver.TOOL_ID:
					return AssignmentReferenceResolver.resolveReference(eventRef, asnServ, siteServ);
				case PodcastReferenceResolver.TOOL_ID:
					return PodcastReferenceResolver.resolveReference(eventRef, tool.getEventParserTips(), podServ);
				case ContentReferenceResolver.DROPBOX_TOOL_ID:
				case ContentReferenceResolver.RESOURCES_TOOL_ID:
					return ContentReferenceResolver.resolveReference(eventRef, tool.getEventParserTips(), contentHostServ);
				case WebContentReferenceResolver.IFRAME_MYWORKSPACE_TOOL_ID:
				case WebContentReferenceResolver.IFRAME_SERVICE_TOOL_ID:
				case WebContentReferenceResolver.IFRAME_SITE_TOOL_ID:
				case WebContentReferenceResolver.WEB_TOOL_ID:
					return WebContentReferenceResolver.resolveReference(eventRef, tool.getEventParserTips(), siteServ);
				case SamigoReferenceResolver.TOOL_ID:
					// Samigo services perform no permission checks so we ensure the user has grading permissions in Samigo before
					// allowing them to see quiz info
					if (PersistenceService.getInstance().getAuthzQueriesFacade().hasPrivilege(SamigoConstants.AUTHZ_GRADE_ASSESSMENT_ANY))
					{
						return SamigoReferenceResolver.resolveReference(eventType, eventRef, assessServ, pubAssessServ, samGradeServ, samItemServ, samPubItemServ);
					}
					return ResolvedEventData.PERM_ERROR;
			}
		}

		return ResolvedEventData.ERROR;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public List<String> getUsersForTracking(String siteID)
	{
		String realmID = siteServ.siteReference(siteID);
		try
		{
			AuthzGroup realm = authzServ.getAuthzGroup(realmID);
			Set<Member> members = realm.getMembers();

			// Filter out any users that do not have the 'be tracked' permission
			return members.stream().map(Member::getUserId)
					.filter(u -> statsAuthz.canUserBeTracked(siteID, u)).collect(Collectors.toList());
		}
		catch (GroupNotDefinedException ex)
		{
			log.warn("Unable to get group for realm, ID = " + realmID, ex);
			return Collections.emptyList();
		}
	}
}
