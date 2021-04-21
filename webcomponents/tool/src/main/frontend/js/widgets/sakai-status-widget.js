import { html } from "../assets/lit-element/lit-element.js";
import '../sakai-icon.js';
import { SakaiDashboardWidget } from './sakai-dashboard-widget.js';

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

if (!customElements.get("sakai-status-widget")) {
  customElements.define("sakai-status-widget", SakaiStatusWidget);
}
