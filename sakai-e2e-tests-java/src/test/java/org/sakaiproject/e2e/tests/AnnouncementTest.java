package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

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

    private void fillAnnouncementBody(String html) {
        String plainText = html.replaceAll("<[^>]+>", "").trim();

        if (tryFillBodyWithCkEditor(html)) {
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
            + "if (editor) { editor.updateElement(); }"
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

    private boolean tryFillBodyWithCkEditor(String html) {
        try {
            page.waitForFunction(
                "() => Boolean(window.CKEDITOR && window.CKEDITOR.instances && window.CKEDITOR.instances.body && window.CKEDITOR.instances.body.status === 'ready')"
            );
            sakai.typeCkEditor("body", html);
            page.evaluate("() => {"
                + "const editor = window.CKEDITOR && window.CKEDITOR.instances && window.CKEDITOR.instances.body;"
                + "if (editor) { editor.updateElement(); }"
                + "}");
            assertBodyPopulated();
            return true;
        } catch (PlaywrightException ignored) {
            return false;
        }
    }

    private void assertBodyPopulated() {
        Boolean hasBody = (Boolean) page.evaluate("() => {"
            + "const editor = window.CKEDITOR && window.CKEDITOR.instances && window.CKEDITOR.instances.body;"
            + "if (editor) {"
            + "const text = editor.getData().replace(/<[^>]+>/g, ' ').replace(/\\s+/g, ' ').trim();"
            + "if (text.length > 0) { return true; }"
            + "editor.updateElement();"
            + "}"
            + "const textarea = document.querySelector('#body');"
            + "if (!textarea) { return false; }"
            + "return textarea.value && textarea.value.trim().length > 0;"
            + "}");

        if (!Boolean.TRUE.equals(hasBody)) {
            throw new IllegalStateException("Announcement body is empty before submit");
        }
    }
}
