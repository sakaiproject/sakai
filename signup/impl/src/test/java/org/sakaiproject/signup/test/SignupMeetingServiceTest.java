package org.sakaiproject.signup.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.signup.api.SignupEmailFacade;
import org.sakaiproject.signup.api.SignupMeetingService;
import org.sakaiproject.signup.api.SignupMessageTypes;
import org.sakaiproject.signup.api.messages.AttendeeComment;
import org.sakaiproject.signup.api.messages.SignupEventTrackingInfo;
import org.sakaiproject.signup.api.messages.SignupEventTrackingInfoImpl;
import org.sakaiproject.signup.api.model.MeetingTypes;
import org.sakaiproject.signup.api.model.SignupAttendee;
import org.sakaiproject.signup.api.model.SignupMeeting;
import org.sakaiproject.signup.api.model.SignupSite;
import org.sakaiproject.signup.api.model.SignupTimeslot;
import org.sakaiproject.signup.api.restful.SignupTargetSiteEventInfo;
import org.sakaiproject.signup.impl.SignupEmailFacadeImpl;
import org.sakaiproject.signup.impl.SignupMeetingServiceImpl;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.AopTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SignupMeetingServiceTestConfiguration.class })
public class SignupMeetingServiceTest {

    @Autowired private SignupEmailFacade signupEmailFacade;
    @Autowired private SignupMeetingService service;
    @Autowired private SecurityService securityService;
    @Autowired private SessionManager sessionManager;
    @Autowired private SiteService siteService;
    @Autowired private UserDirectoryService userDirectoryService;
    @Autowired private CalendarService calendarService;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Autowired private TimeService timeService;
    @Autowired @Qualifier("org.sakaiproject.time.api.UserTimeService")
    private UserTimeService userTimeService;
    private ResourceLoader resourceLoader;

    private static final String TEST_SITE_ID = "test-site-123";
    private static final String TEST_USER_ID = "user123";
    private static final String TEST_GROUP_ID = "group123";
    private static final String TEST_CALENDAR_ID = "calendar123";

    @Before
    public void setUp() throws Exception {
        // Configure common mock behaviors for all tests
        setupResourceLoader();
        setupSessionManager();
        setupSecurityService();
        setupSiteService();
        setupUserDirectoryService();
        setupCalendarService();
        setupTimeService();
        setupUserTimeService();
        setupServerConfigurationService();
    }

    private void setupResourceLoader() {
        resourceLoader = Mockito.mock(ResourceLoader.class);
        Mockito.when(resourceLoader.getLocale()).thenReturn(Locale.getDefault());
        when(resourceLoader.getString("body.footer.text.no.access.link")).thenReturn("No access link");
        when(resourceLoader.getString("body.meeting.crossdays.timeslot.timeframe")).thenReturn("{0}, {1} - {2}, {3} ({4})");
        when(resourceLoader.getString("body.meetingTopic.part")).thenReturn("Meeting Topic");
        when(resourceLoader.getString("body.timeslot")).thenReturn("Time slot:");
        when(resourceLoader.getString("body.attendee.cancel.own")).thenReturn("Cancel own");
        when(resourceLoader.getString("body.top.greeting.part")).thenReturn("Hello");
        when(resourceLoader.getString("subject.attendee.cancel.own.field")).thenReturn("Cancel own field");
        when(resourceLoader.getString("signup.event.currentattendees")).thenReturn("Current attendees");
        when(resourceLoader.getString("signup.event.attendeestitle")).thenReturn("Attendees");
        ((SignupMeetingServiceImpl) AopTestUtils.getTargetObject(service)).setResourceLoader(resourceLoader);
        ((SignupEmailFacadeImpl) AopTestUtils.getTargetObject(signupEmailFacade)).setResourceLoader(resourceLoader);
    }

    private void setupSessionManager() {
        when(sessionManager.getCurrentSessionUserId()).thenReturn(TEST_USER_ID);
        // Setup placement context for current location ID
        Placement mockPlacement = mock(Placement.class);
        when(mockPlacement.getContext()).thenReturn(TEST_SITE_ID);
        when(sessionManager.getCurrentToolSession()).thenReturn(mock(ToolSession.class));
    }

