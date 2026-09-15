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
        help: "Total site visits, including repeat visits.",
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
    expect(metrics[0].classList.contains("has-help")).to.be.true;
    expect(metrics[0].getAttribute("aria-describedby")).to.equal("sitestats-metric-help-visits-total");
    expect(metrics[0].getAttribute("aria-expanded")).to.equal("false");
    expect(metrics[0].querySelector(".sitestats-metric-help").textContent).to.equal("Total site visits, including repeat visits.");
    expect(metrics[1].classList.contains("has-help")).to.be.false;

    metrics[0].click();
    await elementUpdated(el);
    expect(el.querySelector(".sitestats-metric").classList.contains("is-open")).to.be.true;
    expect(el.querySelector(".sitestats-metric").getAttribute("aria-expanded")).to.equal("true");
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
