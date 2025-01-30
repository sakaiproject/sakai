import { html } from "lit";
import { SakaiDashboardWidget } from "./SakaiDashboardWidget.js";

export class SakaiStatusWidget extends SakaiDashboardWidget {

  constructor() {

    super();

    this.title = "Status";
  }

  content() {

    return html`
      This is the status widget
    `;
  }
}
