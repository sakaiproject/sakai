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
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.AriaRole;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Exercises the shipped Lit bundles with tool markup and HTTP boundary fixtures. */
class LitComponentsMigrationTest {

    private static final Path FRONTEND = Path.of("../webcomponents/tool/src/main/frontend");
    private static final String SCORE = """
        {"id":"score","siteId":"site","itemId":"source","type":"SCORE",
         "operator":"GREATER_THAN","argument":"10"}
        """;
    private Playwright playwright;
    private Browser browser;
    private Page page;
    private String markup;
    private final List<String> writes = new ArrayList<>();

    @BeforeEach
    void openBrowser() {
        assertTrue(Files.isRegularFile(FRONTEND.resolve("bundles/base.js")),
                "Build the webcomponents frontend with npm run bundle before running this test");
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
        page = browser.newPage();
        page.route("http://sakai.test/**", this::respond);
    }

    @AfterEach
    void closeBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    private void openFixture(String content) {
        markup = """
            <!doctype html><html lang="en"><head><title>Tool integration</title>
            <script>window.portal = { locale: 'en_US', user: { id: 'teacher' } };</script>
            <script type="module" src="/webcomponents/bundles/base.js"></script>
            </head><body>
            """ + content + "</body></html>";
        page.navigate("http://sakai.test/fixture");
    }

    private void respond(Route route) {
        URI uri = URI.create(route.request().url());
        String path = uri.getPath();
        try {
            if (path.startsWith("/webcomponents/bundles/")) {
                String filename = path.substring("/webcomponents/bundles/".length());
                Path bundle = FRONTEND.resolve("bundles").resolve(filename).normalize();
                if (!bundle.startsWith(FRONTEND.resolve("bundles")) || !Files.isRegularFile(bundle)) {
                    route.fulfill(new Route.FulfillOptions().setStatus(404));
                    return;
                }
                route.fulfill(new Route.FulfillOptions().setContentType("text/javascript").setPath(bundle));
            } else if (path.equals("/sakai-ws/rest/i18n/getI18nProperties")) {
                String name = List.of(uri.getQuery().split("&")).stream()
                        .filter(pair -> pair.startsWith("resourcebundle="))
                        .map(pair -> URLDecoder.decode(pair.substring("resourcebundle=".length()), StandardCharsets.UTF_8))
                        .findFirst().orElseThrow();
                Properties properties = new Properties();
                try (InputStream input = Files.newInputStream(Path.of("../webcomponents/bundle/src/main/bundle", name + ".properties"))) {
                    properties.load(input);
                }
                String body = properties.stringPropertyNames().stream().sorted()
                        .map(key -> key + "=" + properties.getProperty(key)).collect(Collectors.joining("\n"));
                route.fulfill(new Route.FulfillOptions().setContentType("text/plain").setBody(body));
            } else if (path.startsWith("/api/sites/site/items/")) {
                json(route, """
                    [{"name":"Group A","items":[{"id":"a1","name":"Essay"},{"id":"a2","name":"Quiz"}]},
                     {"name":"Group B","items":[{"id":"b1","name":"Essay"}]}]
                    """);
            } else if (path.equals("/direct/lessons/lesson/1.json")) {
                json(route, "{\"contentsList\":[{\"id\":\"source\",\"name\":\"Essay\"}]}");
            } else if (path.startsWith("/api/sites/site/conditions")) {
                conditionResponse(route, uri);
            } else if (path.contains("rubric-associations/")) {
                json(route, "{\"id\":\"association\",\"rubricId\":\"rubric\"}");
            } else if (path.equals("/api/sites/site/rubrics/rubric")) {
                json(route, """
                    {"id":"rubric","title":"pub.published.item","criteria":[
                      {"id":"one","title":"Explanation","ratings":[{"id":"r1","points":2}]},
                      {"id":"two","title":"Evidence","ratings":[{"id":"r2","points":3}]}]}
                    """);
            } else if (path.contains("rubric-evaluations/") && route.request().method().equals("GET")) {
                json(route, """
                    {"id":"evaluation","criterionOutcomes":[{"criterionId":"one","selectedRatingId":"r1"}]}
                    """);
            } else if (path.equals("/api/sites/site/rubrics/adhoc") || path.contains("rubric-evaluations")) {
                writes.add(route.request().url() + " " + route.request().postData());
                json(route, route.request().postData());
            } else {
                route.fulfill(new Route.FulfillOptions().setContentType("text/html").setBody(markup));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load production component fixture", exception);
        }
    }

    private void conditionResponse(Route route, URI uri) {
        if (route.request().method().equals("GET")) {
            json(route, uri.getQuery() == null ? "[" + SCORE + "]" : "[]");
        } else {
            String body = route.request().postData();
            writes.add(route.request().method() + " " + uri.getPath() + " " + body);
            if (route.request().method().equals("POST")) {
                String id = body.contains("\"ROOT\"") ? "root" : body.contains("\"PARENT\"") ? "parent" : "created";
                json(route, "{\"id\":\"" + id + "\"," + body.substring(1));
            } else if (route.request().method().equals("DELETE")) {
                route.fulfill(new Route.FulfillOptions().setContentType("text/plain").setBody("created"));
            } else {
                json(route, body);
            }
        }
    }

    private void json(Route route, String body) {
        route.fulfill(new Route.FulfillOptions().setContentType("application/json").setBody(body));
    }

    @Test
    void gradebookSelectionUpdatesTheToolFormWithOneItemPerGradebook() {
        openFixture("""
            <form><input type="hidden" id="selected" name="gradebooks" value="a1">
              <sakai-multi-gradebook site-id="site" app-name="sakai.samigo"
                  selected-temp="a1" input-id="selected"></sakai-multi-gradebook>
            </form>
            """);
        Locator search = page.locator("sakai-multi-gradebook sakai-tag-selector input");
        search.click();
        page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("Group A - Quiz").setExact(true)).click();
        assertThat(page.locator("#selected")).hasValue("a2");
        search.fill("Group B");
        search.press("ArrowDown");
        search.press("Enter");
        assertThat(page.locator("#selected")).hasValue("a2,b1");
        assertEquals("a2,b1", page.evaluate("new FormData(document.querySelector('form')).get('gradebooks')"));
        assertEquals("http://sakai.test/fixture", page.url());
    }

