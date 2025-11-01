/**
 * Copyright (c) 2003-2024 The Apereo Foundation
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
package org.sakaiproject.calendar.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.calendar.api.CalendarEdit;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.calendar.impl.BaseCalendarService.Storage;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;

@RunWith(MockitoJUnitRunner.class)
public class BaseCalendarServiceHardDeleteTest
{
        private static final String SITE_ID = "site-id";

        @Mock
        private CalendarEdit calendarEdit;

        @Mock
        private CalendarEvent calendarEvent;

        @Mock
        private CalendarEventEdit calendarEventEdit;

        private HardDeleteTestCalendarService calendarService;

        @Before
        public void setUp()
        {
                calendarService = new HardDeleteTestCalendarService();
        }

        @Test
        public void hardDeleteRemovesCalendar() throws Exception
        {
                when(calendarEvent.getId()).thenReturn("event-1");
                when(calendarEdit.getEvents(isNull(), isNull())).thenReturn(Collections.singletonList(calendarEvent));
                when(calendarEdit.getEditEvent("event-1", CalendarService.EVENT_REMOVE_CALENDAR)).thenReturn(calendarEventEdit);

                calendarService.setCalendarEdit(calendarEdit);

                calendarService.hardDelete(SITE_ID);

                verify(calendarEdit).removeEvent(calendarEventEdit);
                assertTrue(calendarService.wasRemoveCalendarAttempted());
                assertTrue(calendarService.wasCalendarRemoved());
                assertFalse(calendarService.wasCalendarCancelled());
        }

        @Test
        public void hardDeleteHandlesMissingCalendar()
        {
                calendarService.setCalendarEdit(null);

                calendarService.hardDelete(SITE_ID);

                assertFalse(calendarService.wasRemoveCalendarAttempted());
                assertFalse(calendarService.wasCalendarRemoved());
                assertFalse(calendarService.wasCalendarCancelled());
        }

        @Test
        public void hardDeleteCancelsWhenRemovalFails() throws Exception
        {
                when(calendarEdit.getEvents(isNull(), isNull())).thenReturn(Collections.emptyList());

                calendarService.setCalendarEdit(calendarEdit);
                calendarService.setRemoveCalendarException(new PermissionException("user", "lock", "resource"));

                calendarService.hardDelete(SITE_ID);

                assertTrue(calendarService.wasRemoveCalendarAttempted());
                assertFalse(calendarService.wasCalendarRemoved());
                assertTrue(calendarService.wasCalendarCancelled());
        }

        private static class HardDeleteTestCalendarService extends BaseCalendarService
        {
                private CalendarEdit calendarEdit;
                private boolean removeAttempted;
                private boolean removed;
                private boolean cancelled;
                private PermissionException removeCalendarException;

                HardDeleteTestCalendarService()
                {
                        m_relativeAccessPoint = "/calendar";
                }

                void setCalendarEdit(CalendarEdit calendarEdit)
                {
                        this.calendarEdit = calendarEdit;
                }

                void setRemoveCalendarException(PermissionException exception)
                {
                        this.removeCalendarException = exception;
                }

                boolean wasRemoveCalendarAttempted()
                {
                        return removeAttempted;
                }

                boolean wasCalendarRemoved()
                {
                        return removed;
                }

                boolean wasCalendarCancelled()
                {
                        return cancelled;
                }

                @Override
                protected Storage newStorage()
                {
                        return null;
                }

                @Override
                public CalendarEdit editCalendar(String ref) throws IdUnusedException, PermissionException, InUseException
                {
                        if (calendarEdit == null)
                        {
                                throw new IdUnusedException(ref);
                        }
                        return calendarEdit;
                }

                @Override
                public void removeCalendar(CalendarEdit calendar) throws PermissionException
                {
                        removeAttempted = true;
                        if (removeCalendarException != null)
                        {
                                throw removeCalendarException;
                        }
                        removed = true;
                }

                @Override
                public void cancelCalendar(CalendarEdit edit)
                {
                        cancelled = true;
                }
        }
}
