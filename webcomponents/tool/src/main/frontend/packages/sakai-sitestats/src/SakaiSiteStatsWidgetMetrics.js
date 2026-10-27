import { html, nothing } from "lit";
import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import { siteStatsCompactMetricsStyles } from "./site-stats-compact-styles.js";

export class SakaiSiteStatsWidgetMetrics extends SakaiShadowElement {

  static properties = {
    endpoint: { type: String },
    dashboard: { type: Boolean, reflect: true },
    _metrics: { state: true },
    _loading: { state: true },
    _error: { state: true },
    _errorStatus: { state: true },
    _openHelpId: { state: true },
    _expandedHelp: { state: true },
  };

  static styles = [
    ...SakaiShadowElement.styles,
    siteStatsCompactMetricsStyles,
  ];

  constructor() {

    super();
    this.dashboard = false;
    this._expandedHelp = new Set();
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

    if (this.dashboard) {
      return html`
        <div class="sitestats-metrics">
          ${this._metrics.map(metric => this._renderDashboardMetric(metric))}
        </div>
      `;
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
    const helpId = `sitestats-metric-help-${metric.id}`;
    const labelId = `sitestats-metric-label-${metric.id}`;
    const open = this._openHelpId === metric.id;

    return html`
      <div class="sitestats-metric ${help ? "has-help" : ""} ${open ? "is-open" : ""}"
           title=${title || nothing}>
        ${help ? html`
          <button type="button"
                  class="sitestats-metric-help-trigger"
                  aria-labelledby=${labelId}
                  aria-describedby=${helpId}
                  aria-expanded=${String(open)}
                  @click=${() => this._toggleHelp(metric.id)}
                  @keydown=${event => this._helpKeydown(event)}></button>
        ` : nothing}
        <dt id=${help ? labelId : nothing}>${metric.label}</dt>
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

  _renderDashboardMetric(metric) {

    const snapshot = metric.snapshot || {};
    const detail = snapshot.detail;
    const primary = snapshot.primary;
    const help = metric.help;
    const extra = !help && detail && detail !== primary ? detail : null;
    const helpText = help || extra;
    const expanded = helpText && this._expandedHelp.has(metric.id);

    return html`
      <div class="sitestats-metric ${expanded ? "is-expanded" : ""}">
        <dd>
          <span class="sitestats-metric-primary">${primary ?? ""}</span>
          ${snapshot.percentage != null ? html`
            <span class="sitestats-metric-percentage">${this._formatPercentage(snapshot.percentage)}</span>
          ` : nothing}
        </dd>
        <div class="sitestats-metric-header">
          <dt>${metric.label}</dt>
          ${helpText ? html`
            <button type="button"
                class="sitestats-metric-help ${expanded ? "is-expanded" : ""}"
                @click=${() => this._toggleDashboardHelp(metric.id)}
                title="${helpText}"
                aria-expanded="${expanded ? "true" : "false"}">
              ${helpText}
            </button>
          ` : nothing}
        </div>
      </div>
    `;
  }

  _toggleHelp(id) {

    this._openHelpId = this._openHelpId === id ? undefined : id;
  }

  _toggleDashboardHelp(metricId) {

    const next = new Set(this._expandedHelp);
    if (next.has(metricId)) {
      next.delete(metricId);
    } else {
      next.add(metricId);
    }
    this._expandedHelp = next;
  }

  _helpKeydown(event) {

    if (event.key === "Escape") {
      this._openHelpId = undefined;
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
        this.dispatchEvent(new CustomEvent("sitestats-unavailable", {
          detail: { status: response.status },
          bubbles: true,
          composed: true,
        }));
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
