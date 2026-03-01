package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
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
        page.navigate(sakaiUrl);
        sakai.toolClick("Announcements");

        page.locator(".navIntraTool a").filter(new Locator.FilterOptions().setHasText("Add")).first().click(new Locator.ClickOptions().setForce(true));
        page.locator("#subject").fill(FUTURE_ANNOUNCEMENT_TITLE);
        fillAnnouncementBody("<p>This is a future announcement that should only be visible after the specified date.</p>");

        page.locator("#hidden_specify").click(new Locator.ClickOptions().setForce(true));
        page.locator("#use_start_date").click(new Locator.ClickOptions().setForce(true));
        sakai.selectDate("#opendate", "06/01/2035 08:30 am");
        page.locator("#use_end_date").click(new Locator.ClickOptions().setForce(true));
        sakai.selectDate("#closedate", "06/03/2035 08:30 am");

        page.locator(".act input.active").first().click(new Locator.ClickOptions().setForce(true));
        assertThat(page.locator("table tr.inactive")).hasCount(1);
    }

    @Test
    @Order(3)
    void canCreatePastAnnouncement() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Announcements");

        page.locator(".navIntraTool a").filter(new Locator.FilterOptions().setHasText("Add")).first().click(new Locator.ClickOptions().setForce(true));
        page.locator("#subject").fill(PAST_ANNOUNCEMENT_TITLE);
        fillAnnouncementBody("<p>This is a past announcement that should not be visible anymore.</p>");

        page.locator("#hidden_specify").click(new Locator.ClickOptions().setForce(true));
        page.locator("#use_start_date").click(new Locator.ClickOptions().setForce(true));
        sakai.selectDate("#opendate", "01/01/2020 08:30 am");
        page.locator("#use_end_date").click(new Locator.ClickOptions().setForce(true));
        sakai.selectDate("#closedate", "01/03/2020 08:30 am");

        page.locator(".act input.active").first().click(new Locator.ClickOptions().setForce(true));
        assertThat(page.locator("table tr.inactive")).hasCount(2);
    }

    @Test
    @Order(4)
    void canCreateCurrentAnnouncement() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Announcements");

        page.locator(".navIntraTool a").filter(new Locator.FilterOptions().setHasText("Add")).first().click(new Locator.ClickOptions().setForce(true));
        page.locator("#subject").fill(CURRENT_ANNOUNCEMENT_TITLE);
        fillAnnouncementBody("<p>This is a current announcement that should be visible to everyone.</p>");

        page.locator(".act input.active").first().click(new Locator.ClickOptions().setForce(true));

        assertThat(page.locator("table tr.inactive")).hasCount(2);
        int activeCount = page.locator("table tr:not(.inactive)").count();
        assertTrue(activeCount > 0);
    }

    @Test
    @Order(5)
    void studentCanOnlySeeCurrentAnnouncement() {
        sakai.login("student0011");
        page.navigate(sakaiUrl);

        sakai.toolClick("Announcements");
        Locator announcementRows = page.locator("table tr");
        assertThat(announcementRows.filter(new Locator.FilterOptions().setHasText(CURRENT_ANNOUNCEMENT_TITLE))).hasCount(1);
        assertThat(announcementRows.filter(new Locator.FilterOptions().setHasText(FUTURE_ANNOUNCEMENT_TITLE))).hasCount(0);
        assertThat(announcementRows.filter(new Locator.FilterOptions().setHasText(PAST_ANNOUNCEMENT_TITLE))).hasCount(0);
    }

    private void fillAnnouncementBody(String html) {
        boolean hasCkEditor = Boolean.TRUE.equals(page.evaluate("() => Boolean(window.CKEDITOR && window.CKEDITOR.instances && window.CKEDITOR.instances.body)"));
        if (hasCkEditor) {
            sakai.typeCkEditor("body", html);
            return;
        }

        Locator fallback = page.locator("textarea#body, textarea:visible, [contenteditable=\"true\"]:visible").first();
        if (fallback.count() > 0) {
            fallback.fill(html.replaceAll("<[^>]+>", ""));
        }
    }
}
