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

class ResourcesOptionsTest extends SakaiUiTestBase {

    @Test
    void resourcesHideEmptyOptionsWhileDropboxKeepsItsSettings() {
        sakai.login("instructor1");
        sakai.createCourse("instructor1", List.of("sakai\\.resources", "sakai\\.dropbox"));
        sakai.toolClick("Resources");

        Locator menu = page.locator("#contentMenu");
        Locator options = menu.getByRole(AriaRole.LINK,
            new Locator.GetByRoleOptions().setName("Options").setExact(true));
        assertThat(menu.getByRole(AriaRole.LINK,
            new Locator.GetByRoleOptions().setName("Permissions").setExact(true))).isVisible();
        assertThat(options).hasCount(0);

        sakai.toolClick("Drop Box");
        assertThat(options).isVisible();
        options.click();
        assertThat(page.locator("#optionsForm")).isVisible();
        assertThat(page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Update").setExact(true))).isVisible();
        page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Cancel").setExact(true)).click();
        assertThat(options).isVisible();

        sakai.toolClick("Resources");
        assertThat(options).hasCount(0);
    }
}