    @Test
    void lessonsCreatesAndRemovesConditionsWithoutSubmittingTheHostForm() {
        openFixture("""
            <form><sakai-condition-editor site-id="site" tool-id="sakai.lessonbuildertool" item-id="target"></sakai-condition-editor></form>
            """);
        Locator editor = page.locator("sakai-condition-editor");
        editor.getByRole(AriaRole.TEXTBOX, new Locator.GetByRoleOptions().setName("Points")).fill("12");
        editor.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Add condition").setExact(true)).click();
        assertThat(editor.locator(".condition-row")).hasCount(1);
        assertTrue(writes.get(0).contains("\"argument\":\"12\""));
        editor.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Remove condition").setExact(true)).click();
        assertThat(editor.locator(".condition-row")).hasCount(0);
        assertTrue(writes.get(1).startsWith("DELETE /api/sites/site/conditions/created"));
        assertEquals("http://sakai.test/fixture", page.url());
    }

    @Test
    void lessonsPersistsThePrerequisiteTreeAndRemovesItsReference() {
        openFixture("""
            <sakai-condition-picker site-id="site" tool-id="sakai.lessonbuildertool"
                item-id="target" lesson-id="1"></sakai-condition-picker>
            """);
        Locator picker = page.locator("sakai-condition-picker");
        picker.locator("select").first().selectOption("score");
        picker.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Add condition as prerequisite").setExact(true)).click();
        assertThat(picker.locator(".condition-row")).hasCount(1);
        assertTrue(writes.stream().anyMatch(write -> write.startsWith("PUT /api/sites/site/conditions/parent") && write.contains("\"id\":\"score\"")));
        picker.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Remove condition").setExact(true)).click();
        assertThat(picker.locator(".condition-row")).hasCount(0);
        assertTrue(writes.get(writes.size() - 1).contains("\"subConditions\":[]"));
    }

    @Test
    void samigoCancelsSelectionsAndRecalculatesAfterConfirmedCriterionEdits() {
        openFixture("""
            <sakai-dynamic-rubric site-id="site" entity-id="entity" grading-id="grade"
                evaluated-item-owner-id="student" previous-grade="10" origin="gradeStudentResult.faces"></sakai-dynamic-rubric>
            """);
        Locator rubric = page.locator("sakai-dynamic-rubric");
        Locator total = rubric.locator("input[name=newtotalgrade]");
        assertThat(total).hasValue("10.00");
        rubric.locator("sakai-dynamic-criterion").nth(1).getByRole(AriaRole.BUTTON).click();
        assertThat(total).hasValue("13.00");
        page.evaluate("document.querySelector('sakai-dynamic-rubric').cancel()");
        assertThat(total).hasValue("10.00");
        rubric.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Edit Criterions").setExact(true)).click();
        Locator criterion = rubric.locator("sakai-dynamic-criterion").first();
        criterion.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Edit this criterion").setExact(true)).click();
        criterion.getByRole(AriaRole.SPINBUTTON, new Locator.GetByRoleOptions().setName("Criterion points")).fill("4");
        criterion.getByRole(AriaRole.SPINBUTTON).press("Tab");
        page.onceDialog(dialog -> dialog.accept());
        rubric.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Confirm Criterion Changes").setExact(true)).click();
        page.waitForURL("**/gradeStudentResult.faces?resetCache=true&publishedId=published&itemId=item");
        assertTrue(writes.stream().anyMatch(write -> write.contains("rubrics/adhoc?pointsUpdated=true") && write.contains("\"points\":\"4.00\"")));
    }

    @Test
    void samigoTimerWarnsSavesAndEndsUsingItsExistingMessages() {
        openFixture("""
            <script>window.timerMessages = []; window.addEventListener('message', event => {
              if (event.data.id === 'timer') window.timerMessages.push(event.data.msg);
            });</script>
            <sakai-timer-bar id="timer" time-limit="6"></sakai-timer-bar>
            """);
        Locator timer = page.locator("sakai-timer-bar");
        timer.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Hide Time Remaining")).click();
        assertThat(timer.locator("#remaining")).isHidden();
        page.waitForFunction("window.timerMessages.includes('SAVE')");
        page.waitForFunction("window.timerMessages.includes('END')");
        assertEquals(List.of("SAVE", "END"), page.evaluate("window.timerMessages"));
    }
}
