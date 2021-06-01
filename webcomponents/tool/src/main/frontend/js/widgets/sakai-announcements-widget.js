import { html } from "../assets/lit-element/lit-element.js";
import { ifDefined } from "../assets/lit-html/directives/if-defined.js";
import "../announcements/sakai-announcements.js";
import { SakaiDashboardWidget } from "./sakai-dashboard-widget.js";

export class SakaiAnnouncementsWidget extends SakaiDashboardWidget {

  constructor() {

    super();
    this.widgetId = "announcements";
    this.widgetId = "announcements";
    this.loadTranslations("announcements");
  }

  content() {

    return html`
      <sakai-announcements
        user-id="${ifDefined(this.userId ? this.userId : undefined)}"
        site-id="${ifDefined(this.siteId ? this.siteId : undefined)}"
      >
    `;
  }
}

if (!customElements.get("sakai-announcements-widget")) {
  customElements.define("sakai-announcements-widget", SakaiAnnouncementsWidget);
}
