package org.sakaiproject.signup.logic;

import java.util.Date;

import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;

import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.util.PlainTextFormat;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;

/**
 * Impl of SignupCalendarHelper
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@CommonsLog
public class SignupCalendarHelperImpl implements SignupCalendarHelper {

	@Override
	public CalendarEventEdit generateEvent(SignupMeeting m) {
		return generateEvent(m, null);
	}
	
	@Override
	public CalendarEventEdit generateEvent(SignupMeeting m, SignupTimeslot ts) {
		
		//only interested in first site
		String siteId = m.getSignupSites().get(0).getSiteId();
		
		Date start;
		Date end;
		//use timeslot if set, otherwise use meeting
		if(ts == null) {
			start = m.getStartTime();
			end = m.getEndTime();
		} else {
			start = ts.getStartTime();
			end = ts.getEndTime();
		}
		String title = m.getTitle();
		String description = m.getDescription();
		String location = m.getLocation();
		
		//note that there is no way to set the creator unless the user creating the event is the current session user
		//and sometimes this is not the case, esp for transient events that are not persisted
			
		return generateEvent(siteId,start,end,title,description,location);
	}
	
	
	/**
	 * Helper to generate a calendar event from some pieces of data
	 * @param siteId
	 * @param startTime
	 * @param endTime
	 * @param title
	 * @param description
	 * @param location
	 * @return
	 */
	private CalendarEventEdit generateEvent(String siteId, Date startTime, Date endTime, String title, String description, String location) {
		
		Calendar calendar;
		CalendarEventEdit event = null;
		try {
			calendar = sakaiFacade.getCalendar(siteId);
		
			if (calendar == null) {
				return null;
			}
			
			event = calendar.addEvent();
			event.setType("Meeting");
			
			//time range for this timeslot
			TimeService timeService = sakaiFacade.getTimeService();
			Time start = timeService.newTime(startTime.getTime());
			Time end = timeService.newTime(endTime.getTime());
			TimeRange timeRange = timeService.newTimeRange(start, end, true, false);
			event.setRange(timeRange);
			
			//NOTE: these pieces of data may need adjusting so that its obvious this is a timeslot within the meeting
			event.setDisplayName(title);
			event.setDescription(PlainTextFormat.convertFormattedHtmlTextToPlaintext(description));
			event.setLocation(location);
		} catch (PermissionException e) {
			e.printStackTrace();
			return null;
		} catch (IdUnusedException e) {
			log.warn("Site " + siteId + " does not have calendar tool. Cannot proceed");
			return null;
		}
		
		return event;
	}
	
	@Setter
	private SakaiFacade sakaiFacade;

}
