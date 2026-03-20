/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.util.impl;

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

public class LocaleServiceImplTest {

    @Test
    public void siteLocaleOverridesUserLocale() {
        SiteService siteService = mock(SiteService.class);
        PreferencesService preferencesService = mock(PreferencesService.class);
        ToolManager toolManager = mock(ToolManager.class);
        SessionManager sessionManager = mock(SessionManager.class);

        when(siteService.getSiteLocale("site-id")).thenReturn(Optional.of(Locale.FRANCE));
        when(preferencesService.getLocale("user-id")).thenReturn(Locale.GERMANY);

        LocaleServiceImpl localeService = buildService(siteService, preferencesService, toolManager, sessionManager);

        Assert.assertEquals(Locale.FRANCE, localeService.getLocaleForSiteAndUser("site-id", "user-id"));
    }

    @Test
    public void fallsBackToUserLocaleWhenSiteLocaleMissing() {
        SiteService siteService = mock(SiteService.class);
        PreferencesService preferencesService = mock(PreferencesService.class);
        ToolManager toolManager = mock(ToolManager.class);
        SessionManager sessionManager = mock(SessionManager.class);

        when(siteService.getSiteLocale("site-id")).thenReturn(Optional.empty());
        when(preferencesService.getLocale("user-id")).thenReturn(Locale.GERMANY);

        LocaleServiceImpl localeService = buildService(siteService, preferencesService, toolManager, sessionManager);

        Assert.assertEquals(Locale.GERMANY, localeService.getLocaleForSiteAndUser("site-id", "user-id"));
    }

    @Test
    public void fallsBackToDefaultLocaleWhenSiteAndUserAreMissing() {
        SiteService siteService = mock(SiteService.class);
        PreferencesService preferencesService = mock(PreferencesService.class);
        ToolManager toolManager = mock(ToolManager.class);
        SessionManager sessionManager = mock(SessionManager.class);

        LocaleServiceImpl localeService = buildService(siteService, preferencesService, toolManager, sessionManager);

        Assert.assertEquals(Locale.getDefault(), localeService.getLocaleForSiteAndUser(null, null));
    }

    @Test
    public void resolvesCurrentSiteAndUserFromManagers() {
        SiteService siteService = mock(SiteService.class);
        PreferencesService preferencesService = mock(PreferencesService.class);
        ToolManager toolManager = mock(ToolManager.class);
        SessionManager sessionManager = mock(SessionManager.class);
        Placement placement = mock(Placement.class);

        when(toolManager.getCurrentPlacement()).thenReturn(placement);
        when(placement.getContext()).thenReturn("site-id");
        when(sessionManager.getCurrentSessionUserId()).thenReturn("user-id");
        when(siteService.getSiteLocale("site-id")).thenReturn(Optional.empty());
        when(preferencesService.getLocale("user-id")).thenReturn(Locale.ITALY);

        LocaleServiceImpl localeService = buildService(siteService, preferencesService, toolManager, sessionManager);

        Assert.assertEquals(Locale.ITALY, localeService.getLocaleForCurrentSiteAndUser());
    }

    @Test
    public void fallsBackToUserLocaleWhenSiteLookupThrows() {
        SiteService siteService = mock(SiteService.class);
        PreferencesService preferencesService = mock(PreferencesService.class);
        ToolManager toolManager = mock(ToolManager.class);
        SessionManager sessionManager = mock(SessionManager.class);

        when(siteService.getSiteLocale("site-id")).thenThrow(new RuntimeException("boom"));
        when(preferencesService.getLocale("user-id")).thenReturn(Locale.CANADA_FRENCH);

        LocaleServiceImpl localeService = buildService(siteService, preferencesService, toolManager, sessionManager);

        Assert.assertEquals(Locale.CANADA_FRENCH, localeService.getLocaleForSiteAndUser("site-id", "user-id"));
    }

    @Test
    public void neverReturnsNullWhenSiteServiceReturnsNullOptional() {
        SiteService siteService = mock(SiteService.class);
        PreferencesService preferencesService = mock(PreferencesService.class);
        ToolManager toolManager = mock(ToolManager.class);
        SessionManager sessionManager = mock(SessionManager.class);

        when(siteService.getSiteLocale("site-id")).thenReturn(null);
        when(preferencesService.getLocale("user-id")).thenReturn(null);

        LocaleServiceImpl localeService = buildService(siteService, preferencesService, toolManager, sessionManager);

        Assert.assertNotNull(localeService.getLocaleForSiteAndUser("site-id", "user-id"));
    }

    @Test
    public void neverReturnsNullWhenPlacementResolutionThrows() {
        SiteService siteService = mock(SiteService.class);
        PreferencesService preferencesService = mock(PreferencesService.class);
        ToolManager toolManager = mock(ToolManager.class);
        SessionManager sessionManager = mock(SessionManager.class);

        when(toolManager.getCurrentPlacement()).thenThrow(new RuntimeException("boom"));
        when(sessionManager.getCurrentSessionUserId()).thenReturn("user-id");
        when(preferencesService.getLocale("user-id")).thenReturn(null);

        LocaleServiceImpl localeService = buildService(siteService, preferencesService, toolManager, sessionManager);

        Assert.assertNotNull(localeService.getLocaleForCurrentSiteAndUser());
    }

    private LocaleServiceImpl buildService(SiteService siteService, PreferencesService preferencesService,
            ToolManager toolManager, SessionManager sessionManager) {
        LocaleServiceImpl localeService = new LocaleServiceImpl();
        localeService.setSiteService(siteService);
        localeService.setPreferencesService(preferencesService);
        localeService.setToolManager(toolManager);
        localeService.setSessionManager(sessionManager);
        localeService.init();
        return localeService;
    }
}
