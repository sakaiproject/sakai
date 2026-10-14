/*
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class TagsTest extends SakaiUiTestBase {
    @Test
    void instructorsReuseGlobalAndSiteTagsAcrossToolsWithoutEditingGlobalCollections() {
        String suffix = Long.toString(System.currentTimeMillis());
        String collectionName = "Learning objectives " + suffix;
        String globalLabel = "Global competency " + suffix;
        String localLabel = "Local competency " + suffix;
        String otherLabel = "Other site competency " + suffix;

        sakai.login("admin");
        page.navigate("/portal/site/!admin");
        sakai.toolClick("Tags");
        createCollection(collectionName);
        collectionRow(collectionName, "Global collection").getByRole(AriaRole.LINK,
            new Locator.GetByRoleOptions().setName("View Tags").setExact(true)).click();
        createTag(globalLabel);

        sakai.login("instructor1");
        String courseUrl = sakai.createCourse("instructor1", List.of("sakai\\.tagservice", "sakai\\.assignment\\.grades",
            "sakai\\.messages", "sakai\\.conversations", "sakai\\.samigo"));
        page.navigate(courseUrl);
        sakai.toolClick("Tags");
        Locator globalRow = collectionRow(collectionName, "Global collection");
        assertThat(globalRow).isVisible();
        assertThat(globalRow.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Edit").setExact(true))).hasCount(0);
        globalRow.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("View Tags").setExact(true)).click();
        assertThat(page.locator(".tagservice-table")).containsText(globalLabel);
        assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Create Tag").setExact(true))).hasCount(0);
        assertThat(page.locator(".tagservice-table .actions a")).hasCount(0);
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Return").setExact(true)).click();

        // Different scopes can use the same collection name.
        createCollection(collectionName);
        collectionRow(collectionName, "Site collection").getByRole(AriaRole.LINK,
            new Locator.GetByRoleOptions().setName("View Tags").setExact(true)).click();
        createTag(localLabel);
        String siteId = sakai.siteIdFromUrl(courseUrl);
        for (String tool : List.of("assignments", "privatemessages", "conversations", "samigo")) {
            APIResponse response = context.request().get("/api/sites/" + siteId + "/tools/" + tool + "/tags/" + siteId);
            assertTrue(response.ok(), response.text());
            assertTrue(response.text().contains(globalLabel), tool);
            assertTrue(response.text().contains(localLabel), tool);
        }

        sakai.toolClick("Assignments");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Add").setExact(true)).click();
        selectTag(page.locator("sakai-tag-selector"), globalLabel);
        selectTag(page.locator("sakai-tag-selector"), localLabel);
        assertFalse(page.locator("#tag_selector").inputValue().contains(globalLabel));

        sakai.toolClick("Messages");
        page.locator("#messagesComposeMenuLink").click();
        selectTag(page.locator("sakai-tag-selector"), globalLabel);
        selectTag(page.locator("sakai-tag-selector"), localLabel);

        sakai.toolClick("Conversation");
        page.locator("#conv-add-topic").click();
        selectTag(page.locator("sakai-add-topic sakai-tag-selector"), globalLabel);
        selectTag(page.locator("sakai-add-topic sakai-tag-selector"), localLabel);

        String topicTitle = "Shared catalog topic " + suffix;
        page.locator("#summary").fill(topicTitle);
        Locator editor = page.locator("#topic-details-editor").frameLocator("iframe.cke_wysiwyg_frame")
            .locator("body[contenteditable='true']");
        editor.pressSequentially("A topic using global and site collections.");
        editor.press("Tab");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Publish").setExact(true)).click();
        assertThat(page.locator(".conversations-topic__title")).hasText(topicTitle);
        assertThat(page.locator("sakai-topic:visible .topic-tags")).containsText(globalLabel);
        assertThat(page.locator("sakai-topic:visible .topic-tags")).containsText(localLabel);

        sakai.toolClick("Tests");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Question Pools").setExact(true)).click();
        page.locator("#questionpool\\:add").click();
        selectTag(page.locator("#tag-selector"), globalLabel);

        String otherUrl = sakai.createProject("instructor1", List.of("sakai\\.tagservice"));
        page.navigate(otherUrl);
        sakai.toolClick("Tags");
        createCollection(collectionName);
        collectionRow(collectionName, "Site collection").getByRole(AriaRole.LINK,
            new Locator.GetByRoleOptions().setName("View Tags").setExact(true)).click();
        createTag(otherLabel);
        String otherSiteId = sakai.siteIdFromUrl(otherUrl);
        APIResponse otherCatalog = context.request().get("/api/sites/" + otherSiteId + "/tools/assignments/tags/" + otherSiteId);
        assertTrue(otherCatalog.ok(), otherCatalog.text());
        assertTrue(otherCatalog.text().contains(globalLabel));
        assertTrue(otherCatalog.text().contains(otherLabel));
        assertFalse(otherCatalog.text().contains(localLabel));
        APIResponse search = context.request().get("/direct/tagservice/getTagsPaginatedByPrefixInLabel.json?siteId="
            + URLEncoder.encode(siteId, StandardCharsets.UTF_8) + "&prefix=");
        assertTrue(search.ok(), search.text());
        assertTrue(search.text().contains(globalLabel));
        assertTrue(search.text().contains(localLabel));
        assertFalse(search.text().contains(otherLabel));
    }

    private Locator collectionRow(String name, String scope) {
        Locator row = page.locator(".tagservice-table tbody tr").filter(new Locator.FilterOptions().setHasText(name))
            .filter(new Locator.FilterOptions().setHasText(scope));
        Locator pager = page.locator("sakai-pager");
        Locator nextPage = pager.getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName("Next page").setExact(true));
        int pageCount = pager.count() > 0 ? Integer.parseInt(pager.getAttribute("count")) : 1;
        for (int attempt = 1; attempt < pageCount && row.count() == 0; attempt++) {
            int current = Integer.parseInt(pager.getAttribute("current"));
            if (current >= pageCount) {
                break;
            }
            int next = current + 1;
            String nextUrl = pager.getAttribute("data-page-base") + next + "/" + pager.getAttribute("data-page-size");
            nextPage.click();
            page.waitForURL(nextUrl);
        }
        return row;
    }

    private void createCollection(String name) {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Create Tag Collection").setExact(true)).click();
        page.locator("#name").fill(name);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save Tag Collection").setExact(true)).click();
        assertThat(collectionRow(name, "").first()).isVisible();
    }

    private void createTag(String label) {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Create Tag").setExact(true)).click();
        page.locator("#tagLabel").fill(label);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save Tag").setExact(true)).click();
        assertThat(page.locator(".tagservice-table")).containsText(label);
    }

    private void selectTag(Locator selector, String label) {
        selector.getByRole(AriaRole.COMBOBOX).fill(label);
        selector.getByRole(AriaRole.COMBOBOX).press("ArrowDown");
        selector.getByRole(AriaRole.COMBOBOX).press("Enter");
        assertThat(selector.getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName("Deselect: " + label).setExact(true))).isVisible();
    }
}