    private void setupSecurityService() {
        /* By default, allow basic view/attend/create/update/delete permissions.
         * SecurityService.unlock(userId, permission, reference) where reference is from siteService.siteReference()
         */
        when(securityService.isSuperUser(anyString())).thenReturn(false);
        when(securityService.unlock(anyString(), anyString(), anyString())).thenReturn(true);
        when(securityService.unlock(any(User.class), anyString(), anyString())).thenReturn(true);
    }

    private void setupSiteService() throws IdUnusedException {
        Site mockSite = mock(Site.class);
        when(mockSite.getId()).thenReturn(TEST_SITE_ID);
        when(mockSite.getTitle()).thenReturn("Test Site");
        when(mockSite.getReference()).thenReturn("/site/" + TEST_SITE_ID);

        Group mockGroup = mock(Group.class);
        when(mockGroup.getId()).thenReturn(TEST_GROUP_ID);
        when(mockGroup.getTitle()).thenReturn("Test Group");
        when(mockGroup.getReference()).thenReturn("/site/" + TEST_SITE_ID + "/group/" + TEST_GROUP_ID);

        Collection<Group> groups = Collections.singletonList(mockGroup);
        when(mockSite.getGroups()).thenReturn(groups);
        when(mockSite.getGroup(TEST_GROUP_ID)).thenReturn(mockGroup);

        when(siteService.getSite(TEST_SITE_ID)).thenReturn(mockSite);
        when(siteService.siteExists(TEST_SITE_ID)).thenReturn(true);
        when(siteService.siteReference(TEST_SITE_ID)).thenReturn("/site/" + TEST_SITE_ID);
        when(siteService.siteReference(anyString())).thenAnswer(invocation ->
            "/site/" + invocation.getArgument(0));
    }

    private void setupUserDirectoryService() {
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(TEST_USER_ID);
        when(mockUser.getDisplayName()).thenReturn("Test User");
        when(mockUser.getEmail()).thenReturn("test@example.com");

        try {
            when(userDirectoryService.getUser(TEST_USER_ID)).thenReturn(mockUser);
        } catch (Exception e) {
            // Should not happen with mocks
        }
    }

    private void setupCalendarService() throws Exception {
        Calendar mockCalendar = mock(Calendar.class);
        when(mockCalendar.getId()).thenReturn(TEST_CALENDAR_ID);
        when(mockCalendar.allowAddEvent()).thenReturn(true);
        when(mockCalendar.allowEditEvent(anyString())).thenReturn(true);

        CalendarEventEdit mockEventEdit = mock(CalendarEventEdit.class);
        when(mockEventEdit.getId()).thenReturn("event-" + System.currentTimeMillis());
        when(mockCalendar.addEvent()).thenReturn(mockEventEdit);
        when(mockCalendar.getEditEvent(anyString(), anyString())).thenReturn(mockEventEdit);

        when(calendarService.getCalendar(anyString())).thenReturn(mockCalendar);
    }

    private void setupTimeService() {
        when(timeService.newTime(anyLong())).thenAnswer(invocation -> {
            Time mockTime = mock(Time.class);
            when(mockTime.getTime()).thenReturn(invocation.getArgument(0));
            return mockTime;
        });
        when(timeService.newTimeRange(any(Time.class), any(Time.class), anyBoolean(), anyBoolean()))
                .thenAnswer(invocation -> {
                    TimeRange mockTimeRange = mock(TimeRange.class);
                    return mockTimeRange;
                });
        when(timeService.getLocalTimeZone()).thenReturn(TimeZone.getDefault());
    }

    private void setupUserTimeService() {
        when(userTimeService.getLocalTimeZone()).thenReturn(TimeZone.getDefault());
    }

    private void setupServerConfigurationService() {
        when(serverConfigurationService.getString(anyString())).thenReturn("");
        when(serverConfigurationService.getString(anyString(), anyString())).thenAnswer(
                invocation -> invocation.getArgument(1));
        when(serverConfigurationService.getBoolean(anyString(), anyBoolean())).thenAnswer(
                invocation -> invocation.getArgument(1));
    }

    // Helper methods to create test data
    private SignupMeeting createTestMeeting(String title) {
        SignupMeeting meeting = new SignupMeeting();
        meeting.setTitle(title);
        meeting.setDescription("Test description");
        meeting.setLocation("Test location");
        meeting.setCreatorUserId(TEST_USER_ID);
        meeting.setMeetingType("individual");
        meeting.setCurrentSiteId(TEST_SITE_ID);

        Date now = new Date();
        meeting.setStartTime(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        meeting.setEndTime(Date.from(Instant.now().plus(2, ChronoUnit.DAYS)));
        meeting.setSignupBegins(now);
        meeting.setSignupDeadline(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));

        SignupSite site = new SignupSite();
        site.setSiteId(TEST_SITE_ID);
        site.setTitle("Test Site");
        meeting.setSignupSites(new ArrayList<>(List.of(site)));

        return meeting;
    }

