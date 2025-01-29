import { html } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";
import "@sakai-ui/sakai-calendar";
import { loadProperties } from "@sakai-ui/sakai-i18n";
import { SakaiDashboardWidget } from "./SakaiDashboardWidget.js";

export class SakaiCalendarWidget extends SakaiDashboardWidget {

  constructor() {

    super();

    this.widgetId = "calendar";

    loadProperties("calendar-wc").then(r => this.title = r.widget_title);
  }

  set widgetId(value) {
    this._widgetId = value;
  }

  get widgetId() { return this._widgetId; }

  shouldUpdate() {
    return this.siteId || this.userId;
  }

  content() {

    return html`
      <sakai-calendar
        site-id=${ifDefined(this.siteId ? this.siteId : undefined)}
        user-id=${ifDefined(this.userId ? this.userId : undefined)}
      >
      </sakai-calendar>
    `;
  }
}
