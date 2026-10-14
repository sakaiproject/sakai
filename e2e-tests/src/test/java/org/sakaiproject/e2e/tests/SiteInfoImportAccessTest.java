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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/** Requires a site with Site Info visible to an instructor and a student without site-update permission. */
@EnabledIfSystemProperty(named = "siteinfo.security.siteUrl", matches = ".+")
class SiteInfoImportAccessTest extends SakaiUiTestBase {

    @Test
    void studentCannotPostDirectlyToArchiveUploadHandler() {
        String response = postUploadWithoutFile(System.getProperty("siteinfo.security.student", "student0011"));
        assertTrue(response.contains("You do not have permission to change the access to this site."));
    }

    @Test
    void instructorCanReachArchiveUploadValidation() {
        String response = postUploadWithoutFile(System.getProperty("siteinfo.security.instructor", "instructor1"));
        // A missing file reaches upload validation only after the permission check succeeds.
        assertTrue(response.contains("The upload size limit of"));
    }

    private String postUploadWithoutFile(String username) {
        sakai.login(username);
        page.navigate(System.getProperty("siteinfo.security.siteUrl"));
        sakai.toolClick("Site Info");
        page.waitForURL(Pattern.compile(".*/tool/[^/?]+(?:[/?].*)?"));
        String csrf = page.locator("input[name=sakai_csrf_token]").first().getAttribute("value");
        APIResponse response = context.request().post(page.url(), RequestOptions.create()
            .setHeader("Referer", page.url())
            .setMultipart(FormData.create()
                .set("sakai_action", "doUpload_Mtrl_Frm_File")
                .set("sakai_csrf_token", csrf)));
        try {
            assertEquals(200, response.status());
            return response.text();
        } finally {
            response.dispose();
        }
    }
}
