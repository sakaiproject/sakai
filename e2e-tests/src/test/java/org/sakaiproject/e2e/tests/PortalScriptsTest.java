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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.ColorScheme;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Exercises the production portal scripts without requiring a running Sakai server. */
class PortalScriptsTest {

    private static Playwright playwright;
    private static Browser browser;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @ParameterizedTest
    @CsvSource({"DARK,notSet,true", "LIGHT,notSet,true", "DARK,light,true", "LIGHT,dark,true",
        "DARK,notSet,false", "LIGHT,notSet,false"})
    void appliesThemeWithoutSavingUntilUserChooses(ColorScheme osTheme, String preference, boolean showSwitcher)
            throws IOException {
        String script = Files.readString(Path.of("../library/src/webapp/js/portal/portal.theme.switcher.js"));
        AtomicReference<String> savedTheme = new AtomicReference<>("sakaiUserTheme-" + preference);
        List<String> saves = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            page.emulateMedia(new Page.EmulateMediaOptions().setColorScheme(osTheme));
            page.onPageError(errors::add);
            page.route("https://portal.test/**", route -> {
                if (route.request().url().endsWith("/theme.js")) {
                    route.fulfill(new Route.FulfillOptions().setContentType("text/javascript").setBody(script));
                } else if ("PUT".equals(route.request().method())) {
                    String theme = URI.create(route.request().url()).getQuery().substring("theme=".length());
                    saves.add(theme);
                    savedTheme.set(theme);
                    route.fulfill(new Route.FulfillOptions().setStatus(200));
                } else {
                    String switcher = showSwitcher
                        ? "<button id='sakai-dark-theme-switcher' aria-checked='false'>Theme</button>" : "";
                    route.fulfill(new Route.FulfillOptions().setContentType("text/html").setBody("""
                        <html class='%s'><body>%s<script>
                        var portal = { user: { id: 'test-user' }, userTheme: '%s',
                          userThemeAutoDetectDark: true,
                          i18n: { theme_switch_to_light: 'Light', theme_switch_to_dark: 'Dark' } };
                        </script><script src='/theme.js'></script></body></html>
                        """.formatted(savedTheme.get(), switcher, savedTheme.get())));
                }
            });

            String expected = "notSet".equals(preference)
                ? (osTheme == ColorScheme.DARK ? "dark" : "light") : preference;
            page.navigate("https://portal.test/portal");
            assertThat(page.locator("html")).hasClass("sakaiUserTheme-" + expected);
            page.reload();
            assertThat(page.locator("html")).hasClass("sakaiUserTheme-" + expected);
            assertTrue(saves.isEmpty(), "Loading and reloading must not save inferred or existing themes");
            assertEquals("sakaiUserTheme-" + preference, savedTheme.get());

            if (showSwitcher) {
                String selected = "sakaiUserTheme-" + ("dark".equals(expected) ? "light" : "dark");
                page.waitForResponse(response -> "PUT".equals(response.request().method()),
                    () -> page.locator("#sakai-dark-theme-switcher").click());
                assertEquals(List.of(selected), saves);
                page.reload();
                assertThat(page.locator("html")).hasClass(selected);
                assertEquals(List.of(selected), saves, "Reload must preserve the choice without saving again");
            }
            assertTrue(errors.isEmpty(), errors.toString());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void pinChoicesSaveOnceWithDebounceOrImmediateBulk(boolean bulk) throws IOException {
        List<String> saves = new ArrayList<>();
        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            List<String> errors = new ArrayList<>();
            page.onPageError(errors::add);
            String html = """
                <base href='https://portal.test/'>
                <div id='select-site-sidebar'>
                  <div class='fav-sites-card'>
                    <button class='favorites-select-all-none'><i class='si-pin'></i><i class='si-pin-fill'></i></button>
                    <div class='fav-sites-entry'>
                      <button id='pin-a' class='site-favorite-btn' data-site-id='a' data-pinned-state='pinned'
                          aria-pressed='true'><i class='si-pin'></i><i class='si-pin-fill'></i></button>
                    </div>
                    <div class='fav-sites-entry'>
                      <button id='pin-b' class='site-favorite-btn' data-site-id='b' data-pinned-state='unpinned'
                          aria-pressed='false'><i class='si-pin'></i><i class='si-pin-fill'></i></button>
                    </div>
                  </div>
                <div id='allsites-refresh-notification' style='display:none'>Saved</div>
                <ul id='organize-favorites-list'><li class='organize-favorite-item'>A</li></ul>
                </div>
                """;
            page.route("https://portal.test/**", route -> {
                if ("POST".equals(route.request().method())) {
                    saves.add(URLDecoder.decode(route.request().postData(), StandardCharsets.UTF_8));
                    route.fulfill(new Route.FulfillOptions().setStatus(200));
                } else {
                    route.fulfill(new Route.FulfillOptions().setContentType("text/html").setBody(html));
                }
            });
            page.navigate("https://portal.test/portal");
            // Sorting is not involved in this flow; only satisfy the external library's setup boundary.
            page.addScriptTag(new Page.AddScriptTagOptions().setContent("window.Sortable = { create: () => ({}) };"));
            page.addScriptTag(new Page.AddScriptTagOptions().setPath(
                Path.of("../library/src/webapp/js/portal/portal.all.sites.js")));
            page.evaluate("allsites.setup()");
            page.clock().install();
            page.clock().pauseAt(System.currentTimeMillis());

            page.locator("#pin-a").click();
            assertTrue(saves.isEmpty());
            if (bulk) {
                page.waitForResponse(response -> "POST".equals(response.request().method()),
                    () -> page.locator(".favorites-select-all-none").click());
            } else {
                page.locator("#pin-b").click();
                assertTrue(saves.isEmpty());
                page.waitForResponse(response -> "POST".equals(response.request().method()),
                    () -> page.clock().runFor(600));
            }
            page.clock().runFor(1000);

            String selected = bulk ? "[\"a\",\"b\"]" : "[\"b\"]";
            assertEquals(List.of("userFavorites={\"favoriteSiteIds\":" + selected + "}"), saves);
            assertThat(page.locator(".site-favorite-btn[aria-pressed=true]")).hasCount(bulk ? 2 : 1);
            assertThat(page.locator("#allsites-refresh-notification")).isVisible();
            assertTrue(errors.isEmpty(), errors.toString());
        }
    }
}
