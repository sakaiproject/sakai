package org.sakaiproject.announcement.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementMessageHeader;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.message.api.MessageHeader;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Unit tests for the PrivacyFilter class in BaseAnnouncementService.
 * These tests focus on the release and retract date filtering logic.
 */
@RunWith(MockitoJUnitRunner.class)
public class PrivacyFilterTest {

    private TestBaseAnnouncementService service;
    
    @Mock private SecurityService securityService;
    @Mock private SessionManager sessionManager;
    @Mock private TimeService timeService;
    
    @Mock private AnnouncementMessage message;
    @Mock private AnnouncementMessageHeader messageHeader;
    @Mock private ResourceProperties messageProperties;
    @Mock private User messageCreator;
    
    private static final String CURRENT_USER_ID = "current-user";
    private static final String OTHER_USER_ID = "other-user";
    
    @Before
    public void setUp() {
        service = new TestBaseAnnouncementService();
        service.setSecurityService(securityService);
        service.setSessionManager(sessionManager);
        service.setTimeService(timeService);
        
        // Common mock setup
        when(message.getAnnouncementHeader()).thenReturn(messageHeader);
        when(message.getHeader()).thenReturn(messageHeader);
        when(message.getProperties()).thenReturn(messageProperties);
        when(messageHeader.getFrom()).thenReturn(messageCreator);
        when(sessionManager.getCurrentSessionUserId()).thenReturn(CURRENT_USER_ID);
    }
    
    /**
     * Test that a message with a retract date in the past is not accessible
     * to regular users, but is accessible to the message creator and admin
     */
    @Test
    public void testRetractDateInPast() throws Exception {
        // Create the filter to test
        BaseAnnouncementService.PrivacyFilter filter = service.new PrivacyFilter(null);
        
        // Create a retract date in the past
        when(messageProperties.getInstantProperty("retractDate")).thenReturn(Instant.now().minus(1, ChronoUnit.DAYS));
        
        // Regular user case - message creator is different from current user
        when(messageCreator.getId()).thenReturn(OTHER_USER_ID);
        when(securityService.isSuperUser()).thenReturn(false);
        
        // Message should not be accessible for regular user
        assertFalse("Regular user should not be able to access message with past retract date", 
                filter.accept(message));
        
        // Message creator case - message creator is current user
        when(messageCreator.getId()).thenReturn(CURRENT_USER_ID);
        
        // Message should be accessible for creator
        assertTrue("Message creator should be able to access message with past retract date", 
                filter.accept(message));
        
        // Admin case
        when(securityService.isSuperUser()).thenReturn(true);
        
        // Message should be accessible for admin
        assertTrue("Admin should be able to access message with past retract date", 
                filter.accept(message));
    }
    
    /**
     * Test that a message with a release date in the future is not accessible
     * to regular users, but is accessible to the message creator and admin
     */
    @Test
    public void testReleaseDateInFuture() throws Exception {
        // Create the filter to test
        BaseAnnouncementService.PrivacyFilter filter = service.new PrivacyFilter(null);
        
        // Create a release date in the future
        when(messageProperties.getInstantProperty("releaseDate")).thenReturn(Instant.now().plus(1, ChronoUnit.DAYS));
        
        // Regular user case - message creator is different from current user
        when(messageCreator.getId()).thenReturn(OTHER_USER_ID);
        when(securityService.isSuperUser()).thenReturn(false);
        
        // Message should not be accessible for regular user
        assertFalse("Regular user should not be able to access message with future release date", 
                filter.accept(message));
        
        // Message creator case - message creator is current user
        when(messageCreator.getId()).thenReturn(CURRENT_USER_ID);
        
        // Message should be accessible for creator
        assertTrue("Message creator should be able to access message with future release date", 
                filter.accept(message));
        
        // Admin case
        when(securityService.isSuperUser()).thenReturn(true);
        
        // Message should be accessible for admin
        assertTrue("Admin should be able to access message with future release date", 
                filter.accept(message));
    }
    
    /**
     * Test that a message with both release date in the past and retract date in the future
     * is accessible to all users
     */
    @Test
    public void testMessageWithValidDates() throws Exception {
        // Create the filter to test
        BaseAnnouncementService.PrivacyFilter filter = service.new PrivacyFilter(null);
        
        // Create a release date in the past
        when(messageProperties.getInstantProperty("releaseDate")).thenReturn(Instant.now().minus(1, ChronoUnit.DAYS));
        
        // Create a retract date in the future
        when(messageProperties.getInstantProperty("retractDate")).thenReturn(Instant.now().plus(1, ChronoUnit.DAYS));
        
        // Regular user case - message creator is different from current user
        when(messageCreator.getId()).thenReturn(OTHER_USER_ID);
        when(securityService.isSuperUser()).thenReturn(false);
        
        // Message should be accessible for regular user
        assertTrue("Regular user should be able to access message with valid dates", 
                filter.accept(message));
    }
    
    /**
     * Test service implementation with the isMessageViewable method exposed for testing
     */
    public class TestBaseAnnouncementService extends BaseAnnouncementService {
        
        @Override
        protected Storage newStorage() {
            return null; // Not needed for these tests
        }
        
        @Override
        public void init() {
            // Skip initialization for tests
        }
        
        // Make the inner class accessible for testing
        public PrivacyFilter newPrivacyFilter(org.sakaiproject.javax.Filter filter) {
            return new PrivacyFilter(filter);
        }
    }
}