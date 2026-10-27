/*
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.RequestOptions;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/** Exercises deployed assessment exports with prepared fixtures. */
@EnabledIfSystemProperty(named = "samigo.export.siteUrl", matches = ".+")
class SamigoExportTest extends SakaiUiTestBase {

    @ParameterizedTest
    @CsvSource({
        "DownloadCP, student0011", "ExportMarkupText, student0011",
        "DownloadCP, anonymous", "ExportMarkupText, anonymous"
    })
    void deniedExportsLeaveDraftUnchanged(String servlet, String requester) {
        openDraft();
        String assessmentId = page.locator("[id='assessmentForm:assessmentId']").inputValue();
        // Establish the owner as the last modifier through the normal export path.
        APIResponse ownerExport = context.request().get("/samigo-app/servlet/ExportMarkupText",
            RequestOptions.create().setQueryParam("assessmentId", assessmentId));
        assertEquals(200, ownerExport.status());
        String before = openDraft();
        assertFalse(before.isBlank(), "The fixture must have a named last modifier");

        if ("anonymous".equals(requester)) {
            context.clearCookies();
        } else {
            sakai.login(requester);
        }
        APIResponse response = context.request().get("/samigo-app/servlet/" + servlet,
            RequestOptions.create().setQueryParam("assessmentId", assessmentId));
        assertEquals(200, response.status());
        assertTrue(response.text().contains("Export Not Allowed"));

        assertEquals(before, openDraft(), "A denied export must not modify the draft");
    }

    @ParameterizedTest
    @ValueSource(strings = {"DownloadCP", "ExportMarkupText"})
    void ownerCanStillExportDraft(String servlet) {
        openDraft();
        String assessmentId = page.locator("[id='assessmentForm:assessmentId']").inputValue();
        APIResponse response = context.request().get("/samigo-app/servlet/" + servlet,
            RequestOptions.create().setQueryParam("assessmentId", assessmentId));
        assertEquals(200, response.status());
        assertTrue(response.headers().get("content-disposition").contains("attachment"));
        assertTrue(response.body().length > 0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"xlsx", "pdf"})
    void reportRequiresMatchingSiteAndAssessment(String format) {
        sakai.login(System.getProperty("samigo.export.otherInstructor", "instructor2"));
        page.navigate(System.getProperty("samigo.export.otherSiteUrl"));
        sakai.toolClick("Tests");
        APIResponse response = context.request().get("/samigo-app/servlet/ExportReport",
            reportOptions(format).setQueryParam("isFromStudent", "true").setQueryParam("siteId",
                sakai.siteIdFromUrl(System.getProperty("samigo.export.otherSiteUrl"))));
        assertEquals(403, response.status());
    }

    @ParameterizedTest
    @ValueSource(strings = {"xlsx", "pdf"})
    void ownerCanStillExportReport(String format) {
        sakai.login(System.getProperty("samigo.export.owner", "instructor1"));
        page.navigate(System.getProperty("samigo.export.siteUrl"));
        sakai.toolClick("Tests");
        APIResponse response = context.request().get("/samigo-app/servlet/ExportReport",
            reportOptions(format).setQueryParam("siteId",
                sakai.siteIdFromUrl(System.getProperty("samigo.export.siteUrl"))));
        assertEquals(200, response.status());
        assertTrue(response.headers().get("content-disposition").contains("attachment"));
        assertTrue(response.body().length > 0);
    }

    private RequestOptions reportOptions(String format) {
        return RequestOptions.create().setQueryParam("assessmentId", System.getProperty("samigo.export.publishedId"))
            .setQueryParam("type", "statistics").setQueryParam("format", format);
    }

    private String openDraft() {
        sakai.login(System.getProperty("samigo.export.owner", "instructor1"));
        page.navigate(System.getProperty("samigo.export.siteUrl"));
        sakai.toolClick("Tests");
        Locator row = page.locator("#authorIndexForm\\:coreAssessments tbody tr")
            .filter(new Locator.FilterOptions().setHasText("Draft - " + System.getProperty("samigo.export.draftTitle")));
        String lastModifier = row.locator("td").nth(8).innerText();
        row.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Actions")).click();
        row.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Edit").setExact(true)).click();
        assertThat(page.locator("#assessmentForm")).isVisible();
        assertThat(page.locator("#assessmentForm")).containsText("Random draw from");
        return lastModifier;
    }
}
