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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.options.LoadState;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/** SAK-50040: scores entered in the Gradebook notify the student when the item is released. */
class GradebookGradeNotificationTest extends SakaiUiTestBase {

    private static final String STUDENT = "student0011";
    private static final String OTHER_STUDENT = "student0012";

    @Test
    void releasedItemScoresNotifyTheStudent() {
        String studentId = userId(STUDENT);
        String otherStudentId = userId(OTHER_STUDENT);

        sakai.login("instructor1");
        String siteUrl = sakai.createCourse("instructor1", List.of("sakai.gradebookng"));
        String siteId = sakai.siteIdFromUrl(siteUrl);
        page.navigate(siteUrl);
        sakai.toolClick("Gradebook");
        assertThat(page.locator(".gb-view-grade-summary").nth(1)).isVisible();

        String released = "Released " + sakai.randomId();
        String hidden = "Hidden " + sakai.randomId();
        String releasedId = addItem(released, true);
        String hiddenId = addItem(hidden, false);

        enterScore(releasedId, STUDENT, studentId, "85");
        // A re-score before the student has looked must not stack a second notification
        enterScore(releasedId, STUDENT, studentId, "90");
        enterScore(hiddenId, OTHER_STUDENT, otherStudentId, "70");

        sakai.login(STUDENT);
        page.waitForCondition(() -> !notifications(siteId, released).isEmpty(),
            new Page.WaitForConditionOptions().setTimeout(15_000));
        List<JsonObject> forStudent = notifications(siteId, released);
        assertEquals(1, forStudent.size());
        JsonObject notification = forStudent.get(0);
        assertEquals("gradebook.updateItemScore", notification.get("event").getAsString());
        assertEquals("sakai.gradebookng", notification.get("tool").getAsString());

        page.navigate(siteUrl);
        page.locator("button[aria-controls='sakai-notifications-panel']:visible").click();
        page.locator("sakai-notifications button[aria-controls='gradebook-accordion']").click();
        Locator item = page.locator("sakai-notifications #gradebook-accordion li.toast")
            .filter(new Locator.FilterOptions().setHasText(released));
        assertThat(item).containsText("Posted a grade for \"" + released + "\"");
        item.locator("a").click();
        assertThat(page.locator("body")).containsText(released);
        assertThat(page.locator("body")).not().containsText(hidden);

        sakai.login(OTHER_STUDENT);
        assertTrue(notifications(siteId, hidden).isEmpty(), "an unreleased item must not notify");
    }

    private String addItem(String title, boolean releaseToStudents) {
        page.locator("button.gb-add-gradebook-item-button").click();
        Locator dialog = page.locator(".gb-modal-dialog:visible");
        dialog.locator("input[name$=\"title\"]").fill(title);
        dialog.locator("input[name$=\"points\"]").fill("100");
        dialog.locator("input[name$=\"released\"]").setChecked(releaseToStudents);
        dialog.locator("input[name$=\"counted\"]").uncheck();
        dialog.locator("button.gb-create-or-save").click();
        assertThat(dialog).isHidden(new LocatorAssertions.IsHiddenOptions().setTimeout(15_000));
        Locator column = page.locator(".tabulator-col[data-assignment-id]")
            .filter(new Locator.FilterOptions().setHasText(title));
        assertThat(column).isVisible();
        page.waitForLoadState(LoadState.LOAD);
        page.waitForTimeout(100);
        return column.getAttribute("data-assignment-id");
    }

    private void enterScore(String assignmentId, String eid, String studentId, String score) {
        Locator filter = page.locator("#studentFilterInput");
        filter.fill(eid);
        filter.press("Enter");
        Locator cell = page.locator(".tabulator-cell[data-assignment-id=\"" + assignmentId
            + "\"][data-student-id=\"" + studentId + "\"]");
        cell.click();
        Locator editor = cell.locator("input.tabulator-input");
        assertThat(editor).isFocused();
        editor.fill(score);
        page.keyboard().press("Enter");
        assertThat(cell.locator(".gb-value")).hasText(score);
        assertThat(cell).hasClass(java.util.regex.Pattern.compile(".*\\bgb-save-success\\b.*"));
    }

    private String userId(String eid) {
        sakai.login(eid);
        APIResponse response = page.request().get("/direct/user/current.json");
        try {
            assertTrue(response.ok());
            return JsonParser.parseString(response.text()).getAsJsonObject().get("id").getAsString();
        } finally {
            response.dispose();
        }
    }

    private List<JsonObject> notifications(String siteId, String title) {
        APIResponse response = page.request().get("/api/users/me/notifications");
        try {
            assertTrue(response.ok());
            List<JsonObject> result = new ArrayList<>();
            for (JsonElement element : JsonParser.parseString(response.text()).getAsJsonArray()) {
                JsonObject n = element.getAsJsonObject();
                if (siteId.equals(n.get("siteId").getAsString()) && title.equals(n.get("title").getAsString())) {
                    result.add(n);
                }
            }
            return result;
        } finally {
            response.dispose();
        }
    }
}
