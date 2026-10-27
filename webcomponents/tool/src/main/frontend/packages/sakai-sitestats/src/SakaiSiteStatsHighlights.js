import { html, nothing } from "lit";
import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import { siteStatsCompactHighlightsStyles } from "./site-stats-compact-styles.js";
import "../sakai-sitestats-chart.js";

export class SakaiSiteStatsHighlights extends SakaiShadowElement {

  static properties = {
    charts: { type: Array },
    endpoint: { type: String },
    dashboard: { type: Boolean, reflect: true },
  };

  static styles = [
    ...SakaiShadowElement.styles,
    siteStatsCompactHighlightsStyles,
  ];

  constructor() {

    super();
    this.charts = [];
    this.dashboard = false;
    this.hidden = true;
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
        this.dispatchEvent(new CustomEvent("sitestats-unavailable", {
          detail: { status: response.status },
          bubbles: true,
          composed: true,
        }));
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
