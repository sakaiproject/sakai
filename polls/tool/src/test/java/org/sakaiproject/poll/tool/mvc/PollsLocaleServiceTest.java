package org.sakaiproject.poll.tool.mvc;

import java.util.Locale;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
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
        SiteService siteService = mock(SiteService.class);
        ToolManager toolManager = mock(ToolManager.class);
        SessionManager sessionManager = mock(SessionManager.class);
        Placement placement = mock(Placement.class);

        when(toolManager.getCurrentPlacement()).thenReturn(placement);
        when(placement.getContext()).thenReturn("site-id");
        when(siteService.getSiteLocale("site-id")).thenReturn(Optional.of(Locale.FRANCE));
        when(sessionManager.getCurrentSessionUserId()).thenReturn("user1");
        when(preferencesService.getLocale("user1")).thenReturn(Locale.GERMANY);

        PollsLocaleService service = new PollsLocaleService(preferencesService, siteService, toolManager, sessionManager);

        Assert.assertEquals(Locale.FRANCE, service.getLocaleForCurrentSiteAndUser());
    }

    @Test
    public void fallsBackToUserLocaleWhenSiteLocaleMissing() throws Exception {
        PreferencesService preferencesService = mock(PreferencesService.class);
        SiteService siteService = mock(SiteService.class);
        ToolManager toolManager = mock(ToolManager.class);
        SessionManager sessionManager = mock(SessionManager.class);
        Placement placement = mock(Placement.class);

        when(toolManager.getCurrentPlacement()).thenReturn(placement);
        when(placement.getContext()).thenReturn("site-id");
        when(siteService.getSiteLocale("site-id")).thenReturn(Optional.empty());
        when(sessionManager.getCurrentSessionUserId()).thenReturn("user1");
        when(preferencesService.getLocale("user1")).thenReturn(Locale.GERMANY);

        PollsLocaleService service = new PollsLocaleService(preferencesService, siteService, toolManager, sessionManager);

        Assert.assertEquals(Locale.GERMANY, service.getLocaleForCurrentSiteAndUser());
    }
}
