import { css, html, nothing } from "lit";
import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import "../sakai-sitestats-chart.js";

export class SakaiSiteStatsHighlights extends SakaiShadowElement {

  static properties = {
    charts: { type: Array },
  };

  static styles = [
    ...SakaiShadowElement.styles,
    css`
      :host {
        display: block;
        width: 100%;
      }

      .highlight {
        display: block;
        width: 100%;
      }

      .highlight + .highlight {
        margin-block-start: 0.75rem;
      }
    `,
  ];

  constructor() {

    super();
    this.charts = [];
  }

  render() {

    const charts = (Array.isArray(this.charts) ? this.charts : []).filter(chart => this._hasData(chart));
    if (!charts.length) {
      return nothing;
    }

    return html`
      ${charts.map(chart => html`
        <sakai-sitestats-chart
            class="highlight"
            compact
            .chart=${chart}
            .renderTableFallback=${false}>
        </sakai-sitestats-chart>
      `)}
    `;
  }

  _hasData(chart) {

    return Array.isArray(chart?.datasets)
      && chart.datasets.some(dataset => Array.isArray(dataset.points)
        && dataset.points.some(point => Number(point?.y) > 0));
  }
}
