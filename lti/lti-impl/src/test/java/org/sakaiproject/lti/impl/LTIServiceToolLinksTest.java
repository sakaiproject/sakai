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
package org.sakaiproject.lti.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.lti.api.model.LtiContent;
import org.sakaiproject.lti.api.model.LtiTool;
import org.sakaiproject.lti.api.repository.LtiContentRepository;
import org.sakaiproject.lti.api.repository.LtiToolRepository;
import org.sakaiproject.lti.beans.LtiToolLinkPage;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.api.LocaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

@ContextConfiguration(classes = LtiTestConfiguration.class)
public class LTIServiceToolLinksTest extends AbstractTransactionalJUnit4SpringContextTests {
    @Autowired private LTIService ltiService;
    @Autowired private LtiContentRepository contents;
    @Autowired private LtiToolRepository tools;
    @Autowired private SiteService sites;
    @Autowired private SessionManager sessions;
    @Autowired private LocaleService locales;
    @Autowired @Qualifier("org.sakaiproject.time.api.UserTimeService") private UserTimeService times;
    @Autowired @Qualifier("toolLinksSiteA") private Site siteA;
    @Autowired @Qualifier("toolLinksSiteB") private Site siteB;
    @Autowired @Qualifier("toolLinksSiteProperties") private ResourceProperties properties;

    @Before
    public void setUp() {
        reset(sites, sessions, locales, times, siteA, siteB, properties);
        when(sessions.getCurrentSessionUserId()).thenReturn("instructor");
        when(sites.allowUpdateSite("site-a")).thenReturn(true);
        when(sites.getOptionalSite("site-a")).thenReturn(Optional.of(siteA));
        when(sites.getOptionalSite("site-b")).thenReturn(Optional.of(siteB));
        when(siteA.getTitle()).thenReturn("Course A");
        when(siteB.getTitle()).thenReturn("Course B");
        when(siteA.getUrl()).thenReturn("/portal/site/site-a");
        when(siteB.getUrl()).thenReturn("/portal/site/site-b");
        when(siteA.getProperties()).thenReturn(properties);
        when(properties.getProperty("contact-name")).thenReturn("Contact A");
        when(properties.getProperty("contact-email")).thenReturn("contact@example.com");
        when(locales.getLocaleForSiteAndUser("site-a", "instructor")).thenReturn(Locale.FRANCE);
        when(locales.getLocaleForSiteAndUser(LTIService.ADMIN_SITE, "instructor")).thenReturn(Locale.FRANCE);
        when(times.getLocalTimeZone()).thenReturn(TimeZone.getTimeZone("America/New_York"));
    }

    @Test
    public void requiresAuthenticatedMaintainerIncludingAdminScope() {
        assertThrows(SecurityException.class, () -> page("site-b", null, 0, 50, Map.of()));
        assertThrows(SecurityException.class, () -> page(LTIService.ADMIN_SITE, null, 0, 50, Map.of()));
        when(sessions.getCurrentSessionUserId()).thenReturn(null);
        assertThrows(SecurityException.class, () -> page("site-a", null, 0, 50, Map.of()));
    }

    @Test
    public void pagesAndFiltersWithinVisibleSiteAndTool() {
        LtiTool first = tool("https://example.com/fallback");
        LtiTool second = tool("https://example.com/second");
        content(first, "site-a", "Bravo", null);
        content(first, "site-a", "Alpha", "https://example.com/override");
        content(second, "site-a", "Charlie", null);
        content(first, "site-b", "Other site", null);
        content(first, null, "Global", null);

        LtiToolLinkPage page = page("site-a", first.getId(), 1, 1, Map.of());
        assertEquals(3, page.getTotal());
        assertEquals(3, page.getFiltered());
        assertEquals(1, page.getLinks().size());
        assertEquals("Bravo", page.getLinks().get(0).getTitle());
        assertEquals("https://example.com/fallback", page.getLinks().get(0).getLaunch());
        assertNull(page.getLinks().get(0).getSiteContactName());
        assertNull(page("site-a", first.getId(), 2, 1, Map.of()).getLinks().get(0).getLaunchUrl());

        LtiToolLinkPage filtered = page("site-a", first.getId(), 0, 50, Map.of("searchURL", "override"));
        assertEquals(3, filtered.getTotal());
        assertEquals(1, filtered.getFiltered());
        assertEquals("Alpha", filtered.getLinks().get(0).getTitle());
        assertTrue(page("site-a", first.getId(), 100, 50, Map.of()).getLinks().isEmpty());
        assertEquals(0, page("site-a", first.getId(), 0, 50, Map.of("title", "absent")).getFiltered());
    }

