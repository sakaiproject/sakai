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
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class LessonsImportReplaceTest extends SakaiUiTestBase {

    private static final String IMPORTED_PAGE = "Imported page";
    private static final String PRESERVED_PAGE = "Preserved page";
    private static final String PRESERVED_CONTENT = "Preserved content";

    @Test
    void replaceImportKeepsExistingLessonsPagesAvailableForRecovery() {
        sakai.login("instructor1");
        String sourceSite = sakai.createCourse("instructor1", List.of("sakai\\.lessonbuildertool"));
        String destinationSite = sakai.createCourse("instructor1", List.of("sakai\\.lessonbuildertool"));

        page.navigate(sourceSite);
        sakai.toolClick("Lessons");
        addSubpage(IMPORTED_PAGE);

        page.navigate(destinationSite);
        sakai.toolClick("Lessons");
        addSubpage(PRESERVED_PAGE);
        openPage(PRESERVED_PAGE);
        addSubpage(PRESERVED_CONTENT);

        replaceLessonsFromSite(destinationSite, sourceSite);

        page.navigate(destinationSite);
        sakai.toolClick("Lessons");
        assertThat(page.locator("body")).containsText(IMPORTED_PAGE);

        page.locator("#show-pages:visible").click(new Locator.ClickOptions().setForce(true));
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Pages in Current Site"))).isVisible();
        assertThat(page.locator("body")).containsText("The following pages are currently not in use");

        Locator preservedPageRow = page.locator("#list > li")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^\\s*" + PRESERVED_PAGE + "\\s*$")))
            .first();
        assertThat(preservedPageRow.locator("input.deletebox")).isVisible();

        page.navigate(destinationSite);
        sakai.toolClick("Lessons");
        openExistingPageChooser();
        Locator pageChoice = page.locator("#list > li")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^\\s*" + PRESERVED_PAGE + "\\s*$")))
            .first();
        pageChoice.locator("input[type=radio]").check(new Locator.CheckOptions().setForce(true));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("^Select$", Pattern.CASE_INSENSITIVE)))
            .click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();

        openPage(PRESERVED_PAGE);
        assertThat(page.locator("body")).containsText(PRESERVED_CONTENT);
    }

    private void addSubpage(String title) {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Content"))
            .first()
            .click(new Locator.ClickOptions().setForce(true));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Subpage"))
            .first()
            .click(new Locator.ClickOptions().setForce(true));
        page.locator("#subpage-title:visible").fill(title);
        page.locator("#subpage-dialog:visible button.btn-primary")
            .click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();
        assertThat(page.locator("body")).containsText(title);
    }

    private void openPage(String title) {
        page.locator("a:visible")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^\\s*" + title + "\\s*$")))
            .first()
            .click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();
    }

    private void openExistingPageChooser() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Content"))
            .first()
            .click(new Locator.ClickOptions().setForce(true));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Subpage"))
            .first()
            .click(new Locator.ClickOptions().setForce(true));
        page.locator("#subpage-choose-button:visible").click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();
    }

    private void replaceLessonsFromSite(String destinationSite, String sourceSite) {
        page.navigate(destinationSite);
        sakai.toolClick("Site Info");
        page.locator(".navIntraTool a, .navIntraTool button")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Import from Site$", Pattern.CASE_INSENSITIVE)))
            .first()
            .click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions()
                .setName(Pattern.compile("replace my data", Pattern.CASE_INSENSITIVE)))
            .first()
            .click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();

        String sourceSiteId = sakai.siteIdFromUrl(sourceSite);
        page.locator("input[name=\"importSites\"][value=\"" + sourceSiteId + "\"]")
            .check(new Locator.CheckOptions().setForce(true));
        clickContinueOrFinish();

        page.locator("#toolSite-sakai\\.lessonbuildertool-" + sourceSiteId)
            .check(new Locator.CheckOptions().setForce(true));
        page.onDialog(dialog -> dialog.accept());
        clickContinueOrFinish();
        page.waitForTimeout(10_000);
    }

    private void clickContinueOrFinish() {
        Locator button = page.locator(
            "input[type=\"submit\"][value*=\"Continue\"], input[type=\"submit\"][value*=\"Finish\"], "
                + "button:has-text(\"Continue\"), button:has-text(\"Finish\"), #siteimport-finish-button"
        ).first();
        assertThat(button).isVisible();
        button.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();
    }
}
