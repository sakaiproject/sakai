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
    expect(highlights.hasAttribute("dashboard")).to.be.true;
    await waitUntil(() => metrics.renderRoot.querySelector(".sitestats-metric-primary"));
    expect(metrics.renderRoot.querySelector(".sitestats-metric-primary").textContent).to.equal("12");
  });

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
