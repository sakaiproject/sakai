package org.sakaiproject.poll.tool.mvc;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.PreferencesService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PollsLocaleServiceTest {

    @Test
    public void siteLocaleOverridesUserLocale() throws Exception {
        PreferencesService preferencesService = mock(PreferencesService.class);
        ServerConfigurationService serverConfigurationService = mock(ServerConfigurationService.class);
        SiteService siteService = mock(SiteService.class);
        ToolManager toolManager = mock(ToolManager.class);
        SessionManager sessionManager = mock(SessionManager.class);
        Placement placement = mock(Placement.class);
        Site site = mock(Site.class);
        ResourceProperties properties = mock(ResourceProperties.class);

        when(toolManager.getCurrentPlacement()).thenReturn(placement);
        when(placement.getContext()).thenReturn("site-id");
        when(siteService.getSite("site-id")).thenReturn(site);
        when(site.getProperties()).thenReturn(properties);
        when(properties.get("locale_string")).thenReturn("fr_FR");
        when(serverConfigurationService.getLocaleFromString("fr_FR")).thenReturn(Locale.FRANCE);
        when(sessionManager.getCurrentSessionUserId()).thenReturn("user1");
        when(preferencesService.getLocale("user1")).thenReturn(Locale.GERMANY);

        PollsLocaleService service = new PollsLocaleService(preferencesService, serverConfigurationService,
                siteService, toolManager, sessionManager);

        Assert.assertEquals(Locale.FRANCE, service.getLocaleForCurrentSiteAndUser());
    }

    @Test
    public void fallsBackToUserLocaleWhenSiteLocaleMissing() throws Exception {
        PreferencesService preferencesService = mock(PreferencesService.class);
        ServerConfigurationService serverConfigurationService = mock(ServerConfigurationService.class);
        SiteService siteService = mock(SiteService.class);
        ToolManager toolManager = mock(ToolManager.class);
        SessionManager sessionManager = mock(SessionManager.class);
        Placement placement = mock(Placement.class);
        Site site = mock(Site.class);
        ResourceProperties properties = mock(ResourceProperties.class);

        when(toolManager.getCurrentPlacement()).thenReturn(placement);
        when(placement.getContext()).thenReturn("site-id");
        when(siteService.getSite("site-id")).thenReturn(site);
        when(site.getProperties()).thenReturn(properties);
        when(properties.get("locale_string")).thenReturn(null);
        when(sessionManager.getCurrentSessionUserId()).thenReturn("user1");
        when(preferencesService.getLocale("user1")).thenReturn(Locale.GERMANY);

        PollsLocaleService service = new PollsLocaleService(preferencesService, serverConfigurationService,
                siteService, toolManager, sessionManager);

        Assert.assertEquals(Locale.GERMANY, service.getLocaleForCurrentSiteAndUser());
    }
}