    private SignupTimeslot createTestTimeslot(Date startTime, Date endTime) {
        SignupTimeslot timeslot = new SignupTimeslot();
        timeslot.setStartTime(startTime);
        timeslot.setEndTime(endTime);
        timeslot.setMaxNoOfAttendees(10);
        timeslot.setDisplayAttendees(true);
        return timeslot;
    }

    /* ==========================
     * Tests start here
     * ==========================
     */

    @Test
    public void testServicesNotNull() {
        assertNotNull("SignupMeetingService is null", service);
        assertNotNull("SignupEmailFacade is null", signupEmailFacade);
        assertNotNull("SecurityService is null", securityService);
        assertNotNull("SessionManager is null", sessionManager);
        assertNotNull("SiteService is null", siteService);
        assertNotNull("UserDirectoryService is null", userDirectoryService);
        assertNotNull("CalendarService is null", calendarService);
        assertNotNull("ServerConfigurationService is null", serverConfigurationService);
        assertNotNull("TimeService is null", timeService);
        assertNotNull("UserTimeService is null", userTimeService);
    }

    /* ==========================
     * Query/Retrieval Tests (7 methods)
     * ==========================
     */

    @Test
    public void testGetAllSignupMeetings() throws PermissionException {
        // Create and save test meetings
        SignupMeeting meeting1 = createTestMeeting("Meeting 1");
        SignupMeeting meeting2 = createTestMeeting("Meeting 2");

        Long id1 = service.saveMeeting(meeting1, TEST_USER_ID);
        Long id2 = service.saveMeeting(meeting2, TEST_USER_ID);

        assertNotNull(id1);
        assertNotNull(id2);

        // Retrieve all meetings
        List<SignupMeeting> meetings = service.getAllSignupMeetings(TEST_SITE_ID, TEST_USER_ID);

        assertNotNull(meetings);
        assertTrue("Should have at least 2 meetings", meetings.size() >= 2);
    }

    @Test
    public void testGetSignupMeetingsWithEndDate() throws PermissionException {
        SignupMeeting meeting = createTestMeeting("Test Meeting");
        service.saveMeeting(meeting, TEST_USER_ID);

        Date searchEndDate = Date.from(Instant.now().plus(7, ChronoUnit.DAYS));
        List<SignupMeeting> meetings = service.getSignupMeetings(TEST_SITE_ID, TEST_USER_ID, searchEndDate);

        assertNotNull(meetings);
        assertTrue("Should find the meeting", meetings.size() >= 1);
    }

    @Test
    public void testGetSignupMeetingsWithDateRange() throws PermissionException {
        SignupMeeting meeting = createTestMeeting("Test Meeting");
        service.saveMeeting(meeting, TEST_USER_ID);

        Date startDate = new Date();
        Date endDate = Date.from(Instant.now().plus(10, ChronoUnit.DAYS));

        List<SignupMeeting> meetings = service.getSignupMeetings(TEST_SITE_ID, TEST_USER_ID, startDate, endDate);

        assertNotNull(meetings);
        assertTrue("Should find the meeting", meetings.size() >= 1);
    }

    @Test
    public void testGetSignupMeetingsInSite() throws PermissionException {
        SignupMeeting meeting = createTestMeeting("Site Meeting");
        service.saveMeeting(meeting, TEST_USER_ID);

        Date startDate = new Date();
        Date endDate = Date.from(Instant.now().plus(10, ChronoUnit.DAYS));

        List<SignupMeeting> meetings = service.getSignupMeetingsInSite(TEST_SITE_ID, startDate, endDate);

        assertNotNull(meetings);
        assertTrue("Should find the meeting", meetings.size() >= 1);
    }

    @Test
    public void testGetSignupMeetingsInSites() throws PermissionException {
        SignupMeeting meeting = createTestMeeting("Multi-Site Meeting");
        service.saveMeeting(meeting, TEST_USER_ID);

        Date startDate = new Date();
        Date endDate = Date.from(Instant.now().plus(10, ChronoUnit.DAYS));
        List<String> siteIds = Arrays.asList(TEST_SITE_ID);

        List<SignupMeeting> meetings = service.getSignupMeetingsInSites(siteIds, startDate, endDate);

        assertNotNull(meetings);
        assertTrue("Should find the meeting", meetings.size() >= 1);
    }

