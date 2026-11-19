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

import com.google.gson.JsonParser;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;
import com.microsoft.playwright.options.SelectOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class AssignmentTeachingAssistantTest extends SakaiUiTestBase {

    private static final String GROUP_ASSIGNMENT = "SAK-51409 Group assignment";
    private static final String SITE_ASSIGNMENT = "SAK-51409 Site assignment";
    private static final String TA_GROUP = "SAK-51409 TA group";
    private static final String OTHER_GROUP = "SAK-51409 Other group";
    private static final String TA = "ta1";
    private static final String STUDENT = "student0011";
    private static final String OTHER_STUDENT = "student0012";

    @Test
    void assignmentsByStudentRespectsTeachingAssistantPermissions() {
        sakai.login("instructor1");
        String siteUrl = sakai.createCourse("instructor1", List.of("sakai\\.assignment\\.grades"));
        String siteId = sakai.siteIdFromUrl(siteUrl);
        String taId = addMember(siteId, TA, "Teaching Assistant");
        String studentId = addMember(siteId, STUDENT, "Student");
        String otherStudentId = addMember(siteId, OTHER_STUDENT, "Student");
        createGroup(siteId, TA_GROUP, List.of(taId, studentId));
        createGroup(siteId, OTHER_GROUP, List.of(otherStudentId));
        createAssignment(siteUrl, GROUP_ASSIGNMENT, true);
        createAssignment(siteUrl, SITE_ASSIGNMENT, false);

        sakai.login(STUDENT);
        page.navigate(siteUrl);
        sakai.toolClick("Assignments");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(GROUP_ASSIGNMENT).setExact(true)).click();
        sakai.typeCkEditor("Assignment.view_submission_text", "<p>Student submission for the TA to grade.</p>");
        page.locator("#confirm").click();
        page.locator("#post").click();
        assertThat(page.locator("body")).containsText("Submission Confirmation");

        openStudentList(siteUrl, "instructor1");
        assertThat(studentLink(STUDENT)).isVisible();
        assertThat(studentLink(OTHER_STUDENT)).isVisible();
        assertThat(studentLink(TA)).hasCount(0);

        openStudentList(siteUrl, TA);
        assertThat(studentLink(STUDENT)).isVisible();
        assertThat(studentLink(TA)).hasCount(0);
        assertThat(studentLink(OTHER_STUDENT)).hasCount(0);
        assertThat(page.locator("select[name=\"viewgroup\"] option")
            .filter(new Locator.FilterOptions().setHasText(TA_GROUP))).hasCount(1);
        assertThat(page.locator("select[name=\"viewgroup\"] option")
            .filter(new Locator.FilterOptions().setHasText(OTHER_GROUP))).hasCount(0);
        studentLink(STUDENT).click();
        Locator table = page.locator("#assignmentsByStudent");
        assertThat(table).not().containsText(SITE_ASSIGNMENT);
        Response grading = page.waitForResponse(
            response -> response.url().contains("/direct/assignment/gradable.json"),
            () -> table.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName(GROUP_ASSIGNMENT).setExact(true)).click()
        );
        assertEquals(200, grading.status());
        assertThat(page.locator("sakai-grader")).isVisible();

        openStudentList(siteUrl, TA);
        studentLink(STUDENT).click();
        Locator assignmentRow = page.locator("#assignmentsByStudent tbody tr")
            .filter(new Locator.FilterOptions().setHasText(GROUP_ASSIGNMENT));
        assignmentRow.getByRole(AriaRole.LINK,
            new Locator.GetByRoleOptions().setName("Submit on behalf of Student").setExact(true)).click();
        assertThat(page.locator("textarea[name=\"Assignment.view_submission_text\"]")).isEnabled();
        assertThat(page.locator("#submit_on_behalf_of")).hasValue(studentId);
    }

    private String addMember(String siteId, String eid, String role) {
        APIResponse user = page.request().get("/direct/user/" + eid + ".json");
        assertTrue(user.ok(), user.text());
        String userId = JsonParser.parseString(user.text()).getAsJsonObject().get("id").getAsString();
        APIResponse membership = page.request().post("/direct/membership/new", RequestOptions.create().setForm(FormData.create()
            .set("locationReference", "/site/" + siteId).set("userId", userId).set("memberRole", role).set("active", "true")));
        assertTrue(membership.ok(), membership.text());
        return userId;
    }

    private void createGroup(String siteId, String title, List<String> userIds) {
        APIResponse response = page.request().put("/direct/site/" + siteId + "/group", RequestOptions.create()
            .setQueryParam("groupTitle", title).setQueryParam("userIds", String.join(",", userIds)));
        assertTrue(response.ok(), response.text());
    }

    private void createAssignment(String siteUrl, String title, boolean grouped) {
        page.navigate(siteUrl);
        sakai.toolClick("Assignments");
        page.locator(".navIntraTool").getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Add").setExact(true)).click();
        page.locator("#new_assignment_title").fill(title);
        page.locator("#subType").selectOption("1");
        page.locator("#new_assignment_grade_points").fill("10");
        page.locator("#new_assignment_check_add_honor_pledge").uncheck();
        sakai.typeCkEditor("new_assignment_instructions", "<p>Submit a short response.</p>");
        LocalDateTime openDate = LocalDateTime.parse(page.locator("#opendate input[type=\"datetime-local\"]").inputValue());
        sakai.selectDate("#opendate", openDate.minusDays(1).toString());
        sakai.selectDate("#duedate", openDate.plusDays(7).toString());
        sakai.selectDate("#closedate", openDate.plusDays(7).toString());
        page.locator("#allowResToggle").check();
        if (grouped) {
            page.locator("#groups").check();
            page.locator("#selectedGroups").selectOption(new SelectOption().setLabel(TA_GROUP),
                new Locator.SelectOptionOptions().setForce(true));
        }
        page.locator("input[name=\"post\"]").click();
        assertThat(page.locator("#assignments-list")).containsText(title);
    }

    private void openStudentList(String siteUrl, String user) {
        sakai.login(user);
        page.navigate(siteUrl);
        sakai.toolClick("Assignments");
        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName("Assignments by Student").setExact(true)).click();
        page.locator("select[aria-controls=\"assignmentsByStudent\"]").selectOption("100");
    }

    private Locator studentLink(String eid) {
        return page.locator("#assignmentsByStudent").getByRole(AriaRole.LINK,
            new Locator.GetByRoleOptions().setName(Pattern.compile("\\(" + eid + "\\)")));
    }
}
