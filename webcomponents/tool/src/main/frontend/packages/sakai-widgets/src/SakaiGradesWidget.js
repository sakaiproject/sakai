import { html } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";
import { SakaiDashboardWidget } from "./SakaiDashboardWidget.js";
import "@sakai-ui/sakai-grades";

export class SakaiGradesWidget extends SakaiDashboardWidget {

  constructor() {

    super();

    this.widgetId = "grades";
  }

  content() {

    return html`

      <sakai-grades
        user-id="${ifDefined(this.userId ? this.userId : undefined)}"
        site-id="${ifDefined(this.siteId ? this.siteId : undefined)}"
      >
      </sakai-grades>
    `;
  }
}

SakaiGradesWidget.roles = [ "instructor" ];
