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
    ],
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
    expect(el.shadowRoot.querySelector("sakai-sitestats-report-panel").endpoint).to.equal(updatedEndpoint);
    await waitUntil(() => fetchMock.callHistory.called(updatedEndpoint));
  });
});
