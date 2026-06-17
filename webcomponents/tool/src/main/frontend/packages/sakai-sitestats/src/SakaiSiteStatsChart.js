import { html, css, nothing } from "lit";
import Chart from "chart.js/auto";
import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import "../sakai-sitestats-table.js";

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
      this._fallbackTable = this._tableFromChart();
      this.updateComplete.then(() => this._renderChart());
    }
  }

  render() {

    if (!this._i18n) return nothing;

    if (!this._hasChartData()) {
      return html`<div class="empty">${this.chart?.emptyMessage || this._i18n.no_data_available}</div>`;
    }

    const chartLabel = this.chart.title || this._i18n.site_statistics_chart;

    return html`
      <figure>
        <div class=${this._useDepthEffect() ? "chart-frame depth" : "chart-frame"}>
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
    if (!this._hasChartData()) return;

    const canvas = this.shadowRoot.querySelector("canvas");
    if (!canvas) return;

    this._chartInstance = new Chart(canvas, {
      type: this._chartType(),
      data: this._chartData(),
      options: {
        responsive: true,
        maintainAspectRatio: false,
        layout: {
          padding: {
            top: this._showItemLabels() && this._chartType() !== "pie" ? 18 : 0,
          },
        },
        plugins: {
          legend: {
            display: this.chart.datasets.length > 1 || this._chartType() === "pie",
          },
          title: {
            display: false,
          },
        },
        elements: {
          bar: {
            borderSkipped: false,
            borderRadius: this._useDepthEffect() ? 2 : 0,
          },
        },
        scales: this._chartType() === "pie" ? {} : {
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

  _chartType() {

    switch (this.chart?.type) {
      case "line":
      case "timeseries":
        return "line";
      case "pie":
        return "pie";
      default:
        return "bar";
    }
  }

  _chartData() {

    const labels = this.chart.datasets[0].points.map(point => point.label ?? point.x);
    const chartType = this._chartType();
    const alpha = this._chartAlpha();
    const borderAlpha = Math.min(1, alpha + 0.22);
    if (chartType === "pie") {
      const dataset = this.chart.datasets[0];
      return {
        labels,
        datasets: [ {
          label: dataset.label,
          data: dataset.points.map(point => point.y),
          backgroundColor: this._colors(dataset.points.length, alpha),
          borderColor: this._colors(dataset.points.length, borderAlpha),
          borderWidth: this._useDepthEffect() ? 2 : 1,
          hoverOffset: this._useDepthEffect() ? 8 : 4,
        } ],
      };
    }

    const datasetCount = this.chart.datasets.length;
    return {
      labels,
      datasets: this.chart.datasets.map((dataset, index) => ({
        label: dataset.label,
        data: dataset.points.map(point => point.y),
        borderColor: this._colors(datasetCount, borderAlpha)[index],
        backgroundColor: this._colors(datasetCount, chartType === "line" ? this._chartAlpha(0.18) : this._chartAlpha(0.65))[index],
        borderWidth: this._useDepthEffect() ? 3 : 2,
        fill: chartType !== "line",
        pointRadius: chartType === "line" ? 3 : undefined,
        tension: 0.2,
      })),
    };
  }

  _colors(count, alpha = 0.82) {

    const base = [
      [ 25, 118, 210 ],
      [ 46, 125, 50 ],
      [ 239, 108, 0 ],
      [ 123, 31, 162 ],
      [ 0, 121, 107 ],
      [ 198, 40, 40 ],
      [ 69, 90, 100 ],
      [ 245, 124, 0 ],
    ];
    const colors = [];
    for (let i = 0; i < count; i++) {
      const c = base[i % base.length];
      colors.push(`rgba(${c[0]}, ${c[1]}, ${c[2]}, ${alpha})`);
    }
    return colors;
  }

  _chartAlpha(multiplier = 1) {

    const transparency = Number(this.chart?.transparency);
    const alpha = Number.isFinite(transparency) ? transparency : 1;
    return Math.min(1, Math.max(0, alpha * multiplier));
  }

  _showItemLabels() {

    return this.chart?.itemLabelsVisible !== false;
  }

  _useDepthEffect() {

    return this.chart?.threeDimensional === true;
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

  _hasChartData() {

    return Array.isArray(this.chart?.datasets)
      && this.chart.datasets.some(dataset => Array.isArray(dataset.points) && dataset.points.length > 0);
  }

  _tableFromChart() {

    if (!this._hasChartData()) return undefined;

    const columns = [
      { key: "label", label: this.chart.xKey || this._i18n?.label, type: "text" },
      ...this.chart.datasets.map(dataset => ({ key: dataset.key, label: dataset.label, type: "number", align: "end" })),
    ];

    const maxRows = Math.max(...this.chart.datasets.map(dataset => dataset.points.length));
    const rows = [];
    for (let i = 0; i < maxRows; i++) {
      const cells = {};
      const firstPoint = this.chart.datasets[0].points[i] || {};
      cells.label = { raw: firstPoint.x, display: firstPoint.label ?? firstPoint.x };
      this.chart.datasets.forEach(dataset => {
        const point = dataset.points[i] || {};
        cells[dataset.key] = { raw: point.y, display: point.y == null ? "" : String(point.y) };
      });
      rows.push({ cells });
    }

    return {
      caption: this.chart.title,
      columns,
      rows,
      page: 1,
      pageSize: rows.length,
      totalRows: rows.length,
    };
  }
}
