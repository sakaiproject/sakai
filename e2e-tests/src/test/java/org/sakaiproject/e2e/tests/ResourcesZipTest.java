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

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/** ZIP download of selected Resources items. */
class ResourcesZipTest extends SakaiUiTestBase {

    private static final String GROUP_ONE_STUDENT = "student0011";
    private static final String GROUP_TWO_STUDENT = "student0012";

    @Test
    @EnabledIfSystemProperty(named = "resources.test.zipDownload", matches = "true")
    void studentZipDownloadOnlyContainsResourcesTheStudentCanAccess() throws IOException {
        // Requires content.zip.download.maxindividualfilesize and content.zip.download.maxtotalsize above 0.
        ResourcesUi resources = new ResourcesUi(page, sakai);
        sakai.login("instructor1");
        String siteUrl = sakai.createFreshCourse("instructor1", List.of("sakai\\.resources"));
        sakai.addMember(siteUrl, GROUP_ONE_STUDENT, "Student");
        sakai.addMember(siteUrl, GROUP_TWO_STUDENT, "Student");
        String groupOne = sakai.addGroup(siteUrl, "Group1", GROUP_ONE_STUDENT);
        String groupTwo = sakai.addGroup(siteUrl, "Group2", GROUP_TWO_STUDENT);
        String siteId = sakai.siteIdFromUrl(siteUrl);

        page.navigate(siteUrl);
        sakai.toolClick("Resources");
        resources.openCreateFolders(resources.rootRow(), List.of("Site Folder", "Group1", "Group2", "Hidden", "Future"));
        resources.restrictToGroup(1, groupOne);
        resources.restrictToGroup(2, groupTwo);
        page.locator("#hidden_true_3").check();
        resources.setStartDate(4, LocalDateTime.now().plusDays(2));
        resources.createFolders();
        resources.uploadText("Site Folder", "visible.txt", "Visible");
        resources.upload(resources.row("Site Folder"), "hidden.txt", "text/plain",
            "Hidden file".getBytes(StandardCharsets.UTF_8), true);
        resources.uploadText("Group1", "g1.txt", "Group one");
        resources.uploadText("Group2", "g2.txt", "Group two");
        resources.uploadText("Hidden", "h.txt", "Hidden folder");
        resources.uploadText("Future", "f.txt", "Future folder");

        // The instructor keeps the list actions alongside Download Zip.
        for (String button : List.of("move", "copy", "delete", "show", "hide", "zipdownload")) {
            assertThat(page.locator("#" + button + "-button")).isVisible();
        }

        sakai.login(GROUP_ONE_STUDENT);
        page.navigate(siteUrl);
        sakai.toolClick("Resources");
        Locator root = resources.rootRow();
        root.locator("button[title='Actions']").click();
        assertThat(root.getByRole(AriaRole.MENUITEM,
            new Locator.GetByRoleOptions().setName("Compress to ZIP Archive"))).hasCount(0);
        page.keyboard().press("Escape");

        // Students have no list actions, but still get Download Zip.
        for (String button : List.of("move", "copy", "delete", "show", "hide")) {
            assertThat(page.locator("#" + button + "-button")).hasCount(0);
        }
        Locator downloadZip = page.locator("#zipdownload-button");
        assertThat(downloadZip).isDisabled();
        Locator selections = page.locator("input[name='selectedMembers']");
        assertThat(selections).hasCount(2);
        for (int i = 0; i < 2; i++) {
            selections.nth(i).check();
        }
        assertThat(downloadZip).isEnabled();
        downloadZip.click();

        Locator confirmation = page.locator("#zipDownloadFileForm");
        assertThat(confirmation).containsText("Site Folder");
        assertThat(confirmation).containsText("Group1");
        for (String inaccessible : List.of("Group2", "Hidden", "Future", "hidden.txt")) {
            assertThat(confirmation).not().containsText(inaccessible);
        }

        Download zip = page.waitForDownload(() -> page.locator("#zipDownloadButton").click());
        assertTrue(zip.suggestedFilename().endsWith(".zip"), "Unexpected file name " + zip.suggestedFilename());
        assertTrue(zip.suggestedFilename().contains(siteId), "ZIP is not named after the site: " + zip.suggestedFilename());
        // Entry names replace characters outside [a-zA-Z0-9.-/\] with "_" (SAK-49509).
        assertEquals(Map.of("Site_Folder/visible.txt", "Visible", "Group1/g1.txt", "Group one"),
            zipEntries(Files.readAllBytes(zip.path())));
    }

    /** File entries of an archive by path, ignoring folder entries. */
    private static Map<String, String> zipEntries(byte[] archive) throws IOException {
        Map<String, String> entries = new TreeMap<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive), StandardCharsets.UTF_8)) {
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if (!entry.isDirectory()) {
                    entries.put(entry.getName(), new String(zip.readAllBytes(), StandardCharsets.UTF_8));
                }
            }
        }
        return entries;
    }
}
