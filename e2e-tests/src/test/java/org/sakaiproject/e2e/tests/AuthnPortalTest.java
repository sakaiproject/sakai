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

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.sakaiproject.e2e.support.SakaiEnvironment;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class AuthnPortalTest extends SakaiUiTestBase {

    private static final String USERNAME = "instructor1";
    private static final String PARAMETER_NAME = "\"><script>document.body.dataset.authnXss='executed'</script>";

    @ParameterizedTest
    @CsvSource({"GET,false", "POST,false", "GET,true", "POST,true"})
    void rejectsUnknownRequestsWithoutExposingRequestDetails(String method, boolean authenticated) {
        if (authenticated) {
            sakai.login(sakai.resolveUsername(USERNAME));
            assertThat(page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName("Home").setExact(true)).first()).isVisible();
            assertTrue(context.cookies().stream().anyMatch(cookie -> cookie.httpOnly));
        }

        context.addCookies(List.of(new Cookie("authn-regression", "authn-cookie-marker")
            .setUrl(SakaiEnvironment.baseUrl())));
        List<Cookie> cookies = context.cookies();
        String path = "/authn/doesnotexist?" + URLEncoder.encode(PARAMETER_NAME, StandardCharsets.UTF_8)
            + "=authn-parameter-marker";
        RequestOptions options = RequestOptions.create().setHeader("X-Authn-Regression", "authn-header-marker");
        APIResponse response = "GET".equals(method)
            ? page.request().get(path, options)
            : page.request().post(path, options);
        try {
            assertEquals(404, response.status());
            String body = response.text();
            assertFalse(body.contains(PARAMETER_NAME));
            assertFalse(body.contains("authn-parameter-marker"));
            assertFalse(body.contains("authn-header-marker"));
            assertFalse(body.contains("Snoop for request"));
            assertFalse(body.contains("sakai.session"));
            for (Cookie cookie : cookies) {
                if (cookie.httpOnly || "authn-regression".equals(cookie.name)) {
                    assertFalse(body.contains(cookie.value), "Response must not disclose request cookies");
                }
            }
        } finally {
            response.dispose();
        }
    }

    @ParameterizedTest
    @CsvSource({"/authn/,false", "/authn/login,false", "/authn/login,true"})
    void loginPreservesReturnDestination(String path, boolean relative) {
        String username = sakai.resolveUsername(USERNAME);
        String returnUrl = URI.create(SakaiEnvironment.baseUrl()).resolve("/portal/site/~" + username).toString();
        String destination = relative ? URI.create(returnUrl).getRawPath() : returnUrl;
        Response response = page.navigate(path + "?url=" + URLEncoder.encode(destination, StandardCharsets.UTF_8));
        assertEquals(200, response.status());

        page.locator("input[name=\"eid\"]").fill(username);
        page.locator("input[name=\"pw\"]").fill(sakai.passwordFor(username));
        page.locator("input[name=\"pw\"]").press("Enter");

        assertThat(page).hasURL(returnUrl);
        assertThat(page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName("Home").setExact(true)).first()).isVisible();
    }

    static Stream<Arguments> untrustedReturnDestinations() {
        List<String> destinations = List.of(
            "https://redirect-probe.invalid/", "HtTPs://redirect-probe.invalid/",
            "//redirect-probe.invalid/", "////redirect-probe.invalid/", "/\\redirect-probe.invalid/",
            SakaiEnvironment.baseUrl() + ".redirect-probe.invalid/",
            "https://sakai.example@redirect-probe.invalid/", "javascript:alert(1)",
            "aHR0cDovL3d3dy5obS5jb20", "");
        return Stream.of("GET-login", "POST-login", "GET-logout").flatMap(action ->
            destinations.stream().map(destination -> Arguments.of(action, destination)));
    }

    @ParameterizedTest
    @MethodSource("untrustedReturnDestinations")
    void rejectsUntrustedReturnDestinations(String action, String destination) {
        String path = "/authn/" + (action.endsWith("logout") ? "logout" : "login");
        RequestOptions options = RequestOptions.create().setMaxRedirects(0);
        APIResponse response = action.startsWith("POST")
            ? page.request().post(path, options.setForm(FormData.create().set("url", destination)))
            : page.request().get(path + "?url=" + URLEncoder.encode(destination, StandardCharsets.UTF_8), options);
        try {
            assertEquals(400, response.status());
            assertFalse(response.headers().containsKey("location"));
        } finally {
            response.dispose();
        }
    }

    @ParameterizedTest
    @CsvSource({"/authn/login,GET", "/authn/login,POST", "/authn/logout,GET"})
    void rejectsRepeatedReturnDestinations(String path, String method) {
        String local = URLEncoder.encode(SakaiEnvironment.baseUrl() + "/portal/", StandardCharsets.UTF_8);
        String external = URLEncoder.encode("https://redirect-probe.invalid/", StandardCharsets.UTF_8);
        RequestOptions options = RequestOptions.create().setMaxRedirects(0);
        APIResponse response = "POST".equals(method)
            ? page.request().post(path + "?url=" + local,
                options.setForm(FormData.create().set("url", "https://redirect-probe.invalid/")))
            : page.request().get(path + "?url=" + local + "&%75rl=" + external, options);
        try {
            assertEquals(400, response.status());
            assertFalse(response.headers().containsKey("location"));
        } finally {
            response.dispose();
        }
    }

    @Test
    void logoutPreservesReturnDestination() {
        sakai.login(sakai.resolveUsername(USERNAME));
        String returnUrl = URI.create(SakaiEnvironment.baseUrl()).resolve("/portal/xlogin").toString();

        page.navigate("/authn/logout?url=" + URLEncoder.encode(returnUrl, StandardCharsets.UTF_8));

        assertThat(page).hasURL(returnUrl);
        assertThat(page.locator("input[name=\"eid\"]")).isVisible();
    }
}
