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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.FilePayload;
import com.microsoft.playwright.options.LoadState;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiHelper.TestUser;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/** Regression checks for Lessons page release settings and access restrictions. */
class LessonsReleaseTest extends SakaiUiTestBase {

    private static final String INSTRUCTOR = "instructor1";
    private static final List<String> TOOLS = List.of("sakai\\.lessonbuildertool");

    private static String siteUrl;

    private LessonsUi lessons;

    @BeforeEach
    void openCourse() {
        lessons = new LessonsUi(page, sakai);
        sakai.login(INSTRUCTOR);
        if (siteUrl == null) {
            siteUrl = sakai.createFreshCourse(INSTRUCTOR, TOOLS);
        }
        lessons.open(siteUrl);
    }

    private String newPage(String prefix) {
        String title = prefix + " " + sakai.randomId();
        lessons.createTopLevelPage(title);
        return title;
    }

    private TestUser newStudent(String prefix) {
        return lessons.newStudent(siteUrl, prefix);
    }

    private void openAsStudent(TestUser student, String pageTitle) {
        sakai.login(student.username());
        page.navigate(siteUrl);
        sakai.toolClick(pageTitle);
    }

    @Test
    void pageWithPrerequisitesOpensAfterTheRequiredPage() {
        String required = newPage("Required");
        lessons.addText("<p>Required content</p>");
        Locator settings = lessons.openPageSettings();
        settings.locator("#page-required").check();
        lessons.savePageSettings(settings);

        String locked = newPage("Locked");
        lessons.addText("<p>Locked content</p>");
        settings = lessons.openPageSettings();
        settings.locator("#page-prerequisites").check();
        lessons.savePageSettings(settings);

        try (TestUser student = newStudent("lessons-prerequisites")) {
            openAsStudent(student, locked);
            assertThat(page.getByText("Before viewing page, you must complete the following pages:")).isVisible();
            assertThat(page.getByText("Locked content")).hasCount(0);

            sakai.toolClick(required);
            assertThat(page.getByText("Required content")).isVisible();
            sakai.toolClick(locked);
            assertThat(page.getByText("Locked content")).isVisible();
        }
    }

