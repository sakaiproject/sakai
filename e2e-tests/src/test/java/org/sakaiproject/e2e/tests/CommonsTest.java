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

import java.util.List;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CommonsTest extends SakaiUiTestBase {

    private static String sakaiUrl;

    @Test
    @Order(1)
    void canCreateANewCourse() {
        sakai.login("instructor1");
        sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.commons"));
    }

    @Test
    @Order(2)
    void canCreateCommonsPostAsStudent() {
        sakai.login("student0011");
        page.navigate(sakaiUrl);
        sakai.toolClick("Commons");

        page.locator("#commons-post-creator-editor").fill("This is a student test post");
        page.locator("#commons-editor-post-button").click();

        assertThat(page.locator("body")).containsText("This is a student test post");
    }

    @Test
    @Order(3)
    void canCreateCommonsPostAsInstructor() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Commons");

        assertThat(page.locator("body")).containsText("This is a student test post");

        page.locator("#commons-post-creator-editor").fill("This is a test post");
        page.locator("#commons-editor-post-button").click();

        assertThat(page.locator("body")).containsText("This is a test post");
    }

    @Test
    @Order(4)
    void canInsertAndPostALink() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Commons");

        page.locator("#commons-post-creator-editor").click();
        page.locator("#commons-editor-link-button").click();
        // Bootstrap focuses the dialog after its opening transition.
        assertThat(page.locator("#commons-link-dialog")).isFocused();
        assertThat(page.locator("#commons-link-dialog-load-thumbnail")).hasCount(0);
        page.locator("#commons-link-dialog-text").fill("Sakai project");
        page.locator("#commons-link-dialog-url").pressSequentially("https://www.sakaiproject.org/");
        assertThat(page.locator("#commons-link-dialog-url")).hasValue("https://www.sakaiproject.org/");
        page.locator("#commons-link-dialog-insert-button").click();

        assertThat(page.locator("#commons-link-dialog")).isHidden();
        assertThat(page.locator("#commons-post-creator-editor a")).hasText("Sakai project");
        assertThat(page.locator("#commons-post-creator-editor a")).hasAttribute("href", "https://www.sakaiproject.org/");
        page.locator("#commons-editor-post-button").click();

        assertThat(page.locator("#commons-posts a[href='https://www.sakaiproject.org/']")).hasText("Sakai project");
        page.reload();
        assertThat(page.locator("#commons-posts a[href='https://www.sakaiproject.org/']")).hasText("Sakai project");
    }

}
