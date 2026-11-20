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

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/** Resources files used from Lessons, from the community Resources regression script. */
class ResourcesLessonsTest extends SakaiUiTestBase {

    private static final List<String> TOOLS = List.of("sakai\\.resources", "sakai\\.lessonbuildertool");
    private static final String STUDENT = "student0011";

    @Test
    void lessonsLinkToFileInFolderWithPipeOpensTheFile() {
        // SAK-46201: the Lessons link keeps the "|" of the folder name in its path.
        String folder = "Pipe | Folder";
        ResourcesUi resources = new ResourcesUi(page, sakai);
        sakai.login("instructor1");
        String siteUrl = sakai.createFreshCourse("instructor1", TOOLS);
        page.navigate(siteUrl);
        sakai.toolClick("Resources");
        resources.openCreateFolders(resources.rootRow(), List.of(folder));
        resources.createFolders();
        resources.uploadText(folder, "pipe.txt", "Pipe content");

        sakai.toolClick("Lessons");
        chooseFromResources("Add Content Links", folder, List.of("pipe.txt"));

        Locator link = page.locator("a.itemlink").filter(new Locator.FilterOptions().setHasText("pipe.txt"));
        assertThat(link).hasAttribute("target", "_blank");
        // The browser resolves the link; request exactly the URL it would open.
        String url = (String) link.evaluate("a => a.href");
        assertEquals("Pipe content", read(url));
    }

    @Test
    @Disabled("SAK-44571 is open: Lessons still shows embedded resources whose release date is in the future")
    void lessonsHidesEmbeddedFilesWhoseReleaseDateIsInTheFuture() {
        // SAK-44571: embedded resources that are not yet available must not reach students.
        String folder = "Embedded";
        ResourcesUi resources = new ResourcesUi(page, sakai);
        sakai.login("instructor1");
        String siteUrl = sakai.createFreshCourse("instructor1", TOOLS);
        sakai.addMember(siteUrl, STUDENT, "Student");
        page.navigate(siteUrl);
        sakai.toolClick("Resources");
        resources.openCreateFolders(resources.rootRow(), List.of(folder));
        page.locator("#hidden_with_accessible_content_true_0").check();
        resources.createFolders();
        assertThat(resources.row(folder).locator(".resource-access")).hasText("Entire site (Hidden)");
        resources.uploadText(folder, "visible.txt", "Visible now");
        resources.uploadText(folder, "future.txt", "Visible later");

        resources.openFolder(folder);
        resources.itemAction(resources.row("future.txt"), "Edit Details");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Availability and Access").setExact(true)).click();
        resources.setStartDate(0, LocalDateTime.now().plusDays(2));
        page.locator("#finish_button").click();
        assertThat(resources.itemLink("future.txt")).isVisible();

        sakai.toolClick("Lessons");
        String lessonsUrl = page.url();
        chooseFromResources("Embed Content on the Page", folder, List.of("visible.txt", "future.txt"));
        assertThat(embedded("visible.txt")).hasCount(1);
        assertThat(embedded("future.txt")).hasCount(1);
        // Lessons item URL without the instructor's lb.session token, which authorizes whoever presents it.
        String futureItem = embedded("future.txt").getAttribute("data").replaceFirst("\\?.*$", "");

        sakai.login(STUDENT);
        page.navigate(lessonsUrl);
        assertThat(embedded("visible.txt")).hasCount(1);
        List<String> studentUrls = new ArrayList<>(List.of(futureItem));
        Locator studentEmbed = embedded("future.txt");
        if (studentEmbed.count() > 0) {
            studentUrls.add(studentEmbed.first().getAttribute("data"));
        }
        for (String url : studentUrls) {
            APIResponse response = page.request().get(url);
            try {
                assertFalse(response.ok() && response.text().contains("Visible later"),
                    "A student can read the future file through " + url);
            } finally {
                response.dispose();
            }
        }
        assertThat(page.locator("[data*='future.txt'], [href*='future.txt'], [src*='future.txt']")).hasCount(0);
    }

    /** Adds files from one folder of the site's Resources through the Lessons resource picker. */
    private void chooseFromResources(String addContentAction, String folder, List<String> files) {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Content")).first().click();
        Locator addContent = page.locator("#addContentDiv");
        assertThat(addContent).isVisible();
        addContent.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(addContentAction).setExact(true)).click();
        Locator dialog = page.locator("#add-multimedia-dialog");
        assertThat(dialog).isVisible();
        dialog.locator("#existing-resource-section-collapse").click();
        dialog.locator("#mm-choose").click();

        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Please Choose a File"))).isVisible();
        Locator select = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Select").setExact(true));
        Locator firstFile = page.locator("tr").filter(new Locator.FilterOptions().setHasText(files.get(0)).setHas(select));
        page.locator("tr").filter(new Locator.FilterOptions().setHasText(folder))
            .getByRole(AriaRole.LINK).filter(new Locator.FilterOptions().setHasText(folder)).first().click();
        assertThat(firstFile).isVisible();
        Locator removeLinks = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Remove").setExact(true));
        for (int i = 0; i < files.size(); i++) {
            page.locator("tr").filter(new Locator.FilterOptions().setHasText(files.get(i)).setHas(select))
                .getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Select").setExact(true)).click();
            assertThat(removeLinks).hasCount(i + 1);
        }
        page.locator("#attachButton1").click();
        assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Content")).first()).isVisible();
    }

    private Locator embedded(String fileName) {
        return page.locator("object[data*='" + fileName + "']");
    }

    private String read(String url) {
        APIResponse response = page.request().get(url);
        try {
            assertTrue(response.ok(), "Unable to read " + url + ": HTTP " + response.status());
            return response.text();
        } finally {
            response.dispose();
        }
    }
}
