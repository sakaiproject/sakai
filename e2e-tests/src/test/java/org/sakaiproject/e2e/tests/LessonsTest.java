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
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LessonsTest extends SakaiUiTestBase {

    private static String sakaiUrl;

    @Test
    @Order(1)
    void canCreateNewCourse() {
        sakai.login("instructor1");
        sakaiUrl = sakai.createCourse("instructor1", List.of(
            "sakai\\.rubrics",
            "sakai\\.assignment\\.grades",
            "sakai\\.gradebookng",
            "sakai\\.lessonbuildertool"
        ));
    }

    @Test
    @Order(2)
    void createLessonChecklistItem() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Lessons");

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Content")).first().click(new Locator.ClickOptions().setForce(true));
        page.locator(".add-checklist-link:visible").first().click(new Locator.ClickOptions().setForce(true));

        Locator nameInput = page.locator("#name:visible").first();
        if (nameInput.count() == 0) {
            assertThat(page.locator("body")).containsText(Pattern.compile("Checklist|Add Checklist", Pattern.CASE_INSENSITIVE));
            return;
        }

        nameInput.fill("Checklist");

        page.locator("#addChecklistItemButton > :nth-child(2)").click(new Locator.ClickOptions().setForce(true));
        page.locator("#checklistItemDiv1 > .checklist-item-name").fill("A");
        page.locator("#addChecklistItemButton").click(new Locator.ClickOptions().setForce(true));
        page.locator("#checklistItemDiv2 > .checklist-item-name").fill("B");
        page.locator("#addChecklistItemButton").click(new Locator.ClickOptions().setForce(true));
        page.locator("#checklistItemDiv3 > .checklist-item-name").fill("C");
        page.locator("#addChecklistItemButton").click(new Locator.ClickOptions().setForce(true));
        page.locator("#checklistItemDiv4 > .checklist-item-name").fill("D");

        page.locator("#save").click(new Locator.ClickOptions().setForce(true));
        assertThat(page.locator("#content")).isVisible();
    }

    @Test
    @Order(3)
    void canPromoteOnlySubpagesToSiteNavigation() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Lessons");

        addSubpage("Promote Me");

        // Returning through the tool menu can retain the current subpage. More Tools
        // belongs to the top-level page, so return through its breadcrumb explicitly.
        page.locator("#subpage-breadcrumb-div").getByRole(AriaRole.LINK,
            new Locator.GetByRoleOptions().setName("Lessons").setExact(true)).click();
        openAddMorePages();
        chooseExistingPageForSiteNavigation();

        Locator lessonsRadio = page.locator("input[type=radio][title^=\"Lessons \"]").first();
        assertThat(lessonsRadio).isDisabled();
        lessonsRadio.evaluate("element => element.removeAttribute('disabled')");
        lessonsRadio.check();
        useSelectedItem();
        assertThat(page.locator("body")).containsText("The page could not be added to site navigation.");
        Locator currentSiteNavigation = page.locator("li.site-list-item.is-current-site .site-page-list a.btn-nav");
        assertThat(currentSiteNavigation.filter(new Locator.FilterOptions()
            .setHasText(Pattern.compile("^\\s*Lessons\\s*$", Pattern.CASE_INSENSITIVE)))).hasCount(1);

        openAddMorePages();
        chooseExistingPageForSiteNavigation();
        Locator subpageRadio = page.locator("input[type=radio][title^=\"Promote Me \"]").first();
        assertThat(subpageRadio).isEnabled();
        subpageRadio.check();
        useSelectedItem();

        sakai.toolClick("Promote Me");
        assertThat(page.locator(".neoPortletTitleWrap")).containsText("Promote Me");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("More Tools")).click();
        Locator moreToolsDialog = page.locator("#moreDiv");
        assertThat(moreToolsDialog).isVisible();
        assertThat(moreToolsDialog.getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName("Add More Pages").setExact(true))).isVisible();
    }

    @Test
    @Order(4)
    void canRestoreTopLevelPageRemovedViaMoreTools() {
        restoreTopLevelPageAfterRemoval("Restore More Tools", this::removeTopLevelPageViaMoreTools);
    }

    @Test
    @Order(5)
    void canRestoreTopLevelPageRemovedViaManageTools() {
        restoreTopLevelPageAfterRemoval("Restore Manage Tools", this::removeTopLevelPageViaManageTools);
    }

    @Test
    @Order(6)
    void canRestoreTopLevelPageRemovedViaToolOrder() {
        restoreTopLevelPageAfterRemoval("Restore Tool Order", this::removeTopLevelPageViaToolOrder);
    }

    @Test
    @Order(7)
    void resizingEmbeddedImagePreservesItsMediaType() throws IOException {
        if (sakaiUrl == null) {
            canCreateNewCourse();
        }
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Lessons");

        // Sakai probes the URL server-side; keep that request and the browser's image
        // request independent of public hosts and development TLS certificates.
        HttpServer imageServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        byte[] png = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR4nGNgKF/1HwADvQIhA4EOVwAAAABJRU5ErkJggg==");
        imageServer.createContext("/image.png", exchange -> {
            exchange.getResponseHeaders().set("Content-Type", "image/png");
            exchange.sendResponseHeaders(200, png.length);
            try (OutputStream response = exchange.getResponseBody()) {
                response.write(png);
            }
        });
        imageServer.start();
        try {
            // The query string prevents extension fallback from masking a lost MIME type.
            String imageUrl = "http://127.0.0.1:" + imageServer.getAddress().getPort()
                + "/image.png?regression=SAK-52825";
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Content")).first().click();
            page.locator("#addContentDiv .add-multimedia").click();
            Locator addDialog = page.locator("#add-multimedia-dialog");
            assertThat(addDialog).isVisible();
            addDialog.locator("#new-url-section-collapse").click();
            addDialog.locator("#mm-url").fill(imageUrl);
            addDialog.locator("#mm-add-item").click();

            Locator embeddedImage = page.locator("img.multimedia");
            assertThat(embeddedImage).hasAttribute("src", imageUrl);
            assertThat(embeddedImage).hasJSProperty("naturalWidth", 1);
            assertThat(embeddedImage).isVisible();
            embeddedImage.hover();
            page.locator(".image-col .multimedia-edit").click();
            Locator editDialog = page.locator("#edit-multimedia-dialog");
            assertThat(editDialog).isVisible();
            editDialog.locator("#width").fill("120");
            editDialog.locator("#height").fill("80");
            editDialog.locator("#alt").fill("Resized logo");
            editDialog.locator("#edit-multimedia-item").click();

            assertThat(embeddedImage).isVisible();
            assertThat(embeddedImage).hasAttribute("width", "120");
            assertThat(embeddedImage).hasAttribute("height", "80");
            assertThat(embeddedImage).hasAttribute("alt", "Resized logo");
            page.reload();
            assertThat(embeddedImage).isVisible();
            assertThat(embeddedImage).hasAttribute("width", "120");
            assertThat(embeddedImage).hasAttribute("height", "80");
            assertThat(embeddedImage).hasAttribute("alt", "Resized logo");
            assertThat(page.locator(".image-col .mm-type")).hasText("image/png");
        } finally {
            imageServer.stop(0);
        }
    }

    private void restoreTopLevelPageAfterRemoval(String title, java.util.function.Consumer<String> removePage) {
        if (sakaiUrl == null) {
            canCreateNewCourse();
        }
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Lessons");

        createTopLevelPage(title);
        assertNavigationLinkCount(title, 1);

        removePage.accept(title);
        assertNavigationLinkCount(title, 0);

        sakai.toolClick("Lessons");
        openAddMorePages();
        chooseExistingPageForSiteNavigation();
        Locator removedRadio = page.locator("#removed-pages input[type=radio][title^=\"" + title + " \"]").first();
        assertThat(removedRadio).isEnabled();
        removedRadio.check();
        useSelectedItem();

        sakai.toolClick(title);
        assertThat(page.locator(".neoPortletTitleWrap")).containsText(title);
        assertNavigationLinkCount(title, 1);
    }

    private void createTopLevelPage(String title) {
        openAddMorePages();
        Locator newPageDialog = page.locator("#new-page-dialog");
        newPageDialog.locator("#newPage").fill(title);
        newPageDialog.getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName("Save").setExact(true)).click();
        page.waitForLoadState();
        sakai.toolClick(title);
        assertThat(page.locator(".neoPortletTitleWrap")).containsText(title);
    }

    private void removeTopLevelPageViaMoreTools(String title) {
        sakai.toolClick(title);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("More Tools")).click();
        Locator moreToolsDialog = page.locator("#moreDiv");
        assertThat(moreToolsDialog).isVisible();
        moreToolsDialog.getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName("Remove Page").setExact(true)).click();
        assertThat(page.locator("#remove-page-dialog")).isVisible();
        page.locator("#remove-page-submit").click();
        page.waitForURL(Pattern.compile(".*/portal/site/.*"));
        page.waitForLoadState();
    }

    private void removeTopLevelPageViaManageTools(String title) {
        sakai.toolClick("Site Info");
        page.locator(".navIntraTool a, .navIntraTool button")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Manage Tools$", Pattern.CASE_INSENSITIVE)))
            .first()
            .click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();

        Locator expandAll = page.locator("#expandAll");
        if (expandAll.count() > 0 && expandAll.isVisible()) {
            expandAll.click(new Locator.ClickOptions().setForce(true));
        }

        Locator checkbox = page.locator("#toolHolder .toolListControl")
            .filter(new Locator.FilterOptions().setHas(page.getByText(title,
                new Page.GetByTextOptions().setExact(true))))
            .locator("input[type=\"checkbox\"][name=\"selectedTools\"]")
            .first();
        if (checkbox.count() == 0) {
            checkbox = page.getByRole(AriaRole.CHECKBOX,
                new Page.GetByRoleOptions().setName(title).setExact(true)).first();
        }
        assertThat(checkbox).isVisible();
        assertThat(checkbox).isChecked();
        checkbox.uncheck(new Locator.UncheckOptions().setForce(true));

        page.locator("#btnContinue").click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();
        clickContinueOrFinish();
        page.waitForLoadState();
        page.navigate(sakaiUrl);
        assertNavigationLinkCount(title, 0);
    }

    private void removeTopLevelPageViaToolOrder(String title) {
        sakai.toolClick("Site Info");
        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile("Tool Order", Pattern.CASE_INSENSITIVE))).first().click();
        page.waitForLoadState();

        Locator app = page.locator("#tool-order-app");
        assertThat(app).isVisible();

        Locator row = page.locator(".tool-order-row")
            .filter(new Locator.FilterOptions().setHas(page.getByText(title,
                new Page.GetByTextOptions().setExact(true))))
            .first();
        if (row.count() == 0) {
            row = page.locator(".tool-order-row")
                .filter(new Locator.FilterOptions().setHasText(title))
                .first();
        }
        assertThat(row).isVisible();
        Locator deleteButton = row.locator("[data-action=\"delete\"]");
        assertThat(deleteButton).isVisible();
        page.onceDialog(dialog -> dialog.accept());
        deleteButton.click();
        assertThat(page.locator("#tool-order-alert"))
            .containsText(Pattern.compile("successfully removed", Pattern.CASE_INSENSITIVE));
        assertThat(page.locator(".tool-order-row")
            .filter(new Locator.FilterOptions().setHasText(title))).hasCount(0);

        page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName(Pattern.compile("Done", Pattern.CASE_INSENSITIVE))).click();
        page.waitForLoadState();
        page.navigate(sakaiUrl);
        assertNavigationLinkCount(title, 0);
    }

    private void assertNavigationLinkCount(String title, int expectedCount) {
        Locator currentSiteNavigation = page.locator(
            "li.site-list-item.is-current-site .site-page-list");
        // Count membership in navigation even when the site's tool list is collapsed.
        assertThat(currentSiteNavigation.getByRole(AriaRole.LINK,
            new Locator.GetByRoleOptions().setName(title).setExact(true).setIncludeHidden(true)))
            .hasCount(expectedCount);
    }

    private void clickContinueOrFinish() {
        Locator button = page.locator(
            "input#revise, input[type=\"submit\"][value*=\"Finish\"], button:has-text(\"Finish\"), "
                + "input[type=\"submit\"][value*=\"Continue\"], button:has-text(\"Continue\")"
        ).first();
        assertThat(button).isVisible();
        button.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();
    }

    private void addSubpage(String title) {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Content")).first().click();

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
    }

    private void openAddMorePages() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("More Tools")).click();
        Locator moreToolsDialog = page.locator("#moreDiv");
        assertThat(moreToolsDialog).isVisible();
        moreToolsDialog.getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName("Add More Pages").setExact(true)).click();
        assertThat(page.locator("#new-page-dialog")).isVisible();
    }

    private void chooseExistingPageForSiteNavigation() {
        page.locator("#new-page-dialog").getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions()
            .setName(Pattern.compile("Put existing page", Pattern.CASE_INSENSITIVE))).click();
    }

    private void useSelectedItem() {
        page.locator(".PagePicker").getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions()
            .setName(Pattern.compile("Use selected item", Pattern.CASE_INSENSITIVE))).click();
    }
}
