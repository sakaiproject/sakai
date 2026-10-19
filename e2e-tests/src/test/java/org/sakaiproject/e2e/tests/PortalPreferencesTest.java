/*
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
package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.options.ColorScheme;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class PortalPreferencesTest extends SakaiUiTestBase {

    private static final String THEME = "sakai:portal:theme";
    private static final String NAVIGATION = "sakai:portal:sitenav";

    @ParameterizedTest
    @EnumSource(value = ColorScheme.class, names = {"DARK", "LIGHT"})
    void navigationReadsPreferencesAndExplicitChoicesSurviveReload(ColorScheme osTheme) {
        page.emulateMedia(new Page.EmulateMediaOptions().setColorScheme(osTheme));
        String username = sakai.resolveUsername("instructor1");
        sakai.login(username);
        String firstSite = sakai.createCourse(username, List.of("sakai.announcements"));
        String secondSite = sakai.createProject(username, List.of("sakai.announcements"));
        page.navigate(firstSite);
        String siteId = firstSite.substring(firstSite.lastIndexOf('/') + 1);
        String userId = (String) page.evaluate("portal.user.id");
        Map<?, ?> originalTheme = preferences(THEME);
        Map<?, ?> originalNavigation = preferences(NAVIGATION);
        List<Request> writes = new ArrayList<>();
        page.onRequest(request -> {
            if (request.url().contains("/direct/userPrefs/") && !"GET".equals(request.method())) {
                writes.add(request);
            }
        });
        try {
            savePreferences(THEME, Map.of("theme", "sakaiUserTheme-notSet"));
            savePreferences(NAVIGATION, Map.of("currentExpanded", "false", "expandedSite", siteId));
            Map<String, Map<?, ?>> before = snapshot();
            writes.clear();

            page.navigate(firstSite);
            assertThat(currentSiteTools()).not().hasClass(Pattern.compile(".*\\bshow\\b.*"));
            page.reload();
            assertThat(currentSiteTools()).not().hasClass(Pattern.compile(".*\\bshow\\b.*"));
            assertEquals(before, snapshot(), "Reload must retain an explicit collapsed choice without saving");
            page.navigate(secondSite);
            assertThat(currentSiteTools()).hasClass(Pattern.compile(".*\\bshow\\b.*"));
            assertEquals(before, snapshot(), "Switching sites must derive expansion without changing preferences");
            page.navigate(firstSite);
            assertThat(currentSiteTools()).not().hasClass(Pattern.compile(".*\\bshow\\b.*"));
            assertEquals(before, snapshot());
            assertTrue(writes.isEmpty(), "Page loads must not issue preference mutations");

            page.waitForResponse(response -> response.url().contains("/direct/userPrefs/updateKey/")
                    && response.url().contains("currentExpanded=true"),
                () -> page.locator("li.is-current-site button[data-bs-toggle=collapse]").first().click());
            assertEquals("true", preferences(NAVIGATION).get("currentExpanded"));
            page.reload();
            assertThat(currentSiteTools()).hasClass(Pattern.compile(".*\\bshow\\b.*"));

            page.locator("[data-bs-target='#sakai-account-panel']:visible").first().click();
            Locator themeSwitcher = page.locator("#sakai-dark-theme-switcher");
            assertThat(themeSwitcher).isVisible();
            writes.clear();
            page.waitForResponse(response -> response.url().contains("/" + THEME + "?theme="),
                themeSwitcher::click);
            String chosen = osTheme == ColorScheme.DARK ? "sakaiUserTheme-light" : "sakaiUserTheme-dark";
            assertEquals(chosen, preferences(THEME).get("theme"));
            assertEquals(1, writes.size(), "One explicit theme click must save exactly once");
            Map<String, Map<?, ?>> after = snapshot();
            page.reload();
            assertThat(page.locator("html")).hasClass(Pattern.compile(".*" + chosen + ".*"));
            assertEquals(after, snapshot());
            assertEquals(1, writes.size(), "Reload must not save the chosen theme again");
        } finally {
            savePreferences(THEME, Map.of("theme", valueOr(originalTheme, "theme", "sakaiUserTheme-notSet")));
            savePreferences(NAVIGATION, Map.of(
                "currentExpanded", valueOr(originalNavigation, "currentExpanded", "true"),
                "expandedSite", valueOr(originalNavigation, "expandedSite", "~" + userId)));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void immediatePinSaveCancelsPendingIndividualSave(boolean reorder) {
        String username = sakai.resolveUsername("instructor1");
        sakai.login(username);
        String firstSite = sakai.createCourse(username, List.of("sakai.announcements"));
        sakai.createProject(username, List.of("sakai.announcements"));
        page.navigate(firstSite);
        String siteId = firstSite.substring(firstSite.lastIndexOf('/') + 1);
        Object originalFavorites = page.evaluate("async () => (await fetch('/portal/favorites/list')).json()");
        try {
            page.locator("[data-bs-target='#select-site-sidebar']:visible").first().click();
            Locator individualPin = page.locator("#select-site-sidebar .site-favorite-btn[data-site-id='" + siteId + "']");
            assertThat(individualPin).isVisible();
            assertThat(individualPin).hasAttribute("data-pinned-state", "pinned");
            List<Request> saves = new ArrayList<>();
            page.onRequest(request -> {
                if (request.url().contains("/portal/favorites/update") && "POST".equals(request.method())) {
                    saves.add(request);
                }
            });
            page.clock().install();
            page.clock().pauseAt(System.currentTimeMillis());
            individualPin.click();
            assertTrue(saves.isEmpty());
            if (reorder) {
                // Switch panes before the pending save can display the reload notification.
                page.locator("#allsites-organize-favourites-tab").click();
                Locator last = page.locator("#organize-favorites-list .organize-favorite-item").last();
                page.waitForResponse(response -> response.url().contains("/portal/favorites/update"),
                    () -> last.press("e"));
            } else {
                Locator bulkPin = individualPin.locator("xpath=ancestor::*[contains(@class,'fav-sites-card')]")
                    .locator(".favorites-select-all-none");
                page.waitForResponse(response -> response.url().contains("/portal/favorites/update"), bulkPin::click);
            }
            page.clock().runFor(1000);
            assertEquals(1, saves.size(), "The immediate save must cancel the pending 500ms save");
            Object expectedIds = page.evaluate(reorder
                ? "Array.from(document.querySelectorAll('#organize-favorites-list .organize-favorite-item')).map(el => el.dataset.siteId)"
                : "allsites.getPinnedSiteIds()");
            Object actualIds = page.evaluate("async () => (await (await fetch('/portal/favorites/list')).json()).favoriteSiteIds");
            if (reorder) {
                assertEquals(expectedIds, actualIds, "The final pin order must be persisted");
            } else {
                assertEquals(new HashSet<>((List<?>) expectedIds), new HashSet<>((List<?>) actualIds),
                    "The final pin selection must be persisted");
            }
        } finally {
            page.evaluate("""
                async favorites => {
                  const response = await fetch('/portal/favorites/update', { method: 'POST',
                    body: new URLSearchParams({ userFavorites: JSON.stringify(favorites), reorder: 'true' }) });
                  if (!response.ok) throw new Error(`Unable to restore favorites: ${response.status}`);
                }
                """, originalFavorites);
        }
    }

    private Locator currentSiteTools() {
        return page.locator("li.is-current-site .site-list-item-collapse").first();
    }

    private Map<String, Map<?, ?>> snapshot() {
        return Map.of("theme", preferences(THEME), "navigation", preferences(NAVIGATION));
    }

    private Map<?, ?> preferences(String key) {
        return (Map<?, ?>) page.evaluate("""
            async key => {
              const response = await fetch(`/direct/userPrefs/key/${portal.user.id}/${key}.json`);
              if (!response.ok) throw new Error(`Unable to read preferences: ${response.status}`);
              return (await response.json()).data;
            }
            """, key);
    }

    private void savePreferences(String key, Map<String, String> values) {
        page.evaluate("""
            async ({key, values}) => {
              const query = Object.entries(values).map(([name, value]) => `${name}=${encodeURIComponent(value)}`).join('&');
              const response = await fetch(`/direct/userPrefs/updateKey/${portal.user.id}/${key}?${query}`,
                { method: 'PUT' });
              if (!response.ok) throw new Error(`Unable to save preferences: ${response.status}`);
            }
            """, Map.of("key", key, "values", values));
    }

    private String valueOr(Map<?, ?> properties, String key, String fallback) {
        Object value = properties.get(key);
        return value == null ? fallback : value.toString();
    }
}
