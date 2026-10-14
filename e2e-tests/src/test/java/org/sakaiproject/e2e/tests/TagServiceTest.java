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
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class TagServiceTest extends SakaiUiTestBase {
    @Test
    void preservesCollectionAfterValidationAndDisplaysLiteralLabels() {
        sakai.login("instructor1");
        String courseUrl = sakai.createCourse("instructor1", List.of("sakai\\.tagservice"));
        page.navigate(courseUrl);
        sakai.toolClick("Tags Service");
        String name = "Literal label collection " + System.currentTimeMillis();
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Create Tag Collection").setExact(true)).click();
        page.locator("#name").fill(name);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save Tag Collection").setExact(true)).click();
        Locator collection = page.locator(".tagservice-table tbody tr")
            .filter(new Locator.FilterOptions().setHasText(name));
        String label = "<script>alert('test')</script> & \"日本語\" " + System.currentTimeMillis();
        collection.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("View Tags").setExact(true)).click();
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Create Tag")).click();
        String collectionId = page.locator("#tagCollectionId").inputValue();
        page.getByLabel("Label", new Page.GetByLabelOptions().setExact(true)).fill(label);
        page.getByLabel("Description", new Page.GetByLabelOptions().setExact(true)).fill("x".repeat(1001));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save Tag").setExact(true)).click();
        assertThat(page.locator(".alert-danger")).containsText("description is too long");
        assertThat(page.locator("#tagCollectionId")).hasValue(collectionId);
        assertThat(page.locator(".incollection")).containsText(name);
        assertThat(page.locator("#tagLabel")).hasValue(label);
        page.getByLabel("Description", new Page.GetByLabelOptions().setExact(true)).fill("Corrected");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save Tag").setExact(true)).click();
        Locator row = page.locator("tr").filter(new Locator.FilterOptions().setHasText(label));
        assertThat(row).containsText("Corrected");
        assertThat(row.locator("script")).hasCount(0);
        row.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Edit").setExact(true)).click();
        assertThat(page.locator("#tagLabel")).hasValue(label);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Cancel")).click();
        assertThat(page.locator("tr").filter(new Locator.FilterOptions().setHasText(label))).isVisible();
    }

    @Test
    void rendersSamigoTagLabelsAndCollectionNamesAsText() {
        page.navigate("/portal");
        page.evaluate("""
            async () => {
                const { tagLabel } = await import('/samigo-app/js/tag-display.js');
                const label = tagLabel('<img src=x onerror=alert(1)> & 日本語', '<script>alert(1)</script>');
                label.id = 'literal-tag';
                document.body.append(label);
            }
            """);
        Locator label = page.locator("#literal-tag");
        assertThat(label).containsText("<img src=x onerror=alert(1)> & 日本語");
        assertThat(label.locator(".collection")).hasText("(<script>alert(1)</script>)");
        assertThat(label.locator("img, script")).hasCount(0);
    }
}
