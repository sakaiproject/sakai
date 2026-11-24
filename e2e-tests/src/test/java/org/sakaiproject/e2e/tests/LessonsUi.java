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

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.FilePayload;
import com.microsoft.playwright.options.LoadState;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
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

    /**
     * An account outside the course roster is never rewritten by the provider refresh, which
     * briefly removes promoted members and can leave them with a cached permission denial.
     * The account is removed again if it cannot join the site.
     */
    SakaiHelper.TestUser newStudent(String siteUrl, String prefix) {
        SakaiHelper.TestUser student = sakai.createUser(prefix);
        try {
            sakai.addMember(siteUrl, student.username(), "Student");
        } catch (RuntimeException e) {
            try {
                student.close();
            } catch (RuntimeException closeFailure) {
                e.addSuppressed(closeFailure);
            }
            throw e;
        }
        return student;
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
        saveTextFrom(openAddContent(), html);
    }

    /** Adds text through an item or column button, which decides where the new item goes. */
    void addTextFrom(Locator addButton, String html) {
        saveTextFrom(openAddContentFrom(addButton), html);
    }

    private void saveTextFrom(Locator menu, String html) {
        menu.locator(".add-text-link").first().click();
        sakai.typeCkEditor(TEXT_EDITOR_ID, html);
        page.locator("input[type=submit].active").first().click();
        assertThat(page.locator("#addcontent")).isAttached();
    }

    private Locator openAddContentFrom(Locator addButton) {
        addButton.click();
        Locator menu = page.locator("#addContentDiv");
        assertThat(menu).isVisible();
        return menu;
    }

    /** The add and delete buttons of an item only appear while it is hovered. */
    Locator addAboveButton(Locator item) {
        item.hover();
        return item.locator(".add-link");
    }

    void deleteItem(Locator item) {
        item.hover();
        item.locator(".del-item-link").click();
        Locator confirm = page.locator("#delete-confirm");
        assertThat(confirm).isVisible();
        confirm.locator("#delete-confirm-delete-button").click();
        assertThat(item).hasCount(0);
    }

    Locator sections() {
        return page.locator(".section");
    }

    Locator sectionHeader(String title) {
        return page.locator(".sectionHeader").filter(new Locator.FilterOptions().setHasText(title));
    }

    Locator columns(Locator section) {
        return section.locator(":scope > .column");
    }

    /** The section title enables the collapsible options, see typeTitle. */
    Locator openSectionLayout(String title) {
        page.locator("button[data-bs-target='#layout-dialog']").click();
        assertThat(page.locator("#layout-dialog")).isVisible();
        Locator form = page.locator("#sectionlayout form");
        typeTitle(form.locator("#layout-section-title"), title);
        return form;
    }

    /**
     * Lessons enables the collapsible options from a keyup handler. Typing key by key sometimes lost the
     * text after the first space in CI, so fill the field, fire keyup and check the whole value.
     */
    void typeTitle(Locator field, String title) {
        field.fill(title);
        field.dispatchEvent("keyup");
        assertThat(field).hasValue(title);
    }

    void submitLayout(Locator form) {
        sakai.submitForm(() -> form.locator("input[type=submit]").click());
    }

    void addSubpageListing(String titlePrefix, int count, String color) {
        page.locator("button[data-bs-target='#layout-dialog']").click();
        Locator dialog = page.locator("#layout-dialog");
        assertThat(dialog).isVisible();
        dialog.locator("button[data-bs-target='#pagelayout']").click();
        Locator form = page.locator("#pagelayout form");
        assertThat(form).isVisible();
        form.locator("#page-dropdown-selection").selectOption("addSubpageList");
        form.locator("#page-option-subpage-title").fill(titlePrefix);
        form.locator("#page-option-subpage-count-selection").selectOption(String.valueOf(count));
        form.locator("#page-color-scheme-selection").selectOption(color);
        submitLayout(form);
    }

    Locator openSubpageDialogFrom(Locator addButton, String title) {
        openAddContentFrom(addButton).locator(".subpage-link").first().click();
        Locator dialog = page.locator("#subpage-dialog");
        assertThat(dialog).isVisible();
        dialog.locator("#subpage-title").fill(title);
        return dialog;
    }

    Locator openColumnSettings(Locator column) {
        column.locator(".columnopen").click();
        Locator dialog = page.locator("#column-dialog");
        assertThat(dialog).isVisible();
        return dialog;
    }

    /** Saving posts the settings and then reloads the page from script. */
    void saveColumnSettings(Locator dialog) {
        page.waitForRequestFinished(new Page.WaitForRequestFinishedOptions().setPredicate(request ->
            request.isNavigationRequest() && request.frame().equals(page.mainFrame())),
            () -> dialog.locator("#column-submit").click());
        page.waitForLoadState();
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

    /** Picks an item of another tool from the Lessons picker for the given Add Content entry. */
    void linkExisting(String menuEntry, String title) {
        openAddContent().getByText(menuEntry, new Locator.GetByTextOptions().setExact(true)).click();
        page.locator("[role=listitem]").filter(new Locator.FilterOptions().setHasText(title))
            .locator("input[type=radio]").check();
        page.locator("input[type=submit].bold").click();
        assertThat(item(title)).isVisible();
    }

    /** Embeds the listing of a Resources folder of the site. */
    Locator addResourcesFolder(String folder) {
        openAddContent().locator("a.add-folder").click();
        page.locator("#workspace li.directory > a").filter(new Locator.FilterOptions().setHasText(folder)).click();
        page.waitForFunction("folder => document.querySelector('#folder-path').value.endsWith('/' + folder + '/')", folder);
        page.locator("#folder-picker input[type=submit].bold").click();
        Locator item = page.locator(".item.resourceFolderType").filter(new Locator.FilterOptions().setHasText(folder));
        assertThat(item).isVisible();
        return item;
    }

    Locator embedCalendar() {
        openAddContent().locator("a.calendar-link").click();
        Locator item = page.locator(".item.calendar");
        assertThat(item).isVisible();
        return item;
    }

    Locator embedAnnouncements(String count) {
        openAddContent().getByText("Embed Announcements", new Locator.GetByTextOptions().setExact(true)).click();
        Locator dialog = page.locator("#add-announcements-dialog");
        assertThat(dialog).isVisible();
        dialog.locator("#announcementsNumberDropdown-selection").selectOption(count);
        dialog.locator("#announcements-add-item").click();
        Locator item = page.locator(".item.announcementsType");
        assertThat(item).isVisible();
        return item;
    }

    Locator embedForumConversations(String count) {
        openAddContent().locator("a.forum-summary-link").click();
        Locator dialog = page.locator("#add-forum-summary-dialog");
        assertThat(dialog).isVisible();
        dialog.locator("#forumNumberDropdown-selection").selectOption(count);
        dialog.locator("#forum-summary-add-item").click();
        Locator item = page.locator(".item.forumSummary");
        assertThat(item).isVisible();
        return item;
    }

    /** Uploads a ZIP file that Lessons expands into a folder and links through its index.html. */
    void uploadZip(String linkTitle, String fileName, byte[] zip) {
        openAddContent().locator("a.add-website").click();
        Locator dialog = page.locator("#add-multimedia-dialog");
        assertThat(dialog).isVisible();
        dialog.locator("#new-file-section-collapse").click();
        dialog.locator("#mm-file").setInputFiles(new FilePayload(fileName, "application/zip", zip));
        dialog.locator(".mm-file-input-names").fill(linkTitle);
        dialog.locator("#mm-add-item").click();
        assertThat(item(linkTitle)).isVisible();
    }

    Download exportCommonCartridge() {
        clickMoreTools("Export CC");
        Locator dialog = page.locator("#export-cc-dialog");
        assertThat(dialog).isVisible();
        dialog.locator("#export-cc-v13").check();
        return page.waitForDownload(() -> dialog.locator("#export-cc-submit").click());
    }

    void importCommonCartridge(Path cartridge) {
        clickMoreTools("Import CC/CP");
        Locator dialog = page.locator("#import-cc-dialog");
        assertThat(dialog).isVisible();
        dialog.locator("#cc-file").setInputFiles(cartridge);
        page.waitForNavigation(() -> dialog.locator("#import-cc-submit").click());
    }

    Locator openPageSettings() {
        page.locator("#edit-title").click();
        Locator dialog = page.locator("#edit-title-dialog");
        assertThat(dialog).isVisible();
        return dialog;
    }

    /** Saving a top-level page goes through ReloadPage, whose script starts a second navigation. */
    void savePageSettings(Locator dialog) {
        page.waitForResponse(response -> response.request().isNavigationRequest()
            && response.frame().equals(page.mainFrame()) && response.status() < 300
            && "GET".equals(response.request().method()) && !response.url().contains("ReloadPage"),
            () -> dialog.locator("input[type=submit]").click());
        page.waitForURL(url -> !url.contains("ReloadPage"));
        assertThat(dialog).isHidden();
    }

    /** Opens the text editor of an item and shows it only to the given group. */
    void restrictTextToGroup(Locator item, String groupTitle) {
        item.hover();
        item.locator("a.itemLink").click();
        page.locator("#editgroups a").click();
        page.locator("#grouplist div").filter(new Locator.FilterOptions().setHasText(groupTitle))
            .locator("input[type=checkbox]").check();
        page.locator("input[type=submit].active").first().click();
        assertThat(page.locator("#addcontent")).isAttached();
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
