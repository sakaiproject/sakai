import { html } from "../assets/lit-element/lit-element.js";
import { ifDefined } from "../assets/lit-html/directives/if-defined.js";
import '../sakai-icon.js';
import '../calendar/sakai-calendar.js';
import "../assets/@lion/calendar/lion-calendar.js";
import { SakaiDashboardWidget } from './sakai-dashboard-widget.js';

export class SakaiCalendarWidget extends SakaiDashboardWidget {

  constructor() {

    super();

    this.widgetId = "calendar";
    this.title = "Calendar";
    this.loadTranslations("calendar");
  }

  shouldUpdate() {
    return this.siteId || this.userId;
  }

  content() {

    return html`
      <sakai-calendar
        site-id=${ifDefined(this.siteId ? this.siteId : undefined)}
        user-id=${ifDefined(this.userId ? this.userId : undefined)}
      ></sakai-calendar>
    `;
  }
}

if (!customElements.get("sakai-calendar-widget")) {
  customElements.define("sakai-calendar-widget", SakaiCalendarWidget);
}
