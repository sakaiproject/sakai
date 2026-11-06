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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.AbstractToolSearchTest;

// Poll form steps follow PollTest
class PollsSearchTest extends AbstractToolSearchTest {

    private String optionWord;

    @Override
    protected String toolId() {
        return "sakai\\.poll";
    }

    @Override
    protected String searchToolName() {
        return "polls";
    }

    @Override
    protected String toolLabel() {
        return "Polls";
    }

    // an open site poll is visible to every member; dates and groups are covered by PollsContentProducerTest
    @Override
    protected String deniedUser() {
        return null;
    }

    @Override
    protected String createItem(String siteId, String title, String body) {
        openPolls(siteId);
        page.locator(".navIntraTool").getByRole(AriaRole.LINK,
            new Locator.GetByRoleOptions().setName("Add").setExact(true)).click();
        page.locator("form:visible input[type=\"text\"]").first().fill(title);
        page.locator("#poll-details").fill(body);
        page.locator("form:visible input[type=\"submit\"]").first().click();

        // the option form the save leads to carries the new poll's id
        optionWord = uniqueWord();
        page.locator("textarea").fill("Answer " + optionWord);
        String pollId = page.url().replaceFirst(".*[?&]pollId=([^&#]+).*", "$1");
        page.locator("button:has-text(\"Save\"), input[type=\"submit\"][value*=\"Save\"]").first().click();
        assertThat(page.locator(".sak-banner-success")).isVisible();
        return pollId;
    }

    @Override
    protected void updateItem(String siteId, String itemKey, String body) {
        openPolls(siteId);
        page.locator("a[href*='voteAdd'][href*='pollId=" + itemKey + "']").click();
        page.locator("#poll-details").fill(body);
        page.locator("form:visible input[type=\"submit\"]").first().click();
        assertThat(page.locator(".sak-banner-success")).isVisible();
    }

    @Override
    protected void deleteItem(String siteId, String itemKey) {
        openPolls(siteId);
        page.locator("input[name=\"deleteIds\"][value=\"" + itemKey + "\"]").check();
        page.locator("#delete-polls").click();
        page.locator("#confirmDelete").click();
        assertThat(page.locator(".sak-banner-success")).isVisible();
    }

    @Override
    protected void assertOpensTool() {
        assertThat(page.locator(".portletBody")).containsText(titleWord);
    }

    @Test
    @Order(5)
    void answerOptionsAreFound() {
        sakai.login(author());

        assertNotNull(awaitItem(optionWord));
    }

    private void openPolls(String siteId) {
        sakai.gotoPath("/portal/site/" + siteId);
        sakai.toolClick("Poll");
    }
}
