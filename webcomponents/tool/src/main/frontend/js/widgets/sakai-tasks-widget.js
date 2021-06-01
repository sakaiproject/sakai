import { html } from "../assets/lit-element/lit-element.js";
import { ifDefined } from "../assets/lit-html/directives/if-defined.js";
import { SakaiDashboardWidget } from './sakai-dashboard-widget.js';
import '../tasks/sakai-tasks.js';

export class SakaiTasksWidget extends SakaiDashboardWidget {

  constructor() {

    super();
    this.widgetId = "tasks";
    this.title = "Tasks";
    this.loadTranslations("tasks");
  }

  content() {

    return html`
      <sakai-tasks
        user-id="${ifDefined(this.userId ? this.userId : undefined)}"
      >
    `;
  }
}

if (!customElements.get("sakai-tasks-widget")) {
  customElements.define("sakai-tasks-widget", SakaiTasksWidget);
}
