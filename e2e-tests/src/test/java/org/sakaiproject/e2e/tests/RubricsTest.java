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
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.Cookie;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RubricsTest extends SakaiUiTestBase {

    private static String sakaiUrl;
    private static final String RUBRIC_TITLE = "Playwright Rubric " + System.currentTimeMillis();

    @Test
    @Order(1)
    void canCreateNewCourse() {
        sakai.login("instructor1");
        sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.rubrics"));
    }

    @Test
    @Order(2)
    void canCreateRubricAndSetTitle() {
        sakai.createRubric("instructor1", sakaiUrl);

        assertEquals("undefined", page.evaluate("typeof window.sakaiSessionId"));
        String html = page.content();
        for (Cookie cookie : context.cookies()) {
            if (cookie.httpOnly) {
                String sessionId = cookie.value.split("\\.", 2)[0];
                assertFalse(html.contains(sessionId), "Rubrics page must not disclose authentication cookies");
            }
        }

        page.locator("input[title=\"Rubric Title\"]:visible").first().fill(RUBRIC_TITLE);
        Response saveResponse = page.waitForResponse(response -> response.url().contains("/rubrics/") && response.request().method().equals("PATCH"),
            () -> page.locator("button:visible").filter(new Locator.FilterOptions().setHasText(Pattern.compile("Save", Pattern.CASE_INSENSITIVE))).first().click(new Locator.ClickOptions().setForce(true)));
        assertEquals(200, saveResponse.status());
        assertEquals("application/json", saveResponse.headerValue("Content-Type").split(";")[0]);

        assertThat(page.locator("body")).containsText(RUBRIC_TITLE);

        try (Page stalePage = context.newPage()) {
            stalePage.navigate(page.url());
            Locator staleRubric = stalePage.locator("sakai-rubric").filter(new Locator.FilterOptions().setHasText(RUBRIC_TITLE)).first();
            staleRubric.locator("sakai-rubric-edit button.edit-button").click();
            Locator staleModal = staleRubric.locator("sakai-rubric-edit .modal.show");
            staleModal.locator("input").fill("Unsaved concurrent title");

            Locator rubric = page.locator("sakai-rubric").filter(new Locator.FilterOptions().setHasText(RUBRIC_TITLE)).first();
            rubric.locator("sakai-rubric-edit button.edit-button").click();
            Locator modal = rubric.locator("sakai-rubric-edit .modal.show");
            modal.locator("input").fill(RUBRIC_TITLE + " updated");
            page.waitForResponse(response -> response.url().contains("/rubrics/") && response.request().method().equals("PATCH") && response.status() == 200,
                    () -> modal.locator("button.btn-primary").click());
            assertThat(rubric.locator(".rubric-name")).hasText(RUBRIC_TITLE + " updated");
            assertThat(rubric.locator(".rubric-last-modified-by")).isVisible();

            stalePage.waitForResponse(response -> response.url().contains("/rubrics/") && response.request().method().equals("PATCH") && response.status() == 409,
                    () -> staleModal.locator("button.btn-primary").click());
            assertThat(staleModal.locator("[role=alert]")).containsText("Someone else changed");
            assertThat(staleModal.locator("input")).hasValue("Unsaved concurrent title");
            page.reload();
            assertThat(page.locator("body")).containsText(RUBRIC_TITLE + " updated");
        }
    }

    @Test
    @Order(3)
    void canCreateRubricAndAddCriterion() {
        sakai.createRubric("instructor1", sakaiUrl);

        Locator cancelButton = page.locator("button:visible").filter(new Locator.FilterOptions().setHasText(Pattern.compile("Cancel", Pattern.CASE_INSENSITIVE)));
        if (cancelButton.count() > 0) {
            cancelButton.first().click(new Locator.ClickOptions().setForce(true));
        }

        page.locator(".add-criterion:visible").first().click(new Locator.ClickOptions().setForce(true));
        assertThat(page.locator("body")).containsText(Pattern.compile("Criterion", Pattern.CASE_INSENSITIVE));
    }

    @Test
    @Order(4)
    void canDeleteRubric() {
        sakai.createRubric("instructor1", sakaiUrl);

        Locator cancelButton = page.locator("button:visible").filter(new Locator.FilterOptions().setHasText(Pattern.compile("Cancel", Pattern.CASE_INSENSITIVE)));
        if (cancelButton.count() > 0) {
            cancelButton.first().click(new Locator.ClickOptions().setForce(true));
        }

        page.locator("sakai-item-delete.sakai-rubric").last().click(new Locator.ClickOptions().setForce(true));
        page.locator("button:visible").filter(new Locator.FilterOptions().setHasText(Pattern.compile("Save", Pattern.CASE_INSENSITIVE))).first().click(new Locator.ClickOptions().setForce(true));

        assertThat(page.locator("#site_rubrics rubric-item")).hasCount(0);
    }

    @Test
    @Order(5)
    void canCopyRubric() {
        sakai.createRubric("instructor1", sakaiUrl);

        Locator cancelButton = page.locator("button:visible").filter(new Locator.FilterOptions().setHasText(Pattern.compile("Cancel", Pattern.CASE_INSENSITIVE)));
        if (cancelButton.count() > 0) {
            cancelButton.first().click(new Locator.ClickOptions().setForce(true));
        }

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("Copy .*Rubric", Pattern.CASE_INSENSITIVE))).first().click(new Locator.ClickOptions().setForce(true));
        assertThat(page.locator("body")).containsText(Pattern.compile("Copy|Rubric", Pattern.CASE_INSENSITIVE));
    }

    @Test
    @Order(6)
    void canCopyCriterion() {
        sakai.createRubric("instructor1", sakaiUrl);

        Locator cancelButton = page.locator("button:visible").filter(new Locator.FilterOptions().setHasText(Pattern.compile("Cancel", Pattern.CASE_INSENSITIVE)));
        if (cancelButton.count() > 0) {
            cancelButton.first().click(new Locator.ClickOptions().setForce(true));
        }

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("Copy Criterion", Pattern.CASE_INSENSITIVE))).first().click(new Locator.ClickOptions().setForce(true));
        assertThat(page.locator("body")).containsText(Pattern.compile("Criterion", Pattern.CASE_INSENSITIVE));
    }

    @Test
    @Order(7)
    void pdfExportUsesDownloadLink() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Rubrics");

        Locator pdfExport = page.locator("sakai-rubric-pdf a").first();
        assertThat(pdfExport).isVisible();
        assertThat(pdfExport).hasAttribute("download", "");
    }
}