    @Test
    void pageWithFutureReleaseDateWaitsForStudents() {
        String title = newPage("Release date");
        lessons.addText("<p>Future content</p>");
        LocalDateTime releaseDate = LocalDateTime.now().plusDays(30).withHour(10).withMinute(30);
        Locator settings = lessons.openPageSettings();
        settings.locator("#page-releasedate").check();
        settings.locator("#release_date").fill(releaseDate.format(DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm")));
        lessons.savePageSettings(settings);
        assertThat(page.getByText("Future content")).isVisible();
        // Lessons reads the date in the instructor's time zone and shows it in the student's one.
        String timeZone = (String) page.evaluate("() => portal.user.timezone");

        try (TestUser student = newStudent("lessons-release-date")) {
            sakai.login(student.username());
            sakai.setUserTimeZone(student.username(), timeZone);
            openAsStudent(student, title);
            String expected = releaseDate.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                .withLocale(Locale.US));
            // JDK versions differ in the space they put before AM and PM. Playwright runs the pattern as a
            // JavaScript regex, where \s also matches no-break spaces and Pattern.quote is not understood.
            assertThat(page.getByText("This page will not be available until")).containsText(Pattern.compile(
                Arrays.stream(expected.split("[\\s\\u202f]+")).map(part -> part.replaceAll("[\\\\^$.|?*+()\\[\\]{}]", "\\\\$0"))
                    .collect(Collectors.joining("\\s+"))));
            assertThat(page.getByText("Future content")).hasCount(0);
        }
    }

    /**
     * Subpage release dates used a different submit path than page Settings and drifted by the
     * browser/user timezone offset on every Update. Keep Europe/Madrid while the Playwright browser
     * stays on America/New_York so a regression shows up as a multi-hour shift.
     */
    @Test
    void subpageReleaseDateDoesNotDriftAcrossEditAndSettings() {
        try (TestUser instructor = sakai.createUser("lessons-subpage-release")) {
            sakai.login(instructor.username());
            String courseUrl = sakai.createFreshCourse(instructor.username(), TOOLS);
            sakai.setUserTimeZone(instructor.username(), "Europe/Madrid");
            assertEquals("Europe/Madrid", page.evaluate("() => portal.user.timezone"));

            lessons = new LessonsUi(page, sakai);
            lessons.open(courseUrl);

            String parent = "Release parent " + sakai.randomId();
            lessons.createTopLevelPage(parent);
            String child = "Release child " + sakai.randomId();
            lessons.addSubpage(child);
            lessons.backToParent(parent);

            String releaseValue = "2025-09-10T00:00";
            Locator edit = lessons.openItemEditor(lessons.item(child));
            edit.locator("#page-releasedate2").check();
            edit.locator("#release_date2").fill(releaseValue);
            edit.locator("#release_date2").dispatchEvent("change");
            lessons.saveItemEditor(edit);

            lessons.openSubpageFromParent(child);
            Locator settings = lessons.openPageSettings();
            assertTrue(settings.locator("#page-releasedate").isChecked());
            assertEquals(releaseValue, settings.locator("#release_date").inputValue());
            settings.locator("button.btn-close").click();
            assertThat(settings).isHidden();

            lessons.backToParent(parent);
            edit = lessons.openItemEditor(lessons.item(child));
            assertTrue(edit.locator("#page-releasedate2").isChecked());
            assertEquals(releaseValue, edit.locator("#release_date2").inputValue());
            lessons.saveItemEditor(edit);

            edit = lessons.openItemEditor(lessons.item(child));
            assertEquals(releaseValue, edit.locator("#release_date2").inputValue());
            edit.locator("button.btn-close").click();
            assertThat(edit).isHidden();

            lessons.openSubpageFromParent(child);
            settings = lessons.openPageSettings();
            assertEquals(releaseValue, settings.locator("#release_date").inputValue());
        }
    }

    @Test
    void hiddenPageIsUnavailableToStudents() {
        String title = newPage("Hidden");
        lessons.addText("<p>Hidden content</p>");
        String pageUrl = lessons.navigationLink(title).getAttribute("href");
        Locator settings = lessons.openPageSettings();
        settings.locator("input[type=radio][value=hide]").check();
        lessons.savePageSettings(settings);
        assertThat(lessons.navigationLink(title)).hasCount(1);

        try (TestUser student = newStudent("lessons-hidden")) {
            sakai.login(student.username());
            page.navigate(siteUrl);
            assertThat(lessons.navigationLink(title)).hasCount(0);

            // The portal shows another page of the site instead of the hidden one.
            page.navigate(pageUrl);
            page.waitForLoadState(LoadState.LOAD);
            assertFalse(page.title().contains(title));
            for (Frame frame : page.frames()) {
                assertThat(frame.getByText("Hidden content")).hasCount(0);
            }
        }
    }

    @Test
    void uploadedCustomCssIsAppliedToThePage() {
        newPage("Custom CSS");
        String cssName = "lessons-" + sakai.randomId() + ".css";
        Locator settings = lessons.openPageSettings();
        settings.locator("input[type=file][name=cssUpload]").setInputFiles(new FilePayload(cssName, "text/css",
            ".portletBody { outline: 3px solid rgb(1, 2, 3); }\n".getBytes(StandardCharsets.UTF_8)));
        lessons.savePageSettings(settings);

        assertThat(page.locator("link[rel=stylesheet][href$='/LB-CSS/" + cssName + "']")).hasCount(1);
        assertThat(page.locator(".portletBody")).hasCSS("outline-color", "rgb(1, 2, 3)");
        settings = lessons.openPageSettings();
        assertThat(settings.locator("#cssDropdown-selection option:checked")).hasText(cssName);
    }

    @Test
    void itemReleasedToAGroupIsOnlyShownToItsMembers() {
        String title = newPage("Group item");
        try (TestUser member = newStudent("lessons-group-member");
             TestUser outsider = newStudent("lessons-group-outsider")) {
            String groupTitle = "Lessons group " + sakai.randomId();
            sakai.addGroup(siteUrl, groupTitle, member.username());
            page.reload();
            lessons.addText("<p>Group only text</p>");
            lessons.addText("<p>Text for everyone</p>");
            lessons.restrictTextToGroup(lessons.item("Group only text"), groupTitle);

            openAsStudent(member, title);
            assertThat(page.getByText("Group only text")).isVisible();
            assertThat(page.getByText("Text for everyone")).isVisible();

            openAsStudent(outsider, title);
            assertThat(page.getByText("Text for everyone")).isVisible();
            assertThat(page.getByText("Group only text")).hasCount(0);
        }
    }
}
