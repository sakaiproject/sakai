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

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;
import org.sakaiproject.e2e.support.SakaiEnvironment;
import org.sakaiproject.e2e.support.SakaiHelper;

/** HTTP requests to /access made by another user, or anonymously, without following redirects. */
final class ResourcesRequests implements AutoCloseable {

    static final int OK = 200;
    static final int REDIRECT = 302;
    static final int FORBIDDEN = 403;

    /** Status and Location header of a response, and whether its body is the Sakai login form. */
    record Result(int status, String location, boolean loginForm) {
        /** Container login redirects to the login URL; otherwise Sakai serves its login form directly. */
        boolean isLoginPrompt() {
            return loginForm || status == REDIRECT && location.contains("login");
        }
    }

    private final BrowserContext userContext;

    /** Requests as the given user, or anonymously when the username is null. */
    ResourcesRequests(Browser browser, SakaiHelper sakai, String username) {
        userContext = browser.newContext(new Browser.NewContextOptions()
            .setBaseURL(SakaiEnvironment.baseUrl()).setIgnoreHTTPSErrors(true));
        if (username != null) {
            try {
                logIn(sakai, username);
            } catch (RuntimeException | Error e) {
                // The caller never receives this instance, so it cannot close the context.
                try {
                    userContext.close();
                } catch (RuntimeException closeFailure) {
                    // Keep the login failure as the reported cause.
                    e.addSuppressed(closeFailure);
                }
                throw e;
            }
        }
    }

    private void logIn(SakaiHelper sakai, String username) {
        APIResponse login = userContext.request().post("/portal/xlogin", RequestOptions.create()
            .setForm(FormData.create().set("eid", username).set("pw", sakai.passwordFor(username))));
        try {
            assertTrue(login.ok(), "Unable to log in as " + username + ": HTTP " + login.status());
        } finally {
            login.dispose();
        }
    }

    Result get(String path) {
        APIResponse response = userContext.request().get(path, RequestOptions.create().setMaxRedirects(0));
        try {
            return new Result(response.status(), response.headers().getOrDefault("location", ""), isLoginForm(response));
        } finally {
            response.dispose();
        }
    }

    private static boolean isLoginForm(APIResponse response) {
        if (response.status() != OK || !response.headers().getOrDefault("content-type", "").contains("text/html")) {
            return false;
        }
        String body = response.text();
        return body.contains("name=\"eid\"") && body.contains("name=\"pw\"");
    }

    /** Status of a file request, reported as {@link #REDIRECT} whenever the user is asked to log in. */
    int status(String path) {
        Result result = get(path);
        if (result.isLoginPrompt()) {
            return REDIRECT;
        }
        assertTrue(result.status() != REDIRECT, "Redirect for " + path + " is not a login prompt: " + result.location());
        return result.status();
    }

    @Override
    public void close() {
        userContext.close();
    }
}
