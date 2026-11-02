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
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class CalendarRecurrenceWidgetTest extends SakaiUiTestBase {

    @Test
    void midnightOccurrencesAppearInCourseAndUserWidgets() {
        sakai.login("instructor1");
        String siteUrl = sakai.createCourse("instructor1", List.of("sakai\\.schedule", "sakai\\.dashboard"));
        sakai.gotoPath(siteUrl);
        sakai.toolClick("Calendar");

        String timezone = (String) page.evaluate("() => portal.user.timezone");
        LocalDate first = LocalDate.now(ZoneId.of(timezone)).withDayOfMonth(1);
        String title = "Midnight recurrence " + UUID.randomUUID();
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Add Event").setExact(true)).click();
        page.locator("#activitytitle").fill(title);
        sakai.selectDate("#newCalendarDate", first.toString());
        if (page.locator("#startHour24").isVisible()) {
            page.locator("#startHour24").selectOption("0");
        } else {
            page.locator("#startHour").selectOption("12");
            page.locator("#startAmpm").selectOption("am");
        }
        page.locator("#startMinute").selectOption("0");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Frequency").setExact(true)).click();
        page.getByLabel("Event Frequency").selectOption("daily");
        page.getByRole(AriaRole.RADIO, new Page.GetByRoleOptions().setName("On").setExact(true)).check();
        sakai.selectDate("#frequencyDate", first.plusDays(2).toString());
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save").setExact(true)).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save Event").setExact(true)).click();
        assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Add Event").setExact(true))).isVisible();

        sakai.toolClick("Dashboard");
        assertOccurrence(first, title);
        assertOccurrence(first.plusDays(1), title);

        sakai.gotoPath("/portal/site/~instructor1");
        sakai.toolClick("Dashboard");
        assertOccurrence(first, title);
        assertOccurrence(first.plusDays(1), title);
    }

    private void assertOccurrence(LocalDate date, String title) {
        Locator calendar = page.locator("sakai-calendar-widget sakai-calendar");
        String label = date.format(DateTimeFormatter.ofPattern("d MMMM uuuu EEEE", Locale.ENGLISH));
        Locator day = calendar.getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName(label).setExact(true));
        if (!day.isVisible()) {
            calendar.locator(".calendar__expand-toggle").click();
        }
        day.click();
        assertThat(calendar.locator("#days-events")).containsText(title);
        assertThat(day).hasClass(java.util.regex.Pattern.compile(".*\\bhas-events\\b.*"));
    }
}
