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

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConversationsTest extends SakaiUiTestBase {

    private static String sakaiUrl;
    private static final String TOPIC_TITLE = "Playwright Conversation Topic " + System.currentTimeMillis();
    private static final String TOPIC_BODY = "This is a Playwright-created Conversations topic.";

    @Test
    @Order(1)
    void createsSiteWithConversations() {
        sakai.login("instructor1");
        sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.conversations"));
    }

    @Test
    @Order(2)
    void createsAndPublishesNewTopic() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Conversation");

        assertThat(page.locator("#content, main, .portletBody, .Mrphs-toolTitle").first()).isVisible();

        Locator createButton = page.locator("#conv-topbar-and-content > .conv-topbar > .conv-settings-and-create > .btn-primary:visible").first();
        if (createButton.count() == 0) {
            createButton = page.locator("#conv-topbar-and-content .conv-settings-and-create button:visible, #conv-topbar-and-content .conv-settings-and-create a:visible").first();
        }
        if (createButton.count() == 0) {
            createButton = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Create new topic", Pattern.CASE_INSENSITIVE))).first();
        }

        assertThat(createButton).isVisible();
        Locator fallbackTitleInput = page.locator("form input[type=\"text\"]:visible, form input:not([type]):visible").first();
        Locator titleInput = page.getByRole(AriaRole.TEXTBOX,
            new Page.GetByRoleOptions().setName(Pattern.compile("Title", Pattern.CASE_INSENSITIVE))).first();

        boolean composerOpened = false;
        for (int attempt = 0; attempt < 3; attempt++) {
            createButton.click(new Locator.ClickOptions().setForce(true));
            if (isVisible(titleInput, 5_000)) {
                composerOpened = true;
                break;
            }
            if (isVisible(fallbackTitleInput, 1_000)) {
                titleInput = fallbackTitleInput;
                composerOpened = true;
                break;
            }
            page.waitForTimeout(750);
        }

        if (!composerOpened) {
            throw new IllegalStateException("Conversation composer did not open");
        }

        titleInput.fill(TOPIC_TITLE);

        Locator editor = page.locator("sakai-conversations iframe.cke_wysiwyg_frame:visible")
            .contentFrame().locator("body");
        editor.click();
        editor.pressSequentially(TOPIC_BODY);
        editor.press("Tab");

        page.locator("sakai-add-topic:visible input[value='Publish']").click();
        assertThat(page.locator("sakai-topic-summary").filter(new Locator.FilterOptions().setHasText(TOPIC_TITLE)).first())
            .isVisible();
    }

    @Test
    @Order(3)
    void studentViewingAndReplyingSurvivesRefresh() {
        sakai.login("student0011");
        page.navigate(sakaiUrl);
        sakai.toolClick("Conversation");

        Locator topic = page.locator("sakai-topic-summary:visible")
            .filter(new Locator.FilterOptions().setHasText(TOPIC_TITLE));
        assertThat(topic).isVisible();
        page.locator("sakai-topic-list:visible select:has(option[value='by_unviewed'])").selectOption("by_unviewed");
        page.waitForResponse(response -> response.url().endsWith("/markpostsviewed") && response.ok(), topic::click);
        assertThat(page.locator("sakai-topic:visible .topic-message")).containsText(TOPIC_BODY);
        assertThat(page.locator("sakai-topic-list:visible option[value='by_unviewed']")).hasCount(0);

        page.reload();
        assertThat(topic).isVisible();
        assertThat(page.locator("sakai-topic-list:visible option[value='by_unviewed']")).hasCount(0);
        topic.click();
        assertThat(page.locator("sakai-topic:visible .topic-message")).containsText(TOPIC_BODY);

        page.locator("sakai-topic:visible .editor-placeholder").click();
        Locator editor = page.locator("sakai-topic:visible .topic-reply-block iframe.cke_wysiwyg_frame")
            .contentFrame().locator("body");
        editor.click();
        editor.pressSequentially("My already-read reply");
        editor.press("Tab");
        page.locator("sakai-topic:visible .topic-reply-block input[value='Publish']").click();
        assertThat(page.locator("sakai-topic:visible .topic-posts-block")).containsText("My already-read reply");

        page.reload();
        assertThat(topic).isVisible();
        assertThat(page.locator("sakai-topic-list:visible option[value='by_unviewed']")).hasCount(0);
        topic.click();
        assertThat(page.locator("sakai-topic:visible .topic-posts-block")).containsText("My already-read reply");
    }

    private boolean isVisible(Locator locator, double timeoutMs) {
        try {
            locator.waitFor(new Locator.WaitForOptions().setTimeout(timeoutMs));
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
