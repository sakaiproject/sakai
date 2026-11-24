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
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.FilePayload;
import com.microsoft.playwright.options.LoadState;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

        try (TestUser student = newStudent("lessons-release-date")) {
            openAsStudent(student, title);
            assertThat(page.getByText("This page will not be available until")).containsText(
                String.valueOf(releaseDate.getYear()));
            assertThat(page.getByText("Future content")).hasCount(0);
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
