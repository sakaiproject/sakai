import { html } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";
import "@sakai-ui/sakai-calendar";
import { SakaiDashboardWidget } from "./SakaiDashboardWidget.js";

export class SakaiCalendarWidget extends SakaiDashboardWidget {

  constructor() {

    super();

    this.widgetId = "calendar";
  }

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
