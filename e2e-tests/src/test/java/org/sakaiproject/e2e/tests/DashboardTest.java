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

import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DashboardTest extends SakaiUiTestBase {

    private static String sakaiUrl;

    @Test
    @Order(1)
    void createsSiteWithDashboard() {
        sakai.login("instructor1");
        sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.dashboard", "sakai\\.announcements", "sakai\\.schedule"));
    }

    @Test
    @Order(2)
    void canEditAndSaveDashboard() {
        sakai.login("instructor1");

        sakai.gotoPath(sakaiUrl);

        sakai.toolClick("Dashboard");

        com.microsoft.playwright.Locator editButton = page.locator("#course-dashboard-edit");
        assertThat(editButton).isVisible();
        editButton.click();

        com.microsoft.playwright.Locator saveButton = page.locator("#course-dashboard-save");
        if (saveButton.count() > 0) {
            saveButton.click();
        }

        assertThat(page.locator("main#content")).isVisible();
    }

    @Test
    @Order(3)
    void savesAndReloadsWidgetLayoutViaApi() {
        sakai.login("instructor1");

        String siteId = sakaiUrl.contains("/site/")
            ? sakaiUrl.replaceAll(".*/site/([^/]+).*", "$1")
            : null;
        org.junit.jupiter.api.Assertions.assertNotNull(siteId);

        com.microsoft.playwright.APIResponse getBefore = page.request().get("/api/sites/" + siteId + "/dashboard");
        org.junit.jupiter.api.Assertions.assertTrue(getBefore.ok());
        String body = getBefore.text();
        org.junit.jupiter.api.Assertions.assertTrue(body.contains("\"widgetLayout\""));

        String putBody = "{\"widgetLayout\":[{\"id\":\"announcements\",\"w\":2,\"h\":1},{\"id\":\"calendar\",\"w\":1,\"h\":1}],"
            + "\"overview\":\"\",\"programme\":\"\",\"template\":2}";
        com.microsoft.playwright.APIResponse put = page.request().put("/api/sites/" + siteId + "/dashboard",
            com.microsoft.playwright.options.RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(putBody));
        org.junit.jupiter.api.Assertions.assertTrue(put.ok());

        com.microsoft.playwright.APIResponse getAfter = page.request().get("/api/sites/" + siteId + "/dashboard");
        org.junit.jupiter.api.Assertions.assertTrue(getAfter.ok());
        String after = getAfter.text();
        org.junit.jupiter.api.Assertions.assertTrue(after.contains("\"w\":2") || after.contains("\"w\": 2"));
        org.junit.jupiter.api.Assertions.assertTrue(after.contains("announcements"));
    }

    @Test
    @Order(4)
    void filterPrefsRequireExplicitSave() {
        sakai.login("instructor1");
        page.navigate("/portal/");
        String userId = (String) page.evaluate("portal.user.id");
        org.junit.jupiter.api.Assertions.assertNotNull(userId);

        com.microsoft.playwright.APIResponse clear = page.request().put(
            "/api/users/" + userId + "/dashboard/filters/courses",
            com.microsoft.playwright.options.RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData("{}"));
        org.junit.jupiter.api.Assertions.assertTrue(clear.ok());

        com.microsoft.playwright.APIResponse save = page.request().put(
            "/api/users/" + userId + "/dashboard/filters/courses",
            com.microsoft.playwright.options.RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData("{\"filter\":\"courses\",\"term\":\"none\",\"sort\":\"alerts_first\",\"density\":\"compact\",\"alertsFirst\":true}"));
        org.junit.jupiter.api.Assertions.assertTrue(save.ok());

        com.microsoft.playwright.APIResponse dashboard = page.request().get("/api/users/" + userId + "/dashboard");
        org.junit.jupiter.api.Assertions.assertTrue(dashboard.ok());
        String text = dashboard.text();
        org.junit.jupiter.api.Assertions.assertTrue(text.contains("alerts_first"), text);
        org.junit.jupiter.api.Assertions.assertTrue(text.contains("compact"), text);
    }

    @Test
    @Order(5)
    void missingToolsAreAbsentFromDashboardAndPicker() {
        sakai.login("instructor1");
        String siteUrl = sakai.createFreshCourse("instructor1", List.of("sakai\\.dashboard"));
        sakai.gotoPath(siteUrl);
        sakai.toolClick("Dashboard");
        assertThat(page.locator("#course-dashboard-edit")).isVisible();
        assertThat(page.locator("sakai-grades-widget, sakai-forums-widget, sakai-announcements-widget, sakai-calendar-widget"))
            .hasCount(0);
        assertThat(page.locator("sakai-sitestats-widget#sitestats-grades, sakai-sitestats-widget#sitestats-submissions, "
            + "sakai-sitestats-widget#sitestats-communication, sakai-sitestats-widget#sitestats-lessons")).hasCount(0);

        page.locator("#course-dashboard-edit").click();
        assertThat(page.locator("#course-dashboard-save")).isVisible();
        assertThat(page.locator("#available-widgets .available-widget").filter(
            new com.microsoft.playwright.Locator.FilterOptions().setHasText(
                Pattern.compile("Grades|Forums|Announcements|Calendar|Submissions|Communication|Lessons"))))
            .hasCount(0);
    }

    @Test
    @Order(6)
    void freshGradebookDashboardLoadsWithoutGradingErrors() {
        sakai.login("instructor1");
        String siteUrl = sakai.createFreshCourse("instructor1", List.of("sakai\\.dashboard", "sakai\\.gradebookng"));
        String siteId = siteUrl.replaceAll(".*/site/([^/]+).*", "$1");
        com.microsoft.playwright.Response metrics = page.waitForResponse(
            response -> response.url().endsWith("/sitestats/widgets/grades/metrics"), () -> {
                com.microsoft.playwright.Response highlights = page.waitForResponse(
                    response -> response.url().endsWith("/sitestats/widgets/grades/highlights"), () -> {
                        com.microsoft.playwright.Response grades = page.waitForResponse(
                            response -> response.url().endsWith("/sites/" + siteId + "/grades"),
                            () -> sakai.gotoPath(siteUrl));
                        org.junit.jupiter.api.Assertions.assertTrue(grades.ok(), grades.text());
                        org.junit.jupiter.api.Assertions.assertTrue(grades.text().contains("\"grades\":[]"), grades.text());
                    });
                org.junit.jupiter.api.Assertions.assertTrue(highlights.ok(), highlights.text());
            });
        org.junit.jupiter.api.Assertions.assertTrue(metrics.ok(), metrics.text());
        assertThat(page.locator("sakai-grades-widget")).isVisible();
        assertThat(page.locator("sakai-sitestats-widget#sitestats-grades")).isVisible();
    }

}
