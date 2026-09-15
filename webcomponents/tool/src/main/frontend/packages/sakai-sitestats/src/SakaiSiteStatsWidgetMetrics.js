import { html, nothing } from "lit";
import { SakaiShadowElement } from "@sakai-ui/sakai-element";

export class SakaiSiteStatsWidgetMetrics extends SakaiShadowElement {

  static properties = {
    endpoint: { type: String },
    _metrics: { state: true },
    _loading: { state: true },
    _error: { state: true },
    _errorStatus: { state: true },
    _openHelpId: { state: true },
  };

  createRenderRoot() {
    return this;
  }

  constructor() {

    super();
    this.loadTranslations("sitestats");
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
      return html`<div class="sitestats-metrics-status" aria-busy="true">${this._i18n.loading_statistics}</div>`;
    }

    if (this._error) {
      const message = this._errorStatus
        ? this.tr("failed_to_load_statistics", [ this._errorStatus ])
        : this._i18n.failed_to_load_statistics_unknown;

      return html`<div class="sak-banner-error" role="alert">${message}</div>`;
    }

    if (!this._metrics?.length) {
      return nothing;
    }

    return html`
      <dl class="sitestats-metrics">
        ${this._metrics.map(metric => this._renderMetric(metric))}
      </dl>
    `;
  }

  _renderMetric(metric) {

    const snapshot = metric.snapshot || {};
    const detail = snapshot.detail;
    const primary = snapshot.primary;
    const help = metric.help;
    const title = !help && detail && detail !== primary ? detail : null;
    const helpId = help ? `sitestats-metric-help-${metric.id}` : nothing;
    const open = this._openHelpId === metric.id;

    return html`
      <div class="sitestats-metric ${help ? "has-help" : ""} ${open ? "is-open" : ""}"
           title=${title || nothing}
           tabindex=${help ? "0" : nothing}
           aria-describedby=${helpId || nothing}
           aria-expanded=${help ? String(open) : nothing}
           @click=${help ? () => this._toggleHelp(metric.id) : nothing}
           @keydown=${help ? event => this._helpKeydown(event, metric.id) : nothing}>
        <dt>${metric.label}</dt>
        <dd class="mb-0">
          <span class="sitestats-metric-primary">${primary ?? ""}</span>
          ${snapshot.percentage != null ? html`
            <span class="sitestats-metric-percentage">${this._formatPercentage(snapshot.percentage)}</span>
          ` : nothing}
          ${title ? html`<span class="visually-hidden">${detail}</span>` : nothing}
        </dd>
        ${help ? html`
          <span id=${helpId} class="visually-hidden">${help}</span>
          <span class="sitestats-metric-help" role="tooltip" aria-hidden="true">${help}</span>
        ` : nothing}
      </div>
    `;
  }

  _toggleHelp(id) {

    this._openHelpId = this._openHelpId === id ? undefined : id;
  }

  _helpKeydown(event, id) {

    if (event.key === "Escape") {
      this._openHelpId = undefined;
      return;
    }
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      this._toggleHelp(id);
    }
  }

  _formatPercentage(percentage) {

    return new Intl.NumberFormat(document.documentElement.lang || undefined, {
      style: "percent",
      maximumFractionDigits: 0,
    }).format(percentage / 100);
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
      this._metrics = await response.json();
    } catch (error) {
      if (error.name !== "AbortError") {
        this._error = true;
      }
    } finally {
      this._loading = false;
    }
  }
}
