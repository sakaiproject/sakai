import "../sakai-sitestats-chart.js";
import * as i18n from "./i18n.js";
import { expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";

describe("sakai-sitestats-chart tests", () => {

  const chart = {
    title: "Visits",
    type: "bar",
    xKey: "date",
    yKey: "visits",
    threeDimensional: true,
    transparency: 0.4,
    itemLabelsVisible: true,
    datasets: [
      {
        key: "visits",
        label: "Visits",
        points: [{ x: "2026-06-17", label: "6/17/26", y: 3 }],
      },
    ],
  };

  beforeEach(() => {
    window.sessionStorage.clear();
    window.sakai = undefined;
    fetchMock.mockGlobal();
    fetchMock.get(i18n.i18nUrl, i18n.i18n);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  it("applies SiteStats chart preferences from the view contract", async () => {

    const el = await fixture(html`<sakai-sitestats-chart .chart=${chart}></sakai-sitestats-chart>`);
    await waitUntil(() => el._chartInstance);

    const dataset = el._chartData().datasets[0];
    const plugins = el._chartInstance.config.plugins || [];

    expect(el.shadowRoot.querySelector(".chart-frame.depth")).to.exist;
    expect(dataset.backgroundColor).to.contain("0.26");
    expect(dataset.borderWidth).to.equal(3);
    expect(el._chartInstance.config.options.layout.padding.top).to.equal(18);
    expect(plugins.some(plugin => plugin.id === "sakai-sitestats-value-labels")).to.be.true;
    expect(el.shadowRoot.querySelector("sakai-sitestats-table.visually-hidden")).to.exist;
  });

  it("can suppress the hidden table fallback when a semantic table is already rendered", async () => {

    const el = await fixture(html`
      <sakai-sitestats-chart
          .chart=${chart}
          .renderTableFallback=${false}>
      </sakai-sitestats-chart>
    `);
    await waitUntil(() => el._chartInstance);

    expect(el.shadowRoot.querySelector("sakai-sitestats-table.visually-hidden")).to.not.exist;
  });
});
