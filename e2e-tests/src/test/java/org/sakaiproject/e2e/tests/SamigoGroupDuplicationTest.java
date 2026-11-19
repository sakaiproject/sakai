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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;
import com.microsoft.playwright.options.SelectOption;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class SamigoGroupDuplicationTest extends SakaiUiTestBase {

    @Test
    void duplicatedGroupQuizRequiresAnExplicitGroupBeforePublishing() {
        String title = "SAK-52145 " + UUID.randomUUID();
        sakai.login("instructor1");
        String siteUrl = sakai.createCourse("instructor1", List.of("sakai\\.samigo"));
        addTeachingAssistant(siteUrl);
        String groupA = sakai.addGroup(siteUrl, "Lecture A", "ta1", "student0011");
        String groupB = sakai.addGroup(siteUrl, "Lecture B", "student0012");
        openAssessments(siteUrl);
        allowTeachingAssistantToViewDrafts();
        createQuiz(title);

        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Settings").setExact(true)).first().click();
        releaseTo().selectOption("Selected Groups");
        groups().selectOption(groupA, new Locator.SelectOptionOptions().setForce(true));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save").setExact(true)).click();
        openAssessments(siteUrl);
        assertThat(draftRow(title).locator("td.releaseTo")).containsText("1 Selected Group");

        chooseAction(draftRow(title), "Duplicate");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Duplicate").setExact(true)).click();
        String copyTitle = title + " - Copy #1";
        assertThat(draftRow(copyTitle)).isVisible();
        assertThat(draftRow(copyTitle).locator("td.releaseTo")).hasText(
            "This assessment was released to a group, but no group is selected. Please select a group to publish this assessment.");
        page.reload();
        assertThat(draftRow(copyTitle)).isVisible();
        assertThat(draftRow(title).locator("td.releaseTo")).containsText("1 Selected Group");

        // An unassigned duplicate must not bypass the TA's group restrictions.
        sakai.login("ta1");
        openAssessments(siteUrl);
        assertThat(draftRow(title)).isVisible();
        assertThat(draftRow(copyTitle)).hasCount(0);

        sakai.login("instructor1");
        openAssessments(siteUrl);
        chooseAction(draftRow(copyTitle), "Publish");
        assertThat(page.getByText("Please select to whom you wish to make the assessment available", new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(releaseTo()).hasValue("Selected Groups");
        assertThat(groups().locator("option:checked")).hasCount(0);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save Settings and Publish").setExact(true)).click();
        assertThat(page.getByText("Alert: You need to choose at least one group.", new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(releaseTo()).hasValue("Selected Groups");

        groups().selectOption(groupB, new Locator.SelectOptionOptions().setForce(true));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save Settings and Publish").setExact(true)).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Publish").setExact(true)).click();
        Locator publishedCopy = assessmentRows(copyTitle)
            .filter(new Locator.FilterOptions().setHas(page.locator("span[class^='status_published_']")));
        assertThat(publishedCopy).isVisible();
        assertThat(publishedCopy.locator("td.releaseTo")).containsText("1 Selected Group");
        assertThat(publishedCopy.locator("td.releaseTo")).containsText("Lecture B");
        assertThat(publishedCopy.locator("td.releaseTo")).not().containsText("Lecture A");

        sakai.login("ta1");
        openAssessments(siteUrl);
        assertThat(draftRow(title)).isVisible();
        assertThat(assessmentRows(copyTitle)).hasCount(0);
    }

    private void openAssessments(String siteUrl) {
        page.navigate(siteUrl);
        sakai.toolClick("Tests");
        assertThat(page.locator("#authorIndexForm")).isVisible();
    }

    private Locator assessmentRows(String title) {
        return page.locator("#authorIndexForm\\:coreAssessments tbody tr")
            .filter(new Locator.FilterOptions().setHas(page.getByText(title, new Page.GetByTextOptions().setExact(true))));
    }

    private Locator draftRow(String title) {
        return assessmentRows(title).filter(new Locator.FilterOptions().setHas(page.locator(".status_draft")));
    }

    private void chooseAction(Locator row, String action) {
        row.getByRole(AriaRole.BUTTON).click();
        row.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName(action).setExact(true)).click();
    }

    private Locator releaseTo() {
        return page.locator("#assessmentSettingsAction\\:releaseTo");
    }

    private Locator groups() {
        return page.locator("#assessmentSettingsAction\\:groupsForSite");
    }

    private void createQuiz(String title) {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Add").setExact(true)).click();
        page.locator("#authorIndexForm\\:title").fill(title);
        page.locator("#authorIndexForm\\:createnew").click();
        page.locator("#assessmentForm\\:parts\\:0\\:changeQType")
            .selectOption(new SelectOption().setLabel("True False"));
        page.locator("#itemForm\\:answerptr").fill("1");
        page.locator("#itemForm textarea").first().fill("A duplicated group quiz must retain its audience setting.");
        page.locator("#itemForm\\:TF input[type=radio]").first().check();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save").setExact(true)).first().click();
        assertThat(page.locator("#assessmentForm\\:parts .samigo-question-callout")).hasCount(1);
    }

    private void addTeachingAssistant(String siteUrl) {
        APIResponse user = page.request().get("/direct/user/" + sakai.resolveUsername("ta1") + ".json");
        assertTrue(user.ok(), user.text());
        String userId = JsonParser.parseString(user.text()).getAsJsonObject().get("id").getAsString();
        APIResponse membership = page.request().post("/direct/membership/new", RequestOptions.create().setForm(FormData.create()
            .set("locationReference", "/site/" + sakai.siteIdFromUrl(siteUrl)).set("userId", userId)
            .set("memberRole", "Teaching Assistant").set("active", "true")));
        assertTrue(membership.ok(), membership.text());
    }

    private void allowTeachingAssistantToViewDrafts() {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Permissions").setExact(true)).click();
        Locator permissions = page.locator("#samigo-permissions");
        permissions.locator("input[data-role='Teaching Assistant'][data-perm='assessment.editAssessment.any']").check();
        permissions.locator("input[data-role='Teaching Assistant'][data-perm='assessment.gradeAssessment.any']").uncheck();
        permissions.locator("input[data-role='Teaching Assistant'][data-perm='assessment.gradeAssessment.own']").check();
        assertThat(permissions.locator("input[data-role='Teaching Assistant'][data-perm='assessment.all.groups']")).not().isChecked();
        page.waitForNavigation(() -> permissions.getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName("Save").setExact(true)).click());
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Assessments").setExact(true)).click();
        assertThat(page.locator("#authorIndexForm")).isVisible();
    }
}
