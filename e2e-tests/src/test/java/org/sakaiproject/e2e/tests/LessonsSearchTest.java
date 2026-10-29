/**
 * Copyright (c) 2026 The Apereo Foundation
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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.AbstractToolSearchTest;

// Text boxes have no title of their own, so the text sits on a subpage carrying the title
class LessonsSearchTest extends AbstractToolSearchTest {

    private String subpageTitle;

    @Override
    protected String toolId() {
        return "sakai\\.lessonbuildertool";
    }

    @Override
    protected String searchToolName() {
        return "lessons";
    }

    // what the portal search panel calls the tool (toolname_lesson)
    @Override
    protected String toolLabel() {
        return "Lesson";
    }

    // the page is visible to every member; release and prerequisite rules are LessonsAccess's
    @Override
    protected String deniedUser() {
        return null;
    }

    @Override
    protected String createItem(String siteId, String title, String body) {
        openLessons(siteId);
        addSubpage(title);
        subpageTitle = title;

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Content")).first().click();
        page.locator("#addContentDiv .add-text-link").click();
        saveText(body);

        String href = page.locator(".edit-col a.itemLink").first().getAttribute("href");
        Matcher id = Pattern.compile("itemId=(\\d+)").matcher(href);
        if (!id.find()) {
            throw new IllegalStateException("no item id in " + href);
        }
        return "/lessonbuilder/item/" + id.group(1);
    }

    @Override
    protected void updateItem(String siteId, String itemKey, String body) {
        openTextEditor(siteId);
        saveText(body);
    }

    @Override
    protected void deleteItem(String siteId, String itemKey) {
        openTextEditor(siteId);
        page.onceDialog(dialog -> dialog.accept());
        page.locator("input.deleteButton").click();
        assertThat(page.locator("#subpage-breadcrumb-div")).containsText(subpageTitle);
    }

    @Test
    @Order(3)
    void subpageIsFoundByItsTitle() {
        sakai.login(author());
        JsonObject subpage = await("'" + titleWord + "' to find the subpage", () -> subpageResult(search(titleWord)), r -> r != null);

        sakai.gotoPath(subpage.get("url").getAsString().replaceFirst("^https?://[^/]+", ""));

        assertThat(page.locator("#subpage-breadcrumb-div")).containsText(subpageTitle);
    }

    private JsonObject subpageResult(JsonArray results) {
        for (JsonElement element : results) {
            JsonObject result = element.getAsJsonObject();
            String reference = result.get("reference").getAsString();
            if (reference.startsWith("/lessonbuilder/item/") && !reference.equals(itemKey)
                    && result.get("title").getAsString().contains(titleWord)) {
                return result;
            }
        }
        return null;
    }

    @Override
    protected void assertOpensTool() {
        // the result opens the subpage holding the text, not the top of the tool
        assertThat(page.locator("#subpage-breadcrumb-div")).containsText(subpageTitle);
        assertThat(page.locator("#content")).containsText(bodyWord);
    }

    private void openLessons(String siteId) {
        sakai.gotoPath("/portal/site/" + siteId);
        sakai.toolClick("Lessons");
    }

    private void openSubpage(String siteId) {
        openLessons(siteId);
        page.locator("#content a").filter(new Locator.FilterOptions().setHasText(subpageTitle)).first().click();
        assertThat(page.locator("#subpage-breadcrumb-div")).containsText(subpageTitle);
    }

    private void openTextEditor(String siteId) {
        openSubpage(siteId);
        // the edit button only shows while the text box is hovered
        page.locator(".textbox").first().hover();
        page.locator(".edit-col a.itemLink").first().click();
    }

    // as LessonsTest does
    private void addSubpage(String title) {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Content")).first().click();
        Locator addContentDialog = page.locator("#addContentDiv");
        assertThat(addContentDialog).isVisible();
        addContentDialog.getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName("Add Subpage").setExact(true)).click();
        Locator subpageDialog = page.locator("#subpage-dialog");
        assertThat(subpageDialog).isVisible();
        subpageDialog.locator("#subpage-title").fill(title);
        subpageDialog.getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName("Create").setExact(true)).click();
        assertThat(page.locator("#subpage-breadcrumb-div")).containsText(title);
    }

    private void saveText(String html) {
        if (!sakai.typeFirstCkEditorIfPresent(html)) {
            throw new IllegalStateException("no editor on the text form");
        }
        page.locator("input[type=submit].active").first().click();
        assertThat(page.locator("#subpage-breadcrumb-div")).containsText(subpageTitle);
    }
}
