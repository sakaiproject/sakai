package org.sakaiproject.announcement.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.announcement.api.AnnouncementChannel;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementMessageHeader;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.message.api.Message;
import org.sakaiproject.message.api.MessageHeader;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.api.FormattedText;

/**
 * Unit tests for BaseAnnouncementService, focusing on security checks
 */
@RunWith(MockitoJUnitRunner.class)
public class BaseAnnouncementServiceTest {

    private TestableBaseAnnouncementService service;
    
    @Mock private FunctionManager functionManager;
    @Mock private SecurityService securityService;
    @Mock private SiteService siteService;
    @Mock private UserDirectoryService userDirectoryService;
    @Mock private ServerConfigurationService serverConfigurationService;
    @Mock private EntityManager entityManager;
    @Mock private EventTrackingService eventTrackingService;
    @Mock private MemoryService memoryService;
    @Mock private ThreadLocalManager threadLocalManager;
    @Mock private FormattedText formattedText;
    @Mock private SessionManager sessionManager;
    @Mock private AuthzGroupService authzGroupService;
    
    // Test data objects
    @Mock private Site site;
    @Mock private Group group1;
    @Mock private Group group2;
    @Mock private User user;
    @Mock private AnnouncementMessage message;
    @Mock private AnnouncementMessageHeader messageHeader;
    @Mock private Reference messageRef;
    @Mock private TestableAnnouncementChannel channel;
    
    private static final String CONTEXT = "site-123";
    private static final String USER_ID = "user-123";
    private static final String MESSAGE_ID = "message-123";
    private static final String GROUP_1_REF = "/site/site-123/group/group-1";
    private static final String GROUP_2_REF = "/site/site-123/group/group-2";
    private static final String MESSAGE_REF = "/announcement/msg/site-123/channel-id/message-123";
    private static final String CHANNEL_REF = "/announcement/channel/site-123/channel-id";
    
    @Before
    public void setUp() throws Exception {
        service = new TestableBaseAnnouncementService();
        
        // Set up service dependencies
        service.setFunctionManager(functionManager);
        service.setSecurityService(securityService);
        service.setSiteService(siteService);
        service.setUserDirectoryService(userDirectoryService);
        service.setServerConfigurationService(serverConfigurationService);
        service.setEntityManager(entityManager);
        service.setEventTrackingService(eventTrackingService);
        service.setMemoryService(memoryService);
        service.setThreadLocalManager(threadLocalManager);
        service.setFormattedText(formattedText);
        service.setSessionManager(sessionManager);
        service.setAuthzGroupService(authzGroupService);
        
        // Common setup for tests
        when(sessionManager.getCurrentSessionUserId()).thenReturn(USER_ID);
        when(messageRef.getContext()).thenReturn(CONTEXT);
        when(messageRef.getId()).thenReturn(MESSAGE_ID);
        when(messageRef.getReference()).thenReturn(MESSAGE_REF);
        
        when(message.getAnnouncementHeader()).thenReturn(messageHeader);
        when(message.getHeader()).thenReturn(messageHeader);
        when(message.getId()).thenReturn(MESSAGE_ID);
        when(message.getReference()).thenReturn(MESSAGE_REF);
        
        when(messageHeader.getDraft()).thenReturn(false);
        when(messageHeader.getAccess()).thenReturn(MessageHeader.MessageAccess.CHANNEL);
        
        when(channel.findMessage(MESSAGE_ID)).thenReturn(message);
        when(channel.getContext()).thenReturn(CONTEXT);
        when(channel.getReference()).thenReturn(CHANNEL_REF);
    }

    /**
     * Test that a user can access a message when they have permission to the channel
     * and the message is not restricted by group
     */
    @Test
    public void testGetAnnouncementMessageWithChannelAccess() throws Exception {
        // Setup permissions
        when(securityService.unlock(USER_ID, AnnouncementService.SECURE_ANNC_READ, MESSAGE_REF)).thenReturn(true);
        when(channel.allowGetMessage(CHANNEL_REF, MESSAGE_REF)).thenReturn(true);
        
        // Call method under test
        AnnouncementMessage result = channel.getAnnouncementMessage(MESSAGE_ID);
        
        // Verify result
        assertNotNull("Should return the message", result);
        assertEquals("Should return the requested message", MESSAGE_ID, result.getId());
    }

    /**
     * Test that a user can't access a message when they have permission to the channel
     * but the message is restricted to a group they don't belong to
     */
    @Test
    public void testGetAnnouncementMessageWithGroupRestriction() throws Exception {
        // Setup message with group access
        when(messageHeader.getAccess()).thenReturn(MessageHeader.MessageAccess.GROUPED);
        Collection<String> messageGroups = new ArrayList<>();
        messageGroups.add(GROUP_1_REF);
        when(messageHeader.getGroups()).thenReturn(messageGroups);
        
        // Setup permissions - user has channel access but not group access
        when(securityService.unlock(USER_ID, AnnouncementService.SECURE_ANNC_READ, MESSAGE_REF)).thenReturn(true);
        when(channel.allowGetMessage(CHANNEL_REF, MESSAGE_REF)).thenReturn(true);
        
        // Setup site and groups
        when(siteService.getSite(CONTEXT)).thenReturn(site);
        Collection<Group> allowedGroups = new Vector<>();
        allowedGroups.add(group2); // User only has access to group2, not group1
        when(group2.getReference()).thenReturn(GROUP_2_REF);
        
        // Setup service to return empty allowed groups for the function
        service.setAllowedGroups(allowedGroups);
        
        // Expect a permission exception
        try {
            channel.getAnnouncementMessage(MESSAGE_ID);
            fail("Should throw PermissionException when user doesn't have access to the message's group");
        } catch (PermissionException e) {
            // Expected
        }
    }

