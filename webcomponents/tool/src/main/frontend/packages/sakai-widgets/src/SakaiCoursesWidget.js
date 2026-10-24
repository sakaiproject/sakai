import { html } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";
import "@sakai-ui/sakai-course-list/sakai-course-list.js";
import { SakaiDashboardWidget } from "./SakaiDashboardWidget.js";

export class SakaiCoursesWidget extends SakaiDashboardWidget {

  static properties = {
    savedFilters: { attribute: false, type: Object },
  };

  constructor() {

    super();

    this.widgetId = "courses";
    this.hasFilters = true;
  }

  content() {

    return html`
      <sakai-course-list
        user-id="${ifDefined(this.userId)}"
        ?save-filters=${Boolean(this.userId)}
        ?filters-open=${this.filtersOpen}
        .savedFilters=${this.savedFilters}
        @filters-closed=${this._closeFilters}>
      </sakai-course-list>
    `;
  }
}
