import { html } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";
import "@sakai-ui/sakai-calendar";
import { SakaiDashboardWidget } from "./SakaiDashboardWidget.js";

export class SakaiCalendarWidget extends SakaiDashboardWidget {

  constructor() {

    super();

    this.widgetId = "calendar";
    this.hasFilters = true;

    this.loadTranslations("calendar-wc").then(r => this.title = r.widget_title);
  }

  set widgetId(value) {
    this._widgetId = value;
  }

  get widgetId() { return this._widgetId; }

  updated(changedProperties) {

    super.updated(changedProperties);

    if (changedProperties.has("siteId")) {
      this.hasFilters = !this.siteId;
      if (this.siteId) {
        this.filtersOpen = false;
      }
    }
  }

  shouldUpdate() {
    return this.siteId || this.userId;
  }

  content() {

    return html`
      <sakai-calendar
        dashboard
        tile-w="${this._displayW()}"
        tile-h="${this.tileH || 1}"
        site-id=${ifDefined(this.siteId ? this.siteId : undefined)}
        user-id=${ifDefined(this.userId ? this.userId : undefined)}
        ?filters-open=${this.filtersOpen}
        @filters-closed=${this._closeFilters}
      >
      </sakai-calendar>
    `;
  }
}
