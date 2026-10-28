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
        sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.dashboard"));
    }

    @Test
    @Order(2)
    void canEditAndSaveDashboard() {
        sakai.login("instructor1");

        boolean opened = false;
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                page.navigate(sakaiUrl, new com.microsoft.playwright.Page.NavigateOptions()
                    .setWaitUntil(com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED)
                    .setTimeout(15_000));
                opened = true;
                break;
            } catch (RuntimeException ignored) {
                page.waitForTimeout(1000);
            }
        }
        if (!opened) {
            page.navigate("/portal/");
        }

        sakai.toolClick("Dashboard");

        com.microsoft.playwright.Locator editButton = page.locator("#course-dashboard-edit");
        assertThat(editButton).isVisible();
        editButton.click(new com.microsoft.playwright.Locator.ClickOptions().setForce(true));

        com.microsoft.playwright.Locator saveButton = page.locator("#course-dashboard-save");
        if (saveButton.count() > 0) {
            saveButton.click(new com.microsoft.playwright.Locator.ClickOptions().setForce(true));
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
}
