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
import com.microsoft.playwright.options.FilePayload;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/** Requires Resources ZIP expansion enabled on the deployed server. */
@EnabledIfSystemProperty(named = "resources.security.zipExpansion", matches = "true")
class ResourcesZipImportTest extends SakaiUiTestBase {
    @Test
    void rejectsTraversalWithoutPartialImportAndStillExpandsSafeArchive() throws IOException {
        sakai.login("instructor1");
        String siteUrl = sakai.createCourse("instructor1", List.of("sakai\\.resources"));
        page.navigate(siteUrl);
        sakai.toolClick("Resources");
        upload("unsafe-package.zip", true);
        expand("unsafe-package.zip");
        assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("unsafe-package").setExact(true)))
            .hasCount(0);
        assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("canary.txt").setExact(true)))
            .hasCount(0);

        // Rejection must release the original ZIP's edit lock immediately.
        resourceRow("unsafe-package.zip").locator("button[title='Actions']").click();
        page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions().setName("Edit Details").setExact(true)).click();
        assertThat(page.locator("input[name^='displayName']").first()).isVisible();
        page.locator("#cancel_button").click();

        upload("safe-package.zip", false);
        expand("safe-package.zip");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("safe-package").setExact(true)).click();
        assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("safe.txt").setExact(true)))
            .isVisible();
    }

    private void upload(String name, boolean traversal) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            zip.putNextEntry(new ZipEntry("safe.txt"));
            zip.write("valid content".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            if (traversal) {
                zip.putNextEntry(new ZipEntry("../canary.txt"));
                zip.write("unsafe content".getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        page.locator("button[title='Actions']").first().click();
        page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions().setName("Upload Files").setExact(true)).click();
        page.locator("input[type=file]").setInputFiles(new FilePayload(name, "application/zip", bytes.toByteArray()));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Continue").setExact(true)).click();
        assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(name).setExact(true))).isVisible();
    }

    private void expand(String name) {
        resourceRow(name).locator("button[title='Actions']").click();
        page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions().setName("Expand ZIP Archive").setExact(true)).click();
        assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(name).setExact(true))).isVisible();
    }

    private Locator resourceRow(String name) {
        return page.locator("tr").filter(new Locator.FilterOptions().setHas(
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(name).setExact(true))));
    }

}
