import "../sakai-sitestats-chart.js";
import * as i18n from "./i18n.js";
import { expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";
import {
  siteStatsChartData,
  siteStatsChartOptions,
} from "../src/site-stats-chart-adapter.js";
import { siteStatsChartTheme } from "../src/site-stats-chart-theme.js";

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

    const dataset = siteStatsChartData(chart).datasets[0];
    const plugins = el._chartInstance.config.plugins || [];

    expect(el.shadowRoot.querySelector(".chart-frame.depth")).to.exist;
    expect(dataset.backgroundColor).to.contain("0.26");
    expect(dataset.borderWidth).to.equal(3);
    expect(el._chartInstance.config.options.layout.padding.top).to.equal(18);
    expect(plugins.some(plugin => plugin.id === "sakai-sitestats-value-labels")).to.be.true;
    expect(el.shadowRoot.querySelector("sakai-sitestats-table.visually-hidden")).to.exist;
  });

  it("uses Sakai theme variables for chart colors", async () => {

    const el = await fixture(html`
      <sakai-sitestats-chart
          style="
            --sakai-primary-color-1: rgb(10, 20, 30);
            --sakai-text-color-1: rgb(220, 230, 240);
            --sakai-text-color-dimmed: rgb(150, 160, 170);
            --sakai-border-color: rgb(70, 80, 90);
            --sakai-background-color-1: rgb(5, 6, 7);
          "
          .chart=${chart}>
      </sakai-sitestats-chart>
    `);
    await waitUntil(() => el._chartInstance);

    const theme = siteStatsChartTheme(el);
    const dataset = siteStatsChartData(chart, theme).datasets[0];
    const options = siteStatsChartOptions(chart, theme);

    expect(dataset.backgroundColor).to.contain("rgba(10, 20, 30, 0.26)");
    expect(el._chartInstance.data.datasets[0].backgroundColor).to.contain("rgba(10, 20, 30, 0.26)");
    expect(options.plugins.legend.labels.color).to.equal("rgb(220, 230, 240)");
    expect(options.scales.x.ticks.color).to.equal("rgb(150, 160, 170)");
    expect(options.scales.x.grid.color).to.equal("rgba(70, 80, 90, 0.55)");
  });

  it("resolves chart theme aliases through browser CSS", async () => {

    const el = await fixture(html`
      <sakai-sitestats-chart
          style="
            --local-chart-color: hsl(210, 50%, 20%);
            --sakai-sitestats-chart-color-1: var(--local-chart-color);
          "
          .chart=${chart}>
      </sakai-sitestats-chart>
    `);
    await waitUntil(() => el._chartInstance);

    const dataset = el._chartInstance.data.datasets[0];

    expect(dataset.backgroundColor).to.contain("rgba(26, 51, 77, 0.26)");
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
