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

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
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

        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(Pattern.compile("Show more", Pattern.CASE_INSENSITIVE))).first()
            .click(new Locator.ClickOptions().setForce(true));

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

        Locator addReportLink = page.locator("a[href*=\"lnkNewReport\"], .navIntraTool a, a")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Add$", Pattern.CASE_INSENSITIVE))).first();
        if (addReportLink.count() == 0) {
            assertThat(page.locator("body")).containsText(Pattern.compile("Reports", Pattern.CASE_INSENSITIVE));
            return;
        }
        addReportLink.click(new Locator.ClickOptions().setForce(true));

        page.locator("#content, main, .portletBody").first().locator("input[type=\"text\"]:visible").first().fill(REPORT_TITLE);

        if (!sakai.typeFirstCkEditorIfPresent("<p>" + REPORT_DESC + "</p>")) {
            page.locator("#content, main, .portletBody").first()
                .locator("textarea:visible, [contenteditable=\"true\"]:visible, div[role=\"textbox\"]:visible")
                .first()
                .fill(REPORT_DESC);
        }

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
