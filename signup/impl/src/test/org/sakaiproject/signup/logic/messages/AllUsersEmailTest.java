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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Ben Holmes
 */
public class AllUsersEmailTest {

    protected AllUsersEmailBase _email;

    @Mock protected User _mockedUser;
    @Mock protected SignupMeeting _mockedMeeting;
    @Mock protected SignupTrackingItem _mockedItem;
    @Mock protected SignupAttendee _mockedAttendee;
    @Mock protected SakaiFacade _mockedFacade;
    @Mock protected SignupCalendarHelper _mockedCalendarHelper;

    @Mock protected SignupTimeslot _mockedTimeslot1;
    @Mock protected SignupTimeslot _mockedTimeslot2;
    @Mock protected VEvent _mockedEvent;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(_mockedUser.getId()).thenReturn("userId");

        // Prepare for the organiser situation
        when(_mockedMeeting.getVevent()).thenReturn(_mockedEvent);

        // Prepare for the attendee situation
        userIsAttendingTimeslot("userId", _mockedTimeslot1);
        userIsAttendingTimeslot("userId", _mockedTimeslot2);

        List<SignupTimeslot> timeslots = new ArrayList<SignupTimeslot>();
        timeslots.add(_mockedTimeslot1);
        timeslots.add(_mockedTimeslot2);
        when(_mockedMeeting.getSignupTimeSlots()).thenReturn(timeslots);
    }

    /**
     *  We expect the Organiser to receive an iCal for the whole event
     *  but an attendee will be notified of each timeslot that they attend
     */

    @Test
    public void modifyMeetingEmailToOrganiserTest() {
        userIsAnOrganiser();
        _email = new ModifyMeetingEmail(_mockedUser, _mockedMeeting, _mockedFacade, "returnSiteId");
        assertFalse(_email.isCancellation());
        assertGenerates(1);
    }

    @Test
    public void modifyMeetingEmailToAttendeeTest() {
        _email = new ModifyMeetingEmail(_mockedUser, _mockedMeeting, _mockedFacade, "returnSiteId");
        assertFalse(_email.isCancellation());
        assertGenerates(2);
    }

    @Test
    public void newMeetingEmailToOrganiserTest() {
        userIsAnOrganiser();
        _email = new NewMeetingEmail(_mockedUser, _mockedMeeting, _mockedFacade, "returnSiteId");
        assertFalse(_email.isCancellation());
        assertGenerates(1);
    }

    @Test
    public void newMeetingEmailToAttendeeTest() {
        _email = new NewMeetingEmail(_mockedUser, _mockedMeeting, _mockedFacade, "returnSiteId");
        assertFalse(_email.isCancellation());
        assertGenerates(2);
    }

    @Test
    public void cancelMeetingEmailToOrganiserTest() {
        userIsAnOrganiser();
        _email = new CancelMeetingEmail(_mockedUser, _mockedMeeting, _mockedFacade, "returnSiteId");
        assertTrue(_email.isCancellation());
        assertCancels(1);
    }
    @Test
    public void cancelMeetingEmailToAttendeeTest() {
        _email = new CancelMeetingEmail(_mockedUser, _mockedMeeting, _mockedFacade, "returnSiteId");
        assertTrue(_email.isCancellation());
        assertCancels(2);
    }

    private void assertGenerates(int n) {
        final List<VEvent> events = _email.generateEvents(_mockedUser, _mockedCalendarHelper);
        assertEquals(n, events.size());
    }

    private void assertCancels(int n) {
        assertGenerates(n);
        verify(_mockedCalendarHelper, times(n)).cancelVEvent(_mockedEvent);
    }

    private void userIsAttendingTimeslot(String userId, SignupTimeslot mockedTimeslot) {
        when(mockedTimeslot.getVevent()).thenReturn(_mockedEvent);
        when(mockedTimeslot.getAttendee(userId)).thenReturn(_mockedAttendee);
    }

    private void userIsAnOrganiser() {
        when(_mockedMeeting.getCreatorUserId()).thenReturn("userId");
    }

}
