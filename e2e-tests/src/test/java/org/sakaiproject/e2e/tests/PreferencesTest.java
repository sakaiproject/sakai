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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.RequestOptions;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class PreferencesTest extends SakaiUiTestBase {

    @Test
    void userCanSaveATimeZoneAndReadItInANewSession() {
        String username = "timezone-" + UUID.randomUUID();
        sakai.login("admin");
        APIResponse created = page.request().post("/direct/user/new", RequestOptions.create().setForm(FormData.create()
            .set("eid", username).set("firstName", "Time").set("lastName", "Zone")
            .set("email", username + "@example.com").set("type", "registered")
            .set("password", sakai.passwordFor(username))));
        try {
            assertTrue(created.ok(), "Unable to create timezone-test account: HTTP " + created.status());
        } finally {
            created.dispose();
        }
        try {
            openUserTimeZone(username);
            Locator timeZones = page.locator("[id='timezone_form'] select");
            String selected = "Europe/Paris";
            timeZones.selectOption(selected);
            page.locator("[id='timezone_form:submit']").click();
            assertThat(page.getByText("Your preferences have been updated successfully.",
                    new Page.GetByTextOptions().setExact(true))).isVisible();
            openUserTimeZone(username);
            assertThat(page.locator("[id='timezone_form'] select")).hasValue(selected);
        } finally {
            // The server's default timezone may have no corresponding dropdown option.
            // Delete this isolated account instead of trying to restore an empty selection.
            sakai.login("admin");
            APIResponse removed = page.request().delete("/direct/user/" + username);
            try {
                assertTrue(removed.ok(), "Unable to remove timezone-test account: HTTP " + removed.status());
            } finally {
                removed.dispose();
            }
        }
    }

    private void openUserTimeZone(String username) {
        sakai.login(username);
        sakai.gotoPath("/portal/site/~" + username);
        sakai.toolClick("Preferences");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Time Zone").setExact(true)).click();
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        assertThat(page.locator("[id='timezone_form']")).isVisible();
    }

    @Test
    void administratorCanReviewAndResubmitAConflictingEdit() {
        sakai.login("instructor1");
        String siteUrl = sakai.createProject("instructor1", List.of("sakai.admin.prefs"));
        sakai.login("admin");
        page.navigate(siteUrl);
        sakai.toolClick("Administrator Preferences");
        String toolUrl = page.url();
        String editedName = "e2e-edited-" + UUID.randomUUID();
        String concurrentName = "e2e-concurrent-" + UUID.randomUUID();
        try {
            setPreference(editedName, "original");
            openAdminPreferences();
            valueInput(editedName).fill("pending");

            // This public action commits a separate edit after the form's document was read.
            setPreference(concurrentName, "concurrent");
            save();
            assertThat(page.locator("[id='prefs_form']")).isVisible();
            assertThat(page.getByText("Preferences changed in another session. Review your changes and save again.",
                    new Page.GetByTextOptions().setExact(true))).isVisible();
            assertThat(valueInput(editedName)).hasValue("pending");

            save();
            assertThat(page.locator("[id='options_form']")).isVisible();
            openAdminPreferences();
            assertThat(valueInput(editedName)).hasValue("pending");
            assertThat(valueInput(concurrentName)).hasValue("concurrent");
        } finally {
            page.navigate(toolUrl);
            if (page.locator("[id='prefs_form']").isVisible()) {
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Cancel").setExact(true)).click();
            }
            openAdminPreferences();
            for (String name : List.of(editedName, concurrentName)) {
                Locator input = valueInput(name);
                if (input.count() > 0) {
                    input.fill("");
                }
            }
            save();
        }
    }

    private void openAdminPreferences() {
        page.locator("[id='options_form'] input[type='text']").fill("admin");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Edit").setExact(true)).click();
        assertThat(page.locator("[id='prefs_form']")).isVisible();
    }

    private Locator valueInput(String propertyName) {
        return page.locator("[id='prefs_form'] tr").filter(new Locator.FilterOptions()
                .setHas(page.locator("input[value='" + propertyName + "']"))).locator("input[type='text']").nth(2);
    }

    private void save() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save").setExact(true)).click();
    }

    private void setPreference(String name, String value) {
        APIResponse response = page.request().post("/direct/userPrefs/admin/saveDivState",
                RequestOptions.create().setQueryParam("key", name).setQueryParam("state", value));
        try {
            assertTrue(response.ok(), "Could not save the concurrent preference: HTTP " + response.status());
        } finally {
            response.dispose();
        }
    }
}
