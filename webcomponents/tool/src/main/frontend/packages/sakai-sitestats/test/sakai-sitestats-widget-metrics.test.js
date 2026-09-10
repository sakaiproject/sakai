import "../sakai-sitestats-widget-metrics.js";
import * as i18n from "./i18n.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";

describe("sakai-sitestats-widget-metrics tests", () => {

  const endpoint = "/api/sites/site1/sitestats/widgets/visits/metrics";

  beforeEach(() => {
    window.sessionStorage.clear();
    window.sakai = undefined;
    fetchMock.mockGlobal();
    fetchMock.get(i18n.i18nUrl, i18n.i18n);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  it("renders metric snapshots from the metrics endpoint", async () => {

    fetchMock.get(endpoint, [
      {
        id: "visits-total",
        label: "Visits",
        snapshot: { primary: "12", percentage: 50, detail: "12 of 24" },
      },
      {
        id: "visits-unique",
        label: "Unique visitors",
        snapshot: { primary: "4" },
      },
    ]);

    const el = await fixture(html`
      <sakai-sitestats-widget-metrics endpoint=${endpoint}></sakai-sitestats-widget-metrics>
    `);

    await waitUntil(() => el.querySelector(".sitestats-metric-primary"));
    await elementUpdated(el);

    const metrics = el.querySelectorAll(".sitestats-metric");
    expect(metrics).to.have.length(2);
    expect(metrics[0].querySelector("dt").textContent).to.equal("Visits");
    expect(metrics[0].querySelector(".sitestats-metric-primary").textContent).to.equal("12");
    expect(metrics[0].querySelector(".sitestats-metric-percentage").textContent).to.contain("%");
    expect(metrics[1].querySelector(".sitestats-metric-primary").textContent).to.equal("4");
  });

  it("shows an error when the metrics endpoint fails", async () => {

    fetchMock.get(endpoint, 500);
    const el = await fixture(html`
      <sakai-sitestats-widget-metrics endpoint=${endpoint}></sakai-sitestats-widget-metrics>
    `);

    await waitUntil(() => el.querySelector("[role='alert']"));
    expect(el.querySelector("[role='alert']").textContent).to.contain("Failed to load statistics");
  });

  it("loads a preset endpoint once", async () => {

    fetchMock.get(endpoint, [
      {
        id: "visits-total",
        label: "Visits",
        snapshot: { primary: "12" },
      },
    ]);

    const el = await fixture(html`
      <sakai-sitestats-widget-metrics endpoint=${endpoint}></sakai-sitestats-widget-metrics>
    `);

    await waitUntil(() => fetchMock.callHistory.called(endpoint));
    await elementUpdated(el);
    expect(fetchMock.callHistory.calls(endpoint).length).to.equal(1);
  });
});
