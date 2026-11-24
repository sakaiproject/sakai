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

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.FilePayload;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/** Uploading new versions of existing files, from the community Resources regression script. */
class ResourcesVersionsTest extends SakaiUiTestBase {

    private static final String FOLDER = "Overwrite";

    @Test
    void uploadNewVersionReplacesTheContentOfTheSameResource() {
        ResourcesUi resources = openEmptyFolder();
        resources.uploadFiles(resources.rootRow(), List.of(text("notes.txt", "First version")), false);
        assertEquals("First version", read(resources.accessUrl("notes.txt")));

        resources.itemAction(resources.row("notes.txt"), "Upload New Version");
        // The portal's <main> also has the id "content".
        page.locator("input#content[type=file]").setInputFiles(text("notes.txt", "Second version"));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Upload New Version Now").setExact(true)).click();

        assertThat(resources.rootRow()).containsText(FOLDER);
        assertEquals("Second version", read(resources.accessUrl("notes.txt")));
    }

    @Test
    void overwriteExistingFilesReplacesEachFileUploadedAgain() {
        // A name with spaces and accents covers SAK-44341.
        List<String> names = List.of("a.txt", "b.txt", "résumé café.txt");
        ResourcesUi resources = openEmptyFolder();
        resources.uploadFiles(resources.rootRow(), names.stream().map(name -> text(name, "Old " + name)).toList(), false);
        for (String name : names) {
            assertEquals("Old " + name, read(resources.accessUrl(name)));
        }

        resources.uploadFiles(resources.rootRow(), names.stream().map(name -> text(name, "New " + name)).toList(), true);
        for (String name : names) {
            assertEquals("New " + name, read(resources.accessUrl(name)));
        }

        // Without the overwrite option the upload is kept as another resource with the same name.
        resources.uploadFiles(resources.rootRow(), List.of(text("a.txt", "Another a.txt")), false);
        assertThat(resources.row("a.txt")).hasCount(2);
    }

    private ResourcesUi openEmptyFolder() {
        ResourcesUi resources = new ResourcesUi(page, sakai);
        sakai.login("instructor1");
        String siteUrl = sakai.createFreshCourse("instructor1", List.of("sakai\\.resources"));
        page.navigate(siteUrl);
        sakai.toolClick("Resources");
        resources.openCreateFolders(resources.rootRow(), List.of(FOLDER));
        resources.createFolders();
        resources.openFolder(FOLDER);
        return resources;
    }

    private static FilePayload text(String name, String content) {
        return new FilePayload(name, "text/plain", content.getBytes(StandardCharsets.UTF_8));
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
