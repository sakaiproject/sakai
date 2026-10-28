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

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.AriaRole;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class MessagesSearchTest extends SakaiUiTestBase {

    @Test
    void oneSearchBoxFiltersWhileTypingSearchesOnEnterAndClearsWhenEmptied() {
        sakai.login("instructor1");
        String courseUrl = sakai.createCourse("instructor1", List.of("sakai\\.messages"));
        String id = sakai.randomId();
        String exam = "Exam " + id;
        String lab = "Lab " + id;
        String bodyWord = "room" + id;
        saveDraft(courseUrl, exam, "<p>Held in " + bodyWord + "</p>");
        saveDraft(courseUrl, lab, "<p>Bring goggles</p>");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Draft").setExact(true)).click();

        Locator search = page.locator("[id='prefs_pvt_form:search_text']");
        Locator ours = page.locator("[id='prefs_pvt_form:pvtmsgs'] > tbody > tr")
            .filter(new Locator.FilterOptions().setHasText(id));
        assertThat(search).hasAttribute("type", "search");
        // the list has no search box of its own any more
        assertThat(page.locator(".dt-search")).hasCount(0);
        assertThat(ours).hasCount(2);

        // typing filters the listed messages
        search.pressSequentially("Lab " + id);
        assertThat(ours).hasCount(1);
        assertThat(ours).containsText(lab);

        // Enter searches every message in the folder, the body included
        submits(() -> {
            search.fill(bodyWord);
            search.press("Enter");
        });
        assertThat(ours).hasCount(1);
        assertThat(ours).containsText(exam);
        assertThat(search).hasValue(bodyWord);

        // the options for that search sit behind the button in the box
        Locator options = page.locator("#search-options-toggle");
        options.click();
        assertThat(options).hasAttribute("aria-expanded", "true");
        assertThat(page.locator("[id='prefs_pvt_form:body']")).isChecked();

        // deleting the text, as the native clear button does, goes back to the whole folder
        submits(() -> search.fill(""));
        assertThat(ours).hasCount(2);
        assertThat(search).hasValue("");

        // with the options open, Enter searches by an option alone; both drafts have the normal label
        options.click();
        page.locator("[id='prefs_pvt_form:label']").check();
        submits(() -> search.press("Enter"));
        assertThat(page).hasURL(Pattern.compile("/privateMsg/pvtMsgEx"));
        assertThat(ours).hasCount(2);

        // opening a folder leaves no option behind to narrow a later search unseen
        page.locator("a, input, button").filter(new Locator.FilterOptions()
            .setHasText(Pattern.compile("Next Folder"))).first().click();
        assertThat(page).hasURL(Pattern.compile("/privateMsg/pvtMsg$"));
        options.click();
        assertThat(page.locator("[id='prefs_pvt_form:label']")).not().isChecked();
    }

    private void saveDraft(String courseUrl, String subject, String body) {
        sakai.gotoPath(courseUrl);
        sakai.toolClick("Messages");
        page.locator("#messagesComposeMenuLink").click();
        page.locator("#compose\\:subject").fill(subject);
        sakai.typeCkEditor("compose:pvt_message_body_inputRichText", body);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save Draft").setExact(true)).click();
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        assertThat(page.locator("#compose\\:errorMessages:visible")).hasText(new String[0]);
        assertThat(page.locator("#compose\\:subject")).hasCount(0);
    }

    /** Runs an action that posts the messages form and waits for the page it answers with. */
    private void submits(Runnable action) {
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        Response response = page.waitForResponse(r -> "POST".equals(r.request().method()) && r.url().contains("/privateMsg/"), action);
        response.finished();
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }
}
