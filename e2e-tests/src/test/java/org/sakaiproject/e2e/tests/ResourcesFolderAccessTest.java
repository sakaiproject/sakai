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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.sakaiproject.e2e.support.SakaiHelper.TestUser;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/**
 * Folder availability and access settings from the community Resources regression script:
 * the access label shown to the instructor, the HTTP access each audience gets to a file
 * inside the folder, and which folders a group member sees in the list.
 */
class ResourcesFolderAccessTest extends SakaiUiTestBase {

    private static final String GROUP_ONE_STUDENT = "student0011";
    private static final String GROUP_TWO_STUDENT = "student0012";
    private static final String FILE_NAME = "file.txt";

    private static final int OK = ResourcesRequests.OK;
    private static final int LOGIN = ResourcesRequests.REDIRECT;
    private static final int FORBIDDEN = ResourcesRequests.FORBIDDEN;

    /** Expected list label and HTTP status for: anonymous, Group1 student, Group2 student, logged-in non-member. */
    private record FolderCase(String name, String accessLabel, int anonymous, int groupOne, int groupTwo, int outsider) {
    }

    private static final List<FolderCase> CASES = List.of(
        new FolderCase("Site Folder", "Entire site", LOGIN, OK, OK, FORBIDDEN),
        new FolderCase("Public Folder", "Public", OK, OK, OK, OK),
        new FolderCase("Group1", "Select group(s)", LOGIN, OK, FORBIDDEN, FORBIDDEN),
        new FolderCase("Group2", "Select group(s)", LOGIN, FORBIDDEN, OK, FORBIDDEN),
        new FolderCase("Hidden-Access", "Entire site (Hidden)", LOGIN, OK, OK, FORBIDDEN),
        new FolderCase("Hidden", "Entire site (Hidden)", LOGIN, FORBIDDEN, FORBIDDEN, FORBIDDEN),
        new FolderCase("Future", "Entire site (Hidden)", LOGIN, FORBIDDEN, FORBIDDEN, FORBIDDEN),
        new FolderCase("Limit-Dates", "Entire site (Hidden)", LOGIN, FORBIDDEN, FORBIDDEN, FORBIDDEN),
        new FolderCase("Until", "Entire site", LOGIN, OK, OK, FORBIDDEN),
        new FolderCase("Weiß", "Entire site", LOGIN, OK, OK, FORBIDDEN)
    );

    @Test
    void folderAccessSettingsAreLabelledAndEnforcedForEachAudience() {
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
        resources.openCreateFolders(resources.rootRow(), CASES.stream().map(FolderCase::name).toList());
        LocalDateTime now = LocalDateTime.now();
        page.locator("[id='access_role_.anon_1']").check();
        resources.restrictToGroup(2, groupOne);
        resources.restrictToGroup(3, groupTwo);
        page.locator("#hidden_with_accessible_content_true_4").check();
        page.locator("#hidden_true_5").check();
        resources.setStartDate(6, now.plusDays(2));
        resources.setStartDate(7, now.plusDays(2));
        resources.setEndDate(7, now.plusDays(9));
        // Rows added with "Add Another Folder" default the Until date to now, so always set it explicitly.
        resources.setEndDate(8, now.plusDays(7));
        resources.createFolders();

        for (FolderCase folder : CASES) {
            assertThat(resources.row(folder.name()).locator(".resource-access")).hasText(folder.accessLabel());
        }
        assertThat(resources.row("Group1").locator("td.contentAccess"))
            .hasAttribute("title", "Visible to select groups: Group1");

        for (FolderCase folder : CASES) {
            resources.uploadText(folder.name(), FILE_NAME, "Content of " + folder.name());
        }

        try (TestUser outsider = sakai.createUser("resources-outsider");
             ResourcesRequests anonymous = requests(null);
             ResourcesRequests groupOneStudent = requests(GROUP_ONE_STUDENT);
             ResourcesRequests groupTwoStudent = requests(GROUP_TWO_STUDENT);
             ResourcesRequests nonMember = requests(outsider.username())) {
            List<String> failures = new ArrayList<>();
            for (FolderCase folder : CASES) {
                String path = ResourcesUi.accessPath(siteId, folder.name() + "/" + FILE_NAME);
                expectStatus(failures, folder.name(), "anonymous", anonymous.status(path), folder.anonymous());
                expectStatus(failures, folder.name(), GROUP_ONE_STUDENT, groupOneStudent.status(path), folder.groupOne());
                expectStatus(failures, folder.name(), GROUP_TWO_STUDENT, groupTwoStudent.status(path), folder.groupTwo());
                expectStatus(failures, folder.name(), "non-member", nonMember.status(path), folder.outsider());
            }
            assertTrue(failures.isEmpty(), "Unexpected access:\n" + String.join("\n", failures));
        }

        // A folder hidden with accessible content stays reachable by URL but is not listed.
        sakai.login(GROUP_ONE_STUDENT);
        page.navigate(siteUrl);
        sakai.toolClick("Resources");
        assertThat(resources.rootRow()).isVisible();
        for (FolderCase folder : CASES) {
            boolean listed = folder.groupOne() == OK && !"Hidden-Access".equals(folder.name());
            assertThat(resources.row(folder.name())).hasCount(listed ? 1 : 0);
        }
    }

    @Test
    @EnabledIfSystemProperty(named = "resources.test.authRole", matches = "true")
    void loggedInUsersFolderRequiresLoginButNotMembership() {
        // Requires resources.enabled.roles=.anon,.auth on the server.
        ResourcesUi resources = new ResourcesUi(page, sakai);
        sakai.login("instructor1");
        String siteUrl = sakai.createFreshCourse("instructor1", List.of("sakai\\.resources"));
        String siteId = sakai.siteIdFromUrl(siteUrl);

        page.navigate(siteUrl);
        sakai.toolClick("Resources");
        resources.openCreateFolders(resources.rootRow(), List.of("Logged-In"));
        page.locator("[id='access_role_.auth_0']").check();
        resources.createFolders();
        assertThat(resources.row("Logged-In").locator(".resource-access")).hasText("Logged in users");
        resources.uploadText("Logged-In", FILE_NAME, "Content for logged in users");

        String path = ResourcesUi.accessPath(siteId, "Logged-In/" + FILE_NAME);
        try (TestUser outsider = sakai.createUser("resources-auth-outsider");
             ResourcesRequests anonymous = requests(null);
             ResourcesRequests nonMember = requests(outsider.username())) {
            assertEquals(LOGIN, anonymous.status(path), "anonymous user must be asked to log in");
            assertEquals(OK, nonMember.status(path), "logged-in non-member must see the file");
        }
    }

    private static void expectStatus(List<String> failures, String folder, String audience, int actual, int expected) {
        if (actual != expected) {
            failures.add(folder + " / " + audience + ": expected HTTP " + expected + " but got " + actual);
        }
    }

    /** Requests made with a separate browser session, signed in as the given user or anonymous for null. */
    private ResourcesRequests requests(String username) {
        return new ResourcesRequests(context.browser(), sakai, username);
    }
}
