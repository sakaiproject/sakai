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

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

@EnabledIfSystemProperty(named = "sakai.test.assignmentTa.siteUrl", matches = ".+")
class AssignmentTeachingAssistantTest extends SakaiUiTestBase {

    private static final String GROUP_ASSIGNMENT = "SAK-51409 Group assignment";
    private static final String SITE_ASSIGNMENT = "SAK-51409 Site assignment";
    private static final String TA = "instructor2";
    private static final String STUDENT = "student0011";
    private static final String OTHER_STUDENT = "student0012";

    @Test
    void instructorStudentListExcludesTeachingAssistant() {
        openStudentList("instructor1");
        assertThat(studentLink(STUDENT)).isVisible();
        assertThat(studentLink(OTHER_STUDENT)).isVisible();
        assertThat(studentLink(TA)).hasCount(0);
    }

    @Test
    void teachingAssistantSeesOnlyEligibleStudentsAndAssignments() {
        openStudentList(TA);
        assertThat(studentLink(STUDENT)).isVisible();
        assertThat(studentLink(TA)).hasCount(0);
        assertThat(studentLink(OTHER_STUDENT)).hasCount(0);
        assertThat(page.locator("#viewgroup option")
            .filter(new Locator.FilterOptions().setHasText("SAK-51409 Other group"))).hasCount(0);
        studentLink(STUDENT).click();
        Locator table = page.locator("#assignmentsByStudent");
        assertThat(table).not().containsText(SITE_ASSIGNMENT);
        Response grading = page.waitForResponse(
            response -> response.url().contains("/direct/assignment/gradable.json"),
            () -> table.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName(GROUP_ASSIGNMENT).setExact(true)).click()
        );
        assertEquals(200, grading.status());
        assertThat(page.locator("sakai-grader")).isVisible();
    }

    @Test
    void teachingAssistantCanOpenSubmissionOnBehalf() {
        openStudentList(TA);
        studentLink(STUDENT).click();
        Locator assignmentRow = page.locator("#assignmentsByStudent tbody tr")
            .filter(new Locator.FilterOptions().setHasText(GROUP_ASSIGNMENT));
        assignmentRow.getByRole(AriaRole.LINK,
            new Locator.GetByRoleOptions().setName("Submit on behalf of Student").setExact(true)).click();
        assertThat(page.locator("textarea[name=\"Assignment.view_submission_text\"]")).isEnabled();
    }

    private void openStudentList(String user) {
        sakai.login(user);
        page.navigate(System.getProperty("sakai.test.assignmentTa.siteUrl"));
        sakai.toolClick("Assignments");
        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName("Assignments by Student").setExact(true)).click();
    }

    private Locator studentLink(String eid) {
        return page.locator("#assignmentsByStudent").getByRole(AriaRole.LINK,
            new Locator.GetByRoleOptions().setName(Pattern.compile("\\(" + Pattern.quote(eid) + "\\)")));
    }
}