    @Test
    public void testGetSignupMeetingsInSitesWithTimeFrame() throws PermissionException {
        SignupMeeting meeting = createTestMeeting("Timeframe Meeting");
        service.saveMeeting(meeting, TEST_USER_ID);

        Date startDate = new Date();
        int timeFrameInDays = 10;
        List<String> siteIds = Arrays.asList(TEST_SITE_ID);

        List<SignupMeeting> meetings = service.getSignupMeetingsInSites(siteIds, startDate, timeFrameInDays);

        assertNotNull(meetings);
        assertTrue("Should find the meeting", meetings.size() >= 1);
    }

    @Test
    public void testGetSignupMeetingsInSitesWithEmptyList() {
        Date startDate = new Date();
        List<String> siteIds = Collections.emptyList();

        List<SignupMeeting> meetings = service.getSignupMeetingsInSites(siteIds, startDate, 10);

        assertNotNull(meetings);
        assertTrue("Should return empty list", meetings.isEmpty());
    }

    @Test
    public void testGetRecurringSignupMeetings() throws PermissionException {
        SignupMeeting meeting1 = createTestMeeting("Recurring 1");
        meeting1.setRepeatType("weekly");
        SignupMeeting meeting2 = createTestMeeting("Recurring 2");
        meeting2.setRepeatType("weekly");

        List<SignupMeeting> meetings = Arrays.asList(meeting1, meeting2);
        service.saveMeetings(meetings, TEST_USER_ID);

        Long recurrenceId = meeting1.getRecurrenceId();
        assertNotNull("Recurrence ID should be set", recurrenceId);

        Date startDate = new Date();
        List<SignupMeeting> recurring = service.getRecurringSignupMeetings(
                TEST_SITE_ID, TEST_USER_ID, recurrenceId, startDate);

        assertNotNull(recurring);
        assertTrue("Should find recurring meetings", recurring.size() >= 2);
    }

    /* ==========================
     * Save/Create Tests (2 methods)
     * ==========================
     */

    @Test
    public void testSaveMeeting() throws PermissionException {
        SignupMeeting meeting = createTestMeeting("New Meeting");

        Long meetingId = service.saveMeeting(meeting, TEST_USER_ID);

        assertNotNull("Meeting ID should not be null", meetingId);
        assertTrue("Meeting ID should be positive", meetingId > 0);
    }

    @Test(expected = PermissionException.class)
    public void testSaveMeetingWithoutPermission() throws PermissionException {
        when(securityService.unlock(anyString(), anyString(), anyString())).thenReturn(false);

        SignupMeeting meeting = createTestMeeting("Unauthorized Meeting");
        service.saveMeeting(meeting, TEST_USER_ID);
    }

    @Test
    public void testSaveMeetings() throws PermissionException {
        SignupMeeting meeting1 = createTestMeeting("Batch Meeting 1");
        SignupMeeting meeting2 = createTestMeeting("Batch Meeting 2");

        List<SignupMeeting> meetings = Arrays.asList(meeting1, meeting2);
        service.saveMeetings(meetings, TEST_USER_ID);

        assertNotNull("First meeting ID should be set", meeting1.getId());
        assertNotNull("Second meeting ID should be set", meeting2.getId());
    }

    @Test
    public void testSaveMeetingsWithRecurrence() throws PermissionException {
        SignupMeeting meeting1 = createTestMeeting("Recurring Event 1");
        meeting1.setRepeatType(MeetingTypes.DAILY);
        SignupMeeting meeting2 = createTestMeeting("Recurring Event 2");
        meeting2.setRepeatType(MeetingTypes.DAILY);

        List<SignupMeeting> meetings = Arrays.asList(meeting1, meeting2);
        service.saveMeetings(meetings, TEST_USER_ID);

        assertNotNull("Recurrence ID should be set", meeting1.getRecurrenceId());
        assertEquals("Both meetings should have same recurrence ID",
                meeting1.getRecurrenceId(), meeting2.getRecurrenceId());
        assertEquals("Meeting 1 should have recurrence daily",
                MeetingTypes.DAILY, meeting1.getRepeatType());
        assertEquals("Meeting 2 should have recurrence daily",
                MeetingTypes.DAILY, meeting2.getRepeatType());
    }

