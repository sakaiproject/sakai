/**
 * Copyright (c) 2026 The Apereo Foundation
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

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.RequestOptions;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class EmailTemplateAdministrationTest extends SakaiUiTestBase {

    private static final String TOOL = "/emailtemplateservice-tool";
    // A dedicated template that is never used by account or password-reset emails.
    private static final String TEST_KEY = "e2e.email-template-security";

    @Test
    void anonymousVisitorsCannotReadOrSubmitTemplates() {
        assertTrue(context.cookies().isEmpty());
        assertDeniedRoutes();
    }

    @Test
    void ordinaryUsersCannotReadOrSubmitTemplates() {
        sakai.login("instructor1");
        assertDeniedRoutes();
    }

    private void assertDeniedRoutes() {
        for (String path : List.of("/", "/index", "/index/true/validate.newUser/default", "/new", "/edit/1", "/edit/999999")) {
            APIResponse response = page.request().get(TOOL + path);
            assertEquals(403, response.status(), path);
            response.dispose();
        }
        for (String path : List.of("/new/formsubmit", "/edit/1/formsubmit", "/edit/999999/formsubmit")) {
            APIResponse response = page.request().post(TOOL + path, json(Map.of("subject", "E2E denied marker")));
            assertEquals(403, response.status(), path);
            assertTrue(response.text().contains("\"status\":\"ERROR\""));
            response.dispose();
        }
    }

    @Test
    void administratorsCanEditWhileCrossSiteAndMetadataWritesAreBlocked() {
        sakai.login("admin");
        page.navigate(TOOL + "/index");
        Locator templateRow = page.locator("tr").filter(new Locator.FilterOptions().setHasText(TEST_KEY));
        String editUrl = null;
        Map<String, Object> originalValues = null;
        boolean creationAttempted = false;
        boolean createdTemplate = false;
        Throwable testFailure = null;

        try {
            if (templateRow.count() == 0) {
                page.navigate(TOOL + "/new");
                page.locator("#emailSubject").fill("Email template security test");
                page.locator("#emailKey").fill(TEST_KEY);
                page.locator("#emailMessage").fill("This template is used only by automated tests.");
                page.locator("[id='email:Locale']").fill("default");
                originalValues = templateValues();
                creationAttempted = true;
                saveForm();
                createdTemplate = true;
                templateRow = page.locator("tr").filter(new Locator.FilterOptions().setHasText(TEST_KEY));
            }

            templateRow.locator("a").click();
            assertThat(page.locator("#emailTemplateForm")).isVisible();
            editUrl = page.url();
            if (originalValues == null) {
                originalValues = templateValues();
            }
            String messageBefore = (String) originalValues.get("message");
            String subjectAfter = "Email template security test " + Instant.now().toEpochMilli();

            page.locator("#emailSubject").fill(subjectAfter);
            saveForm();
            page.navigate(editUrl);
            assertThat(page.locator("#emailSubject")).hasValue(subjectAfter);
            assertThat(page.locator("#emailMessage")).hasValue(messageBefore);

            for (String fetchSite : List.of("cross-site", "same-site", "none")) {
                APIResponse response = page.request().post(editUrl + "/formsubmit",
                    RequestOptions.create().setHeader("Content-Type", "application/json")
                        .setHeader("Sec-Fetch-Site", fetchSite)
                        .setData(Map.of("subject", "E2E blocked marker")));
                assertEquals(403, response.status());
                response.dispose();
            }
            APIResponse missingOrigin = page.request().post(editUrl + "/formsubmit",
                RequestOptions.create().setHeader("Content-Type", "application/json")
                    .setData(Map.of("subject", "E2E blocked marker")));
            assertEquals(403, missingOrigin.status());
            missingOrigin.dispose();

            for (String field : List.of("owner", "version", "id", "defaultType", "lastModified")) {
                APIResponse response = page.request().post(editUrl + "/formsubmit",
                    json(Map.of("subject", "E2E blocked marker", field, "changed")));
                assertEquals(400, response.status(), field);
                response.dispose();
            }
            page.reload();
            assertThat(page.locator("#emailSubject")).hasValue(subjectAfter);
            assertThat(page.locator("#emailMessage")).hasValue(messageBefore);
        } catch (RuntimeException | Error failure) {
            testFailure = failure;
            throw failure;
        } finally {
            try {
                // A save can persist before saveForm() fails while checking the redirect or banner.
                if (creationAttempted && editUrl == null) {
                    page.navigate(TOOL + "/index");
                    templateRow = page.locator("tr").filter(new Locator.FilterOptions().setHasText(TEST_KEY));
                    createdTemplate = templateRow.count() == 1;
                    if (createdTemplate) {
                        templateRow.locator("a").click();
                        editUrl = page.url();
                    }
                }
                if (originalValues != null && editUrl != null && (!creationAttempted || createdTemplate)) {
                    // There is no delete endpoint; restore newly created fixtures for reuse too.
                    APIResponse restored = page.request().post(editUrl + "/formsubmit", json(originalValues));
                    try {
                        assertEquals(200, restored.status(), "Restore the dedicated test template");
                        assertTrue(restored.text().contains("\"status\":\"SUCCESS\""));
                    } finally {
                        restored.dispose();
                    }
                    page.navigate(editUrl);
                    assertThat(page.locator("#emailSubject")).hasValue((String) originalValues.get("subject"));
                    assertThat(page.locator("#emailMessage")).hasValue((String) originalValues.get("message"));
                    assertThat(page.locator("[id='email:Locale']")).hasValue((String) originalValues.get("locale"));
                }
            } catch (RuntimeException | Error cleanupFailure) {
                if (testFailure != null) {
                    testFailure.addSuppressed(cleanupFailure);
                } else {
                    throw cleanupFailure;
                }
            }
        }
    }

    private Map<String, Object> templateValues() {
        return Map.of("subject", page.locator("#emailSubject").inputValue(),
            "message", page.locator("#emailMessage").inputValue(),
            "locale", page.locator("[id='email:Locale']").inputValue());
    }

    private void saveForm() {
        page.locator("#saveButton").click();
        page.waitForURL(Pattern.compile(".*/emailtemplateservice-tool/index/true/.*"));
        assertThat(page.locator("#banner")).isVisible();
    }

    private RequestOptions json(Map<String, Object> body) {
        return RequestOptions.create().setHeader("Content-Type", "application/json")
            .setHeader("Sec-Fetch-Site", "same-origin").setData(body);
    }
}
