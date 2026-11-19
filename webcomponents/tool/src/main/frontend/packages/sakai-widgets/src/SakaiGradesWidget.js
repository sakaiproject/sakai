import { html } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";
import { SakaiDashboardWidget } from "./SakaiDashboardWidget.js";
import "@sakai-ui/sakai-grades/sakai-grades.js";

export class SakaiGradesWidget extends SakaiDashboardWidget {

  static properties = {
    revealOnClick: { attribute: "reveal-on-click", type: Boolean },
    savedFilters: { attribute: false, type: Object },
  };

  constructor() {

    super();

    this.widgetId = "grades";
    this.revealOnClick = true;
    this.hasFilters = true;
  }

  content() {

    return html`

      <sakai-grades
        user-id="${ifDefined(this.userId ? this.userId : undefined)}"
        site-id="${ifDefined(this.siteId ? this.siteId : undefined)}"
        page-size="${this._pageSize()}"
        ?secret=${this.revealOnClick !== false}
        ?save-filters=${Boolean(this.userId) && !this.siteId}
        ?filters-open=${this.filtersOpen}
        .savedFilters=${this.savedFilters}
        @filters-closed=${this._closeFilters}
      >
      </sakai-grades>
    `;
  }
}

SakaiGradesWidget.roles = [ "instructor" ];
