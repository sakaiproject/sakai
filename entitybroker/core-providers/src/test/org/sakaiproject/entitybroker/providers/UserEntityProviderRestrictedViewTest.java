package org.sakaiproject.entitybroker.providers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.providers.model.EntityUser;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.BaseResourceProperties;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the simple view of a user for non-admins.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserEntityProviderRestrictedViewTest {

    private UserEntityProvider provider;

    @Mock
    private DeveloperHelperService developerHelperService;
    @Mock
    private ServerConfigurationService serverConfigurationService;
    @Mock
    private UserDirectoryService userDirectoryService;
    @Mock
    private SiteService siteService;
    @Mock
    private User user;
    @Mock
    private User admin;
    @Mock
    private Site adminSite;
    @Mock
    private Site userSite;
    @Mock
    private ToolConfiguration toolConfiguration;

    @Before
    public void setUp() throws Exception {
        provider = new UserEntityProvider();
        provider.setDeveloperHelperService(developerHelperService);
        provider.setServerConfigurationService(serverConfigurationService);
        provider.setUserDirectoryService(userDirectoryService);
        provider.setSiteService(siteService);

        // Setup the 2 users.
        when(user.getId()).thenReturn("1");
        when(admin.getId()).thenReturn("admin");
        when(user.getCreatedBy()).thenReturn(admin);
        when(user.getProperties()).thenReturn(new BaseResourceProperties());

        when(userDirectoryService.getUserByAid(any())).thenThrow(UserNotDefinedException.class);
        when(userDirectoryService.getUser("1")).thenReturn(user);
        when(userDirectoryService.getUser("admin")).thenReturn(admin);

        when(siteService.getSite("~admin")).thenReturn(adminSite);
        when(siteService.getUserSiteId("admin")).thenReturn("~admin");

        when(siteService.getSite("~1")).thenReturn(userSite);
        when(siteService.getUserSiteId("1")).thenReturn("~1");
    }

    @Test
    public void testGetEntityInternal() throws Exception {
        EntityReference ref = new EntityReference("/user/1");

        when(developerHelperService.getConfigurationSetting("entity.users.viewall", false)).thenReturn(false);
        when(developerHelperService.getConfigurationSetting("user.explicit.id.only", false)).thenReturn(false);
        when(developerHelperService.isEntityRequestInternal("/user/1")).thenReturn(true);
        when(developerHelperService.getCurrentUserId()).thenReturn("admin");

        when(adminSite.getToolForCommonId("sakai.profile2")).thenReturn(toolConfiguration);

        Object entity = provider.getEntity(ref);
        assertNotNull(entity);
        assertTrue(entity instanceof EntityUser);
        assertEquals("/user/admin", ((EntityUser)entity).getOwner()); // Full details
    }

    @Test
    public void testGetEntityAdmin() throws Exception {
        EntityReference ref = new EntityReference("/user/1");

        when(developerHelperService.getConfigurationSetting("entity.users.viewall", false)).thenReturn(false);
        when(developerHelperService.getConfigurationSetting("user.explicit.id.only", false)).thenReturn(false);
        when(developerHelperService.isEntityRequestInternal("/user/1")).thenReturn(false);
        when(developerHelperService.getCurrentUserReference()).thenReturn("/user/admin");
        when(developerHelperService.getCurrentUserId()).thenReturn("admin");
        when(developerHelperService.isUserAdmin("/user/admin")).thenReturn(true);

        when(adminSite.getToolForCommonId("sakai.profile2")).thenReturn(toolConfiguration);

        Object entity = provider.getEntity(ref);
        assertNotNull(entity);
        assertTrue(entity instanceof EntityUser);
        assertEquals("/user/admin", ((EntityUser)entity).getOwner()); // Full details
    }

    @Test
    public void testGetEntityFullDetails() throws Exception {
        EntityReference ref = new EntityReference("/user/1");

        when(developerHelperService.getConfigurationSetting("entity.users.viewall", false)).thenReturn(true);
        when(developerHelperService.getConfigurationSetting("user.explicit.id.only", false)).thenReturn(false);
        when(developerHelperService.getCurrentUserId()).thenReturn("admin");

        when(adminSite.getToolForCommonId("sakai.profile2")).thenReturn(toolConfiguration);

        Object entity = provider.getEntity(ref);
        assertNotNull(entity);
        assertTrue(entity instanceof EntityUser);
        assertEquals("/user/admin", ((EntityUser)entity).getOwner()); // Full details
    }

    @Test
    public void testGetEntitySelf() throws Exception {
        EntityReference ref = new EntityReference("/user/1");

        when(developerHelperService.getConfigurationSetting("entity.users.viewall", false)).thenReturn(false);
        when(developerHelperService.getConfigurationSetting("user.explicit.id.only", false)).thenReturn(false);
        when(developerHelperService.isEntityRequestInternal("/user/1")).thenReturn(false);
        when(developerHelperService.getCurrentUserReference()).thenReturn("/user/1");
        when(developerHelperService.getCurrentUserId()).thenReturn("1");
        when(developerHelperService.isUserAdmin("/user/1")).thenReturn(false);

        when(userSite.getToolForCommonId("sakai.profile2")).thenReturn(toolConfiguration);

        Object entity = provider.getEntity(ref);
        assertNotNull(entity);
        assertTrue(entity instanceof EntityUser);
        assertEquals("/user/admin", ((EntityUser)entity).getOwner()); // Full details
    }

    @Test(expected = SecurityException.class)
    public void testGetEntityNoProfile() throws Exception {
        EntityReference ref = new EntityReference("/user/admin");

        when(developerHelperService.getConfigurationSetting("entity.users.viewall", false)).thenReturn(false);
        when(developerHelperService.getConfigurationSetting("user.explicit.id.only", false)).thenReturn(false);
        when(developerHelperService.isEntityRequestInternal("/user/admin")).thenReturn(false);
        when(developerHelperService.getCurrentUserReference()).thenReturn("/user/1");
        when(developerHelperService.getCurrentUserId()).thenReturn("1");

        provider.getEntity(ref);
    }

    @Test(expected = SecurityException.class)
    public void testGetEntityHasProfile() throws Exception {
        EntityReference ref = new EntityReference("/user/admin");

        when(developerHelperService.getConfigurationSetting("entity.users.viewall", false)).thenReturn(false);
        when(developerHelperService.getConfigurationSetting("user.explicit.id.only", false)).thenReturn(false);
        when(developerHelperService.isEntityRequestInternal("/user/admin")).thenReturn(false);
        when(developerHelperService.getCurrentUserReference()).thenReturn("/user/1");
        when(developerHelperService.getCurrentUserId()).thenReturn("1");

        provider.getEntity(ref);

        when(userSite.getToolForCommonId("sakai.profile2")).thenReturn(toolConfiguration);

        Object entity = provider.getEntity(ref);
        assertNotNull(entity);
        assertTrue(entity instanceof EntityUser);
        assertNull(((EntityUser)entity).getOwner()); // Partial details
        assertEquals("admin", ((EntityUser)entity).getId());
    }
}
