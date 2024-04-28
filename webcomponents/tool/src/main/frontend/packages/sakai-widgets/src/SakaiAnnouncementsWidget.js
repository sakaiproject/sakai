import { html } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";
import "@sakai-ui/sakai-announcements/sakai-announcements.js";
import { SakaiDashboardWidget } from "./SakaiDashboardWidget.js";

export class SakaiAnnouncementsWidget extends SakaiDashboardWidget {

  constructor() {

    super();

    this.widgetId = "announcements";
  }

  content() {

    return html`
      <sakai-announcements
        user-id="${ifDefined(this.userId ? this.userId : undefined)}"
        site-id="${ifDefined(this.siteId ? this.siteId : undefined)}"
      >
      </sakai-announcements>
    `;
  }
}
