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
    private static final String PAGE_TO_DELETE = "Page to delete";

    @Test
    void replaceImportKeepsExistingLessonsPagesAvailableForRecovery() {
        sakai.login("instructor1");
        String sourceSite = sakai.createCourse("instructor1", List.of("sakai\\.lessonbuildertool"));
        String destinationSite = sakai.createCourse("instructor1", List.of(
            "sakai\\.lessonbuildertool",
            "sakai\\.dashboard"
        ));

        page.navigate(sourceSite);
        sakai.toolClick("Lessons");
        addSubpage(IMPORTED_PAGE);

        page.navigate(destinationSite);
        sakai.toolClick("Lessons");
        addSubpage(PRESERVED_PAGE);
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(PRESERVED_PAGE).setExact(true))
            .click();
        page.waitForLoadState();
        addSubpage(PRESERVED_CONTENT);

        page.navigate(destinationSite);
        sakai.toolClick("Lessons");
        addSubpage(PAGE_TO_DELETE);

        replaceLessonsFromSite(destinationSite, sourceSite);

        page.navigate(destinationSite);
        sakai.toolClick("Lessons");
        assertThat(page.locator("body")).containsText(IMPORTED_PAGE);

        page.locator("#show-pages:visible").click(new Locator.ClickOptions().setForce(true));
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Manage pages"))).isVisible();
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Removed pages"))).isVisible();
        assertThat(page.locator("body")).containsText("Their content is still available");

        Locator pageToDeleteRow = removedPageRow(PAGE_TO_DELETE);
        pageToDeleteRow.getByLabel("Select " + PAGE_TO_DELETE + " for permanent deletion").check();

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Delete permanently")).click();
        Locator deleteDialog = page.locator("#delete-pages-dialog");
        assertThat(deleteDialog.getByRole(AriaRole.HEADING,
            new Locator.GetByRoleOptions().setName("Delete pages permanently?"))).isVisible();
        assertThat(deleteDialog).containsText(PAGE_TO_DELETE);
        assertThat(deleteDialog).containsText("This action cannot be undone");
        deleteDialog.locator(".delete-pages-dialog-footer .delete-pages-dialog-close").click();
        assertThat(pageToDeleteRow).isVisible();

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Delete permanently")).click();
        deleteDialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Delete permanently")).click();
        page.waitForLoadState();

        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Manage pages"))).isVisible();
        assertThat(page.locator(".sak-banner-success")).containsText("Deleted one page permanently");
        assertThat(page.locator("#removed-pages")).not().containsText(PAGE_TO_DELETE);

        Locator preservedPageRow = removedPageRow(PRESERVED_PAGE);
        assertThat(preservedPageRow.locator("input.deletebox")).isVisible();
        preservedPageRow.getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName("Add back to Lessons")).click();
        page.waitForLoadState();

        assertThat(page.locator(".sak-banner-success"))
            .containsText("\"" + PRESERVED_PAGE + "\" was added to Lessons as a top-level page");
        sakai.toolClick(PRESERVED_PAGE);
        assertThat(page.locator("body")).containsText(PRESERVED_CONTENT);
    }

    private Locator removedPageRow(String title) {
        return page.locator("#removed-pages > li.removed-page")
            .filter(new Locator.FilterOptions().setHasText(title))
            .first();
    }

    private void addSubpage(String title) {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Content"))
            .click();

        Locator addContentDialog = page.locator("#addContentDiv");
        assertThat(addContentDialog).isVisible();
        addContentDialog.getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName("Add Subpage").setExact(true)).click();

        Locator subpageDialog = page.locator("#subpage-dialog");
        assertThat(subpageDialog).isVisible();
        subpageDialog.locator("#subpage-title").fill(title);
        subpageDialog.getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName("Create").setExact(true)).click();

        assertThat(page.locator("#subpage-breadcrumb-div")).containsText(title);
        page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Back").setExact(true)).first().click();

        assertThat(page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(title).setExact(true))).isVisible();
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
