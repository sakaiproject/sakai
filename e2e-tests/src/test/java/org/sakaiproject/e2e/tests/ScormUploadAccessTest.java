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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/** Requires scorm.import.siteUrl and a direct bookmarkable upload URL in scorm.import.uploadUrl. */
@EnabledIfSystemProperty(named = "scorm.import.uploadUrl", matches = ".+")
class ScormUploadAccessTest extends SakaiUiTestBase {
    @Test
    void studentCannotOpenUploadPageDirectly() {
        openTool(System.getProperty("scorm.import.student", "student0011"));
        APIResponse response = context.request().get(System.getProperty("scorm.import.uploadUrl"));
        try {
            assertEquals(403, response.status());
        } finally {
            response.dispose();
        }
    }

    @Test
    void instructorCanOpenUploadPageDirectly() {
        openTool(System.getProperty("scorm.import.instructor", "instructor1"));
        APIResponse response = context.request().get(System.getProperty("scorm.import.uploadUrl"));
        try {
            assertEquals(200, response.status());
            assertTrue(response.text().contains("type=\"file\""));
        } finally {
            response.dispose();
        }
    }

    private void openTool(String user) {
        sakai.login(user);
        page.navigate(System.getProperty("scorm.import.siteUrl"));
        sakai.toolClick("SCORM Player");
    }
}
