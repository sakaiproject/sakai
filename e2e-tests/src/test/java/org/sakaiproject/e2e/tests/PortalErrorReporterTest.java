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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.Cookie;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiEnvironment;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class PortalErrorReporterTest extends SakaiUiTestBase {

    @Test
    void errorReportOmitsCredentialsFromVisibleAndHiddenContent() {
        sakai.login("admin");
        assertThat(page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName("Admin Home").setExact(true)).first()).isVisible();
        context.addCookies(List.of(new Cookie("portal-regression", "portal-report-cookie-marker")
            .setUrl(SakaiEnvironment.baseUrl())));
        List<Cookie> cookies = context.cookies();
        assertTrue(cookies.stream().anyMatch(cookie -> cookie.httpOnly));
        page.setExtraHTTPHeaders(Map.of(
            "Authorization", "Bearer portal-report-authorization-marker",
            "X-Portal-Regression", "portal-report-header-marker"));

        Response response = page.navigate("/portal/generatebugreport"
            + "?portal-regression=portal-report-query-marker&csrf-token=portal-report-csrf-marker");

        assertEquals(500, response.status());
        assertTrue(response.headers().get("cache-control").contains("no-store"));
        assertThat(page.locator("form[action$='/error-report']")).isVisible();
        assertThat(page.locator("textarea[name='comment']")).isVisible();
        String responseBody = response.text();
        assertTrue(responseBody.contains("name=\"problemRequest\" value=\""));
        assertTrue(responseBody.contains("generatebugreport"));

        String html = responseBody + page.content();
        assertFalse(html.contains("portal-report-cookie-marker"));
        assertFalse(html.contains("portal-report-authorization-marker"));
        assertFalse(html.contains("portal-report-header-marker"));
        assertFalse(html.contains("portal-report-query-marker"));
        assertFalse(html.contains("portal-report-csrf-marker"));
        assertFalse(html.contains("sakai.session"));
        for (Cookie cookie : cookies) {
            if (cookie.httpOnly) {
                String sessionId = cookie.value.split("\\.", 2)[0];
                assertFalse(html.contains(sessionId), "Error report must not disclose authentication cookies");
            }
        }
    }
}