    @Test
    public void combinesFiltersAndTreatsSearchTokensAsLiteralText() {
        LtiTool tool = tool("https://example.com/fallback");
        content(tool, "site-a", "Alpha#|#one", "https://example.com/override");
        content(tool, "site-a", "Alpha", null);
        assertEquals(1, page("site-a", null, 0, 50, Map.of("title", "#|#")).getFiltered());
        assertEquals(0, page("site-a", null, 0, 50,
                Map.of("title", "#|#", "searchURL", "fallback")).getFiltered());
    }

    @Test
    public void adminCanSearchSiteColumnsAndSortDescending() {
        when(sites.allowUpdateSite(LTIService.ADMIN_SITE)).thenReturn(true);
        LtiTool tool = tool("https://example.com");
        content(tool, "site-a", "Alpha", null);
        content(tool, "site-b", "Bravo", null);
        LtiToolLinkPage page = ltiService.getToolLinks(LTIService.ADMIN_SITE, null, 0, 50,
                "title", false, Map.of());
        assertEquals(List.of("Bravo", "Alpha"), page.getLinks().stream().map(LtiToolLinkPage.Link::getTitle).toList());
        LtiToolLinkPage filtered = page(LTIService.ADMIN_SITE, null, 0, 50, Map.of("SITE_TITLE", "Course A"));
        assertEquals(2, filtered.getTotal());
        assertEquals(1, filtered.getFiltered());
        assertEquals("Contact A", filtered.getLinks().get(0).getSiteContactName());
        assertEquals("/portal/site/site-a", filtered.getLinks().get(0).getSiteUrl());
    }

    @Test
    public void boundsPagesAndRejectsHiddenFields() {
        for (int length : new int[] {-1, 0, 201}) {
            assertThrows(IllegalArgumentException.class, () -> page("site-a", null, 0, length, Map.of()));
        }
        assertThrows(IllegalArgumentException.class, () -> page("site-a", null, -1, 50, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> page("site-a", null, 0, 50, Map.of("placementsecret", "secret")));
        assertThrows(IllegalArgumentException.class, () -> page("site-a", null, 0, 50, Map.of("SITE_TITLE", "other")));
        assertThrows(IllegalArgumentException.class, () -> ltiService.getToolLinks("site-a", null, 0, 50, "placementsecret", true, Map.of()));
    }

    @Test
    public void usesSakaiLocaleAndTimezoneAndDoesNotSerializeSecrets() throws Exception {
        LtiContent content = content(tool("https://example.com"), "site-a", "<b>Title</b>", null);
        LtiToolLinkPage page = page("site-a", null, 0, 50, Map.of());
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(Locale.FRANCE).withZone(TimeZone.getTimeZone("America/New_York").toZoneId());
        assertEquals(formatter.format(content.getCreatedAt()), page.getLinks().get(0).getCreatedAt());
        String json = new ObjectMapper().writeValueAsString(page);
        assertFalse(json.contains("placementsecret"));
        assertFalse(json.contains("private-credential"));
        assertFalse(json.contains("custom"));
        assertEquals(1, page("site-a", null, 0, 50, Map.of("created_at", page.getLinks().get(0).getCreatedAt())).getFiltered());
    }

    private LtiToolLinkPage page(String site, Long tool, int start, int length, Map<String, String> filters) {
        return ltiService.getToolLinks(site, tool, start, length, "title", true, filters);
    }

    private LtiTool tool(String launch) {
        LtiTool tool = new LtiTool();
        tool.setTitle("Test tool");
        tool.setLaunch(launch);
        tool.setSiteId(LTIService.ADMIN_SITE);
        return tools.save(tool);
    }

    private LtiContent content(LtiTool tool, String site, String title, String launch) {
        LtiContent content = new LtiContent();
        content.setTool(tool);
        content.setSiteId(site);
        content.setTitle(title);
        content.setLaunch(launch);
        content.setPlacementsecret("private-credential");
        content.setCustom("private-configuration");
        return contents.save(content);
    }
}
