import { css, html, nothing } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";
import { SakaiDashboardWidget } from "./SakaiDashboardWidget.js";
import { loadToolnames } from "./toolnames.js";
import "@sakai-ui/sakai-sitestats/sakai-sitestats-widget-metrics.js";
import "@sakai-ui/sakai-sitestats/sakai-sitestats-highlights.js";

export class SakaiSiteStatsWidget extends SakaiDashboardWidget {

  static properties = {
    ...SakaiDashboardWidget.properties,
    sitestatsWidgetId: { attribute: "sitestats-widget-id", type: String },
    _forbidden: { state: true },
  };

  static styles = [
    SakaiDashboardWidget.styles,
    css`
      :host([hidden]) {
        display: none !important;
      }

      .sitestats-dashboard-widget {
        display: flex;
        flex-direction: column;
        height: 100%;
        min-height: 0;
      }

      #content {
        padding: 0;
        overflow: hidden;
      }

      sakai-sitestats-widget-metrics {
        display: block;
        flex: 1 1 auto;
        min-width: 0;
        min-height: 0;
        overflow: auto;
      }

      sakai-sitestats-highlights {
        display: block;
        flex: 0 0 auto;
        min-width: 0;
      }
    `,
  ];

  constructor() {

    super();
    this.hasOptions = false;
    this._forbidden = false;
    this.addEventListener("sitestats-unavailable", this._onUnavailable);
  }

  set sitestatsWidgetId(value) {

    this._sitestatsWidgetId = value;
    if (value) {
      this.widgetId = `sitestats-${value}`;
    }
  }

  get sitestatsWidgetId() { return this._sitestatsWidgetId; }

  set widgetId(value) {

    this._widgetId = value;
    if (value?.startsWith("sitestats-") && !this._sitestatsWidgetId) {
      this._sitestatsWidgetId = value.substring("sitestats-".length);
    }
    loadToolnames().then(r => {
      this.title = r?.[value] || r?.unknown || value;
    });
  }

  get widgetId() { return this._widgetId; }

  get _resolvedSiteId() {
    return this.siteId || "!admin";
  }

  _endpoint(kind) {

    if (!this.sitestatsWidgetId) {
      return undefined;
    }
    return `/api/sites/${encodeURIComponent(this._resolvedSiteId)}/sitestats/widgets/${encodeURIComponent(this.sitestatsWidgetId)}/${kind}`;
  }

  _onUnavailable = e => {

    const status = e.detail?.status;
    if (status === 403 || status === 404) {
      this._forbidden = true;
      this.hidden = true;
    }
  };

  shouldUpdate() {
    return this._baseI18n && this.title && !this._forbidden;
  }

  content() {

    if (this._forbidden || !this.sitestatsWidgetId) {
      return nothing;
    }

    return html`
      <div class="sitestats-dashboard-widget">
        <sakai-sitestats-widget-metrics
            dashboard
            endpoint="${ifDefined(this._endpoint("metrics"))}">
        </sakai-sitestats-widget-metrics>
        <sakai-sitestats-highlights
            dashboard
            class="sitestats-highlights"
            endpoint="${ifDefined(this._endpoint("highlights"))}">
        </sakai-sitestats-highlights>
      </div>
    `;
  }
}
