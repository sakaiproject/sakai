/**
 * Copyright (c) 2026 The Apereo Foundation
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
import org.sakaiproject.e2e.support.AbstractToolSearchTest;

// Event form steps follow CalendarTest
class CalendarSearchTest extends AbstractToolSearchTest {

    private static final String EVENT_DATE = java.time.LocalDate.now().plusMonths(1).toString();

    private String eventTitle;

    @Override
    protected String toolId() {
        return "sakai\\.schedule";
    }

    @Override
    protected String searchToolName() {
        return "calendar";
    }

    @Override
    protected String toolLabel() {
        return "Calendar";
    }

    // site events are visible to every member; group restriction is covered by CalendarContentProducerTest
    @Override
    protected String deniedUser() {
        return null;
    }

    @Override
    protected String createItem(String siteId, String title, String body) {
        openCalendar(siteId);
        page.locator(".navIntraTool a").filter(new Locator.FilterOptions().setHasText("Add Event")).first()
            .click(new Locator.ClickOptions().setForce(true));
        page.locator("#activitytitle").fill(title);
        sakai.selectDate("#newCalendarDate", EVENT_DATE);
        Locator hour24 = page.locator("#startHour24").first();
        if (hour24.count() > 0 && hour24.isVisible()) {
            hour24.selectOption("9");
        } else {
            page.locator("#startHour").selectOption("9");
            page.locator("#startAmpm").selectOption("am");
        }
        page.locator("#startMinute").selectOption("0");
        page.locator("#duHour").selectOption("1");
        page.locator("#duMinute").selectOption("0");
        sakai.typeCkEditorIfPresent("description", body);
        submit("eventSubmit_doAdd");
        eventTitle = title;
        // the site has this one event, so its references share this prefix
        return "/calendar/event/" + siteId + "/";
    }

    @Override
    protected void updateItem(String siteId, String itemKey, String body) {
        openEvent(siteId);
        page.locator("input[name=\"eventSubmit_doRevise\"]").click();
        sakai.typeCkEditorIfPresent("description", body);
        submit("eventSubmit_doUpdate");
    }

    @Override
    protected void deleteItem(String siteId, String itemKey) {
        openEvent(siteId);
        page.locator("input[name=\"eventSubmit_doDelete\"]").click();
        page.locator("input[name=\"eventSubmit_doConfirm\"]").click();
    }

    @Override
    protected void assertOpensTool() {
        // the result opens the event itself
        assertThat(page.locator("body")).containsText(titleWord);
    }

    private void openCalendar(String siteId) {
        sakai.gotoPath("/portal/site/" + siteId);
        sakai.toolClick("Calendar");
    }

    private void openEvent(String siteId) {
        openCalendar(siteId);
        Locator view = page.locator("select#view").first();
        assertThat(view).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(15_000));
        if (!"List of Events".equals(view.inputValue())) {
            page.waitForNavigation(() -> view.selectOption("List of Events"));
        }
        Locator filter = page.locator("select#timeFilterOption").first();
        assertThat(filter).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(15_000));
        if (!"SHOW_ALL".equals(filter.inputValue())) {
            filter.selectOption("SHOW_ALL");
            page.waitForNavigation(() ->
                page.locator("input[name=\"eventSubmit_doFilter\"]").click(new Locator.ClickOptions().setForce(true)));
        }
        page.locator("table a").filter(new Locator.FilterOptions().setHasText(eventTitle)).first()
            .click(new Locator.ClickOptions().setForce(true));
    }

    private void submit(String buttonName) {
        page.evaluate("() => { const editor = window.CKEDITOR && window.CKEDITOR.instances && window.CKEDITOR.instances.description;"
            + " if (editor) { editor.updateElement(); } }");
        Locator button = page.locator("input[name=\"" + buttonName + "\"]:visible").first();
        button.click();
        assertThat(button).hasCount(0);
    }
}
