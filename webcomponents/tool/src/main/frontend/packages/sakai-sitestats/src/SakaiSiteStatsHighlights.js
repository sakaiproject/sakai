import { css, html, nothing } from "lit";
import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import "../sakai-sitestats-chart.js";

export class SakaiSiteStatsHighlights extends SakaiShadowElement {

  static properties = {
    charts: { type: Array },
    endpoint: { type: String },
  };

  static styles = [
    ...SakaiShadowElement.styles,
    css`
      :host {
        display: block;
        width: 100%;
      }

      :host([hidden]) {
        display: none;
      }

      .metric-highlight {
        display: block;
        width: 100%;
        color: var(--sakai-text-color-dimmed);
        font-size: 0.875rem;
        font-weight: 400;
        text-align: center;
      }

      .metric-highlight + .metric-highlight {
        margin-block-start: 0.75rem;
      }
    `,
  ];

  constructor() {

    super();
    this.charts = [];
    this.hidden = true;
  }

  connectedCallback() {

    super.connectedCallback();
    if (this.endpoint) {
      this._load();
    }
  }

  updated(changedProperties) {

    if (changedProperties.has("endpoint") && this.endpoint) {
      this._load();
    }
    if (changedProperties.has("charts")) {
      this.hidden = !this._visibleCharts().length;
    }
  }

  disconnectedCallback() {

    this._abortController?.abort();
    super.disconnectedCallback();
  }

  render() {

    const charts = this._visibleCharts();
    if (!charts.length) {
      return nothing;
    }

    return html`
      ${charts.map(chart => html`
        <sakai-sitestats-chart
            class="metric-highlight"
            compact
            .chart=${chart}
            .renderTableFallback=${false}>
        </sakai-sitestats-chart>
      `)}
    `;
  }

  _visibleCharts() {

    return (Array.isArray(this.charts) ? this.charts : []).filter(chart => this._hasData(chart));
  }

  _hasData(chart) {

    return Array.isArray(chart?.datasets)
      && chart.datasets.some(dataset => Array.isArray(dataset.points)
        && dataset.points.some(point => Number(point?.y) > 0));
  }

  async _load() {

    this._abortController?.abort();
    this._abortController = new AbortController();

    try {
      const response = await fetch(this.endpoint, {
        credentials: "include",
        signal: this._abortController.signal,
      });
      if (!response.ok) {
        return;
      }
      this.charts = await response.json();
    } catch (error) {
      if (error.name !== "AbortError") {
        this.charts = this.charts || [];
      }
    }
  }
}
