import { html, css, nothing } from "lit";
import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import "../sakai-sitestats-chart.js";
import "../sakai-sitestats-table.js";

export class SakaiSiteStatsReportPanel extends SakaiShadowElement {

  static properties = {
    endpoint: { type: String },
    _report: { state: true },
    _loading: { state: true },
    _error: { state: true },
    _errorStatus: { state: true },
  };

  static styles = [
    ...SakaiShadowElement.styles,
    css`
      :host {
        display: block;
      }

      .panel {
        display: grid;
        gap: 1rem;
      }

      .summary {
        display: grid;
        grid-template-columns: max-content minmax(0, 1fr);
        gap: 0.5rem 1rem;
        margin: 0;
        padding: 0;
      }

      .summary-row {
        display: contents;
      }

      .summary dt {
        font-weight: 600;
      }

      .summary dd {
        margin: 0;
        min-width: 0;
      }

      .status {
        border: 1px solid var(--sakai-border-color, #d8dde6);
        padding: 1rem;
        background: var(--sakai-background-color-2, #f8f9fb);
      }
    `
  ];

  constructor() {

    super();

    this.loadTranslations("sitestats");
  }

  connectedCallback() {

    super.connectedCallback();
    this._load();
  }

  updated(changedProperties) {

    if (changedProperties.has("endpoint")) {
      this._load();
    }
  }

  disconnectedCallback() {

    this._abortController?.abort();
    super.disconnectedCallback();
  }

  render() {

    if (!this._i18n) return nothing;

    if (this._loading) {
      return html`<div class="status" aria-busy="true">${this._i18n.loading_statistics}</div>`;
    }

    if (this._error) {
      const message = this._errorStatus
        ? this.tr("failed_to_load_statistics", [ this._errorStatus ])
        : this._i18n.failed_to_load_statistics_unknown;

      return html`<div class="alert alert-danger" role="alert">${message}</div>`;
    }

    if (!this._report) {
      return nothing;
    }

    const presentationMode = this._report.presentationMode || "how-presentation-both";
    const showChart = this._report.chart && presentationMode !== "how-presentation-table";
    const showTable = this._report.table && presentationMode !== "how-presentation-chart";

    return html`
      <div class="panel">
        ${this._renderSummary(this._report.summary)}
        ${showChart ? html`<sakai-sitestats-chart .chart=${this._report.chart}></sakai-sitestats-chart>` : nothing}
        ${showTable ? html`<sakai-sitestats-table .table=${this._report.table}></sakai-sitestats-table>` : nothing}
      </div>
    `;
  }

  _renderSummary(summary) {

    if (!Array.isArray(summary) || !summary.length) {
      return nothing;
    }

    return html`
      <dl class="summary">
        ${summary.map(item => html`
          <div class="summary-row">
            <dt>${item.label}</dt>
            <dd>${item.value}</dd>
          </div>
        `)}
      </dl>
    `;
  }

  async _load() {

    if (!this.endpoint) return;

    this._abortController?.abort();
    this._abortController = new AbortController();
    this._loading = true;
    this._error = false;
    this._errorStatus = undefined;

    try {
      const response = await fetch(this.endpoint, {
        credentials: "include",
        signal: this._abortController.signal,
      });
      if (!response.ok) {
        this._error = true;
        this._errorStatus = response.status;
        return;
      }
      this._report = await response.json();
    } catch (error) {
      if (error.name !== "AbortError") {
        this._error = true;
      }
    } finally {
      this._loading = false;
    }
  }
}
