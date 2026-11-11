import { sendKeys } from "@web/test-runner-commands";
import "../sakai-sitestats-widget.js";
import * as data from "./data.js";
import * as sitestatsI18n from "../../sakai-sitestats/test/i18n.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";

describe("sakai-sitestats-widget tests", () => {

  const siteId = "course1";
  const widgetId = "visits";
  const metricsUrl = `/api/sites/${siteId}/sitestats/widgets/${widgetId}/metrics`;
  const highlightsUrl = `/api/sites/${siteId}/sitestats/widgets/${widgetId}/highlights`;

  const mockCommonI18n = () => {
    fetchMock
      .get(data.dashboardWidgetI18nUrl, data.dashboardWidgetI18n)
      .get(data.toolnamesI18nUrl, `${data.toolnamesI18n}\nsitestats-visits=Visits\n`)
      .get(sitestatsI18n.i18nUrl, sitestatsI18n.i18n);
  };

  beforeEach(() => {
    window.top.portal = { locale: "en_GB" };
    fetchMock.mockGlobal();
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  it("renders compact metrics for a SiteStats overview widget", async () => {

    mockCommonI18n();
    fetchMock
      .get(metricsUrl, [
        {
          id: "visits-total",
          label: "Total visits",
          snapshot: { primary: "12" },
        },
      ])
      .get(highlightsUrl, []);

    const el = await fixture(html`
      <sakai-sitestats-widget
          sitestats-widget-id=${widgetId}
          site-id=${siteId}>
      </sakai-sitestats-widget>
    `);

    await waitUntil(() => el.shadowRoot?.querySelector("sakai-sitestats-widget-metrics"));
    await elementUpdated(el);

    expect(el.title).to.equal("Visits");
    const metrics = el.shadowRoot.querySelector("sakai-sitestats-widget-metrics");
    const highlights = el.shadowRoot.querySelector("sakai-sitestats-highlights");
    expect(metrics.hasAttribute("dashboard")).to.be.true;
    expect(metrics.revealOnClick).to.be.false;
    expect(highlights.hasAttribute("dashboard")).to.be.true;
    await waitUntil(() => metrics.renderRoot.querySelector(".sitestats-metric-primary"));
    expect(metrics.renderRoot.querySelector(".sitestats-metric-primary").textContent).to.equal("12");
  });

  for (const gradeWidgetId of [ "grades", "student-grades" ]) {
    it(`blurs and toggles ${gradeWidgetId} values using the dashboard setting`, async () => {

      mockCommonI18n();
      fetchMock
        .get(`/api/sites/${siteId}/sitestats/widgets/${gradeWidgetId}/metrics`, [
          { id: "average", label: "Average grade", snapshot: { primary: "75", percentage: 75 } },
          { id: "graded", label: "Graded", snapshot: { primary: "12" } },
        ])
        .get(`/api/sites/${siteId}/sitestats/widgets/${gradeWidgetId}/highlights`, []);

      const el = await fixture(html`
        <sakai-sitestats-widget sitestats-widget-id=${gradeWidgetId} site-id=${siteId}>
        </sakai-sitestats-widget>
      `);
      await waitUntil(() => el.renderRoot.querySelector("sakai-sitestats-widget-metrics"));
      const metrics = el.renderRoot.querySelector("sakai-sitestats-widget-metrics");
      await waitUntil(() => metrics.renderRoot.querySelector(".score-toggle"));

      const button = metrics.renderRoot.querySelector(".score-toggle");
      const value = button.querySelector("span");
      const secondButton = metrics.renderRoot.querySelectorAll(".score-toggle")[1];
      const secondValue = secondButton.querySelector("span");
      const labelId = button.getAttribute("aria-labelledby");
      expect(metrics.renderRoot.getElementById(labelId).textContent).to.equal("Average grade");
      expect(button.getAttribute("aria-pressed")).to.equal("false");
      expect(value.getAttribute("aria-hidden")).to.equal("true");
      expect(getComputedStyle(value).filter).to.equal("blur(3px)");

      for (const [ revealed, key ] of [ [ true, null ], [ false, "Enter" ], [ true, "Space" ] ]) {
        button.focus();
        if (key) {
          await sendKeys({ press: key });
        } else {
          button.click();
        }
        await elementUpdated(metrics);
        expect(button.getAttribute("aria-pressed")).to.equal(String(revealed));
        expect(value.getAttribute("aria-hidden")).to.equal(String(!revealed));
        expect(getComputedStyle(value).filter).to.equal(revealed ? "none" : "blur(3px)");
        expect(button.getAttribute("aria-labelledby")).to.equal(revealed ? `${labelId} ${value.id}` : labelId);
        expect(value.textContent).to.contain("75");
        expect(value.querySelector(".sitestats-metric-percentage").textContent).to.contain("75");
        expect(secondButton.getAttribute("aria-pressed")).to.equal("false");
        expect(secondValue.getAttribute("aria-hidden")).to.equal("true");
        expect(getComputedStyle(secondValue).filter).to.equal("blur(3px)");
      }

      // A widget rerender using the same endpoint preserves revealed values.
      el.tileW = 2;
      await elementUpdated(el);
      await elementUpdated(metrics);
      expect(button.getAttribute("aria-pressed")).to.equal("true");

      // Reusing the component for another site must hide even matching metric IDs.
      fetchMock
        .get(`/api/sites/course2/sitestats/widgets/${gradeWidgetId}/metrics`, [
          { id: "average", label: "Average grade", snapshot: { primary: "95" } },
        ])
        .get(`/api/sites/course2/sitestats/widgets/${gradeWidgetId}/highlights`, []);
      el.siteId = "course2";
      await elementUpdated(el);
      await waitUntil(() => metrics.renderRoot.querySelector(".sitestats-metric-primary")?.textContent === "95");
      const newButton = metrics.renderRoot.querySelector(".score-toggle");
      expect(newButton.getAttribute("aria-pressed")).to.equal("false");
      expect(newButton.getAttribute("aria-labelledby")).to.equal(labelId);
      expect(newButton.querySelector("span").getAttribute("aria-hidden")).to.equal("true");
      expect(getComputedStyle(newButton.querySelector("span")).filter).to.equal("blur(3px)");

      el.revealOnClick = false;
      await elementUpdated(el);
      await elementUpdated(metrics);
      expect(metrics.renderRoot.querySelector(".score-toggle")).to.be.null;
      expect(metrics.renderRoot.querySelector(".blurred")).to.be.null;
    });
  }

  it("hides itself when metrics return 403", async () => {

    mockCommonI18n();
    fetchMock
      .get(metricsUrl, 403)
      .get(highlightsUrl, 403);

    const el = await fixture(html`
      <sakai-sitestats-widget
          sitestats-widget-id=${widgetId}
          site-id=${siteId}>
      </sakai-sitestats-widget>
    `);

    await waitUntil(() => el.hidden === true, "widget should hide on 403", { timeout: 5000 });
    expect(el.hidden).to.be.true;
  });
});
