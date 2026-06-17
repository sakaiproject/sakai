import "../sakai-sitestats-report-panel.js";
import * as i18n from "./i18n.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";

describe("sakai-sitestats-report-panel tests", () => {

  const endpoint = "/api/sites/site1/sitestats/widgets/visits/tabs/bydate?include=table,chart";
  const chartOnlyEndpoint = "/api/sites/site1/sitestats/widgets/visits/tabs/bydate?include=chart";

  beforeEach(() => {
    window.sessionStorage.clear();
    window.sakai = undefined;
    fetchMock.mockGlobal();
    fetchMock.get(i18n.i18nUrl, i18n.i18n);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  it("renders chart and table data from the SiteStats view contract", async () => {

    fetchMock.get(endpoint, {
      siteId: "site1",
      presentationMode: "how-presentation-both",
      summary: [
        { id: "site", label: "Site:", value: "Project Site" },
        { id: "generated-on", label: "Report generated:", value: "6/17/26, 2:15 PM" },
      ],
      table: {
        caption: "Visits",
        columns: [
          { key: "date", label: "Date", type: "date" },
          { key: "visits", label: "Visits", type: "number", align: "end" },
        ],
        rows: [
          {
            cells: {
              date: { raw: "2026-06-17", display: "6/17/26" },
              visits: { raw: 3, display: "3" },
            },
          },
        ],
        page: 1,
        pageSize: 50,
        totalRows: 1,
      },
      chart: {
        title: "Visits",
        type: "bar",
        xKey: "date",
        yKey: "visits",
        threeDimensional: false,
        transparency: 1,
        itemLabelsVisible: true,
        datasets: [
          {
            key: "visits",
            label: "Visits",
            points: [{ x: "2026-06-17", label: "6/17/26", y: 3 }],
          },
        ],
      },
    });

    const el = await fixture(html`<sakai-sitestats-report-panel endpoint="${endpoint}"></sakai-sitestats-report-panel>`);
    await waitUntil(() => el.shadowRoot.querySelector("sakai-sitestats-table"));
    await elementUpdated(el);

    expect(el.shadowRoot.querySelector(".summary").textContent).to.include("Project Site");
    expect(el.shadowRoot.querySelector(".summary").textContent).to.include("Report generated:");
    const chartEl = el.shadowRoot.querySelector("sakai-sitestats-chart");
    expect(chartEl).to.exist;
    expect(el.shadowRoot.querySelector("sakai-sitestats-table")).to.exist;
    await waitUntil(() => chartEl._i18n);
    await elementUpdated(chartEl);
    expect(chartEl.renderTableFallback).to.be.false;
    expect(chartEl.shadowRoot.querySelector("sakai-sitestats-table.visually-hidden")).to.not.exist;
  });

  it("keeps the hidden chart table fallback in chart-only mode", async () => {

    fetchMock.get(chartOnlyEndpoint, {
      siteId: "site1",
      presentationMode: "how-presentation-chart",
      table: {
        caption: "Visits",
        columns: [
          { key: "date", label: "Date", type: "date" },
          { key: "visits", label: "Visits", type: "number", align: "end" },
        ],
        rows: [
          {
            cells: {
              date: { raw: "2026-06-17", display: "6/17/26" },
              visits: { raw: 3, display: "3" },
            },
          },
        ],
      },
      chart: {
        title: "Visits",
        type: "bar",
        xKey: "date",
        yKey: "visits",
        datasets: [
          {
            key: "visits",
            label: "Visits",
            points: [{ x: "2026-06-17", label: "6/17/26", y: 3 }],
          },
        ],
      },
    });

    const el = await fixture(html`<sakai-sitestats-report-panel endpoint="${chartOnlyEndpoint}"></sakai-sitestats-report-panel>`);
    await waitUntil(() => el.shadowRoot.querySelector("sakai-sitestats-chart"));
    const chartEl = el.shadowRoot.querySelector("sakai-sitestats-chart");
    await waitUntil(() => chartEl.shadowRoot.querySelector("sakai-sitestats-table.visually-hidden"));

    expect(el.shadowRoot.querySelector("sakai-sitestats-table")).to.not.exist;
    expect(chartEl.renderTableFallback).to.be.true;
  });

  it("renders an alert when the endpoint fails", async () => {

    fetchMock.get(endpoint, 403);

    const el = await fixture(html`<sakai-sitestats-report-panel endpoint="${endpoint}"></sakai-sitestats-report-panel>`);
    await waitUntil(() => el.shadowRoot.querySelector("[role='alert']"));

    expect(el.shadowRoot.querySelector("[role='alert']").textContent).to.equal(el.tr("failed_to_load_statistics", [ 403 ]));
  });
});
