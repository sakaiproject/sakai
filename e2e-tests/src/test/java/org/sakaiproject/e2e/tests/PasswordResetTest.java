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
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import java.util.Map;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class PasswordResetTest extends SakaiUiTestBase {

    @Test
    void canRequestAPasswordChangeEmail() {
        page.navigate("/sakai-reset-pass/");
        assertThat(page.locator("#sendBtn")).hasCount(1);
        assertThat(page.locator("#sendBtn")).hasText("Request Password Change");
        assertThat(page.locator("#instructionInfo")).containsText("containing a link");

        assertThat(page.locator("#email-sent-msg")).isHidden();
        assertThat(page.locator("#error-msg")).isHidden();
        assertThat(page.locator("#support-msg")).isHidden();

        String email = "reset-" + UUID.randomUUID() + "@example.test";
        page.locator("#mailInput").fill(email);
        page.locator("#sendBtn").click();

        assertThat(page.locator("#email-sent-msg")).isVisible();
        assertThat(page.locator("#email-sent-msg")).containsText("password change email");
        assertThat(page.locator("#email-sent-msg")).containsText(email);
        assertThat(page.locator("#error-msg")).isHidden();
        assertThat(page.locator("#instructionInfo")).isHidden();
        assertThat(page.locator("#instructions")).isHidden();
    }
    @Test
    void resetCompletionRejectsInvalidTokensAndSubmittedTokenState() {
        APIResponse invalidToken = page.request().post("/accountvalidator/passwordReset?tokenId=" + UUID.randomUUID(),
                RequestOptions.create().setData(Map.of("password", "a long test password", "password2", "a long test password")));
        assertEquals(200, invalidToken.status());
        assertTrue(invalidToken.text().contains("\"error\""));

        APIResponse forbiddenFields = page.request().post("/accountvalidator/passwordReset?tokenId=" + UUID.randomUUID(),
                RequestOptions.create().setData(Map.of("password", "a long test password", "password2", "a long test password", "status", 0, "accountStatus", 1)));
        assertEquals(400, forbiddenFields.status());
    }
}
