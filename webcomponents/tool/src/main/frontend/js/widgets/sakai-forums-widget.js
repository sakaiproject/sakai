import { html } from "../assets/lit-element/lit-element.js";
import { ifDefined } from "../assets/lit-html/directives/if-defined.js";
import "../forums/sakai-forums.js";
import { SakaiDashboardWidget } from './sakai-dashboard-widget.js';

export class SakaiForumsWidget extends SakaiDashboardWidget {

  constructor() {

    super();
    this.widgetId = "forums";
    this.loadTranslations({bundle: "org.sakaiproject.api.app.messagecenter.bundle.Messages"});
  }

  content() {

    return html`
      <sakai-forums
        user-id="${ifDefined(this.userId ? this.userId : undefined)}"
        site-id="${ifDefined(this.siteId ? this.siteId : undefined)}"
      >
    `;
  }
}

if (!customElements.get("sakai-forums-widget")) {
  customElements.define("sakai-forums-widget", SakaiForumsWidget);
}
