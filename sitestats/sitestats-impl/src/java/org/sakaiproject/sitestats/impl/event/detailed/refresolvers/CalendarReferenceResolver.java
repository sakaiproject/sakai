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
package org.sakaiproject.sitestats.impl.event.detailed.refresolvers;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.calendar.api.RecurrenceRule;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;
import org.sakaiproject.sitestats.api.event.detailed.calendar.CalendarEntryData;
import org.sakaiproject.sitestats.api.parser.EventParserTip;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.parsers.CalendarRefParser;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.parsers.GenericRefParser;

/**
 * Resolves Calendar references into meaningful details.
 *
 * @author bjones86, plukasew
 */
@Slf4j
public class CalendarReferenceResolver
{
    private static final String CAL_READ = "calendar.read";

    public static final String TOOL_ID = "sakai.schedule";

    /**
     * Resolves the given reference into meaningful details
     * @param eventType the event type
     * @param eventRef the event reference
     * @param tips tips for parsing out the components of the reference
     * @param calServ the calendar service
     * @return a CalendarEntryData object, or ResolvedEventData.ERROR/PERM_ERROR/NO_DETAILS
     */
    public static ResolvedEventData resolveReference( final String eventType, final String eventRef, List<EventParserTip> tips, CalendarService calServ )
    {
        if( StringUtils.isBlank( eventRef ) || calServ == null )
        {
            log.warn( "Cannot resolve reference. Reference is null/empty or service(s) are not initialized." );
            return ResolvedEventData.ERROR;
        }

        GenericRefParser.GenericEventRef ref = CalendarRefParser.parse( eventType, eventRef, tips );
        String calendarRef = "/calendar/calendar/" + ref.contextId + "/";
        try
        {
            // 1. some calendar.read refs do not contain an event id and details can't be retrieved
            // however, this is normal and should not be treated the same as an error
            // 2. calendar.read refs do not contain a calendar id, but we can still find the event
            // if there is only one calendar for the site
            if ( CAL_READ.equals( eventType ) )
            {
                // we have a site id but no calendar id, try to find the calendar by site only
                List<String> calendars = calServ.getCalendarReferences( ref.contextId );
                if ( ref.entityId.isEmpty() || calendars.size() != 1 )
                {
                    return ResolvedEventData.NO_DETAILS;
                }

                calendarRef = calendars.get( 0 );
            }

            Calendar cal = calServ.getCalendar( calendarRef );
            if( cal != null )
            {
                CalendarEvent calEvent = cal.getEvent(ref.entityId);
                if( calEvent != null )
                {
                    int interval = 1;
                    String frequencyUnit = CalendarEntryData.FREQ_ONCE;
                    RecurrenceRule recRule = calEvent.getRecurrenceRule();
                    if( recRule != null )
                    {
                        interval = recRule.getInterval();
                        frequencyUnit = recRule.getFrequencyDescription();
                    }

                    CalendarEntryData calEntryData = new CalendarEntryData( calEvent.getDisplayName(), calEvent.getRange(), interval, frequencyUnit );
                    return calEntryData;
                }
            }
        }
        catch( IdUnusedException iue )
        {
            log.warn( "Unable to retrieve calendar/event.", iue );
        }
        catch( PermissionException pe )
        {
            log.warn( "Permission exception trying to retrieve calendar/event.", pe );
            return ResolvedEventData.PERM_ERROR;
        }

        log.warn( "Unable to retrieve data; ref = " + eventRef );
        return ResolvedEventData.ERROR;
    }
}
