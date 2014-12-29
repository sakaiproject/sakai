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

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Ben Holmes
 */
public class TransferEmailTest {

    protected TransferEmailBase _email;

    @Mock protected User _mockedOrganiser;
    @Mock protected User _mockedAttendingUser1;
    @Mock protected User _mockedAttendingUser2;
    @Mock protected SignupMeeting _mockedMeeting;
    @Mock protected SignupTrackingItem _mockedItem;
    @Mock protected SignupAttendee _mockedAttendee;
    @Mock protected SakaiFacade _mockedFacade;
    @Mock protected SignupCalendarHelper _mockedCalendarHelper;

    @Mock protected VEvent _mockedEvent;
    @Mock protected SignupTimeslot _mockedTimeslot;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(_mockedItem.getAttendee()).thenReturn(_mockedAttendee);
        when(_mockedAttendee.getSignupSiteId()).thenReturn("signupSiteId");
        
        when(_mockedItem.getRemovedFromTimeslot()).thenReturn(Collections.singletonList(_mockedTimeslot));
        when(_mockedItem.getAddToTimeslot()).thenReturn(_mockedTimeslot);
        
        when(_mockedCalendarHelper.generateVEventForTimeslot(_mockedMeeting, _mockedTimeslot)).thenReturn(_mockedEvent);
    }

    @Test
    public void moveAttendeeEmailTest() {
        _email = new MoveAttendeeEmail(_mockedOrganiser, _mockedAttendingUser1, _mockedItem, _mockedMeeting, _mockedFacade);
        assertFalse(_email.isCancellation());
        assertGenerates(2);
        assertCancels(1);
    }

    @Test
    public void swapAttendeeEmail() {
        _email = new SwapAttendeeEmail(_mockedOrganiser, _mockedAttendingUser1, _mockedAttendingUser2, _mockedItem, _mockedMeeting, _mockedFacade);
        assertFalse(_email.isCancellation());
        assertGenerates(2);
        assertCancels(1);
    }

    private void assertGenerates(int n) {
        final List<VEvent> events = _email.generateEvents(_mockedAttendingUser1, _mockedCalendarHelper);
        assertEquals(n, events.size());
    }
    
    private void assertCancels(int n) {
        verify(_mockedCalendarHelper, times(n)).cancelVEvent(_mockedEvent);
    }
}
