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
import com.microsoft.playwright.options.FilePayload;
import com.microsoft.playwright.options.LoadState;
import java.nio.charset.StandardCharsets;
import org.sakaiproject.e2e.support.SakaiHelper;

/** Shared steps for building and reading Lessons pages. */
final class LessonsUi {

    private static final String TEXT_EDITOR_ID = "page::input";

    private final Page page;
    private final SakaiHelper sakai;

    LessonsUi(Page page, SakaiHelper sakai) {
        this.page = page;
        this.sakai = sakai;
    }

    void open(String siteUrl) {
        page.navigate(siteUrl);
        sakai.toolClick("Lessons");
        assertThat(page.locator("#lessonsCurrentPageId")).isAttached();
    }

    Locator items() {
        return page.locator(".portletBody .item");
    }

    Locator item(String text) {
        return items().filter(new Locator.FilterOptions().setHasText(text));
    }

    Locator navigationLink(String title) {
        return page.locator("li.site-list-item.is-current-site .site-page-list a.btn-nav")
            .filter(new Locator.FilterOptions().setHasText(title));
    }

    /** Adds a top-level page and leaves the browser on it. */
    void createTopLevelPage(String title) {
        clickMoreTools("Add More Pages");
        Locator dialog = page.locator("#new-page-dialog");
        assertThat(dialog).isVisible();
        dialog.locator("#newPage").fill(title);
        dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Save").setExact(true)).click();
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        assertThat(navigationLink(title)).hasCount(1);
        sakai.toolClick(title);
        assertThat(page.locator(".neoPortletTitleWrap")).containsText(title);
    }

    private Locator openAddContent() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Content")).first().click();
        Locator menu = page.locator("#addContentDiv");
        assertThat(menu).isVisible();
        return menu;
    }

    void addText(String html) {
        openAddContent().locator(".add-text-link").first().click();
        sakai.typeCkEditor(TEXT_EDITOR_ID, html);
        page.locator("input[type=submit].active").first().click();
        assertThat(page.locator("#addcontent")).isAttached();
    }

    private Locator openContentLinks() {
        openAddContent().locator("a.add-resource").first().click();
        Locator dialog = page.locator("#add-multimedia-dialog");
        assertThat(dialog).isVisible();
        return dialog;
    }

    void addUrl(String name, String url) {
        Locator dialog = openContentLinks();
        dialog.locator("#new-url-section-collapse").click();
        assertThat(dialog.locator("#mm-url")).isVisible();
        dialog.locator("#mm-url").fill(url);
        dialog.locator("#mm-name").fill(name);
        dialog.locator("#mm-add-item").click();
        assertThat(item(name)).isVisible();
    }

    void uploadTextFile(String linkTitle, String fileName, String content) {
        Locator dialog = openContentLinks();
        dialog.locator("#new-file-section-collapse").click();
        dialog.locator("#mm-file").setInputFiles(
            new FilePayload(fileName, "text/plain", content.getBytes(StandardCharsets.UTF_8)));
        dialog.locator(".mm-file-input-names").fill(linkTitle);
        dialog.locator("#mm-add-item").click();
        assertThat(item(linkTitle)).isVisible();
    }

    /** The first save previews the embed code and the second one stores it. */
    void addEmbed(String embedCode) {
        openAddContent().locator("a.add-multimedia").first().click();
        Locator dialog = page.locator("#add-multimedia-dialog");
        assertThat(dialog).isVisible();
        dialog.locator("#new-url-section-collapse").click();
        assertThat(dialog.locator("#mm-url")).isVisible();
        dialog.locator("#mm-url").fill(embedCode);
        dialog.locator("#mm-add-item").click();
        assertThat(dialog.locator("#mm-test-embed-results")).isVisible();
        dialog.locator("#mm-add-item").click();
        assertThat(dialog).isHidden();
    }

    Locator openSubpageDialog(String title) {
        openAddContent().locator(".subpage-link").first().click();
        Locator dialog = page.locator("#subpage-dialog");
        assertThat(dialog).isVisible();
        dialog.locator("#subpage-title").fill(title);
        return dialog;
    }

    void submitSubpage(Locator dialog) {
        dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Create").setExact(true)).click();
    }

    void addSubpage(String title) {
        submitSubpage(openSubpageDialog(title));
        assertThat(page.locator("#subpage-breadcrumb-div")).containsText(title);
    }

    void openReorder() {
        page.locator("#reorder-button a").click();
        assertThat(page.locator("#reorderCol1")).isVisible();
        page.waitForFunction("document.getElementById('order')?.value.includes('---')");
    }

    Locator reorderItem(String text) {
        return page.locator("#reorderCol1 .reorder-item").filter(new Locator.FilterOptions().setHasText(text));
    }

    void saveReorder() {
        page.locator("#save").click();
        assertThat(page.locator("#addcontent")).isAttached();
    }

    /** Opens the page picker and selects the source page, which leads to the reorder step. */
    void startCopyFrom(String sourcePageTitle) {
        openAddContent().getByText("Add items from another page").click();
        page.locator("input[type=radio][title='" + sourcePageTitle + " Select']").check();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Use selected item").setExact(true)).click();
        assertThat(page.locator("#reorderCol1")).isVisible();
    }

    void rename(String newTitle) {
        page.locator("#edit-title").click();
        Locator dialog = page.locator("#edit-title-dialog");
        assertThat(dialog).isVisible();
        dialog.locator("#pageTitle").fill(newTitle);
        dialog.locator("input[type=submit].bold").click();
        assertThat(page.locator(".neoPortletTitleWrap")).containsText(newTitle);
    }

    void addChecklist(String name, String... entries) {
        openAddContent().locator(".add-checklist-link").first().click();
        page.locator("#name").fill(name);
        for (int i = 0; i < entries.length; i++) {
            page.locator("#addChecklistItemButton").click();
            page.locator("#checklistItemDiv" + (i + 1) + " > .checklist-item-name").fill(entries[i]);
        }
        page.locator("#save").click();
        assertThat(item(name)).isVisible();
    }

    /** Ticks a checklist box and waits until the server has stored it. */
    void tickChecklistEntry(Locator checkbox) {
        String saveUrl = (String) page.locator("#saveChecklistForm-checklistId").evaluate("input => input.form.action");
        page.waitForResponse(
            response -> "POST".equals(response.request().method()) && response.url().startsWith(saveUrl),
            checkbox::check);
    }

    void openPermissions() {
        clickMoreTools("Permissions");
        assertThat(studentCanUpdate()).isAttached();
    }

    private void clickMoreTools(String entry) {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("More Tools")).click();
        Locator moreTools = page.locator("#moreDiv");
        assertThat(moreTools).isVisible();
        moreTools.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(entry).setExact(true)).click();
    }

    Locator studentCanUpdate() {
        return page.locator("[id='Studentlessonbuilder.upd']");
    }

    void savePermissions() {
        page.locator("input[name=eventSubmit_doSave]").click();
        assertThat(page.locator("#addcontent")).isAttached();
    }
}