    /**
     * Test that a user can access a message when they have both channel permission
     * and access to the group the message is restricted to
     */
    @Test
    public void testGetAnnouncementMessageWithGroupAccess() throws Exception {
        // Setup message with group access
        when(messageHeader.getAccess()).thenReturn(MessageHeader.MessageAccess.GROUPED);
        Collection<String> messageGroups = new ArrayList<>();
        messageGroups.add(GROUP_1_REF);
        when(messageHeader.getGroups()).thenReturn(messageGroups);
        
        // Setup permissions - user has channel access
        when(securityService.unlock(USER_ID, AnnouncementService.SECURE_ANNC_READ, MESSAGE_REF)).thenReturn(true);
        when(channel.allowGetMessage(CHANNEL_REF, MESSAGE_REF)).thenReturn(true);
        
        // Setup site and groups
        when(siteService.getSite(CONTEXT)).thenReturn(site);
        Collection<Group> allowedGroups = new Vector<>();
        allowedGroups.add(group1); // User has access to group1, which is the message's group
        when(group1.getReference()).thenReturn(GROUP_1_REF);
        
        // Setup service to return allowed groups for the function
        service.setAllowedGroups(allowedGroups);
        
        // Call method under test
        AnnouncementMessage result = channel.getAnnouncementMessage(MESSAGE_ID);
        
        // Verify result
        assertNotNull("Should return the message", result);
        assertEquals("Should return the requested message", MESSAGE_ID, result.getId());
    }

    /**
     * Test that a site admin (with all groups permission) can access a message
     * even if it's restricted to a group they don't belong to
     */
    @Test
    public void testGetAnnouncementMessageWithAllGroupsPermission() throws Exception {
        // Setup message with group access
        when(messageHeader.getAccess()).thenReturn(MessageHeader.MessageAccess.GROUPED);
        Collection<String> messageGroups = new ArrayList<>();
        messageGroups.add(GROUP_1_REF);
        when(messageHeader.getGroups()).thenReturn(messageGroups);
        
        // Setup permissions
        when(securityService.unlock(USER_ID, AnnouncementService.SECURE_ANNC_READ, MESSAGE_REF)).thenReturn(true);
        when(channel.allowGetMessage(CHANNEL_REF, MESSAGE_REF)).thenReturn(true);
        
        // Setup site and user as having "all groups" permission
        when(siteService.getSite(CONTEXT)).thenReturn(site);
        // Note: We're not checking site membership directly
        when(securityService.unlock(USER_ID, 
                AnnouncementService.SECURE_ANNC_ALL_GROUPS, 
                siteService.siteReference(CONTEXT))).thenReturn(true);
                
        // Call method under test
        AnnouncementMessage result = channel.getAnnouncementMessage(MESSAGE_ID);
        
        // Verify result
        assertNotNull("Should return the message", result);
        assertEquals("Should return the requested message", MESSAGE_ID, result.getId());
    }

    /**
     * Test that a draft message can only be accessed by the owner or admin
     */
    @Test
    public void testGetAnnouncementMessageDraft() throws Exception {
        // Setup draft message
        when(messageHeader.getDraft()).thenReturn(true);
        
        // Setup user as non-owner, non-admin
        User msgCreator = mock(User.class);
        when(msgCreator.getId()).thenReturn("other-user");
        when(messageHeader.getFrom()).thenReturn(msgCreator);
        when(securityService.isSuperUser()).thenReturn(false);
        
        // Setup basic permissions
        when(securityService.unlock(USER_ID, AnnouncementService.SECURE_ANNC_READ, MESSAGE_REF)).thenReturn(true);
        when(channel.allowGetMessage(CHANNEL_REF, MESSAGE_REF)).thenReturn(true);
        when(channel.unlockCheck(AnnouncementService.SECURE_ANNC_READ_DRAFT, MESSAGE_REF)).thenReturn(false);
        
        // Expect a permission exception
        try {
            channel.getAnnouncementMessage(MESSAGE_ID);
            fail("Should throw PermissionException when user doesn't have access to draft message");
        } catch (PermissionException e) {
            // Expected
        }
        
        // Now test with the owner
        when(msgCreator.getId()).thenReturn(USER_ID);
        
        // Call method under test
        AnnouncementMessage result = channel.getAnnouncementMessage(MESSAGE_ID);
        
        // Verify result
        assertNotNull("Owner should be able to access draft", result);
        
        // Now test with admin
        when(msgCreator.getId()).thenReturn("other-user");
        when(securityService.isSuperUser()).thenReturn(true);
        
        // Call method under test
        result = channel.getAnnouncementMessage(MESSAGE_ID);
        
        // Verify result
        assertNotNull("Admin should be able to access draft", result);
    }
    
    /**
     * Testable extension of BaseAnnouncementService to allow us to use it in tests
     */
    class TestableBaseAnnouncementService extends BaseAnnouncementService {
        private Collection<Group> allowedGroups;
        
        public void setAllowedGroups(Collection<Group> groups) {
            this.allowedGroups = groups;
        }
        
        @Override
        protected Collection<Group> getGroupsAllowFunction(String function, String context, String reference) {
            return allowedGroups;
        }
        
        @Override
        public void init() {
            // Don't do anything in init for tests
        }
        
        @Override
        protected Storage newStorage() {
            // Stub implementation just for testing
            return null;
        }
    }
    
    /**
     * Test interface that merges AnnouncementChannel methods we need access to
     */
    interface TestableAnnouncementChannel extends AnnouncementChannel {
        Message findMessage(String messageId);
        boolean allowGetMessage(String channelRef, String messageRef);
        boolean unlockCheck(String function, String reference);
    }
}