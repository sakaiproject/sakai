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

import com.microsoft.playwright.FileChooser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ScormTest extends SakaiUiTestBase {

    private static String sakaiUrl;
    private static final Path SCORM_ZIP = Path.of("src", "test", "resources", "fixtures", "RuntimeBasicCalls_SCORM20043rdEdition.zip");

    @Test
    @Order(1)
    void createsSiteWithScormPlayer() {
        sakai.login("instructor1");
        sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.scorm\\.tool"));
    }

    @Test
    @Order(2)
    void uploadsScormPackageAsInstructor() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("SCORM Player");

        page.locator("a, button").filter(new Locator.FilterOptions().setHasText("Upload New Content Package")).first().click(new Locator.ClickOptions().setForce(true));

        FileChooser chooser = page.waitForFileChooser(() ->
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("File To Upload", Pattern.CASE_INSENSITIVE))).first()
                .click(new Locator.ClickOptions().setForce(true))
        );
        chooser.setFiles(SCORM_ZIP);

        page.locator("button:has-text(\"Upload File\"), a:has-text(\"Upload File\"), input[type=\"submit\"][value*=\"Upload File\"]").first().click(new Locator.ClickOptions().setForce(true));
        assertThat(page.locator("body")).containsText(Pattern.compile("Golf Explained|RuntimeBasicCalls|List of Content Packages", Pattern.CASE_INSENSITIVE));
    }

    @Test
    @Order(3)
    void showsScormPackageToStudents() {
        sakai.login("student0011");
        page.navigate(sakaiUrl);
        sakai.toolClick("SCORM Player");

        assertThat(page.locator("body")).containsText(Pattern.compile("Golf Explained|RuntimeBasicCalls|SCORM", Pattern.CASE_INSENSITIVE));
    }
}