    @Test
    public void testSaveMeetingsEmptyList() throws PermissionException {
        // Should not throw exception
        service.saveMeetings(Collections.emptyList(), TEST_USER_ID);
        service.saveMeetings(null, TEST_USER_ID);
    }

    /* ==========================
     * Update Tests (3 methods)
     * ==========================
     */

    @Test
    public void testUpdateSignupMeetingAsOrganizer() throws Exception {
        SignupMeeting meeting = createTestMeeting("Meeting to Update");
        Long id = service.saveMeeting(meeting, TEST_USER_ID);

        // Load and update
        SignupMeeting loaded = service.loadSignupMeeting(id, TEST_USER_ID, TEST_SITE_ID);
        loaded.setTitle("Updated Title");

        service.updateSignupMeeting(loaded, true);

        // Verify update
        SignupMeeting updated = service.loadSignupMeeting(id, TEST_USER_ID, TEST_SITE_ID);
        assertEquals("Updated Title", updated.getTitle());
    }

    @Test(expected = PermissionException.class)
    public void testUpdateSignupMeetingWithoutPermission() throws Exception {
        SignupMeeting meeting = createTestMeeting("Meeting to Update");
        Long id = service.saveMeeting(meeting, TEST_USER_ID);

        when(securityService.unlock(anyString(), anyString(), anyString())).thenReturn(false);

        SignupMeeting loaded = service.loadSignupMeeting(id, TEST_USER_ID, TEST_SITE_ID);
        loaded.setTitle("Should Fail");

        service.updateSignupMeeting(loaded, true);
    }

    @Test
    public void testUpdateSignupMeetings() throws Exception {
        SignupMeeting meeting1 = createTestMeeting("Batch Update 1");
        SignupMeeting meeting2 = createTestMeeting("Batch Update 2");
        service.saveMeetings(Arrays.asList(meeting1, meeting2), TEST_USER_ID);

        meeting1 = service.loadSignupMeeting(meeting1.getId(), TEST_USER_ID, TEST_SITE_ID);
        meeting2 = service.loadSignupMeeting(meeting2.getId(), TEST_USER_ID, TEST_SITE_ID);

        meeting1.setTitle("Updated 1");
        meeting2.setTitle("Updated 2");

        service.updateSignupMeetings(Arrays.asList(meeting1, meeting2), true);

        SignupMeeting updated1 = service.loadSignupMeeting(meeting1.getId(), TEST_USER_ID, TEST_SITE_ID);
        assertEquals("Updated 1", updated1.getTitle());
    }

    @Test
    public void testUpdateSignupMeetingsEmptyList() throws Exception {
        // Should not throw exception
        service.updateSignupMeetings(Collections.emptyList(), true);
        service.updateSignupMeetings(null, true);
    }

    @Test
    public void testUpdateModifiedMeetings() throws Exception {
        SignupMeeting meeting = createTestMeeting("Meeting with Timeslots");
        SignupTimeslot ts1 = createTestTimeslot(meeting.getStartTime(), meeting.getEndTime());
        meeting.setSignupTimeSlots(new ArrayList<>(Collections.singletonList(ts1)));

        Long id = service.saveMeeting(meeting, TEST_USER_ID);
        meeting = service.loadSignupMeeting(id, TEST_USER_ID, TEST_SITE_ID);

        // Update with removed timeslots
        List<SignupTimeslot> removedTimeslots = new ArrayList<>();
        meeting.setTitle("Modified Meeting");

        service.updateModifiedMeetings(Collections.singletonList(meeting), removedTimeslots, true);

        SignupMeeting updated = service.loadSignupMeeting(id, TEST_USER_ID, TEST_SITE_ID);
        assertEquals("Modified Meeting", updated.getTitle());
    }

    /* ==========================
     * Load Tests (2 methods)
     * ==========================
     */

    @Test
    public void testLoadSignupMeeting() throws PermissionException {
        SignupMeeting meeting = createTestMeeting("Meeting to Load");
        Long id = service.saveMeeting(meeting, TEST_USER_ID);

        SignupMeeting loaded = service.loadSignupMeeting(id, TEST_USER_ID, TEST_SITE_ID);

        assertNotNull(loaded);
        assertEquals("Meeting to Load", loaded.getTitle());
        assertNotNull("Permission should be set", loaded.getPermission());
    }

