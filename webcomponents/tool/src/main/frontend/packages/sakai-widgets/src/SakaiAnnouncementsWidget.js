import { html } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";
import "@sakai-ui/sakai-announcements/sakai-announcements.js";
import { SakaiDashboardWidget } from "./SakaiDashboardWidget.js";

export class SakaiAnnouncementsWidget extends SakaiDashboardWidget {

  static properties = {
    savedFilters: { attribute: false, type: Object },
  };

  constructor() {

    super();

    this.widgetId = "announcements";
    this.hasFilters = true;
  }

  content() {

    return html`
      <sakai-announcements
        user-id="${ifDefined(this.userId ? this.userId : undefined)}"
        site-id="${ifDefined(this.siteId ? this.siteId : undefined)}"
        page-size="${this._pageSize()}"
        ?save-filters=${Boolean(this.userId) && !this.siteId}
        ?filters-open=${this.filtersOpen}
        .savedFilters=${this.savedFilters}
        @filters-closed=${this._closeFilters}
      >
      </sakai-announcements>
    `;
  }
}
