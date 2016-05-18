package org.sakaiproject.calendar.entityproviders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarService;
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
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;

import lombok.Setter;

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
	 * Time service.
	 */
	@Setter
	private transient TimeService timeService;

	/**
	 * @return prefix
	 */
	@Override
	public String getEntityPrefix() {
		return this.ENTITY_PREFIX;
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
	@Override
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.JSON, Formats.XML, Formats.HTML };
	}

	@Override
	public Object getSampleEntity() {
		return new CalendarEventSummary();
	}

	/**
	 * Checks if an entity exists.
	 *
	 * @param id id
	 * @return if entity exists
	 */
	public boolean entityExists(final String id) {
		return false;
	}

	/**
	 * site/siteId
	 *
	 * OPtion firstDate and lastDate query params in ISO-8601 date format, YYYY-MM-DD
	 */
	@EntityCustomAction(action = "site", viewKey = EntityView.VIEW_LIST)
	public List<CalendarEventSummary> getCalendarEventsForSite(final EntityView view, final Map<String, Object> params) {

		final List<CalendarEventSummary> r = new ArrayList<CalendarEventSummary>();

		// get siteId
		final String siteId = view.getPathSegment(2);

		// OPTIONAL params
		String firstDate = null;
		String lastDate = null;
		if (params.containsKey("firstDate")) {
			firstDate = (String) params.get("firstDate");
		}
		if (params.containsKey("lastDate")) {
			lastDate = (String) params.get("lastDate");
		}

		// check siteId supplied
		if (StringUtils.isBlank(siteId)) {
			throw new IllegalArgumentException(
					"siteId must be set in order to get the calendar feeds for a site, via the URL /calendar/site/siteId");
		}

		// check date params are correct (length is ok)
		if (StringUtils.isNotBlank(firstDate) && StringUtils.length(firstDate) != 10) {
			throw new IllegalArgumentException(
					"firstDate must be in the format yyyy-MM-dd");
		}
		if (StringUtils.isNotBlank(lastDate) && StringUtils.length(lastDate) != 10) {
			throw new IllegalArgumentException(
					"lastDate must be in the format yyyy-MM-dd");
		}

		// if we have dates, create a range for filtering
		// these need to be converted to Time and then a TimeRange created. This is what the CalendarService uses to filter :(
		// note that our firstDate is always the beginning of the day and the lastDate is always the end
		// this is to ensure we get full days
		TimeRange range = null;
		if (StringUtils.isNotBlank(firstDate) && StringUtils.isNotBlank(lastDate)) {
			final Time start = this.timeService.newTimeLocal(
					Integer.valueOf(StringUtils.substring(firstDate, 0, 4)),
					Integer.valueOf(StringUtils.substring(firstDate, 5, 7)),
					Integer.valueOf(StringUtils.substring(firstDate, 8, 10)),
					0, 0, 0, 0);

			final Time end = this.timeService.newTimeLocal(
					Integer.valueOf(StringUtils.substring(lastDate, 0, 4)),
					Integer.valueOf(StringUtils.substring(lastDate, 5, 7)),
					Integer.valueOf(StringUtils.substring(lastDate, 8, 10)),
					23, 59, 59, 999);

			range = this.timeService.newTimeRange(start, end, true, true);
		}

		// user being logged in and having access to the site is handled in the
		// API
		Calendar cal;
		try {
			cal = this.calendarService.getCalendar(String.format(
					"/calendar/calendar/%s/main", siteId));

			for (final Object o : cal.getEvents(range, null)) {
				final CalendarEvent event = (CalendarEvent) o;

				r.add(new CalendarEventSummary(event));
			}

			return r;
		} catch (final IdUnusedException e) {
			throw new EntityNotFoundException("Invalid siteId: " + siteId,
					siteId);
		} catch (final PermissionException e) {
			throw new EntityNotFoundException("No access to site: " + siteId,
					siteId);
		}
	}

	/**
	 * my
	 */
	@EntityCustomAction(action = "my", viewKey = EntityView.VIEW_LIST)
	public List<CalendarEventSummary> getMyCalendarEventsForAllSite(
			final EntityView view, final Map<String, Object> params) {
		final List<CalendarEventSummary> rv = new ArrayList<CalendarEventSummary>();

		// add events form my workspace
		Calendar cal;
		try {
			cal = this.calendarService.getCalendar(String.format(
					"/calendar/calendar/~%s/main",
					this.developerHelperService.getCurrentUserId()));

			for (final Object o : cal.getEvents(null, null)) {
				final CalendarEvent event = (CalendarEvent) o;

				rv.add(new CalendarEventSummary(event));
			}
		} catch (final IdUnusedException e) {
			// should not happened
		} catch (final PermissionException e) {
			// should not happened
		}

		// get list of all sites
		final List<Site> sites = this.siteService.getSites(
				SiteService.SelectionType.ACCESS, null, null, null,
				SiteService.SortType.TITLE_ASC, null);
		// no need to check user can access this site, as the get sites only
		// returned accessible sites

		// get all assignments from each site
		for (final Site site : sites) {
			final String siteId = site.getId();

			try {
				cal = this.calendarService.getCalendar(String.format(
						"/calendar/calendar/%s/main", siteId));

				for (final Object o : cal.getEvents(null, null)) {
					final CalendarEvent event = (CalendarEvent) o;

					rv.add(new CalendarEventSummary(event));
				}
			} catch (final IdUnusedException e) {
				// should not happened
			} catch (final PermissionException e) {
				// should not happened
			}

		}
		return rv;
	}

	/**
	 * event/siteId/eventId
	 */
	@EntityCustomAction(action = "event", viewKey = EntityView.VIEW_LIST)
	public CalendarEventDetails getCalendarEventDetails(final EntityView view) {
		// get siteId
		final String siteId = view.getPathSegment(2);
		final String eventId = view.getPathSegment(3);

		// check siteId supplied
		if (StringUtils.isBlank(siteId)) {
			throw new IllegalArgumentException(
					"siteId must be set in order to get the calendar feeds for a site, via the URL /calendar/site/siteId");
		}

		// user being logged in and having access to the site is handled in the
		// API
		Calendar cal;
		try {
			cal = this.calendarService.getCalendar(String.format(
					"/calendar/calendar/%s/main", siteId));

			return new CalendarEventDetails(cal.getEvent(eventId));
		} catch (final IdUnusedException e) {
			throw new EntityNotFoundException("Invalid siteId: " + siteId,
					siteId);
		} catch (final PermissionException e) {
			throw new EntityNotFoundException("No access to site: " + siteId,
					siteId);
		}
	}

}
