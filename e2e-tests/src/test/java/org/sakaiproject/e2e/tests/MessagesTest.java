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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class MessagesTest extends SakaiUiTestBase {

    @Test
    void restoresIndependentDraftRecipientSelections() {
        sakai.login("instructor1");
        String courseUrl = sakai.createCourse("instructor1", List.of("sakai\\.messages"));
        page.navigate(courseUrl);
        sakai.toolClick("Messages");
        page.locator("#messagesComposeMenuLink").click();

        Locator to = page.locator("#compose\\:list1 + sakai-user-picker");
        Locator bcc = page.locator("#compose\\:list2 + sakai-user-picker");
        Locator toSearch = to.getByRole(AriaRole.COMBOBOX);
        toSearch.click();
        Locator toOption = to.getByRole(AriaRole.OPTION).filter(new Locator.FilterOptions().setHasText("student0011"));
        assertThat(toOption).isVisible();
        toOption.click();
        page.locator("#compose\\:bcc_toggle").check();
        bcc.getByRole(AriaRole.COMBOBOX).fill("student0012");
        bcc.getByRole(AriaRole.OPTION).filter(new Locator.FilterOptions().setHasText("student0012")).click();

        String subject = "Recipient draft " + sakai.randomId();
        page.locator("#compose\\:subject").fill(subject);
        assertTrue(sakai.typeFirstCkEditorIfPresent("<p>Testing saved recipients.</p>"));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save Draft").setExact(true)).click();
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Draft").setExact(true)).click();
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(subject).setExact(true)).click();

        assertThat(to.getByRole(AriaRole.BUTTON)).hasCount(1);
        assertThat(bcc.getByRole(AriaRole.BUTTON)).hasCount(1);
        assertThat(to.getByRole(AriaRole.BUTTON)).containsText("student0011");
        assertThat(bcc.getByRole(AriaRole.BUTTON)).containsText("student0012");
    }

    @Test
    void canSaveAndRemoveDraftTags() {
        sakai.login("instructor1");
        String courseUrl = sakai.createCourse("instructor1", List.of("sakai\\.messages"));
        sakai.gotoPath(courseUrl);
        sakai.toolClick("Messages");
        // Saving a draft returns to the folder the compose form was opened from.
        Locator draftFolder = page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName("Draft").setExact(true));
        if (!draftFolder.isVisible()) {
            page.locator("#messagesMainMenuLink a").click();
        }
        draftFolder.click();
        page.locator("#messagesComposeMenuLink").click();
        String subject = "Tagged draft " + System.currentTimeMillis();
        String tag = "Draft tag " + System.currentTimeMillis();
        page.locator("#compose\\:subject").fill(subject);
        sakai.typeCkEditor("compose:pvt_message_body_inputRichText", "<p>Tagged draft.</p>");
        Locator selector = page.locator("sakai-tag-selector");
        selector.getByRole(AriaRole.COMBOBOX).fill(tag);
        selector.getByRole(AriaRole.COMBOBOX).press("Enter");
        assertThat(page.locator("#compose\\:tag_selector")).hasValue(tag);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save Draft").setExact(true)).click();
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        assertThat(page.locator("#compose\\:errorMessages:visible")).hasText(new String[0]);
        String tagRequests = "**/api/sites/*/tools/*/tags/*";
        page.route(tagRequests, route -> route.fulfill(new Route.FulfillOptions().setStatus(503)));
        page.waitForResponse(response -> response.url().contains("/tools/privatemessages/tags/")
                && response.status() == 503,
            () -> page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(subject).setExact(true)).click());
        assertThat(selector.getByRole(AriaRole.ALERT)).isVisible();
        String savedTagIds = selector.getAttribute("selected-ids");
        assertTrue(savedTagIds != null && !savedTagIds.isBlank());
        assertThat(page.locator("#compose\\:tag_selector")).hasValue(savedTagIds);
        page.unroute(tagRequests);
        selector.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Retry").setExact(true)).click();
        Locator remove = selector.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Deselect: " + tag).setExact(true));
        assertThat(remove).isVisible();
        sakai.typeCkEditor("compose:pvt_message_body_inputRichText", "<p>Updated draft.</p>");
        remove.click();
        assertThat(page.locator("#compose\\:tag_selector")).hasValue("");
        // With no recipients, preview redisplays the form with a validation error.
        page.locator("input[type=submit][value=Preview]").click();
        assertThat(selector.getByRole(AriaRole.COMBOBOX)).isEnabled();
        assertThat(remove).hasCount(0);
        assertThat(page.locator("#compose\\:tag_selector")).hasValue("");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save Draft").setExact(true)).click();
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(subject).setExact(true)).click();
        assertThat(selector.getByRole(AriaRole.COMBOBOX)).isEnabled();
        assertThat(remove).hasCount(0);
        assertThat(page.locator("#compose\\:tag_selector")).hasValue("");
    }
}
