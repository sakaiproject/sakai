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
import static org.junit.jupiter.api.Assertions.fail;

import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.assertions.LocatorAssertions;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AnnouncementTest extends SakaiUiTestBase {

    private static String sakaiUrl;
    private static final String FUTURE_ANNOUNCEMENT_TITLE = "Future Announcement";
    private static final String PAST_ANNOUNCEMENT_TITLE = "Past Announcement";
    private static final String CURRENT_ANNOUNCEMENT_TITLE = "Current Announcement";

    @Test
    @Order(1)
    void canCreateNewCourse() {
        sakai.login("instructor1");
        sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.announcements", "sakai\\.schedule"));
    }

    @Test
    @Order(2)
    void canCreateFutureAnnouncement() {
        sakai.login("instructor1");
        sakai.gotoPath(sakaiUrl);
        sakai.toolClick("Announcements");
        ensureViewAll();

        page.locator(".navIntraTool a").filter(new Locator.FilterOptions().setHasText("Add")).first().click(new Locator.ClickOptions().setForce(true));
        page.locator("#subject").fill(FUTURE_ANNOUNCEMENT_TITLE);
        fillAnnouncementBody("<p>This is a future announcement that should only be visible after the specified date.</p>");

        page.locator("#hidden_specify").click(new Locator.ClickOptions().setForce(true));
        page.locator("#use_start_date").click(new Locator.ClickOptions().setForce(true));
        sakai.selectDate("#opendate", "06/01/2035 08:30 am");
        page.locator("#use_end_date").click(new Locator.ClickOptions().setForce(true));
        sakai.selectDate("#closedate", "06/03/2035 08:30 am");

        submitAnnouncementForm();
        assertThat(announcementRows().filter(new Locator.FilterOptions().setHasText(FUTURE_ANNOUNCEMENT_TITLE))).hasCount(1,
            new LocatorAssertions.HasCountOptions().setTimeout(20_000));
    }

    @Test
    @Order(3)
    void canCreatePastAnnouncement() {
        sakai.login("instructor1");
        sakai.gotoPath(sakaiUrl);
        sakai.toolClick("Announcements");
        ensureViewAll();

        page.locator(".navIntraTool a").filter(new Locator.FilterOptions().setHasText("Add")).first().click(new Locator.ClickOptions().setForce(true));
        page.locator("#subject").fill(PAST_ANNOUNCEMENT_TITLE);
        fillAnnouncementBody("<p>This is a past announcement that should not be visible anymore.</p>");

        page.locator("#hidden_specify").click(new Locator.ClickOptions().setForce(true));
        page.locator("#use_start_date").click(new Locator.ClickOptions().setForce(true));
        sakai.selectDate("#opendate", "01/01/2020 08:30 am");
        page.locator("#use_end_date").click(new Locator.ClickOptions().setForce(true));
        sakai.selectDate("#closedate", "01/03/2020 08:30 am");

        submitAnnouncementForm();
        assertThat(announcementRows().filter(new Locator.FilterOptions().setHasText(FUTURE_ANNOUNCEMENT_TITLE))).hasCount(1,
            new LocatorAssertions.HasCountOptions().setTimeout(20_000));
        assertThat(announcementRows().filter(new Locator.FilterOptions().setHasText(PAST_ANNOUNCEMENT_TITLE))).hasCount(1,
            new LocatorAssertions.HasCountOptions().setTimeout(20_000));
    }

    @Test
    @Order(4)
    void canCreateCurrentAnnouncement() {
        sakai.login("instructor1");
        sakai.gotoPath(sakaiUrl);
        sakai.toolClick("Announcements");
        ensureViewAll();

        page.locator(".navIntraTool a").filter(new Locator.FilterOptions().setHasText("Add")).first().click(new Locator.ClickOptions().setForce(true));
        page.locator("#subject").fill(CURRENT_ANNOUNCEMENT_TITLE);
        fillAnnouncementBody("<p>This is a current announcement that should be visible to everyone.</p>");

        submitAnnouncementForm();
        assertThat(announcementRows().filter(new Locator.FilterOptions().setHasText(FUTURE_ANNOUNCEMENT_TITLE))).hasCount(1,
            new LocatorAssertions.HasCountOptions().setTimeout(20_000));
        assertThat(announcementRows().filter(new Locator.FilterOptions().setHasText(PAST_ANNOUNCEMENT_TITLE))).hasCount(1,
            new LocatorAssertions.HasCountOptions().setTimeout(20_000));
        assertThat(announcementRows().filter(new Locator.FilterOptions().setHasText(CURRENT_ANNOUNCEMENT_TITLE))).hasCount(1,
            new LocatorAssertions.HasCountOptions().setTimeout(20_000));
    }

    @Test
    @Order(5)
    void studentCanOnlySeeCurrentAnnouncement() {
        sakai.login("student0011");
        sakai.gotoPath(sakaiUrl);

        sakai.toolClick("Announcements");
        ensureViewAll();
        Locator announcementRows = announcementRows();
        assertThat(announcementRows.filter(new Locator.FilterOptions().setHasText(CURRENT_ANNOUNCEMENT_TITLE))).hasCount(1,
            new LocatorAssertions.HasCountOptions().setTimeout(20_000));
        assertThat(announcementRows.filter(new Locator.FilterOptions().setHasText(FUTURE_ANNOUNCEMENT_TITLE))).hasCount(0,
            new LocatorAssertions.HasCountOptions().setTimeout(20_000));
        assertThat(announcementRows.filter(new Locator.FilterOptions().setHasText(PAST_ANNOUNCEMENT_TITLE))).hasCount(0,
            new LocatorAssertions.HasCountOptions().setTimeout(20_000));
    }

    @Test
    @Order(6)
    void reorderSortsAnnouncementsBeyondTheFirstPage() {
        String titlePrefix = "A SAK-52721 Reorder " + System.currentTimeMillis() + " ";

        sakai.login("instructor1");
        sakai.gotoPath(sakaiUrl);
        sakai.toolClick("Announcements");
        ensureViewAll();

        for (int i = 1; i <= 20; i++) {
            page.locator(".navIntraTool a").filter(new Locator.FilterOptions().setHasText("Add")).first().click(new Locator.ClickOptions().setForce(true));
            page.locator("#subject").fill(titlePrefix + String.format("%02d", i));
            fillAnnouncementBody("<p>SAK-52721 reorder regression announcement.</p>");
            submitAnnouncementForm();
        }

        ensureViewAll();
        Locator pageSizeSelect = page.locator("select[id^=\"selectPageSize\"]").first();
        assertThat(pageSizeSelect).isVisible();
        pageSizeSelect.selectOption("10");
        page.waitForLoadState();

        Locator reorderLink = page.locator(".navIntraTool a, .navIntraTool button, .navIntraTool [role=\"button\"]")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Reorder$", Pattern.CASE_INSENSITIVE))).first();
        assertThat(reorderLink).isVisible();
        reorderLink.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();

        Locator reorderedAnnouncements = page.locator("#reorder-list li").filter(new Locator.FilterOptions().setHasText(titlePrefix));
        assertThat(reorderedAnnouncements).hasCount(20);
        dragAnnouncementBefore(
            reorderedAnnouncements.filter(new Locator.FilterOptions().setHasText(titlePrefix + "02")).locator(".grabHandle"),
            reorderedAnnouncements.filter(new Locator.FilterOptions().setHasText(titlePrefix + "01")).first());
        assertAnnouncementPrecedes("#reorder-list", titlePrefix + "02", titlePrefix + "01");

        page.locator("#reorder-list-sortingToolBar a.title").click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();
        page.locator("input[name=\"eventSubmit_doReorderUpdate\"]").click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();

        assertAnnouncementOrder(titlePrefix, 1, 10);

        page.locator("input[name=\"eventSubmit_doList_next\"]").first().click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();
        assertAnnouncementOrder(titlePrefix, 11, 20);
    }

    private void assertAnnouncementOrder(String titlePrefix, int first, int last) {
        String pageText = page.locator("#announcements-list").innerText();
        int previousPosition = -1;
        for (int i = first; i <= last; i++) {
            String title = titlePrefix + String.format("%02d", i);
            int titlePosition = pageText.indexOf(title);
            if (titlePosition <= previousPosition) {
                fail("Expected announcement " + title + " to be ordered after the preceding announcement.");
            }
            previousPosition = titlePosition;
        }
    }

    private void dragAnnouncementBefore(Locator source, Locator target) {
        BoundingBox sourceBox = source.boundingBox();
        BoundingBox targetBox = target.boundingBox();
        if (sourceBox == null || targetBox == null) {
            fail("Expected announcement reorder items to have bounding boxes.");
            return;
        }

        double sourceX = sourceBox.x + sourceBox.width / 2;
        double sourceY = sourceBox.y + sourceBox.height / 2;
        double targetY = targetBox.y + targetBox.height / 4;

        page.mouse().move(sourceX, sourceY);
        page.mouse().down();
        for (int step = 1; step <= 10; step++) {
            page.mouse().move(sourceX, sourceY + (targetY - sourceY) * step / 10);
        }
        page.mouse().up();
    }

    private void assertAnnouncementPrecedes(String selector, String firstTitle, String secondTitle) {
        String listText = page.locator(selector).innerText();
        int firstTitlePosition = listText.indexOf(firstTitle);
        int secondTitlePosition = listText.indexOf(secondTitle);
        if (firstTitlePosition < 0 || secondTitlePosition < 0) {
            fail("Expected reorder list to contain " + firstTitle + " and " + secondTitle + ".");
        }
        if (firstTitlePosition >= secondTitlePosition) {
            fail("Expected announcement " + firstTitle + " to precede " + secondTitle + ".");
        }
    }

    private void fillAnnouncementBody(String html) {
        String plainText = html.replaceAll("<[^>]+>", "").trim();

        if (sakai.typeCkEditorIfPresent("body", html)) {
            return;
        }

        Locator fallback = page.locator("textarea#body, textarea:visible, [contenteditable=\"true\"]:visible").first();
        assertThat(fallback).isVisible();
        fallback.fill(plainText);
        fallback.dispatchEvent("input");
        fallback.dispatchEvent("change");
        fallback.dispatchEvent("blur");

        assertBodyPopulated();
    }

    private Locator announcementRows() {
        return page.locator("table tr");
    }

    private void submitAnnouncementForm() {
        page.evaluate("() => {"
            + "const editor = window.CKEDITOR && window.CKEDITOR.instances && window.CKEDITOR.instances.body;"
            + "const textarea = document.querySelector('#body');"
            + "if (editor) {"
            + "const editorText = editor.getData().replace(/<[^>]+>/g, ' ').replace(/\\s+/g, ' ').trim();"
            + "if (!editorText && textarea && textarea.value.trim()) { editor.setData(textarea.value); }"
            + "editor.updateElement();"
            + "}"
            + "}");
        assertBodyPopulated();

        Locator submit = page.locator(
            "#saveChanges:visible, .act #saveChanges:visible, input[name=\"post\"]#saveChanges:visible, .act input[name=\"post\"]:visible"
        ).first();
        assertThat(submit).isVisible();
        submit.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();
        assertThat(page.locator("body")).containsText(Pattern.compile("Announcements", Pattern.CASE_INSENSITIVE));
    }

    private void ensureViewAll() {
        Locator viewSelect = page.locator("#viewFilter_viewFilterForm, select[name=\"view\"]").first();
        if (viewSelect.count() == 0 || !viewSelect.isVisible()) {
            return;
        }

        try {
            if (!"view.all".equals(viewSelect.inputValue())) {
                viewSelect.selectOption("view.all");
                page.waitForLoadState();
            }
        } catch (PlaywrightException ignored) {
            // Not all Announcements contexts expose the same view filter controls.
        }
    }

    private void assertBodyPopulated() {
        Boolean hasBody = (Boolean) page.evaluate("() => {"
            + "const editor = window.CKEDITOR && window.CKEDITOR.instances && window.CKEDITOR.instances.body;"
            + "if (editor) {"
            + "const text = editor.getData().replace(/<[^>]+>/g, ' ').replace(/\\s+/g, ' ').trim();"
            + "if (text.length > 0) { return true; }"
            + "}"
            + "const textarea = document.querySelector('#body');"
            + "if (!textarea) { return false; }"
            + "return Boolean(textarea.value && textarea.value.trim().length > 0);"
            + "}");

        if (!Boolean.TRUE.equals(hasBody)) {
            throw new IllegalStateException("Announcement body is empty before submit");
        }
    }
}
