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

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

/**
 * @author Ben Holmes
 */
public class OrganizerEmailTest {

    protected OrganizerEmailBase _email;

    @Mock protected User _mockedOrganiser;
    @Mock protected User _mockedAttendingUser;
    @Mock protected SignupMeeting _mockedMeeting;
    @Mock protected SignupTrackingItem _mockedItem;
    @Mock protected SignupAttendee _mockedAttendee;
    @Mock protected SakaiFacade _mockedFacade;
    @Mock protected SignupCalendarHelper _mockedCalendarHelper;

    @Mock protected SignupTimeslot _mockedTimeslot;
    @Mock protected VEvent _mockedEvent;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(_mockedMeeting.getVevent()).thenReturn(_mockedEvent);
    }

    /**
     *  We expect the Organiser to receive an iCal for the whole event
     *  but an attendee will be notified of each timeslot that they attend
     */

    @Test
    public void attendeeSignupEmailTest() {
        _email = new AttendeeSignupEmail(_mockedOrganiser, _mockedAttendingUser, _mockedMeeting, _mockedTimeslot,_mockedFacade);
        assertFalse(_email.isCancellation());
        assertGenerates(1);
    }

    @Test
    public void attendeeCancellationEmail() {
        final List<SignupTrackingItem> mockedItems = Collections.singletonList(_mockedItem);
        _email = new AttendeeCancellationEmail(_mockedOrganiser, _mockedAttendingUser, mockedItems, _mockedMeeting, _mockedFacade);
        // We don't want a cancelled signup to result in a full cancellation for the organiser.
        assertFalse(_email.isCancellation());
        assertGenerates(1);
    }

    private void assertGenerates(int n) {
        final List<VEvent> events = _email.generateEvents(_mockedAttendingUser, _mockedCalendarHelper);
        assertEquals(n, events.size());
    }
}