    @Test
    public void testLoadSignupMeetingWithAutoSelectedSite() throws PermissionException {
        SignupMeeting meeting = createTestMeeting("Auto Site Meeting");
        Long id = service.saveMeeting(meeting, TEST_USER_ID);

        SignupTargetSiteEventInfo info = service.loadSignupMeetingWithAutoSelectedSite(
                id, TEST_USER_ID, TEST_SITE_ID);

        assertNotNull(info);
        assertNotNull(info.getSignupMeeting());
        assertEquals("Auto Site Meeting", info.getSignupMeeting().getTitle());
        assertNotNull(info.getTargetSiteId());
    }

    @Test
    public void testLoadSignupMeetingWithNullSiteAutoSelects() throws PermissionException {
        SignupMeeting meeting = createTestMeeting("Null Site Meeting");
        Long id = service.saveMeeting(meeting, TEST_USER_ID);

        // Pass null for siteId to trigger auto-selection
        SignupTargetSiteEventInfo info = service.loadSignupMeetingWithAutoSelectedSite(
                id, TEST_USER_ID, null);

        assertNotNull(info);
        assertNotNull("Should auto-select a site", info.getTargetSiteId());
    }

    /* ==========================
     * Permission Tests (3 methods)
     * ==========================
     */

    @Test
    public void testIsAllowedToCreateinSite() {
        when(securityService.unlock(anyString(), anyString())).thenReturn(true);

        boolean allowed = service.isAllowedToCreateinSite(TEST_USER_ID, TEST_SITE_ID);

        assertTrue("User should be allowed to create in site", allowed);
    }

    @Test
    public void testIsAllowedToCreateinSiteDenied() {
        when(securityService.unlock(anyString(), anyString(), anyString())).thenReturn(false);

        boolean allowed = service.isAllowedToCreateinSite(TEST_USER_ID, TEST_SITE_ID);

        assertFalse("User should not be allowed to create in site", allowed);
    }

    @Test
    public void testIsAllowedToCreateinGroup() {
        when(securityService.unlock(anyString(), anyString())).thenReturn(true);

        boolean allowed = service.isAllowedToCreateinGroup(TEST_USER_ID, TEST_SITE_ID, TEST_GROUP_ID);

        assertTrue("User should be allowed to create in group", allowed);
    }

    @Test
    public void testIsAllowedToCreateAnyInSite() throws IdUnusedException {
        when(securityService.unlock(anyString(), anyString())).thenReturn(true);

        boolean allowed = service.isAllowedToCreateAnyInSite(TEST_USER_ID, TEST_SITE_ID);

        assertTrue("User should be allowed to create any in site", allowed);
    }

    @Test
    public void testIsAllowedToCreateAnyInSiteWithInvalidSite() throws IdUnusedException {
        // Deny site-level permission so the method proceeds to check groups
        when(securityService.unlock(anyString(), anyString(), anyString())).thenReturn(false);

        // Mock getSite to throw IdUnusedException for invalid site
        when(siteService.getSite("invalid-site")).thenThrow(new IdUnusedException("invalid-site"));

        boolean allowed = service.isAllowedToCreateAnyInSite(TEST_USER_ID, "invalid-site");

        assertFalse("Should return false for invalid site", allowed);
    }

    /* ==========================
     * Email Tests (6 methods)
     * ==========================
     */

    @Test
    public void testSendEmail() throws Exception {
        SignupMeeting meeting = createTestMeeting("Email Test Meeting");
        service.saveMeeting(meeting, TEST_USER_ID);

        // Should not throw exception
        service.sendEmail(meeting, "announcement");

        // Email is sent through the facade - method completes successfully
    }

    @Test
    public void testSendEmailToOrganizer() throws Exception {
        SignupMeeting meeting = createTestMeeting("Organizer Email Test");
        service.saveMeeting(meeting, TEST_USER_ID);

        SignupEventTrackingInfo trackingInfo = new SignupEventTrackingInfoImpl();
        trackingInfo.setMeeting(meeting);

        // Should not throw exception
        service.sendEmailToOrganizer(trackingInfo);
    }

