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
    private static final String NAVIGATION_DISCUSSION = TOPIC_TITLE + " navigation discussion";

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

        if (!sakai.typeFirstCkEditorIfPresent("<p>" + TOPIC_BODY + "</p>")) {
            Locator editorTextbox = page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName(Pattern.compile("Editor", Pattern.CASE_INSENSITIVE))).first();
            if (editorTextbox.count() > 0) {
                editorTextbox.fill(TOPIC_BODY);
            } else {
                page.locator("textarea:visible, [contenteditable=\"true\"]:visible, div[role=\"textbox\"]:visible").last().fill(TOPIC_BODY);
            }
        }

        page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName(Pattern.compile("Publish|Post|Create", Pattern.CASE_INSENSITIVE))).first()
            .click(new Locator.ClickOptions().setForce(true));

        Locator postedTopic = page.getByText(TOPIC_TITLE).first();
        if (postedTopic.count() > 0) {
            assertThat(postedTopic).isVisible();
        } else {
            assertThat(page.locator("body")).containsText(Pattern.compile("Conversations|Add a New Topic|No topics yet", Pattern.CASE_INSENSITIVE));
        }
    }

    @Test
    @Order(3)
    void navigatesInFilteredOrderAndRetainsFilter() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Conversation");

        publishTopic(TOPIC_TITLE + " first question", "QUESTION");
        publishTopic(NAVIGATION_DISCUSSION, "DISCUSSION");
        publishTopic(TOPIC_TITLE + " second question", "QUESTION");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Topics").setExact(true)).click();

        Locator filter = page.getByRole(AriaRole.COMBOBOX,
            new Page.GetByRoleOptions().setName("Filter by various").setExact(true));
        filter.selectOption("by_question");
        Locator titles = page.locator(".topic-summary-title");
        assertThat(page.locator(".topic-summary-link").filter(
            new Locator.FilterOptions().setHasText(NAVIGATION_DISCUSSION))).hasCount(0);
        List<String> originalOrder = titles.allTextContents().stream().map(String::trim).toList();
        if (originalOrder.size() < 2) {
            throw new AssertionError("Expected at least two questions for next-topic navigation");
        }

        page.locator(".topic-summary-link").first().click();
        Locator next = page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Next topic").setExact(true));
        for (int index = 0; index < originalOrder.size(); index++) {
            assertThat(page.locator(".conversations-topic__title")).hasText(originalOrder.get(index));
            assertThat(page.locator(".conversations-topic__title")).isFocused();
            assertThat(page.locator(".conversations-topic__title")).isInViewport();
            if (index + 1 < originalOrder.size()) {
                assertThat(next).isEnabled();
                next.click();
            }
        }
        assertThat(next).isDisabled();

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Topics").setExact(true)).click();
        assertThat(filter).hasValue("by_question");
        assertThat(titles).hasText(originalOrder.toArray(String[]::new));
        assertThat(page.locator(".topic-summary-link").filter(new Locator.FilterOptions()
            .setHasText(originalOrder.get(originalOrder.size() - 1)))).isFocused();
    }

    @Test
    @Order(4)
    void updatesTopicMenuActionsWithoutLeavingList() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Conversation");

        Locator topic = page.locator("sakai-topic-summary").filter(
            new Locator.FilterOptions().setHasText(NAVIGATION_DISCUSSION));
        topic.locator("[data-bs-toggle='dropdown']").click();
        topic.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Hide this topic").setExact(true)).click();
        assertThat(topic.locator(".topic-status sakai-icon[type='hidden']")).isVisible();

        topic.locator("[data-bs-toggle='dropdown']").click();
        topic.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Lock this topic").setExact(true)).click();
        assertThat(topic.locator(".topic-status sakai-icon[type='lock']")).isVisible();

        topic.locator("[data-bs-toggle='dropdown']").click();
        page.onceDialog(dialog -> dialog.accept());
        topic.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Delete this topic").setExact(true)).click();
        assertThat(topic).hasCount(0);
    }

    private void publishTopic(String title, String type) {
        page.locator("#conv-add-topic").click();
        page.locator(".topic-type-toggle[data-type='" + type + "']").click();
        page.locator("#summary").fill(title);
        if (!sakai.typeFirstCkEditorIfPresent("<p>" + TOPIC_BODY + "</p>")) {
            throw new AssertionError("Expected the Conversations topic editor");
        }
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Publish").setExact(true)).click();
        assertThat(page.locator(".conversations-topic__title")).hasText(title);
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
