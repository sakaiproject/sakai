/**
 * Copyright (c) 2007-2014 The Apereo Foundation
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
package org.sakaiproject.signup.logic.messages;

import net.fortuna.ical4j.model.component.VEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.logic.SignupCalendarHelper;
import org.sakaiproject.signup.logic.SignupTrackingItem;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.user.api.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Ben Holmes
 */
public class CancellationEmailTest {

    protected SignupEmailBase _email;

    @Mock protected User _mockedUser;
    @Mock protected SignupMeeting _mockedMeeting;
    @Mock protected SignupTrackingItem _mockedItem;
    @Mock protected SignupAttendee _mockedAttendee;
    @Mock protected SakaiFacade _mockedFacade;
    @Mock protected SignupCalendarHelper _mockedCalendarHelper;

    @Mock protected SignupTimeslot _mockedTimeslot;
    @Mock protected SignupTimeslot _mockedCancelledTimeslot;

    @Before
    public void setUp() {

        MockitoAnnotations.initMocks(this);

        VEvent mockedEvent = mock(VEvent.class);
        when(_mockedCancelledTimeslot.getVevent()).thenReturn(mockedEvent);

        List<SignupTimeslot> timeslots = new ArrayList<SignupTimeslot>();
        timeslots.add(_mockedTimeslot);
        timeslots.add(_mockedCancelledTimeslot);
        when(_mockedMeeting.getSignupTimeSlots()).thenReturn(timeslots);

        when(_mockedItem.getRemovedFromTimeslot()).thenReturn(Collections.singletonList(_mockedCancelledTimeslot));

    }

    @Test
    public void canGenerateEventsFromAttendeeCancellationOwnEmail() {

        when(_mockedUser.getId()).thenReturn("userId");
        when(_mockedCancelledTimeslot.getAttendee("userId")).thenReturn(_mockedAttendee);

        when(_mockedItem.isInitiator()).thenReturn(true);
        final List<SignupTrackingItem> items = Collections.singletonList(_mockedItem);
        _email = new AttendeeCancellationOwnEmail(_mockedUser, items, _mockedMeeting, _mockedFacade);

        final List<VEvent> events = _email.generateEvents(_mockedUser, _mockedCalendarHelper);
        verify(_mockedCalendarHelper, times(1)).cancelVEvent(any(VEvent.class));
        assertEquals(1, events.size());

        assertTrue(_email.isCancellation());
    }

    @Test
    public void canGenerateEventsFromCancellationEmail() {

        when(_mockedItem.getAttendee()).thenReturn(_mockedAttendee);
        when(_mockedAttendee.getSignupSiteId()).thenReturn("123");

        _email = new CancellationEmail(_mockedUser, _mockedItem, _mockedMeeting, _mockedFacade);

        List<VEvent> events = _email.generateEvents(_mockedUser, _mockedCalendarHelper);
        verify(_mockedCalendarHelper, times(1)).cancelVEvent(any(VEvent.class));
        assertEquals(1, events.size());

        assertTrue(_email.isCancellation());
    }

}
