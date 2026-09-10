import "../sakai-sitestats-chart.js";
import * as i18n from "./i18n.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";
import {
  compactTooltipLabel,
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
    const fallbackTable = el.shadowRoot.querySelector("sakai-sitestats-table.visually-hidden");
    expect(fallbackTable).to.exist;
    const figcaption = el.shadowRoot.querySelector("figcaption");
    expect(figcaption.textContent).to.equal("Visits");
    expect(figcaption.classList.contains("visually-hidden")).to.be.true;
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
    expect(options.scales.x.border.color).to.equal("rgb(70, 80, 90)");
    expect(options.scales.y.border.color).to.equal("rgb(70, 80, 90)");
    expect(options.scales.x.grid.color).to.equal("rgba(70, 80, 90, 0.55)");
  });

  it("renders a short chart without value labels in compact mode", async () => {

    const compactChart = { ...chart, compact: true, itemLabelsVisible: false };
    const el = await fixture(html`<sakai-sitestats-chart compact .chart=${compactChart} .renderTableFallback=${false}></sakai-sitestats-chart>`);
    await waitUntil(() => el._chartInstance);

    const options = siteStatsChartOptions(compactChart, siteStatsChartTheme(el), false, true);
    expect(el.hasAttribute("compact")).to.be.true;
    expect((el._chartInstance.config.plugins || []).some(plugin => plugin.id === "sakai-sitestats-value-labels")).to.be.false;
    expect(options.plugins.legend.display).to.be.false;
    expect(options.scales.x.ticks.display).to.be.false;
    expect(options.scales.y.ticks.display).to.be.false;
    expect(el.shadowRoot.querySelector("sakai-sitestats-table.visually-hidden")).to.not.exist;
    const figcaption = el.shadowRoot.querySelector("figcaption");
    expect(figcaption.textContent).to.equal("Visits");
    expect(figcaption.classList.contains("visually-hidden")).to.be.false;
  });

  it("renders a compact stacked status bar with semantic colors and no legend", async () => {

    const stackedChart = {
      title: "Submission status",
      type: "bar",
      compact: true,
      stacked: true,
      horizontal: true,
      itemLabelsVisible: false,
      datasets: [
        { key: "onTime", label: "On time", color: "success", points: [{ x: "status", label: "Submission status", y: 2 }] },
        { key: "late", label: "Late", color: "warning", points: [{ x: "status", label: "Submission status", y: 1 }] },
        { key: "missed", label: "Missed", color: "danger", points: [{ x: "status", label: "Submission status", y: 1 }] },
      ],
    };
    const el = await fixture(html`<sakai-sitestats-chart compact .chart=${stackedChart} .renderTableFallback=${false}></sakai-sitestats-chart>`);
    await waitUntil(() => el._chartInstance);

    const options = siteStatsChartOptions(stackedChart, siteStatsChartTheme(el), false, true);
    const data = siteStatsChartData(stackedChart, siteStatsChartTheme(el));

    expect(el.hasAttribute("stacked")).to.be.true;
    expect(el.hasAttribute("horizontal")).to.be.true;
    expect(el.hasAttribute("single-category")).to.be.true;
    expect(options.indexAxis).to.equal("y");
    expect(options.plugins.legend.display).to.be.false;
    expect(options.plugins.tooltip.enabled).to.be.false;
    expect(el._chartInstance.config.options.plugins.tooltip.external).to.be.a("function");
    expect(options.scales.x.stacked).to.be.true;
    expect(options.scales.x.max).to.equal(4);
    expect(options.scales.x.grace).to.equal(0);
    expect(options.scales.y.stacked).to.be.true;
    expect(options.scales.y.ticks.display).to.be.false;
    expect(data.datasets[0].borderWidth).to.equal(2);
    expect(data.datasets[0].borderAlign).to.equal("inner");
    expect(data.datasets[0].categoryPercentage).to.equal(1);
    expect(data.datasets[0].barPercentage).to.equal(1);
    expect(data.datasets[0].borderRadius).to.deep.equal({ topLeft: 4, bottomLeft: 4, topRight: 0, bottomRight: 0 });
    expect(data.datasets[1].borderRadius).to.equal(0);
    expect(data.datasets[2].borderRadius).to.deep.equal({ topLeft: 0, bottomLeft: 0, topRight: 4, bottomRight: 4 });
    expect(data.datasets[0].backgroundColor).to.contain("rgba(46, 125, 50");
    expect(data.datasets[1].backgroundColor).to.contain("rgba(239, 108, 0");
    expect(data.datasets[2].backgroundColor).to.contain("rgba(198, 40, 40");
    expect((el._chartInstance.config.plugins || []).some(plugin => plugin.id === "sakai-sitestats-value-labels")).to.be.false;
    const figcaption = el.shadowRoot.querySelector("figcaption");
    expect(figcaption.textContent).to.equal("Submission status");
    expect(figcaption.classList.contains("visually-hidden")).to.be.false;
  });

  it("renders a compact funnel with category labels on the y axis", async () => {

    const funnelChart = {
      title: "Grading funnel",
      type: "bar",
      compact: true,
      stacked: true,
      horizontal: true,
      itemLabelsVisible: false,
      datasets: [
        {
          key: "complete",
          label: "Fully graded",
          points: [
            { x: "Enrolled", label: "Enrolled", y: 20 },
            { x: "With grades", label: "With grades", y: 12 },
            { x: "Meeting threshold", label: "Meeting threshold", y: 12, color: "success" },
          ],
        },
        {
          key: "partial",
          label: "Not fully graded",
          color: "warning",
          points: [
            { x: "Enrolled", label: "Enrolled", y: 0 },
            { x: "With grades", label: "With grades", y: 4 },
            { x: "Meeting threshold", label: "Meeting threshold", y: 0 },
          ],
        },
      ],
    };
    const el = await fixture(html`<sakai-sitestats-chart compact .chart=${funnelChart} .renderTableFallback=${false}></sakai-sitestats-chart>`);
    await waitUntil(() => el._chartInstance);

    const options = siteStatsChartOptions(funnelChart, siteStatsChartTheme(el), true, true);

    expect(el.hasAttribute("horizontal")).to.be.true;
    expect(el.hasAttribute("stacked")).to.be.true;
    expect(el.hasAttribute("single-category")).to.be.false;
    expect(options.indexAxis).to.equal("y");
    expect(options.plugins.legend.display).to.be.false;
    expect(options.scales.y.ticks.display).to.be.true;
    expect(options.scales.x.ticks.display).to.be.false;
    expect((el._chartInstance.config.plugins || []).some(plugin => plugin.id === "sakai-sitestats-value-labels")).to.be.false;
    const data = siteStatsChartData(funnelChart, siteStatsChartTheme(el));
    expect(data.labels).to.deep.equal([ "Enrolled", "With grades", "Meeting threshold" ]);
    expect(data.datasets[0].backgroundColor[2]).to.contain("rgba(46, 125, 50");
    expect(data.datasets[0].borderColor[2]).to.contain("rgba(46, 125, 50");
    expect(data.datasets[1].backgroundColor).to.contain("rgba(239, 108, 0");
    expect(data.datasets[0].categoryPercentage).to.be.undefined;
    expect(options.layout.autoPadding).to.be.true;
    expect(options.scales.y.ticks.autoSkip).to.be.false;
    expect(options.scales.x.max).to.equal(20);
    expect(options.scales.x.min).to.equal(0);
    expect(options.scales.x.grace).to.equal(0);
    expect(options.scales.x.bounds).to.equal("data");
    expect(compactTooltipLabel({
      parsed: { x: 12 },
      datasetIndex: 0,
      dataIndex: 1,
      dataset: { key: "complete", label: "Fully graded" },
      chart: { options: { indexAxis: "y" }, data: { datasets: data.datasets } },
    })).to.equal("Fully graded: 12");
    expect(compactTooltipLabel({
      parsed: { x: 4 },
      datasetIndex: 1,
      dataIndex: 1,
      dataset: { key: "partial", label: "Not fully graded" },
      chart: { options: { indexAxis: "y" }, data: { datasets: data.datasets } },
    })).to.equal("Not fully graded: 4");
    const figcaption = el.shadowRoot.querySelector("figcaption");
    expect(figcaption.textContent).to.equal("Grading funnel");
    expect(figcaption.classList.contains("visually-hidden")).to.be.false;
  });

  it("uses success warning and danger colors for a submissions status pie", () => {

    const pieChart = {
      title: "Status",
      type: "pie",
      datasets: [
        {
          key: "status",
          label: "Status",
          points: [
            { x: "On time", label: "On time", y: 2, color: "success" },
            { x: "Late", label: "Late", y: 1, color: "warning" },
            { x: "Missed", label: "Missed", y: 1, color: "danger" },
          ],
        },
      ],
    };
    const data = siteStatsChartData(pieChart);

    expect(data.datasets[0].backgroundColor[0]).to.contain("rgba(46, 125, 50");
    expect(data.datasets[0].backgroundColor[1]).to.contain("rgba(239, 108, 0");
    expect(data.datasets[0].backgroundColor[2]).to.contain("rgba(198, 40, 40");
    expect(data.datasets[0].borderColor[0]).to.contain("rgba(46, 125, 50");
    expect(data.datasets[0].borderWidth).to.equal(1);
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

  it("suppresses empty chart messaging when a visible table owns the no-data state", async () => {

    const emptyChart = {
      title: "Visits",
      type: "bar",
      datasets: [],
    };

    const el = await fixture(html`
      <sakai-sitestats-chart
          .chart=${emptyChart}
          .renderTableFallback=${false}>
      </sakai-sitestats-chart>
    `);
    await waitUntil(() => el._i18n);
    await elementUpdated(el);

    expect(el.shadowRoot.querySelector(".empty")).to.not.exist;
    expect(el.shadowRoot.querySelector("canvas")).to.not.exist;
  });

  it("resizes the Chart.js instance when ResizeObserver reports a chart frame size change", async () => {

    const nativeResizeObserver = window.ResizeObserver;
    const observers = [];

    window.ResizeObserver = class {

      constructor(callback) {

        this.callback = callback;
        observers.push(this);
      }

      observe(target) {

        this.target = target;
      }

      disconnect() {

        this.disconnected = true;
      }
    };

    try {
      const el = await fixture(html`<sakai-sitestats-chart .chart=${chart}></sakai-sitestats-chart>`);
      await waitUntil(() => el._chartInstance);

      const frame = el.shadowRoot.querySelector(".chart-frame");
      await waitUntil(() => observers.some(observer => observer.target === frame));
      const frameObservers = observers.filter(observer => observer.target === frame);
      const resizeObserver = frameObservers[frameObservers.length - 1];
      expect(resizeObserver.target).to.equal(frame);

      let resizeCount = 0;
      el._chartInstance.resize = () => {
        resizeCount += 1;
      };

      resizeObserver.callback([{ target: frame }]);
      resizeObserver.callback([{ target: frame }]);
      await new Promise(resolve => requestAnimationFrame(resolve));

      expect(resizeCount).to.equal(1);
    } finally {
      window.ResizeObserver = nativeResizeObserver;
    }
  });
});