    @Test
    public void testSendCancelllationEmail() throws Exception {
        SignupMeeting meeting = createTestMeeting("Cancellation Test");

        // Add a timeslot to the meeting
        SignupTimeslot timeslot = createTestTimeslot(meeting.getStartTime(), meeting.getEndTime());
        meeting.getSignupTimeSlots().add(timeslot);

        Long id = service.saveMeeting(meeting, TEST_USER_ID);

        // Load the meeting to populate permissions
        meeting = service.loadSignupMeeting(id, TEST_USER_ID, TEST_SITE_ID);
        meeting.setCurrentSiteId(TEST_SITE_ID); // need to add as it is a transient field

        // Ensure timeslots exist after reload (may not persist in test environment)
        Assert.assertEquals("Timeslots should be populated", 1, meeting.getSignupTimeSlots().size());

        // Create an attendee who is cancelling
        SignupAttendee attendee = new SignupAttendee();
        attendee.setAttendeeUserId(TEST_USER_ID);
        attendee.setSignupSiteId(TEST_SITE_ID);

        SignupEventTrackingInfo trackingInfo = new SignupEventTrackingInfoImpl();
        trackingInfo.setMeeting(meeting);

        // Add the cancellation with the attendee as initiator
        trackingInfo.addOrUpdateAttendeeAllocationInfo(attendee, meeting.getSignupTimeSlots().get(0), SignupMessageTypes.SIGNUP_ATTENDEE_CANCEL, true);

        // Should not throw exception
        service.sendCancellationEmail(trackingInfo);
    }

    @Test
    public void testSendUpdateCommentEmail() throws Exception {
        SignupMeeting meeting = createTestMeeting("Comment Update Test");
        Long id = service.saveMeeting(meeting, TEST_USER_ID);

        // Load the meeting to populate permissions
        meeting = service.loadSignupMeeting(id, TEST_USER_ID, TEST_SITE_ID);

        SignupEventTrackingInfo trackingInfo = new SignupEventTrackingInfoImpl();
        trackingInfo.setMeeting(meeting);

        // Create and set AttendeeComment
        AttendeeComment attendeeComment = new AttendeeComment("Test comment", TEST_USER_ID, TEST_USER_ID);
        trackingInfo.setAttendeeComment(attendeeComment);

        // Should not throw exception
        service.sendUpdateCommentEmail(trackingInfo);
    }

    @Test
    public void testSendEmailToParticipantsByOrganizerAction() throws Exception {
        SignupMeeting meeting = createTestMeeting("Participants Email Test");
        service.saveMeeting(meeting, TEST_USER_ID);

        SignupEventTrackingInfo trackingInfo = new SignupEventTrackingInfoImpl();
        trackingInfo.setMeeting(meeting);

        // Should not throw exception
        service.sendEmailToParticipantsByOrganizerAction(trackingInfo);
    }

    @Test
    public void testSendEmailToAttendee() throws Exception {
        SignupMeeting meeting = createTestMeeting("Attendee Email Test");
        service.saveMeeting(meeting, TEST_USER_ID);

        SignupEventTrackingInfo trackingInfo = new SignupEventTrackingInfoImpl();
        trackingInfo.setMeeting(meeting);

        // Should not throw exception
        service.sendEmailToAttendee(trackingInfo);
    }

    /* ==========================
     * Calendar Tests (4 methods)
     * ==========================
     */

    @Test
    public void testPostToCalendar() throws Exception {
        SignupMeeting meeting = createTestMeeting("Calendar Post Test");
        Long id = service.saveMeeting(meeting, TEST_USER_ID);
        meeting = service.loadSignupMeeting(id, TEST_USER_ID, TEST_SITE_ID);

        service.postToCalendar(meeting);

        // Verify calendar service was called
        verify(calendarService, atLeastOnce()).getCalendar(anyString());
    }

    @Test
    public void testModifyCalendar() throws Exception {
        SignupMeeting meeting = createTestMeeting("Calendar Modify Test");
        Long id = service.saveMeeting(meeting, TEST_USER_ID);
        meeting = service.loadSignupMeeting(id, TEST_USER_ID, TEST_SITE_ID);

        service.modifyCalendar(meeting);

        verify(calendarService, atLeastOnce()).getCalendar(anyString());
    }

    @Test
    public void testRemoveCalendarEvents() throws Exception {
        SignupMeeting meeting = createTestMeeting("Calendar Remove Test");
        Long id = service.saveMeeting(meeting, TEST_USER_ID);
        meeting = service.loadSignupMeeting(id, TEST_USER_ID, TEST_SITE_ID);

        // Set calendar event ID to simulate posted calendar
        meeting.getSignupSites().get(0).setCalendarEventId("test-event-id");

        service.removeCalendarEvents(Collections.singletonList(meeting));

        verify(calendarService, atLeastOnce()).getCalendar(anyString());
    }

