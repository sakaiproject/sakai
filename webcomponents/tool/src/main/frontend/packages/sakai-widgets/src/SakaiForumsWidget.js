import { html } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";
import "@sakai-ui/sakai-forums/sakai-forums.js";
import { SakaiDashboardWidget } from "./SakaiDashboardWidget.js";

export class SakaiForumsWidget extends SakaiDashboardWidget {

  static properties = {
    savedFilters: { attribute: false, type: Object },
  };

  constructor() {

    super();

    this.widgetId = "forums";
    this.hasFilters = true;
  }

  content() {

    return html`
      <sakai-forums
        user-id="${ifDefined(this.userId ? this.userId : undefined)}"
        site-id="${ifDefined(this.siteId ? this.siteId : undefined)}"
        page-size="${this._pageSize()}"
        ?save-filters=${Boolean(this.userId)}
        ?filters-open=${this.filtersOpen}
        .savedFilters=${this.savedFilters}
        @filters-closed=${this._closeFilters}
      >
      </sakai-forums>
    `;
  }
}
