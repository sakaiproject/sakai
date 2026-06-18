import { html, css, nothing } from "lit";
import Chart from "chart.js/auto";
import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import "../sakai-sitestats-table.js";
import {
  hasSiteStatsChartData,
  siteStatsChartData,
  siteStatsChartType,
  siteStatsFallbackTable,
  useDepthEffect,
} from "./site-stats-chart-adapter.js";

export class SakaiSiteStatsChart extends SakaiShadowElement {

  static properties = {
    chart: { type: Object },
    renderTableFallback: { type: Boolean, attribute: "render-table-fallback" },
    _fallbackTable: { state: true },
  };

  static styles = [
    ...SakaiShadowElement.styles,
    css`
      :host {
        display: block;
      }

      figure {
        margin: 0;
      }

      .chart-frame {
        position: relative;
        block-size: min(22rem, 55vh);
        min-block-size: 16rem;
        border: 1px solid var(--sakai-border-color, #d8dde6);
        background: var(--sakai-background-color-1, #fff);
        padding: 0.75rem;
      }

      .chart-frame.depth {
        box-shadow: inset 0 -0.45rem 0 rgba(0, 0, 0, 0.08);
      }

      canvas {
        inline-size: 100%;
        block-size: 100%;
      }

      figcaption {
        margin-block-start: 0.5rem;
        color: var(--sakai-text-color-dimmed, #5f6773);
        font-size: 0.9rem;
      }

      .empty {
        border: 1px solid var(--sakai-border-color, #d8dde6);
        padding: 1rem;
        background: var(--sakai-background-color-2, #f8f9fb);
      }

      .visually-hidden:where(:not(:focus-within, :active)) {
        position: absolute !important;
        clip-path: inset(50%) !important;
        overflow: hidden !important;
        width: 1px !important;
        height: 1px !important;
        margin: -1px !important;
        padding: 0 !important;
        border: 0 !important;
        white-space: nowrap !important;
      }
    `
  ];

  constructor() {

    super();

    this.renderTableFallback = true;
    this.loadTranslations("sitestats");
  }

  disconnectedCallback() {

    this._destroyChart();
    super.disconnectedCallback();
  }

  updated(changedProperties) {

    if (changedProperties.has("chart") || changedProperties.has("_i18n")) {
      this._fallbackTable = siteStatsFallbackTable(this.chart, this._i18n);
      this.updateComplete.then(() => this._renderChart());
    }
  }

  render() {

    if (!this._i18n) return nothing;

    if (!hasSiteStatsChartData(this.chart)) {
      return html`<div class="empty">${this.chart?.unsupportedReason || this.chart?.emptyMessage || this._i18n.no_data_available}</div>`;
    }

    const chartLabel = this.chart.title || this._i18n.site_statistics_chart;

    return html`
      <figure>
        <div class=${useDepthEffect(this.chart) ? "chart-frame depth" : "chart-frame"}>
          <canvas aria-label="${chartLabel}" role="img"></canvas>
        </div>
        ${this.chart.title ? html`<figcaption>${this.chart.title}</figcaption>` : nothing}
      </figure>
      ${this.renderTableFallback && this._fallbackTable
        ? html`<sakai-sitestats-table class="visually-hidden" .table=${this._fallbackTable}></sakai-sitestats-table>`
        : nothing}
    `;
  }

  _renderChart() {

    this._destroyChart();
    if (!hasSiteStatsChartData(this.chart)) return;

    const canvas = this.shadowRoot.querySelector("canvas");
    if (!canvas) return;

    this._chartInstance = new Chart(canvas, {
      type: siteStatsChartType(this.chart),
      data: siteStatsChartData(this.chart),
      options: {
        responsive: true,
        maintainAspectRatio: false,
        layout: {
          padding: {
            top: this._showItemLabels() && siteStatsChartType(this.chart) !== "pie" ? 18 : 0,
          },
        },
        plugins: {
          legend: {
            display: this.chart.datasets.length > 1 || siteStatsChartType(this.chart) === "pie",
          },
          title: {
            display: false,
          },
        },
        elements: {
          bar: {
            borderSkipped: false,
            borderRadius: useDepthEffect(this.chart) ? 2 : 0,
          },
        },
        scales: siteStatsChartType(this.chart) === "pie" ? {} : {
          x: {
            ticks: {
              autoSkip: true,
              maxRotation: 0,
            },
          },
          y: {
            beginAtZero: true,
          },
        },
      },
      plugins: this._showItemLabels() ? [ this._valueLabelsPlugin() ] : [],
    });
  }

  _destroyChart() {

    if (this._chartInstance) {
      this._chartInstance.destroy();
      this._chartInstance = undefined;
    }
  }

  _showItemLabels() {

    return this.chart?.itemLabelsVisible !== false;
  }

  _valueLabelsPlugin() {

    return {
      id: "sakai-sitestats-value-labels",
      afterDatasetsDraw: chart => {

        const context = chart.ctx;
        context.save();
        context.font = "12px sans-serif";
        context.fillStyle = getComputedStyle(this).getPropertyValue("--sakai-text-color-1").trim() || "#1f2937";
        context.textAlign = "center";

        chart.data.datasets.forEach((dataset, datasetIndex) => {
          const meta = chart.getDatasetMeta(datasetIndex);
          if (meta.hidden) return;

          meta.data.forEach((element, index) => {
            const label = this._formatChartValue(dataset.data[index]);
            if (!label) return;

            const position = element.tooltipPosition();
            context.textBaseline = chart.config.type === "pie" ? "middle" : "bottom";
            context.fillText(label, position.x, chart.config.type === "pie" ? position.y : position.y - 4);
          });
        });

        context.restore();
      },
    };
  }

  _formatChartValue(value) {

    const number = Number(value);
    if (!Number.isFinite(number) || number === 0) return "";

    return Number.isInteger(number) ? String(number) : String(Math.round(number * 10) / 10);
  }

}
