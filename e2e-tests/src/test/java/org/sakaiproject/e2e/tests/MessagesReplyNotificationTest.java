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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class MessagesReplyNotificationTest extends SakaiUiTestBase {

    private String subject;

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void repliesNotifyTheirRecipientsAndOpenTheReply(boolean replyAll) {
        sakai.login("instructor1");
        String courseUrl = sakai.createCourse("instructor1", List.of("sakai.messages"));
        String siteId = sakai.siteIdFromUrl(courseUrl);
        subject = "Reply notification " + sakai.randomId();
        sakai.gotoPath(courseUrl);
        sakai.toolClick("Messages");
        page.locator("#messagesComposeMenuLink").click();
        page.locator("[id='compose:list1']").selectOption(new SelectOption().setLabel("Student Role"));
        page.locator("[id='compose:subject']").fill(subject);
        sakai.typeCkEditor("compose:pvt_message_body_inputRichText", "<p>Original instructor message</p>");
        send();

        sakai.login("student0011");
        JsonObject original = awaitNotification(siteId, "messages.new");
        openNotification(original);
        assertThat(page.locator("body")).containsText("Original instructor message");
        reply("Student reply body", replyAll, false);

        sakai.login("instructor1");
        JsonObject studentReply = awaitNotification(siteId, "messages.reply");
        assertEquals("Re: " + subject, studentReply.get("title").getAsString());
        assertNotEquals(original.get("from"), studentReply.get("from"));
        assertNotEquals(original.get("url"), studentReply.get("url"));
        openNotification(studentReply);
        assertThat(page.locator("body")).containsText("Student reply body");
        reply("Instructor reply body", false, true);

        sakai.login("student0011");
        JsonObject instructorReply = awaitNotification(siteId, "messages.reply");
        assertEquals(original.get("from"), instructorReply.get("from"));
        assertNotEquals(studentReply.get("url"), instructorReply.get("url"));
        openNotification(instructorReply);
        assertThat(page.locator("body")).containsText("Instructor reply body");

        // Another student received the role message, but only Reply All includes them in the reply.
        sakai.login("student0012");
        List<JsonObject> notifications = siteNotifications(siteId);
        assertEquals(replyAll ? 2 : 1, notifications.size());
        if (replyAll) {
            JsonObject otherStudentReply = awaitNotification(siteId, "messages.reply");
            assertEquals(studentReply.get("from"), otherStudentReply.get("from"));
            assertEquals(studentReply.get("url"), otherStudentReply.get("url"));
        }
    }

    private void reply(String body, boolean replyAll, boolean preview) {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
            .setName(replyAll ? "Reply to all" : "Reply").setExact(true)).first().click();
        assertTrue(sakai.typeFirstCkEditorIfPresent("<p>" + body + "</p>"));
        if (preview) {
            page.locator("input[type=submit][value=Preview]").click();
            assertThat(page.locator("body")).containsText(body);
        }
        send();
    }

    private void send() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Send").setExact(true)).first().click();
        assertThat(page.locator("#messagesComposeMenuLink")).isVisible();
    }

    private List<JsonObject> siteNotifications(String siteId) {
        APIResponse response = page.request().get("/api/users/me/notifications");
        try {
            assertTrue(response.ok());
            List<JsonObject> notifications = new ArrayList<>();
            for (JsonElement element : JsonParser.parseString(response.text()).getAsJsonArray()) {
                JsonObject notification = element.getAsJsonObject();
                if (siteId.equals(notification.get("siteId").getAsString())
                        && notification.get("title").getAsString().contains(subject)) {
                    notifications.add(notification);
                }
            }
            return notifications;
        } finally {
            response.dispose();
        }
    }

    private JsonObject awaitNotification(String siteId, String event) {
        page.waitForCondition(() -> siteNotifications(siteId).stream()
            .anyMatch(n -> event.equals(n.get("event").getAsString())),
            new Page.WaitForConditionOptions().setTimeout(15_000));
        List<JsonObject> matching = siteNotifications(siteId).stream()
            .filter(n -> event.equals(n.get("event").getAsString())).toList();
        assertEquals(1, matching.size());
        return matching.get(0);
    }

    private void openNotification(JsonObject notification) {
        page.locator("button[aria-controls='sakai-notifications-panel']:visible").click();
        Locator messages = page.locator("sakai-notifications #messages-accordion");
        page.locator("sakai-notifications button[aria-controls='messages-accordion']").click();
        Locator item = messages.locator("li.toast").filter(new Locator.FilterOptions()
            .setHas(page.locator("a[href='" + notification.get("url").getAsString() + "']")));
        assertThat(item).containsText(notification.get("fromDisplayName").getAsString() + " sent you a message");
        item.locator("a").click();
        assertThat(page.locator("#pvtMsgDetail .breadCrumb")).containsText("Received");
    }
}
