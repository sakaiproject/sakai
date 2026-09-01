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

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;
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

        Locator percentages = page.locator(".sitestats-metric-percentage");
        assertTrue(percentages.count() > 0);
        assertThat(percentages.first()).containsText("%");

        Locator widgetTab = page.locator(
            ".sitestats-widget-tab[endpoint*='/widgets/visits/tabs/bydate']");
        assertThat(widgetTab).hasCount(1);
        assertTrue(widgetTab.getAttribute("open") != null);
        widgetTab.getByLabel("Period:").selectOption("when-all");

        Locator reportPanel = widgetTab.locator("sakai-sitestats-report-panel");
        assertThat(reportPanel).isVisible();
        String reportEndpoint = (String) reportPanel.evaluate("panel => panel.endpoint");
        assertTrue(Pattern.compile("/api/sites/.*/sitestats/widgets/.*/tabs/.*date=when-all")
            .matcher(reportEndpoint).find());
        Locator table = reportPanel.locator("sakai-sitestats-table table");
        assertThat(table).isVisible();
        assertThat(reportPanel.locator("sakai-sitestats-chart")).hasCount(1);
        assertNoLegacyReportChartImages();
    }

    @Test
    @Order(9)
    void presenceAccessWidgetRendersThroughJsonPanel() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Statistics");

        Locator presenceTab = page.locator(
            ".sitestats-widget-tab[endpoint*='/widgets/presence-access/tabs/bydate']");
        assertThat(presenceTab).hasCount(1);
        presenceTab.locator("summary").click();

        Locator reportPanel = presenceTab.locator("sakai-sitestats-report-panel");
        assertThat(reportPanel).isVisible();
        Locator lastVisitMetric = page.locator(".sitestats-widget")
            .filter(new Locator.FilterOptions().setHas(presenceTab))
            .locator(".sitestats-metric").first();
        assertThat(lastVisitMetric).isVisible();
        assertNoLegacyReportChartImages();
    }

    @Test
    @Order(3)
    void reportValidationDisplaysOneErrorBanner() {
        openReportsAsInstructor();
        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Add$", Pattern.CASE_INSENSITIVE))).click();
        page.getByLabel("Title").fill(REPORT_TITLE);
        page.getByLabel("Period:").selectOption("when-custom");
        page.locator("#when-from").fill("");
        page.locator("#when-to").fill("");

        Locator validationError = page.locator(".sak-banner-error[role='alert']");
        for (String action : List.of("Save report", "Generate report")) {
            page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("^" + action + "$", Pattern.CASE_INSENSITIVE)))
                .click();
            assertThat(validationError).hasCount(1);
            assertThat(validationError).containsText("Custom time period not defined");
        }
    }

    @Test
    @Order(4)
    void createsReportViaReportsFlow() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Statistics");

        page.locator(".navIntraTool a, .navIntraTool button, a, button")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Reports$", Pattern.CASE_INSENSITIVE))).first()
            .click(new Locator.ClickOptions().setForce(true));

        Locator addReportLink = page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Add$", Pattern.CASE_INSENSITIVE))).first();
        addReportLink.click(new Locator.ClickOptions().setForce(true));

        Locator activity = page.getByLabel("Activity:");
        assertThat(activity.locator("option[value='what-resources']")).hasCount(0);
        assertThat(page.locator("#event-options")).isHidden();
        activity.selectOption("what-events");
        assertThat(page.locator("#event-options")).isVisible();
        assertThat(page.locator("#tool-selection")).isVisible();
        page.getByLabel("Selection:").selectOption(new SelectOption().setLabel("Select by event"));
        assertThat(page.locator("#tool-selection")).isHidden();
        assertThat(page.locator("#event-selection-list")).isVisible();

        assertThat(page.locator("#custom-dates")).isHidden();
        page.getByLabel("Period:").selectOption("when-custom");
        assertThat(page.locator("#custom-dates")).isVisible();
        assertThat(page.locator("#who-role-options")).isHidden();
        page.getByLabel("Users:").selectOption("who-role");
        assertThat(page.locator("#who-role-options")).isVisible();
        page.getByLabel("Users:").selectOption("who-custom");
        Locator userSearch = page.getByLabel("Search site users");
        assertThat(userSearch).isVisible();
        userSearch.fill("instructor1");
        Locator userResult = page.locator("#who-user-search-results button").first();
        assertThat(userResult).isVisible();
        userResult.click();
        assertThat(page.locator("#who-users option:checked")).hasCount(1);

        page.getByLabel("Specify sorting field").check();
        assertThat(page.locator("#sorting-options")).isVisible();
        page.getByLabel("Limit to:").check();
        assertThat(page.locator("#max-results-options")).isVisible();
        Locator maxResults = page.locator("#max-results");
        maxResults.fill("25");
        page.getByLabel("Limit to:").uncheck();
        assertThat(page.locator("#max-results-options")).isHidden();
        page.getByLabel("Limit to:").check();
        assertThat(maxResults).hasValue("25");
        page.getByLabel(Pattern.compile("Presentation", Pattern.CASE_INSENSITIVE)).selectOption("how-presentation-both");
        assertThat(page.locator("#chart-options")).isVisible();
        page.getByLabel("Chart type:").selectOption("timeseries");
        assertThat(page.locator("#chart-source-options")).isHidden();
        assertThat(page.locator("#chart-series-options")).isVisible();

        activity.selectOption("what-visits");
        page.getByLabel("Period:").selectOption("when-last7days");
        page.getByLabel("Users:").selectOption("who-all");
        page.getByLabel("Specify sorting field").uncheck();
        page.getByLabel("Limit to:").uncheck();
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
    @Order(5)
    void savesEditsCopiesExportsAndDeletesReport() {
        openReportsAsInstructor();
        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Add$", Pattern.CASE_INSENSITIVE))).click();
        assertThat(page.locator("#report-hidden")).hasCount(0);
        page.getByLabel("Title").fill(SAVED_REPORT_TITLE);
        page.getByLabel("Description").fill(REPORT_DESC);
        page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Save report$", Pattern.CASE_INSENSITIVE))).click();

        assertThat(page.getByRole(AriaRole.STATUS)).containsText(
            "Report '" + SAVED_REPORT_TITLE + "' saved successfully");
        assertThat(page.getByRole(AriaRole.HEADING,
            new Page.GetByRoleOptions().setName(SAVED_REPORT_TITLE))).isVisible();
        Download csv = page.waitForDownload(() -> page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile("Export CSV", Pattern.CASE_INSENSITIVE))).click());
        assertTrue(csv.suggestedFilename().endsWith(".csv"));

        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Back$", Pattern.CASE_INSENSITIVE))).click();
        Locator savedRow = page.getByRole(AriaRole.ROW).filter(new Locator.FilterOptions().setHasText(SAVED_REPORT_TITLE));
        savedRow.getByRole(AriaRole.LINK,
            new Locator.GetByRoleOptions().setName(Pattern.compile("^Edit$", Pattern.CASE_INSENSITIVE))).click();
        page.getByLabel("Title").fill(SAVED_REPORT_TITLE + " Edited");
        page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Save report$", Pattern.CASE_INSENSITIVE))).click();
        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Back$", Pattern.CASE_INSENSITIVE))).click();

        Locator editedRow = page.getByRole(AriaRole.ROW)
            .filter(new Locator.FilterOptions().setHasText(SAVED_REPORT_TITLE + " Edited"));
        editedRow.getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName(Pattern.compile("^Duplicate$", Pattern.CASE_INSENSITIVE))).click();
        assertThat(page.getByText("The report was copied.")).isVisible();
        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Back$", Pattern.CASE_INSENSITIVE))).click();

        Locator deleteButtons = page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Delete$", Pattern.CASE_INSENSITIVE)));
        assertThat(deleteButtons).hasCount(2);
        deleteButtons.first().click();
        page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Delete$", Pattern.CASE_INSENSITIVE))).click();
        assertThat(page.getByText("No reports defined.")).isVisible();
    }

    @Test
    @Order(6)
    void preferencesUseSpringForms() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Statistics");

        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Preferences$", Pattern.CASE_INSENSITIVE))).click();
        Locator preferenceTools = page.locator("fieldset").filter(
            new Locator.FilterOptions().setHas(page.locator("#all-tools"))).locator("h2");
        assertTrue(preferenceTools.count() > 0);
        for (int index = 0; index < preferenceTools.count(); index++) {
            assertTrue(!preferenceTools.nth(index).textContent().startsWith("sakai."));
        }
        Locator allTools = page.locator("#all-tools");
        Locator activityEventOptions = page.locator("#activity-event-options");
        allTools.check();
        assertThat(activityEventOptions).isHidden();
        allTools.uncheck();
        assertThat(activityEventOptions).isVisible();
        allTools.check();
        assertThat(activityEventOptions).isHidden();
        page.getByLabel("Show their own statistics to students").check();
        page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Update$", Pattern.CASE_INSENSITIVE))).click();
        assertThat(page.getByText("Preferences updated successfully.")).isVisible();
    }

    @Test
    @Order(7)
    void studentOwnStatisticsViewDoesNotRenderInstructorNavigation() {
        sakai.login("admin");
        String siteId = sakai.siteIdFromUrl(sakaiUrl);
        APIResponse permissionsResponse = page.request().post("/api/sites/" + siteId + "/permissions",
            RequestOptions.create().setForm(FormData.create()
                .set("ref", "/site/" + siteId)
                .set("Student:sitestats.view", "true")
                .set("Student:sitestats.own", "true")));
        assertTrue(permissionsResponse.ok(),
            "Unable to grant student access to Statistics: HTTP " + permissionsResponse.status());

        sakai.login("student0011");
        page.navigate(sakaiUrl);
        sakai.toolClick("Statistics");

        assertThat(page.getByRole(AriaRole.HEADING,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Overview$", Pattern.CASE_INSENSITIVE)))).isVisible();
        assertThat(page.getByRole(AriaRole.NAVIGATION,
            new Page.GetByRoleOptions().setName("SiteStats"))).hasCount(0);
        assertThat(page.locator(".sitestats-widget-tab[endpoint*='/widgets/student-presence-access/']"))
            .hasCount(1);
        assertThat(page.locator(".sitestats-widget-tab[endpoint*='/widgets/presence-access/tabs/']"))
            .hasCount(0);
    }

    @Test
    @Order(8)
    void adminRegistrationRendersServerWideReports() {
        sakai.login("admin");
        sakai.gotoPath("/portal/site/!admin/tool/!admin-1225");

        assertThat(page.getByRole(AriaRole.HEADING,
            new Page.GetByRoleOptions().setName(Pattern.compile("All sites", Pattern.CASE_INSENSITIVE)))).isVisible();
        Locator siteLinks = page.locator("table tbody a[href*='serverwide']");
        assertTrue(siteLinks.count() > 0);
        siteLinks.first().click();
        assertThat(page.locator("nav.mb-3 a[aria-current='page']")).hasCount(1);
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
            + " const panels = [];"
            + " const collectPanels = root => {"
            + "   panels.push(...root.querySelectorAll('sakai-sitestats-report-panel'));"
            + "   for (const element of root.querySelectorAll('*')) {"
            + "     if (element.shadowRoot) { collectPanels(element.shadowRoot); }"
            + "   }"
            + " };"
            + " collectPanels(document);"
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
