import { html, nothing } from "lit";
import { SakaiElement } from "@sakai-ui/sakai-element";
import { ifDefined } from "lit/directives/if-defined.js";
import { unsafeHTML } from "lit/directives/unsafe-html.js";
import "@sakai-ui/sakai-icon/sakai-icon.js";
import "@sakai-ui/sakai-course-list/sakai-course-list.js";
import "@sakai-ui/sakai-widgets";
import "@sakai-ui/sakai-widgets/sakai-widget-panel.js";
import "@sakai-ui/sakai-button/sakai-button.js";

export class SakaiHomeDashboard extends SakaiElement {

  static properties = {

    courses: { type: Array },
    userId: { attribute: "user-id", type: String },
    showSites: { attribute: "show-sites", type: Boolean },
    _data: { state: true },
    _showMotd: { state: true },
    _editing: { state: true },
  };

  constructor() {

    super();

    this.showSites = true;

    this.loadTranslations("dashboard");
  }

  set userId(value) {

    this._userId = value;
    this._loadData();
  }

  get userId() { return this._userId; }

  _loadData() {

    const url = `/api/users/${this.userId}/dashboard`;
    fetch(url, { credentials: "include" })
      .then(r => {

        if (r.ok) {
          return r.json();
        }
        throw new Error(`Failed to get dashboard data from ${url}`);

      })
      .then(r => {

        this._data = r;
        this._showMotd = this._data.motd;
      })
      .catch(error => console.error(error));
  }

  widgetLayoutChange(e) {
    this._data.layout = e.detail.layout;
  }

  edit() {

    this._editing = !this._editing;
    this.layoutBackup = [ ...this._data.layout ];
  }

  cancel() {

    this._editing = false;
    this._data.layout = [ ...this.layoutBackup ];
    this.requestUpdate();
  }

  save() {

    this._editing = !this._editing;

    const url = `/api/users/${this.userId}/dashboard`;
    fetch(url, {
      method: "PUT",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ layout: this._data.layout }),
    }).then(r => {

      if (!r.ok) {
        throw new Error(`Failed to update dashboard for url ${url}`);
      }
    }).catch(error => console.error(error.message));
  }

  _toggleMotd() {
    this._showMotd = !this._showMotd;
  }

  shouldUpdate() {
    return this._i18n && this._data;
  }

  render() {

    return html`

      <div>
        <div class="d-lg-flex flex-wrap align-items-center justify-content-between mb-4">
          <h2 class="my-2">${this._i18n.welcome} ${this._data.givenName}</h2>
          <div class="d-flex mb-2 mb-lg-0">
          ${this._editing ? html`
            <div class="me-1">
              <sakai-button @click=${this.save} title="${this._i18n.save_tooltip}" aria-label="${this._i18n.save_tooltip}">${this._i18n.save}</sakai-button>
            </div>
            <div>
              <sakai-button @click=${this.cancel} title="${this._i18n.cancel_tooltip}" aria-label="${this._i18n.cancel_tooltip}">${this._i18n.cancel}</sakai-button>
            </div>
          ` : html`
            ${this._data.worksiteSetupUrl ? html`
              <div class="me-1">
                <sakai-button href="${this._data.worksiteSetupUrl}" title="${this._i18n.worksite_setup_tooltip}" aria-label="${this._i18n.worksite_setup_tooltip}">
                  <div class="d-flex justify-content-between text-center">
                    <div><sakai-icon type="add" size="small" class="me-3"></sakai-icon></div>
                    <div>${this._i18n.worksite_setup}</div>
                  </div>
                </sakai-button>
              </div>
            ` : nothing}
            <div>
              <sakai-button slot="invoker" @click=${this.edit} title="${this._i18n.edit_tooltip}" arial-label="${this._i18n.edit_tooltip}">${this._i18n.edit}</sakai-button>
            </div>
          `}
          </div>
        </div>
        ${this._data.motd ? html`
          <div class="p-3 mt-2 mb-3 border border-1 rounded-1 fs-5 fw-normal">
            <div class="d-flex mb-4 align-items-center" @click=${this._toggleMotd}>
              <div class="me-3">${this._i18n.motd}</div>
              <div>
                <a href="javascript:;"
                  title="${this._showMotd ? this._i18n.hide_motd_tooltip : this._i18n.show_motd_tooltip}"
                  aria-label="${this._showMotd ? this._i18n.hide_motd_tooltip : this._i18n.show_motd_tooltip}">
                  <sakai-icon type="${this._showMotd ? "up" : "down"}" size="small"></sakai-icon>
                </a>
              </div>
            </div>
            <div class="mt-4 ps-4 fs-6" style="display: ${this._showMotd ? "block" : "none"}">${unsafeHTML(this._data.motd)}</div>
          </div>
        ` : nothing}
        <div class="d-lg-flex">
          ${this.showSites ? html`
            <div class="me-lg-3 pe-lg-3 mb-4 mb-lg-0 sakai-course-list">
              <sakai-course-list user-id="${this.userId}"></sakai-course-list>
            </div>
          ` : nothing}
          <div class="w-100">
            <sakai-widget-panel
              @changed=${this.widgetLayoutChange}
              .widgetIds=${this._data.widgets}
              .layout=${this._data.layout}
              site-id=""
              user-id="${ifDefined(this.userId)}"
              ?editing=${this._editing}>
            </sakai-widget-panel>
          </div>
        </div>
      </div>
    `;
  }
}
