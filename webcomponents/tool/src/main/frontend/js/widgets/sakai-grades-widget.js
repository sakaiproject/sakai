import { html } from "../assets/lit-element/lit-element.js";
import { ifDefined } from "../assets/lit-html/directives/if-defined.js";
import { SakaiDashboardWidget } from "./sakai-dashboard-widget.js";
import "../grades/sakai-grades.js";

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

if (!customElements.get("sakai-grades-widget")) {
  customElements.define("sakai-grades-widget", SakaiGradesWidget);
}

SakaiGradesWidget.roles = ["instructor"];
