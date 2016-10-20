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
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Ben Holmes
 */
public class AttendeeEmailTest {

    protected AttendeeEmailBase _email;

    @Mock protected User _mockedOrganiser;
    @Mock protected User _mockedAttendingUser;
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

        when(_mockedAttendingUser.getId()).thenReturn("userId");

        when(_mockedItem.getAttendee()).thenReturn(_mockedAttendee);
        when(_mockedAttendee.getSignupSiteId()).thenReturn("signupSiteId");

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
    public void addAttendeeEmailTest() {
        _email = new AddAttendeeEmail(_mockedOrganiser, _mockedAttendingUser, _mockedItem, _mockedMeeting, _mockedFacade);
        assertFalse(_email.isCancellation());
        assertGenerates(2);
    }

    @Test
    public void promoteAttendeeEmailTest() {
        _email = new PromoteAttendeeEmail(_mockedAttendingUser, _mockedItem, _mockedMeeting, _mockedFacade);
        assertFalse(_email.isCancellation());
        assertGenerates(2);
    }

    @Test
    public void organizerPreAssignEmailTest() {
        _email = new OrganizerPreAssignEmail(_mockedOrganiser, _mockedMeeting, _mockedTimeslot1, _mockedAttendingUser, _mockedFacade, "returnSiteId");
        assertFalse(_email.isCancellation());
        assertGenerates(2);
    }

    @Test
    public void attendeeSignupOwnEmailTest() {
        _email = new AttendeeSignupOwnEmail(_mockedAttendingUser, _mockedMeeting, _mockedTimeslot1, _mockedFacade);
        assertFalse(_email.isCancellation());
        assertGenerates(2);
    }

    private void assertGenerates(int n) {
        final List<VEvent> events = _email.generateEvents(_mockedAttendingUser, _mockedCalendarHelper);
        assertEquals(n, events.size());
        verify(_mockedCalendarHelper, times(n)).addUsersToVEvent(eq(_mockedEvent), any(Set.class));
    }

    private void userIsAttendingTimeslot(String userId, SignupTimeslot mockedTimeslot) {
        when(mockedTimeslot.getVevent()).thenReturn(_mockedEvent);
        when(mockedTimeslot.getAttendee(userId)).thenReturn(_mockedAttendee);
    }

}
