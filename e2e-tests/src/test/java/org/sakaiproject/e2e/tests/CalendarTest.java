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
import com.microsoft.playwright.assertions.LocatorAssertions;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CalendarTest extends SakaiUiTestBase {

    private static String sakaiUrl;
    private static final String EVENT_DATE = java.time.LocalDate.now().plusMonths(1).toString();
    private static final String EVENT_TITLE = "Playwright Calendar Event " + System.currentTimeMillis();
    private static final String UPDATED_EVENT_TITLE = "Playwright Calendar Event Revised " + System.currentTimeMillis();

    @Test
    @Order(1)
    void canCreateNewCourse() {
        sakai.login("instructor1");
        sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.schedule"));
    }

    @Test
    @Order(2)
    void canCreateEvent() {
        sakai.login("instructor1");
        sakai.gotoPath(sakaiUrl);
        sakai.toolClick("Calendar");

        clickAddEvent();
        page.locator("#activitytitle").fill(EVENT_TITLE);
        sakai.selectDate("#newCalendarDate", EVENT_DATE);
        setEventStartTime("9", "9", "0", "am");
        page.locator("#duHour").selectOption("1");
        page.locator("#duMinute").selectOption("0");
        fillEventDescription("<p>Created by the Playwright Java Calendar test.</p>");

        submitEventForm("eventSubmit_doAdd");

        switchToListView();
        assertThat(eventRows().filter(new Locator.FilterOptions().setHasText(EVENT_TITLE))).hasCount(1,
            new LocatorAssertions.HasCountOptions().setTimeout(20_000));
    }

    @Test
    @Order(3)
    void canEditEvent() {
        sakai.login("instructor1");
        sakai.gotoPath(sakaiUrl);
        sakai.toolClick("Calendar");

        openEvent(EVENT_TITLE);
        page.locator("input[name=\"eventSubmit_doRevise\"]").click(new Locator.ClickOptions().setForce(true));

        page.locator("#activitytitle").fill(UPDATED_EVENT_TITLE);
        submitEventForm("eventSubmit_doUpdate");

        switchToListView();
        assertThat(eventRows().filter(new Locator.FilterOptions().setHasText(UPDATED_EVENT_TITLE))).hasCount(1,
            new LocatorAssertions.HasCountOptions().setTimeout(20_000));
    }

    @Test
    @Order(4)
    void canDeleteEvent() {
        sakai.login("instructor1");
        sakai.gotoPath(sakaiUrl);
        sakai.toolClick("Calendar");

        openEvent(UPDATED_EVENT_TITLE);
        page.locator("input[name=\"eventSubmit_doDelete\"]").click(new Locator.ClickOptions().setForce(true));
        page.locator("input[name=\"eventSubmit_doConfirm\"]").click(new Locator.ClickOptions().setForce(true));

        switchToListView();
        assertThat(eventRows().filter(new Locator.FilterOptions().setHasText(UPDATED_EVENT_TITLE))).hasCount(0,
            new LocatorAssertions.HasCountOptions().setTimeout(20_000));
    }

    @Test
    @Order(5)
    void studentOutsideGroupCannotSeeGroupRestrictedEvent() {
        String groupTitle = "Playwright Calendar Group " + System.currentTimeMillis();
        String eventTitle = "Playwright Group Event " + System.currentTimeMillis();

        sakai.login("instructor1");
        sakai.gotoPath(sakaiUrl);
        createGroupWithMember(groupTitle, "student0011", "student0012");

        sakai.gotoPath(sakaiUrl);
        sakai.toolClick("Calendar");
        clickAddEvent();
        page.locator("#activitytitle").fill(eventTitle);
        sakai.selectDate("#newCalendarDate", EVENT_DATE);
        setEventStartTime("10", "10", "0", "am");
        page.locator("#duHour").selectOption("1");
        page.locator("#duMinute").selectOption("0");
        fillEventDescription("<p>Group-restricted event created by the Playwright Java Calendar test.</p>");
        restrictEventToGroup(groupTitle);
        submitEventForm("eventSubmit_doAdd");

        sakai.login("student0011");
        sakai.gotoPath(sakaiUrl);
        sakai.toolClick("Calendar");
        switchToListView();
        assertThat(eventRows().filter(new Locator.FilterOptions().setHasText(eventTitle))).hasCount(1,
            new LocatorAssertions.HasCountOptions().setTimeout(20_000));

        sakai.login("student0012");
        sakai.gotoPath(sakaiUrl);
        sakai.toolClick("Calendar");
        switchToListView();
        assertThat(eventRows().filter(new Locator.FilterOptions().setHasText(eventTitle))).hasCount(0,
            new LocatorAssertions.HasCountOptions().setTimeout(20_000));
    }

    private void ensureSiteParticipant(String eid) {
        page.locator(".navIntraTool a").filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Add Participants$", Pattern.CASE_INSENSITIVE)))
            .first().click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();

        page.locator("#officialAccountParticipant").fill(eid);
        page.locator("#participant-helper form").first().locator("button[type=\"submit\"]").first().click();
        page.waitForLoadState();

        // Nothing to add if eid is already a site member: the wizard re-shows step 1 with a
        // warning banner instead of advancing to role/email selection.
        Locator alreadyMember = page.locator("#participant-helper").filter(new Locator.FilterOptions().setHasText("already members"));
        if (alreadyMember.count() > 0 && alreadyMember.isVisible()) {
            sakai.gotoPath(sakaiUrl);
            sakai.toolClick("Site Info");
            return;
        }

        Locator sameRole = page.locator("#same-role");
        if (sameRole.count() > 0 && sameRole.isVisible()) {
            sameRole.check(new Locator.CheckOptions().setForce(true));
            Locator maintainChoice = page.locator("input[name=\"sameRoleChoice\"][value=\"maintain\"]");
            if (maintainChoice.count() > 0 && maintainChoice.isVisible()) {
                maintainChoice.check(new Locator.CheckOptions().setForce(true));
            }
            page.locator("#participant-helper form").first().locator("button[type=\"submit\"]").first().click();
            page.waitForLoadState();
        }

        Locator dontSendEmail = page.locator("#dont-send-email");
        if (dontSendEmail.count() > 0 && dontSendEmail.isVisible()) {
            dontSendEmail.check(new Locator.CheckOptions().setForce(true));
            page.locator("#participant-helper form").first().locator("button[type=\"submit\"]").first().click();
            page.waitForLoadState();
        }
    }

    private void createGroupWithMember(String groupTitle, String memberEid, String nonMemberSiteParticipantEid) {
        sakai.toolClick("Site Info");
        // Worksite Setup picks whichever course/section checkbox happens to be first, so the new
        // site's CM roster (and whether memberEid/nonMemberSiteParticipantEid end up on it) isn't
        // predictable. Adding both explicitly makes memberEid's presence in #groupMembers
        // deterministic, and gives nonMemberSiteParticipantEid site access without group
        // membership, so its negative visibility check tests group restriction, not site access.
        ensureSiteParticipant(memberEid);
        ensureSiteParticipant(nonMemberSiteParticipantEid);

        page.locator(".navIntraTool a").filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Manage Groups$", Pattern.CASE_INSENSITIVE)))
            .first().click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();

        page.locator(".navIntraTool a").filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Create New Group$", Pattern.CASE_INSENSITIVE)))
            .first().click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();

        Locator groupTitleInput = page.locator("#groupTitle");
        assertThat(groupTitleInput).isVisible();
        groupTitleInput.fill(groupTitle);
        // The create-group button starts disabled; only a keyup on the title field re-enables it.
        groupTitleInput.dispatchEvent("keyup");

        // #groupMembers is a native multi-select wrapped by Select2; drive the underlying
        // element directly and fire the change event Select2/Thymeleaf's binding listens for.
        Boolean memberSelected = (Boolean) page.evaluate("(eid) => {"
            + "const select = document.getElementById('groupMembers');"
            + "const option = [...select.options].find((o) => o.textContent.includes('(' + eid + ')'));"
            + "if (!option) { return false; }"
            + "option.selected = true;"
            + "if (window.jQuery) { jQuery(select).trigger('change'); } else { select.dispatchEvent(new Event('change', { bubbles: true })); }"
            + "return true;"
            + "}", memberEid);
        if (!Boolean.TRUE.equals(memberSelected)) {
            throw new IllegalStateException("Unable to find group member option for " + memberEid);
        }

        Locator submit = page.locator("#create-group-submit-button");
        assertThat(submit).isEnabled();
        submit.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();
    }

    private void restrictEventToGroup(String groupTitle) {
        page.locator("#groups").click(new Locator.ClickOptions().setForce(true));
        Locator checkbox = page.locator("#groupTable tr").filter(new Locator.FilterOptions().setHasText(groupTitle))
            .locator("input[name=\"selectedGroups\"]");
        assertThat(checkbox).isVisible();
        checkbox.check(new Locator.CheckOptions().setForce(true));
    }

    private void clickAddEvent() {
        page.locator(".navIntraTool a").filter(new Locator.FilterOptions().setHasText("Add Event")).first()
            .click(new Locator.ClickOptions().setForce(true));
    }

    private void openEvent(String title) {
        switchToListView();
        page.locator("table a").filter(new Locator.FilterOptions().setHasText(title)).first()
            .click(new Locator.ClickOptions().setForce(true));
    }

    // Locale can render either the 12-hour selects (#startHour/#startAmpm) or the 24-hour
    // select (#startHour24) as the visible one; the other pair stays in the DOM but hidden.
    private void setEventStartTime(String hour24Value, String hourValue, String minuteValue, String ampmValue) {
        Locator hour24Select = page.locator("#startHour24").first();
        if (hour24Select.count() > 0 && hour24Select.isVisible()) {
            hour24Select.selectOption(hour24Value);
        } else {
            page.locator("#startHour").selectOption(hourValue);
            page.locator("#startAmpm").selectOption(ampmValue);
        }
        page.locator("#startMinute").selectOption(minuteValue);
    }

    private void fillEventDescription(String html) {
        if (sakai.typeCkEditorIfPresent("description", html)) {
            return;
        }

        String plainText = html.replaceAll("<[^>]+>", "").trim();
        Locator fallback = page.locator("textarea#description, textarea:visible, [contenteditable=\"true\"]:visible").first();
        assertThat(fallback).isVisible();
        fallback.fill(plainText);
        fallback.dispatchEvent("input");
        fallback.dispatchEvent("change");
        fallback.dispatchEvent("blur");
    }

    private void submitEventForm(String submitButtonName) {
        page.evaluate("() => {"
            + "const editor = window.CKEDITOR && window.CKEDITOR.instances && window.CKEDITOR.instances.description;"
            + "if (editor) { editor.updateElement(); }"
            + "}");

        Locator submit = page.locator("input[name=\"" + submitButtonName + "\"]:visible").first();
        assertThat(submit).isVisible();
        submit.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();
        assertThat(page.locator("body")).containsText(Pattern.compile("Calendar", Pattern.CASE_INSENSITIVE));
    }

    private void switchToListView() {
        // selectOption()/click() trigger onchange->form.submit() navigations but don't wait for
        // them, and the resulting page has the same select#timeFilterOption id as the page it
        // replaces, so polling for that element's visibility can pass against the stale page
        // before the navigation even starts. waitForNavigation() arms the wait before the
        // triggering action so it's tied to the actual page load, not to an element that's
        // present on both the old and new DOM.
        Locator viewSelect = page.locator("select#view").first();
        assertThat(viewSelect).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(15_000));
        if (!"List of Events".equals(viewSelect.inputValue())) {
            page.waitForNavigation(() -> viewSelect.selectOption("List of Events"));
        }

        Locator timeFilter = page.locator("select#timeFilterOption").first();
        assertThat(timeFilter).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(15_000));
        if (!"SHOW_ALL".equals(timeFilter.inputValue())) {
            timeFilter.selectOption("SHOW_ALL");
            page.waitForNavigation(() ->
                page.locator("input[name=\"eventSubmit_doFilter\"]").click(new Locator.ClickOptions().setForce(true)));

            Locator timeFilterAfterNav = page.locator("select#timeFilterOption").first();
            assertThat(timeFilterAfterNav).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(15_000));
            if (!"SHOW_ALL".equals(timeFilterAfterNav.inputValue())) {
                throw new IllegalStateException("Calendar list view did not apply the All events filter");
            }
        }
    }

    private Locator eventRows() {
        return page.locator("table tr");
    }
}
