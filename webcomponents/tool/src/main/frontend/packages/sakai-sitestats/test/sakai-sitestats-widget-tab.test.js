import "../sakai-sitestats-widget-tab.js";
import { SakaiSiteStatsWidgetTab } from "../src/SakaiSiteStatsWidgetTab.js";
import * as i18n from "./i18n.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";

describe("sakai-sitestats-widget-tab tests", () => {

  const endpoint = "/api/sites/site1/sitestats/widgets/visits/tabs/bydate?date=when-last7days&page=2";
  const updatedEndpoint = "/api/sites/site1/sitestats/widgets/visits/tabs/bydate?date=when-all&page=1";
  const filters = [ {
    id: "date",
    label: "Period:",
    options: [
      { value: "when-all", label: "All" },
      { value: "when-last7days", label: "Last 7 days" },
      { value: "when-custom", label: "Custom" },
    ],
  }, {
    id: "whenFrom",
    label: "From:",
    type: "date",
    value: "2026-08-25",
  }, {
    id: "whenTo",
    label: "To:",
    type: "date",
    value: "2026-09-01",
  } ];

  beforeEach(() => {
    window.sessionStorage.clear();
    window.sakai = undefined;
    fetchMock.mockGlobal();
    fetchMock.get(i18n.i18nUrl, i18n.i18n);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  it("lazily loads the report and initializes property filters from the endpoint", async () => {

    fetchMock.get(endpoint, {});
    const el = await fixture(html`
      <sakai-sitestats-widget-tab .endpoint=${endpoint} .filters=${filters}>
        <span slot="title">Visits</span>
      </sakai-sitestats-widget-tab>
    `);

    expect(fetchMock.callHistory.called(endpoint)).to.be.false;
    expect(el.shadowRoot.querySelector("select").value).to.equal("when-last7days");

    const details = el.shadowRoot.querySelector("details");
    details.open = true;
    details.dispatchEvent(new Event("toggle"));
    await elementUpdated(el);

    const panel = el.shadowRoot.querySelector("sakai-sitestats-report-panel");
    expect(panel.endpoint).to.equal(endpoint);
    await waitUntil(() => fetchMock.callHistory.called(endpoint));
  });

  it("loads the report immediately when initially open", async () => {

    fetchMock.get(endpoint, {});
    const el = await fixture(html`
      <sakai-sitestats-widget-tab open endpoint=${endpoint}>
        <span slot="title">Visits</span>
      </sakai-sitestats-widget-tab>
    `);

    expect(el.shadowRoot.querySelector("details").open).to.be.true;
    expect(el.shadowRoot.querySelector("sakai-sitestats-report-panel").endpoint).to.equal(endpoint);
    await waitUntil(() => fetchMock.callHistory.called(endpoint));
  });

  it("preserves open from declarative markup when upgraded", async () => {

    fetchMock.get(endpoint, {});
    const container = document.createElement("div");
    container.innerHTML = `
      <test-sakai-sitestats-widget-tab open endpoint="${endpoint}">
        <span slot="title">Visits</span>
      </test-sakai-sitestats-widget-tab>
    `;
    document.body.append(container);

    customElements.define(
      "test-sakai-sitestats-widget-tab",
      class extends SakaiSiteStatsWidgetTab {}
    );

    const el = container.querySelector("test-sakai-sitestats-widget-tab");
    await elementUpdated(el);

    expect(el.open).to.be.true;
    expect(el.shadowRoot.querySelector("details").open).to.be.true;
    expect(el.shadowRoot.querySelector("sakai-sitestats-report-panel").endpoint).to.equal(endpoint);
    await waitUntil(() => fetchMock.callHistory.called(endpoint));
    container.remove();
  });

  it("reloads a previously opened report when a slotted filter changes while closed", async () => {

    fetchMock.get(endpoint, {});
    fetchMock.get(updatedEndpoint, {});
    const el = await fixture(html`
      <sakai-sitestats-widget-tab endpoint=${endpoint}>
        <span slot="title">Visits</span>
        <div slot="filter" class="sitestats-widget-filter">
          <label for="period">Period:</label>
          <select id="period" data-report-filter="date">
            <option value="when-all">All</option>
            <option value="when-last7days">Last 7 days</option>
          </select>
        </div>
      </sakai-sitestats-widget-tab>
    `);

    const details = el.shadowRoot.querySelector("details");
    details.open = true;
    details.dispatchEvent(new Event("toggle"));
    await elementUpdated(el);
    await waitUntil(() => fetchMock.callHistory.called(endpoint));

    details.open = false;
    details.dispatchEvent(new Event("toggle"));
    const filter = el.querySelector("select");
    filter.value = "when-all";
    filter.dispatchEvent(new Event("change", { bubbles: true, composed: true }));
    await elementUpdated(el);

    expect(el.endpoint).to.equal(updatedEndpoint);
    expect(el.getAttribute("endpoint")).to.equal(updatedEndpoint);
    expect(el.shadowRoot.querySelector("sakai-sitestats-report-panel").endpoint).to.equal(updatedEndpoint);
    await waitUntil(() => fetchMock.callHistory.called(updatedEndpoint));
  });

  it("shows custom date fields and appends whenFrom and whenTo", async () => {

    fetchMock.get(endpoint, {});
    fetchMock.get(/whenFrom=2026-08-25/, {});
    fetchMock.get(/whenFrom=2026-01-01/, {});
    const el = await fixture(html`
      <sakai-sitestats-widget-tab .endpoint=${endpoint} .filters=${filters}>
        <span slot="title">Visits</span>
      </sakai-sitestats-widget-tab>
    `);

    await elementUpdated(el);
    expect(el.shadowRoot.querySelector("[data-report-filter='whenFrom']")).to.not.exist;

    const dateFilter = el.shadowRoot.querySelector("select");
    dateFilter.value = "when-custom";
    dateFilter.dispatchEvent(new Event("change", { bubbles: true, composed: true }));
    await elementUpdated(el);
    await waitUntil(() => new URL(el.endpoint, "http://localhost").searchParams.get("whenFrom") === "2026-08-25");

    const fromInput = el.shadowRoot.querySelector("[data-report-filter='whenFrom']");
    const toInput = el.shadowRoot.querySelector("[data-report-filter='whenTo']");
    expect(fromInput).to.exist;
    expect(toInput).to.exist;
    expect(fromInput.value).to.equal("2026-08-25");
    expect(toInput.value).to.equal("2026-09-01");

    fromInput.value = "2026-01-01";
    fromInput.dispatchEvent(new Event("change", { bubbles: true, composed: true }));
    await elementUpdated(el);

    const to = el.shadowRoot.querySelector("[data-report-filter='whenTo']");
    to.value = "2026-06-30";
    to.dispatchEvent(new Event("change", { bubbles: true, composed: true }));
    await elementUpdated(el);

    const params = new URL(el.endpoint, "http://localhost").searchParams;
    expect(params.get("date")).to.equal("when-custom");
    expect(params.get("whenFrom")).to.equal("2026-01-01");
    expect(params.get("whenTo")).to.equal("2026-06-30");
  });

  it("reveals slotted custom date fields that already have labels", async () => {

    fetchMock.get(endpoint, {});
    fetchMock.get(/date=when-custom/, {});
    const el = await fixture(html`
      <sakai-sitestats-widget-tab .endpoint=${endpoint}>
        <span slot="title">Visits</span>
        <div slot="filter" class="sitestats-widget-filter">
          <label for="filter-date">Period:</label>
          <select id="filter-date" data-report-filter="date">
            <option value="when-all">All</option>
            <option value="when-last7days">Last 7 days</option>
            <option value="when-custom">Custom</option>
          </select>
        </div>
        <div slot="filter" class="sitestats-widget-filter" hidden>
          <label for="filter-whenFrom">From:</label>
          <input id="filter-whenFrom" type="date" data-report-filter="whenFrom" value="2026-08-25">
        </div>
        <div slot="filter" class="sitestats-widget-filter" hidden>
          <label for="filter-whenTo">To:</label>
          <input id="filter-whenTo" type="date" data-report-filter="whenTo" value="2026-09-01">
        </div>
      </sakai-sitestats-widget-tab>
    `);

    const fromWrapper = el.querySelector("[data-report-filter='whenFrom']").closest("[slot='filter']");
    expect(fromWrapper.hidden).to.be.true;

    const dateFilter = el.querySelector("[data-report-filter='date']");
    dateFilter.value = "when-custom";
    dateFilter.dispatchEvent(new Event("change", { bubbles: true, composed: true }));
    await elementUpdated(el);
    await waitUntil(() => new URL(el.endpoint, "http://localhost").searchParams.get("whenFrom") === "2026-08-25");

    expect(fromWrapper.hidden).to.be.false;
    expect(el.querySelector("label[for='filter-whenFrom']").textContent).to.equal("From:");
    expect(el.querySelector("label[for='filter-whenTo']").textContent).to.equal("To:");
    const params = new URL(el.endpoint, "http://localhost").searchParams;
    expect(params.get("date")).to.equal("when-custom");
    expect(params.get("whenFrom")).to.equal("2026-08-25");
    expect(params.get("whenTo")).to.equal("2026-09-01");
  });

  it("loads no custom range when a slotted date is cleared", async () => {

    fetchMock.get(endpoint, {});
    fetchMock.get(/date=when-custom/, {});
    const el = await fixture(html`
      <sakai-sitestats-widget-tab .endpoint=${endpoint}>
        <span slot="title">Visits</span>
        <div slot="filter" class="sitestats-widget-filter">
          <label for="filter-date">Period:</label>
          <select id="filter-date" data-report-filter="date">
            <option value="when-last7days">Last 7 days</option>
            <option value="when-custom">Custom</option>
          </select>
        </div>
        <div slot="filter" class="sitestats-widget-filter" hidden>
          <input type="date" data-report-filter="whenFrom" value="2026-08-25">
        </div>
        <div slot="filter" class="sitestats-widget-filter" hidden>
          <input type="date" data-report-filter="whenTo" value="2026-09-01">
        </div>
      </sakai-sitestats-widget-tab>
    `);

    const dateFilter = el.querySelector("[data-report-filter='date']");
    dateFilter.value = "when-custom";
    dateFilter.dispatchEvent(new Event("change", { bubbles: true, composed: true }));
    await elementUpdated(el);
    await waitUntil(() => new URL(el.endpoint, "http://localhost").searchParams.get("whenFrom") === "2026-08-25");

    const fromInput = el.querySelector("[data-report-filter='whenFrom']");
    fromInput.value = "";
    fromInput.dispatchEvent(new Event("change", { bubbles: true, composed: true }));
    await elementUpdated(el);

    const params = new URL(el.endpoint, "http://localhost").searchParams;
    expect(params.get("date")).to.equal("when-custom");
    expect(params.get("whenFrom")).to.equal(null);
    expect(params.get("whenTo")).to.equal("2026-09-01");
  });
});
