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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;
import org.sakaiproject.e2e.support.SakaiHelper.TestUser;

class CalendarRecurrenceWidgetTest extends SakaiUiTestBase {

    @Test
    void midnightOccurrencesAppearInCourseAndUserWidgets() {
        try (TestUser user = sakai.createUser("calendar-midnight")) {
            // New accounts have no institutional roster from which to create a course.
            sakai.login("instructor1");
            String siteUrl = sakai.createFreshCourse("instructor1", List.of("sakai\\.schedule", "sakai\\.dashboard"));
            sakai.addMember(siteUrl, user.username(), "Instructor");
            sakai.login(user.username());
            APIResponse pin = page.request().post("/portal/favorites/update", RequestOptions.create()
                .setForm(FormData.create().set("siteId", sakai.siteIdFromUrl(siteUrl)).set("pinned", "true")));
            try {
                assertTrue(pin.ok(), "Unable to pin the isolated user's course: HTTP " + pin.status());
            } finally {
                pin.dispose();
            }
            String timezone = (String) page.evaluate("() => Intl.DateTimeFormat().resolvedOptions().timeZone");
            sakai.gotoPath("/portal/site/~" + user.username());
            sakai.toolClick("Preferences");
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Time Zone").setExact(true)).click();
            page.locator("[id='timezone_form'] select").selectOption(timezone);
            page.locator("[id='timezone_form:submit']").click();
            assertThat(page.getByText("Your preferences have been updated successfully.",
                new Page.GetByTextOptions().setExact(true))).isVisible();
            // Reload Sakai's session context after saving the preference. The server JVM's
            // default timezone need not match either the user preference or the browser.
            sakai.login(user.username());
            assertEquals(timezone, page.evaluate("() => portal.user.timezone"));
            ZoneId zone = ZoneId.of(timezone);
            sakai.gotoPath(siteUrl);
            sakai.toolClick("Calendar");

            LocalDate first = LocalDate.now(zone).withDayOfMonth(1);
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
            assertCalendarData("/api/sites/" + sakai.siteIdFromUrl(siteUrl) + "/calendar", first, zone, title);
            assertOccurrence(first, title);
            assertOccurrence(first.plusDays(1), title);

            sakai.gotoPath("/portal/site/~" + user.username());
            sakai.toolClick("Dashboard");
            assertCalendarData("/api/users/current/calendar", first, zone, title);
            assertOccurrence(first, title);
            assertOccurrence(first.plusDays(1), title);
        }
    }

    private void assertCalendarData(String path, LocalDate first, ZoneId zone, String title) {
        APIResponse response = page.request().get(path);
        JsonArray events;
        try {
            assertTrue(response.ok(), "Calendar request failed: " + path + " HTTP " + response.status());
            events = JsonParser.parseString(response.text()).getAsJsonObject().getAsJsonArray("events");
        } finally {
            response.dispose();
        }
        for (LocalDate date : List.of(first, first.plusDays(1))) {
            // Resolve each local midnight separately: consecutive days can have different DST offsets.
            long midnight = date.atStartOfDay(zone).toInstant().toEpochMilli();
            boolean found = false;
            for (JsonElement element : events) {
                JsonObject event = element.getAsJsonObject();
                if (title.equals(event.get("title").getAsString()) && midnight == event.get("start").getAsLong()) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Expected midnight occurrence on " + date + " in " + zone + " from " + path);
        }
    }

    private void assertOccurrence(LocalDate date, String title) {
        Locator calendar = page.locator("sakai-calendar-widget sakai-calendar");
        String label = date.format(DateTimeFormatter.ofPattern("d MMMM uuuu EEEE", Locale.ENGLISH));
        Locator day = calendar.getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName(label).setExact(true));
        if (!day.isVisible()) {
            calendar.locator(".calendar__expand-toggle").click();
        }
        // A response alone does not establish that the component has applied its data.
        // Loading also selects today, so wait for the rendered marker before choosing a day.
        assertThat(day).hasClass(java.util.regex.Pattern.compile(".*\\bhas-events\\b.*"));
        day.click();
        assertThat(calendar.locator("#days-events")).containsText(title);
    }
}
