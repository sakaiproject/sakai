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
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.microsoft.playwright.Locator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class SiteInfoTest extends SakaiUiTestBase {

    private static String sakaiUrl;

    @Test
    void canOpenManageGroupsHelper() {
        sakai.login("instructor1");
        page.navigate(ensureCourseUrl());
        sakai.toolClick("Site Info");

        assertNoTemplateRenderingError();
        assertThat(page.locator("body")).containsText(Pattern.compile("Site Information|Site Info", Pattern.CASE_INSENSITIVE));

        Locator manageGroups = page.locator(".navIntraTool a")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Manage Groups$", Pattern.CASE_INSENSITIVE)))
            .first();
        assertThat(manageGroups).isVisible();
        manageGroups.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();

        assertNoTemplateRenderingError();
        assertThat(page.locator("body")).containsText(Pattern.compile("Group List|No groups", Pattern.CASE_INSENSITIVE));

        Locator createGroup = page.locator(".navIntraTool a")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Create New Group$", Pattern.CASE_INSENSITIVE)))
            .first();
        assertThat(createGroup).isVisible();
        createGroup.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();

        assertNoTemplateRenderingError();
        assertThat(page.locator("#creategroup-form")).isVisible();
        assertThat(page.locator("#groupTitle")).isVisible();
        assertThat(page.locator("#groupMembers")).isVisible();
    }

    @Test
    void canAddParticipantWithThymeleafWizard() {
        sakai.login("instructor1");
        page.navigate(ensureCourseUrl());
        sakai.toolClick("Site Info");

        Locator addParticipants = page.locator(".navIntraTool a")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Add Participants$", Pattern.CASE_INSENSITIVE)))
            .first();
        assertThat(addParticipants).isVisible();
        addParticipants.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();

        assertNoTemplateRenderingError();
        assertThat(page.locator("#participant-helper")).isVisible();
        assertThat(page.locator("nav[aria-label=\"Add participants progress\"] .breadcrumb-item").last()).hasText("Finish");
        page.locator("#officialAccountParticipant").fill("instructor1");
        page.locator("#participant-helper form").first().locator("button[type=\"submit\"]").first().click();
        page.waitForLoadState();
        Locator existingParticipantMessage = page.locator("#participant-helper .sak-banner-warn")
                .filter(new Locator.FilterOptions().setHasText("instructor1"));
        assertThat(existingParticipantMessage).containsText("instructor1");
        assertThat(existingParticipantMessage).not().containsText("[Ljava.lang.Object;");
        page.locator("#officialAccountParticipant").fill("instructor1\nstudent0011\nstudent0011");
        page.locator("#participant-helper form").first().locator("button[type=\"submit\"]").first().click();
        page.waitForLoadState();

        assertThat(existingParticipantMessage).isVisible();
        assertThat(existingParticipantMessage).containsText("instructor1");
        assertThat(page.locator("#participant-helper")).not()
                .containsText("You may continue adding the other participants by following the directions below.");

        page.locator("#different-role").check(new Locator.CheckOptions().setForce(true));
        page.locator("#participant-helper form").first().locator("button[type=\"submit\"]").first().click();
        page.waitForLoadState();
        assertThat(page.locator("#participant-helper .sak-banner-error")).isVisible();
        assertThat(page.locator("#participant-role-0")).hasValue("");

        page.locator("#same-role").check(new Locator.CheckOptions().setForce(true));
        Locator sameRole = page.locator("input[name=\"sameRoleChoice\"][value=\"maintain\"]");
        assertThat(sameRole).isVisible();
        sameRole.check(new Locator.CheckOptions().setForce(true));
        assertThat(page.locator("#same-role-participants")).not().isVisible();
        page.locator("#participant-helper form").first().locator("button[type=\"submit\"]").first().click();
        page.waitForLoadState();

        Locator sendEmail = page.locator("#send-email");
        assertThat(sendEmail).isVisible();
        sendEmail.check(new Locator.CheckOptions().setForce(true));
        page.locator("#participant-helper form button")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Back$", Pattern.CASE_INSENSITIVE)))
            .click();
        page.waitForLoadState();

        page.locator("#participant-helper form").first().locator("button[type=\"submit\"]").first().click();
        page.waitForLoadState();
        assertThat(page.locator("#send-email")).isChecked();

        Locator dontSend = page.locator("#dont-send-email");
        dontSend.check(new Locator.CheckOptions().setForce(true));
        assertThat(page.locator("#participant-helper")).containsText("student0011");
        page.locator("#participant-helper form").first().locator("button[type=\"submit\"]").first().click();
        page.waitForLoadState();

        assertNoTemplateRenderingError();
        assertThat(page.locator("body")).containsText("Manage Participants");
        assertThat(page.locator("body")).containsText("student0011");
    }

    @Test
    void canCancelParticipantWizard() {
        sakai.login("instructor1");
        page.navigate(ensureCourseUrl());
        sakai.toolClick("Site Info");

        Locator addParticipants = page.locator(".navIntraTool a")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Add Participants$", Pattern.CASE_INSENSITIVE)))
            .first();
        addParticipants.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();

        page.locator("#participant-helper button")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Cancel$", Pattern.CASE_INSENSITIVE)))
            .click();
        page.waitForLoadState();

        assertThat(page.locator("body")).containsText(Pattern.compile("Site Information|Site Info", Pattern.CASE_INSENSITIVE));
    }

    @ParameterizedTest
    @ValueSource(strings = {"merge", "replace"})
    void canSearchImportSitesWithoutLosingSelection(String mode) {
        sakai.login("instructor1");
        // A different tool set gives this source its own site in the helper cache.
        sakai.createProject("instructor1", List.of("sakai\\.resources"));
        page.navigate(ensureCourseUrl());
        sakai.toolClick("Site Info");
        page.locator(".navIntraTool a").filter(new Locator.FilterOptions()
            .setHasText("Import from Site")).click();
        page.locator("a.siteimport-method-link").filter(new Locator.FilterOptions()
            .setHasText("I would like to " + mode + " my data")).click();

        Locator mySites = page.locator("#import-my-sites");
        assertThat(mySites).hasAttribute("open", "");
        assertThat(page.locator("#import-templates[open], #import-hidden-sites[open]")).hasCount(0);
        Locator row = mySites.locator("[data-import-site]").first();
        String title = row.locator("[data-import-title]").innerText();
        Locator selection = row.locator("input[name=importSites]");
        assertThat(selection).hasAttribute("type", "merge".equals(mode) ? "checkbox" : "radio");
        selection.check();

        Locator summary = mySites.locator("summary");
        summary.focus();
        summary.press("Space");
        assertThat(row).not().isVisible();

        Locator search = page.getByLabel("Search site titles");
        search.fill("  " + title.toUpperCase(Locale.ROOT) + "  ");
        assertThat(row).isVisible();
        assertThat(selection).isChecked();
        search.press("Enter");
        assertThat(search).isVisible();

        search.fill("no-site-matches-52645");
        assertThat(row).not().isVisible();
        assertThat(page.locator("#import-no-matches")).isVisible();
        assertThat(selection).isChecked();
        search.fill("");
        assertThat(row).isVisible();
        assertThat(page.locator("#import-no-matches")).not().isVisible();
        assertThat(selection).isChecked();

        // Continue while the selected row is filtered out: the source must still submit.
        search.fill("no-site-matches-52645");
        page.locator("input[name=eventSubmit_doContinue]").click();
        assertThat(page.locator("form[name=importSitesForm]")).isVisible();
        assertThat(page.locator("body")).containsText(title);
    }

    private String ensureCourseUrl() {
        if (sakaiUrl == null || sakaiUrl.isBlank()) {
            sakaiUrl = sakai.createProject("instructor1", List.of("sakai\\.announcements"));
        }
        return sakaiUrl;
    }

    private void assertNoTemplateRenderingError() {
        String bodyText = page.locator("body").textContent();
        Pattern templateError = Pattern.compile(
            "TemplateProcessingException|TemplateOutputException|An error happened during template rendering",
            Pattern.CASE_INSENSITIVE
        );
        assertFalse(templateError.matcher(bodyText == null ? "" : bodyText).find(), "Manage Groups rendered a Thymeleaf error");
    }
}