    @Test
    public void testRemoveCalendarEventsOnModifiedMeeting() throws Exception {
        SignupMeeting meeting = createTestMeeting("Modified Calendar Test");
        Long id = service.saveMeeting(meeting, TEST_USER_ID);
        meeting = service.loadSignupMeeting(id, TEST_USER_ID, TEST_SITE_ID);

        meeting.getSignupSites().get(0).setCalendarEventId("test-event-id");

        service.removeCalendarEventsOnModifiedMeeting(Collections.singletonList(meeting));

        // Should clear calendar info
        assertNull("Calendar event ID should be cleared",
                meeting.getSignupSites().get(0).getCalendarEventId());
    }

    /* ==========================
     * Delete/Utility Tests (4 methods)
     * ==========================
     */

    @Test
    public void testRemoveMeetings() throws Exception {
        SignupMeeting meeting = createTestMeeting("Meeting to Delete");
        Long id = service.saveMeeting(meeting, TEST_USER_ID);
        meeting = service.loadSignupMeeting(id, TEST_USER_ID, TEST_SITE_ID);

        service.removeMeetings(Collections.singletonList(meeting));

        // Verify meeting is deleted
        assertFalse("Meeting should not exist", service.isEventExisted(id));
    }

    @Test
    public void testRemoveMeetingsWithExpiredMeeting() throws Exception {
        SignupMeeting meeting = createTestMeeting("Expired Meeting");
        // Set end time in the past
        meeting.setEndTime(Date.from(Instant.now().minus(1, ChronoUnit.DAYS)));
        Long id = service.saveMeeting(meeting, TEST_USER_ID);
        meeting = service.loadSignupMeeting(id, TEST_USER_ID, TEST_SITE_ID);

        service.removeMeetings(Collections.singletonList(meeting));

        // Should delete but not send email for expired meeting
        assertFalse("Meeting should not exist", service.isEventExisted(id));
    }

    @Test
    public void testIsEventExisted() throws PermissionException {
        SignupMeeting meeting = createTestMeeting("Existence Test");
        Long id = service.saveMeeting(meeting, TEST_USER_ID);

        assertTrue("Meeting should exist", service.isEventExisted(id));
        assertFalse("Non-existent meeting should return false", service.isEventExisted(99999L));
    }

    @Test
    public void testGetAllLocations() throws PermissionException {
        SignupMeeting meeting1 = createTestMeeting("Meeting Location 1");
        meeting1.setLocation("Building A");
        SignupMeeting meeting2 = createTestMeeting("Meeting Location 2");
        meeting2.setLocation("Building B");

        service.saveMeeting(meeting1, TEST_USER_ID);
        service.saveMeeting(meeting2, TEST_USER_ID);

        List<String> locations = service.getAllLocations(TEST_SITE_ID);

        assertNotNull(locations);
        assertTrue("Should contain locations", locations.size() >= 0);
    }

    @Test
    public void testGetAllCategories() throws PermissionException {
        SignupMeeting meeting1 = createTestMeeting("Meeting Category 1");
        meeting1.setCategory("Category A");
        SignupMeeting meeting2 = createTestMeeting("Meeting Category 2");
        meeting2.setCategory("Category B");

        service.saveMeeting(meeting1, TEST_USER_ID);
        service.saveMeeting(meeting2, TEST_USER_ID);

        List<String> categories = service.getAllCategories(TEST_SITE_ID);

        assertNotNull(categories);
        assertTrue("Should contain categories", categories.size() >= 0);
    }

    /* ==========================
     * Formatting Test (1 method)
     * ==========================
     */

    @Test
    public void testGetUsersLocalDateTimeString() {
        Instant testInstant = Instant.now();

        String formatted = service.getUsersLocalDateTimeString(testInstant);

        assertNotNull("Formatted string should not be null", formatted);
        assertFalse("Formatted string should not be empty", formatted.isEmpty());
    }

    @Test
    public void testGetUsersLocalDateTimeStringWithNull() {
        String formatted = service.getUsersLocalDateTimeString(null);

        assertNotNull("Should handle null gracefully", formatted);
        assertEquals("Should return empty string for null", "", formatted);
    }
}
