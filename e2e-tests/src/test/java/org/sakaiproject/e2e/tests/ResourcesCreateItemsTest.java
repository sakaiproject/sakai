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
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.RequestOptions;
import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiHelper.TestUser;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/** Creating web links, HTML pages and text documents, and renaming items, from the community Resources regression script. */
class ResourcesCreateItemsTest extends SakaiUiTestBase {

    private static final String FOLDER = "Creations";
    private static final String STUDENT = "student0011";
    private static final String APEREO = "https://www.apereo.org/";
    private static final String SAKAI = "https://www.sakailms.org/";

    @Test
    void webLinksNeedNameAndAddressAndRedirectEachAudienceByItsAccess() {
        ResourcesUi resources = new ResourcesUi(page, sakai);
        sakai.login("instructor1");
        String siteUrl = sakai.createFreshCourse("instructor1", List.of("sakai\\.resources"));
        sakai.addMember(siteUrl, STUDENT, "Student");
        openFolder(resources, siteUrl);

        resources.itemAction(resources.rootRow(), "Add Web Links (URLs)");
        Locator addLinks = page.locator("#saveChanges");
        assertThat(addLinks).isDisabled();
        // The inputs render before the script that installs the handlers enabling the submit button.
        page.waitForFunction("document.getElementById('content_0').oninput !== null");
        page.locator("#displayName_0").fill("Apereo");
        assertThat(addLinks).isDisabled();
        page.locator("#content_0").fill(APEREO);
        assertThat(addLinks).isEnabled();
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Add Another Web Link").setExact(true)).click();
        page.locator("#displayName_1").fill("Sakai");
        assertThat(addLinks).isDisabled();
        page.locator("#content_1").fill(SAKAI);
        assertThat(addLinks).isEnabled();
        addLinks.click();

        for (String name : List.of("Apereo", "Sakai")) {
            assertThat(resources.itemLink(name)).hasAttribute("target", "_blank");
        }
        assertEquals(APEREO, redirectLocation(resources.accessUrl("Apereo")));
        assertEquals(SAKAI, redirectLocation(resources.accessUrl("Sakai")));

        // A site-only link: members are redirected to the address, other users are not.
        resources.itemAction(resources.row("Apereo"), "Edit Details");
        assertThat(page.locator("#access_mode_inherited_0")).isChecked();
        String siteOnly = page.locator("#fileURLHolder").inputValue();
        page.locator("#cancel_button").click();

        // A public link redirects anonymous users too.
        resources.itemAction(resources.row("Sakai"), "Edit Details");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Availability and Access").setExact(true)).click();
        page.locator("[id='access_role_.anon_0']").check();
        String publicLink = page.locator("#fileURLHolder").inputValue();
        page.locator("#finish_button").click();
        assertThat(resources.row("Sakai").locator(".resource-access")).hasText("Public");

        try (TestUser outsider = sakai.createUser("resources-link-outsider");
             ResourcesRequests anonymous = requests(null);
             ResourcesRequests member = requests(STUDENT);
             ResourcesRequests nonMember = requests(outsider.username())) {
            assertEquals(APEREO, member.get(siteOnly).location(), "a member follows a site-only link");
            assertEquals(ResourcesRequests.FORBIDDEN, nonMember.get(siteOnly).status(), "SAK-44704: a non-member gets 403");
            assertTrue(anonymous.get(siteOnly).isLoginPrompt(), "an anonymous user must log in");
            assertEquals(SAKAI, anonymous.get(publicLink).location(), "an anonymous user follows a public link");
            assertEquals(SAKAI, nonMember.get(publicLink).location(), "a non-member follows a public link");
        }
    }

    @Test
    void htmlPageAndTextDocumentAreCreatedRenamedAndServed() {
        ResourcesUi resources = new ResourcesUi(page, sakai);
        sakai.login("instructor1");
        openFolder(resources, sakai.createFreshCourse("instructor1", List.of("sakai\\.resources")));

        resources.itemAction(resources.rootRow(), "Create HTML Page");
        sakai.typeCkEditor("contenttext", "<p>Hello from Resources</p>");
        continueToDetails("new_page.html", "welcome.html");
        assertThat(resources.itemLink("welcome.html")).hasAttribute("target", "_blank");
        assertTrue(read(resources.accessUrl("welcome.html")).contains("Hello from Resources"));

        // The short URL leads to the same page.
        resources.itemAction(resources.row("welcome.html"), "Edit Details");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("File Details").setExact(true)).click();
        page.locator("#doShortUrl").check();
        Locator url = page.locator("#fileURLHolder");
        assertThat(url).hasValue(Pattern.compile("/x/"));
        String shortPath = URI.create(url.inputValue().trim()).getPath();
        page.locator("#finish_button").click();
        assertTrue(read(shortPath).contains("Hello from Resources"), "Short URL " + shortPath + " does not lead to the page");

        resources.itemAction(resources.rootRow(), "Create Text Document");
        // The portal's <main> also has the id "content".
        page.locator("textarea#content").fill("Plain text from Resources");
        continueToDetails("new_document.txt", "notes.txt");
        assertThat(resources.itemLink("notes.txt")).hasAttribute("target", "_blank");
        assertEquals("Plain text from Resources", read(resources.accessUrl("notes.txt")));
    }

    @Test
    void folderAndFileDisplayNamesCanBeChanged() {
        ResourcesUi resources = new ResourcesUi(page, sakai);
        sakai.login("instructor1");
        String siteUrl = sakai.createFreshCourse("instructor1", List.of("sakai\\.resources"));
        page.navigate(siteUrl);
        sakai.toolClick("Resources");
        resources.openCreateFolders(resources.rootRow(), List.of("Overwritten"));
        resources.createFolders();
        resources.uploadText("Overwritten", "draft.txt", "Draft content");

        resources.itemAction(resources.row("Overwritten"), "Edit Details");
        page.locator("#displayName_0").fill("Updated");
        page.locator("#finish_button").click();
        assertThat(resources.itemLink("Updated")).isVisible();
        assertThat(resources.itemLink("Overwritten")).hasCount(0);

        resources.openFolder("Updated");
        resources.itemAction(resources.row("draft.txt"), "Edit Details");
        page.locator("#displayName_0").fill("final.txt");
        page.locator("#finish_button").click();
        assertThat(resources.itemLink("final.txt")).isVisible();
        assertThat(resources.itemLink("draft.txt")).hasCount(0);
        assertEquals("Draft content", read(resources.accessUrl("final.txt")));
    }

    private void openFolder(ResourcesUi resources, String siteUrl) {
        page.navigate(siteUrl);
        sakai.toolClick("Resources");
        resources.openCreateFolders(resources.rootRow(), List.of(FOLDER));
        resources.createFolders();
        resources.openFolder(FOLDER);
    }

    /** Continues from the content editor to the details page and finishes with another file name. */
    private void continueToDetails(String defaultName, String name) {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Continue").setExact(true)).click();
        Locator displayName = page.locator("#displayName_0");
        assertThat(displayName).hasValue(defaultName);
        displayName.fill(name);
        page.locator("#finish_button").click();
    }

    private String redirectLocation(String url) {
        APIResponse response = page.request().get(url, RequestOptions.create().setMaxRedirects(0));
        try {
            assertEquals(ResourcesRequests.REDIRECT, response.status(), "Web link " + url + " does not redirect");
            return response.headers().getOrDefault("location", "");
        } finally {
            response.dispose();
        }
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

    private ResourcesRequests requests(String username) {
        return new ResourcesRequests(context.browser(), sakai, username);
    }
}
