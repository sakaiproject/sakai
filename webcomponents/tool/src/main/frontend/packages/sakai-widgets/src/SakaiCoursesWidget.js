import { html } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";
import "@sakai-ui/sakai-course-list/sakai-course-list.js";
import { SakaiDashboardWidget } from "./SakaiDashboardWidget.js";

export class SakaiCoursesWidget extends SakaiDashboardWidget {

  constructor() {

    super();

    this.widgetId = "courses";
  }

  content() {

    return html`
      <sakai-course-list
        user-id="${ifDefined(this.userId ? this.userId : undefined)}">
      </sakai-course-list>
    `;
  }
}
