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
import com.microsoft.playwright.options.SelectOption;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class SamigoTimedPreviewTest extends SakaiUiTestBase {

    @Test
    void canPreviewTimedPartWithLinearNavigation() {
        createAssessment("Timed preview " + sakai.randomId());
        addQuestion("First timed preview question");
        addQuestion("Second timed preview question");

        page.locator("[id='assessmentForm:parts:0:editPart']").click();
        page.locator("input[name='modifyPartForm:selTimed'][value='true']").check();
        page.locator("[id='modifyPartForm:timeSection'] input[type='number']").nth(1).fill("3");
        button("Save").first().click();

        link("Settings").click();
        page.getByText("Layout and Appearance", new Page.GetByTextOptions().setExact(true)).click();
        page.locator("input[name='assessmentSettingsAction:itemNavigation'][value='1']").check();
        page.locator("input[name='assessmentSettingsAction:assessmentFormat'][value='1']").check();
        button("Save").first().click();

        link("Preview").click();
        beginAssessment();

        assertThat(page.getByText("First timed preview question")).isVisible();
        Locator progress = page.locator("#questionProgressPanel table[id$=':tocquestions']");
        assertThat(progress.locator(".progressItem .linearButton")).hasCount(2);
        Locator next = page.locator("input[id$=':next']");
        assertThat(next).isEnabled();
        next.click();
        assertThat(page.getByText("Second timed preview question")).isVisible();
        assertThat(progress.locator(".currentQuestion")).containsText("2");
    }

    @Test
    void studentCanBeginAssessmentWithExtendedTime() {
        String title = "Extended time " + sakai.randomId();
        String courseUrl = createAssessment(title);
        addQuestion("Extended time delivery question");

        link("Settings").click();
        link("Expand All").click();
        String availableSince = LocalDate.now(ZoneOffset.UTC).minusDays(1).atStartOfDay().toString();
        sakai.selectDate("[id='assessmentSettingsAction:startDate']", availableSince);
        page.locator("input[name='assessmentSettingsAction:selTimeAssess'][value='true']").check();
        page.locator("select[id$=':timedMinutes']").selectOption(new SelectOption().setLabel("05"));
        page.locator("input[id$=':extendedEnableUser']").check();
        Locator student = page.locator("select[id$=':newEntry-user']");
        String studentId = student.locator("option").filter(new Locator.FilterOptions().setHasText("(student0011)"))
            .getAttribute("value");
        student.selectOption(studentId);
        sakai.selectDate("[id='assessmentSettingsAction:newEntry-start_date']", availableSince);
        page.locator("select[id$=':newEntry-mins']").selectOption(new SelectOption().setLabel("12"));
        page.locator("[id='assessmentSettingsAction:extendedTimeAdd']").click();
        button("Save Settings and Publish").click();
        button("Publish").click();

        sakai.login("student0011");
        sakai.gotoPath(courseUrl);
        sakai.toolClick("Tests");
        link(title).click();
        assertThat(page.locator("#takeAssessmentForm")).containsText("12 minutes");
        beginAssessment();
        assertThat(page.getByText("Extended time delivery question")).isVisible();
    }

    private String createAssessment(String title) {
        sakai.login("instructor1");
        String courseUrl = sakai.createCourse("instructor1", List.of("sakai\\.samigo"));
        sakai.gotoPath(courseUrl);
        sakai.toolClick("Tests");
        link("Add").click();
        page.locator("[id='authorIndexForm:title']").fill(title);
        page.locator("[id='authorIndexForm:createnew']").click();
        return courseUrl;
    }

    private void beginAssessment() {
        Locator honorPledge = page.locator("[id='takeAssessmentForm:honor_pledge']");
        if (honorPledge.isVisible()) {
            honorPledge.check();
        }
        button("Begin Assessment").click();
    }

    private void addQuestion(String text) {
        Locator type = page.locator("[id='assessmentForm:parts:0:changeQType']");
        type.selectOption("1");
        page.locator("[id='itemForm:answerptr']").fill("1");
        page.locator("#itemForm textarea").first().fill(text);
        Locator choices = page.locator("[id='itemForm:mcchoices']");
        choices.locator("textarea").nth(0).fill("Option A");
        choices.locator("textarea").nth(1).fill("Option B");
        choices.locator("input[type='radio']").first().check();
        button("Save").first().click();
        assertThat(page.locator("[id='assessmentForm:parts']")).isVisible();
    }

    private Locator link(String name) {
        return page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(name).setExact(true));
    }

    private Locator button(String name) {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(name).setExact(true));
    }
}
