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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SiteStatsTest extends SakaiUiTestBase {

    private static String sakaiUrl;
    private static final String REPORT_TITLE = "Playwright Report " + System.currentTimeMillis();
    private static final String REPORT_DESC = "This is a Playwright-generated SiteStats report.";
    private static final String SAVED_REPORT_TITLE = REPORT_TITLE + " Saved";

    @Test
    @Order(1)
    void createsSiteWithStatistics() {
        sakai.login("instructor1");
        sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.sitestats"));
    }

    @Test
    @Order(2)
    void overviewWidgetsRenderThroughJsonPanels() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Statistics");

        Locator reportPanel = page.locator("sakai-sitestats-report-panel:visible").first();
        assertThat(reportPanel).isVisible();
        assertThat(reportPanel).hasAttribute("endpoint", Pattern.compile("/api/sites/.*/sitestats/widgets/.*/tabs/.*"));
        assertThat(page.locator("sakai-sitestats-report-panel:visible sakai-sitestats-table table").first()).isVisible();
        assertNoLegacyReportChartImages();
    }

    @Test
    @Order(3)
    void createsReportViaReportsFlow() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Statistics");

        page.locator(".navIntraTool a, .navIntraTool button, a, button")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Reports$", Pattern.CASE_INSENSITIVE))).first()
            .click(new Locator.ClickOptions().setForce(true));

        Locator addReportLink = page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Add report$", Pattern.CASE_INSENSITIVE))).first();
        addReportLink.click(new Locator.ClickOptions().setForce(true));

        page.getByLabel("Title").fill(REPORT_TITLE);
        page.getByLabel("Description").fill(REPORT_DESC);
        page.getByLabel(Pattern.compile("Presentation", Pattern.CASE_INSENSITIVE)).selectOption("how-presentation-both");

        page.locator("button:has-text(\"Generate report\"), input[type=\"submit\"][value*=\"Generate report\"]").first().click(new Locator.ClickOptions().setForce(true));
        assertThat(page.getByText(REPORT_TITLE).first()).isVisible();
        Locator reportPanel = page.locator("sakai-sitestats-report-panel").first();
        assertThat(reportPanel).isVisible();
        assertThat(reportPanel).hasAttribute("endpoint", Pattern.compile("/api/sites/.*/sitestats/"));
        assertReportSummaryRendered();
        assertReportTableRenderedWithData();
        assertThat(page.locator("sakai-sitestats-report-panel sakai-sitestats-chart canvas").first()).isVisible();
        page.waitForFunction(siteStatsCanvasHasPixelsScript());
        assertTrue(Boolean.TRUE.equals(page.evaluate(siteStatsCanvasHasPixelsScript())));
        assertNoLegacyReportChartImages();
    }

    @Test
    @Order(4)
    void savesEditsCopiesExportsAndDeletesReport() {
        openReportsAsInstructor();
        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Add report$", Pattern.CASE_INSENSITIVE))).click();
        page.getByLabel("Title").fill(SAVED_REPORT_TITLE);
        page.getByLabel("Description").fill(REPORT_DESC);
        page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Save report$", Pattern.CASE_INSENSITIVE))).click();

        assertThat(page.getByText("The report was saved.")).isVisible();
        assertThat(page.getByRole(AriaRole.HEADING,
            new Page.GetByRoleOptions().setName(SAVED_REPORT_TITLE))).isVisible();
        Download csv = page.waitForDownload(() -> page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile("Export CSV", Pattern.CASE_INSENSITIVE))).click());
        assertTrue(csv.suggestedFilename().endsWith(".csv"));

        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile("Back to reports", Pattern.CASE_INSENSITIVE))).click();
        Locator savedRow = page.getByRole(AriaRole.ROW).filter(new Locator.FilterOptions().setHasText(SAVED_REPORT_TITLE));
        savedRow.getByRole(AriaRole.LINK,
            new Locator.GetByRoleOptions().setName(Pattern.compile("Edit report", Pattern.CASE_INSENSITIVE))).click();
        page.getByLabel("Title").fill(SAVED_REPORT_TITLE + " Edited");
        page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Save report$", Pattern.CASE_INSENSITIVE))).click();
        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile("Back to reports", Pattern.CASE_INSENSITIVE))).click();

        Locator editedRow = page.getByRole(AriaRole.ROW)
            .filter(new Locator.FilterOptions().setHasText(SAVED_REPORT_TITLE + " Edited"));
        editedRow.getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName(Pattern.compile("Copy report", Pattern.CASE_INSENSITIVE))).click();
        assertThat(page.getByText("The report was copied.")).isVisible();
        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile("Back to reports", Pattern.CASE_INSENSITIVE))).click();

        Locator deleteButtons = page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName(Pattern.compile("Delete report", Pattern.CASE_INSENSITIVE)));
        assertThat(deleteButtons).hasCount(2);
        deleteButtons.first().click();
        page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName(Pattern.compile("Delete report", Pattern.CASE_INSENSITIVE))).click();
        assertThat(page.getByText("No reports are available.")).isVisible();
    }

    @Test
    @Order(5)
    void preferencesAndUserActivityUseSpringForms() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Statistics");

        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Preferences$", Pattern.CASE_INSENSITIVE))).click();
        page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Update$", Pattern.CASE_INSENSITIVE))).click();
        assertThat(page.getByText("The preferences were saved.")).isVisible();

        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile("^User Activity$", Pattern.CASE_INSENSITIVE))).click();
        assertThat(page.locator("input[type=date]")).hasCount(2);
        Locator users = page.getByLabel("User");
        if (users.locator("option").count() > 1) {
            users.selectOption(new SelectOption().setIndex(1));
        }
        page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Search$", Pattern.CASE_INSENSITIVE))).click();
        assertTrue(page.url().contains("startDate=") && page.url().contains("endDate="));
    }

    @Test
    @Order(6)
    void adminRegistrationRendersServerWideReports() {
        sakai.login("admin");
        sakai.gotoPath("/portal/site/!admin/tool/!admin-1225");

        assertThat(page.getByRole(AriaRole.HEADING,
            new Page.GetByRoleOptions().setName(Pattern.compile("All sites", Pattern.CASE_INSENSITIVE)))).isVisible();
        Locator siteLinks = page.locator("table tbody a[href*='serverwide']");
        assertTrue(siteLinks.count() > 0);
        siteLinks.first().click();
        assertThat(page.locator("sakai-sitestats-report-panel")).isVisible();
        assertThat(page.locator("sakai-sitestats-report-panel sakai-sitestats-table table")).isVisible();
        assertNoLegacyReportChartImages();
    }

    private void openReportsAsInstructor() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Statistics");
        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Reports$", Pattern.CASE_INSENSITIVE))).click();
    }

    private void assertReportSummaryRendered() {
        Locator summary = page.locator("sakai-sitestats-report-panel dl.summary").first();
        assertThat(summary).isVisible();
        assertThat(summary).containsText(REPORT_DESC);
        assertThat(summary).containsText(Pattern.compile("Site|Generated", Pattern.CASE_INSENSITIVE));
    }

    private void assertReportTableRenderedWithData() {
        Locator table = page.locator("sakai-sitestats-report-panel sakai-sitestats-table table").first();
        assertThat(table).isVisible();
        assertTrue(table.locator("tbody tr").count() > 0);
        assertThat(table).not().containsText(Pattern.compile("No data available", Pattern.CASE_INSENSITIVE));
    }

    private void assertNoLegacyReportChartImages() {
        assertThat(page.locator("sakai-sitestats-report-panel img, .chartContainer img")).hasCount(0);
    }

    private String siteStatsCanvasHasPixelsScript() {
        return "() => {"
            + " const panels = Array.from(document.querySelectorAll('sakai-sitestats-report-panel'));"
            + " for (const panel of panels) {"
            + "   const chart = panel.shadowRoot && panel.shadowRoot.querySelector('sakai-sitestats-chart');"
            + "   const canvas = chart && chart.shadowRoot && chart.shadowRoot.querySelector('canvas');"
            + "   if (!canvas || !canvas.width || !canvas.height) { continue; }"
            + "   const context = canvas.getContext('2d');"
            + "   const data = context.getImageData(0, 0, canvas.width, canvas.height).data;"
            + "   for (let i = 3; i < data.length; i += 64) {"
            + "     if (data[i] !== 0) { return true; }"
            + "   }"
            + " }"
            + " return false;"
            + "}";
    }
}
