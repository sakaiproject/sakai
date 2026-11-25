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

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class LessonsImportHierarchyTest extends SakaiUiTestBase {

    private static final String INSTRUCTOR = "instructor1";
    private static final List<String> TOOLS = List.of("sakai\\.lessonbuildertool");

    private LessonsUi lessons;

    @BeforeEach
    void openInstructor() {
        lessons = new LessonsUi(page, sakai);
        sakai.login(INSTRUCTOR);
    }

    @Test
    void importFromSiteShowsLessonsSubpagesUnderTheirParent() {
        String sourceSite = sakai.createFreshCourse(INSTRUCTOR, TOOLS);
        String destinationSite = sakai.createFreshCourse(INSTRUCTOR, TOOLS);

        lessons.open(sourceSite);
        String week1 = "Week 1 " + sakai.randomId();
        String assignments = "Assignments " + sakai.randomId();
        String readings = "Readings " + sakai.randomId();
        lessons.createTopLevelPage(week1);
        lessons.addSubpage(assignments);
        lessons.backToParent(week1);
        lessons.addSubpage(readings);
        lessons.backToParent(week1);

        openImportToolSelection(destinationSite, sourceSite);
        String sourceSiteId = sakai.siteIdFromUrl(sourceSite);
        Locator expand = page.locator("button.tool-items-button[data-tool-id='sakai.lessonbuildertool']");
        assertThat(expand).isVisible();
        expand.click();

        Locator assignmentLabel = page.locator(".tool-item-row label")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^" + Pattern.quote(assignments) + "$")));
        Locator readingsLabel = page.locator(".tool-item-row label")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^" + Pattern.quote(readings) + "$")));
        assertThat(assignmentLabel).isVisible();
        assertThat(readingsLabel).isVisible();

        Locator assignmentCell = assignmentLabel.locator("xpath=ancestor::td[1]");
        Locator readingsCell = readingsLabel.locator("xpath=ancestor::td[1]");
        assertThat(assignmentCell.locator(".item-parent-label")).containsText("Subpage of " + week1);
        assertThat(readingsCell.locator(".item-parent-label")).containsText("Subpage of " + week1);

        Locator assignmentCheckbox = assignmentCell.locator("input.tool-item-checkbox");
        Locator readingsCheckbox = readingsCell.locator("input.tool-item-checkbox");
        String weekPageId = assignmentCheckbox.getAttribute("data-parent-id");
        assertTrue(weekPageId != null && !weekPageId.isBlank() && !"0".equals(weekPageId));
        Locator weekCheckbox = page.locator(
            "input.tool-item-checkbox[data-site-id=\"" + sourceSiteId + "\"][value=\"" + weekPageId + "\"]");
        assertThat(weekCheckbox).isVisible();

        weekCheckbox.check(new Locator.CheckOptions().setForce(true));
        assertTrue(assignmentCheckbox.isChecked());
        assertTrue(readingsCheckbox.isChecked());

        weekCheckbox.uncheck(new Locator.UncheckOptions().setForce(true));
        assertTrue(!assignmentCheckbox.isChecked());
        assertTrue(!readingsCheckbox.isChecked());
        assertThat(page.locator("#toolSite-sakai\\.lessonbuildertool-" + sourceSiteId)).isVisible();
    }

    private void openImportToolSelection(String destinationSite, String sourceSite) {
        page.navigate(destinationSite);
        sakai.toolClick("Site Info");
        page.locator(".navIntraTool a, .navIntraTool button")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Import from Site$", Pattern.CASE_INSENSITIVE)))
            .first()
            .click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions()
                .setName(Pattern.compile("replace my data", Pattern.CASE_INSENSITIVE)))
            .first()
            .click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);

        String sourceSiteId = sakai.siteIdFromUrl(sourceSite);
        page.locator("input[name=\"importSites\"][value=\"" + sourceSiteId + "\"]")
            .check(new Locator.CheckOptions().setForce(true));
        clickContinueOrFinish();
        assertThat(page.locator("#toolSite-sakai\\.lessonbuildertool-" + sourceSiteId)).isVisible();
    }

    private void clickContinueOrFinish() {
        Locator button = page.locator(
            "input[type=\"submit\"][value*=\"Continue\"], input[type=\"submit\"][value*=\"Finish\"], "
                + "button:has-text(\"Continue\"), button:has-text(\"Finish\"), #siteimport-finish-button"
        ).first();
        assertThat(button).isVisible();
        button.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }
}
