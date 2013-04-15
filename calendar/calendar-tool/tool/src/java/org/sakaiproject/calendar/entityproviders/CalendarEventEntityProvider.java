package org.sakaiproject.calendar.entityproviders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Setter;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Sampleable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

/**
 * The sakai entity used to access calendar events.
 * 
 * @author Denny
 */
public class CalendarEventEntityProvider extends AbstractEntityProvider
		implements AutoRegisterEntityProvider, Describeable, EntityProvider,
		ActionsExecutable, Outputable, Sampleable {

	String ENTITY_PREFIX = "calendar";

	/**
	 * Calendar service.
	 */
	@Setter
	private transient CalendarService calendarService;

	/**
	 * Reference to site service.
	 */
	@Setter
	private SiteService siteService;

	/**
	 * Reference to the developer helper service.
	 */
	@Setter
	private DeveloperHelperService developerHelperService;

	/**
	 * @return prefix
	 */
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	/**
	 * @return formats
	 */
	public String[] getHandledInputFormats() {
		return new String[] { Formats.JSON };
	}

	/**
	 * @return formats
	 */
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.JSON, Formats.XML, Formats.HTML };
	}

	public Object getSampleEntity() {
		return new CalendarEventSummary();
	}

	/**
	 * Checks if an entity exists.
	 * 
	 * @param id
	 *            id
	 * @return if entity exists
	 */
	public boolean entityExists(final String id) {
		return false;
	}

	/**
	 * site/siteId
	 */
	@EntityCustomAction(action = "site", viewKey = EntityView.VIEW_LIST)
	public List<CalendarEventSummary> getCalendarEventsForSite(EntityView view) {
		List<CalendarEventSummary> r = new ArrayList<CalendarEventSummary>();

		// get siteId
		String siteId = view.getPathSegment(2);

		// check siteId supplied
		if (StringUtils.isBlank(siteId)) {
			throw new IllegalArgumentException(
					"siteId must be set in order to get the news feeds for a site, via the URL /news/site/siteId");
		}

		// user being logged in and having access to the site is handled in the
		// API
		Calendar cal;
		try {
			cal = calendarService.getCalendar(String.format(
					"/calendar/calendar/%s/main", siteId));

			for (Object o : cal.getEvents(null, null)) {
				CalendarEvent event = (CalendarEvent) o;

				r.add(new CalendarEventSummary(event));
			}

			return r;
		} catch (IdUnusedException e) {
			throw new EntityNotFoundException("Invalid siteId: " + siteId,
					siteId);
		} catch (PermissionException e) {
			throw new EntityNotFoundException("No access to site: " + siteId,
					siteId);
		}
	}

	/**
	 * my
	 */
	@EntityCustomAction(action = "my", viewKey = EntityView.VIEW_LIST)
	public List<CalendarEventSummary> getMyCalendarEventsForAllSite(
			EntityView view, Map<String, Object> params) {
		List<CalendarEventSummary> rv = new ArrayList<CalendarEventSummary>();

		// add events form my workspace
		Calendar cal;
		try {
			cal = calendarService.getCalendar(String.format(
					"/calendar/calendar/~%s/main",
					developerHelperService.getCurrentUserId()));

			for (Object o : cal.getEvents(null, null)) {
				CalendarEvent event = (CalendarEvent) o;

				rv.add(new CalendarEventSummary(event));
			}
		} catch (IdUnusedException e) {
			// should not happened
		} catch (PermissionException e) {
			// should not happened
		}

		// get list of all sites
		List<Site> sites = siteService.getSites(
				SiteService.SelectionType.ACCESS, null, null, null,
				SiteService.SortType.TITLE_ASC, null);
		// no need to check user can access this site, as the get sites only
		// returned accessible sites

		// get all assignments from each site
		for (Site site : sites) {
			String siteId = site.getId();

			try {
				cal = calendarService.getCalendar(String.format(
						"/calendar/calendar/%s/main", siteId));

				for (Object o : cal.getEvents(null, null)) {
					CalendarEvent event = (CalendarEvent) o;

					rv.add(new CalendarEventSummary(event));
				}
			} catch (IdUnusedException e) {
				// should not happened
			} catch (PermissionException e) {
				// should not happened
			}

		}
		return rv;
	}

	/**
	 * event/siteId/eventId
	 */
	@EntityCustomAction(action = "event", viewKey = EntityView.VIEW_LIST)
	public CalendarEventDetails getCalendarEventDetails(EntityView view) {
		// get siteId
		String siteId = view.getPathSegment(2);
		String eventId = view.getPathSegment(3);

		// check siteId supplied
		if (StringUtils.isBlank(siteId)) {
			throw new IllegalArgumentException(
					"siteId must be set in order to get the news feeds for a site, via the URL /news/site/siteId");
		}

		// user being logged in and having access to the site is handled in the
		// API
		Calendar cal;
		try {
			cal = calendarService.getCalendar(String.format(
					"/calendar/calendar/%s/main", siteId));

			return new CalendarEventDetails(cal.getEvent(eventId));
		} catch (IdUnusedException e) {
			throw new EntityNotFoundException("Invalid siteId: " + siteId,
					siteId);
		} catch (PermissionException e) {
			throw new EntityNotFoundException("No access to site: " + siteId,
					siteId);
		}
	}

}
