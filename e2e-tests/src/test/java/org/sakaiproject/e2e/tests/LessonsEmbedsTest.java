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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.options.AriaRole;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/** Regression checks for Lessons items that embed content from other tools, ZIP uploads and Common Cartridge. */
class LessonsEmbedsTest extends SakaiUiTestBase {

    private static final String INSTRUCTOR = "instructor1";
    private static final List<String> TOOLS = List.of("sakai\\.lessonbuildertool", "sakai\\.resources",
        "sakai\\.schedule", "sakai\\.announcements", "sakai\\.forums");

    private static String siteUrl;

    private LessonsUi lessons;

    @BeforeEach
    void openCourse() {
        lessons = new LessonsUi(page, sakai);
        sakai.login(INSTRUCTOR);
        if (siteUrl == null) {
            siteUrl = sakai.createFreshCourse(INSTRUCTOR, TOOLS);
        }
        page.navigate(siteUrl);
    }

    private String newPage(String prefix) {
        String title = prefix + " " + sakai.randomId();
        lessons.open(siteUrl);
        lessons.createTopLevelPage(title);
        return title;
    }

    @Test
    void resourcesFolderListsItsFiles() {
        String folder = "Lessons folder " + sakai.randomId();
        ResourcesUi resources = new ResourcesUi(page, sakai);
        sakai.toolClick("Resources");
        resources.openCreateFolders(resources.rootRow(), List.of(folder));
        resources.createFolders();
        resources.uploadText(folder, "folder-notes.txt", "Folder notes");

        newPage("Folder listing");
        Locator item = lessons.addResourcesFolder(folder);
        assertThat(item.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("folder-notes.txt"))).isVisible();
    }

    @Test
    void embeddedCalendarIsRendered() {
        newPage("Calendar embed");
        Locator item = lessons.embedCalendar();
        assertThat(item.locator(".fc-toolbar-title")).isVisible();
    }

    @Test
    void embeddedAnnouncementsListTheLatestAnnouncement() {
        String subject = "Lessons announcement " + sakai.randomId();
        postAnnouncement(subject);
        newPage("Announcements embed");
        Locator item = lessons.embedAnnouncements("10");
        assertThat(item.locator(".announcementSummaryList")).containsText(subject);
        assertThat(item.locator(".numberOfAnnouncements")).containsText("10");
    }

    @Test
    void embeddedForumConversationsListTheLatestMessage() {
        String subject = "Lessons conversation " + sakai.randomId();
        postToDefaultTopic(subject);
        newPage("Conversations embed");
        Locator item = lessons.embedForumConversations("5");
        assertThat(item.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName(subject))).isVisible();
    }

    @Test
    void uploadedZipIsExpandedIntoAWebsite() {
        newPage("Zip upload");
        lessons.uploadZip("Zip website", "website.zip", website());

        String indexUrl = (String) lessons.item("Zip website").locator("a.itemlink").evaluate("a => a.href");
        assertTrue(indexUrl.endsWith("/index.html"), indexUrl);
        // Sakai wraps uploaded HTML files in its own page.
        assertTrue(read(indexUrl).contains("<p>Zip home page</p>"));
        assertTrue(read(indexUrl.replaceFirst("index\\.html$", "second.html")).contains("<p>Zip second page</p>"));
    }

    @Test
    void exportedCartridgeImportsIntoAnotherCourse() {
        String title = newPage("Cartridge page");
        lessons.addText("<p>Cartridge content</p>");
        Download cartridge = lessons.exportCommonCartridge();

        String otherSite = sakai.createFreshCourse(INSTRUCTOR, List.of("sakai\\.lessonbuildertool"));
        lessons.open(otherSite);
        lessons.importCommonCartridge(cartridge.path());
        Locator imported = lessons.navigationLink(title);
        assertThat(imported).hasCount(1, new LocatorAssertions.HasCountOptions().setTimeout(60_000));
        sakai.toolClick(title);
        assertThat(page.getByText("Cartridge content")).isVisible();
    }

    private void postAnnouncement(String subject) {
        sakai.toolClick("Announcements");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Add").setExact(true)).first().click();
        page.locator("#subject").fill(subject);
        sakai.typeCkEditor("body", "<p>Announcement body</p>");
        page.locator("#saveChanges").click();
        assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(subject)).first()).isVisible();
    }

    /** Discussions creates a forum with one topic the first time an instructor opens it. */
    private void postToDefaultTopic(String subject) {
        sakai.toolClick("Discussions");
        page.locator("[id='msgForum:forums:0:topics:0:topic_title']").click();
        page.locator("[id='msgForum:df_componse_message_dfAllMessages']").click();
        page.locator("[id='dfCompose:df_compose_title']").fill(subject);
        sakai.typeCkEditor("dfCompose:df_compose_body_inputRichText", "<p>Conversation body</p>");
        page.locator("[id='dfCompose:post']").click();
        assertThat(page.getByText(subject).first()).isVisible();
    }

    private String read(String url) {
        APIResponse response = page.request().get(url);
        try {
            assertTrue(response.ok(), url + " returned HTTP " + response.status());
            return response.text().trim();
        } finally {
            response.dispose();
        }
    }

    private static byte[] website() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            zip.putNextEntry(new ZipEntry("index.html"));
            zip.write("<p>Zip home page</p>".getBytes(StandardCharsets.UTF_8));
            zip.putNextEntry(new ZipEntry("second.html"));
            zip.write("<p>Zip second page</p>".getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bytes.toByteArray();
    }
}
